package com.trms.ai.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * TRMS AI Service for handling chat interactions with Spring AI
 * 
 * This service provides intelligent responses using Spring AI's ChatClient
 * with function calling capabilities to interact with TRMS backend systems.
 */
@Service
public class TrmsAiService {

    private static final Logger logger = LoggerFactory.getLogger(TrmsAiService.class);

    private final ChatClient chatClient;
    private final TrmsFunctions trmsFunctions;
    private final SwiftFunctions swiftFunctions;

    private final String SYSTEM_PROMPT = """
        You are a helpful Treasury and Risk Management System (TRMS) AI assistant with integrated SWIFT messaging capabilities.

        You have access to the following TRMS functions:
        - getAccountsByCurrency: Get accounts filtered by currency (USD, EUR, GBP, JPY)
        - checkAccountBalance: Check balance for a specific account by account ID
        - bookTransaction: Book transactions between accounts
        - checkEODReadiness: Check End of Day processing readiness
        - proposeRateFixings: Get proposed rate fixings for missing resets

        You also have access to SWIFT messaging functions:
        - sendSwiftPayment: Send payment via SWIFT network (MT103 message)
        - checkSwiftMessageStatus: Check status of a SWIFT message
        - getSwiftMessagesByAccount: Get all SWIFT messages for an account
        - getSwiftMessagesByTransaction: Get SWIFT messages for a transaction
        - reconcileSwiftMessages: Reconcile SWIFT messages with TRMS transactions
        - getUnreconciledMessages: Get unreconciled SWIFT messages
        - processRedemptionReport: Process redemption report file (automates manual data entry)
        - verifyEODReports: Verify EOD reports in shared drive

        Always use the appropriate function to get real data from the systems when users ask about:
        - Account information or balances
        - Transactions or transfers
        - SWIFT payments and message status
        - Reconciliation between TRMS and SWIFT
        - End of day processing and reports
        - Redemption reports processing
        - Rate fixings or market data

        Provide clear, professional responses about financial operations.
        When you retrieve data, format it clearly and explain what the information means.
        If a function call fails, explain the error and suggest alternatives.
        """;

    @Autowired
    public TrmsAiService(@Qualifier("ollamaChatModel") ChatModel chatModel,
                        TrmsFunctions trmsFunctions,
                        SwiftFunctions swiftFunctions) {
        this.trmsFunctions = trmsFunctions;
        this.swiftFunctions = swiftFunctions;
        this.chatClient = ChatClient.builder(chatModel)
            .defaultSystem(SYSTEM_PROMPT)
            .build();

        logger.info("TrmsAiService initialized with Ollama ChatModel, TRMS and SWIFT function calling");
    }

    /**
     * Process a chat message using hybrid approach: function detection + Spring AI
     */
    public String chat(String userMessage) {
        logger.debug("Processing chat message with hybrid function calling: {}", userMessage);
        
        try {
            // First, check if the message requires function calling
            String functionResult = tryExecuteFunction(userMessage);
            
            if (functionResult != null) {
                // Function was executed, now get AI to format the response
                String aiPrompt = String.format("""
                    User asked: %s
                    
                    I executed the appropriate TRMS function and got this data:
                    %s
                    
                    Please format this data in a clear, professional way and explain what it means to the user.
                    """, userMessage, functionResult);
                
                String response = chatClient
                    .prompt()
                    .user(aiPrompt)
                    .call()
                    .content();
                
                logger.info("Hybrid function calling response generated successfully");
                return response;
            } else {
                // No function needed, use regular AI response
                String response = chatClient
                    .prompt()
                    .user(userMessage)
                    .call()
                    .content();
                
                logger.info("Regular AI response generated successfully");
                return response;
            }
            
        } catch (Exception e) {
            logger.error("Error processing chat: {}", e.getMessage(), e);
            return generateFallbackResponse(userMessage);
        }
    }
    
