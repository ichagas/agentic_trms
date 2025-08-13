package com.trms.mock.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/health")
@Slf4j
@Tag(name = "Health Check", description = "Application health and status endpoints")
@CrossOrigin(origins = "*")
public class HealthController {

    @GetMapping
    @Operation(summary = "Health check", description = "Check if the TRMS Mock API is running and healthy")
    @ApiResponse(responseCode = "200", description = "Service is healthy")
    public ResponseEntity<Map<String, Object>> health() {
        log.info("Health check requested");
        
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "TRMS Mock API");
        health.put("version", "1.0.0");
        health.put("timestamp", LocalDateTime.now());
        health.put("environment", "development");
        
        Map<String, Object> components = new HashMap<>();
        components.put("mock-data-service", "UP");
        components.put("eod-service", "UP");
        components.put("api-endpoints", "UP");
        
        health.put("components", components);
        
        return ResponseEntity.ok(health);
    }
}