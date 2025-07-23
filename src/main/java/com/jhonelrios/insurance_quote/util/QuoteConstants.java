package com.jhonelrios.insurance_quote.util;

import java.math.BigDecimal;
import java.time.Duration;

public final class QuoteConstants {
    private QuoteConstants() {}

    // Premium
    public static final BigDecimal BASE_PREMIUM = new BigDecimal("500.00");

    // Cache configuration
    public static final Duration CACHE_DURATION = Duration.ofMinutes(5);

    // Adjustment Factors
    public static final int YEAR_THRESHOLD = 2015;
    public static final BigDecimal YEAR_ADJUSTMENT_FACTOR = BigDecimal.valueOf(0.15);

    public static final BigDecimal CARGA_USAGE_ADJUSTMENT_FACTOR = BigDecimal.valueOf(0.1);

    public static final int DRIVER_AGE_THRESHOLD = 50;
    public static final BigDecimal DRIVER_AGE_ADJUSTMENT_FACTOR = BigDecimal.valueOf(0.05);

    // Brand Adjustment Factors
    public static final BigDecimal BMW_ADJUSTMENT_FACTOR = BigDecimal.valueOf(0.2);
    public static final BigDecimal AUDI_ADJUSTMENT_FACTOR = BigDecimal.valueOf(0.1);

    // Brand Names (in this case)
    public static final String BRAND_BMW = "bmw";
    public static final String BRAND_AUDI = "audi";
}
