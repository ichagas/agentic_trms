package com.trms.mock.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account {
    
    @NotBlank(message = "Account ID is required")
    private String accountId;
    
    @NotBlank(message = "Account name is required")
    private String accountName;
    
    @NotBlank(message = "Currency is required")
    private String currency;
    
    @NotNull(message = "Account type is required")
    private AccountType accountType;
    
    @NotNull(message = "Status is required")
    private AccountStatus status;
    
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdated;
    
    public enum AccountType {
        CASH,
        TRADING,
        SETTLEMENT,
        NOSTRO,
        VOSTRO,
        COLLATERAL
    }
    
    public enum AccountStatus {
        ACTIVE,
        INACTIVE,
        FROZEN,
        CLOSED
    }
    
}