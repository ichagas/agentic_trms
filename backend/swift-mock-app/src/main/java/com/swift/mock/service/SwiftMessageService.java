package com.swift.mock.service;

import com.swift.mock.config.SwiftProperties;
import com.swift.mock.dto.*;
import com.swift.mock.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Service for SWIFT message operations
 */
@Service
public class SwiftMessageService {

    private static final Logger logger = LoggerFactory.getLogger(SwiftMessageService.class);

    private final MockSwiftDataService dataService;
    private final SwiftProperties swiftProperties;

    public SwiftMessageService(MockSwiftDataService dataService, SwiftProperties swiftProperties) {
        this.dataService = dataService;
        this.swiftProperties = swiftProperties;
    }

    /**
     * Send a SWIFT message
     */
    public SwiftMessage sendMessage(SendMessageRequest request) {
        logger.info("Sending SWIFT {} message for account: {}", request.getMessageType(), request.getAccountId());

        SwiftMessage message = SwiftMessage.builder()
                .messageType(request.getMessageType())
                .senderBIC(swiftProperties.getOurBic())
                .receiverBIC(request.getReceiverBIC())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .accountId(request.getAccountId())
                .transactionId(request.getTransactionId())
                .status(MessageStatus.SENT)
                .reference(request.getReference() != null ? request.getReference() : "REF-" + UUID.randomUUID().toString().substring(0, 8))
                .valueDate(LocalDate.now())
                .sentTimestamp(LocalDateTime.now())
                .beneficiaryName(request.getBeneficiaryName())
                .beneficiaryAccount(request.getBeneficiaryAccount())
                .orderingCustomer(request.getOrderingCustomer())
                .remittanceInfo(request.getRemittanceInfo())
                .build();

        // Simulate confirmation after a delay (instant for mock)
        message.setStatus(MessageStatus.CONFIRMED);
        message.setConfirmedTimestamp(LocalDateTime.now().plusSeconds(5));

        // Generate raw message content
        message.setRawMessageContent(generateMessageContent(message));

        SwiftMessage savedMessage = dataService.saveMessage(message);

        // Create associated payment
        createPaymentFromMessage(savedMessage);

        logger.info("SWIFT message sent successfully: {}", savedMessage.getId());
        return savedMessage;
    }

