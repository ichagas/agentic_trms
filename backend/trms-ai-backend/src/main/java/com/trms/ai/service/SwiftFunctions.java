package com.trms.ai.service;

import com.trms.ai.client.SwiftClient;
import com.trms.ai.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Function;

/**
 * SWIFT Functions for Spring AI integration
 *
 * This service provides AI-callable functions for SWIFT messaging system operations.
 * Each function is annotated with @Bean and @Description to enable Spring AI function calling.
 */
@Service
public class SwiftFunctions {

    private static final Logger logger = LoggerFactory.getLogger(SwiftFunctions.class);

    private final SwiftClient swiftClient;
    private final FunctionCallTracker functionCallTracker;

    public SwiftFunctions(SwiftClient swiftClient, FunctionCallTracker functionCallTracker) {
        this.swiftClient = swiftClient;
        this.functionCallTracker = functionCallTracker;
    }

    /**
     * Function to send SWIFT payment message
     */
    @Bean
    @Description("Send a payment via SWIFT network. Creates an MT103 message for the payment " +
                "and transmits it to the beneficiary's bank. Requires account ID, transaction ID, " +
                "amount, currency, receiver BIC code, beneficiary name and account. " +
                "Returns the SWIFT message details including message ID and status. " +
                "Use this after booking a transaction in TRMS to send the payment confirmation.")
    public Function<SendSwiftPaymentRequest, SwiftMessage> sendSwiftPayment() {
        return request -> {
            functionCallTracker.trackFunctionCall("sendSwiftPayment");
            logger.debug("AI function call: sendSwiftPayment for account: {}", request.accountId());
            try {
                SwiftMessage message = swiftClient.sendSwiftPayment(
                        request.accountId(),
                        request.transactionId(),
                        request.amount(),
                        request.currency(),
                        request.receiverBIC(),
                        request.beneficiaryName(),
                        request.beneficiaryAccount()
                );
                logger.info("SWIFT payment sent: {}", message.id());
                return message;
            } catch (Exception e) {
                logger.error("Error in sendSwiftPayment function: {}", e.getMessage());
                throw new RuntimeException("Failed to send SWIFT payment: " + e.getMessage());
            }
        };
    }

    /**
     * Function to check SWIFT message status
     */
    @Bean
    @Description("Check the status of a SWIFT message by message ID. Returns detailed status " +
                "information including whether the message was sent, confirmed, and if it has been " +
                "reconciled with a TRMS transaction. Status can be: PENDING, SENT, CONFIRMED, " +
                "FAILED, RECONCILED, or UNRECONCILED. Use this to verify if a payment message " +
                "was successfully transmitted and acknowledged.")
    public Function<CheckSwiftStatusRequest, MessageStatusResponse> checkSwiftMessageStatus() {
        return request -> {
            functionCallTracker.trackFunctionCall("checkSwiftMessageStatus");
            logger.debug("AI function call: checkSwiftMessageStatus for message: {}", request.messageId());
            try {
                MessageStatusResponse status = swiftClient.getMessageStatus(request.messageId());
                logger.info("Retrieved status for SWIFT message {}: {}", request.messageId(), status.status());
                return status;
            } catch (Exception e) {
                logger.error("Error in checkSwiftMessageStatus function: {}", e.getMessage());
                throw new RuntimeException("Failed to check SWIFT message status: " + e.getMessage());
            }
        };
    }

    /**
     * Function to get SWIFT messages by account
     */
    @Bean
    @Description("Get all SWIFT messages for a specific account. Returns a list of all SWIFT " +
                "payment messages (MT103, MT202, etc.) that were sent from or received by the " +
                "specified account. Includes message details such as amount, currency, beneficiary, " +
                "status, and timestamps. Useful for tracking payment history and reconciliation.")
    public Function<GetSwiftMessagesByAccountRequest, List<SwiftMessage>> getSwiftMessagesByAccount() {
        return request -> {
            functionCallTracker.trackFunctionCall("getSwiftMessagesByAccount");
            logger.debug("AI function call: getSwiftMessagesByAccount for account: {}", request.accountId());
            try {
                List<SwiftMessage> messages = swiftClient.getMessagesByAccount(request.accountId());
                logger.info("Retrieved {} SWIFT messages for account: {}", messages.size(), request.accountId());
                return messages;
            } catch (Exception e) {
                logger.error("Error in getSwiftMessagesByAccount function: {}", e.getMessage());
                throw new RuntimeException("Failed to get SWIFT messages: " + e.getMessage());
            }
        };
    }

    /**
     * Function to get SWIFT messages by transaction
     */
    @Bean
    @Description("Get SWIFT messages associated with a specific TRMS transaction ID. Returns all " +
                "SWIFT messages that were sent for a particular transaction. This is useful for " +
                "verifying that a transaction was communicated via SWIFT and checking the status " +
                "of the payment instruction.")
    public Function<GetSwiftMessagesByTransactionRequest, List<SwiftMessage>> getSwiftMessagesByTransaction() {
        return request -> {
            functionCallTracker.trackFunctionCall("getSwiftMessagesByTransaction");
            logger.debug("AI function call: getSwiftMessagesByTransaction for transaction: {}", request.transactionId());
            try {
                List<SwiftMessage> messages = swiftClient.getMessagesByTransaction(request.transactionId());
                logger.info("Retrieved {} SWIFT messages for transaction: {}", messages.size(), request.transactionId());
                return messages;
            } catch (Exception e) {
                logger.error("Error in getSwiftMessagesByTransaction function: {}", e.getMessage());
                throw new RuntimeException("Failed to get SWIFT messages: " + e.getMessage());
            }
        };
    }

