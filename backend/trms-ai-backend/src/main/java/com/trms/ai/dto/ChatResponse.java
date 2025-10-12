package com.trms.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Chat response DTO for AI interactions
 */
public record ChatResponse(
    @JsonProperty("message") String message,
    @JsonProperty("sessionId") String sessionId,
    @JsonProperty("timestamp") String timestamp,
    @JsonProperty("success") boolean success,
    @JsonProperty("error") String error,
    @JsonProperty("metadata") Metadata metadata
) {

    /**
     * Metadata about the AI response
     */
    public record Metadata(
        @JsonProperty("functionCalls") List<String> functionCalls
    ) {}

    /**
     * Create a successful chat response with metadata
     */
    public static ChatResponse success(String message, String sessionId, String timestamp, List<String> functionCalls) {
        Metadata metadata = new Metadata(functionCalls);
        return new ChatResponse(message, sessionId, timestamp, true, null, metadata);
    }

    /**
     * Create a successful chat response without metadata (backward compatible)
     */
    public static ChatResponse success(String message, String sessionId, String timestamp) {
        return new ChatResponse(message, sessionId, timestamp, true, null, null);
    }

    /**
     * Create an error chat response
     */
    public static ChatResponse error(String error, String sessionId, String timestamp) {
        return new ChatResponse(null, sessionId, timestamp, false, error, null);
    }
}