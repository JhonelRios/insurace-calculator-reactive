package com.jhonelrios.insurance_quote.controller;

import com.jhonelrios.insurance_quote.model.Quote;
import com.jhonelrios.insurance_quote.model.VehicleData;
import com.jhonelrios.insurance_quote.service.QuoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/quotes")
@RequiredArgsConstructor
public class QuoteController {
    private final QuoteService quoteService;

    @PostMapping
    public Mono<Quote> calculate(@RequestBody @Valid VehicleData data) {
        return quoteService.calculateQuote(data);
    }
}
