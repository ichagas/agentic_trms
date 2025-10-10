package com.trms.ai.dto;

/**
 * SWIFT Message Status Response DTO
 */
public record MessageStatusResponse(
        String messageId,
        String status,
        String statusDescription,
        String details,
        boolean isReconciled,
        String reconciliationDetails
) {}
