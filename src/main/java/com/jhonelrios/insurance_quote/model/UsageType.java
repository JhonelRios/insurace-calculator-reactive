package com.jhonelrios.insurance_quote.model;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Arrays;

public enum UsageType {
    PERSONAL,
    TRABAJO,
    CARGA;

    @JsonCreator
    public static UsageType fromString(String value) {
        return Arrays.stream(UsageType.values())
                .filter(e -> e.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid usage type: " + value));
    }
}
