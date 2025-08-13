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
public class AccountBalance {
    
    @NotBlank(message = "Account ID is required")
    private String accountId;
    
    @NotNull(message = "Available balance is required")
    private BigDecimal availableBalance;
    
    @NotNull(message = "Current balance is required")
    private BigDecimal currentBalance;
    
    @NotNull(message = "Reserved balance is required")
    private BigDecimal reservedBalance;
    
    @NotBlank(message = "Currency is required")
    private String currency;
    
    @NotNull(message = "Last updated timestamp is required")
    private LocalDateTime lastUpdated;
    
    private BigDecimal pendingCredits;
    private BigDecimal pendingDebits;
    private BigDecimal overdraftLimit;
    private LocalDate valueDate;
}