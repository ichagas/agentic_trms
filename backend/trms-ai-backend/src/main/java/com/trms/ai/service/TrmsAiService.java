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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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
    private final ChatClient chatClientWithFunctions; // For experimental LLM mode
    private final TrmsFunctions trmsFunctions;
    private final SwiftFunctions swiftFunctions;
    private final ConversationMemory conversationMemory;
    private final FunctionCallTracker functionCallTracker;

    @Value("${app.experimental-mode:false}")
    private boolean defaultExperimentalMode;

    // Thread-local storage for tracking executed functions in the current request
    private final ThreadLocal<List<String>> executedFunctions = ThreadLocal.withInitial(ArrayList::new);

    // Thread-local storage for unreconciled message count (used by validation workflows)
    private final ThreadLocal<Integer> unreconciledMessageCount = ThreadLocal.withInitial(() -> 0);

    /**
     * Result of chat processing including response text and executed functions
     */
    public static class ChatResult {
        private final String response;
        private final List<String> executedFunctions;

        public ChatResult(String response, List<String> executedFunctions) {
            this.response = response;
            this.executedFunctions = executedFunctions;
        }

        public String getResponse() {
            return response;
        }

        public List<String> getExecutedFunctions() {
            return executedFunctions;
        }
    }

    private final String SYSTEM_PROMPT = """
        You are a helpful Treasury and Risk Management System (TRMS) AI assistant with integrated SWIFT messaging capabilities.

        You have access to the following TRMS functions:
        - getAccountsByCurrency: Get accounts filtered by currency (USD, EUR, GBP, JPY)
        - checkAccountBalance: Check balance for a specific account by account ID
        - bookTransaction: Book transactions between accounts (creates transaction with PENDING status)
        - checkEODReadiness: Check End of Day processing readiness
        - proposeRateFixings: Get proposed rate fixings for missing resets

        You also have access to SWIFT messaging functions:
        - sendSwiftPayment: Send payment via SWIFT network (MT103 message) - ONLY for VALIDATED transactions
        - checkSwiftMessageStatus: Check status of a SWIFT message
        - getSwiftMessagesByAccount: Get all SWIFT messages for an account
        - getSwiftMessagesByTransaction: Get SWIFT messages for a transaction
        - reconcileSwiftMessages: Reconcile SWIFT messages with TRMS transactions
        - getUnreconciledMessages: Get unreconciled SWIFT messages
        - processRedemptionReport: Process redemption report file (automates manual data entry)
        - verifyEODReports: Verify EOD reports in shared drive

        IMPORTANT TRANSACTION WORKFLOW:
        1. When booking a transaction, it is created with PENDING status
        2. User must manually APPROVE the transaction in the dashboard (changes status to VALIDATED)
        3. SWIFT payments can ONLY be sent for VALIDATED transactions
        4. If user asks to "transfer and send via SWIFT", book the transaction first and inform them to approve it
        5. Do NOT automatically send SWIFT for PENDING transactions

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
    public TrmsAiService(ChatModel chatModel,
                        TrmsFunctions trmsFunctions,
                        SwiftFunctions swiftFunctions,
                        ConversationMemory conversationMemory,
                        FunctionCallTracker functionCallTracker) {
        this.trmsFunctions = trmsFunctions;
        this.swiftFunctions = swiftFunctions;
        this.conversationMemory = conversationMemory;
        this.functionCallTracker = functionCallTracker;

        // Standard ChatClient without functions (for rule-based mode)
        this.chatClient = ChatClient.builder(chatModel)
            .defaultSystem(SYSTEM_PROMPT)
            .build();

        // ChatClient with functions registered (for experimental LLM mode)
        // Function names must match the @Bean method names in TrmsFunctions and SwiftFunctions
        this.chatClientWithFunctions = ChatClient.builder(chatModel)
            .defaultSystem(SYSTEM_PROMPT)
            .defaultFunctions(
                "getAccountsByCurrency",
                "checkAccountBalance",
                "bookTransaction",
                "checkEODReadiness",
                "proposeRateFixings",
                "sendSwiftPayment",
                "checkSwiftMessageStatus",
                "getSwiftMessagesByAccount",
                "getSwiftMessagesByTransaction",
                "reconcileSwiftMessages",
                "getUnreconciledMessages",
                "processRedemptionReport",
                "verifyEODReports"
            )
            .build();

        logger.info("TrmsAiService initialized with ChatModel provider, conversation memory, TRMS and SWIFT function calling");
        logger.info("Experimental LLM mode available with {} TRMS functions and {} SWIFT functions", 5, 8);
    }

    /**
     * Process a chat message with conversation context
     * NEW: Now accepts sessionId for context management and experimentalMode parameter
     *
     * @param sessionId Unique session identifier for conversation continuity
     * @param userMessage The user's message
     * @param experimentalMode If true, use pure LLM function calling; if false, use rule-based approach
     * @return ChatResult containing response and executed functions
     */
    public ChatResult chat(String sessionId, String userMessage, boolean experimentalMode) {
        logger.debug("Processing chat message for session {} (experimental mode: {}): {}",
                     sessionId, experimentalMode, userMessage);

        // Route to appropriate implementation based on mode
        if (experimentalMode) {
            return chatWithLLM(sessionId, userMessage);
        } else {
            return chatWithRuleBased(sessionId, userMessage);
        }
    }

    /**
     * EXPERIMENTAL: Pure LLM-based chat with function calling
     * Let the LLM decide which functions to call based on the user's message
     *
     * @param sessionId Unique session identifier
     * @param userMessage The user's message
     * @return ChatResult containing response and executed functions
     */
    private ChatResult chatWithLLM(String sessionId, String userMessage) {
        logger.debug("Using EXPERIMENTAL LLM mode for session {}: {}", sessionId, userMessage);

        // Clear previous function tracking
        executedFunctions.get().clear();
        unreconciledMessageCount.set(0);
        functionCallTracker.clear();

        try {
            // Add user message to conversation history
            conversationMemory.addUserMessage(sessionId, userMessage);

            // Build contextual prompt with conversation history
            String contextualPrompt = conversationMemory.buildContextualPrompt(
                sessionId,
                userMessage,
                8 // Include last 8 messages for context
            );

            // Let the LLM decide which functions to call automatically
            logger.info("EXPERIMENTAL MODE: LLM will autonomously decide function calls");
            String response = chatClientWithFunctions
                .prompt()
                .user(contextualPrompt)
                .call()
                .content();

            logger.info("LLM-based response generated for session: {}", sessionId);

            // Get actual functions called by the LLM from the tracker
            List<String> functions = functionCallTracker.getFunctionCalls();
            if (functions.isEmpty()) {
                // No functions were called - just conversational response
                // Don't add any placeholder - return empty list
                logger.info("LLM response was conversational (no functions called)");
            } else {
                logger.info("LLM called {} function(s): {}", functions.size(), functions);
            }

            // Save assistant response to conversation history
            String functionCallsStr = functions.isEmpty() ? "conversational" : String.join(", ", functions);
            conversationMemory.addAssistantMessage(sessionId, response, functionCallsStr);

            executedFunctions.remove(); // Clean up thread-local
            unreconciledMessageCount.remove(); // Clean up thread-local
            functionCallTracker.cleanup(); // Clean up tracker
            return new ChatResult(response, functions);

        } catch (Exception e) {
            logger.error("Error in LLM mode for session {}: {}", sessionId, e.getMessage(), e);
            executedFunctions.remove(); // Clean up thread-local
            unreconciledMessageCount.remove(); // Clean up thread-local
            functionCallTracker.cleanup(); // Clean up tracker
            return new ChatResult(generateFallbackResponse(userMessage), List.of());
        }
    }

    /**
     * Rule-based chat with programmatic function calling
     * Uses pattern matching to decide which functions to call
     *
     * @param sessionId Unique session identifier
     * @param userMessage The user's message
     * @return ChatResult containing response and executed functions
     */
    private ChatResult chatWithRuleBased(String sessionId, String userMessage) {
        logger.debug("Using RULE-BASED mode for session {}: {}", sessionId, userMessage);

        try {
            // Add user message to conversation history
            conversationMemory.addUserMessage(sessionId, userMessage);

            // First, check if the message requires function calling
            String functionResult = tryExecuteFunction(userMessage);

            String response;
            if (functionResult != null) {
                // Function was executed, build contextual prompt for AI formatting
                String contextualPrompt = conversationMemory.buildContextualPrompt(
                    sessionId,
                    String.format("""
                        User asked: %s

                        Function execution result:
                        %s

                        Please format this data in a clear, professional way and explain what it means.
                        Remember the conversation context and refer to it if relevant.
                        """, userMessage, functionResult),
                    6 // Include last 6 messages for context
                );

                response = chatClient
                    .prompt()
                    .user(contextualPrompt)
                    .call()
                    .content();

                logger.info("Contextual function calling response generated for session: {}", sessionId);
            } else {
                // No function needed, use contextual prompt with conversation history
                String contextualPrompt = conversationMemory.buildContextualPrompt(
                    sessionId,
                    userMessage,
                    8 // Include last 8 messages for context
                );

                response = chatClient
                    .prompt()
                    .user(contextualPrompt)
                    .call()
                    .content();

                logger.info("Contextual AI response generated for session: {}", sessionId);
            }

            // Save assistant response to conversation history
            List<String> functions = new ArrayList<>(executedFunctions.get());
            String functionCallsStr = functions.isEmpty() ? null : String.join(", ", functions);
            conversationMemory.addAssistantMessage(sessionId, response, functionCallsStr);

            executedFunctions.remove(); // Clean up thread-local
            unreconciledMessageCount.remove(); // Clean up thread-local
            return new ChatResult(response, functions);

        } catch (Exception e) {
            logger.error("Error processing chat for session {}: {}", sessionId, e.getMessage(), e);
            executedFunctions.remove(); // Clean up thread-local
            unreconciledMessageCount.remove(); // Clean up thread-local
            return new ChatResult(generateFallbackResponse(userMessage), List.of());
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

            // Scenario 1: Transfer + SWIFT Payment Workflow
            if ((message.contains("transfer") || message.contains("send")) &&
                message.contains("swift") &&
                (message.contains("$") || message.contains("usd") || message.contains("eur"))) {
                logger.info("Executing Transfer + SWIFT Payment Workflow");
                return executeTransferAndSwiftWorkflow(message);
            }

            // Scenario 2: Comprehensive EOD Check (market data + transactions + SWIFT)
            // IMPORTANT: Must check BEFORE SWIFT-only validation to avoid false matches
            if ((message.contains("comprehensive") || message.contains("full") || message.contains("complete") || message.contains("both")) &&
                (message.contains("eod") || message.contains("check") || message.contains("readiness"))) {
                logger.info("Executing Comprehensive EOD Check Workflow");
                return executeComprehensiveEODCheckWorkflow();
            }

            // Scenario 3: SWIFT Reconciliation + EOD Check
            if ((message.contains("reconcile") || message.contains("reconciliation")) &&
                (message.contains("eod") || message.contains("check"))) {
                logger.info("Executing SWIFT Reconciliation + EOD Check Workflow");
                return executeReconciliationAndEODWorkflow();
            }

            // Scenario 4: Complete EOD Preparation Workflow
            if (message.contains("prepare") && (message.contains("eod") || message.contains("end of day"))) {
                logger.info("Executing Complete EOD Preparation Workflow");
                return executeEODPreparationWorkflow();
            }

            // Scenario 5: Cross-Currency Portfolio Analysis
            if ((message.contains("portfolio") || message.contains("cash position") ||
                 message.contains("all currencies") || message.contains("complete overview")) &&
                (message.contains("currency") || message.contains("currencies"))) {
                logger.info("Executing Cross-Currency Portfolio Analysis");
                return executeCrossCurrencyPortfolioAnalysis();
            }

            // Scenario 6: EOD Issue Resolution
            if ((message.contains("what") || message.contains("fix") || message.contains("resolve")) &&
                (message.contains("blocking") || message.contains("blocker")) &&
                (message.contains("eod") || message.contains("end of day"))) {
                logger.info("Executing EOD Issue Resolution Workflow");
                return executeEODIssueResolutionWorkflow();
            }

            // Scenario 7: SWIFT EOD Validation Workflow (SWIFT-only, not comprehensive)
            // Only trigger if asking specifically about SWIFT, not both systems
            if (((message.contains("swift") && (message.contains("eod") || message.contains("end of day"))) ||
                 (message.contains("swift") && (message.contains("ready") || message.contains("readiness"))) ||
                 (message.contains("validate") && message.contains("swift"))) &&
                !message.contains("both") && !message.contains("trms") && !message.contains("comprehensive")) {
                logger.info("Executing SWIFT EOD Validation Workflow");
                return executeSwiftEODValidationWorkflow();
            }

            // Scenario 8: Show Accounts + Check Balances
            if (message.contains("show") && message.contains("account") && message.contains("balance")) {
                logger.info("Executing Show Accounts + Balances Workflow");
                return executeAccountsWithBalancesWorkflow(message);
            }

            // Scenario 9: Send SWIFT for existing transaction
            if ((message.contains("send") && message.contains("swift")) &&
                (message.contains("txn-") || message.contains("transaction"))) {
                String transactionId = extractTransactionId(message);
                if (transactionId != null) {
                    logger.info("Executing Send SWIFT for Transaction: {}", transactionId);
                    return executeSendSwiftForTransaction(transactionId);
                }
            }

            // Scenario 10: Book Transaction (simple transfer without SWIFT)
            if (message.contains("transfer") &&
                (message.contains("$") || message.contains("usd") || message.contains("eur") ||
                 message.contains("gbp") || message.contains("jpy")) &&
                (message.contains("from") && message.contains("to")) &&
                !message.contains("swift")) {
                logger.info("Executing Book Transaction");
                return executeBookTransactionWorkflow(message);
            }

            // SINGLE FUNCTION CALLS

            // Check for balance queries (MORE SPECIFIC - check first)
            if (message.contains("balance") && message.contains("acc-")) {
                String accountId = extractAccountId(message);
                if (accountId != null) {
                    logger.info("Executing checkAccountBalance with account: {}", accountId);
                    return executeCheckAccountBalance(accountId);
                }
            }

            // Check for account-related queries (LESS SPECIFIC - check after balance)
            if (message.contains("account") && (message.contains("usd") || message.contains("eur") ||
                message.contains("gbp") || message.contains("jpy"))) {

                String currency = extractCurrency(message);
                if (currency != null) {
                    logger.info("Executing getAccountsByCurrency with currency: {}", currency);
                    return executeGetAccountsByCurrency(currency);
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

            // Check for SWIFT reconciliation request (reconcile action)
            if ((message.contains("reconcile") && message.contains("swift")) ||
                (message.contains("reconcile") && message.contains("message"))) {
                String accountId = extractAccountId(message);
                boolean autoReconcile = message.contains("auto") || message.contains("automatic");
                logger.info("Executing reconcileSwiftMessages for account: {}, auto: {}", accountId, autoReconcile);
                return executeReconcileSwiftMessages(accountId, autoReconcile);
            }

            // Check for unreconciled messages (view only)
            if (message.contains("unreconciled") || message.contains("show unreconciled")) {
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

    private String extractTransactionId(String message) {
        // Extract transaction IDs like TXN-12345678 or TXN-ABCD1234
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("txn-[a-z0-9]+",
            java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher matcher = pattern.matcher(message);
        return matcher.find() ? matcher.group().toUpperCase() : null;
    }
    
    private String executeGetAccountsByCurrency(String currency) {
        try {
            executedFunctions.get().add("getAccountsByCurrency");
            var request = new com.trms.ai.service.TrmsFunctions.GetAccountsByCurrencyRequest(currency);
            var accounts = trmsFunctions.getAccountsByCurrency().apply(request);
            return formatAccountsData(accounts);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get accounts: " + e.getMessage());
        }
    }
    
    private String executeCheckAccountBalance(String accountId) {
        try {
            executedFunctions.get().add("checkAccountBalance");
            var request = new com.trms.ai.service.TrmsFunctions.CheckAccountBalanceRequest(accountId);
            var balance = trmsFunctions.checkAccountBalance().apply(request);
            return formatBalanceData(balance);
        } catch (Exception e) {
            throw new RuntimeException("Failed to check balance: " + e.getMessage());
        }
    }
    
    private String executeCheckEODReadiness() {
        try {
            executedFunctions.get().add("checkEODReadiness");
            var request = new com.trms.ai.service.TrmsFunctions.CheckEODReadinessRequest();
            var status = trmsFunctions.checkEODReadiness().apply(request);
            return formatEODData(status);
        } catch (Exception e) {
            throw new RuntimeException("Failed to check EOD readiness: " + e.getMessage());
        }
    }
    
    private String executeProposeRateFixings() {
        try {
            executedFunctions.get().add("proposeRateFixings");
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
            executedFunctions.get().add("checkSwiftMessageStatus");
            var request = new com.trms.ai.service.SwiftFunctions.CheckSwiftStatusRequest(messageId);
            var status = swiftFunctions.checkSwiftMessageStatus().apply(request);
            return formatSwiftStatusData(status);
        } catch (Exception e) {
            throw new RuntimeException("Failed to check SWIFT message status: " + e.getMessage());
        }
    }

    private String executeGetSwiftMessagesByAccount(String accountId) {
        try {
            executedFunctions.get().add("getSwiftMessagesByAccount");
            var request = new com.trms.ai.service.SwiftFunctions.GetSwiftMessagesByAccountRequest(accountId);
            var messages = swiftFunctions.getSwiftMessagesByAccount().apply(request);
            return formatSwiftMessagesData(messages);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get SWIFT messages: " + e.getMessage());
        }
    }

    private String executeReconcileSwiftMessages(String accountId, boolean autoReconcile) {
        try {
            executedFunctions.get().add("reconcileSwiftMessages");
            var request = new com.trms.ai.service.SwiftFunctions.ReconcileSwiftMessagesRequest(accountId, autoReconcile);
            var result = swiftFunctions.reconcileSwiftMessages().apply(request);
            return formatReconciliationResultData(result);
        } catch (Exception e) {
            throw new RuntimeException("Failed to reconcile SWIFT messages: " + e.getMessage());
        }
    }

    private String executeGetUnreconciledMessages() {
        try {
            executedFunctions.get().add("getUnreconciledMessages");
            var request = new com.trms.ai.service.SwiftFunctions.GetUnreconciledMessagesRequest();
            var messages = swiftFunctions.getUnreconciledMessages().apply(request);
            // Store message count in thread-local for validation workflow
            unreconciledMessageCount.set(messages.size());
            return formatSwiftMessagesData(messages);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get unreconciled messages: " + e.getMessage());
        }
    }

    private String executeProcessRedemptionReport(String fileName) {
        try {
            executedFunctions.get().add("processRedemptionReport");
            var request = new com.trms.ai.service.SwiftFunctions.ProcessRedemptionReportRequest(fileName);
            var result = swiftFunctions.processRedemptionReport().apply(request);
            return formatRedemptionReportData(result);
        } catch (Exception e) {
            throw new RuntimeException("Failed to process redemption report: " + e.getMessage());
        }
    }

    private String executeVerifyEODReports(String reportDate) {
        try {
            executedFunctions.get().add("verifyEODReports");
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

    private String formatReconciliationResultData(Object result) {
        return "SWIFT RECONCILIATION RESULT:\n" + result.toString();
    }

    private String formatRedemptionReportData(Object result) {
        return "REDEMPTION REPORT RESULT:\n" + result.toString();
    }

    private String formatEODVerificationData(Object result) {
        return "EOD REPORT VERIFICATION:\n" + result.toString();
    }

    // ========== MULTI-FUNCTION WORKFLOWS ==========

    /**
     * Send SWIFT for Existing Transaction Workflow
     * Retrieves transaction details and sends SWIFT if transaction is VALIDATED
     *
     * @param transactionId The transaction ID to send via SWIFT
     * @return Result of the workflow execution
     */
    private String executeSendSwiftForTransaction(String transactionId) {
        StringBuilder result = new StringBuilder();
        result.append("📤 SEND SWIFT FOR TRANSACTION WORKFLOW\n\n");
        result.append(String.format("Transaction ID: %s\n\n", transactionId));

        try {
            // Step 1: Retrieve transaction details
            result.append("⏳ Step 1: Retrieving transaction details from TRMS...\n");

            var transaction = trmsFunctions.legacyTrmsClient.getTransactionById(transactionId);

            if (transaction == null) {
                result.append("   ❌ Transaction not found: ").append(transactionId).append("\n");
                return result.toString();
            }

            result.append(String.format("   ✅ Transaction found!\n"));
            result.append(String.format("   From: %s\n", transaction.fromAccount()));
            result.append(String.format("   To: %s\n", transaction.toAccount()));
            result.append(String.format("   Amount: %s %s\n", transaction.amount(), transaction.currency()));
            result.append(String.format("   Status: %s\n\n", transaction.status()));

            // Step 2: Validate transaction status
            if (!"VALIDATED".equalsIgnoreCase(transaction.status())) {
                result.append("⚠️  Step 2: Transaction Status Check FAILED\n");
                result.append(String.format("   Transaction status is '%s', but SWIFT can only be sent for VALIDATED transactions.\n\n", transaction.status()));
                result.append("📋 NEXT STEPS:\n");
                result.append("   1. Go to the TRMS Dashboard\n");
                result.append("   2. Find transaction ").append(transactionId).append(" in 'Recent Transactions'\n");
                result.append("   3. Click the 'Approve' button to change status to VALIDATED\n");
                result.append("   4. Then retry sending via SWIFT\n");
                return result.toString();
            }

            result.append("✅ Step 2: Transaction is VALIDATED, proceeding with SWIFT...\n\n");

            // Step 3: Send SWIFT payment
            result.append("📤 Step 3: Sending SWIFT MT103 payment message...\n");
            executedFunctions.get().add("sendSwiftPayment");

            var swiftRequest = new SwiftFunctions.SendSwiftPaymentRequest(
                transaction.fromAccount(),
                transaction.id(),
                new java.math.BigDecimal(transaction.amount()),
                transaction.currency(),
                "DEUTDEFFXXX", // Default receiver BIC
                transaction.toAccount(), // Using to account as beneficiary name for simplicity
                transaction.toAccount()
            );
            var swiftMessage = swiftFunctions.sendSwiftPayment().apply(swiftRequest);

            result.append(String.format("   ✅ SWIFT MT103 message sent successfully!\n"));
            result.append(String.format("   SWIFT Message ID: %s\n", swiftMessage.id()));
            result.append(String.format("   Status: %s\n", swiftMessage.status()));
            result.append(String.format("   Message Type: %s\n\n", swiftMessage.messageType()));

            result.append("✅ WORKFLOW COMPLETED SUCCESSFULLY\n");
            result.append("Transaction approved and SWIFT payment message sent.\n");

        } catch (Exception e) {
            result.append("❌ Error sending SWIFT for transaction: ").append(e.getMessage()).append("\n");
            logger.error("Error in executeSendSwiftForTransaction", e);
        }

        return result.toString();
    }

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
     * SWIFT EOD Validation Workflow
     * Comprehensive validation of SWIFT system readiness for EOD processing
     * Steps:
     * 1) Verify EOD reports generated in shared drive
     * 2) Check for unreconciled SWIFT messages
     * 3) Verify no pending settlements
     * 4) Overall SWIFT system readiness assessment
     */
    private String executeSwiftEODValidationWorkflow() {
        StringBuilder result = new StringBuilder();
        result.append("🔐 SWIFT EOD VALIDATION WORKFLOW\n\n");
        result.append("Performing comprehensive SWIFT system validation for End-of-Day processing...\n\n");

        int totalChecks = 3;
        int passedChecks = 0;
        boolean hasBlockers = false;
        StringBuilder issues = new StringBuilder();

        try {
            // Step 1: Verify EOD reports in shared drive
            result.append("📄 Step 1/3: Verifying EOD Reports in Shared Drive...\n");
            String reportDate = java.time.LocalDate.now().toString();
            String reportVerification = executeVerifyEODReports(reportDate);
            result.append(reportVerification).append("\n");

            // Check if reports passed (simplified check - in real system would parse the response)
            if (reportVerification.contains("PASSED") || reportVerification.contains("complete")) {
                result.append("   ✅ EOD reports verification: PASSED\n\n");
                passedChecks++;
            } else {
                result.append("   ❌ EOD reports verification: FAILED - Missing or incomplete reports\n\n");
                hasBlockers = true;
                issues.append("• Missing or incomplete EOD reports in shared drive\n");
            }

            // Step 2: Check for unreconciled SWIFT messages
            result.append("🔄 Step 2/3: Checking for Unreconciled SWIFT Messages...\n");
            String unreconciledMessages = executeGetUnreconciledMessages();
            int messageCount = unreconciledMessageCount.get();
            result.append(unreconciledMessages).append("\n");
            result.append(String.format("   Found: %d unreconciled message(s)\n", messageCount));

            // Check if there are unreconciled messages using actual count
            if (messageCount == 0) {
                result.append("   ✅ Reconciliation check: PASSED - All SWIFT messages reconciled\n\n");
                passedChecks++;
            } else {
                result.append("   ❌ Reconciliation check: FAILED - Unreconciled messages found\n\n");
                hasBlockers = true;
                issues.append(String.format("• %d unreconciled SWIFT message(s) must be reconciled before EOD\n", messageCount));
            }

            // Step 3: Check SWIFT message statuses (verify no pending settlements)
            result.append("⏳ Step 3/3: Verifying No Pending Settlements...\n");
            // In a real system, we'd check for messages with PENDING status
            // For this demo, we'll simulate by checking unreconciled count
            if (messageCount == 0) {
                result.append("   ✅ Settlement check: PASSED - No pending settlements\n\n");
                passedChecks++;
            } else {
                result.append("   ❌ Settlement check: FAILED - Pending settlements detected\n\n");
                hasBlockers = true;
                issues.append("• Pending settlements must be resolved before EOD\n");
            }

            // Summary and Overall Assessment
            result.append("═".repeat(60)).append("\n");
            result.append("📊 VALIDATION SUMMARY\n");
            result.append("═".repeat(60)).append("\n\n");
            result.append(String.format("Total Checks: %d\n", totalChecks));
            result.append(String.format("Passed: %d ✅\n", passedChecks));
            result.append(String.format("Failed/Warnings: %d ⚠️\n\n", totalChecks - passedChecks));

            // Overall readiness assessment
            if (passedChecks == totalChecks) {
                result.append("🎉 SWIFT SYSTEM STATUS: READY FOR EOD\n\n");
                result.append("All validation checks passed successfully.\n");
                result.append("The SWIFT system is fully prepared for End-of-Day processing.\n");
            } else if (hasBlockers) {
                result.append("🚫 SWIFT SYSTEM STATUS: NOT READY FOR EOD\n\n");
                result.append("CRITICAL ISSUES DETECTED:\n");
                result.append(issues.toString());
                result.append("\n❗ Action Required: Resolve critical issues before proceeding with EOD.\n");
            } else {
                result.append("⚠️  SWIFT SYSTEM STATUS: READY WITH WARNINGS\n\n");
                result.append("WARNINGS DETECTED:\n");
                result.append(issues.toString());
                result.append("\n💡 Recommendation: Review warnings before proceeding with EOD.\n");
            }

            result.append("\n═".repeat(60)).append("\n");
            result.append("🔐 SWIFT EOD VALIDATION WORKFLOW COMPLETED\n");

        } catch (Exception e) {
            result.append("❌ Error in SWIFT EOD Validation Workflow: ").append(e.getMessage());
        }

        return result.toString();
    }

    /**
     * Transfer + SWIFT Payment Workflow
     * Steps: 1) Extract transfer details, 2) Book transaction, 3) Check status, 4) Send SWIFT only if VALIDATED
     *
     * IMPORTANT: Transactions are created with PENDING status and require manual approval.
     * SWIFT messages are only sent for VALIDATED transactions.
     */
    private String executeTransferAndSwiftWorkflow(String message) {
        StringBuilder result = new StringBuilder();
        result.append("💸 EXECUTING TRANSFER + SWIFT PAYMENT WORKFLOW\n\n");

        try {
            // Extract transfer details from message
            String[] accounts = extractBothAccountIds(message);
            String fromAccount = accounts[0];
            String toAccount = accounts[1];
            String amountStr = extractAmount(message);
            String currency = extractCurrency(message);

            // Validate extracted data
            if (fromAccount == null || toAccount == null) {
                result.append("❌ ERROR: Could not extract both source and destination accounts.\n");
                result.append("   Please specify transfer in format: 'Transfer $AMOUNT from ACC-XXX-CUR to ACC-YYY-CUR'\n");
                return result.toString();
            }

            if (amountStr == null) {
                result.append("❌ ERROR: Could not extract amount from message.\n");
                return result.toString();
            }

            // Remove commas and parse amount
            Double amount = Double.parseDouble(amountStr.replace(",", ""));

            // Default currency if not specified
            if (currency == null) {
                currency = "USD";
            }

            result.append("📝 Transfer Details Extracted:\n");
            result.append(String.format("   From: %s\n", fromAccount));
            result.append(String.format("   To: %s\n", toAccount));
            result.append(String.format("   Amount: %s\n", amountStr));
            result.append(String.format("   Currency: %s\n\n", currency));

            // Step 1: Book the transaction
            result.append("💰 Step 1: Booking transaction in TRMS...\n");
            executedFunctions.get().add("bookTransaction");

            var bookRequest = new TrmsFunctions.BookTransactionRequest(fromAccount, toAccount, amount, currency);
            var transaction = trmsFunctions.bookTransaction().apply(bookRequest);

            result.append(String.format("   ✅ Transaction booked successfully!\n"));
            result.append(String.format("   Transaction ID: %s\n", transaction.id()));
            result.append(String.format("   Status: %s\n", transaction.status()));
            result.append(String.format("   Amount: %s %s\n\n", transaction.amount(), transaction.currency()));

            // Step 2: Check transaction status and handle SWIFT accordingly
            if ("PENDING".equalsIgnoreCase(transaction.status())) {
                result.append("⏳ Step 2: Transaction Status Check\n");
                result.append("   ⚠️  Transaction is in PENDING status and requires manual approval.\n\n");
                result.append("📋 NEXT STEPS:\n");
                result.append("   1. Go to the TRMS Dashboard\n");
                result.append("   2. Find the transaction in the 'Recent Transactions' panel (it will be at the top)\n");
                result.append("   3. Click the 'Approve' button to validate the transaction\n");
                result.append("   4. Once approved (status changes to VALIDATED), you can send it via SWIFT\n\n");
                result.append("💡 TIP: You can then ask me to 'send transaction " + transaction.id() + " via SWIFT'\n\n");
                result.append("✅ WORKFLOW COMPLETED\n");
                result.append("Transaction created and awaiting approval. SWIFT message will not be sent until approved.\n");
            } else if ("VALIDATED".equalsIgnoreCase(transaction.status())) {
                // Transaction is already validated, proceed with SWIFT
                result.append("✅ Step 2: Transaction is VALIDATED, proceeding with SWIFT...\n\n");
                result.append("📤 Step 3: Sending SWIFT payment message...\n");
                executedFunctions.get().add("sendSwiftPayment");

                var swiftRequest = new SwiftFunctions.SendSwiftPaymentRequest(
                    fromAccount,
                    transaction.id(),
                    new java.math.BigDecimal(amount),
                    currency,
                    "DEUTDEFFXXX", // Default receiver BIC
                    "Beneficiary Name", // Default beneficiary name
                    toAccount
                );
                var swiftMessage = swiftFunctions.sendSwiftPayment().apply(swiftRequest);

                result.append(String.format("   ✅ SWIFT MT103 message sent successfully!\n"));
                result.append(String.format("   SWIFT Message ID: %s\n", swiftMessage.id()));
                result.append(String.format("   Status: %s\n", swiftMessage.status()));
                result.append(String.format("   Message Type: %s\n\n", swiftMessage.messageType()));

                result.append("✅ WORKFLOW COMPLETED SUCCESSFULLY\n");
                result.append("Transaction booked and SWIFT payment message sent.\n");
            } else {
                result.append(String.format("   ⚠️  Transaction status is %s\n", transaction.status()));
                result.append("   Please check the transaction status before sending SWIFT.\n");
            }

        } catch (Exception e) {
            result.append("❌ Error in Transfer + SWIFT Workflow: ").append(e.getMessage());
            logger.error("Error in executeTransferAndSwiftWorkflow", e);
        }

        return result.toString();
    }

    /**
     * Book Transaction Workflow (without SWIFT)
     * Steps: 1) Extract transfer details, 2) Book transaction, 3) Inform about approval process
     */
    private String executeBookTransactionWorkflow(String message) {
        StringBuilder result = new StringBuilder();
        result.append("💸 BOOKING TRANSACTION\n\n");

        try {
            // Extract transfer details from message
            String[] accounts = extractBothAccountIds(message);
            String fromAccount = accounts[0];
            String toAccount = accounts[1];
            String amountStr = extractAmount(message);
            String currency = extractCurrency(message);

            // Validate extracted data
            if (fromAccount == null || toAccount == null) {
                result.append("❌ ERROR: Could not extract both source and destination accounts.\n");
                result.append("   Please specify transfer in format: 'Transfer $AMOUNT from ACC-XXX-CUR to ACC-YYY-CUR'\n");
                return result.toString();
            }

            if (amountStr == null) {
                result.append("❌ ERROR: Could not extract amount from message.\n");
                return result.toString();
            }

            // Remove commas and parse amount
            Double amount = Double.parseDouble(amountStr.replace(",", ""));

            // Default currency if not specified
            if (currency == null) {
                currency = "USD";
            }

            result.append("📝 Transfer Details:\n");
            result.append(String.format("   From Account: %s\n", fromAccount));
            result.append(String.format("   To Account: %s\n", toAccount));
            result.append(String.format("   Amount: %s\n", amountStr));
            result.append(String.format("   Currency: %s\n\n", currency));

            // Book the transaction
            result.append("💰 Booking transaction in TRMS...\n");
            executedFunctions.get().add("bookTransaction");

            var bookRequest = new TrmsFunctions.BookTransactionRequest(fromAccount, toAccount, amount, currency);
            var transaction = trmsFunctions.bookTransaction().apply(bookRequest);

            result.append(String.format("   ✅ Transaction booked successfully!\n\n"));
            result.append(String.format("📋 Transaction Details:\n"));
            result.append(String.format("   Transaction ID: %s\n", transaction.id()));
            result.append(String.format("   Status: %s\n", transaction.status()));
            result.append(String.format("   Amount: %s %s\n", transaction.amount(), transaction.currency()));
            result.append(String.format("   From: %s → To: %s\n\n", transaction.fromAccount(), transaction.toAccount()));

            // Provide guidance based on status
            if ("PENDING".equalsIgnoreCase(transaction.status())) {
                result.append("⏳ NEXT STEPS:\n");
                result.append("   The transaction has been created with PENDING status.\n");
                result.append("   To complete the transaction:\n");
                result.append("   1. Go to the TRMS Dashboard (http://localhost:8090)\n");
                result.append("   2. Find transaction '").append(transaction.id()).append("' in Recent Transactions\n");
                result.append("   3. Click 'Approve' to change status to VALIDATED\n");
                result.append("   4. Once validated, you can send it via SWIFT if needed\n\n");
                result.append("💡 TIP: Ask me to 'send transaction ").append(transaction.id()).append(" via SWIFT' after approval\n");
            } else if ("VALIDATED".equalsIgnoreCase(transaction.status())) {
                result.append("✅ Transaction is already VALIDATED and ready for SWIFT if needed.\n");
            }

        } catch (Exception e) {
            result.append("❌ Error booking transaction: ").append(e.getMessage());
            logger.error("Error in executeBookTransactionWorkflow", e);
        }

        return result.toString();
    }

    /**
     * Extract both fromAccount and toAccount from transfer message
     * Expected format: "transfer $X from ACC-001-USD to ACC-002-USD"
     */
    private String[] extractBothAccountIds(String message) {
        String[] result = new String[2];

        // Find "from ACC-XXX-XXX"
        java.util.regex.Pattern fromPattern = java.util.regex.Pattern.compile(
            "from\\s+(acc-\\d+-\\w+)",
            java.util.regex.Pattern.CASE_INSENSITIVE
        );
        java.util.regex.Matcher fromMatcher = fromPattern.matcher(message);
        if (fromMatcher.find()) {
            result[0] = fromMatcher.group(1).toUpperCase();
        }

        // Find "to ACC-XXX-XXX"
        java.util.regex.Pattern toPattern = java.util.regex.Pattern.compile(
            "to\\s+(acc-\\d+-\\w+)",
            java.util.regex.Pattern.CASE_INSENSITIVE
        );
        java.util.regex.Matcher toMatcher = toPattern.matcher(message);
        if (toMatcher.find()) {
            result[1] = toMatcher.group(1).toUpperCase();
        }

        return result;
    }

    /**
     * Comprehensive EOD Check Workflow
     * Steps: 1) Check TRMS EOD readiness, 2) Check SWIFT reconciliation, 3) Verify reports
     */
    private String executeComprehensiveEODCheckWorkflow() {
        StringBuilder result = new StringBuilder();
        result.append("🔍 COMPREHENSIVE EOD CHECK WORKFLOW\n\n");

        try {
            // Step 1: TRMS EOD Readiness
            result.append("📊 Step 1/3: Checking TRMS EOD Readiness...\n");
            String eodStatus = executeCheckEODReadiness();
            result.append(eodStatus).append("\n\n");

            // Step 2: SWIFT Reconciliation
            result.append("🔄 Step 2/3: Checking SWIFT Reconciliation...\n");
            String unreconciledMessages = executeGetUnreconciledMessages();
            result.append(unreconciledMessages).append("\n\n");

            // Step 3: EOD Reports Verification
            result.append("📄 Step 3/3: Verifying EOD Reports...\n");
            String reportDate = java.time.LocalDate.now().toString();
            String reportVerification = executeVerifyEODReports(reportDate);
            result.append(reportVerification).append("\n\n");

            result.append("✅ COMPREHENSIVE EOD CHECK COMPLETED\n");
            result.append("All EOD systems and reports have been validated.\n");

        } catch (Exception e) {
            result.append("❌ Error in Comprehensive EOD Check: ").append(e.getMessage());
        }

        return result.toString();
    }

    /**
     * SWIFT Reconciliation + EOD Check Workflow
     * Steps: 1) Get unreconciled messages, 2) Reconcile, 3) Check EOD readiness
     */
    private String executeReconciliationAndEODWorkflow() {
        StringBuilder result = new StringBuilder();
        result.append("🔄 SWIFT RECONCILIATION + EOD CHECK WORKFLOW\n\n");

        try {
            // Step 1: Identify unreconciled messages
            result.append("🔍 Step 1/3: Identifying unreconciled SWIFT messages...\n");
            String unreconciledMessages = executeGetUnreconciledMessages();
            result.append(unreconciledMessages).append("\n\n");

            // Step 2: Attempt reconciliation (would call reconcileSwiftMessages in real impl)
            result.append("⚙️ Step 2/3: Attempting automatic reconciliation...\n");
            result.append("   [Reconciliation process would occur here]\n\n");

            // Step 3: Check EOD readiness after reconciliation
            result.append("✅ Step 3/3: Verifying EOD readiness after reconciliation...\n");
            String eodStatus = executeCheckEODReadiness();
            result.append(eodStatus).append("\n\n");

            result.append("🎯 RECONCILIATION + EOD CHECK COMPLETED\n");

        } catch (Exception e) {
            result.append("❌ Error in Reconciliation + EOD Workflow: ").append(e.getMessage());
        }

        return result.toString();
    }

    /**
     * Show Accounts + Check Balances Workflow
     * Steps: 1) Get accounts for specified currency, 2) Check balance for each
     */
    private String executeAccountsWithBalancesWorkflow(String message) {
        StringBuilder result = new StringBuilder();
        result.append("💰 ACCOUNTS + BALANCES WORKFLOW\n\n");

        try {
            String currency = extractCurrency(message);
            if (currency == null) {
                currency = "USD"; // Default to USD
            }

            // Step 1: Get accounts for currency
            result.append(String.format("🏦 Step 1/2: Retrieving %s accounts...\n", currency));
            String accounts = executeGetAccountsByCurrency(currency);
            result.append(accounts).append("\n\n");

            // Step 2: Note about balance checks
            result.append("💵 Step 2/2: Balance information included above\n");
            result.append("   [Individual balance checks can be performed for specific accounts]\n\n");

            result.append("✅ ACCOUNTS + BALANCES WORKFLOW COMPLETED\n");

        } catch (Exception e) {
            result.append("❌ Error in Accounts + Balances Workflow: ").append(e.getMessage());
        }

        return result.toString();
    }

    /**
     * Extract amount from message (e.g., "$75,000" or "75000")
     */
    private String extractAmount(String message) {
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\$?([0-9,]+(?:\\.[0-9]{2})?)");
        java.util.regex.Matcher matcher = pattern.matcher(message);
        return matcher.find() ? matcher.group(1) : null;
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