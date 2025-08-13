package com.trms.mock.controller;

import com.trms.mock.model.Report;
import com.trms.mock.service.MockDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Report Management", description = "Operations related to financial reports and document generation")
@CrossOrigin(origins = "*")
public class ReportController {
    
    private final MockDataService mockDataService;
    
    @GetMapping("/{reportId}")
    @Operation(summary = "Get report by ID", description = "Retrieve a specific financial report by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved report"),
        @ApiResponse(responseCode = "404", description = "Report not found")
    })
    public ResponseEntity<Report> getReportById(
            @Parameter(description = "Report ID", example = "RPT-001")
            @PathVariable String reportId) {
        
        log.info("Fetching report with ID: {}", reportId);
        
        Optional<Report> report = mockDataService.getReportById(reportId);
        if (report.isPresent()) {
            log.info("Report found: {} ({})", report.get().getTitle(), report.get().getType());
            return ResponseEntity.ok(report.get());
        } else {
            log.warn("Report not found with ID: {}", reportId);
            return ResponseEntity.notFound().build();
        }
    }
}