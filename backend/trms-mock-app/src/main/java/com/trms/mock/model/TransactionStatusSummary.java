package com.trms.mock.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionStatusSummary {
    
    @NotNull(message = "Total count is required")
    private Integer total;
    
    @NotNull(message = "Status counts are required")
    private Map<Transaction.TransactionStatus, Integer> statusCounts;
    
    private List<Transaction> pendingTransactions;
    private List<Transaction> failedTransactions;
    private LocalDateTime lastUpdated;
    private Integer criticalCount;
    private Integer warningCount;
    private Double completionPercentage;
}