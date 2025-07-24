package com.jhonelrios.insurance_quote.service;

import com.jhonelrios.insurance_quote.dto.VehicleDTO;
import com.jhonelrios.insurance_quote.model.Quote;
import reactor.core.publisher.Mono;

public interface QuoteService {
    Mono<Quote> calculateQuote(VehicleDTO data);
}
