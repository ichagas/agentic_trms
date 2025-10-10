package com.trms.ai.client;

import com.trms.ai.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Client for communicating with SWIFT mock system
 * Handles all REST API calls to the SWIFT mock backend
 */
@Component
public class SwiftClient {

    private static final Logger logger = LoggerFactory.getLogger(SwiftClient.class);

    private final RestTemplate restTemplate;
    private final String swiftBaseUrl;

    public SwiftClient(RestTemplate restTemplate,
                      @Value("${swift-mock.base-url:http://localhost:8091}") String swiftBaseUrl) {
        this.restTemplate = restTemplate;
        this.swiftBaseUrl = swiftBaseUrl;
    }

    /**
     * Send a SWIFT payment message
     */
    public SwiftMessage sendSwiftPayment(String accountId, String transactionId, BigDecimal amount,
                                        String currency, String receiverBIC, String beneficiaryName,
                                        String beneficiaryAccount) {
        try {
            String url = swiftBaseUrl + "/api/v1/swift/messages";

            Map<String, Object> request = new HashMap<>();
            request.put("messageType", "MT103");
            request.put("accountId", accountId);
            request.put("transactionId", transactionId);
            request.put("amount", amount);
            request.put("currency", currency);
            request.put("receiverBIC", receiverBIC);
            request.put("beneficiaryName", beneficiaryName);
            request.put("beneficiaryAccount", beneficiaryAccount);
            request.put("orderingCustomer", "TRMS System");
            request.put("remittanceInfo", "Payment for transaction " + transactionId);

            logger.debug("Sending SWIFT message: {}", url);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, createHeaders());

            ResponseEntity<SwiftMessage> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    SwiftMessage.class
            );