    /**
     * Function to reconcile SWIFT messages
     */
    @Bean
    @Description("Reconcile SWIFT messages with TRMS transactions. Checks all SWIFT messages " +
                "and matches them with corresponding transactions in the TRMS system. Reconciliation " +
                "is based on transaction ID, amount, and currency matching. Returns a summary showing " +
                "how many messages were reconciled, unreconciled, or pending. Can optionally auto-reconcile " +
                "messages that have clear matches. Use this for EOD processing or to identify discrepancies.")
    public Function<ReconcileSwiftMessagesRequest, ReconciliationResult> reconcileSwiftMessages() {
        return request -> {
            functionCallTracker.trackFunctionCall("reconcileSwiftMessages");
            logger.debug("AI function call: reconcileSwiftMessages");
            try {
                ReconciliationResult result = swiftClient.reconcileMessages(
                        request.accountId(),
                        request.autoReconcile()
                );
                logger.info("Reconciliation completed: {}", result.summary());
                return result;
            } catch (Exception e) {
                logger.error("Error in reconcileSwiftMessages function: {}", e.getMessage());
                throw new RuntimeException("Failed to reconcile SWIFT messages: " + e.getMessage());
            }
        };
    }

    /**
     * Function to get unreconciled SWIFT messages
     */
    @Bean
    @Description("Get all unreconciled SWIFT messages that need attention. Returns messages " +
                "with status UNRECONCILED or SENT that haven't been matched with TRMS transactions. " +
                "These messages may indicate payment discrepancies, missing transaction records, " +
                "or issues that need manual review. Important for EOD reconciliation checks.")
    public Function<GetUnreconciledMessagesRequest, List<SwiftMessage>> getUnreconciledMessages() {
        return request -> {
            functionCallTracker.trackFunctionCall("getUnreconciledMessages");
            logger.debug("AI function call: getUnreconciledMessages");
            try {
                List<SwiftMessage> messages = swiftClient.getUnreconciledMessages();
                logger.info("Retrieved {} unreconciled SWIFT messages", messages.size());
                return messages;
            } catch (Exception e) {
                logger.error("Error in getUnreconciledMessages function: {}", e.getMessage());
                throw new RuntimeException("Failed to get unreconciled messages: " + e.getMessage());
            }
        };
    }

    /**
     * Function to process redemption report
     */
    @Bean
    @Description("Process a redemption report file from the shared drive. The report contains " +
                "redemption requests that need to be processed - typically 96 pages of account data " +
                "including account IDs, beneficiary details, amounts, and currencies. The system " +
                "reads the file, parses all redemption entries, validates the data, and returns " +
                "a summary of processed items. Specify the filename in the redemption reports directory. " +
                "Returns count of successful/failed items and total amount. This automates manual data entry.")
    public Function<ProcessRedemptionReportRequest, RedemptionReportResult> processRedemptionReport() {
        return request -> {
            functionCallTracker.trackFunctionCall("processRedemptionReport");
            logger.debug("AI function call: processRedemptionReport for file: {}", request.fileName());
            try {
                RedemptionReportResult result = swiftClient.processRedemptionReport(request.fileName());
                logger.info("Processed redemption report: {}", result.summary());
                return result;
            } catch (Exception e) {
                logger.error("Error in processRedemptionReport function: {}", e.getMessage());
                throw new RuntimeException("Failed to process redemption report: " + e.getMessage());
            }
        };
    }

    /**
     * Function to verify EOD reports
     */
    @Bean
    @Description("Verify End-of-Day reports in the shared drive for a specific date. Checks " +
                "for the existence and validity of all required EOD reports including balance reports, " +
                "transaction logs, SWIFT reconciliation reports, and settlement reports. Validates " +
                "that each report file exists, is not empty, and contains expected data. Returns " +
                "a detailed verification result showing which reports passed/failed and any issues found. " +
                "Specify the date in YYYY-MM-DD format. Use this to automate manual EOD report verification.")
    public Function<VerifyEODReportsRequest, EODReportVerificationResult> verifyEODReports() {
        return request -> {
            functionCallTracker.trackFunctionCall("verifyEODReports");
            logger.debug("AI function call: verifyEODReports for date: {}", request.reportDate());
            try {
                EODReportVerificationResult result = swiftClient.verifyEODReports(request.reportDate());
                logger.info("EOD report verification completed: {}", result.summary());
                return result;
            } catch (Exception e) {
                logger.error("Error in verifyEODReports function: {}", e.getMessage());
                throw new RuntimeException("Failed to verify EOD reports: " + e.getMessage());
            }
        };
    }

    // Request records for function parameters
    public record SendSwiftPaymentRequest(String accountId, String transactionId, BigDecimal amount,
                                         String currency, String receiverBIC, String beneficiaryName,
                                         String beneficiaryAccount) {}

    public record CheckSwiftStatusRequest(String messageId) {}

    public record GetSwiftMessagesByAccountRequest(String accountId) {}

    public record GetSwiftMessagesByTransactionRequest(String transactionId) {}

    public record ReconcileSwiftMessagesRequest(String accountId, boolean autoReconcile) {}

    public record GetUnreconciledMessagesRequest() {}

    public record ProcessRedemptionReportRequest(String fileName) {}

    public record VerifyEODReportsRequest(String reportDate) {}
}
