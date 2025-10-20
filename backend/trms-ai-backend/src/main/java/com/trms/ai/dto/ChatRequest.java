package com.trms.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

/**
 * Chat request DTO for AI interactions
 */
public record ChatRequest(
    @JsonProperty("message") @NotBlank String message,
    @JsonProperty("sessionId") String sessionId,
    @JsonProperty("experimentalMode") Boolean experimentalMode
) {
    /**
     * Constructor with default experimentalMode
     */
    public ChatRequest {
        // Default experimentalMode to false if not provided
        if (experimentalMode == null) {
            experimentalMode = false;
        }
    }
}