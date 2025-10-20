package com.trms.ai.service;

import com.trms.ai.client.LegacyTrmsClient;
import com.trms.ai.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Function;

/**
 * TRMS Functions for Spring AI integration
 * 
 * This service provides AI-callable functions for Treasury and Risk Management System operations.
 * Each function is annotated with @Bean and @Description to enable Spring AI function calling.
 */
@Service
public class TrmsFunctions {

    private static final Logger logger = LoggerFactory.getLogger(TrmsFunctions.class);

    public final LegacyTrmsClient legacyTrmsClient;
    private final FunctionCallTracker functionCallTracker;

    public TrmsFunctions(LegacyTrmsClient legacyTrmsClient, FunctionCallTracker functionCallTracker) {
        this.legacyTrmsClient = legacyTrmsClient;
        this.functionCallTracker = functionCallTracker;
    }

    /**
     * Function to get accounts by currency
     */
    @Bean
    @Description("Get a list of accounts filtered by currency from the TRMS system. " +
                "Pass a 3-letter currency code (e.g., USD, EUR, GBP) to filter accounts, " +
                "or leave empty to get all accounts. Returns account metadata (ID, name, type, status) " +
                "but does NOT include balance information. " +
                "Use checkAccountBalance function instead if you need balance details.")
    public Function<GetAccountsByCurrencyRequest, List<Account>> getAccountsByCurrency() {
        return request -> {
            functionCallTracker.trackFunctionCall("getAccountsByCurrency");
            logger.debug("AI function call: getAccountsByCurrency with currency: {}", request.currency());
            try {
                List<Account> accounts = legacyTrmsClient.getAccountsByCurrency(request.currency());
                logger.info("Retrieved {} accounts for currency: {}", accounts.size(), request.currency());
                return accounts;
            } catch (Exception e) {
                logger.error("Error in getAccountsByCurrency function: {}", e.getMessage());
                throw new RuntimeException("Failed to retrieve accounts: " + e.getMessage());
            }
        };
    }

    /**
     * Function to check account balance
     */
    @Bean
    @Description("Check the current balance for a specific account. " +
                "REQUIRED: Pass the exact account ID (e.g., ACC-001-USD, ACC-002-EUR). " +
                "Returns detailed balance information: current balance, available balance, " +
                "pending transactions, and last update timestamp. " +
                "ALWAYS use this function when user asks about account balance or available funds.")
    public Function<CheckAccountBalanceRequest, AccountBalance> checkAccountBalance() {
        return request -> {
            functionCallTracker.trackFunctionCall("checkAccountBalance");
            logger.debug("AI function call: checkAccountBalance for account: {}", request.accountId());
            try {
                AccountBalance balance = legacyTrmsClient.checkAccountBalance(request.accountId());
                logger.info("Retrieved balance for account {}: {} {}",
                           request.accountId(), balance.currentBalance(), balance.currency());
                return balance;
            } catch (Exception e) {
                logger.error("Error in checkAccountBalance function: {}", e.getMessage());
                throw new RuntimeException("Failed to check account balance: " + e.getMessage());
            }
        };
    }

    /**
     * Function to book a transaction
     */
    @Bean
    @Description("Book a financial transaction between two accounts in the TRMS system. " +
                "Requires source account ID, destination account ID, amount, and currency. " +
                "The transaction will be processed immediately and returns transaction details " +
                "including transaction ID, status, and reference number. Validates account " +
                "existence and sufficient balance before processing.")
    public Function<BookTransactionRequest, Transaction> bookTransaction() {
        return request -> {
            functionCallTracker.trackFunctionCall("bookTransaction");
            logger.debug("AI function call: bookTransaction - {} {} from {} to {}",
                        request.amount(), request.currency(), request.fromAccount(), request.toAccount());
            try {
                Transaction transaction = legacyTrmsClient.bookTransaction(
                    request.fromAccount(),
                    request.toAccount(),
                    request.amount(),
                    request.currency()
                );
                logger.info("Successfully booked transaction: {} for {} {}",
                           transaction.id(), request.amount(), request.currency());
                return transaction;
            } catch (Exception e) {
                logger.error("Error in bookTransaction function: {}", e.getMessage());
                throw new RuntimeException("Failed to book transaction: " + e.getMessage());
            }
        };
    }

    /**
     * Function to check End of Day readiness
     */
    @Bean
    @Description("Check the End of Day (EOD) processing readiness status in the TRMS system. " +
                "Returns comprehensive status including whether EOD can be run, last run date, " +
                "next scheduled run, any blocking issues, completed checks, and pending tasks. " +
                "Use this to determine if the system is ready for daily closing procedures.")
    public Function<CheckEODReadinessRequest, EODStatus> checkEODReadiness() {
        return request -> {
            functionCallTracker.trackFunctionCall("checkEODReadiness");
            logger.debug("AI function call: checkEODReadiness");
            try {
                EODStatus status = legacyTrmsClient.checkEODReadiness();
                logger.info("EOD readiness status: ready={}, status={}",
                           status.isReady(), status.status());
                return status;
            } catch (Exception e) {
                logger.error("Error in checkEODReadiness function: {}", e.getMessage());
                throw new RuntimeException("Failed to check EOD readiness: " + e.getMessage());
            }
        };
    }

    /**
     * Function to propose rate fixings
     */
    @Bean
    @Description("Get proposed rate fixings for missing interest rate resets in the TRMS system. " +
                "Returns a list of currency/tenor combinations that need rate fixes for EOD processing, " +
                "including current rates, proposed rates, reset dates, and data sources. " +
                "Use this to identify and resolve missing market data before running EOD procedures.")
    public Function<ProposeRateFixingsRequest, List<RateReset>> proposeRateFixings() {
        return request -> {
            functionCallTracker.trackFunctionCall("proposeRateFixings");
            logger.debug("AI function call: proposeRateFixings");
            try {
                List<RateReset> rateResets = legacyTrmsClient.proposeRateFixings();
                logger.info("Retrieved {} rate fixing proposals", rateResets.size());
                return rateResets;
            } catch (Exception e) {
                logger.error("Error in proposeRateFixings function: {}", e.getMessage());
                throw new RuntimeException("Failed to get rate fixings: " + e.getMessage());
            }
        };
    }

    // Request records for function parameters
    public record GetAccountsByCurrencyRequest(String currency) {}
    public record CheckAccountBalanceRequest(String accountId) {}
    public record BookTransactionRequest(String fromAccount, String toAccount, Double amount, String currency) {}
    public record CheckEODReadinessRequest() {}
    public record ProposeRateFixingsRequest() {}
}