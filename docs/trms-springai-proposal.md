# TRMS AI Agent POC - Implementation Task Plan

## Project Overview
A simplified Proof of Concept for AI-powered Treasury and Risk Management System with three independent components that can be developed in parallel.

## Project Structure
```
trms-ai-poc/
├── trms-legacy-mock/      # Part 1: Mock Legacy TRMS
├── trms-ai-backend/       # Part 2: Spring AI Backend
└── trms-frontend/         # Part 3: React Frontend
```

---

# Part 1: Backend - Legacy TRMS Mock Service

## Overview
Simple Spring Boot application that simulates a legacy TRMS system with REST endpoints and static mock data.

## Tasks

### 1.1 Project Setup (2 hours)
```bash
# Create Spring Boot project
spring init --dependencies=web,lombok --name=trms-legacy-mock
```

**Deliverables:**
- Basic Spring Boot application
- Port: 8090
- Health check endpoint: `/actuator/health`

### 1.2 Data Models (2 hours)
Create POJOs for core entities:

```java
// Account.java
@Data
@Builder
public class Account {
    private String accountId;
    private String accountName;
    private String currency;
    private String accountType;
    private String status;
}

// AccountBalance.java
@Data
@Builder
public class AccountBalance {
    private String accountId;
    private BigDecimal availableBalance;
    private BigDecimal currentBalance;
    private String currency;
    private LocalDateTime lastUpdated;
}

// Transaction.java
@Data
@Builder
public class Transaction {
    private String transactionId;
    private String fromAccount;
    private String toAccount;
    private BigDecimal amount;
    private String currency;
    private String status;
    private LocalDateTime createdAt;
}

// Report.java
@Data
@Builder
public class Report {
    private String reportId;
    private String title;
    private String type;
    private String content;
    private LocalDate createdDate;
}

// Additional models for EOD processing
// MarketDataStatus.java
@Data
@Builder
public class MarketDataStatus {
    private String feedType;
    private int expected;
    private int received;
    private List<String> missing;
    private boolean complete;
}

// TransactionStatus.java
@Data
@Builder
public class TransactionStatusSummary {
    private int total;
    private Map<String, Integer> statusCounts;
    private List<Transaction> pendingTransactions;
}

// RateReset.java
@Data
@Builder
public class RateReset {
    private String instrumentId;
    private String indexName;
    private LocalDate fixingDate;
    private BigDecimal notional;
    private String currency;
    private BigDecimal proposedRate;
}

// EODCheckResult.java
@Data
@Builder
public class EODCheckResult {
    private boolean ready;
    private List<MarketDataStatus> marketDataStatus;
    private TransactionStatusSummary transactionStatus;
    private List<RateReset> missingResets;
    private List<String> requiredActions;
}
```

### 1.3 Mock Data Service (3 hours)
Create static mock data:

```java
@Service
public class MockDataService {
    private static final List<Account> ACCOUNTS = Arrays.asList(
        Account.builder()
            .accountId("ACC-001-USD")
            .accountName("Trading Account USD")
            .currency("USD")
            .accountType("TRADING")
            .status("ACTIVE")
            .build(),
        // ... more accounts
    );
    
    private static final Map<String, AccountBalance> BALANCES = Map.of(
        "ACC-001-USD", AccountBalance.builder()
            .accountId("ACC-001-USD")
            .availableBalance(new BigDecimal("1234567.89"))
            .currentBalance(new BigDecimal("1250000.00"))
            .currency("USD")
            .lastUpdated(LocalDateTime.now())
            .build()
        // ... more balances
    );
}
```

### 1.4 REST Controllers (4 hours)
Implement mock endpoints:

```java
@RestController
@RequestMapping("/api/v1")
public class TrmsController {
    
    // GET /api/v1/accounts?currency=USD
    @GetMapping("/accounts")
    public List<Account> getAccounts(@RequestParam(required = false) String currency) {
        // Return filtered mock data
    }
    
    // GET /api/v1/accounts/{accountId}/balance
    @GetMapping("/accounts/{accountId}/balance")
    public AccountBalance getBalance(@PathVariable String accountId) {
        // Return mock balance
    }
    
    // POST /api/v1/transactions
    @PostMapping("/transactions")
    public Transaction createTransaction(@RequestBody TransactionRequest request) {
        // Return mock transaction with generated ID
    }
    
    // GET /api/v1/reports/{reportId}
    @GetMapping("/reports/{reportId}")
    public Report getReport(@PathVariable String reportId) {
        // Return mock report
    }
    
    // NEW: EOD specific endpoints
    // GET /api/v1/eod/market-data-status
    @GetMapping("/eod/market-data-status")
    public List<MarketDataStatus> getMarketDataStatus() {
        return Arrays.asList(
            MarketDataStatus.builder()
                .feedType("FX_RATES")
                .expected(284)
                .received(284)
                .complete(true)
                .build(),
            MarketDataStatus.builder()
                .feedType("EQUITY_PRICES")
                .expected(205)
                .received(197)
                .missing(Arrays.asList("AAPL", "GOOGL", "MSFT", "AMZN", "TSLA", "NVDA", "META", "BRK.B"))
                .complete(false)
                .build()
        );
    }
    
    // GET /api/v1/eod/transaction-status
    @GetMapping("/eod/transaction-status")
    public TransactionStatusSummary getTransactionStatus() {
        return TransactionStatusSummary.builder()
            .total(1247)
            .statusCounts(Map.of(
                "VALIDATED", 1189,
                "NEW", 23,
                "PROPOSAL", 35
            ))
            .pendingTransactions(generatePendingTransactions())
            .build();
    }
    
    // GET /api/v1/eod/missing-resets
    @GetMapping("/eod/missing-resets")
    public List<RateReset> getMissingResets() {
        return Arrays.asList(
            RateReset.builder()
                .instrumentId("SWAP-2024-0156")
                .indexName("USD-LIBOR-3M")
                .fixingDate(LocalDate.now())
                .notional(new BigDecimal("15000000"))
                .currency("USD")
                .build()
            // ... more missing resets
        );
    }
    
    // POST /api/v1/eod/propose-fixings
    @PostMapping("/eod/propose-fixings")
    public List<RateReset> proposeFixings(@RequestBody List<String> instrumentIds) {
        // Return proposed rates
    }
    
    // POST /api/v1/eod/run
    @PostMapping("/eod/run")
    public EODResult runEOD(@RequestBody EODRequest request) {
        // Simulate EOD execution
    }
}
```

### 1.5 Documentation & Testing (2 hours)
- Add Swagger/OpenAPI documentation
- Create Postman collection
- Write README with example requests

**Total Time: 13 hours**

---

# Part 2: Backend - Spring AI Application

## Overview
Spring Boot application with Spring AI that connects to the mock legacy system and provides AI-powered chat interface.

## Tasks

### 2.1 Project Setup (2 hours)
```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-openai-spring-boot-starter</artifactId>
        <version>0.8.0</version>
    </dependency>
    <!-- For Ollama support -->
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-ollama-spring-boot-starter</artifactId>
        <version>0.8.0</version>
    </dependency>
    <!-- For mocking AI responses during development -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
    </dependency>
</dependencies>
```

Configuration:
```yaml
server:
  port: 8080
  
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY:mock-key-for-development}
      chat:
        options:
          model: gpt-3.5-turbo
          temperature: 0.7
    ollama:
      base-url: ${OLLAMA_BASE_URL:http://localhost:11434}
      chat:
        options:
          model: llama3
          temperature: 0.7

legacy-trms:
  base-url: http://localhost:8090/api/v1
  
app:
  mock-ai: true  # Enable AI mocking for development
  ai-provider: mock  # mock, openai, or ollama
```

### 2.2 Legacy TRMS Client (3 hours)
Create REST client for legacy system:

```java
@Component
public class LegacyTrmsClient {
    private final RestTemplate restTemplate;
    private final String baseUrl;
    
    public List<Account> getAccountsByCurrency(String currency) {
        return restTemplate.exchange(
            baseUrl + "/accounts?currency=" + currency,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<Account>>() {}
        ).getBody();
    }
    
    public AccountBalance getBalance(String accountId) {
        return restTemplate.getForObject(
            baseUrl + "/accounts/" + accountId + "/balance",
            AccountBalance.class
        );
    }
    
    public Transaction createTransaction(TransactionRequest request) {
        return restTemplate.postForObject(
            baseUrl + "/transactions",
            request,
            Transaction.class
        );
    }
}
```