            SwiftMessage message = response.getBody();
            logger.info("SWIFT message sent successfully: {}", message != null ? message.id() : "unknown");
            return message;

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            logger.error("HTTP error while sending SWIFT message: {} - {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Failed to send SWIFT message: " + e.getMessage(), e);
        } catch (ResourceAccessException e) {
            logger.error("Connection error while sending SWIFT message: {}", e.getMessage());
            throw new RuntimeException("Unable to connect to SWIFT system", e);
        } catch (Exception e) {
            logger.error("Unexpected error while sending SWIFT message: {}", e.getMessage());
            throw new RuntimeException("Unexpected error occurred", e);
        }
    }

    /**
     * Get SWIFT message status
     */
    public MessageStatusResponse getMessageStatus(String messageId) {
        try {
            String url = swiftBaseUrl + "/api/v1/swift/messages/" + messageId + "/status";

            logger.debug("Getting SWIFT message status: {}", url);

            ResponseEntity<MessageStatusResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    createHttpEntity(),
                    MessageStatusResponse.class
            );

            MessageStatusResponse status = response.getBody();
            logger.info("Retrieved SWIFT message status for: {}", messageId);
            return status;

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                logger.warn("SWIFT message not found: {}", messageId);
                throw new RuntimeException("SWIFT message not found: " + messageId);
            }
            logger.error("HTTP error while getting message status: {} - {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Failed to get message status: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Error getting message status: {}", e.getMessage());
            throw new RuntimeException("Error occurred: " + e.getMessage(), e);
        }
    }

    /**
     * Get SWIFT messages by account
     */
    public List<SwiftMessage> getMessagesByAccount(String accountId) {
        try {
            String url = swiftBaseUrl + "/api/v1/swift/messages/account/" + accountId;

            logger.debug("Getting SWIFT messages for account: {}", url);

            ResponseEntity<List<SwiftMessage>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    createHttpEntity(),
                    new ParameterizedTypeReference<List<SwiftMessage>>() {}
            );

            List<SwiftMessage> messages = response.getBody();
            logger.info("Retrieved {} SWIFT messages for account: {}",
                    messages != null ? messages.size() : 0, accountId);
            return messages != null ? messages : Collections.emptyList();

        } catch (Exception e) {
            logger.error("Error getting messages by account: {}", e.getMessage());
            throw new RuntimeException("Error occurred: " + e.getMessage(), e);
        }
    }

    /**
     * Get SWIFT messages by transaction
     */
    public List<SwiftMessage> getMessagesByTransaction(String transactionId) {
        try {
            String url = swiftBaseUrl + "/api/v1/swift/messages/transaction/" + transactionId;

            logger.debug("Getting SWIFT messages for transaction: {}", url);

            ResponseEntity<List<SwiftMessage>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    createHttpEntity(),
                    new ParameterizedTypeReference<List<SwiftMessage>>() {}
            );

            List<SwiftMessage> messages = response.getBody();
            logger.info("Retrieved {} SWIFT messages for transaction: {}",
                    messages != null ? messages.size() : 0, transactionId);
            return messages != null ? messages : Collections.emptyList();

        } catch (Exception e) {
            logger.error("Error getting messages by transaction: {}", e.getMessage());
            throw new RuntimeException("Error occurred: " + e.getMessage(), e);
        }
    }

    /**
     * Reconcile SWIFT messages
     */
    public ReconciliationResult reconcileMessages(String accountId, boolean autoReconcile) {
        try {
            String url = swiftBaseUrl + "/api/v1/swift/messages/reconcile";

            Map<String, Object> request = new HashMap<>();
            request.put("accountId", accountId);
            request.put("autoReconcile", autoReconcile);

            logger.debug("Reconciling SWIFT messages: {}", url);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, createHeaders());

            ResponseEntity<ReconciliationResult> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    ReconciliationResult.class
            );

            ReconciliationResult result = response.getBody();
            logger.info("Reconciliation completed: {}", result != null ? result.summary() : "unknown");
            return result;

        } catch (Exception e) {
            logger.error("Error reconciling messages: {}", e.getMessage());
            throw new RuntimeException("Error occurred: " + e.getMessage(), e);
        }
    }

    /**
     * Get unreconciled SWIFT messages
     */
    public List<SwiftMessage> getUnreconciledMessages() {
        try {
            String url = swiftBaseUrl + "/api/v1/swift/messages/unreconciled";

            logger.debug("Getting unreconciled SWIFT messages: {}", url);

            ResponseEntity<List<SwiftMessage>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    createHttpEntity(),
                    new ParameterizedTypeReference<List<SwiftMessage>>() {}
            );

            List<SwiftMessage> messages = response.getBody();
            logger.info("Retrieved {} unreconciled SWIFT messages",
                    messages != null ? messages.size() : 0);
            return messages != null ? messages : Collections.emptyList();

        } catch (Exception e) {
            logger.error("Error getting unreconciled messages: {}", e.getMessage());
            throw new RuntimeException("Error occurred: " + e.getMessage(), e);
        }
    }

    /**
     * Process redemption report
     */
    public RedemptionReportResult processRedemptionReport(String fileName) {
        try {
            String url = swiftBaseUrl + "/api/v1/swift/reports/redemptions/process?fileName=" + fileName;

            logger.debug("Processing redemption report: {}", url);

            ResponseEntity<RedemptionReportResult> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    createHttpEntity(),
                    RedemptionReportResult.class
            );

            RedemptionReportResult result = response.getBody();
            logger.info("Redemption report processed: {}", result != null ? result.summary() : "unknown");
            return result;

        } catch (Exception e) {
            logger.error("Error processing redemption report: {}", e.getMessage());
            throw new RuntimeException("Error occurred: " + e.getMessage(), e);
        }
    }

    /**
     * Verify EOD reports
     */
    public EODReportVerificationResult verifyEODReports(String reportDate) {
        try {
            String url = swiftBaseUrl + "/api/v1/swift/reports/eod/verify?reportDate=" + reportDate;

            logger.debug("Verifying EOD reports: {}", url);

            ResponseEntity<EODReportVerificationResult> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    createHttpEntity(),
                    EODReportVerificationResult.class
            );

            EODReportVerificationResult result = response.getBody();
            logger.info("EOD report verification completed: {}", result != null ? result.summary() : "unknown");
            return result;

        } catch (Exception e) {
            logger.error("Error verifying EOD reports: {}", e.getMessage());
            throw new RuntimeException("Error occurred: " + e.getMessage(), e);
        }
    }

    /**
     * Create HTTP entity with headers
     */
    private HttpEntity<Void> createHttpEntity() {
        return new HttpEntity<>(createHeaders());
    }

    /**
     * Create HTTP headers for requests
     */
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
    }
}
