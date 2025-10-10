package com.swift.mock.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for sending SWIFT messages
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendMessageRequest {

    @NotBlank(message = "Message type is required")
    private String messageType; // MT103, MT202, etc.

    @NotBlank(message = "Account ID is required")
    private String accountId;

    private String transactionId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    private String currency;

    @NotBlank(message = "Receiver BIC is required")
    private String receiverBIC;

    private String beneficiaryName;
    private String beneficiaryAccount;
    private String orderingCustomer;
    private String remittanceInfo;
    private String reference;
}
