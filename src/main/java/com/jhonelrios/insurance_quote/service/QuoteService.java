package com.jhonelrios.insurance_quote.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jhonelrios.insurance_quote.model.Quote;
import com.jhonelrios.insurance_quote.model.VehicleData;
import com.jhonelrios.insurance_quote.repository.QuoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QuoteService {
    private final QuoteRepository quoteRepository;
    private final ReactiveStringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public Mono<Quote> calculateQuote(VehicleData data) {
        String key = generateKey(data);

        return redisTemplate.opsForValue().get(key)
                .flatMap(this::deserializeQuote)
                .switchIfEmpty(Mono.defer(() -> {
                    BigDecimal base = new BigDecimal("500.00");
                    BigDecimal adjustment = calculateAdjustment(data, base);
                    BigDecimal total = base.add(adjustment);

                    Quote quote = new Quote(
                            null, data.getBrand(), data.getModel(), data.getYear(), data.getUsageType(),
                            data.getDriverAge(), base, adjustment, total, LocalDateTime.now()
                    );

                    return quoteRepository.save(quote)
                            .flatMap(saved -> serializeQuote(saved)
                                    .flatMap(json -> redisTemplate.opsForValue().set(key, json, Duration.ofMinutes(5)))
                                    .thenReturn(saved));
                }));
    }

    private BigDecimal calculateAdjustment(VehicleData data, BigDecimal base) {
        BigDecimal adjustment = BigDecimal.ZERO;

        if (data.getYear() > 2015) adjustment = adjustment.add(base.multiply(BigDecimal.valueOf(0.15)));
        if ("carga".equalsIgnoreCase(data.getUsageType())) adjustment = adjustment.add(base.multiply(BigDecimal.valueOf(0.1)));
        if (data.getDriverAge() > 50) adjustment = adjustment.subtract(base.multiply(BigDecimal.valueOf(0.05)));
        if ("bmw".equalsIgnoreCase(data.getBrand())) adjustment = adjustment.add(base.multiply(BigDecimal.valueOf(0.2)));
        if ("audi".equalsIgnoreCase(data.getBrand())) adjustment = adjustment.add(base.multiply(BigDecimal.valueOf(0.1)));

        return adjustment;
    }

    private String generateKey(VehicleData data) {
        return "quote:" + data.getBrand() + ":" + data.getModel() + ":" + data.getYear() + ":" + data.getUsageType() + ":" + data.getDriverAge();
    }

    private Mono<String> serializeQuote(Quote quote) {
        try {
            return Mono.just(objectMapper.writeValueAsString(quote));
        } catch (JsonProcessingException e) {
            return Mono.error(e);
        }
    }

    private Mono<Quote> deserializeQuote(String json) {
        try {
            return Mono.just(objectMapper.readValue(json, Quote.class));
        } catch (JsonProcessingException e) {
            return Mono.error(e);
        }
    }
}
