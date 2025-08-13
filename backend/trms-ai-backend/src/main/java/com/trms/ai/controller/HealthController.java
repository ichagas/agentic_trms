package com.trms.ai.controller;

import com.trms.ai.client.LegacyTrmsClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Health check controller for TRMS AI Backend
 */
@RestController
@RequestMapping("/api/health")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"})
public class HealthController {

    private static final Logger logger = LoggerFactory.getLogger(HealthController.class);

    private final LegacyTrmsClient legacyTrmsClient;

    public HealthController(LegacyTrmsClient legacyTrmsClient) {
        this.legacyTrmsClient = legacyTrmsClient;
    }

    /**
     * Basic health check
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        try {
            // Test connection to TRMS backend
            legacyTrmsClient.checkEODReadiness();
            
            return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "trms-ai-backend",
                "timestamp", ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT),
                "trms_connection", "UP"
            ));
        } catch (Exception e) {
            logger.warn("Health check failed - TRMS connection issue: {}", e.getMessage());
            
            return ResponseEntity.ok(Map.of(
                "status", "DEGRADED",
                "service", "trms-ai-backend", 
                "timestamp", ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT),
                "trms_connection", "DOWN",
                "error", e.getMessage()
            ));
        }
    }
}