package com.swift.mock.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO representing a TRMS transaction for reconciliation purposes
 * Used when fetching transaction data from TRMS mock service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrmsTransaction {

    private String transactionId;
    private String fromAccount;
    private String toAccount;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String type;
    private String description;
    private String reference;
    private LocalDateTime createdAt;
    private LocalDateTime valueDate;
    private LocalDateTime settledAt;
    private String settlementMethod;
}
