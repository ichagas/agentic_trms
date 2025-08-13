package com.trms.mock.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RateReset {
    
    @NotBlank(message = "Instrument ID is required")
    private String instrumentId;
    
    @NotBlank(message = "Index name is required")
    private String indexName;
    
    @NotNull(message = "Fixing date is required")
    private LocalDate fixingDate;
    
    @NotNull(message = "Notional is required")
    private BigDecimal notional;
    
    @NotBlank(message = "Currency is required")
    private String currency;
    
    private BigDecimal proposedRate;
    private BigDecimal currentRate;
    private RateStatus status;
    private String tenor;
    private LocalDateTime createdAt;
    private LocalDateTime approvedAt;
    private String approvedBy;
    private String source;
    private String description;
    
    public enum RateStatus {
        MISSING,
        PROPOSED,
        APPROVED,
        REJECTED,
        APPLIED
    }
}