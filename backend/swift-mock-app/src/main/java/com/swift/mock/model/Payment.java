package com.swift.mock.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Payment model representing payment instructions
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    private String id;
    private String transactionId;
    private String swiftMessageId;
    private String accountId;
    private BigDecimal amount;
    private String currency;
    private String beneficiaryName;
    private String beneficiaryAccount;
    private String beneficiaryBIC;
    private String orderingCustomer;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate valueDate;

    private String paymentPurpose;
    private String reference;
    private String status; // PENDING, SENT, CONFIRMED, REJECTED
}
