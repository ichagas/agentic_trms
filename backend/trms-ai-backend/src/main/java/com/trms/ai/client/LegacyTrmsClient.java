package com.trms.ai.client;

import com.trms.ai.config.TrmsProperties;
import com.trms.ai.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.util.Collections;
import java.util.List;

/**
 * Client for communicating with legacy TRMS system
 * Handles all REST API calls to the mock TRMS backend
 */
@Component
public class LegacyTrmsClient {

    private static final Logger logger = LoggerFactory.getLogger(LegacyTrmsClient.class);

    private final RestTemplate restTemplate;
    private final TrmsProperties trmsProperties;

    public LegacyTrmsClient(RestTemplate restTemplate, TrmsProperties trmsProperties) {
        this.restTemplate = restTemplate;
        this.trmsProperties = trmsProperties;
    }

    /**
     * Get accounts by currency from legacy TRMS system
     */
    public List<Account> getAccountsByCurrency(String currency) {
        try {
            String url = trmsProperties.baseUrl() + "/api/v1/accounts";
            if (currency != null && !currency.trim().isEmpty()) {
                url += "?currency=" + currency.trim().toUpperCase();
            }

            logger.debug("Fetching accounts from TRMS: {}", url);

            ResponseEntity<List<Account>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                createHttpEntity(),
                new ParameterizedTypeReference<List<Account>>() {}
            );

            List<Account> accounts = response.getBody();
            logger.info("Successfully retrieved {} accounts for currency: {}", 
                       accounts != null ? accounts.size() : 0, currency);
            return accounts != null ? accounts : Collections.emptyList();

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            logger.error("HTTP error while fetching accounts for currency {}: {} - {}", 
                        currency, e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Failed to fetch accounts: " + e.getMessage(), e);
        } catch (ResourceAccessException e) {
            logger.error("Connection error while fetching accounts for currency {}: {}", 
                        currency, e.getMessage());
            throw new RuntimeException("Unable to connect to TRMS system", e);
        } catch (Exception e) {
            logger.error("Unexpected error while fetching accounts for currency {}: {}", 
                        currency, e.getMessage());
            throw new RuntimeException("Unexpected error occurred", e);
        }
    }

    /**
     * Check account balance from legacy TRMS system
     */
    public AccountBalance checkAccountBalance(String accountId) {
        try {
            String url = trmsProperties.baseUrl() + "/api/v1/accounts/" + accountId + "/balance";
            
            logger.debug("Fetching account balance from TRMS: {}", url);

            ResponseEntity<AccountBalance> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                createHttpEntity(),
                AccountBalance.class
            );

            AccountBalance balance = response.getBody();
            logger.info("Successfully retrieved balance for account: {}", accountId);
            return balance;

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                logger.warn("Account not found: {}", accountId);
                throw new RuntimeException("Account not found: " + accountId);
            }
            logger.error("HTTP error while fetching balance for account {}: {} - {}", 
                        accountId, e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Failed to fetch account balance: " + e.getMessage(), e);
        } catch (HttpServerErrorException e) {
            logger.error("Server error while fetching balance for account {}: {} - {}", 
                        accountId, e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("TRMS system error: " + e.getMessage(), e);
        } catch (ResourceAccessException e) {
            logger.error("Connection error while fetching balance for account {}: {}", 
                        accountId, e.getMessage());
            throw new RuntimeException("Unable to connect to TRMS system", e);
        } catch (Exception e) {
            logger.error("Unexpected error while fetching balance for account {}: {}", 
                        accountId, e.getMessage());
            throw new RuntimeException("Unexpected error occurred", e);
        }
    }

