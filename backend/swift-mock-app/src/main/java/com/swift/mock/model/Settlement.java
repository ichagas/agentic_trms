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
 * Settlement model representing payment settlements
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Settlement {

    private String id;
    private String accountId;
    private String swiftMessageId;
    private BigDecimal amount;
    private String currency;
    private String settlementType; // INCOMING, OUTGOING

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate settlementDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    private String status; // PENDING, SETTLED, FAILED
    private String counterpartyBIC;
    private String counterpartyAccount;
    private String reference;
}
