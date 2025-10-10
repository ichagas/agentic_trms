package com.swift.mock.dto;

import com.swift.mock.model.SwiftMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Result DTO for reconciliation operations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReconciliationResult {

    private int totalMessages;
    private int reconciledCount;
    private int unreconciledCount;
    private int pendingCount;

    @Builder.Default
    private List<SwiftMessage> reconciledMessages = new ArrayList<>();

    @Builder.Default
    private List<SwiftMessage> unreconciledMessages = new ArrayList<>();

    @Builder.Default
    private List<String> issues = new ArrayList<>();

    private String summary;
}
