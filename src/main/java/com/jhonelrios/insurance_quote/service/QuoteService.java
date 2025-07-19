package com.jhonelrios.insurance_quote.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jhonelrios.insurance_quote.model.Quote;
import com.jhonelrios.insurance_quote.model.UsageType;
import com.jhonelrios.insurance_quote.model.VehicleData;
import com.jhonelrios.insurance_quote.repository.QuoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuoteService {
    private final QuoteRepository quoteRepository;
    private final ReactiveStringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public Mono<Quote> calculateQuote(VehicleData data) {
        String key = generateKey(data);
        log.info("Calculating quote for: {}", data);
        log.debug("Generated cache key: {}", key);

        return redisTemplate.opsForValue().get(key)
                .doOnNext(value -> log.info("Cache found for key: {}", key))
                .flatMap(this::deserializeQuote)
                .switchIfEmpty(Mono.defer(() -> {
                    log.info("No cached quote found. Calculating new quote.");

                    BigDecimal base = new BigDecimal("500.00");
                    BigDecimal adjustment = calculateAdjustment(data, base);
                    BigDecimal total = base.add(adjustment);

                    log.debug("Base: {} | Adjustment: {} | Total: {}", base, adjustment, total);

                    Quote quote = new Quote(
                            null, data.getBrand(), data.getModel(), data.getYear(), data.getUsageType(),
                            data.getDriverAge(), base, adjustment, total, LocalDateTime.now()
                    );

                    return quoteRepository.save(quote)
                            .doOnSuccess(saved -> log.info("Quote saved with ID: {}", saved.getId()))
                            .doOnError(e -> log.error("Error saving quote in database", e))
                            .flatMap(saved -> serializeQuote(saved)
                                    .flatMap(json -> redisTemplate.opsForValue().set(key, json, Duration.ofMinutes(5))
                                            .doOnSuccess(set -> log.info("Quote cached for 5 min with key: {}", key)))
                                    .thenReturn(saved));
                }));
    }

    private BigDecimal calculateAdjustment(VehicleData data, BigDecimal base) {
        BigDecimal adjustment = BigDecimal.ZERO;

        if (data.getYear() > 2015) adjustment = adjustment.add(base.multiply(BigDecimal.valueOf(0.15)));
        if (data.getUsageType() == UsageType.CARGA) adjustment = adjustment.add(base.multiply(BigDecimal.valueOf(0.1)));
        if (data.getDriverAge() > 50) adjustment = adjustment.subtract(base.multiply(BigDecimal.valueOf(0.05)));

        switch (data.getBrand().toLowerCase()) {
            case "bmw" -> adjustment = adjustment.add(base.multiply(BigDecimal.valueOf(0.2)));
            case "audi" -> adjustment = adjustment.add(base.multiply(BigDecimal.valueOf(0.1)));
        }

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
