package com.trms.mock.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EODCheckResult {
    
    @NotNull(message = "Ready status is required")
    private Boolean ready;
    
    @NotNull(message = "Market data status is required")
    private MarketDataStatus marketDataStatus;
    
    @NotNull(message = "Transaction status is required")
    private TransactionStatusSummary transactionStatus;
    
    private List<RateReset> missingResets;
    private List<String> requiredActions;
    private LocalDateTime checkTime;
    private EODStatus overallStatus;
    private String summary;
    private List<BlockerIssue> blockers;
    private List<WarningIssue> warnings;
    private Double readinessPercentage;

    // SWIFT reconciliation status
    private SwiftReconciliationStatus swiftReconciliation;
    private Integer unreconciledSwiftMessages;
    private List<String> swiftIssues;
    
    public enum EODStatus {
        READY,
        NOT_READY,
        PARTIAL_READY,
        BLOCKED,
        UNKNOWN
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BlockerIssue {
        private String type;
        private String description;
        private String resolution;
        private IssueSeverity severity;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WarningIssue {
        private String type;
        private String description;
        private String recommendation;
        private IssueSeverity severity;
    }
    
    public enum IssueSeverity {
        CRITICAL,
        HIGH,
        MEDIUM,
        LOW,
        INFO
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SwiftReconciliationStatus {
        private Integer totalMessages;
        private Integer reconciledCount;
        private Integer unreconciledCount;
        private Boolean isComplete;
        private String summary;
        private Boolean swiftServiceAvailable;
    }
}