### 2.3 Spring AI Functions Implementation (4 hours)
Create Spring AI functions using the @Function annotation:

```java
@Component
@Slf4j
public class TrmsFunctions {
    
    @Autowired
    private LegacyTrmsClient trmsClient;
    
    @Function(name = "getAccountsByCurrency", 
              description = "Get accounts filtered by currency code")
    public List<Account> getAccountsByCurrency(String currency) {
        log.info("Function called: getAccountsByCurrency({})", currency);
        
        try {
            return trmsClient.getAccountsByCurrency(currency.toUpperCase());
        } catch (Exception e) {
            log.error("Error fetching accounts", e);
            throw new RuntimeException("Failed to fetch accounts: " + e.getMessage());
        }
    }
    
    @Function(name = "checkAccountBalance",
              description = "Check the balance of a specific account")
    public AccountBalance checkBalance(String accountId) {
        log.info("Function called: checkBalance({})", accountId);
        
        try {
            AccountBalance balance = trmsClient.getBalance(accountId);
            if (balance == null) {
                throw new IllegalArgumentException("Account not found: " + accountId);
            }
            return balance;
        } catch (Exception e) {
            log.error("Error fetching balance", e);
            throw new RuntimeException("Failed to fetch balance: " + e.getMessage());
        }
    }
    
    @Function(name = "bookTransaction",
              description = "Book a new transaction between accounts")
    public Transaction bookTransaction(
            String fromAccount,
            String toAccount,
            BigDecimal amount,
            String currency,
            String description) {
        
        log.info("Function called: bookTransaction({}, {}, {}, {})", 
            fromAccount, toAccount, amount, currency);
        
        try {
            return trmsClient.createTransaction(
                TransactionRequest.builder()
                    .fromAccount(fromAccount)
                    .toAccount(toAccount)
                    .amount(amount)
                    .currency(currency)
                    .description(description)
                    .build()
            );
        } catch (Exception e) {
            log.error("Error booking transaction", e);
            throw new RuntimeException("Failed to book transaction: " + e.getMessage());
        }
    }
    
    @Function(name = "checkEODReadiness",
              description = "Check if system is ready for End-of-Day processing")
    public EODCheckResult checkEODReadiness() {
        log.info("Function called: checkEODReadiness()");
        
        try {
            // Step 1: Check market data
            List<MarketDataStatus> marketData = trmsClient.getMarketDataStatus();
            
            // Step 2: Check transactions
            TransactionStatusSummary txnStatus = trmsClient.getTransactionStatus();
            
            // Step 3: Check missing resets
            List<RateReset> missingResets = trmsClient.getMissingResets();
            
            // Build comprehensive result
            boolean isReady = marketData.stream().allMatch(MarketDataStatus::isComplete)
                && txnStatus.getStatusCounts().getOrDefault("NEW", 0) == 0
                && txnStatus.getStatusCounts().getOrDefault("PROPOSAL", 0) == 0
                && missingResets.isEmpty();
            
            return EODCheckResult.builder()
                .ready(isReady)
                .marketDataStatus(marketData)
                .transactionStatus(txnStatus)
                .missingResets(missingResets)
                .requiredActions(buildRequiredActions(marketData, txnStatus, missingResets))
                .build();
        } catch (Exception e) {
            log.error("Error checking EOD readiness", e);
            throw new RuntimeException("Failed to check EOD readiness: " + e.getMessage());
        }
    }
    
    @Function(name = "proposeRateFixings",
              description = "Propose fixing rates for instruments with missing resets")
    public List<RateReset> proposeRateFixings(String instruments) {
        log.info("Function called: proposeRateFixings({})", instruments);
        
        try {
            List<RateReset> missingResets = trmsClient.getMissingResets();
            
            if ("ALL".equalsIgnoreCase(instruments)) {
                return trmsClient.proposeFixings(
                    missingResets.stream()
                        .map(RateReset::getInstrumentId)
                        .collect(Collectors.toList())
                );
            } else {
                List<String> instrumentIds = Arrays.asList(instruments.split(","));
                return trmsClient.proposeFixings(instrumentIds);
            }
        } catch (Exception e) {
            log.error("Error proposing fixings", e);
            throw new RuntimeException("Failed to propose fixings: " + e.getMessage());
        }
    }
    
    private List<String> buildRequiredActions(
            List<MarketDataStatus> marketData,
            TransactionStatusSummary txnStatus, 
            List<RateReset> missingResets) {
        
        List<String> actions = new ArrayList<>();
        
        // Check market data
        marketData.stream()
            .filter(md -> !md.isComplete())
            .forEach(md -> actions.add(
                String.format("Obtain missing %s data: %s", 
                    md.getFeedType(), String.join(", ", md.getMissing()))
            ));
        
        // Check transactions
        if (txnStatus.getStatusCounts().getOrDefault("NEW", 0) > 0) {
            actions.add("Validate " + txnStatus.getStatusCounts().get("NEW") + " new transactions");
        }
        if (txnStatus.getStatusCounts().getOrDefault("PROPOSAL", 0) > 0) {
            actions.add("Review " + txnStatus.getStatusCounts().get("PROPOSAL") + " proposal transactions");
        }
        
        // Check resets
        if (!missingResets.isEmpty()) {
            Map<String, Long> byIndex = missingResets.stream()
                .collect(Collectors.groupingBy(RateReset::getIndexName, Collectors.counting()));
            byIndex.forEach((index, count) -> 
                actions.add("Import " + count + " missing " + index + " fixings"));
        }
        
        return actions;
    }
}
```

