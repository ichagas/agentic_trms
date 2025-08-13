package com.trms.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for TRMS legacy system integration
 */
@ConfigurationProperties(prefix = "trms.legacy")
public record TrmsProperties(
    String baseUrl,
    long timeout,
    int retryAttempts,
    long retryDelay
) {}