package com.trms.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Account Balance DTO for TRMS legacy system integration
 */
public record AccountBalance(
    @JsonProperty("accountId") String accountId,
    @JsonProperty("balance") Double balance,
    @JsonProperty("currency") String currency,
    @JsonProperty("availableBalance") Double availableBalance,
    @JsonProperty("pendingTransactions") Double pendingTransactions,
    @JsonProperty("lastUpdated") String lastUpdated
) {}