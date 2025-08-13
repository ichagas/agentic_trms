package com.trms.mock.service;

import com.trms.mock.dto.EODRunRequest;
import com.trms.mock.dto.ProposeFixingsRequest;
import com.trms.mock.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EODService {
    
    private final MockDataService mockDataService;
    
    public EODCheckResult checkEODReadiness() {
        log.info("Performing comprehensive EOD readiness check");
        
        // Check market data status
        MarketDataStatus marketDataStatus = aggregateMarketDataStatus();
        
        // Check transaction status
        TransactionStatusSummary transactionStatus = mockDataService.getTransactionStatusSummary();
        
        // Check missing rate resets
        List<RateReset> missingResets = mockDataService.getMissingRateResets();
        
        // Determine overall readiness
        boolean marketDataReady = marketDataStatus.getComplete();
        boolean transactionsReady = transactionStatus.getCriticalCount() == 0 && 
                                   transactionStatus.getCompletionPercentage() >= 95.0;
        boolean rateResetsReady = missingResets.isEmpty();
        
        boolean overallReady = marketDataReady && transactionsReady && rateResetsReady;
        
        // Generate required actions
        List<String> requiredActions = generateRequiredActions(marketDataStatus, transactionStatus, missingResets);
        
        // Generate blockers and warnings
        List<EODCheckResult.BlockerIssue> blockers = generateBlockers(marketDataStatus, transactionStatus, missingResets);
        List<EODCheckResult.WarningIssue> warnings = generateWarnings(marketDataStatus, transactionStatus, missingResets);
        
        // Calculate readiness percentage
        double readinessPercentage = calculateReadinessPercentage(marketDataReady, transactionsReady, rateResetsReady);
        
        EODCheckResult.EODStatus eodStatus = determineEODStatus(overallReady, blockers.size(), warnings.size());
        
        String summary = generateEODSummary(eodStatus, marketDataStatus, transactionStatus, missingResets.size());
        
        return EODCheckResult.builder()
                .ready(overallReady)
                .marketDataStatus(marketDataStatus)
                .transactionStatus(transactionStatus)
                .missingResets(missingResets)
                .requiredActions(requiredActions)
                .checkTime(LocalDateTime.now())
                .overallStatus(eodStatus)
                .summary(summary)
                .blockers(blockers)
                .warnings(warnings)
                .readinessPercentage(readinessPercentage)
                .build();
    }
    
    public List<RateReset> proposeRateFixings(ProposeFixingsRequest request) {
        log.info("Proposing rate fixings for instruments: {}", request.getInstrumentIds());
        
        List<RateReset> proposedFixings = new ArrayList<>();
        LocalDate fixingDate = request.getFixingDate() != null ? request.getFixingDate() : LocalDate.now();
        
        for (String instrumentId : request.getInstrumentIds()) {
            // Find existing rate reset or create new one
            Optional<RateReset> existingReset = mockDataService.getAllRateResets().stream()
                    .filter(reset -> reset.getInstrumentId().equals(instrumentId))
                    .findFirst();
            
            RateReset proposedReset;
            if (existingReset.isPresent()) {
                proposedReset = existingReset.get();
                proposedReset.setProposedRate(generateProposedRate(proposedReset.getIndexName(), proposedReset.getCurrency()));
                proposedReset.setStatus(RateReset.RateStatus.PROPOSED);
            } else {
                proposedReset = RateReset.builder()
                        .instrumentId(instrumentId)
                        .indexName(request.getIndexName() != null ? request.getIndexName() : "USD-LIBOR-3M")
                        .fixingDate(fixingDate)
                        .notional(new BigDecimal("5000000.00"))
                        .currency("USD")
                        .proposedRate(generateProposedRate(request.getIndexName(), "USD"))
                        .status(RateReset.RateStatus.PROPOSED)
                        .tenor("3M")
                        .createdAt(LocalDateTime.now())
                        .source(request.getSource() != null ? request.getSource() : "Bloomberg")
                        .description("AI-generated rate fixing proposal")
                        .build();
            }
            
            if (Boolean.TRUE.equals(request.getAutoApprove())) {
                proposedReset.setStatus(RateReset.RateStatus.APPROVED);
                proposedReset.setApprovedAt(LocalDateTime.now());
                proposedReset.setApprovedBy("ai-system");
            }
            
            mockDataService.updateRateReset(proposedReset);
            proposedFixings.add(proposedReset);
        }
        
        return proposedFixings;
    }
    
    public String runEOD(EODRunRequest request) {
        log.info("Initiating EOD run for business date: {}", request.getBusinessDate());
        
        LocalDate businessDate = request.getBusinessDate() != null ? request.getBusinessDate() : LocalDate.now();
        
        if (!Boolean.TRUE.equals(request.getForceRun()) && !Boolean.TRUE.equals(request.getSkipValidation())) {
            EODCheckResult readinessCheck = checkEODReadiness();
            if (!readinessCheck.getReady()) {
                return "EOD run cannot proceed. Readiness check failed. Use forceRun=true to override.";
            }
        }
        
        // Simulate EOD processing steps
        StringBuilder eodLog = new StringBuilder();
        eodLog.append("EOD Processing Started for ").append(businessDate).append("\n");
        eodLog.append("Initiated by: ").append(request.getInitiatedBy() != null ? request.getInitiatedBy() : "system").append("\n\n");
        
        // Step 1: Market Data Validation
        eodLog.append("Step 1: Market Data Validation\n");
        eodLog.append("- FX Rates: Validated\n");
        eodLog.append("- Equity Prices: Validated\n");
        eodLog.append("- Interest Rates: Partially validated (3 missing rates handled)\n\n");
        
        // Step 2: Transaction Settlement
        eodLog.append("Step 2: Transaction Settlement\n");
        eodLog.append("- Pending transactions: 1 settled\n");
        eodLog.append("- Failed transactions: 1 investigated and resolved\n\n");
        
        // Step 3: Rate Reset Processing
        eodLog.append("Step 3: Rate Reset Processing\n");
        eodLog.append("- Applied 3 rate fixings\n");
        eodLog.append("- Generated 2 rate proposals for approval\n\n");
        
        // Step 4: Position Reconciliation
        eodLog.append("Step 4: Position Reconciliation\n");
        eodLog.append("- All account balances reconciled\n");
        eodLog.append("- Cross-currency positions validated\n\n");
        
        // Step 5: Risk Calculations
        eodLog.append("Step 5: Risk Calculations\n");
        eodLog.append("- VaR calculations completed\n");
        eodLog.append("- Credit exposure limits checked\n");
        eodLog.append("- Liquidity ratios calculated\n\n");
        
        // Step 6: Regulatory Reporting
        eodLog.append("Step 6: Regulatory Reporting\n");
        eodLog.append("- Central bank reporting files generated\n");
        eodLog.append("- Risk reporting packages created\n\n");
        
        eodLog.append("EOD Processing Completed Successfully at ").append(LocalDateTime.now()).append("\n");
        
        return eodLog.toString();
    }
    
    private MarketDataStatus aggregateMarketDataStatus() {
        List<MarketDataStatus> allFeeds = mockDataService.getAllMarketDataStatus();
        
        int totalExpected = allFeeds.stream().mapToInt(MarketDataStatus::getExpected).sum();
        int totalReceived = allFeeds.stream().mapToInt(MarketDataStatus::getReceived).sum();
        int totalMissing = allFeeds.stream().mapToInt(MarketDataStatus::getMissing).sum();
        
        boolean overallComplete = allFeeds.stream().allMatch(MarketDataStatus::getComplete);
        
        List<String> allMissingItems = allFeeds.stream()
                .flatMap(feed -> feed.getMissingItems().stream())
                .collect(Collectors.toList());
                
        MarketDataStatus.FeedStatus overallStatus = overallComplete ? 
                MarketDataStatus.FeedStatus.HEALTHY : 
                (totalMissing > 10 ? MarketDataStatus.FeedStatus.INCOMPLETE : MarketDataStatus.FeedStatus.DELAYED);
        
        return MarketDataStatus.builder()
                .feedType("AGGREGATE")
                .expected(totalExpected)
                .received(totalReceived)
                .missing(totalMissing)
                .complete(overallComplete)
                .lastUpdate(LocalDateTime.now())
                .provider("Multiple")
                .status(overallStatus)
                .missingItems(allMissingItems)
                .build();
    }
    
    private List<String> generateRequiredActions(MarketDataStatus marketData, 
                                               TransactionStatusSummary transactions, 
                                               List<RateReset> missingResets) {
        List<String> actions = new ArrayList<>();
        
        if (!marketData.getComplete()) {
            actions.add("Resolve missing market data items: " + String.join(", ", marketData.getMissingItems()));
        }
        
        if (transactions.getCriticalCount() > 0) {
            actions.add("Investigate and resolve " + transactions.getCriticalCount() + " failed transactions");
        }
        
        if (transactions.getWarningCount() > 0) {
            actions.add("Process " + transactions.getWarningCount() + " pending transactions");
        }
        
        if (!missingResets.isEmpty()) {
            actions.add("Provide rate fixings for " + missingResets.size() + " instruments");
        }
        
        if (actions.isEmpty()) {
            actions.add("All systems ready - EOD processing can proceed");
        }
        
        return actions;
    }
    
    private List<EODCheckResult.BlockerIssue> generateBlockers(MarketDataStatus marketData, 
                                                             TransactionStatusSummary transactions, 
                                                             List<RateReset> missingResets) {
        List<EODCheckResult.BlockerIssue> blockers = new ArrayList<>();
        
        if (transactions.getCriticalCount() > 0) {
            blockers.add(EODCheckResult.BlockerIssue.builder()
                    .type("FAILED_TRANSACTIONS")
                    .description("Critical transactions in failed state")
                    .resolution("Investigate and reprocess failed transactions")
                    .severity(EODCheckResult.IssueSeverity.CRITICAL)
                    .build());
        }
        
        if (marketData.getMissing() > 20) {
            blockers.add(EODCheckResult.BlockerIssue.builder()
                    .type("MARKET_DATA_INCOMPLETE")
                    .description("Too many missing market data points")
                    .resolution("Contact market data provider or use alternative sources")
                    .severity(EODCheckResult.IssueSeverity.HIGH)
                    .build());
        }
        
        return blockers;
    }
    
    private List<EODCheckResult.WarningIssue> generateWarnings(MarketDataStatus marketData, 
                                                             TransactionStatusSummary transactions, 
                                                             List<RateReset> missingResets) {
        List<EODCheckResult.WarningIssue> warnings = new ArrayList<>();
        
        if (!missingResets.isEmpty()) {
            warnings.add(EODCheckResult.WarningIssue.builder()
                    .type("MISSING_RATE_RESETS")
                    .description(missingResets.size() + " instruments missing rate fixings")
                    .recommendation("Use AI rate fixing proposals or manual intervention")
                    .severity(EODCheckResult.IssueSeverity.MEDIUM)
                    .build());
        }
        
        if (transactions.getWarningCount() > 0) {
            warnings.add(EODCheckResult.WarningIssue.builder()
                    .type("PENDING_TRANSACTIONS")
                    .description(transactions.getWarningCount() + " transactions still pending")
                    .recommendation("Monitor settlement progress or investigate delays")
                    .severity(EODCheckResult.IssueSeverity.MEDIUM)
                    .build());
        }
        
        if (marketData.getMissing() > 0 && marketData.getMissing() <= 20) {
            warnings.add(EODCheckResult.WarningIssue.builder()
                    .type("MINOR_MARKET_DATA_GAPS")
                    .description("Minor gaps in market data coverage")
                    .recommendation("Acceptable for EOD processing but monitor for trends")
                    .severity(EODCheckResult.IssueSeverity.LOW)
                    .build());
        }
        
        return warnings;
    }
    
    private double calculateReadinessPercentage(boolean marketDataReady, boolean transactionsReady, boolean rateResetsReady) {
        int readyComponents = 0;
        if (marketDataReady) readyComponents++;
        if (transactionsReady) readyComponents++;
        if (rateResetsReady) readyComponents++;
        
        return (readyComponents / 3.0) * 100.0;
    }
    
    private EODCheckResult.EODStatus determineEODStatus(boolean overallReady, int blockerCount, int warningCount) {
        if (overallReady && blockerCount == 0) {
            return EODCheckResult.EODStatus.READY;
        } else if (blockerCount > 0) {
            return EODCheckResult.EODStatus.BLOCKED;
        } else if (warningCount > 0) {
            return EODCheckResult.EODStatus.PARTIAL_READY;
        } else {
            return EODCheckResult.EODStatus.NOT_READY;
        }
    }
    
    private String generateEODSummary(EODCheckResult.EODStatus status, MarketDataStatus marketData, 
                                    TransactionStatusSummary transactions, int missingResets) {
        StringBuilder summary = new StringBuilder();
        
        summary.append("EOD Status: ").append(status).append("\n");
        summary.append("Market Data: ").append(marketData.getReceived()).append("/").append(marketData.getExpected()).append(" received\n");
        summary.append("Transactions: ").append(String.format("%.1f%%", transactions.getCompletionPercentage())).append(" completion rate\n");
        summary.append("Rate Resets: ").append(missingResets).append(" missing fixings\n");
        
        if (status == EODCheckResult.EODStatus.READY) {
            summary.append("\nAll systems are ready for EOD processing.");
        } else {
            summary.append("\nReview required actions before proceeding with EOD.");
        }
        
        return summary.toString();
    }
    
    private BigDecimal generateProposedRate(String indexName, String currency) {
        // Simulate intelligent rate proposal based on market conditions
        Map<String, BigDecimal> baseRates = Map.of(
            "USD-LIBOR-3M", new BigDecimal("5.25"),
            "EUR-EURIBOR-6M", new BigDecimal("3.75"),
            "GBP-SONIA", new BigDecimal("4.95"),
            "USD", new BigDecimal("5.25"),
            "EUR", new BigDecimal("3.75"),
            "GBP", new BigDecimal("4.95")
        );
        
        BigDecimal baseRate = baseRates.getOrDefault(indexName, baseRates.get(currency));
        if (baseRate == null) {
            baseRate = new BigDecimal("4.50"); // Default rate
        }
        
        // Add small random variation to simulate market movement
        double variation = (Math.random() - 0.5) * 0.1; // +/- 5 basis points
        return baseRate.add(BigDecimal.valueOf(variation));
    }
}