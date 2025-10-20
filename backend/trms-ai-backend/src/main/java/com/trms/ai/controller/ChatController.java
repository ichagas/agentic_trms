package com.trms.ai.controller;

import com.trms.ai.client.LegacyTrmsClient;
import com.trms.ai.config.OllamaConfiguration;
import com.trms.ai.dto.ChatRequest;
import com.trms.ai.dto.ChatResponse;
import com.trms.ai.model.ConversationHistory;
import com.trms.ai.model.ConversationMessage;
import com.trms.ai.service.ConversationMemory;
import com.trms.ai.service.TrmsAiService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * REST Controller for AI chat interactions with TRMS functionality
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(originPatterns = {"http://localhost:*", "http://127.0.0.1:*"}, 
             allowCredentials = "true",
             methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS})
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    private final LegacyTrmsClient legacyTrmsClient;
    private final TrmsAiService trmsAiService;
    private final ConversationMemory conversationMemory;

    @Autowired(required = false)
    private OllamaConfiguration.OllamaClient ollamaClient;

    public ChatController(LegacyTrmsClient legacyTrmsClient,
                         TrmsAiService trmsAiService,
                         ConversationMemory conversationMemory) {
        this.legacyTrmsClient = legacyTrmsClient;
        this.trmsAiService = trmsAiService;
        this.conversationMemory = conversationMemory;
    }

    /**
     * Handle chat messages with basic TRMS functionality (simplified for POC)
     */
    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        String sessionId = request.sessionId() != null ? request.sessionId() : UUID.randomUUID().toString();
        String timestamp = ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT);
        
        logger.info("Processing chat request - Session: {}, Message: {}", sessionId, request.message());

        try {
            String response;
            java.util.List<String> executedFunctions = java.util.List.of();

            // Try to use Spring AI with function calling first, then fallback
            try {
                boolean experimentalMode = request.experimentalMode() != null ? request.experimentalMode() : false;
                logger.debug("Using Spring AI with function calling for response generation (experimental mode: {})", experimentalMode);
                TrmsAiService.ChatResult result = trmsAiService.chat(sessionId, request.message(), experimentalMode);
                response = result.getResponse();
                executedFunctions = result.getExecutedFunctions();
                logger.info("Spring AI response generated successfully for session: {}, mode: {}, executed {} functions",
                           sessionId, experimentalMode ? "EXPERIMENTAL-LLM" : "RULE-BASED", executedFunctions.size());
            } catch (Exception e) {
                logger.warn("Spring AI failed, falling back to Ollama: {}", e.getMessage());

                // Fallback to direct Ollama if Spring AI fails
                if (ollamaClient != null && ollamaClient.isAvailable()) {
                    logger.debug("Using direct Ollama for AI response generation");
                    response = ollamaClient.chat(request.message());
                } else {
                    logger.debug("Using pattern matching for response generation (all AI unavailable)");
                    response = generatePatternMatchedResponse(request.message());
                }
            }

            return ResponseEntity.ok(ChatResponse.success(response, sessionId, timestamp, executedFunctions));

        } catch (Exception e) {
            logger.error("Error processing chat request for session {}: {}", sessionId, e.getMessage(), e);
            
            String errorMessage = "I apologize, but I encountered an error while processing your request. " +
                                 "Please try again or contact support if the issue persists.";
            
            return ResponseEntity.ok(ChatResponse.error(errorMessage, sessionId, timestamp));
        }
    }

    /**
     * Generate pattern-matched response when Ollama is not available
     */
    private String generatePatternMatchedResponse(String input) {
        String message = input.toLowerCase();
        
        if (message.contains("account") && message.contains("balance")) {
            return "I can help you check account balances. To check a specific account balance, please provide the account ID (e.g., 'ACC-001-USD'). " +
                   "The TRMS system is connected and ready to provide real-time balance information.";
        } else if (message.contains("transaction")) {
            return "I can help you process transactions between accounts. To book a transaction, please provide: " +
                   "- Source account ID, - Destination account ID, - Amount, - Currency. " +
                   "All transactions are validated against available balances and account currencies.";
        } else if (message.contains("eod") || message.contains("end of day")) {
            return "I can help you with End of Day processing. The system performs various checks including: " +
                   "- Market data completeness, - Transaction settlement status, - Missing rate fixings. " +
                   "Would you like me to check the current EOD readiness status?";
        } else if (message.contains("account") && (message.contains("usd") || message.contains("eur") || message.contains("gbp"))) {
            return "I can retrieve accounts by currency. The TRMS system manages accounts in multiple currencies: " +
                   "USD, EUR, GBP, JPY. Would you like me to show you all accounts for a specific currency?";
        } else {
            return "Welcome to the TRMS AI Assistant! I can help you with:\n\n" +
                   "• **Account Management** - Check balances, view account details by currency\n" +
                   "• **Transaction Processing** - Book transfers between accounts with validation\n" +
                   "• **End of Day Operations** - Check EOD readiness, manage rate fixings\n" +
                   "• **Financial Reporting** - Access account summaries and transaction history\n\n" +
                   "I'm connected to your live TRMS system and can provide real-time data. " +
                   "What would you like to do today?";
        }
    }

    /**
     * Health check endpoint for the chat service
     */
    @GetMapping("/chat/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> healthStatus = new HashMap<>();
        healthStatus.put("service", "TRMS AI Chat Service");
        healthStatus.put("status", "RUNNING");

        // Add Ollama status if available
        if (ollamaClient != null) {
            if (ollamaClient.isAvailable()) {
                healthStatus.put("ollama", "AVAILABLE at " + ollamaClient.getBaseUrl());
            } else {
                healthStatus.put("ollama", "UNAVAILABLE - using fallback mode");
            }
        } else {
            healthStatus.put("ollama", "NOT CONFIGURED - using pattern matching");
        }

        // Add experimental mode info
        healthStatus.put("experimentalMode", "Available - set experimentalMode: true in request to enable pure LLM function calling");
        healthStatus.put("defaultMode", "Rule-based (programmatic function calling)");

        return ResponseEntity.ok(healthStatus);
    }

    /**
     * Debug endpoint: Get conversation history for a specific session
     *
     * Usage: GET /api/chat/debug/session/{sessionId}
     * Example: curl http://localhost:8080/api/chat/debug/session/abc-123
     */
    @GetMapping("/chat/debug/session/{sessionId}")
    public ResponseEntity<Map<String, Object>> getSessionContext(@PathVariable String sessionId) {
        ConversationHistory history = conversationMemory.getHistory(sessionId);

        if (history == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Session not found");
            response.put("sessionId", sessionId);
            response.put("message", "No conversation history exists for this session ID");
            return ResponseEntity.ok(response);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("sessionId", history.getSessionId());
        response.put("startTime", history.getStartTime().toString());
        response.put("lastAccessTime", history.getLastAccessTime().toString());
        response.put("messageCount", history.getMessageCount());

        // Convert messages to readable format
        List<Map<String, String>> messages = history.getMessages().stream()
            .map(msg -> {
                Map<String, String> msgMap = new HashMap<>();
                msgMap.put("role", msg.getRole().toString());
                msgMap.put("content", msg.getContent());
                msgMap.put("timestamp", msg.getTimestamp().toString());
                if (msg.getFunctionCalls() != null) {
                    msgMap.put("functionCalls", msg.getFunctionCalls());
                }
                return msgMap;
            })
            .collect(Collectors.toList());

        response.put("messages", messages);

        logger.info("Debug request for session: {}, found {} messages", sessionId, history.getMessageCount());
        return ResponseEntity.ok(response);
    }

    /**
     * Debug endpoint: List all active sessions
     *
     * Usage: GET /api/chat/debug/sessions
     * Example: curl http://localhost:8080/api/chat/debug/sessions
     */
    @GetMapping("/chat/debug/sessions")
    public ResponseEntity<Map<String, Object>> getAllSessions() {
        Map<String, Object> response = new HashMap<>();
        response.put("activeSessionCount", conversationMemory.getActiveSessionCount());
        response.put("statistics", conversationMemory.getStatistics());

        logger.info("Debug request for all active sessions");
        return ResponseEntity.ok(response);
    }

    /**
     * Debug endpoint: Clear a specific session's conversation history
     *
     * Usage: DELETE /api/chat/debug/session/{sessionId}
     * Example: curl -X DELETE http://localhost:8080/api/chat/debug/session/abc-123
     */
    @DeleteMapping("/chat/debug/session/{sessionId}")
    public ResponseEntity<Map<String, Object>> clearSession(@PathVariable String sessionId) {
        conversationMemory.clearConversation(sessionId);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Session cleared successfully");
        response.put("sessionId", sessionId);

        logger.info("Debug: Cleared session {}", sessionId);
        return ResponseEntity.ok(response);
    }
}