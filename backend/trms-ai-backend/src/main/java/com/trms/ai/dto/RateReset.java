package com.trms.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Rate Reset DTO for TRMS legacy system integration
 */
public record RateReset(
    @JsonProperty("instrumentId") String instrumentId,
    @JsonProperty("indexName") String indexName,
    @JsonProperty("currency") String currency,
    @JsonProperty("tenor") String tenor,
    @JsonProperty("resetDate") String resetDate,
    @JsonProperty("fixingDate") String fixingDate,
    @JsonProperty("notional") Double notional,
    @JsonProperty("currentRate") Double currentRate,
    @JsonProperty("proposedRate") Double proposedRate,
    @JsonProperty("source") String source,
    @JsonProperty("status") String status,
    @JsonProperty("description") String description
) {}