    /**
     * Try to execute TRMS functions based on user message content
     * Supports both single function calls and complex multi-function workflows
     */
    private String tryExecuteFunction(String userMessage) {
        String message = userMessage.toLowerCase();
        
        try {
            // MULTI-FUNCTION WORKFLOWS
            
            // Scenario 1: Complete EOD Preparation Workflow
            if (message.contains("prepare") && (message.contains("eod") || message.contains("end of day"))) {
                logger.info("Executing Complete EOD Preparation Workflow");
                return executeEODPreparationWorkflow();
            }
            
            // Scenario 2: Cross-Currency Portfolio Analysis
            if ((message.contains("portfolio") || message.contains("cash position") || 
                 message.contains("all currencies") || message.contains("complete overview")) && 
                (message.contains("currency") || message.contains("currencies"))) {
                logger.info("Executing Cross-Currency Portfolio Analysis");
                return executeCrossCurrencyPortfolioAnalysis();
            }
            
            // Scenario 3: EOD Issue Resolution
            if ((message.contains("what") || message.contains("fix") || message.contains("resolve")) && 
                (message.contains("blocking") || message.contains("blocker")) && 
                (message.contains("eod") || message.contains("end of day"))) {
                logger.info("Executing EOD Issue Resolution Workflow");
                return executeEODIssueResolutionWorkflow();
            }
            
            // SINGLE FUNCTION CALLS
            
            // Check for account-related queries
            if (message.contains("account") && (message.contains("usd") || message.contains("eur") || 
                message.contains("gbp") || message.contains("jpy"))) {
                
                String currency = extractCurrency(message);
                if (currency != null) {
                    logger.info("Executing getAccountsByCurrency with currency: {}", currency);
                    return executeGetAccountsByCurrency(currency);
                }
            }
            
            // Check for balance queries
            if (message.contains("balance") && message.contains("acc-")) {
                String accountId = extractAccountId(message);
                if (accountId != null) {
                    logger.info("Executing checkAccountBalance with account: {}", accountId);
                    return executeCheckAccountBalance(accountId);
                }
            }
            
            // Check for EOD queries
            if (message.contains("eod") || message.contains("end of day")) {
                logger.info("Executing checkEODReadiness");
                return executeCheckEODReadiness();
            }
            
            // Check for rate fixing queries
            if (message.contains("rate") && (message.contains("fix") || message.contains("reset"))) {
                logger.info("Executing proposeRateFixings");
                return executeProposeRateFixings();
            }

            // SWIFT FUNCTION CALLS

            // Check for SWIFT message status queries
            if ((message.contains("swift") || message.contains("message")) &&
                (message.contains("status") || message.contains("check")) &&
                message.contains("swift-msg-")) {
                String messageId = extractSwiftMessageId(message);
                if (messageId != null) {
                    logger.info("Executing checkSwiftMessageStatus with message: {}", messageId);
                    return executeCheckSwiftMessageStatus(messageId);
                }
            }

            // Check for SWIFT messages by account
            if ((message.contains("swift") && message.contains("account")) ||
                (message.contains("messages") && message.contains("acc-"))) {
                String accountId = extractAccountId(message);
                if (accountId != null) {
                    logger.info("Executing getSwiftMessagesByAccount with account: {}", accountId);
                    return executeGetSwiftMessagesByAccount(accountId);
                }
            }

            // Check for unreconciled messages
            if (message.contains("unreconciled") ||
                (message.contains("reconcil") && message.contains("swift"))) {
                logger.info("Executing getUnreconciledMessages");
                return executeGetUnreconciledMessages();
            }

            // Check for redemption report processing
            if (message.contains("redemption") && message.contains("report")) {
                logger.info("Executing processRedemptionReport");
                return executeProcessRedemptionReport("redemption_report_latest.csv");
            }

            // Check for EOD report verification
            if (message.contains("verify") && message.contains("eod") && message.contains("report")) {
                logger.info("Executing verifyEODReports");
                String reportDate = extractDate(message);
                if (reportDate == null) {
                    reportDate = java.time.LocalDate.now().toString();
                }
                return executeVerifyEODReports(reportDate);
            }

        } catch (Exception e) {
            logger.error("Error executing function: {}", e.getMessage());
            return "Error executing function: " + e.getMessage();
        }

        return null; // No function execution needed
    }
    
    private String extractCurrency(String message) {
        if (message.contains("usd")) return "USD";
        if (message.contains("eur")) return "EUR";
        if (message.contains("gbp")) return "GBP";
        if (message.contains("jpy")) return "JPY";
        return null;
    }
    