### 2.4 Mock AI Service (3 hours)
Create mock AI service for development:

```java
@Service
@ConditionalOnProperty(name = "app.mock-ai", havingValue = "true")
@Slf4j
public class MockAiService implements AiService {
    
    @Autowired
    private TrmsFunctions functions;
    
    @Override
    public String processMessage(String message) {
        log.info("Mock AI processing: {}", message);
        
        // Simple pattern matching for demo
        String lowerMessage = message.toLowerCase();
        
        if (lowerMessage.contains("usd") && lowerMessage.contains("account")) {
            FunctionResponse response = functions.getAccountsByCurrency("USD");
            return formatAccountsResponse(response);
        }
        
        if (lowerMessage.contains("balance") && lowerMessage.contains("acc-")) {
            String accountId = extractAccountId(message);
            FunctionResponse response = functions.checkBalance(accountId);
            return formatBalanceResponse(response);
        }
        
        if (lowerMessage.contains("transfer") || lowerMessage.contains("transaction")) {
            return "To book a transaction, please provide: source account, target account, amount, and currency.";
        }
        
        if (lowerMessage.contains("eod") || lowerMessage.contains("end of day") || lowerMessage.contains("end-of-day")) {
            FunctionResponse response = functions.checkEODReadiness();
            return formatEODResponse(response);
        }
        
        if (lowerMessage.contains("propose") && lowerMessage.contains("fixing")) {
            FunctionResponse response = functions.proposeRateFixings("ALL");
            return formatProposedFixingsResponse(response);
        }
        
        return "I can help you with accounts, balances, transactions, and end-of-day processing. What would you like to know?";
    }
    
    private String formatEODResponse(FunctionResponse response) {
        if (!response.isSuccess()) {
            return "Sorry, I couldn't check EOD readiness: " + response.getError();
        }
        
        EODCheckResult result = (EODCheckResult) response.getData();
        StringBuilder sb = new StringBuilder();
        
        sb.append("End-of-Day Readiness Check:\n\n");
        
        // Market Data Status
        sb.append("📊 Step 1: Market Data Status\n");
        for (MarketDataStatus mds : result.getMarketDataStatus()) {
            String icon = mds.isComplete() ? "✅" : "⚠️";
            sb.append(icon).append(" ").append(mds.getFeedType())
              .append(": ").append(mds.getReceived()).append("/").append(mds.getExpected());
            
            if (!mds.isComplete()) {
                sb.append("\n   Missing: ").append(String.join(", ", mds.getMissing()));
            }
            sb.append("\n");
        }
        
        // Transaction Status
        sb.append("\n📝 Step 2: Transaction Status Check\n");
        TransactionStatusSummary txnStatus = result.getTransactionStatus();
        sb.append("Total Transactions: ").append(txnStatus.getTotal()).append("\n");
        
        txnStatus.getStatusCounts().forEach((status, count) -> {
            String icon = "VALIDATED".equals(status) ? "✅" : "❌";
            sb.append(icon).append(" ").append(status).append(": ").append(count).append("\n");
        });
        
        // Missing Resets
        if (!result.getMissingResets().isEmpty()) {
            sb.append("\n🔄 Step 3: Floating Rate Resets Check\n");
            sb.append("❌ Missing Resets Found: ").append(result.getMissingResets().size()).append("\n");
            
            Map<String, List<RateReset>> byIndex = result.getMissingResets().stream()
                .collect(Collectors.groupingBy(RateReset::getIndexName));
            
            byIndex.forEach((index, resets) -> {
                BigDecimal totalNotional = resets.stream()
                    .map(RateReset::getNotional)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                sb.append("• ").append(index).append(": ")
                  .append(resets.size()).append(" instruments, ")
                  .append("Total Notional: ").append(formatCurrency(totalNotional, resets.get(0).getCurrency()))
                  .append("\n");
            });
        }
        
        // Summary
        sb.append("\n📋 Summary & Recommendations:\n");
        sb.append("EOD Status: ").append(result.isReady() ? "✅ READY" : "❌ NOT READY").append("\n");
        
        if (!result.getRequiredActions().isEmpty()) {
            sb.append("\nRequired Actions:\n");
            for (int i = 0; i < result.getRequiredActions().size(); i++) {
                sb.append((i + 1)).append(". ").append(result.getRequiredActions().get(i)).append("\n");
            }
            
            sb.append("\nWould you like me to:\n");
            sb.append("A) Generate validation report for new transactions?\n");
            sb.append("B) Auto-propose missing rate fixings?\n");
            sb.append("C) Override with supervisor approval?\n");
            sb.append("D) Schedule EOD for later?\n");
        }
        
        return sb.toString();
    }
    
    private String formatProposedFixingsResponse(FunctionResponse response) {
        if (!response.isSuccess()) {
            return "Sorry, I couldn't propose fixings: " + response.getError();
        }
        
        List<RateReset> proposed = (List<RateReset>) response.getData();
        StringBuilder sb = new StringBuilder("Generating Proposed Rate Fixings:\n\n");
        
        Map<String, List<RateReset>> byIndex = proposed.stream()
            .collect(Collectors.groupingBy(RateReset::getIndexName));
        
        byIndex.forEach((index, resets) -> {
            sb.append("📊 ").append(index).append(":\n");
            sb.append("• Proposed Rate: ").append(resets.get(0).getProposedRate()).append("%\n");
            sb.append("• Instruments affected: ").append(resets.size()).append("\n");
            sb.append("• Total Notional: ").append(
                formatCurrency(
                    resets.stream().map(RateReset::getNotional).reduce(BigDecimal.ZERO, BigDecimal::add),
                    resets.get(0).getCurrency()
                )
            ).append("\n\n");
        });
        
        sb.append("⚠️ Important: These are proposed rates requiring approval.\n\n");
        sb.append("Would you like to:\n");
        sb.append("1. Send approval request to managers?\n");
        sb.append("2. View detailed impact report?\n");
        sb.append("3. Manually adjust rates?\n");
        sb.append("4. Cancel and wait for official fixings?\n");
        
        return sb.toString();
    }
    
    private String formatCurrency(BigDecimal amount, String currency) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance();
        if ("USD".equals(currency)) {
            formatter.setCurrency(Currency.getInstance("USD"));
        } else if ("EUR".equals(currency)) {
            formatter.setCurrency(Currency.getInstance("EUR"));
        }
        return formatter.format(amount);
    }
    
    private String formatAccountsResponse(FunctionResponse response) {
        if (!response.isSuccess()) {
            return "Sorry, I couldn't fetch the accounts: " + response.getError();
        }
        
        List<Account> accounts = (List<Account>) response.getData();
        StringBuilder sb = new StringBuilder("I found " + accounts.size() + " USD accounts:\n\n");
        
        for (Account account : accounts) {
            sb.append("• ").append(account.getAccountName())
              .append(" (").append(account.getAccountId()).append(")\n")
              .append("  Type: ").append(account.getAccountType()).append("\n")
              .append("  Status: ").append(account.getStatus()).append("\n\n");
        }
        
        return sb.toString();
    }
}
```

