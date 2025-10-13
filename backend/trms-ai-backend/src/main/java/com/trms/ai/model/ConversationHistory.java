package com.trms.ai.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the complete conversation history for a session
 *
 * Manages the lifecycle of a conversation including:
 * - Message storage with automatic size limiting
 * - Session expiration tracking
 * - Thread-safe message retrieval
 */
public class ConversationHistory {

    private final String sessionId;
    private final List<ConversationMessage> messages;
    private final LocalDateTime startTime;
    private LocalDateTime lastAccessTime;
    private final int maxMessages;

    public ConversationHistory(String sessionId, int maxMessages) {
        this.sessionId = sessionId;
        this.messages = new ArrayList<>();
        this.startTime = LocalDateTime.now();
        this.lastAccessTime = LocalDateTime.now();
        this.maxMessages = maxMessages;
    }

    /**
     * Add a message to conversation history
     * Automatically limits size by removing oldest messages
     */
    public void addMessage(ConversationMessage message) {
        messages.add(message);
        lastAccessTime = LocalDateTime.now();

        // Limit history size (keep most recent messages)
        if (messages.size() > maxMessages) {
            // Remove oldest messages, but keep system message if present
            int removeCount = messages.size() - maxMessages;
            for (int i = 0; i < removeCount; i++) {
                // If first message is system message, preserve it
                if (!messages.isEmpty() && messages.get(0).getRole() == ConversationMessage.Role.SYSTEM) {
                    if (messages.size() > 1) {
                        messages.remove(1); // Remove second message
                    }
                } else {
                    messages.remove(0); // Remove first message
                }
            }
        }
    }

    /**
     * Get all messages in the conversation
     * Returns a copy to prevent external modification
     */
    public List<ConversationMessage> getMessages() {
        lastAccessTime = LocalDateTime.now();
        return new ArrayList<>(messages); // Return defensive copy
    }

    /**
     * Get the most recent N messages
     */
    public List<ConversationMessage> getRecentMessages(int count) {
        lastAccessTime = LocalDateTime.now();
        int start = Math.max(0, messages.size() - count);
        return new ArrayList<>(messages.subList(start, messages.size()));
    }

    public String getSessionId() {
        return sessionId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getLastAccessTime() {
        return lastAccessTime;
    }

    public int getMessageCount() {
        return messages.size();
    }

    /**
     * Check if this conversation has expired based on idle time
     */
    public boolean isExpired(long maxIdleMinutes) {
        return LocalDateTime.now().minusMinutes(maxIdleMinutes).isAfter(lastAccessTime);
    }
}
