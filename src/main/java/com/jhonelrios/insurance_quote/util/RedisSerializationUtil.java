package com.jhonelrios.insurance_quote.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jhonelrios.insurance_quote.model.Quote;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisSerializationUtil {
    private final ObjectMapper objectMapper;

    public Mono<String> serializeQuote(Quote quote) {
        try {
            return Mono.just(objectMapper.writeValueAsString(quote));
        } catch (JsonProcessingException e) {
            return Mono.error(new RuntimeException("Failed to serialize Quote for Redis", e));
        }
    }

    public Mono<Quote> deserializeQuote(String json) {
        try {
            return Mono.just(objectMapper.readValue(json, Quote.class));
        } catch (JsonProcessingException e) {
            return Mono.error(new RuntimeException("Failed to deserialize Quote from Redis", e));
        }
    }
}
