package com.trms.ai.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * SWIFT Message DTO for AI backend
 */
public record SwiftMessage(
        String id,
        String messageType,
        String senderBIC,
        String receiverBIC,
        BigDecimal amount,
        String currency,
        String accountId,
        String transactionId,
        String status,
        String reference,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate valueDate,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime sentTimestamp,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime confirmedTimestamp,
        String beneficiaryName,
        String beneficiaryAccount
) {}
