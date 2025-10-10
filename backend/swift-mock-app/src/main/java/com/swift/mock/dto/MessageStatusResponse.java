package com.swift.mock.dto;

import com.swift.mock.model.MessageStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for message status queries
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageStatusResponse {

    private String messageId;
    private MessageStatus status;
    private String statusDescription;
    private String details;
    private boolean isReconciled;
    private String reconciliationDetails;
}
