package com.swift.mock.controller;

import com.swift.mock.dto.EODReportVerificationResult;
import com.swift.mock.dto.RedemptionReportResult;
import com.swift.mock.service.SwiftMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for report processing operations
 */
@RestController
@RequestMapping("/api/v1/swift/reports")
@Tag(name = "Reports", description = "Report processing and verification")
@CrossOrigin(origins = "*")
public class ReportController {

    private static final Logger logger = LoggerFactory.getLogger(ReportController.class);

    private final SwiftMessageService swiftMessageService;

    public ReportController(SwiftMessageService swiftMessageService) {
        this.swiftMessageService = swiftMessageService;
    }

    @PostMapping("/redemptions/process")
    @Operation(summary = "Process a redemption report file")
    public ResponseEntity<RedemptionReportResult> processRedemptionReport(
            @RequestParam String fileName) {
        logger.info("POST /api/v1/swift/reports/redemptions/process - fileName: {}", fileName);
        try {
            RedemptionReportResult result = swiftMessageService.processRedemptionReport(fileName);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error processing redemption report: {}", e.getMessage());
            throw new RuntimeException("Failed to process redemption report: " + e.getMessage());
        }
    }

    @GetMapping("/eod/verify")
    @Operation(summary = "Verify EOD reports for a specific date")
    public ResponseEntity<EODReportVerificationResult> verifyEODReports(
            @RequestParam String reportDate) {
        logger.info("GET /api/v1/swift/reports/eod/verify - reportDate: {}", reportDate);
        try {
            EODReportVerificationResult result = swiftMessageService.verifyEODReports(reportDate);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error verifying EOD reports: {}", e.getMessage());
            throw new RuntimeException("Failed to verify EOD reports: " + e.getMessage());
        }
    }
}
