package com.trms.ai.model;

import java.time.LocalDateTime;

/**
 * Represents a single message in a conversation
 *
 * Tracks individual messages exchanged between user and AI assistant,
 * including timestamps and function calls for context management.
 */
public class ConversationMessage {

    /**
     * Role of the message sender
     */
    public enum Role {
        USER,       // Message from user
        ASSISTANT,  // Message from AI assistant
        SYSTEM      // System-level message (prompts, instructions)
    }

    private final Role role;
    private final String content;
    private final LocalDateTime timestamp;
    private final String functionCalls; // Optional: track which functions were called

    public ConversationMessage(Role role, String content) {
        this.role = role;
        this.content = content;
        this.timestamp = LocalDateTime.now();
        this.functionCalls = null;
    }

    public ConversationMessage(Role role, String content, String functionCalls) {
        this.role = role;
        this.content = content;
        this.timestamp = LocalDateTime.now();
        this.functionCalls = functionCalls;
    }

    // Getters
    public Role getRole() {
        return role;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getFunctionCalls() {
        return functionCalls;
    }

    @Override
    public String toString() {
        return role + ": " + content;
    }
}
