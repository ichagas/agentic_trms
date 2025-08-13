package com.trms.mock.service;

import com.trms.mock.model.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class MockDataService {
    
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
        
        transactions.put("TXN-001", Transaction.builder()
                .transactionId("TXN-001")
                .fromAccount("ACC-001-USD")
                .toAccount("ACC-002-USD")
                .amount(new BigDecimal("500000.00"))
                .currency("USD")
                .status(Transaction.TransactionStatus.SETTLED)
                .type(Transaction.TransactionType.TRANSFER)
                .description("Intraday funding transfer")
                .reference("REF-001-2024")
                .createdAt(now.minusHours(2))
                .valueDate(now.toLocalDate().atStartOfDay())
                .settledAt(now.minusHours(1))
                .settlementMethod("RTGS")
                .build());
                
        transactions.put("TXN-002", Transaction.builder()
                .transactionId("TXN-002")
                .fromAccount("ACC-004-EUR")
                .toAccount("ACC-005-EUR")
                .amount(new BigDecimal("750000.00"))
                .currency("EUR")
                .status(Transaction.TransactionStatus.PENDING)
                .type(Transaction.TransactionType.FX_SETTLEMENT)
                .description("EUR leg of FX trade settlement")
                .reference("FX-EUR-001-2024")
                .createdAt(now.minusMinutes(30))
                .valueDate(now.toLocalDate().atStartOfDay())
                .settlementMethod("TARGET2")
                .build());
                
        transactions.put("TXN-003", Transaction.builder()
                .transactionId("TXN-003")
                .fromAccount("ACC-006-GBP")
                .toAccount("ACC-007-GBP")
                .amount(new BigDecimal("250000.00"))
                .currency("GBP")
                .status(Transaction.TransactionStatus.VALIDATED)
                .type(Transaction.TransactionType.COLLATERAL_MOVEMENT)
                .description("Collateral posting for derivative trades")
                .reference("COL-001-2024")
                .createdAt(now.minusMinutes(15))
                .valueDate(now.toLocalDate().atStartOfDay())
                .settlementMethod("CHAPS")
                .build());
                
        transactions.put("TXN-004", Transaction.builder()
                .transactionId("TXN-004")
                .fromAccount("ACC-008-JPY")
                .toAccount("ACC-001-USD")
                .amount(new BigDecimal("50000000.00"))
                .currency("JPY")
                .status(Transaction.TransactionStatus.FAILED)
                .type(Transaction.TransactionType.FX_SETTLEMENT)
                .description("JPY leg of USD/JPY FX settlement")
                .reference("FX-JPY-001-2024")
                .createdAt(now.minusHours(4))
                .valueDate(now.toLocalDate().atStartOfDay())
                .reasonCode("INSUFFICIENT_FUNDS")
                .settlementMethod("ZENGIN")
                .build());
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
        
        rateResets.add(RateReset.builder()
                .instrumentId("SWAP-USD-001")
                .indexName("USD-LIBOR-3M")
                .fixingDate(today)
                .notional(new BigDecimal("10000000.00"))
                .currency("USD")
                .currentRate(new BigDecimal("5.25"))
                .status(RateReset.RateStatus.MISSING)
                .tenor("3M")
                .createdAt(LocalDateTime.now().minusHours(2))
                .source("Bloomberg")
                .description("USD LIBOR 3-month fixing for interest rate swap")
                .build());
                
        rateResets.add(RateReset.builder()
                .instrumentId("SWAP-EUR-002")
                .indexName("EUR-EURIBOR-6M")
                .fixingDate(today)
                .notional(new BigDecimal("15000000.00"))
                .currency("EUR")
                .currentRate(new BigDecimal("3.75"))
                .proposedRate(new BigDecimal("3.78"))
                .status(RateReset.RateStatus.PROPOSED)
                .tenor("6M")
                .createdAt(LocalDateTime.now().minusHours(1))
                .source("Reuters")
                .description("EUR EURIBOR 6-month fixing for interest rate swap")
                .build());
                
        rateResets.add(RateReset.builder()
                .instrumentId("SWAP-GBP-003")
                .indexName("GBP-SONIA")
                .fixingDate(today.minusDays(1))
                .notional(new BigDecimal("8000000.00"))
                .currency("GBP")
                .currentRate(new BigDecimal("4.95"))
                .proposedRate(new BigDecimal("4.98"))
                .status(RateReset.RateStatus.APPROVED)
                .tenor("1D")
                .createdAt(LocalDateTime.now().minusHours(6))
                .approvedAt(LocalDateTime.now().minusHours(3))
                .approvedBy("treasury-manager")
                .source("Bank of England")
                .description("GBP SONIA overnight fixing")
                .build());
    }
    
    private void createMockMarketDataFeeds() {
        LocalDateTime now = LocalDateTime.now();
        
        marketDataFeeds.put("FX_RATES", MarketDataStatus.builder()
                .feedType("FX_RATES")
                .expected(157)
                .received(155)
                .missing(2)
                .complete(false)
                .lastUpdate(now.minusMinutes(5))
                .cutoffTime(now.minusHours(1))
                .provider("Bloomberg")
                .status(MarketDataStatus.FeedStatus.INCOMPLETE)
                .missingItems(Arrays.asList("USD/ZAR", "EUR/TRY"))
                .build());
                
        marketDataFeeds.put("EQUITY_PRICES", MarketDataStatus.builder()
                .feedType("EQUITY_PRICES")
                .expected(2500)
                .received(2500)
                .missing(0)
                .complete(true)
                .lastUpdate(now.minusMinutes(2))
                .cutoffTime(now.minusHours(1))
                .provider("Reuters")
                .status(MarketDataStatus.FeedStatus.HEALTHY)
                .missingItems(Collections.emptyList())
                .build());
                
        marketDataFeeds.put("INTEREST_RATES", MarketDataStatus.builder()
                .feedType("INTEREST_RATES")
                .expected(89)
                .received(86)
                .missing(3)
                .complete(false)
                .lastUpdate(now.minusMinutes(10))
                .cutoffTime(now.minusHours(1))
                .provider("ICE")
                .status(MarketDataStatus.FeedStatus.DELAYED)
                .missingItems(Arrays.asList("USD-LIBOR-1M", "EUR-EURIBOR-12M", "GBP-SONIA"))
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
        return transaction.getTransactionId();
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