package com.trms.ai.dto;

import java.util.List;

/**
 * EOD Report Verification Result DTO
 */
public record EODReportVerificationResult(
        String reportDate,
        boolean isComplete,
        int totalChecks,
        int passedChecks,
        int failedChecks,
        List<ReportCheck> checks,
        List<String> missingReports,
        List<String> issues,
        String summary
) {
    public record ReportCheck(
            String reportName,
            String reportType,
            boolean exists,
            boolean isValid,
            String status,
            String details
    ) {}
}
