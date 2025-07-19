package com.jhonelrios.insurance_quote.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table("quotes")
public class Quote {
    @Id
    private UUID id;
    private String brand;
    private String model;
    private int year;
    private UsageType usageType;
    private int driverAge;
    private BigDecimal basePremium;
    private BigDecimal adjustmentAmount;
    private BigDecimal totalPremium;
    private LocalDateTime createdAt;
}
