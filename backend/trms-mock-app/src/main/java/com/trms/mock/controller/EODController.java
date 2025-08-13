package com.trms.mock.controller;

import com.trms.mock.dto.EODRunRequest;
import com.trms.mock.dto.ProposeFixingsRequest;
import com.trms.mock.model.*;
import com.trms.mock.service.EODService;
import com.trms.mock.service.MockDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/eod")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "End-of-Day Processing", description = "Operations related to end-of-day processing, market data, and rate fixings")
@CrossOrigin(origins = "*")
public class EODController {
    
    private final EODService eodService;
    private final MockDataService mockDataService;
    
    @GetMapping("/readiness")
    @Operation(summary = "Check EOD readiness", description = "Perform comprehensive readiness check for end-of-day processing")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved readiness status")
    })
    public ResponseEntity<EODCheckResult> checkEODReadiness() {
        log.info("Performing EOD readiness check");
        
        EODCheckResult result = eodService.checkEODReadiness();
        
        log.info("EOD readiness check completed - Status: {}, Ready: {}", 
                result.getOverallStatus(), result.getReady());
        
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/market-data-status")
    @Operation(summary = "Get market data status", description = "Retrieve current status of all market data feeds")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved market data status")
    })
    public ResponseEntity<List<MarketDataStatus>> getMarketDataStatus() {
        log.info("Fetching market data status");
        
        List<MarketDataStatus> status = mockDataService.getAllMarketDataStatus();
        
        log.info("Retrieved status for {} market data feeds", status.size());
        
        return ResponseEntity.ok(status);
    }
    
    @GetMapping("/transaction-status")
    @Operation(summary = "Get transaction status summary", description = "Retrieve summary of transaction processing status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved transaction status")
    })
    public ResponseEntity<TransactionStatusSummary> getTransactionStatus() {
        log.info("Fetching transaction status summary");
        
        TransactionStatusSummary summary = mockDataService.getTransactionStatusSummary();
        
        log.info("Transaction status: {} total, {:.1f}% completion rate", 
                summary.getTotal(), summary.getCompletionPercentage());
        
        return ResponseEntity.ok(summary);
    }
    
    @GetMapping("/missing-resets")
    @Operation(summary = "Get missing rate resets", description = "Retrieve list of instruments with missing rate fixings")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved missing rate resets")
    })
    public ResponseEntity<List<RateReset>> getMissingRateResets() {
        log.info("Fetching missing rate resets");
        
        List<RateReset> missingResets = mockDataService.getMissingRateResets();
        
        log.info("Found {} missing rate resets", missingResets.size());
        
        return ResponseEntity.ok(missingResets);
    }
    
    @PostMapping("/propose-fixings")
    @Operation(summary = "Propose rate fixings", description = "Generate AI-powered rate fixing proposals for missing resets")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully generated rate proposals"),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters")
    })
    public ResponseEntity<List<RateReset>> proposeRateFixings(
            @Valid @RequestBody ProposeFixingsRequest request) {
        
        log.info("Proposing rate fixings for {} instruments", request.getInstrumentIds().size());
        
        List<RateReset> proposals = eodService.proposeRateFixings(request);
        
        log.info("Generated {} rate fixing proposals", proposals.size());
        
        return ResponseEntity.ok(proposals);
    }
    
    @PostMapping("/run")
    @Operation(summary = "Run EOD processing", description = "Initiate end-of-day processing with optional parameters")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "EOD processing completed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid EOD run parameters"),
        @ApiResponse(responseCode = "422", description = "EOD readiness checks failed")
    })
    public ResponseEntity<String> runEOD(@Valid @RequestBody EODRunRequest request) {
        log.info("Initiating EOD run for business date: {}", request.getBusinessDate());
        
        String result = eodService.runEOD(request);
        
        log.info("EOD run completed");
        
        return ResponseEntity.ok(result);
    }
}