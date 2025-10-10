package com.swift.mock.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * SWIFT Message model representing MT messages (MT103, MT202, etc.)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SwiftMessage {

    private String id;
    private String messageType;
    private String senderBIC;
    private String receiverBIC;
    private BigDecimal amount;
    private String currency;
    private String accountId;
    private String transactionId;
    private MessageStatus status;
    private String reference;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate valueDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sentTimestamp;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime confirmedTimestamp;

    private String rawMessageContent;
    private String beneficiaryName;
    private String beneficiaryAccount;
    private String orderingCustomer;
    private String remittanceInfo;
}
