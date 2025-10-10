package com.swift.mock.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Result DTO for EOD report verification
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EODReportVerificationResult {

    private String reportDate;
    private boolean isComplete;
    private int totalChecks;
    private int passedChecks;
    private int failedChecks;

    @Builder.Default
    private List<ReportCheck> checks = new ArrayList<>();

    @Builder.Default
    private List<String> missingReports = new ArrayList<>();

    @Builder.Default
    private List<String> issues = new ArrayList<>();

    private String summary;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReportCheck {
        private String reportName;
        private String reportType; // BALANCE, TRANSACTION, RECONCILIATION, SWIFT
        private boolean exists;
        private boolean isValid;
        private String status; // PASSED, FAILED, WARNING
        private String details;
    }
}
