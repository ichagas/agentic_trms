# TRMS AI Backend

A Spring AI-powered backend service that integrates AI capabilities with legacy Treasury and Risk Management Systems (TRMS). This service provides intelligent assistance for financial operations, account management, and end-of-day processing through natural language interactions.

## Architecture

This is part of a 3-component TRMS AI POC system:
- **React Frontend** (port 3000) - User interface for AI interactions
- **Spring AI Backend** (port 8080) - This service - AI orchestration and function calling
- **TRMS Mock Backend** (port 8090) - Simulates legacy TRMS system APIs

## Features

### AI-Powered Functions
The system provides AI-callable functions for TRMS operations:

1. **getAccountsByCurrency** - Retrieve accounts filtered by currency (USD, EUR, GBP, etc.)
2. **checkAccountBalance** - Get detailed balance information for specific accounts  
3. **bookTransaction** - Execute transactions between accounts with validation
4. **checkEODReadiness** - Verify End of Day processing readiness status
5. **proposeRateFixings** - Get missing interest rate resets for EOD processing

### AI Integration Patterns
- Spring AI 0.8.0 with OpenAI integration
- Function calling capabilities with automatic parameter validation
- Comprehensive system prompts for TRMS domain expertise
- Error handling and fallback mechanisms

### Legacy System Integration
- RestTemplate-based HTTP client for TRMS API communication
- Comprehensive error handling for network and API failures
- Retry logic and timeout configuration
- Health checks and monitoring

## Prerequisites

- **Java 21** - `JAVA_HOME=/opt/homebrew/opt/openjdk@21`
- **Maven 3.6+**
- **OpenAI API Key** - For AI functionality (or use demo mode)
- **TRMS Mock Backend** running on port 8090

## Quick Start

### 1. Environment Setup
```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@21
export OPENAI_API_KEY=your-openai-api-key  # Or use demo key
```

### 2. Start the Application
```bash
# Using the startup script
./start.sh

# Or manually
mvn spring-boot:run

# Or with specific environment variables
JAVA_HOME=/opt/homebrew/opt/openjdk@21 OPENAI_API_KEY=sk-your-key mvn spring-boot:run
```

### 3. Verify the Service
```bash
# Health check
curl http://localhost:8080/api/health

# Chat endpoint
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "Show me all USD accounts"}'
```

## Configuration

### Application Properties (`application.yml`)

```yaml
server:
  port: 8080

spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY:sk-demo-key}
      chat:
        options:
          model: gpt-4o-mini
          temperature: 0.3

trms:
  legacy:
    base-url: http://localhost:8090
    timeout: 30000
```

### CORS Configuration
Pre-configured for frontend integration:
- Allowed origins: `http://localhost:3000`, `http://127.0.0.1:3000`
- All standard HTTP methods supported
- Credentials enabled for session management

## API Endpoints

### Chat API
- **POST** `/api/chat` - Main AI chat interface
  ```json
  {
    "message": "Show me EUR accounts with balance > 10000",
    "sessionId": "optional-session-id"
  }
  ```

### Health Checks  
- **GET** `/api/health` - Application health with TRMS connectivity
- **GET** `/actuator/health` - Spring Boot actuator health

## Development

### Project Structure
```
src/main/java/com/trms/ai/
├── TrmsAiApplication.java          # Main application class
├── client/
│   └── LegacyTrmsClient.java       # TRMS system HTTP client
├── config/
│   ├── AiConfiguration.java       # Spring AI configuration
│   ├── TrmsProperties.java        # TRMS connection properties
│   └── WebConfig.java              # CORS and HTTP configuration
├── controller/
│   ├── ChatController.java        # Main chat API endpoint
│   ├── GlobalExceptionHandler.java # Error handling
│   └── HealthController.java      # Health check endpoints
├── dto/                            # Data transfer objects
│   ├── Account.java
│   ├── AccountBalance.java
│   ├── ChatRequest.java
│   ├── ChatResponse.java
│   ├── CreateTransactionRequest.java
│   ├── EODStatus.java
│   ├── RateReset.java
│   └── Transaction.java
└── service/
    └── TrmsFunctions.java          # AI-callable TRMS functions
```

### Building and Testing
```bash
# Compile
mvn compile

# Run tests
mvn test

# Package
mvn package

# Run with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Function Development
Functions are implemented with Spring AI `@Function` pattern:

```java
@Bean
@Description("Get accounts filtered by currency")
public Function<GetAccountsByCurrencyRequest, List<Account>> getAccountsByCurrency() {
    return request -> {
        // Implementation calls legacy TRMS client
        return legacyTrmsClient.getAccountsByCurrency(request.currency());
    };
}
```

## TRMS Mock Backend Integration

The service integrates with the following TRMS Mock APIs:

- `GET /api/v1/accounts?currency={currency}` - Account listings
- `GET /api/v1/accounts/{id}/balance` - Account balance details
- `POST /api/v1/transactions` - Transaction booking
- `GET /api/v1/eod/status` - End of Day readiness
- `GET /api/v1/eod/missing-resets` - Rate fixing proposals

## Deployment

### Local Development
1. Start TRMS Mock Backend on port 8090
2. Start this AI Backend on port 8080  
3. Start React Frontend on port 3000
4. Navigate to `http://localhost:3000` for the full experience

### Production Considerations
- Set real OpenAI API key via environment variable
- Configure proper CORS origins for production domains
- Set up proper logging and monitoring
- Consider connection pooling for TRMS client
- Implement caching for frequently accessed data
- Add authentication and authorization

## Troubleshooting

### Common Issues

**ClassNotFoundException for SnakeYAML**
- Ensure SnakeYAML 2.2 dependency is included

**Connection Refused to TRMS Backend**
- Verify TRMS Mock Backend is running on port 8090
- Check network connectivity and firewall settings

**OpenAI API Errors**
- Verify API key is valid and has sufficient credits
- Check OpenAI service status

**CORS Issues**
- Verify frontend origin is in allowed origins list
- Check that credentials are properly configured

### Logs
Application logs are written to `logs/trms-ai-backend.log` with DEBUG level for AI and TRMS components.

## License

This is a proof-of-concept implementation for TRMS AI integration.