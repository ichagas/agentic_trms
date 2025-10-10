package com.trms.ai.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Redemption Report Processing Result DTO
 */
public record RedemptionReportResult(
        String reportFileName,
        int totalRedemptions,
        int processedCount,
        int failedCount,
        BigDecimal totalAmount,
        String currency,
        List<RedemptionItem> redemptions,
        List<String> errors,
        String summary
) {
    public record RedemptionItem(
            String accountId,
            String beneficiaryName,
            String beneficiaryAccount,
            BigDecimal amount,
            String currency,
            String reference,
            String status,
            String errorMessage
    ) {}
}
