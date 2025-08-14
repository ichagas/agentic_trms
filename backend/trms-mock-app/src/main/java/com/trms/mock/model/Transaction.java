package com.trms.mock.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    
    @NotBlank(message = "Transaction ID is required")
    private String transactionId;
    
    @NotBlank(message = "From account is required")
    private String fromAccount;
    
    @NotBlank(message = "To account is required")
    private String toAccount;
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
    
    @NotBlank(message = "Currency is required")
    private String currency;
    
    @NotNull(message = "Status is required")
    private TransactionStatus status;
    
    @NotNull(message = "Created timestamp is required")
    private LocalDateTime createdAt;
    
    private String description;
    private String reference;
    private TransactionType type;
    private LocalDateTime valueDate;
    private LocalDateTime settledAt;
    private String reasonCode;
    private String settlementMethod;
    
    public enum TransactionStatus {
        NEW,
        PENDING,
        VALIDATED,
        SETTLED,
        FAILED,
        CANCELLED,
        REJECTED,
        PROPOSAL
    }
    
    public enum TransactionType {
        TRANSFER,
        PAYMENT,
        FX_SETTLEMENT,
        COLLATERAL_MOVEMENT,
        INTEREST_PAYMENT,
        FEE_PAYMENT
    }
}