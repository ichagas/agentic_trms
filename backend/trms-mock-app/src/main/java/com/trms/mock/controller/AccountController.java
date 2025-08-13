package com.trms.mock.controller;

import com.trms.mock.model.Account;
import com.trms.mock.model.AccountBalance;
import com.trms.mock.service.MockDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/accounts")
@Tag(name = "Account Management", description = "Operations related to account management and balance inquiries")
@CrossOrigin(origins = "*")
public class AccountController {
    
    private static final Logger log = LoggerFactory.getLogger(AccountController.class);
    private final MockDataService mockDataService;
    
    public AccountController(MockDataService mockDataService) {
        this.mockDataService = mockDataService;
    }
    
    @GetMapping
    @Operation(summary = "Get all accounts", description = "Retrieve all accounts, optionally filtered by currency")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved accounts"),
        @ApiResponse(responseCode = "400", description = "Invalid currency parameter")
    })
    public ResponseEntity<List<Account>> getAccounts(
            @Parameter(description = "Currency filter (optional)", example = "USD")
            @RequestParam(required = false) String currency) {
        
        log.info("Fetching accounts with currency filter: {}", currency);
        
        List<Account> accounts;
        if (currency != null && !currency.trim().isEmpty()) {
            accounts = mockDataService.getAccountsByCurrency(currency.trim().toUpperCase());
            log.info("Found {} accounts for currency: {}", accounts.size(), currency);
        } else {
            accounts = mockDataService.getAllAccounts();
            log.info("Found {} total accounts", accounts.size());
        }
        
        return ResponseEntity.ok(accounts);
    }
    
    @GetMapping("/{accountId}")
    @Operation(summary = "Get account by ID", description = "Retrieve a specific account by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved account"),
        @ApiResponse(responseCode = "404", description = "Account not found")
    })
    public ResponseEntity<Account> getAccountById(
            @Parameter(description = "Account ID", example = "ACC-001-USD")
            @PathVariable String accountId) {
        
        log.info("Fetching account with ID: {}", accountId);
        
        Optional<Account> account = mockDataService.getAccountById(accountId);
        if (account.isPresent()) {
            log.info("Account found: {}", account.get().getAccountName());
            return ResponseEntity.ok(account.get());
        } else {
            log.warn("Account not found with ID: {}", accountId);
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/{accountId}/balance")
    @Operation(summary = "Get account balance", description = "Retrieve the current balance information for a specific account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved balance"),
        @ApiResponse(responseCode = "404", description = "Account or balance not found")
    })
    public ResponseEntity<AccountBalance> getAccountBalance(
            @Parameter(description = "Account ID", example = "ACC-001-USD")
            @PathVariable String accountId) {
        
        log.info("Fetching balance for account: {}", accountId);
        
        // First verify account exists
        Optional<Account> account = mockDataService.getAccountById(accountId);
        if (account.isEmpty()) {
            log.warn("Account not found with ID: {}", accountId);
            return ResponseEntity.notFound().build();
        }
        
        Optional<AccountBalance> balance = mockDataService.getAccountBalance(accountId);
        if (balance.isPresent()) {
            log.info("Balance found for account {}: {} {}", 
                    accountId, balance.get().getCurrentBalance(), balance.get().getCurrency());
            return ResponseEntity.ok(balance.get());
        } else {
            log.warn("Balance not found for account: {}", accountId);
            return ResponseEntity.notFound().build();
        }
    }
}