### 2.5 Chat REST API (3 hours)
Create chat endpoints:

```java
@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
@Slf4j
public class ChatController {
    
    @Autowired
    private AiService aiService;
    
    @PostMapping
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        log.info("Chat request: {}", request.getMessage());
        
        try {
            String response = aiService.processMessage(request.getMessage());
            
            return ResponseEntity.ok(
                ChatResponse.builder()
                    .message(response)
                    .timestamp(LocalDateTime.now())
                    .success(true)
                    .build()
            );
        } catch (Exception e) {
            log.error("Error processing chat", e);
            
            return ResponseEntity.ok(
                ChatResponse.builder()
                    .message("Sorry, I encountered an error. Please try again.")
                    .timestamp(LocalDateTime.now())
                    .success(false)
                    .build()
            );
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "mode", aiService instanceof MockAiService ? "MOCK" : "LIVE"
        ));
    }
}

@Data
@Builder
class ChatRequest {
    private String message;
}

@Data
@Builder
class ChatResponse {
    private String message;
    private LocalDateTime timestamp;
    private boolean success;
}
```

### 2.6 WebSocket Support (2 hours)
Add real-time chat support with Spring AI:

```java
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    
    @Autowired
    private ChatWebSocketHandler chatWebSocketHandler;
    
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatWebSocketHandler, "/ws/chat")
                .setAllowedOrigins("*");
    }
}

@Component
@Slf4j
public class ChatWebSocketHandler extends TextWebSocketHandler {
    
    @Autowired
    private TrmsAiService aiService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            ChatRequest request = objectMapper.readValue(message.getPayload(), ChatRequest.class);
            String userId = session.getId();
            
            log.info("WebSocket message from {}: {}", userId, request.getMessage());
            
            // Process with AI service
            String response = aiService.processMessage(userId, request.getMessage());
            
            ChatResponse chatResponse = ChatResponse.builder()
                .message(response)
                .timestamp(LocalDateTime.now())
                .success(true)
                .build();
            
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(chatResponse)));
            
        } catch (Exception e) {
            log.error("WebSocket error", e);
            
            ChatResponse errorResponse = ChatResponse.builder()
                .message("Error processing message")
                .timestamp(LocalDateTime.now())
                .success(false)
                .build();
            
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(errorResponse)));
        }
    }
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("WebSocket connection established: {}", session.getId());
        
        ChatResponse welcome = ChatResponse.builder()
            .message("Connected to TRMS AI Assistant. How can I help you today?")
            .timestamp(LocalDateTime.now())
            .success(true)
            .build();
        
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(welcome)));
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("WebSocket connection closed: {} - {}", session.getId(), status);
    }
}
```