    /**
     * Book a transaction in the legacy TRMS system
     */
    public Transaction bookTransaction(String fromAccount, String toAccount, Double amount, String currency) {
        try {
            String url = trmsProperties.baseUrl() + "/api/v1/transactions";
            
            CreateTransactionRequest request = new CreateTransactionRequest(
                fromAccount, toAccount, amount, currency, 
                "AI-initiated transaction from " + fromAccount + " to " + toAccount
            );

            logger.debug("Booking transaction in TRMS: {} {} from {} to {}", 
                        amount, currency, fromAccount, toAccount);

            HttpEntity<CreateTransactionRequest> entity = new HttpEntity<>(request, createHeaders());

            ResponseEntity<Transaction> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                Transaction.class
            );

            Transaction transaction = response.getBody();
            logger.info("Successfully booked transaction: {}", transaction != null ? transaction.id() : "unknown");
            return transaction;

        } catch (HttpClientErrorException e) {
            logger.error("HTTP error while booking transaction: {} - {}", 
                        e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Failed to book transaction: " + e.getMessage(), e);
        } catch (HttpServerErrorException e) {
            logger.error("Server error while booking transaction: {} - {}", 
                        e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("TRMS system error: " + e.getMessage(), e);
        } catch (ResourceAccessException e) {
            logger.error("Connection error while booking transaction: {}", e.getMessage());
            throw new RuntimeException("Unable to connect to TRMS system", e);
        } catch (Exception e) {
            logger.error("Unexpected error while booking transaction: {}", e.getMessage());
            throw new RuntimeException("Unexpected error occurred", e);
        }
    }

    /**
     * Check EOD readiness from legacy TRMS system
     */
    public EODStatus checkEODReadiness() {
        try {
            String url = trmsProperties.baseUrl() + "/api/v1/eod/readiness";
            
            logger.debug("Checking EOD readiness from TRMS: {}", url);

            ResponseEntity<EODStatus> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                createHttpEntity(),
                EODStatus.class
            );

            EODStatus status = response.getBody();
            logger.info("Successfully retrieved EOD status: ready={}", 
                       status != null ? status.isReady() : "unknown");
            return status;

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            logger.error("HTTP error while checking EOD readiness: {} - {}", 
                        e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Failed to check EOD readiness: " + e.getMessage(), e);
        } catch (ResourceAccessException e) {
            logger.error("Connection error while checking EOD readiness: {}", e.getMessage());
            throw new RuntimeException("Unable to connect to TRMS system", e);
        } catch (Exception e) {
            logger.error("Unexpected error while checking EOD readiness: {}", e.getMessage());
            throw new RuntimeException("Unexpected error occurred", e);
        }
    }

    /**
     * Get proposed rate fixings from legacy TRMS system
     */
    public List<RateReset> proposeRateFixings() {
        try {
            String url = trmsProperties.baseUrl() + "/api/v1/eod/missing-resets";
            
            logger.debug("Fetching proposed rate fixings from TRMS: {}", url);

            ResponseEntity<List<RateReset>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                createHttpEntity(),
                new ParameterizedTypeReference<List<RateReset>>() {}
            );

            List<RateReset> rateResets = response.getBody();
            logger.info("Successfully retrieved {} rate fixing proposals", 
                       rateResets != null ? rateResets.size() : 0);
            return rateResets != null ? rateResets : Collections.emptyList();

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            logger.error("HTTP error while fetching rate fixings: {} - {}", 
                        e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Failed to fetch rate fixings: " + e.getMessage(), e);
        } catch (ResourceAccessException e) {
            logger.error("Connection error while fetching rate fixings: {}", e.getMessage());
            throw new RuntimeException("Unable to connect to TRMS system", e);
        } catch (Exception e) {
            logger.error("Unexpected error while fetching rate fixings: {}", e.getMessage());
            throw new RuntimeException("Unexpected error occurred", e);
        }
    }

    /**
     * Create HTTP entity with headers
     */
    private HttpEntity<Void> createHttpEntity() {
        return new HttpEntity<>(createHeaders());
    }

    /**
     * Create HTTP headers for requests
     */
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
    }
}