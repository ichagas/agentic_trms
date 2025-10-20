package com.trms.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Account DTO for TRMS legacy system integration
 */
public record Account(
    @JsonProperty("accountId") String accountId,
    @JsonProperty("accountName") String accountName,
    @JsonProperty("currency") String currency,
    @JsonProperty("accountType") String accountType,
    @JsonProperty("status") String status,
    @JsonProperty("description") String description,
    @JsonProperty("createdAt") String createdAt,
    @JsonProperty("lastUpdated") String lastUpdated
) {}