### 2.7 Testing & Documentation (2 hours)
- Unit tests for Spring AI functions
- Integration tests for REST endpoints
- API documentation with Spring AI specifics
- Docker configuration

```java
// Test for Spring AI Functions
@SpringBootTest
@AutoConfigureMockMvc
class TrmsFunctionsTest {
    
    @MockBean
    private LegacyTrmsClient trmsClient;
    
    @Autowired
    private TrmsFunctions functions;
    
    @Test
    void testGetAccountsByCurrency() {
        // Given
        List<Account> mockAccounts = Arrays.asList(
            Account.builder()
                .accountId("ACC-001-USD")
                .accountName("Trading Account")
                .currency("USD")
                .build()
        );
        
        when(trmsClient.getAccountsByCurrency("USD"))
            .thenReturn(mockAccounts);
        
        // When
        List<Account> result = functions.getAccountsByCurrency("USD");
        
        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAccountId()).isEqualTo("ACC-001-USD");
    }
    
    @Test
    void testEODReadiness() {
        // Given
        when(trmsClient.getMarketDataStatus()).thenReturn(
            Arrays.asList(
                MarketDataStatus.builder()
                    .feedType("FX_RATES")
                    .expected(100)
                    .received(100)
                    .complete(true)
                    .build()
            )
        );
        
        when(trmsClient.getTransactionStatus()).thenReturn(
            TransactionStatusSummary.builder()
                .total(100)
                .statusCounts(Map.of("VALIDATED", 100))
                .build()
        );
        
        when(trmsClient.getMissingResets()).thenReturn(new ArrayList<>());
        
        // When
        EODCheckResult result = functions.checkEODReadiness();
        
        // Then
        assertThat(result.isReady()).isTrue();
        assertThat(result.getRequiredActions()).isEmpty();
    }
}

// Integration test for Chat Controller
@SpringBootTest
@AutoConfigureMockMvc
class ChatControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testChatEndpoint() throws Exception {
        ChatRequest request = ChatRequest.builder()
            .message("Show me USD accounts")
            .build();
        
        mockMvc.perform(post("/api/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(request))
                .header("X-User-Id", "test-user"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").exists());
    }
}
```

