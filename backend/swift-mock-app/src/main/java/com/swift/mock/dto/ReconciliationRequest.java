package com.swift.mock.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for reconciliation operations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReconciliationRequest {

    private String accountId;
    private String startDate;
    private String endDate;
    private boolean autoReconcile;
}