    /**
     * Get message status
     */
    public MessageStatusResponse getMessageStatus(String messageId) {
        SwiftMessage message = dataService.getMessage(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found: " + messageId));

        boolean isReconciled = message.getStatus() == MessageStatus.RECONCILED;
        String reconciliationDetails = isReconciled
                ? "Matched with transaction: " + message.getTransactionId()
                : message.getTransactionId() != null
                ? "Transaction ID exists but not reconciled"
                : "No transaction ID linked";

        return MessageStatusResponse.builder()
                .messageId(message.getId())
                .status(message.getStatus())
                .statusDescription(message.getStatus().getDescription())
                .details(String.format("Message Type: %s, Amount: %s %s, Sent: %s",
                        message.getMessageType(),
                        message.getAmount(),
                        message.getCurrency(),
                        message.getSentTimestamp()))
                .isReconciled(isReconciled)
                .reconciliationDetails(reconciliationDetails)
                .build();
    }

    /**
     * Get all messages for an account
     */
    public List<SwiftMessage> getMessagesByAccount(String accountId) {
        logger.info("Retrieving SWIFT messages for account: {}", accountId);
        return dataService.getMessagesByAccount(accountId);
    }

    /**
     * Get messages by transaction ID
     */
    public List<SwiftMessage> getMessagesByTransaction(String transactionId) {
        logger.info("Retrieving SWIFT messages for transaction: {}", transactionId);
        return dataService.getMessagesByTransaction(transactionId);
    }

    /**
     * Reconcile SWIFT messages with transactions
     */
    public ReconciliationResult reconcileMessages(ReconciliationRequest request) {
        logger.info("Starting SWIFT message reconciliation for account: {}", request.getAccountId());

        List<SwiftMessage> allMessages = request.getAccountId() != null
                ? dataService.getMessagesByAccount(request.getAccountId())
                : dataService.getAllMessages();

        List<SwiftMessage> reconciled = new ArrayList<>();
        List<SwiftMessage> unreconciled = new ArrayList<>();
        List<String> issues = new ArrayList<>();

        for (SwiftMessage message : allMessages) {
            if (message.getStatus() == MessageStatus.RECONCILED) {
                reconciled.add(message);
                continue;
            }

            // Check if message has transaction ID
            if (message.getTransactionId() != null && !message.getTransactionId().isEmpty()) {
                // In real system, would verify with TRMS
                // For mock, just mark as reconciled if has transaction ID
                if (request.isAutoReconcile()) {
                    message.setStatus(MessageStatus.RECONCILED);
                    dataService.saveMessage(message);
                }
                reconciled.add(message);
            } else {
                unreconciled.add(message);
                issues.add(String.format("Message %s has no transaction ID", message.getId()));
            }
        }

        int pending = (int) allMessages.stream()
                .filter(m -> m.getStatus() == MessageStatus.PENDING || m.getStatus() == MessageStatus.SENT)
                .count();

        String summary = String.format("Reconciled: %d, Unreconciled: %d, Pending: %d",
                reconciled.size(), unreconciled.size(), pending);

        return ReconciliationResult.builder()
                .totalMessages(allMessages.size())
                .reconciledCount(reconciled.size())
                .unreconciledCount(unreconciled.size())
                .pendingCount(pending)
                .reconciledMessages(reconciled)
                .unreconciledMessages(unreconciled)
                .issues(issues)
                .summary(summary)
                .build();
    }

    /**
     * Get unreconciled messages
     */
    public List<SwiftMessage> getUnreconciledMessages() {
        logger.info("Retrieving unreconciled SWIFT messages");
        return dataService.getUnreconciledMessages();
    }

    /**
     * Get settlements by account
     */
    public List<Settlement> getSettlementsByAccount(String accountId) {
        logger.info("Retrieving settlements for account: {}", accountId);
        return dataService.getSettlementsByAccount(accountId);
    }

    /**
     * Process redemption report from file
     */
    public RedemptionReportResult processRedemptionReport(String fileName) {
        logger.info("Processing redemption report: {}", fileName);

        Path reportPath = Paths.get(swiftProperties.getRedemptionReportsDir(), fileName);

        if (!Files.exists(reportPath)) {
            throw new RuntimeException("Redemption report not found: " + fileName);
        }

        List<RedemptionReportResult.RedemptionItem> redemptions = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        int processedCount = 0;
        int failedCount = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(reportPath.toFile()))) {
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                // Skip header or empty lines
                if (lineNumber == 1 || line.trim().isEmpty()) {
                    continue;
                }

                try {
                    RedemptionReportResult.RedemptionItem item = parseRedemptionLine(line);
                    redemptions.add(item);
                    totalAmount = totalAmount.add(item.getAmount());
                    processedCount++;
                    item.setStatus("PROCESSED");
                } catch (Exception e) {
                    failedCount++;
                    errors.add(String.format("Line %d: %s", lineNumber, e.getMessage()));
                    logger.warn("Failed to parse line {}: {}", lineNumber, e.getMessage());
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("Error reading redemption report: " + e.getMessage(), e);
        }

        String summary = String.format("Processed %d redemptions totaling %s USD. Failed: %d",
                processedCount, totalAmount, failedCount);

        return RedemptionReportResult.builder()
                .reportFileName(fileName)
                .totalRedemptions(processedCount + failedCount)
                .processedCount(processedCount)
                .failedCount(failedCount)
                .totalAmount(totalAmount)
                .currency("USD")
                .redemptions(redemptions)
                .errors(errors)
                .summary(summary)
                .build();
    }

