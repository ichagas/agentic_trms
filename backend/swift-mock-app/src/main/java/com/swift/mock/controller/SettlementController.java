package com.swift.mock.controller;

import com.swift.mock.model.Settlement;
import com.swift.mock.service.SwiftMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for settlement operations
 */
@RestController
@RequestMapping("/api/v1/swift/settlements")
@Tag(name = "Settlements", description = "Settlement operations")
@CrossOrigin(origins = "*")
public class SettlementController {

    private static final Logger logger = LoggerFactory.getLogger(SettlementController.class);

    private final SwiftMessageService swiftMessageService;

    public SettlementController(SwiftMessageService swiftMessageService) {
        this.swiftMessageService = swiftMessageService;
    }

    @GetMapping("/account/{accountId}")
    @Operation(summary = "Get settlements by account ID")
    public ResponseEntity<List<Settlement>> getSettlementsByAccount(@PathVariable String accountId) {
        logger.info("GET /api/v1/swift/settlements/account/{}", accountId);
        List<Settlement> settlements = swiftMessageService.getSettlementsByAccount(accountId);
        return ResponseEntity.ok(settlements);
    }
}
