package com.jhonelrios.insurance_quote.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VehicleData {
    @NotBlank
    private String brand;
    @NotBlank
    private String model;
    @NotNull
    private Integer year;
    @NotNull
    private UsageType usageType;
    @Min(18)
    private int driverAge;
}
