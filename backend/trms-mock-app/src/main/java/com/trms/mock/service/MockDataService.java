package com.trms.mock.service;

import com.trms.mock.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class MockDataService {

    private static final Logger logger = LoggerFactory.getLogger(MockDataService.class);
    
    private final Map<String, Account> accounts = new ConcurrentHashMap<>();
    private final Map<String, AccountBalance> balances = new ConcurrentHashMap<>();
    private final Map<String, Transaction> transactions = new ConcurrentHashMap<>();
    private final Map<String, Report> reports = new ConcurrentHashMap<>();
    private final List<RateReset> rateResets = new ArrayList<>();
    private final Map<String, MarketDataStatus> marketDataFeeds = new ConcurrentHashMap<>();
    
    public MockDataService() {
        initializeMockData();
    }
    
    private void initializeMockData() {
        createMockAccounts();
        createMockBalances();
        createMockTransactions();
        createMockReports();
        createMockRateResets();
        createMockMarketDataFeeds();
    }
    
    private void createMockAccounts() {
        LocalDateTime now = LocalDateTime.now();
        
        // USD Accounts
        accounts.put("ACC-001-USD", Account.builder()
                .accountId("ACC-001-USD")
                .accountName("JP Morgan Chase USD Cash Account")
                .currency("USD")
                .accountType(Account.AccountType.CASH)
                .status(Account.AccountStatus.ACTIVE)
                .description("Primary USD operating account")
                .createdAt(now.minusMonths(12))
                .lastUpdated(now)
                .build());
                
        accounts.put("ACC-002-USD", Account.builder()
                .accountId("ACC-002-USD")
                .accountName("Goldman Sachs USD Trading Account")
                .currency("USD")
                .accountType(Account.AccountType.TRADING)
                .status(Account.AccountStatus.ACTIVE)
                .description("USD trading and settlement account")
                .createdAt(now.minusMonths(8))
                .lastUpdated(now)
                .build());
                
        accounts.put("ACC-003-USD", Account.builder()
                .accountId("ACC-003-USD")
                .accountName("Citibank USD Nostro Account")
                .currency("USD")
                .accountType(Account.AccountType.NOSTRO)
                .status(Account.AccountStatus.ACTIVE)
                .description("USD correspondent banking account")
                .createdAt(now.minusMonths(24))
                .lastUpdated(now)
                .build());
        
        // EUR Accounts
        accounts.put("ACC-004-EUR", Account.builder()
                .accountId("ACC-004-EUR")
                .accountName("Deutsche Bank EUR Cash Account")
                .currency("EUR")
                .accountType(Account.AccountType.CASH)
                .status(Account.AccountStatus.ACTIVE)
                .description("Primary EUR operating account")
                .createdAt(now.minusMonths(18))
                .lastUpdated(now)
                .build());
                
        accounts.put("ACC-005-EUR", Account.builder()
                .accountId("ACC-005-EUR")
                .accountName("BNP Paribas EUR Settlement Account")
                .currency("EUR")
                .accountType(Account.AccountType.SETTLEMENT)
                .status(Account.AccountStatus.ACTIVE)
                .description("EUR trade settlement account")
                .createdAt(now.minusMonths(6))
                .lastUpdated(now)
                .build());
        
        // GBP Accounts
        accounts.put("ACC-006-GBP", Account.builder()
                .accountId("ACC-006-GBP")
                .accountName("HSBC GBP Cash Account")
                .currency("GBP")
                .accountType(Account.AccountType.CASH)
                .status(Account.AccountStatus.ACTIVE)
                .description("Primary GBP operating account")
                .createdAt(now.minusMonths(15))
                .lastUpdated(now)
                .build());
                
        accounts.put("ACC-007-GBP", Account.builder()
                .accountId("ACC-007-GBP")
                .accountName("Barclays GBP Collateral Account")
                .currency("GBP")
                .accountType(Account.AccountType.COLLATERAL)
                .status(Account.AccountStatus.ACTIVE)
                .description("GBP collateral management account")
                .createdAt(now.minusMonths(10))
                .lastUpdated(now)
                .build());
        
        // JPY Account
        accounts.put("ACC-008-JPY", Account.builder()
                .accountId("ACC-008-JPY")
                .accountName("Sumitomo Mitsui JPY Account")
                .currency("JPY")
                .accountType(Account.AccountType.CASH)
                .status(Account.AccountStatus.ACTIVE)
                .description("Primary JPY operating account")
                .createdAt(now.minusMonths(20))
                .lastUpdated(now)
                .build());
    }
    
    private void createMockBalances() {
        LocalDateTime now = LocalDateTime.now();
        
        balances.put("ACC-001-USD", AccountBalance.builder()
                .accountId("ACC-001-USD")
                .currentBalance(new BigDecimal("15750000.00"))
                .availableBalance(new BigDecimal("15250000.00"))
                .reservedBalance(new BigDecimal("500000.00"))
                .currency("USD")
                .pendingCredits(new BigDecimal("250000.00"))
                .pendingDebits(new BigDecimal("125000.00"))
                .overdraftLimit(new BigDecimal("5000000.00"))
                .lastUpdated(now)
                .valueDate(now.toLocalDate())
                .build());
                
        balances.put("ACC-002-USD", AccountBalance.builder()
                .accountId("ACC-002-USD")
                .currentBalance(new BigDecimal("8950000.00"))
                .availableBalance(new BigDecimal("8450000.00"))
                .reservedBalance(new BigDecimal("500000.00"))
                .currency("USD")
                .pendingCredits(new BigDecimal("500000.00"))
                .pendingDebits(new BigDecimal("300000.00"))
                .overdraftLimit(new BigDecimal("2000000.00"))
                .lastUpdated(now)
                .valueDate(now.toLocalDate())
                .build());
                
        balances.put("ACC-003-USD", AccountBalance.builder()
                .accountId("ACC-003-USD")
                .currentBalance(new BigDecimal("12300000.00"))
                .availableBalance(new BigDecimal("12100000.00"))
                .reservedBalance(new BigDecimal("200000.00"))
                .currency("USD")
                .pendingCredits(new BigDecimal("150000.00"))
                .pendingDebits(new BigDecimal("75000.00"))
                .overdraftLimit(new BigDecimal("1000000.00"))
                .lastUpdated(now)
                .valueDate(now.toLocalDate())
                .build());
                
        balances.put("ACC-004-EUR", AccountBalance.builder()
                .accountId("ACC-004-EUR")
                .currentBalance(new BigDecimal("9850000.00"))
                .availableBalance(new BigDecimal("9350000.00"))
                .reservedBalance(new BigDecimal("500000.00"))
                .currency("EUR")
                .pendingCredits(new BigDecimal("400000.00"))
                .pendingDebits(new BigDecimal("200000.00"))
                .overdraftLimit(new BigDecimal("3000000.00"))
                .lastUpdated(now)
                .valueDate(now.toLocalDate())
                .build());
                
        balances.put("ACC-005-EUR", AccountBalance.builder()
                .accountId("ACC-005-EUR")
                .currentBalance(new BigDecimal("6750000.00"))
                .availableBalance(new BigDecimal("6250000.00"))
                .reservedBalance(new BigDecimal("500000.00"))
                .currency("EUR")
                .pendingCredits(new BigDecimal("300000.00"))
                .pendingDebits(new BigDecimal("150000.00"))
                .overdraftLimit(new BigDecimal("1500000.00"))
                .lastUpdated(now)
                .valueDate(now.toLocalDate())
                .build());
                
        balances.put("ACC-006-GBP", AccountBalance.builder()
                .accountId("ACC-006-GBP")
                .currentBalance(new BigDecimal("7250000.00"))
                .availableBalance(new BigDecimal("6950000.00"))
                .reservedBalance(new BigDecimal("300000.00"))
                .currency("GBP")
                .pendingCredits(new BigDecimal("200000.00"))
                .pendingDebits(new BigDecimal("100000.00"))
                .overdraftLimit(new BigDecimal("2000000.00"))
                .lastUpdated(now)
                .valueDate(now.toLocalDate())
                .build());
                
        balances.put("ACC-007-GBP", AccountBalance.builder()
                .accountId("ACC-007-GBP")
                .currentBalance(new BigDecimal("4500000.00"))
                .availableBalance(new BigDecimal("4200000.00"))
                .reservedBalance(new BigDecimal("300000.00"))
                .currency("GBP")
                .pendingCredits(new BigDecimal("150000.00"))
                .pendingDebits(new BigDecimal("75000.00"))
                .overdraftLimit(new BigDecimal("1000000.00"))
                .lastUpdated(now)
                .valueDate(now.toLocalDate())
                .build());
                
        balances.put("ACC-008-JPY", AccountBalance.builder()
                .accountId("ACC-008-JPY")
                .currentBalance(new BigDecimal("1850000000.00"))
                .availableBalance(new BigDecimal("1800000000.00"))
                .reservedBalance(new BigDecimal("50000000.00"))
                .currency("JPY")
                .pendingCredits(new BigDecimal("25000000.00"))
                .pendingDebits(new BigDecimal("15000000.00"))
                .overdraftLimit(new BigDecimal("200000000.00"))
                .lastUpdated(now)
                .valueDate(now.toLocalDate())
                .build());
    }
    
    private void createMockTransactions() {
        LocalDateTime now = LocalDateTime.now();
        
        // Create transactions matching specification: Total=1247, VALIDATED=1189, NEW=23, PROPOSAL=35
        
        // 1189 VALIDATED transactions
        for (int i = 1; i <= 1189; i++) {
            String txnId = String.format("TXN-%04d", i);
            transactions.put(txnId, Transaction.builder()
                    .transactionId(txnId)
                    .fromAccount("ACC-001-USD")
                    .toAccount("ACC-002-USD")
                    .amount(new BigDecimal(String.valueOf(100000 + (i * 1000))))
                    .currency("USD")
                    .status(Transaction.TransactionStatus.VALIDATED)
                    .type(Transaction.TransactionType.TRANSFER)
                    .description("Validated transaction - " + i)
                    .reference("REF-VAL-" + i)
                    .createdAt(now.minusHours(2).minusMinutes(i % 60))
                    .valueDate(now.toLocalDate().atStartOfDay())
                    .settlementMethod("RTGS")
                    .build());
        }
        /*
        // 23 NEW transactions (need validation)
        for (int i = 1190; i <= 1212; i++) {
            String txnId = String.format("TXN-%04d", i);
            transactions.put(txnId, Transaction.builder()
                    .transactionId(txnId)
                    .fromAccount("ACC-004-EUR")
                    .toAccount("ACC-005-EUR")
                    .amount(new BigDecimal(String.valueOf(200000 + (i * 500))))
                    .currency("EUR")
                    .status(Transaction.TransactionStatus.NEW)
                    .type(Transaction.TransactionType.FX_SETTLEMENT)
                    .description("New transaction awaiting validation - " + (i - 1189))
                    .reference("REF-NEW-" + (i - 1189))
                    .createdAt(now.minusMinutes(30 + (i % 30)))
                    .valueDate(now.toLocalDate().atStartOfDay())
                    .settlementMethod("TARGET2")
                    .build());
        }
        
        // 35 PROPOSAL transactions (need review)
        for (int i = 1213; i <= 1247; i++) {
            String txnId = String.format("TXN-%04d", i);
            transactions.put(txnId, Transaction.builder()
                    .transactionId(txnId)
                    .fromAccount("ACC-006-GBP")
                    .toAccount("ACC-007-GBP")
                    .amount(new BigDecimal(String.valueOf(150000 + (i * 750))))
                    .currency("GBP")
                    .status(Transaction.TransactionStatus.VALIDATED)
                    .type(Transaction.TransactionType.COLLATERAL_MOVEMENT)
                    .description("Proposal transaction awaiting review - " + (i - 1212))
                    .reference("REF-PROP-" + (i - 1212))
                    .createdAt(now.minusMinutes(15 + (i % 15)))
                    .valueDate(now.toLocalDate().atStartOfDay())
                    .settlementMethod("CHAPS")
                    .build());
        }
                    */
    }
    
    private void createMockReports() {
        LocalDateTime now = LocalDateTime.now();
        
        reports.put("RPT-001", Report.builder()
                .reportId("RPT-001")
                .title("Daily Cash Position Report")
                .type(Report.ReportType.BALANCE_REPORT)
                .content("Comprehensive daily cash position across all accounts and currencies")
                .status(Report.ReportStatus.COMPLETED)
                .createdDate(now.minusHours(1))
                .lastModified(now.minusHours(1))
                .generatedBy("system")
                .format("PDF")
                .build());
                
        reports.put("RPT-002", Report.builder()
                .reportId("RPT-002")
                .title("EOD Transaction Summary")
                .type(Report.ReportType.TRANSACTION_SUMMARY)
                .content("End-of-day transaction processing summary with settlement status")
                .status(Report.ReportStatus.GENERATING)
                .createdDate(now.minusMinutes(30))
                .generatedBy("system")
                .format("Excel")
                .build());
    }
    
    private void createMockRateResets() {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();
        
        // SWAP-2024-0156 as specified in the spec document
        rateResets.add(RateReset.builder()
                .instrumentId("SWAP-2024-0156")
                .indexName("USD-LIBOR-3M")
                .fixingDate(today)
                .notional(new BigDecimal("15000000"))
                .currency("USD")
                .status(RateReset.RateStatus.MISSING)
                .tenor("3M")
                .createdAt(now.minusHours(2))
                .source("Bloomberg")
                .description("USD LIBOR 3-month fixing for interest rate swap - Missing from specification")
                .build());
                
        // Additional missing rate resets to simulate real EOD scenario
        rateResets.add(RateReset.builder()
                .instrumentId("SWAP-2024-0157")
                .indexName("USD-LIBOR-3M")
                .fixingDate(today)
                .notional(new BigDecimal("8500000"))
                .currency("USD")
                .status(RateReset.RateStatus.MISSING)
                .tenor("3M")
                .createdAt(now.minusHours(2))
                .source("Bloomberg")
                .description("USD LIBOR 3-month fixing for interest rate swap")
                .build());
                
        rateResets.add(RateReset.builder()
                .instrumentId("SWAP-2024-0158")
                .indexName("EUR-EURIBOR-6M")
                .fixingDate(today)
                .notional(new BigDecimal("12000000"))
                .currency("EUR")
                .status(RateReset.RateStatus.MISSING)
                .tenor("6M")
                .createdAt(now.minusHours(1))
                .source("Reuters")
                .description("EUR EURIBOR 6-month fixing for interest rate swap")
                .build());
                
        // Some completed resets for variety
        rateResets.add(RateReset.builder()
                .instrumentId("SWAP-2024-0159")
                .indexName("GBP-SONIA")
                .fixingDate(today.minusDays(1))
                .notional(new BigDecimal("8000000.00"))
                .currency("GBP")
                .currentRate(new BigDecimal("4.95"))
                .proposedRate(new BigDecimal("4.98"))
                .status(RateReset.RateStatus.APPROVED)
                .tenor("1D")
                .createdAt(now.minusHours(6))
                .approvedAt(now.minusHours(3))
                .approvedBy("treasury-manager")
                .source("Bank of England")
                .description("GBP SONIA overnight fixing")
                .build());
    }
    
    private void createMockMarketDataFeeds() {
        LocalDateTime now = LocalDateTime.now();
        
        // FX_RATES: Complete as per specification
        marketDataFeeds.put("FX_RATES", MarketDataStatus.builder()
                .feedType("FX_RATES")
                .expected(284)
                .received(284)
                .missing(0)
                .complete(true)
                .lastUpdate(now.minusMinutes(5))
                .cutoffTime(now.minusHours(1))
                .provider("Bloomberg")
                .status(MarketDataStatus.FeedStatus.HEALTHY)
                .missingItems(Collections.emptyList())
                .build());
                
        // EQUITY_PRICES: Incomplete as per specification
        marketDataFeeds.put("EQUITY_PRICES", MarketDataStatus.builder()
                .feedType("EQUITY_PRICES")
                .expected(205)
                .received(205)
                .missing(0)
                .complete(true)
                .lastUpdate(now.minusMinutes(2))
                .cutoffTime(now.minusHours(1))
                .provider("Reuters")
                .status(MarketDataStatus.FeedStatus.HEALTHY)
                //.missingItems(Arrays.asList("AAPL", "GOOGL", "MSFT", "AMZN", "TSLA", "NVDA", "META", "BRK.B"))
                .missingItems(Collections.emptyList())
                .build());
                
        marketDataFeeds.put("INTEREST_RATES", MarketDataStatus.builder()
                .feedType("INTEREST_RATES")
                .expected(89)
                .received(89)
                .missing(0)
                .complete(true)
                .lastUpdate(now.minusMinutes(10))
                .cutoffTime(now.minusHours(1))
                .provider("ICE")
                .status(MarketDataStatus.FeedStatus.HEALTHY)
                //.missingItems(Arrays.asList("USD-LIBOR-1M", "EUR-EURIBOR-12M", "GBP-SONIA"))
                .missingItems(Collections.emptyList())
                .build());
    }
    
    // Getter methods for accessing mock data
    
    public List<Account> getAllAccounts() {
        return new ArrayList<>(accounts.values());
    }
    
    public List<Account> getAccountsByCurrency(String currency) {
        return accounts.values().stream()
                .filter(account -> account.getCurrency().equalsIgnoreCase(currency))
                .collect(Collectors.toList());
    }
    
    public Optional<Account> getAccountById(String accountId) {
        return Optional.ofNullable(accounts.get(accountId));
    }
    
    public Optional<AccountBalance> getAccountBalance(String accountId) {
        return Optional.ofNullable(balances.get(accountId));
    }
    
    public List<Transaction> getAllTransactions() {
        return new ArrayList<>(transactions.values());
    }
    
    public Optional<Transaction> getTransactionById(String transactionId) {
        return Optional.ofNullable(transactions.get(transactionId));
    }
    
    public String createTransaction(Transaction transaction) {
        transactions.put(transaction.getTransactionId(), transaction);

        // Only update account balances if transaction is VALIDATED (not PENDING)
        if (transaction.getStatus() == Transaction.TransactionStatus.VALIDATED) {
            updateAccountBalancesForTransaction(transaction);
        }

        return transaction.getTransactionId();
    }

    public void updateTransaction(Transaction transaction) {
        Transaction oldTransaction = transactions.get(transaction.getTransactionId());
        transactions.put(transaction.getTransactionId(), transaction);

        logger.info("Transaction updated: {} - new status: {}",
                   transaction.getTransactionId(), transaction.getStatus());

        // Update balances when transaction status changes from PENDING to VALIDATED
        if (oldTransaction != null &&
            oldTransaction.getStatus() == Transaction.TransactionStatus.PENDING &&
            transaction.getStatus() == Transaction.TransactionStatus.VALIDATED) {
            logger.info("Transaction approved - updating account balances for: {}",
                       transaction.getTransactionId());
            updateAccountBalancesForTransaction(transaction);
        }
    }

    /**
     * Update account balances based on a new transaction
     * Debits from the fromAccount and credits to the toAccount
     */
    private void updateAccountBalancesForTransaction(Transaction transaction) {
        // Get fromAccount balance
        AccountBalance fromBalance = balances.get(transaction.getFromAccount());
        if (fromBalance != null) {
            // Debit from source account
            BigDecimal newCurrentBalance = fromBalance.getCurrentBalance().subtract(transaction.getAmount());
            BigDecimal newAvailableBalance = fromBalance.getAvailableBalance().subtract(transaction.getAmount());

            AccountBalance updatedFromBalance = AccountBalance.builder()
                    .accountId(fromBalance.getAccountId())
                    .currentBalance(newCurrentBalance)
                    .availableBalance(newAvailableBalance)
                    .reservedBalance(fromBalance.getReservedBalance())
                    .currency(fromBalance.getCurrency())
                    .pendingCredits(fromBalance.getPendingCredits())
                    .pendingDebits(fromBalance.getPendingDebits())
                    .overdraftLimit(fromBalance.getOverdraftLimit())
                    .valueDate(fromBalance.getValueDate())
                    .lastUpdated(LocalDateTime.now())
                    .build();

            balances.put(transaction.getFromAccount(), updatedFromBalance);

            logger.info("Updated balance for {}: {} -> {}",
                    transaction.getFromAccount(),
                    fromBalance.getCurrentBalance(),
                    newCurrentBalance);
        }

        // Get toAccount balance
        AccountBalance toBalance = balances.get(transaction.getToAccount());
        if (toBalance != null) {
            // Credit to destination account
            BigDecimal newCurrentBalance = toBalance.getCurrentBalance().add(transaction.getAmount());
            BigDecimal newAvailableBalance = toBalance.getAvailableBalance().add(transaction.getAmount());

            AccountBalance updatedToBalance = AccountBalance.builder()
                    .accountId(toBalance.getAccountId())
                    .currentBalance(newCurrentBalance)
                    .availableBalance(newAvailableBalance)
                    .reservedBalance(toBalance.getReservedBalance())
                    .currency(toBalance.getCurrency())
                    .pendingCredits(toBalance.getPendingCredits())
                    .pendingDebits(toBalance.getPendingDebits())
                    .overdraftLimit(toBalance.getOverdraftLimit())
                    .valueDate(toBalance.getValueDate())
                    .lastUpdated(LocalDateTime.now())
                    .build();

            balances.put(transaction.getToAccount(), updatedToBalance);

            logger.info("Updated balance for {}: {} -> {}",
                    transaction.getToAccount(),
                    toBalance.getCurrentBalance(),
                    newCurrentBalance);
        }
    }
    
    public Optional<Report> getReportById(String reportId) {
        return Optional.ofNullable(reports.get(reportId));
    }
    
    public List<RateReset> getMissingRateResets() {
        return rateResets.stream()
                .filter(reset -> reset.getStatus() == RateReset.RateStatus.MISSING)
                .collect(Collectors.toList());
    }
    
    public List<RateReset> getAllRateResets() {
        return new ArrayList<>(rateResets);
    }
    
    public void updateRateReset(RateReset rateReset) {
        for (int i = 0; i < rateResets.size(); i++) {
            if (rateResets.get(i).getInstrumentId().equals(rateReset.getInstrumentId())) {
                rateResets.set(i, rateReset);
                break;
            }
        }
    }
    
    public MarketDataStatus getMarketDataStatus(String feedType) {
        return marketDataFeeds.get(feedType);
    }
    
    public List<MarketDataStatus> getAllMarketDataStatus() {
        return new ArrayList<>(marketDataFeeds.values());
    }
    
    public TransactionStatusSummary getTransactionStatusSummary() {
        Map<Transaction.TransactionStatus, Integer> statusCounts = new HashMap<>();
        List<Transaction> pendingTxns = new ArrayList<>();
        List<Transaction> failedTxns = new ArrayList<>();
        
        for (Transaction txn : transactions.values()) {
            statusCounts.merge(txn.getStatus(), 1, Integer::sum);
            if (txn.getStatus() == Transaction.TransactionStatus.PENDING) {
                pendingTxns.add(txn);
            } else if (txn.getStatus() == Transaction.TransactionStatus.FAILED) {
                failedTxns.add(txn);
            }
        }
        
        int total = transactions.size();
        int completed = statusCounts.getOrDefault(Transaction.TransactionStatus.SETTLED, 0);
        double completionPercentage = total > 0 ? (completed * 100.0 / total) : 0.0;
        
        return TransactionStatusSummary.builder()
                .total(total)
                .statusCounts(statusCounts)
                .pendingTransactions(pendingTxns)
                .failedTransactions(failedTxns)
                .lastUpdated(LocalDateTime.now())
                .criticalCount(failedTxns.size())
                .warningCount(pendingTxns.size())
                .completionPercentage(completionPercentage)
                .build();
    }
}