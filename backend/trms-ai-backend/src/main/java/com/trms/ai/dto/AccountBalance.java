package com.trms.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Account Balance DTO for TRMS legacy system integration
 */
public record AccountBalance(
    @JsonProperty("accountId") String accountId,
    @JsonProperty("currentBalance") Double currentBalance,
    @JsonProperty("availableBalance") Double availableBalance,
    @JsonProperty("reservedBalance") Double reservedBalance,
    @JsonProperty("currency") String currency,
    @JsonProperty("lastUpdated") String lastUpdated,
    @JsonProperty("pendingCredits") Double pendingCredits,
    @JsonProperty("pendingDebits") Double pendingDebits,
    @JsonProperty("overdraftLimit") Double overdraftLimit,
    @JsonProperty("valueDate") String valueDate
) {
    // Helper method for backward compatibility
    public Double balance() {
        return currentBalance;
    }
}