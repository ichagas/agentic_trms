# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview
This is a TRMS (Treasury and Risk Management System) AI Agent POC that demonstrates AI-powered natural language interactions with financial systems. The project consists of three main components designed to work together:

1. **trms-legacy-mock** (Port 8090) - Mock legacy TRMS system with REST endpoints and EOD processing
2. **trms-ai-backend** (Port 8080) - Spring AI backend with ChatClient and function calling
3. **trms-frontend** (Port 3000) - React frontend with Google-like search interface

## Technology Stack
- **Backend Framework**: Spring Boot 3.2+ with Spring AI 0.8.0
- **LLM Integration**: Ollama (Llama 3) via Spring AI ChatClient with OpenAI fallback
- **Function Calling**: `@Function` annotations for AI tool calling
- **Frontend**: React with TypeScript, Framer Motion animations
- **Build Tools**: Maven (backend), npm (frontend)
- **Development**: Docker Compose for full stack deployment
- **AI Provider Options**: Mock (development), OpenAI, or Ollama

## Core Architecture Patterns

### Spring AI Function Calling
The system uses Spring AI's `@Function` annotation pattern for tool calling:
```java
@Function(name = "getAccountsByCurrency", 
          description = "Get accounts filtered by currency code")
public List<Account> getAccountsByCurrency(String currency) {
    return trmsClient.getAccountsByCurrency(currency.toUpperCase());
}
```

### AI Service Integration
Spring AI ChatClient is configured with system prompts and function callbacks:
```java
this.chatClient = chatClientBuilder
    .defaultSystem("You are an AI assistant for a Treasury and Risk Management System...")
    .defaultFunctions(functions.toArray(new FunctionCallback[0]))
    .build();
```

### Component Structure
- **Data Models**: Account, AccountBalance, Transaction, Report, MarketDataStatus, RateReset, EODCheckResult POJOs
- **Functions**: TrmsFunctions class with @Function annotated methods
- **Services**: TrmsAiService handles ChatClient interactions, MockAiService for development
- **Controllers**: ChatController for REST endpoints, WebSocket support
- **Client**: LegacyTrmsClient for REST communication with mock TRMS
- **Configuration**: AI provider selection (mock/openai/ollama) via properties

## Development Commands

### Backend Development
```bash
# Run mock legacy TRMS
cd trms-legacy-mock && mvn spring-boot:run

# Run Spring AI backend
cd trms-ai-backend && mvn spring-boot:run

# Run tests
mvn test

# Build and package
mvn clean package
```

### Frontend Development
```bash
# Install dependencies
cd trms-frontend && npm install

# Start development server
npm start

# Build for production
npm run build

# Run tests
npm test
```

### Full Stack Development
```bash
# Start all services with Docker Compose
docker-compose up

# Start in background
docker-compose up -d

# View logs
docker-compose logs -f trms-ai
```

## Key Configuration

### Spring AI Configuration
- **Ollama**: `http://localhost:11434`, model: `llama3`, temperature: `0.7`
- **OpenAI**: API key via `OPENAI_API_KEY`, model: `gpt-3.5-turbo`
- **Mock Mode**: `app.mock-ai=true` for development without LLM
- **AI Provider**: `app.ai-provider` (mock/openai/ollama)
- **Legacy TRMS**: `legacy-trms.base-url=http://localhost:8090/api/v1`

### API Endpoints
- Mock TRMS: `http://localhost:8090/api/v1`
- AI Backend: `http://localhost:8080/api/chat`
- Frontend: `http://localhost:3000`

### Environment Variables
- `OPENAI_API_KEY`: For OpenAI integration (optional, defaults to mock-key-for-development)
- `OLLAMA_BASE_URL`: Ollama server URL (defaults to http://localhost:11434)
- `LEGACY_TRMS_URL`: Base URL for legacy TRMS system
- `REACT_APP_API_URL`: Backend API URL for frontend
- `APP_MOCK_AI`: Enable/disable mock AI mode
- `APP_AI_PROVIDER`: Select AI provider (mock/openai/ollama)

## Business Functions
The system implements these core TRMS operations via AI function calls:
- **Account Management**: Query accounts by currency (`getAccountsByCurrency`)
- **Balance Inquiries**: Real-time balance checks (`checkAccountBalance`)
- **Transaction Processing**: Book transfers between accounts (`bookTransaction`)
- **EOD Processing**: End-of-Day readiness checks (`checkEODReadiness`)
- **Rate Fixings**: Propose missing rate fixings (`proposeRateFixings`)
- **Market Data**: Check market data feed status (FX rates, equity prices)
- **Transaction Validation**: Validate pending transactions
- **Report Generation**: Generate and retrieve financial reports

## EOD (End-of-Day) Processing
The system includes sophisticated EOD processing capabilities that simulate real-world financial operations:

### EOD Workflow
1. **Market Data Status Check**: Verify all pricing feeds (FX rates, equity prices, interest rates)
2. **Transaction Status Validation**: Ensure all transactions are properly validated
3. **Rate Reset Processing**: Check for missing LIBOR/EURIBOR fixings on floating rate instruments
4. **Readiness Assessment**: Comprehensive analysis with required actions
5. **Automated Proposals**: AI can suggest rate fixings based on market data

### Example EOD Interaction
```
User: "Can we run EOD?"
AI: Analyzes market data completeness, transaction statuses, missing rate resets
    Provides detailed readiness report with specific actions required
    Offers automated solutions: rate proposals, validation reports, etc.
```

### EOD Data Models
- `MarketDataStatus`: Track completeness of market data feeds
- `TransactionStatusSummary`: Overview of transaction validation states
- `RateReset`: Missing floating rate instrument fixings
- `EODCheckResult`: Comprehensive readiness assessment

## UI/UX Pattern
The frontend follows a Google-like interaction pattern:
1. Initial search-style interface with large input field
2. Smooth animation transition to chat interface after first query
3. Real-time responses via REST API and WebSocket support
4. Message bubbles with proper user/AI distinction
5. Suggested queries: "Show me all USD accounts", "Check balance for ACC-001-USD"
6. Rich formatting for EOD reports with status indicators and action recommendations

## Testing Strategy
- **Mock AI Service**: Pattern-matching responses for development without LLM
- **Unit Tests**: Spring AI functions with mocked clients
- **Integration Tests**: REST endpoints and WebSocket handlers
- **Component Tests**: React UI components
- **Docker Testing**: Full stack end-to-end testing
- **Health Checks**: `/api/chat/health` endpoint shows AI mode (MOCK/LIVE)

## Production Considerations
- Spring Boot Actuator for health checks and metrics
- Redis for conversation memory and caching
- Rate limiting and audit logging
- Security integration with Spring Security
- GPU support for Ollama deployment