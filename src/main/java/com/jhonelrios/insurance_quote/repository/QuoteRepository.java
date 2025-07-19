package com.jhonelrios.insurance_quote.repository;

import com.jhonelrios.insurance_quote.model.Quote;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface QuoteRepository extends ReactiveCrudRepository<Quote, UUID> {
}
