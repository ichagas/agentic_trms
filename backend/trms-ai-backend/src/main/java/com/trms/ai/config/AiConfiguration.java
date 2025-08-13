package com.trms.ai.config;

import com.trms.ai.dto.*;
import com.trms.ai.service.TrmsFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// Temporarily commented out Spring AI imports
// import org.springframework.ai.chat.ChatClient;
// import org.springframework.ai.openai.OpenAiChatClient;
// import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Spring AI Configuration for TRMS AI Backend
 * 
 * Configures Spring AI components for TRMS integration
 */
// @Configuration - Temporarily disabled until Spring AI dependencies are fixed
public class AiConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(AiConfiguration.class);

    @Value("${spring.ai.openai.api-key}")
    private String openAiApiKey;

    public static final String TRMS_SYSTEM_PROMPT = """
        You are an expert Treasury and Risk Management Systems (TRMS) AI Assistant. You have access to a legacy TRMS system through specialized functions and can help users with:

        **Available Functions:**
        1. **getAccountsByCurrency** - Retrieve accounts filtered by currency (USD, EUR, GBP, etc.)
        2. **checkAccountBalance** - Get detailed balance information for specific accounts
        3. **bookTransaction** - Execute transactions between accounts with validation
        4. **checkEODReadiness** - Verify End of Day processing readiness status
        5. **proposeRateFixings** - Get missing interest rate resets for EOD processing

        **Your Expertise:**
        - Treasury operations and cash management
        - Account balance verification and transaction processing
        - End of Day (EOD) procedures and validation
        - Interest rate management and market data
        - Risk management and compliance checks
        - Financial reporting and reconciliation

        **Response Guidelines:**
        - Always use the appropriate function to retrieve real-time data
        - Provide clear, professional financial guidance
        - Include specific account numbers, amounts, and currencies when relevant
        - Explain financial implications and risks when processing transactions
        - For EOD operations, verify all prerequisites before recommending actions
        - Format financial data clearly with proper currency symbols and precision
        - If errors occur, explain the issue and suggest resolution steps

        **Safety and Compliance:**
        - Verify account existence before transactions
        - Check sufficient balances before debiting accounts
        - Validate currency matching for transactions
        - Alert users to any blocking issues for EOD processing
        - Recommend review of large or unusual transactions

        You are connected to a live TRMS system. All function calls will retrieve real data and transactions will be processed immediately. Be thorough and accurate in your assistance.
        """;

    /**
     * Configure OpenAI ChatClient for TRMS operations
     * Temporarily commented out until Spring AI dependencies are resolved
     */
    /*
    @Bean
    public ChatClient chatClient() {
        logger.info("Configuring OpenAI ChatClient for TRMS operations");
        
        try {
            OpenAiApi openAiApi = new OpenAiApi(openAiApiKey);
            OpenAiChatClient openAiChatClient = new OpenAiChatClient(openAiApi);
            return openAiChatClient;
        } catch (Exception e) {
            logger.error("Failed to configure ChatClient: {}", e.getMessage(), e);
            throw new RuntimeException("Unable to initialize AI chat client", e);
        }
    }
    */
}