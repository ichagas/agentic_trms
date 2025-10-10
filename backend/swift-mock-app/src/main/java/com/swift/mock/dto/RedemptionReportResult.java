package com.swift.mock.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Result DTO for redemption report processing
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RedemptionReportResult {

    private String reportFileName;
    private int totalRedemptions;
    private int processedCount;
    private int failedCount;
    private BigDecimal totalAmount;
    private String currency;

    @Builder.Default
    private List<RedemptionItem> redemptions = new ArrayList<>();

    @Builder.Default
    private List<String> errors = new ArrayList<>();

    private String summary;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RedemptionItem {
        private String accountId;
        private String beneficiaryName;
        private String beneficiaryAccount;
        private BigDecimal amount;
        private String currency;
        private String reference;
        private String status; // PROCESSED, FAILED, PENDING
        private String errorMessage;
    }
}
