package com.trms.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Request DTO for creating transactions in TRMS legacy system
 */
public record CreateTransactionRequest(
    @JsonProperty("fromAccount") @NotNull String fromAccount,
    @JsonProperty("toAccount") @NotNull String toAccount,
    @JsonProperty("amount") @NotNull @Positive Double amount,
    @JsonProperty("currency") @NotNull String currency,
    @JsonProperty("description") String description
) {}