**Docker Setup:**
```dockerfile
# Dockerfile
FROM openjdk:17-jdk-slim
COPY target/trms-ai-backend-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

**Total Time: 19 hours**

---

# Part 3: Frontend - React Application

## Overview
Simple React application with Google-like initial page that transforms into a chat interface.

## Tasks

### 3.1 Project Setup (1 hour)
```bash
npx create-react-app trms-frontend --template typescript
cd trms-frontend
npm install axios framer-motion react-markdown
```

### 3.2 Main Layout Component (3 hours)
Create transforming layout:

```tsx
// App.tsx
import React, { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import SearchView from './components/SearchView';
import ChatView from './components/ChatView';
import './App.css';

function App() {
  const [view, setView] = useState<'search' | 'chat'>('search');
  const [messages, setMessages] = useState<Message[]>([]);

  const handleFirstMessage = (message: string) => {
    setMessages([{ id: '1', text: message, sender: 'user', timestamp: new Date() }]);
    setView('chat');
    // Send message to backend
  };

  return (
    <div className="App">
      <AnimatePresence mode="wait">
        {view === 'search' ? (
          <SearchView key="search" onSubmit={handleFirstMessage} />
        ) : (
          <ChatView key="chat" messages={messages} onNewMessage={handleNewMessage} />
        )}
      </AnimatePresence>
    </div>
  );
}
```

### 3.3 Search View Component (3 hours)
Google-like search interface:

```tsx
// components/SearchView.tsx
import React, { useState } from 'react';
import { motion } from 'framer-motion';
import './SearchView.css';

interface SearchViewProps {
  onSubmit: (message: string) => void;
}

const SearchView: React.FC<SearchViewProps> = ({ onSubmit }) => {
  const [input, setInput] = useState('');

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (input.trim()) {
      onSubmit(input);
    }
  };

  return (
    <motion.div 
      className="search-container"
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      exit={{ opacity: 0, y: -100 }}
    >
      <div className="search-content">
        <h1 className="search-title">TRMS AI Assistant</h1>
        <form onSubmit={handleSubmit} className="search-form">
          <motion.input
            type="text"
            value={input}
            onChange={(e) => setInput(e.target.value)}
            placeholder="Ask about accounts, balances, transactions..."
            className="search-input"
            whileFocus={{ scale: 1.02 }}
            autoFocus
          />
          <motion.button
            type="submit"
            className="search-button"
            whileHover={{ scale: 1.05 }}
            whileTap={{ scale: 0.95 }}
          >
            Search
          </motion.button>
        </form>
        <div className="search-suggestions">
          <span>Try: "Show me all USD accounts"</span>
          <span>or "Check balance for ACC-001-USD"</span>
        </div>
      </div>
    </motion.div>
  );
};
```

### 3.4 Chat View Component (4 hours)
Chat interface:

```tsx
// components/ChatView.tsx
import React, { useState, useRef, useEffect } from 'react';
import { motion } from 'framer-motion';
import ReactMarkdown from 'react-markdown';
import MessageBubble from './MessageBubble';
import './ChatView.css';

interface ChatViewProps {
  messages: Message[];
  onNewMessage: (message: string) => void;
}

const ChatView: React.FC<ChatViewProps> = ({ messages, onNewMessage }) => {
  const [input, setInput] = useState('');
  const messagesEndRef = useRef<HTMLDivElement>(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (input.trim()) {
      onNewMessage(input);
      setInput('');
    }
  };

  return (
    <motion.div 
      className="chat-container"
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
    >
      <div className="chat-header">
        <h2>TRMS AI Assistant</h2>
      </div>
      
      <div className="messages-container">
        {messages.map((message) => (
          <MessageBubble key={message.id} message={message} />
        ))}
        <div ref={messagesEndRef} />
      </div>
      
      <form onSubmit={handleSubmit} className="chat-input-form">
        <input
          type="text"
          value={input}
          onChange={(e) => setInput(e.target.value)}
          placeholder="Type your message..."
          className="chat-input"
        />
        <button type="submit" className="send-button">
          Send
        </button>
      </form>
    </motion.div>
  );
};
```

### 3.5 API Service (2 hours)
Backend communication:

```typescript
// services/api.ts
import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';

