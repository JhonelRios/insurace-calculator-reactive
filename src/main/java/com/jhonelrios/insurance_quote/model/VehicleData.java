package com.jhonelrios.insurance_quote.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VehicleData {
    private String brand;
    private String model;
    private int year;
    private String usageType;
    private int driverAge;
}
