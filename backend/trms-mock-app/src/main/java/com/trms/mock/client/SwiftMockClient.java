package com.trms.mock.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * Client for communicating with SWIFT mock service
 * Used for EOD reconciliation checks
 */
@Component
public class SwiftMockClient {

    private static final Logger logger = LoggerFactory.getLogger(SwiftMockClient.class);

    @Value("${swift-mock.base-url:http://localhost:8091/api/v1/swift}")
    private String swiftBaseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Get count of unreconciled SWIFT messages
     */
    public int getUnreconciledMessageCount() {
        try {
            String url = swiftBaseUrl + "/messages/unreconciled";
            logger.debug("Fetching unreconciled messages from: {}", url);

            SwiftMessage[] messages = restTemplate.getForObject(url, SwiftMessage[].class);
            int count = messages != null ? messages.length : 0;

            logger.info("Retrieved {} unreconciled SWIFT messages", count);
            return count;
        } catch (Exception e) {
            logger.error("Failed to fetch unreconciled messages from SWIFT service: {}", e.getMessage());
            return -1; // Indicate SWIFT service unavailable
        }
    }

    /**
     * Get reconciliation status from SWIFT service
     */
    public ReconciliationResult getReconciliationStatus() {
        try {
            String url = swiftBaseUrl + "/messages/reconcile";
            logger.debug("Fetching reconciliation status from: {}", url);

            // Use POST to trigger reconciliation check without auto-reconcile
            ReconciliationRequest request = new ReconciliationRequest(null, false);
            ReconciliationResult result = restTemplate.postForObject(url, request, ReconciliationResult.class);

            logger.info("Reconciliation status: {} reconciled, {} unreconciled",
                    result != null ? result.getReconciledCount() : 0,
                    result != null ? result.getUnreconciledCount() : 0);

            return result;
        } catch (Exception e) {
            logger.error("Failed to fetch reconciliation status from SWIFT service: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Check if SWIFT service is available
     */
    public boolean isSwiftServiceAvailable() {
        try {
            String url = swiftBaseUrl + "/messages/unreconciled";
            restTemplate.headForHeaders(url);
            return true;
        } catch (Exception e) {
            logger.warn("SWIFT service unavailable: {}", e.getMessage());
            return false;
        }
    }

    // DTOs for SWIFT service communication

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SwiftMessage {
        private String id;
        private String status;
        private String accountId;
        private String transactionId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReconciliationResult {
        private Integer totalMessages;
        private Integer reconciledCount;
        private Integer unreconciledCount;
        private Integer pendingCount;
        private String summary;
        private List<String> issues;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class ReconciliationRequest {
        private String accountId;
        private boolean autoReconcile;
    }
}