    private String extractAccountId(String message) {
        // Simple regex to find account IDs like ACC-001-USD
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("acc-\\d+-\\w+",
            java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher matcher = pattern.matcher(message);
        return matcher.find() ? matcher.group().toUpperCase() : null;
    }

    private String extractSwiftMessageId(String message) {
        // Extract SWIFT message IDs like SWIFT-MSG-00001
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("swift-msg-\\d+",
            java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher matcher = pattern.matcher(message);
        return matcher.find() ? matcher.group().toUpperCase() : null;
    }

    private String extractDate(String message) {
        // Extract dates in YYYY-MM-DD format
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
        java.util.regex.Matcher matcher = pattern.matcher(message);
        return matcher.find() ? matcher.group() : null;
    }
    
    private String executeGetAccountsByCurrency(String currency) {
        try {
            var request = new com.trms.ai.service.TrmsFunctions.GetAccountsByCurrencyRequest(currency);
            var accounts = trmsFunctions.getAccountsByCurrency().apply(request);
            return formatAccountsData(accounts);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get accounts: " + e.getMessage());
        }
    }
    
    private String executeCheckAccountBalance(String accountId) {
        try {
            var request = new com.trms.ai.service.TrmsFunctions.CheckAccountBalanceRequest(accountId);
            var balance = trmsFunctions.checkAccountBalance().apply(request);
            return formatBalanceData(balance);
        } catch (Exception e) {
            throw new RuntimeException("Failed to check balance: " + e.getMessage());
        }
    }
    
    private String executeCheckEODReadiness() {
        try {
            var request = new com.trms.ai.service.TrmsFunctions.CheckEODReadinessRequest();
            var status = trmsFunctions.checkEODReadiness().apply(request);
            return formatEODData(status);
        } catch (Exception e) {
            throw new RuntimeException("Failed to check EOD readiness: " + e.getMessage());
        }
    }
    
    private String executeProposeRateFixings() {
        try {
            var request = new com.trms.ai.service.TrmsFunctions.ProposeRateFixingsRequest();
            var rateResets = trmsFunctions.proposeRateFixings().apply(request);
            return formatRateFixingsData(rateResets);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get rate fixings: " + e.getMessage());
        }
    }
    
    private String formatAccountsData(java.util.List<?> accounts) {
        return "ACCOUNTS DATA:\n" + accounts.toString();
    }
    
    private String formatBalanceData(Object balance) {
        return "BALANCE DATA:\n" + balance.toString();
    }
    
    private String formatEODData(Object status) {
        return "EOD STATUS:\n" + status.toString();
    }
    
    private String formatRateFixingsData(java.util.List<?> rateResets) {
        return "RATE FIXINGS:\n" + rateResets.toString();
    }

    // ========== SWIFT EXECUTION FUNCTIONS ==========

    private String executeCheckSwiftMessageStatus(String messageId) {
        try {
            var request = new com.trms.ai.service.SwiftFunctions.CheckSwiftStatusRequest(messageId);
            var status = swiftFunctions.checkSwiftMessageStatus().apply(request);
            return formatSwiftStatusData(status);
        } catch (Exception e) {
            throw new RuntimeException("Failed to check SWIFT message status: " + e.getMessage());
        }
    }

    private String executeGetSwiftMessagesByAccount(String accountId) {
        try {
            var request = new com.trms.ai.service.SwiftFunctions.GetSwiftMessagesByAccountRequest(accountId);
            var messages = swiftFunctions.getSwiftMessagesByAccount().apply(request);
            return formatSwiftMessagesData(messages);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get SWIFT messages: " + e.getMessage());
        }
    }

    private String executeGetUnreconciledMessages() {
        try {
            var request = new com.trms.ai.service.SwiftFunctions.GetUnreconciledMessagesRequest();
            var messages = swiftFunctions.getUnreconciledMessages().apply(request);
            return formatSwiftMessagesData(messages);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get unreconciled messages: " + e.getMessage());
        }
    }

    private String executeProcessRedemptionReport(String fileName) {
        try {
            var request = new com.trms.ai.service.SwiftFunctions.ProcessRedemptionReportRequest(fileName);
            var result = swiftFunctions.processRedemptionReport().apply(request);
            return formatRedemptionReportData(result);
        } catch (Exception e) {
            throw new RuntimeException("Failed to process redemption report: " + e.getMessage());
        }
    }

    private String executeVerifyEODReports(String reportDate) {
        try {
            var request = new com.trms.ai.service.SwiftFunctions.VerifyEODReportsRequest(reportDate);
            var result = swiftFunctions.verifyEODReports().apply(request);
            return formatEODVerificationData(result);
        } catch (Exception e) {
            throw new RuntimeException("Failed to verify EOD reports: " + e.getMessage());
        }
    }

    private String formatSwiftStatusData(Object status) {
        return "SWIFT MESSAGE STATUS:\n" + status.toString();
    }

    private String formatSwiftMessagesData(java.util.List<?> messages) {
        return "SWIFT MESSAGES:\n" + messages.toString();
    }

    private String formatRedemptionReportData(Object result) {
        return "REDEMPTION REPORT RESULT:\n" + result.toString();
    }

    private String formatEODVerificationData(Object result) {
        return "EOD REPORT VERIFICATION:\n" + result.toString();
    }

    // ========== MULTI-FUNCTION WORKFLOWS ==========
    
    /**
     * Complete EOD Preparation Workflow
     * Steps: 1) Check current EOD status, 2) Propose rate fixings, 3) Verify improvements
     */
    private String executeEODPreparationWorkflow() {
        StringBuilder result = new StringBuilder();
        result.append("🔄 EXECUTING EOD PREPARATION WORKFLOW\n\n");
        
        try {
            // Step 1: Check current EOD readiness
            result.append("📊 Step 1: Checking current EOD readiness...\n");
            String eodStatus = executeCheckEODReadiness();
            result.append(eodStatus).append("\n\n");
            
            // Step 2: Propose rate fixings for missing resets
            result.append("⚙️ Step 2: Proposing rate fixings for missing resets...\n");
            String rateFixings = executeProposeRateFixings();
            result.append(rateFixings).append("\n\n");
            
            // Step 3: Re-check EOD readiness to see improvements
            result.append("✅ Step 3: Verifying EOD readiness after fixes...\n");
            String finalStatus = executeCheckEODReadiness();
            result.append(finalStatus).append("\n\n");
            
            result.append("🎉 EOD PREPARATION WORKFLOW COMPLETED\n");
            result.append("The system has been analyzed and rate fixing proposals have been generated.");
            
        } catch (Exception e) {
            result.append("❌ Error in EOD Preparation Workflow: ").append(e.getMessage());
        }
        
        return result.toString();
    }
    
    /**
     * Cross-Currency Portfolio Analysis
     * Steps: Get accounts for all major currencies (USD, EUR, GBP, JPY)
     */
    private String executeCrossCurrencyPortfolioAnalysis() {
        StringBuilder result = new StringBuilder();
        result.append("💰 CROSS-CURRENCY PORTFOLIO ANALYSIS\n\n");
        
        try {
            String[] currencies = {"USD", "EUR", "GBP", "JPY"};
            
            for (String currency : currencies) {
                result.append("🏦 ").append(currency).append(" Accounts:\n");
                String accounts = executeGetAccountsByCurrency(currency);
                result.append(accounts).append("\n");
            }
            
            result.append("📈 PORTFOLIO ANALYSIS COMPLETED\n");
            result.append("All currency positions have been retrieved from the TRMS system.");
            
        } catch (Exception e) {
            result.append("❌ Error in Portfolio Analysis: ").append(e.getMessage());
        }
        
        return result.toString();
    }
    
    /**
     * EOD Issue Resolution Workflow
     * Steps: 1) Identify blockers, 2) Attempt automatic resolution, 3) Verify resolution
     */
    private String executeEODIssueResolutionWorkflow() {
        StringBuilder result = new StringBuilder();
        result.append("🛠️ EOD ISSUE RESOLUTION WORKFLOW\n\n");
        
        try {
            // Step 1: Identify what's blocking EOD
            result.append("🔍 Step 1: Identifying EOD blockers...\n");
            String eodStatus = executeCheckEODReadiness();
            result.append(eodStatus).append("\n\n");
            
            // Step 2: Attempt to resolve rate fixing issues
            result.append("⚡ Step 2: Attempting automatic issue resolution...\n");
            String rateFixings = executeProposeRateFixings();
            result.append("Rate Fixing Proposals:\n").append(rateFixings).append("\n\n");
            
            // Step 3: Check if issues were resolved
            result.append("✅ Step 3: Verifying issue resolution...\n");
            String finalCheck = executeCheckEODReadiness();
            result.append(finalCheck).append("\n\n");
            
            result.append("🎯 ISSUE RESOLUTION WORKFLOW COMPLETED\n");
            result.append("Automatic resolution attempts have been made for identified EOD blockers.");
            
        } catch (Exception e) {
            result.append("❌ Error in Issue Resolution Workflow: ").append(e.getMessage());
        }
        
        return result.toString();
    }

    /**
     * Generate fallback response when Spring AI is not available
     */
    private String generateFallbackResponse(String input) {
        String message = input.toLowerCase();
        
        if (message.contains("account") && message.contains("balance")) {
            return "I can help you check account balances. To check a specific account balance, please provide the account ID (e.g., 'ACC-001-USD'). " +
                   "The TRMS system is connected and ready to provide real-time balance information.";
        } else if (message.contains("transaction") || message.contains("transfer")) {
            return "I can help you process transactions between accounts. Please provide: source account ID, destination account ID, amount, and currency. " +
                   "All transactions are validated against available balances and account currencies.";
        } else if (message.contains("eod") || message.contains("end of day")) {
            return "I can help you with End of Day processing. The system performs various checks including market data completeness, transaction settlement status, and missing rate fixings. " +
                   "Would you like me to check the current EOD readiness status?";
        } else if (message.contains("account") && (message.contains("usd") || message.contains("eur") || message.contains("gbp"))) {
            return "I can retrieve accounts by currency. The TRMS system manages accounts in USD, EUR, GBP, JPY. " +
                   "Would you like me to show you all accounts for a specific currency?";
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
}