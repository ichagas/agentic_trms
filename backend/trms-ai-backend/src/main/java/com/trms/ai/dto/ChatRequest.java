package com.trms.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

/**
 * Chat request DTO for AI interactions
 */
public record ChatRequest(
    @JsonProperty("message") @NotBlank String message,
    @JsonProperty("sessionId") String sessionId
) {}