package com.trms.mock.controller;

import com.trms.mock.dto.CreateTransactionRequest;
import com.trms.mock.model.Account;
import com.trms.mock.model.AccountBalance;
import com.trms.mock.model.Transaction;
import com.trms.mock.service.MockDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Transaction Management", description = "Operations related to transaction processing and booking")
@CrossOrigin(origins = "*")
public class TransactionController {
    
    private final MockDataService mockDataService;
    
    @GetMapping
    @Operation(summary = "Get all transactions", description = "Retrieve all transactions in the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved transactions")
    })
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        log.info("Fetching all transactions");
        
        List<Transaction> transactions = mockDataService.getAllTransactions();
        log.info("Found {} transactions", transactions.size());
        
        return ResponseEntity.ok(transactions);
    }
    
    @GetMapping("/{transactionId}")
    @Operation(summary = "Get transaction by ID", description = "Retrieve a specific transaction by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved transaction"),
        @ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    public ResponseEntity<Transaction> getTransactionById(
            @Parameter(description = "Transaction ID", example = "TXN-001")
            @PathVariable String transactionId) {
        
        log.info("Fetching transaction with ID: {}", transactionId);
        
        Optional<Transaction> transaction = mockDataService.getTransactionById(transactionId);
        if (transaction.isPresent()) {
            log.info("Transaction found: {} {} from {} to {}", 
                    transaction.get().getAmount(), 
                    transaction.get().getCurrency(),
                    transaction.get().getFromAccount(),
                    transaction.get().getToAccount());
            return ResponseEntity.ok(transaction.get());
        } else {
            log.warn("Transaction not found with ID: {}", transactionId);
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping
    @Operation(summary = "Create new transaction", description = "Book a new transaction between accounts")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Transaction created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid transaction request"),
        @ApiResponse(responseCode = "404", description = "Account not found"),
        @ApiResponse(responseCode = "422", description = "Business validation failed")
    })
    public ResponseEntity<?> createTransaction(
            @Valid @RequestBody CreateTransactionRequest request) {
        
        log.info("Creating transaction: {} {} from {} to {}", 
                request.getAmount(), request.getCurrency(), 
                request.getFromAccount(), request.getToAccount());
        
        // Validate from account exists
        Optional<Account> fromAccount = mockDataService.getAccountById(request.getFromAccount());
        if (fromAccount.isEmpty()) {
            log.warn("From account not found: {}", request.getFromAccount());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("From account not found: " + request.getFromAccount());
        }
        
        // Validate to account exists
        Optional<Account> toAccount = mockDataService.getAccountById(request.getToAccount());
        if (toAccount.isEmpty()) {
            log.warn("To account not found: {}", request.getToAccount());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("To account not found: " + request.getToAccount());
        }
        
        // Validate currency matches from account
        if (!fromAccount.get().getCurrency().equalsIgnoreCase(request.getCurrency())) {
            log.warn("Currency mismatch: account {} has currency {}, but transaction currency is {}", 
                    request.getFromAccount(), fromAccount.get().getCurrency(), request.getCurrency());
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body("Currency mismatch with from account");
        }
        
        // Validate currency matches to account
        if (!toAccount.get().getCurrency().equalsIgnoreCase(request.getCurrency())) {
            log.warn("Currency mismatch: account {} has currency {}, but transaction currency is {}", 
                    request.getToAccount(), toAccount.get().getCurrency(), request.getCurrency());
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body("Currency mismatch with to account");
        }
        
        // Check available balance
        Optional<AccountBalance> fromBalance = mockDataService.getAccountBalance(request.getFromAccount());
        if (fromBalance.isPresent() && fromBalance.get().getAvailableBalance().compareTo(request.getAmount()) < 0) {
            log.warn("Insufficient funds in account {}: available {}, requested {}", 
                    request.getFromAccount(), fromBalance.get().getAvailableBalance(), request.getAmount());
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body("Insufficient available funds");
        }
        
        // Create transaction
        String transactionId = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        Transaction transaction = Transaction.builder()
                .transactionId(transactionId)
                .fromAccount(request.getFromAccount())
                .toAccount(request.getToAccount())
                .amount(request.getAmount())
                .currency(request.getCurrency().toUpperCase())
                .status(Transaction.TransactionStatus.PENDING)
                .type(Transaction.TransactionType.TRANSFER)
                .description(request.getDescription())
                .reference(request.getReference())
                .settlementMethod(request.getSettlementMethod() != null ? request.getSettlementMethod() : "RTGS")
                .createdAt(LocalDateTime.now())
                .valueDate(LocalDateTime.now().toLocalDate().atStartOfDay())
                .build();
        
        String createdId = mockDataService.createTransaction(transaction);
        
        log.info("Transaction created successfully with ID: {}", createdId);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }
}