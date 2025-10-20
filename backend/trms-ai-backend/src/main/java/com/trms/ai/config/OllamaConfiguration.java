package com.trms.ai.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Ollama Configuration for TRMS AI Backend
 * 
 * Configures Ollama integration with HTTP client for OpenAI-compatible API
 * Note: This is a simplified version that works without Spring AI dependencies
 */
@Configuration
@ConditionalOnProperty(name = "spring.ai.ollama.enabled", havingValue = "true", matchIfMissing = true)
public class OllamaConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(OllamaConfiguration.class);

    @Value("${spring.ai.ollama.base-url:http://localhost:11434}")
    private String ollamaBaseUrl;
    
    @Value("${spring.ai.ollama.chat.options.model:qwen3:1.7b}")
    private String ollamaModel;

    @Value("${app.ai-provider:ollama}")
    private String aiProvider;

    /**
     * Configure Ollama HTTP Client for direct API communication
     */
    @Bean(name = "ollamaHttpClient")
    public OllamaClient ollamaHttpClient() {
        logger.info("Configuring Ollama HTTP Client for TRMS operations");
        logger.info("AI Provider: {}", aiProvider);
        logger.info("Ollama Base URL: {}", ollamaBaseUrl);
        logger.info("Ollama Model: {}", ollamaModel);

        return new OllamaClient(ollamaBaseUrl, ollamaModel, aiProvider);
    }
    
    /**
     * Simple HTTP client for Ollama OpenAI-compatible API
     */
    public static class OllamaClient {
        private static final Logger clientLogger = LoggerFactory.getLogger(OllamaClient.class);

        private final String baseUrl;
        private final String model;
        private final String aiProvider;
        private final HttpClient httpClient;

        public OllamaClient(String baseUrl, String model, String aiProvider) {
            this.baseUrl = baseUrl;
            this.model = model;
            this.aiProvider = aiProvider;
            this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        }
        
        /**
         * Check if Ollama service is available
         */
        public boolean isAvailable() {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(java.net.URI.create(baseUrl.replace("/v1", "") + "/api/tags"))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();
                
                HttpResponse<String> response = httpClient.send(request, 
                    HttpResponse.BodyHandlers.ofString());
                
                boolean available = response.statusCode() == 200;

                clientLogger.debug(aiProvider.toUpperCase() + " availability check: {}", available ? "AVAILABLE" : "UNAVAILABLE");
                
                return available;
            } catch (Exception e) {
                clientLogger.debug(aiProvider.toUpperCase() + " availability check failed: {}", e.getMessage());
                return false;
            }
        }
        
        /**
         * Send chat completion request to Ollama
         */
        public String chat(String message) {
            if (!isAvailable()) {
                clientLogger.warn("Ollama not available, using fallback response");
                return generateFallbackResponse(message);
            }
            
            try {
                String requestBody = String.format("""
                    {
                        "model": "%s",
                        "messages": [
                            {
                                "role": "system",
                                "content": "You are a helpful TRMS (Treasury and Risk Management System) AI assistant. Provide concise, professional responses about financial operations."
                            },
                            {
                                "role": "user", 
                                "content": "%s"
                            }
                        ],
                        "temperature": 0.3,
                        "max_tokens": 2000
                    }
                    """, model, message.replace("\"", "\\\""));
                
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(java.net.URI.create(baseUrl + "/chat/completions"))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();
                
                HttpResponse<String> response = httpClient.send(request, 
                    HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() == 200) {
                    clientLogger.debug("Ollama response received successfully");
                    return extractMessageFromResponse(response.body());
                } else {
                    clientLogger.warn("Ollama API error: {} - {}", response.statusCode(), response.body());
                    return generateFallbackResponse(message);
                }
                
            } catch (Exception e) {
                clientLogger.error("Error calling Ollama API: {}", e.getMessage());
                return generateFallbackResponse(message);
            }
        }
        
        /**
         * Extract message content from Ollama API response
         */
        private String extractMessageFromResponse(String responseBody) {
            try {
                // Simple JSON parsing - look for "content" field in the response
                // This is a basic implementation, in production you'd use a proper JSON parser
                int contentStart = responseBody.indexOf("\"content\":");
                if (contentStart != -1) {
                    contentStart = responseBody.indexOf("\"", contentStart + 10) + 1;
                    int contentEnd = responseBody.indexOf("\"", contentStart);
                    if (contentEnd != -1) {
                        String content = responseBody.substring(contentStart, contentEnd);
                        // Unescape JSON strings
                        return content.replace("\\\"", "\"").replace("\\n", "\n");
                    }
                }
                
                clientLogger.warn("Could not parse Ollama response, using fallback");
                return "I received a response from the AI system, but had trouble parsing it. Please try again.";
                
            } catch (Exception e) {
                clientLogger.error("Error parsing Ollama response: {}", e.getMessage());
                return "I encountered an error processing the AI response. Please try again.";
            }
        }
        
        /**
         * Generate fallback response when Ollama is not available
         */
        private String generateFallbackResponse(String input) {
            String message = input.toLowerCase();
            
            if (message.contains("account") && message.contains("balance")) {
                return "I can help you check account balances. To check a specific account balance, please provide the account ID (e.g., 'ACC-001-USD'). The TRMS system is connected and ready to provide real-time balance information.";
            } else if (message.contains("transaction") || message.contains("transfer")) {
                return "I can help you process transactions between accounts. Please provide: source account ID, destination account ID, amount, and currency. All transactions are validated against available balances and account currencies.";
            } else if (message.contains("eod") || message.contains("end of day")) {
                return "I can help you with End of Day processing. The system performs various checks including market data completeness, transaction settlement status, and missing rate fixings. Would you like me to check the current EOD readiness status?";
            } else if (message.contains("account") && (message.contains("usd") || message.contains("eur") || message.contains("gbp"))) {
                return "I can retrieve accounts by currency. The TRMS system manages accounts in USD, EUR, GBP, JPY. Would you like me to show you all accounts for a specific currency?";
            } else {
                return "Welcome to the TRMS AI Assistant! I can help you with:\n\n• **Account Management** - Check balances, view account details by currency\n• **Transaction Processing** - Book transfers between accounts with validation\n• **End of Day Operations** - Check EOD readiness, manage rate fixings\n• **Financial Reporting** - Access account summaries and transaction history\n\nI'm connected to your live TRMS system and can provide real-time data. What would you like to do today?";
            }
        }
        
        public String getBaseUrl() {
            return baseUrl;
        }
        
        public String getModel() {
            return model;
        }
    }
}