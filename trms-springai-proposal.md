# TRMS AI Agent Integration with Spring AI - Proof of Concept Proposal

## Executive Summary

This document outlines a proof of concept (POC) for integrating AI agents into a legacy Treasury and Risk Management System (TRMS) using Spring AI and Ollama. Spring AI provides a native Spring Boot integration for AI capabilities, enabling natural language interactions with the TRMS through a clean, Spring-idiomatic approach.

## Why Spring AI?

Spring AI is the natural choice for Spring Boot applications:
- **Native Spring Integration** - First-class Spring Boot support with auto-configuration
- **Familiar Patterns** - Uses standard Spring annotations and programming model
- **Production Ready** - Built-in observability, security, and transaction support
- **Excellent Ollama Support** - Official integration with streaming capabilities
- **Simple Function Calling** - Clean `@Function` annotation-based approach

## Solution Architecture

### High-Level Architecture

```
┌─────────────────┐     ┌──────────────────┐     ┌─────────────────┐
│                 │     │                  │     │                 │
│   Web UI        │────▶│  Spring Boot     │────▶│  Legacy TRMS    │
│   (React/HTML)  │     │  + Spring AI     │     │  System         │
│                 │     │                  │     │                 │
└─────────────────┘     └────────┬─────────┘     └─────────────────┘
                                 │
                                 │ Spring AI
                                 │
                        ┌────────▼─────────┐
                        │                  │
                        │     Ollama       │
                        │   (LLM Engine)   │
                        │                  │
                        └──────────────────┘
```

### Component Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                     Spring Boot Application                      │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌─────────────────┐  ┌─────────────────┐  ┌────────────────┐  │
│  │   REST API      │  │   Spring AI     │  │ TRMS Functions │  │
│  │   Controller    │──│   ChatClient    │──│  (@Function)   │  │
│  └─────────────────┘  └─────────────────┘  └────────┬───────┘  │
│                                                      │          │
│  ┌─────────────────┐  ┌─────────────────┐  ┌────────▼───────┐  │
│  │ Conversation    │  │   Spring AI     │  │  Legacy TRMS   │  │
│  │ Memory          │──│ Auto-Config     │  │  Service       │  │
│  └─────────────────┘  └─────────────────┘  └────────────────┘  │
│                                                                  │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │            Spring Boot Actuator & Micrometer             │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### Technology Stack

| Component | Technology | Purpose |
|-----------|------------|---------|
| Backend Framework | Spring Boot 3.2+ | Microservice foundation |
| AI Framework | Spring AI 0.8.0 | AI/LLM integration |
| LLM Engine | Ollama (Llama 3) | Natural language processing |
| API Layer | REST + WebSocket | Client communication |
| Memory Store | Spring Cache / Redis | Conversation context |
| Observability | Actuator + Micrometer | Monitoring & metrics |
| Frontend | HTML/JS or React | User interface |
| Build Tool | Maven | Dependency management |

## Implementation Code Structure

### 1. Maven Dependencies

```xml
<dependencies>
    <!-- Spring Boot -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- Spring AI -->
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-ollama-spring-boot-starter</artifactId>
        <version>0.8.0</version>
    </dependency>
    
    <!-- Spring Boot Actuator -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
    
    <!-- WebSocket Support -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-websocket</artifactId>
    </dependency>
    
    <!-- Cache -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-cache</artifactId>
    </dependency>
    
    <!-- Utilities -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
    </dependency>
</dependencies>
```

### 2. Application Configuration

```yaml
spring:
  application:
    name: trms-ai-agent
    
  ai:
    ollama:
      base-url: http://localhost:11434
      chat:
        options:
          model: llama3
          temperature: 0.7
          
  cache:
    type: simple # or redis for production
    
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,ai
  metrics:
    tags:
      application: ${spring.application.name}

legacy-trms:
  base-url: ${TRMS_BASE_URL:http://localhost:8090}
  api-key: ${TRMS_API_KEY}
  timeout: 30s

logging:
  level:
    org.springframework.ai: DEBUG
```

### 3. Core Implementation

