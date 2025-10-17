package com.swift.mock.controller;

import com.swift.mock.dto.*;
import com.swift.mock.model.MessageStatus;
import com.swift.mock.model.SwiftMessage;
import com.swift.mock.service.SwiftMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for SWIFT message operations
 */
@RestController
@RequestMapping("/api/v1/swift/messages")
@Tag(name = "SWIFT Messages", description = "SWIFT messaging operations (MT103, MT202, etc.)")
@CrossOrigin(origins = "*")
public class SwiftMessageController {

    private static final Logger logger = LoggerFactory.getLogger(SwiftMessageController.class);

    private final SwiftMessageService swiftMessageService;

    public SwiftMessageController(SwiftMessageService swiftMessageService) {
        this.swiftMessageService = swiftMessageService;
    }

    @PostMapping
    @Operation(summary = "Send a SWIFT message")
    public ResponseEntity<SwiftMessage> sendMessage(@Valid @RequestBody SendMessageRequest request) {
        logger.info("POST /api/v1/swift/messages - Sending {} message", request.getMessageType());
        try {
            SwiftMessage message = swiftMessageService.sendMessage(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(message);
        } catch (Exception e) {
            logger.error("Error sending SWIFT message: {}", e.getMessage());
            throw new RuntimeException("Failed to send SWIFT message: " + e.getMessage());
        }
    }

    @GetMapping("/{messageId}")
    @Operation(summary = "Get SWIFT message details by ID")
    public ResponseEntity<SwiftMessage> getMessage(@PathVariable String messageId) {
        logger.info("GET /api/v1/swift/messages/{} - Retrieving message", messageId);
        // Note: This would call dataService.getMessage() in a full implementation
        throw new UnsupportedOperationException("Get message by ID not fully implemented in this mock");
    }

    @GetMapping("/{messageId}/status")
    @Operation(summary = "Check SWIFT message status")
    public ResponseEntity<MessageStatusResponse> getMessageStatus(@PathVariable String messageId) {
        logger.info("GET /api/v1/swift/messages/{}/status", messageId);
        try {
            MessageStatusResponse status = swiftMessageService.getMessageStatus(messageId);
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            logger.error("Error getting message status: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping
    @Operation(summary = "Get all SWIFT messages")
    public ResponseEntity<List<SwiftMessage>> getAllMessages() {
        logger.info("GET /api/v1/swift/messages - Retrieving all messages");
        List<SwiftMessage> messages = swiftMessageService.getAllMessages();
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/account/{accountId}")
    @Operation(summary = "Get all SWIFT messages for an account")
    public ResponseEntity<List<SwiftMessage>> getMessagesByAccount(@PathVariable String accountId) {
        logger.info("GET /api/v1/swift/messages/account/{}", accountId);
        List<SwiftMessage> messages = swiftMessageService.getMessagesByAccount(accountId);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/transaction/{transactionId}")
    @Operation(summary = "Get SWIFT messages by transaction ID")
    public ResponseEntity<List<SwiftMessage>> getMessagesByTransaction(@PathVariable String transactionId) {
        logger.info("GET /api/v1/swift/messages/transaction/{}", transactionId);
        List<SwiftMessage> messages = swiftMessageService.getMessagesByTransaction(transactionId);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/unreconciled")
    @Operation(summary = "Get all unreconciled SWIFT messages")
    public ResponseEntity<List<SwiftMessage>> getUnreconciledMessages() {
        logger.info("GET /api/v1/swift/messages/unreconciled");
        List<SwiftMessage> messages = swiftMessageService.getUnreconciledMessages();
        return ResponseEntity.ok(messages);
    }

    @PostMapping("/reconcile")
    @Operation(summary = "Reconcile SWIFT messages with transactions")
    public ResponseEntity<ReconciliationResult> reconcileMessages(@RequestBody ReconciliationRequest request) {
        logger.info("POST /api/v1/swift/messages/reconcile");
        try {
            ReconciliationResult result = swiftMessageService.reconcileMessages(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error reconciling messages: {}", e.getMessage());
            throw new RuntimeException("Failed to reconcile messages: " + e.getMessage());
        }
    }
}
