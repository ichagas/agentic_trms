package com.trms.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * End of Day Status DTO for TRMS legacy system integration
 */
public record EODStatus(
    @JsonProperty("ready") Boolean ready,
    @JsonProperty("overallStatus") String overallStatus,
    @JsonProperty("summary") String summary,
    @JsonProperty("requiredActions") List<String> requiredActions,
    @JsonProperty("blockers") List<BlockerIssue> blockers,
    @JsonProperty("warnings") List<WarningIssue> warnings,
    @JsonProperty("readinessPercentage") Double readinessPercentage,
    @JsonProperty("checkTime") String checkTime,
    @JsonProperty("marketDataStatus") Object marketDataStatus,
    @JsonProperty("transactionStatus") Object transactionStatus,
    @JsonProperty("missingResets") List<Object> missingResets,
    @JsonProperty("swiftReconciliation") SwiftReconciliation swiftReconciliation,
    @JsonProperty("unreconciledSwiftMessages") Integer unreconciledSwiftMessages
) {
    // Helper method for backward compatibility
    public boolean isReady() {
        return ready != null && ready;
    }

    public String status() {
        return overallStatus;
    }

    public record BlockerIssue(
        @JsonProperty("type") String type,
        @JsonProperty("description") String description,
        @JsonProperty("resolution") String resolution,
        @JsonProperty("severity") String severity
    ) {}

    public record WarningIssue(
        @JsonProperty("type") String type,
        @JsonProperty("description") String description,
        @JsonProperty("recommendation") String recommendation,
        @JsonProperty("severity") String severity
    ) {}

    public record SwiftReconciliation(
        @JsonProperty("totalMessages") Integer totalMessages,
        @JsonProperty("reconciledCount") Integer reconciledCount,
        @JsonProperty("unreconciledCount") Integer unreconciledCount,
        @JsonProperty("isComplete") Boolean isComplete,
        @JsonProperty("summary") String summary,
        @JsonProperty("swiftServiceAvailable") Boolean swiftServiceAvailable
    ) {}
}