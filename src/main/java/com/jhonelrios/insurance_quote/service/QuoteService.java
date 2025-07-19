package com.jhonelrios.insurance_quote.service;

import com.jhonelrios.insurance_quote.model.Quote;
import com.jhonelrios.insurance_quote.model.VehicleData;
import com.jhonelrios.insurance_quote.repository.QuoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QuoteService {
    private final QuoteRepository quoteRepository;

    public Mono<Quote> calculateQuote(VehicleData data) {
        BigDecimal base = new BigDecimal("500.00");
        BigDecimal adjustment = calculateAdjustment(data, base);
        BigDecimal total = base.add(adjustment);

        System.out.printf("adjustment: %f%n", adjustment);
        System.out.printf("base: %f%n", base);
        System.out.printf("total: %f%n", total);

        Quote quote = new Quote(
                null, data.getBrand(), data.getModel(), data.getYear(), data.getUsageType(),
                data.getDriverAge(), base, adjustment, total, LocalDateTime.now()
        );

        return quoteRepository.save(quote);
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
}
