package com.swift.mock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * SWIFT Mock Application
 *
 * Mock SWIFT messaging system that simulates payment message processing,
 * settlements, confirmations, and reconciliation for the TRMS AI Agent POC.
 */
@SpringBootApplication
public class SwiftMockApplication {

    public static void main(String[] args) {
        SpringApplication.run(SwiftMockApplication.class, args);
    }
}