```java
// Main Configuration
@Configuration
@EnableCaching
public class AiConfiguration {
    
    @Bean
    public FunctionCallbackWrapper<List<Account>> accountsByCurrentFunction(
            TrmsFunctions trmsFunctions) {
        return FunctionCallbackWrapper.builder(trmsFunctions)
            .withName("getAccountsByCurrency")
            .withDescription("Get accounts filtered by currency code")
            .build();
    }
    
    // Register all functions...
}

// TRMS Functions
@Component
@Slf4j
public class TrmsFunctions {
    
    @Autowired
    private LegacyTrmsService trmsService;
    
    @Function("Get a list of accounts filtered by currency code")
    public List<Account> getAccountsByCurrency(
        @Param("3-letter currency code like USD, EUR, GBP") String currency) {
        log.info("Fetching accounts for currency: {}", currency);
        return trmsService.findAccountsByCurrency(currency.toUpperCase());
    }
    
    @Function("Check the current balance of a specific account")
    @Cacheable("balances")
    public AccountBalance checkAccountBalance(
        @Param("Account identifier") String accountId) {
        log.info("Checking balance for account: {}", accountId);
        return trmsService.getAccountBalance(accountId);
    }
    
    @Function("Book a new transaction between accounts")
    @Transactional
    public TransactionResult bookTransaction(
        @Param("Source account ID") String fromAccount,
        @Param("Target account ID") String toAccount,
        @Param("Transaction amount") BigDecimal amount,
        @Param("Currency code") String currency,
        @Param("Transaction description") String description) {
        
        log.info("Booking transaction: {} {} from {} to {}", 
                amount, currency, fromAccount, toAccount);
        
        Transaction txn = trmsService.createTransaction(
            TransactionRequest.builder()
                .fromAccount(fromAccount)
                .toAccount(toAccount)
                .amount(amount)
                .currency(currency)
                .description(description)
                .build()
        );
        
        return TransactionResult.of(txn);
    }
}

// AI Service
@Service
@Slf4j
public class TrmsAiService {
    
    private final ChatClient chatClient;
    
    public TrmsAiService(ChatClient.Builder chatClientBuilder,
                        List<FunctionCallback> functions) {
        this.chatClient = chatClientBuilder
            .defaultSystem("""
                You are an AI assistant for a Treasury and Risk Management System.
                Help users with account queries, balance checks, transactions, and reports.
                Always format monetary values clearly with currency symbols.
                Be precise with financial data and confirm important operations.
                """)
            .defaultFunctions(functions.toArray(new FunctionCallback[0]))
            .build();
    }
    
    public String processUserRequest(String userId, String message) {
        return chatClient.prompt()
            .user(message)
            .call()
            .content();
    }
    
    public Flux<String> streamResponse(String userId, String message) {
        return chatClient.prompt()
            .user(message)
            .stream()
            .content();
    }
}

// REST Controller
@RestController
@RequestMapping("/api/chat")
@CrossOrigin
@Slf4j
public class ChatController {
    
    @Autowired
    private TrmsAiService aiService;
    
    @PostMapping
    public ChatResponse chat(@RequestBody ChatRequest request,
                           @RequestHeader(value = "X-User-Id", defaultValue = "default") String userId) {
        try {
            String response = aiService.processUserRequest(userId, request.getMessage());
            return ChatResponse.success(response);
        } catch (Exception e) {
            log.error("Error processing request", e);
            return ChatResponse.error("Failed to process request");
        }
    }
    
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> streamChat(
            @RequestParam String message,
            @RequestHeader(value = "X-User-Id", defaultValue = "default") String userId) {
        
        return aiService.streamResponse(userId, message)
            .map(content -> ServerSentEvent.<String>builder()
                .data(content)
                .build())
            .concatWith(Flux.just(ServerSentEvent.<String>builder()
                .event("complete")
                .build()));
    }
}
```

## Business Use Cases Implementation

### Case 1: Account Query by Currency

**Spring AI Implementation:**
```java
@Function("Get accounts filtered by currency")
public List<Account> getAccountsByCurrency(
    @Param("Currency code (USD, EUR, GBP, etc)") String currency) {
    
    List<Account> accounts = trmsService.findAccountsByCurrency(currency);
    
    // Spring AI handles the response formatting automatically
    return accounts;
}
```

**User:** "Show me all USD accounts"

**AI Response:** 
```
I found 4 USD accounts in the system:

1. Trading Account USD (ACC-001-USD)
   - Type: Trading
   - Status: Active

2. Settlement Account USD (ACC-002-USD)
   - Type: Settlement
   - Status: Active

[... additional accounts ...]
```

### Case 2: Balance Check with Caching

**Spring AI Implementation:**
```java
@Function("Check account balance with automatic caching")
@Cacheable(value = "balances", key = "#accountId")
public AccountBalance checkAccountBalance(
    @Param("Account ID to check") String accountId) {
    
    AccountBalance balance = trmsService.getAccountBalance(accountId);
    if (balance == null) {
        throw new AccountNotFoundException(accountId);
    }
    return balance;
}
```

