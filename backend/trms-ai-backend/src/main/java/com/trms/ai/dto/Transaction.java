package com.trms.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Transaction DTO for TRMS legacy system integration
 */
public record Transaction(
    @JsonProperty("id") String id,
    @JsonProperty("fromAccount") String fromAccount,
    @JsonProperty("toAccount") String toAccount,
    @JsonProperty("amount") Double amount,
    @JsonProperty("currency") String currency,
    @JsonProperty("status") String status,
    @JsonProperty("transactionDate") String transactionDate,
    @JsonProperty("description") String description,
    @JsonProperty("reference") String reference
) {}