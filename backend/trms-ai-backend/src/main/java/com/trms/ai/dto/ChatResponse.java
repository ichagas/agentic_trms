package com.trms.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Chat response DTO for AI interactions
 */
public record ChatResponse(
    @JsonProperty("message") String message,
    @JsonProperty("sessionId") String sessionId,
    @JsonProperty("timestamp") String timestamp,
    @JsonProperty("success") boolean success,
    @JsonProperty("error") String error
) {
    
    /**
     * Create a successful chat response
     */
    public static ChatResponse success(String message, String sessionId, String timestamp) {
        return new ChatResponse(message, sessionId, timestamp, true, null);
    }
    
    /**
     * Create an error chat response
     */
    public static ChatResponse error(String error, String sessionId, String timestamp) {
        return new ChatResponse(null, sessionId, timestamp, false, error);
    }
}