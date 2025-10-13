package com.trms.ai.service;

import com.trms.ai.model.ConversationHistory;
import com.trms.ai.model.ConversationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory conversation history management service
 *
 * Features:
 * - Thread-safe conversation storage per session
 * - Automatic cleanup of expired sessions
 * - Configurable history size limits
 * - Memory-efficient with size caps
 */
@Service
public class ConversationMemory {

    private static final Logger logger = LoggerFactory.getLogger(ConversationMemory.class);

    // Configuration constants
    private static final int MAX_MESSAGES_PER_CONVERSATION = 20; // Last 20 messages
    private static final long SESSION_IDLE_TIMEOUT_MINUTES = 60; // 1 hour idle timeout

    // Thread-safe storage for all active conversations
    private final Map<String, ConversationHistory> conversations = new ConcurrentHashMap<>();

    /**
     * Get or create conversation history for a session
     */
    public ConversationHistory getOrCreateConversation(String sessionId) {
        return conversations.computeIfAbsent(sessionId,
            id -> {
                logger.info("Creating new conversation for session: {}", id);
                return new ConversationHistory(id, MAX_MESSAGES_PER_CONVERSATION);
            });
    }

    /**
     * Add a user message to conversation history
     */
    public void addUserMessage(String sessionId, String content) {
        ConversationHistory history = getOrCreateConversation(sessionId);
        history.addMessage(new ConversationMessage(ConversationMessage.Role.USER, content));
        logger.debug("Added user message to session {}: {}", sessionId,
                    content.substring(0, Math.min(50, content.length())));
    }

    /**
     * Add an assistant message to conversation history
     */
    public void addAssistantMessage(String sessionId, String content, String functionCalls) {
        ConversationHistory history = getOrCreateConversation(sessionId);
        history.addMessage(new ConversationMessage(
            ConversationMessage.Role.ASSISTANT,
            content,
            functionCalls
        ));
        logger.debug("Added assistant message to session {} (functions: {})", sessionId, functionCalls);
    }

    /**
     * Get conversation history for a session
     */
    public ConversationHistory getHistory(String sessionId) {
        return conversations.get(sessionId);
    }

    /**
     * Build contextual prompt string from conversation history
     * Returns only recent messages to keep context manageable
     */
    public String buildContextualPrompt(String sessionId, String currentMessage, int recentMessageCount) {
        ConversationHistory history = conversations.get(sessionId);

        if (history == null || history.getMessageCount() == 0) {
            return currentMessage; // First message, no context needed
        }

        StringBuilder contextPrompt = new StringBuilder();
        contextPrompt.append("Previous conversation context:\n");
        contextPrompt.append("─".repeat(50)).append("\n");

        // Get recent messages for context
        var recentMessages = history.getRecentMessages(recentMessageCount);
        for (ConversationMessage msg : recentMessages) {
            // Truncate long messages to keep context manageable
            String content = msg.getContent();
            if (content.length() > 200) {
                content = content.substring(0, 200) + "...";
            }
            contextPrompt.append(msg.getRole()).append(": ").append(content).append("\n");
        }

        contextPrompt.append("─".repeat(50)).append("\n");
        contextPrompt.append("Current user message: ").append(currentMessage);

        return contextPrompt.toString();
    }

    /**
     * Clear a specific conversation
     */
    public void clearConversation(String sessionId) {
        conversations.remove(sessionId);
        logger.info("Cleared conversation for session: {}", sessionId);
    }

    /**
     * Get active session count
     */
    public int getActiveSessionCount() {
        return conversations.size();
    }

    /**
     * Scheduled cleanup of expired sessions
     * Runs every 15 minutes
     */
    @Scheduled(fixedRate = 900000) // 15 minutes in milliseconds
    public void cleanupExpiredSessions() {
        logger.debug("Running scheduled cleanup of expired sessions");

        int removedCount = 0;
        var iterator = conversations.entrySet().iterator();

        while (iterator.hasNext()) {
            var entry = iterator.next();
            if (entry.getValue().isExpired(SESSION_IDLE_TIMEOUT_MINUTES)) {
                iterator.remove();
                removedCount++;
                logger.info("Removed expired session: {}", entry.getKey());
            }
        }

        if (removedCount > 0) {
            logger.info("Cleaned up {} expired sessions. Active sessions: {}",
                       removedCount, conversations.size());
        }
    }

    /**
     * Get statistics for monitoring
     */
    public String getStatistics() {
        int totalSessions = conversations.size();
        int totalMessages = conversations.values().stream()
            .mapToInt(ConversationHistory::getMessageCount)
            .sum();

        double avgMessages = totalSessions > 0 ? (double) totalMessages / totalSessions : 0;

        return String.format("Active sessions: %d, Total messages: %d, Avg messages/session: %.1f",
            totalSessions, totalMessages, avgMessages);
    }
}
