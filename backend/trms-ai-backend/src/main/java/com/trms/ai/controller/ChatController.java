package com.trms.ai.controller;

import com.trms.ai.client.LegacyTrmsClient;
import com.trms.ai.dto.ChatRequest;
import com.trms.ai.dto.ChatResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

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

    public ChatController(LegacyTrmsClient legacyTrmsClient) {
        this.legacyTrmsClient = legacyTrmsClient;
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
            // Simple pattern matching for TRMS operations (POC implementation)
            String message = request.message().toLowerCase();
            String response;
            
            if (message.contains("account") && message.contains("balance")) {
                response = "I can help you check account balances. To check a specific account balance, please provide the account ID (e.g., 'ACC-001-USD'). " +
                          "The TRMS system is connected and ready to provide real-time balance information.";
            } else if (message.contains("transaction")) {
                response = "I can help you process transactions between accounts. To book a transaction, please provide: " +
                          "- Source account ID, - Destination account ID, - Amount, - Currency. " +
                          "All transactions are validated against available balances and account currencies.";
            } else if (message.contains("eod") || message.contains("end of day")) {
                response = "I can help you with End of Day processing. The system performs various checks including: " +
                          "- Market data completeness, - Transaction settlement status, - Missing rate fixings. " +
                          "Would you like me to check the current EOD readiness status?";
            } else if (message.contains("account") && (message.contains("usd") || message.contains("eur") || message.contains("gbp"))) {
                response = "I can retrieve accounts by currency. The TRMS system manages accounts in multiple currencies: " +
                          "USD, EUR, GBP, JPY. Would you like me to show you all accounts for a specific currency?";
            } else {
                response = "Welcome to the TRMS AI Assistant! I can help you with:\n\n" +
                          "• **Account Management** - Check balances, view account details by currency\n" +
                          "• **Transaction Processing** - Book transfers between accounts with validation\n" +
                          "• **End of Day Operations** - Check EOD readiness, manage rate fixings\n" +
                          "• **Financial Reporting** - Access account summaries and transaction history\n\n" +
                          "I'm connected to your live TRMS system and can provide real-time data. " +
                          "What would you like to do today?";
            }

            logger.info("Response generated successfully for session: {}", sessionId);
            
            return ResponseEntity.ok(ChatResponse.success(response, sessionId, timestamp));

        } catch (Exception e) {
            logger.error("Error processing chat request for session {}: {}", sessionId, e.getMessage(), e);
            
            String errorMessage = "I apologize, but I encountered an error while processing your request. " +
                                 "Please try again or contact support if the issue persists.";
            
            return ResponseEntity.ok(ChatResponse.error(errorMessage, sessionId, timestamp));
        }
    }

    /**
     * Health check endpoint for the chat service
     */
    @GetMapping("/chat/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("TRMS AI Chat Service is running");
    }
}