package com.swift.mock.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Confirmation model representing SWIFT message confirmations (MT910)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Confirmation {

    private String id;
    private String swiftMessageId;
    private String confirmationType; // MT910, MT900, etc.
    private String senderBIC;
    private String receiverBIC;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime confirmedAt;

    private String status; // RECEIVED, PROCESSED
    private String reference;
    private String confirmationMessage;
}