export interface ChatMessage {
  message: string;
}

export interface ChatResponse {
  message: string;
  timestamp: string;
  success: boolean;
}

class ApiService {
  async sendMessage(message: string): Promise<ChatResponse> {
    try {
      const response = await axios.post<ChatResponse>(
        `${API_BASE_URL}/api/chat`,
        { message }
      );
      return response.data;
    } catch (error) {
      console.error('API Error:', error);
      throw error;
    }
  }

  async checkHealth(): Promise<boolean> {
    try {
      const response = await axios.get(`${API_BASE_URL}/api/chat/health`);
      return response.data.status === 'UP';
    } catch {
      return false;
    }
  }
}

export default new ApiService();
```

### 3.6 Styling (3 hours)
Create clean, minimal styling:

```css
/* App.css */
.App {
  min-height: 100vh;
  background-color: #f5f5f5;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

/* SearchView.css */
.search-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
}

.search-content {
  text-align: center;
  max-width: 600px;
  width: 100%;
  padding: 20px;
}

.search-title {
  font-size: 48px;
  margin-bottom: 40px;
  color: #333;
  font-weight: 300;
}

.search-input {
  width: 100%;
  padding: 16px 24px;
  font-size: 18px;
  border: 1px solid #ddd;
  border-radius: 24px;
  outline: none;
  transition: box-shadow 0.3s;
}

.search-input:focus {
  box-shadow: 0 1px 6px rgba(32, 33, 36, 0.28);
}

/* ChatView.css */
.chat-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  max-width: 800px;
  margin: 0 auto;
  background: white;
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.message-bubble {
  max-width: 70%;
  margin: 10px 0;
  padding: 12px 18px;
  border-radius: 18px;
  word-wrap: break-word;
}

.user-message {
  background-color: #007bff;
  color: white;
  margin-left: auto;
  text-align: right;
}

.ai-message {
  background-color: #f1f3f4;
  color: #333;
}
```

### 3.7 Testing & Deployment (2 hours)
- Unit tests for components
- Integration tests
- Build configuration
- Docker setup

```dockerfile
# Dockerfile
FROM node:18-alpine as build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/build /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
```

**Total Time: 18 hours**

---

## Integration & Testing Plan

### Integration Tasks (4 hours)
1. Docker Compose setup for all services
2. End-to-end testing
3. Performance testing
4. Documentation

### Docker Compose Configuration
```yaml
version: '3.8'
services:
  legacy-trms:
    build: ./trms-legacy-mock
    ports:
      - "8090:8090"
      
  ai-backend:
    build: ./trms-ai-backend
    ports:
      - "8080:8080"
    environment:
      - LEGACY_TRMS_URL=http://legacy-trms:8090
      - APP_MOCK_AI=true
    depends_on:
      - legacy-trms
      
  frontend:
    build: ./trms-frontend
    ports:
      - "3000:80"
    environment:
      - REACT_APP_API_URL=http://localhost:8080
    depends_on:
      - ai-backend
```

---

## Summary

### Total Development Time
- Part 1 (Legacy Mock): 13 hours
- Part 2 (AI Backend): 19 hours  
- Part 3 (Frontend): 18 hours
- Integration: 4 hours
- **Total: 54 hours**

### Team Distribution
- **Team A**: Legacy Mock (1 developer, 2 days)
- **Team B**: AI Backend (1-2 developers, 3 days)
- **Team C**: Frontend (1 developer, 2-3 days)

### Key Deliverables
1. Working mock legacy TRMS with REST API
2. Spring AI backend with mock AI responses
3. React frontend with animated UI
4. Docker Compose for easy deployment
5. Complete documentation

### Success Criteria
- All components can run independently
- Mock AI can handle basic queries
- Frontend smoothly transitions from search to chat
- Easy to replace mock AI with real OpenAI/Ollama
- Complete POC can run with single docker-compose command