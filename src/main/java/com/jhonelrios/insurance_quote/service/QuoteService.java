package com.jhonelrios.insurance_quote.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jhonelrios.insurance_quote.model.Quote;
import com.jhonelrios.insurance_quote.dto.UsageType;
import com.jhonelrios.insurance_quote.dto.VehicleDTO;
import com.jhonelrios.insurance_quote.repository.QuoteRepository;
import com.jhonelrios.insurance_quote.util.QuoteConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

import static com.jhonelrios.insurance_quote.util.QuoteConstants.*;


@Slf4j
@Service
@RequiredArgsConstructor
public class QuoteService {
    private final QuoteRepository quoteRepository;
    private final ReactiveStringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public Mono<Quote> calculateQuote(VehicleDTO data) {
        String key = generateKey(data);
        log.info("Calculating quote for: {}", data);
        log.debug("Generated cache key: {}", key);

        return redisTemplate.opsForValue().get(key)
                .doOnNext(value -> log.info("Cache found for key: {}", key))
                .flatMap(this::deserializeQuote)
                .switchIfEmpty(Mono.defer(() -> {
                    log.info("No cached quote found. Calculating new quote.");

                    BigDecimal base = BASE_PREMIUM;
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
                                    .flatMap(json -> redisTemplate.opsForValue().set(key, json, CACHE_DURATION)
                                            .doOnSuccess(set -> log.info("Quote cached with key: {}", key)))
                                    .thenReturn(saved));
                }));
    }

    private BigDecimal calculateAdjustment(VehicleDTO data, BigDecimal base) {
        BigDecimal adjustment = BigDecimal.ZERO;

        if (data.getYear() > YEAR_THRESHOLD) adjustment = adjustment.add(base.multiply(YEAR_ADJUSTMENT_FACTOR));
        if (data.getUsageType() == UsageType.CARGA) adjustment = adjustment.add(base.multiply(CARGA_USAGE_ADJUSTMENT_FACTOR));
        if (data.getDriverAge() > DRIVER_AGE_THRESHOLD) adjustment = adjustment.subtract(base.multiply(DRIVER_AGE_ADJUSTMENT_FACTOR));

        switch (data.getBrand().toLowerCase()) {
            case BRAND_BMW -> adjustment = adjustment.add(base.multiply(BMW_ADJUSTMENT_FACTOR));
            case BRAND_AUDI -> adjustment = adjustment.add(base.multiply(AUDI_ADJUSTMENT_FACTOR));
        }

        return adjustment;
    }

    private String generateKey(VehicleDTO data) {
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
