package com.trms.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class for TRMS AI Backend
 *
 * This Spring Boot application provides AI-powered capabilities for Treasury and Risk Management Systems.
 * It integrates with legacy TRMS systems through REST APIs and provides intelligent assistance
 * for financial operations, account management, and end-of-day processing.
 */
@SpringBootApplication(exclude = {})
@ConfigurationPropertiesScan
@EnableScheduling
public class TrmsAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(TrmsAiApplication.class, args);
    }
}