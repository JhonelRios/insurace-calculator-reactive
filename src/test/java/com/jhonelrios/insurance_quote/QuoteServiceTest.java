package com.jhonelrios.insurance_quote;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jhonelrios.insurance_quote.model.Quote;
import com.jhonelrios.insurance_quote.dto.UsageType;
import com.jhonelrios.insurance_quote.dto.VehicleDTO;
import com.jhonelrios.insurance_quote.repository.QuoteRepository;
import com.jhonelrios.insurance_quote.service.QuoteServiceImpl;
import com.jhonelrios.insurance_quote.util.RedisSerializationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class QuoteServiceTest {
    @Mock
    private QuoteRepository quoteRepository;

    @Mock
    private ReactiveStringRedisTemplate redisTemplate;

    @Mock
    private ReactiveValueOperations<String, String> valueOps;

    @Mock
    private RedisSerializationUtil redisSerializationUtil;

    @InjectMocks
    private QuoteServiceImpl quoteService;

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        quoteService = new QuoteServiceImpl(quoteRepository, redisTemplate, redisSerializationUtil);

        when(redisTemplate.opsForValue()).thenReturn(valueOps);
    }

    @Test
    void shouldCalculateQuoteWhenCacheIsEmpty() throws JsonProcessingException {
        VehicleDTO data = new VehicleDTO("BMW", "X5", 2018, UsageType.CARGA, 55);

        when(valueOps.get(anyString())).thenReturn(Mono.empty());

        BigDecimal base = new BigDecimal("500.00");
        BigDecimal adjustment = base.multiply(new BigDecimal("0.4"));
        BigDecimal total = base.add(adjustment);

        Quote expected = new Quote(null, data.getBrand(), data.getModel(), data.getYear(),
                data.getUsageType(), data.getDriverAge(), base, adjustment, total, LocalDateTime.now());

        when(quoteRepository.save(any(Quote.class))).thenAnswer(invocation -> {
            Quote quote = invocation.getArgument(0);
            quote.setId(UUID.randomUUID());
            return Mono.just(quote);
        });

        String serializedQuoteJson = objectMapper.writeValueAsString(expected);
        when(redisSerializationUtil.serializeQuote(any(Quote.class)))
                .thenReturn(Mono.just(serializedQuoteJson));

        when(valueOps.set(anyString(), anyString(), any())).thenReturn(Mono.just(true));

        StepVerifier.create(quoteService.calculateQuote(data))
                .assertNext(quote -> {
                    assert quote.getBrand().equals("BMW");
                    assert quote.getTotalPremium().compareTo(total) == 0;
                })
                .verifyComplete();

        verify(quoteRepository, times(1)).save(any());
        verify(valueOps, times(1)).set(anyString(), anyString(), any());
    }

    @Test
    void shouldReturnQuoteFromCache() throws JsonProcessingException {
        VehicleDTO data = new VehicleDTO("Audi", "A4", 2020, UsageType.PERSONAL, 40);
        Quote cachedQuote = new Quote(
                UUID.randomUUID(),
                data.getBrand(),
                data.getModel(),
                data.getYear(),
                data.getUsageType(),
                data.getDriverAge(),
                new BigDecimal("500.00"),
                new BigDecimal("50.00"),
                new BigDecimal("550.00"),
                LocalDateTime.now()
        );

        String jsonQuote = objectMapper.writeValueAsString(cachedQuote);

        when(valueOps.get(anyString())).thenReturn(Mono.just(jsonQuote));

        when(redisSerializationUtil.deserializeQuote(anyString()))
                .thenReturn(Mono.just(cachedQuote));

        StepVerifier.create(quoteService.calculateQuote(data))
                .expectNextMatches(quote ->
                        quote.getBrand().equals("Audi") && quote.getTotalPremium().compareTo(new BigDecimal("550.00")) == 0
                )
                .verifyComplete();

        verify(quoteRepository, never()).save(any());
        verify(valueOps, times(1)).get(anyString());
    }
}
