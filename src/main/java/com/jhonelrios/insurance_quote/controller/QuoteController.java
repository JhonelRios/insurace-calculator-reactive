package com.jhonelrios.insurance_quote.controller;

import com.jhonelrios.insurance_quote.model.Quote;
import com.jhonelrios.insurance_quote.dto.VehicleDTO;
import com.jhonelrios.insurance_quote.service.QuoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/quotes")
@RequiredArgsConstructor
public class QuoteController {
    private final QuoteService quoteService;

    @PostMapping
    public Mono<Quote> calculate(@RequestBody @Valid VehicleDTO data) {
        return quoteService.calculateQuote(data)
                .doOnSuccess(quote -> log.info("Quote calculated: {}", quote));
    }
}