### Case 3: Transactional Operations

**Spring AI Implementation:**
```java
@Function("Book a transaction with automatic rollback on failure")
@Transactional(rollbackFor = Exception.class)
public TransactionResult bookTransaction(
    @Param("Source account") String from,
    @Param("Target account") String to,
    @Param("Amount") BigDecimal amount,
    @Param("Currency") String currency) {
    
    // Validate accounts
    validateAccounts(from, to);
    
    // Check balance
    if (!hasSufficientBalance(from, amount)) {
        throw new InsufficientFundsException();
    }
    
    // Create transaction
    return trmsService.createTransaction(from, to, amount, currency);
}
```

## Spring AI Specific Advantages

### 1. Native Observability

```yaml
# Automatic metrics available at /actuator/metrics
ai.chat.requests.total
ai.chat.requests.duration
ai.chat.tokens.used
ai.function.calls.total
ai.function.calls.duration
```

### 2. Security Integration

```java
@Function("Sensitive operation with Spring Security")
@PreAuthorize("hasRole('TRADER') and #amount < 1000000")
public TransactionResult bookLargeTransaction(
    String from, String to, BigDecimal amount) {
    // Automatically secured
}
```

### 3. Async Support

```java
@Async
@Function("Analyze report asynchronously")
public CompletableFuture<ReportAnalysis> analyzeReportAsync(
    @Param("Report ID") String reportId) {
    return CompletableFuture.supplyAsync(() -> 
        performAnalysis(reportId)
    );
}
```

### 4. Error Handling

```java
@ControllerAdvice
public class AiExceptionHandler {
    
    @ExceptionHandler(AIException.class)
    public ResponseEntity<ErrorResponse> handleAiException(AIException e) {
        // Centralized AI error handling
        return ResponseEntity.status(503)
            .body(new ErrorResponse("AI service temporarily unavailable"));
    }
}
```

## Production Considerations

### 1. Health Checks

```java
@Component
public class OllamaHealthIndicator implements HealthIndicator {
    
    @Autowired
    private OllamaApi ollamaApi;
    
    @Override
    public Health health() {
        try {
            ollamaApi.listModels();
            return Health.up()
                .withDetail("model", "llama3")
                .build();
        } catch (Exception e) {
            return Health.down()
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}
```

### 2. Rate Limiting

```java
@Component
@Aspect
public class RateLimitingAspect {
    
    @Around("@annotation(Function)")
    public Object rateLimit(ProceedingJoinPoint joinPoint) throws Throwable {
        // Implement rate limiting logic
        return joinPoint.proceed();
    }
}
```

### 3. Audit Logging

```java
@EventListener
public class AiAuditListener {
    
    @EventListener
    public void onFunctionCall(FunctionCallEvent event) {
        auditLog.record(
            event.getUserId(),
            event.getFunctionName(),
            event.getParameters(),
            event.getResult()
        );
    }
}
```

## Deployment Architecture

```yaml
version: '3.8'
services:
  ollama:
    image: ollama/ollama:latest
    ports:
      - "11434:11434"
    volumes:
      - ollama:/root/.ollama
    deploy:
      resources:
        reservations:
          devices:
            - driver: nvidia
              count: 1
              capabilities: [gpu]
              
  trms-ai:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_AI_OLLAMA_BASE_URL=http://ollama:11434
      - SPRING_PROFILES_ACTIVE=production
    depends_on:
      - ollama
      - redis
      
  redis:
    image: redis:alpine
    ports:
      - "6379:6379"
      
volumes:
  ollama:
```

## Migration from LangChain4j

If you've already started with LangChain4j, migration is straightforward:

| LangChain4j | Spring AI |
|-------------|-----------|
| `@Tool` | `@Function` |
| `@P` | `@Param` |
| `ChatLanguageModel` | `ChatClient` |
| `AiServices` | Direct Spring beans |
| Custom memory | Spring Cache |

## Conclusion

Spring AI provides a more natural, Spring-native approach for integrating AI capabilities into your TRMS. The framework offers:

- **Simpler implementation** with familiar Spring patterns
- **Better integration** with Spring ecosystem features
- **Production-ready** capabilities out of the box
- **Lower learning curve** for Spring developers
- **Native observability** and monitoring

For a Spring Boot-based TRMS POC, Spring AI is the optimal choice, providing all necessary features while maintaining the simplicity and elegance of Spring development.