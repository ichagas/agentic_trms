package com.trms.mock.config;

import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Component
public class ActuatorConfig implements InfoContributor {

    @Override
    public void contribute(Info.Builder builder) {
        builder.withDetail("app", Map.of(
                "name", "TRMS Mock Application",
                "description", "Mock Treasury and Risk Management System for AI Agent POC",
                "version", "1.0.0",
                "built", LocalDateTime.now(),
                "environment", "development"
        ));
        
        builder.withDetail("features", Map.of(
                "account-management", "enabled",
                "transaction-processing", "enabled",
                "eod-processing", "enabled",
                "market-data-simulation", "enabled",
                "rate-fixing-ai", "enabled"
        ));
        
        builder.withDetail("api", Map.of(
                "version", "v1",
                "swagger-ui", "http://localhost:8090/swagger-ui.html",
                "api-docs", "http://localhost:8090/api-docs"
        ));
    }
}