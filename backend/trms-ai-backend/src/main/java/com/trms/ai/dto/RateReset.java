package com.trms.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Rate Reset DTO for TRMS legacy system integration
 */
public record RateReset(
    @JsonProperty("currency") String currency,
    @JsonProperty("tenor") String tenor,
    @JsonProperty("resetDate") String resetDate,
    @JsonProperty("currentRate") Double currentRate,
    @JsonProperty("proposedRate") Double proposedRate,
    @JsonProperty("source") String source,
    @JsonProperty("status") String status
) {}