    /**
     * Verify EOD reports
     */
    public EODReportVerificationResult verifyEODReports(String reportDate) {
        logger.info("Verifying EOD reports for date: {}", reportDate);

        String eodDir = swiftProperties.getEodReportsDir();
        Path eodPath = Paths.get(eodDir);

        if (!Files.exists(eodPath)) {
            logger.warn("EOD reports directory does not exist: {}", eodDir);
            try {
                Files.createDirectories(eodPath);
                logger.info("Created EOD reports directory: {}", eodDir);
            } catch (IOException e) {
                throw new RuntimeException("Could not create EOD reports directory", e);
            }
        }

        List<EODReportVerificationResult.ReportCheck> checks = new ArrayList<>();
        List<String> missingReports = new ArrayList<>();
        List<String> issues = new ArrayList<>();

        // Expected reports
        String[] expectedReports = {
                "balance_report_" + reportDate + ".csv",
                "transaction_log_" + reportDate + ".csv",
                "swift_reconciliation_" + reportDate + ".csv",
                "settlement_report_" + reportDate + ".csv"
        };

        int passedChecks = 0;
        int failedChecks = 0;

        for (String reportName : expectedReports) {
            Path reportPath = eodPath.resolve(reportName);
            boolean exists = Files.exists(reportPath);
            boolean isValid = false;

            if (exists) {
                try {
                    isValid = validateReportFile(reportPath);
                } catch (Exception e) {
                    issues.add("Error validating " + reportName + ": " + e.getMessage());
                }
            } else {
                missingReports.add(reportName);
            }

            String status = exists && isValid ? "PASSED" : !exists ? "FAILED" : "WARNING";
            String details = exists && isValid ? "Report exists and is valid"
                    : !exists ? "Report file missing"
                    : "Report exists but validation failed";

            checks.add(EODReportVerificationResult.ReportCheck.builder()
                    .reportName(reportName)
                    .reportType(determineReportType(reportName))
                    .exists(exists)
                    .isValid(isValid)
                    .status(status)
                    .details(details)
                    .build());

            if (exists && isValid) {
                passedChecks++;
            } else {
                failedChecks++;
            }
        }

        boolean isComplete = failedChecks == 0;
        String summary = String.format("EOD Verification: %d/%d checks passed. %s",
                passedChecks, checks.size(),
                isComplete ? "Ready for EOD processing" : failedChecks + " issue(s) found");

        return EODReportVerificationResult.builder()
                .reportDate(reportDate)
                .isComplete(isComplete)
                .totalChecks(checks.size())
                .passedChecks(passedChecks)
                .failedChecks(failedChecks)
                .checks(checks)
                .missingReports(missingReports)
                .issues(issues)
                .summary(summary)
                .build();
    }

    // Helper methods

    private RedemptionReportResult.RedemptionItem parseRedemptionLine(String line) {
        // Expected format: AccountID,BeneficiaryName,BeneficiaryAccount,Amount,Currency,Reference
        String[] parts = line.split(",");

        if (parts.length < 6) {
            throw new RuntimeException("Invalid line format - expected 6 fields");
        }

        return RedemptionReportResult.RedemptionItem.builder()
                .accountId(parts[0].trim())
                .beneficiaryName(parts[1].trim())
                .beneficiaryAccount(parts[2].trim())
                .amount(new BigDecimal(parts[3].trim()))
                .currency(parts[4].trim())
                .reference(parts[5].trim())
                .build();
    }

    private boolean validateReportFile(Path reportPath) throws IOException {
        // Basic validation: file exists, not empty, has header
        long lineCount = Files.lines(reportPath).count();
        return lineCount > 1; // At least header + 1 data row
    }

    private String determineReportType(String reportName) {
        if (reportName.contains("balance")) return "BALANCE";
        if (reportName.contains("transaction")) return "TRANSACTION";
        if (reportName.contains("reconciliation")) return "RECONCILIATION";
        if (reportName.contains("swift") || reportName.contains("settlement")) return "SWIFT";
        return "UNKNOWN";
    }

    private void createPaymentFromMessage(SwiftMessage message) {
        Payment payment = Payment.builder()
                .transactionId(message.getTransactionId())
                .swiftMessageId(message.getId())
                .accountId(message.getAccountId())
                .amount(message.getAmount())
                .currency(message.getCurrency())
                .beneficiaryName(message.getBeneficiaryName())
                .beneficiaryAccount(message.getBeneficiaryAccount())
                .beneficiaryBIC(message.getReceiverBIC())
                .orderingCustomer(message.getOrderingCustomer())
                .valueDate(message.getValueDate())
                .reference(message.getReference())
                .status("SENT")
                .build();

        dataService.savePayment(payment);
    }

    private String generateMessageContent(SwiftMessage message) {
        return String.format("""
                {1:F01%s0000000000}
                {2:I%s%sN}
                {4:
                :20:%s
                :23B:CRED
                :32A:%s%s%s
                :50K:%s
                :59:%s
                /%s
                :71A:SHA
                :72:/REM/%s
                -}
                """,
                swiftProperties.getOurBic(),
                message.getMessageType(),
                message.getReceiverBIC(),
                message.getReference(),
                message.getValueDate().toString().replace("-", ""),
                message.getCurrency(),
                message.getAmount().toString(),
                message.getOrderingCustomer() != null ? message.getOrderingCustomer() : "ORDERING CUSTOMER",
                message.getBeneficiaryName() != null ? message.getBeneficiaryName() : "BENEFICIARY",
                message.getBeneficiaryAccount() != null ? message.getBeneficiaryAccount() : "ACCOUNT",
                message.getRemittanceInfo() != null ? message.getRemittanceInfo() : "PAYMENT"
        );
    }
}
