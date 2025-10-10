package com.trms.ai.dto;

import java.util.List;

/**
 * SWIFT Reconciliation Result DTO
 */
public record ReconciliationResult(
        int totalMessages,
        int reconciledCount,
        int unreconciledCount,
        int pendingCount,
        List<SwiftMessage> reconciledMessages,
        List<SwiftMessage> unreconciledMessages,
        List<String> issues,
        String summary
) {}
