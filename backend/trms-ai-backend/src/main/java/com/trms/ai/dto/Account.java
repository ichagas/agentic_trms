package com.trms.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Account DTO for TRMS legacy system integration
 */
public record Account(
    @JsonProperty("id") String id,
    @JsonProperty("accountNumber") String accountNumber,
    @JsonProperty("accountName") String accountName,
    @JsonProperty("currency") String currency,
    @JsonProperty("accountType") String accountType,
    @JsonProperty("status") String status,
    @JsonProperty("balance") Double balance
) {}