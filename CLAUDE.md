# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview
This is a TRMS (Treasury and Risk Management System) AI Agent POC that demonstrates AI-powered natural language interactions with financial systems. The project consists of four main components designed to work together:

1. **trms-mock-app** (Port 8090) - Mock legacy TRMS system with REST endpoints and EOD processing
2. **swift-mock-app** (Port 8091) - Mock SWIFT messaging system for payment processing and reconciliation
3. **trms-ai-backend** (Port 8080) - Spring AI backend with ChatClient and function calling
4. **trms-frontend** (Port 5174) - React frontend with Google-like search interface

## Technology Stack
- **Backend Framework**: Spring Boot 3.2+ with Spring AI 0.8.0
- **LLM Integration**: Ollama (Llama 3) via Spring AI ChatClient with OpenAI fallback
- **Function Calling**: `@Function` annotations for AI tool calling
- **Frontend**: React with TypeScript, Framer Motion animations
- **Build Tools**: Maven (backend), npm (frontend)
- **Development**: Docker Compose for full stack deployment
- **AI Provider Options**: Mock (development), OpenAI, Azure OpenAI, or Ollama

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
- **Data Models**:
  - TRMS: Account, AccountBalance, Transaction, Report, MarketDataStatus, RateReset, EODCheckResult
  - SWIFT: SwiftMessage, Settlement, Payment, Confirmation, MessageStatus, ReconciliationResult
- **Functions**:
  - TrmsFunctions (5 functions): Account management, transactions, EOD processing
  - SwiftFunctions (8 functions): SWIFT messaging, reconciliation, report processing
- **Services**: TrmsAiService handles ChatClient interactions with hybrid function calling
- **Controllers**: ChatController for REST endpoints, WebSocket support
- **Clients**:
  - LegacyTrmsClient for REST communication with TRMS mock
  - SwiftClient for REST communication with SWIFT mock
- **Configuration**: AI provider selection (mock/openai/azure/ollama) via properties

## Development Commands

### Backend Development
```bash
# Run TRMS mock
cd backend/trms-mock-app && mvn spring-boot:run

# Run SWIFT mock
cd backend/swift-mock-app && mvn spring-boot:run

# Run Spring AI backend
cd backend/trms-ai-backend && mvn spring-boot:run

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
# Start all services (TRMS Mock, SWIFT Mock, AI Backend, Frontend)
./trms-services.sh start

# Check service status
./trms-services.sh status

# View logs (all services or specific: frontend, ai-backend, trms, swift)
./trms-services.sh logs swift

# Run health checks
./trms-services.sh health

# Stop all services
./trms-services.sh stop

# Restart all services
./trms-services.sh restart
```

## Key Configuration

### Spring AI Configuration
- **Ollama**: `http://localhost:11434`, model: `qwen3:1.7b`, temperature: `0.3`
- **OpenAI**: API key via `OPENAI_API_KEY`, model: `gpt-4o-mini`, temperature: `0.3`
- **Azure OpenAI**: Endpoint via `AZURE_OPENAI_ENDPOINT`, deployment via `AZURE_OPENAI_DEPLOYMENT_NAME`, temperature: `0.3`
- **Mock Mode**: `app.mock-ai=true` for development without LLM
- **AI Provider**: `app.ai-provider` (mock/openai/azure/ollama)
- **Legacy TRMS**: `legacy-trms.base-url=http://localhost:8090/api/v1`

### API Endpoints
- TRMS Mock: `http://localhost:8090/api/v1`
- SWIFT Mock: `http://localhost:8091/api/v1/swift`
- AI Backend: `http://localhost:8080/api/chat`
- Frontend: `http://localhost:5174`

### API Documentation
- TRMS Swagger: `http://localhost:8090/swagger-ui.html`
- SWIFT Swagger: `http://localhost:8091/swagger-ui.html`

### Environment Variables
- `OPENAI_API_KEY`: For OpenAI integration (optional, defaults to sk-demo-key)
- `AZURE_OPENAI_API_KEY`: For Azure OpenAI integration (optional, defaults to your-azure-key)
- `AZURE_OPENAI_ENDPOINT`: Azure OpenAI endpoint URL (e.g., https://myresource.openai.azure.com/)
- `AZURE_OPENAI_DEPLOYMENT_NAME`: Azure deployment name (e.g., gpt-4)
- `OLLAMA_BASE_URL`: Ollama server URL (defaults to http://localhost:11434)
- `OLLAMA_MODEL`: Ollama model name (defaults to qwen3:1.7b)
- `SWIFT_MOCK_BASE_URL`: SWIFT mock base URL (defaults to http://localhost:8091)
- `SWIFT_REDEMPTION_DIR`: Directory for redemption reports (defaults to ./data/redemption-reports)
- `SWIFT_EOD_DIR`: Directory for EOD reports (defaults to ./data/eod-reports)

## Business Functions

### TRMS Functions (TrmsFunctions.java)
The system implements these core TRMS operations via AI function calls:
- **Account Management**: Query accounts by currency (`getAccountsByCurrency`)
- **Balance Inquiries**: Real-time balance checks (`checkAccountBalance`)
- **Transaction Processing**: Book transfers between accounts (`bookTransaction`)
- **EOD Processing**: End-of-Day readiness checks (`checkEODReadiness`)
- **Rate Fixings**: Propose missing rate fixings (`proposeRateFixings`)

### SWIFT Functions (SwiftFunctions.java)
The system implements these SWIFT messaging operations via AI function calls:
- **Payment Messaging**: Send SWIFT MT103 payment messages (`sendSwiftPayment`)
- **Message Status**: Check SWIFT message status by ID (`checkSwiftMessageStatus`)
- **Message Query**: Get SWIFT messages by account (`getSwiftMessagesByAccount`)
- **Transaction Linking**: Get SWIFT messages by transaction ID (`getSwiftMessagesByTransaction`)
- **Reconciliation**: Reconcile SWIFT with TRMS transactions (`reconcileSwiftMessages`)
- **Unreconciled Messages**: Find messages needing attention (`getUnreconciledMessages`)
- **Redemption Processing**: Process redemption report files (automates 96-page manual entry) (`processRedemptionReport`)
- **EOD Verification**: Verify EOD reports in shared drive (`verifyEODReports`)

## SWIFT Integration & Reconciliation

### SWIFT Message Processing
The system simulates SWIFT messaging for payment instructions and settlements:

**Message Types Supported:**
- **MT103**: Single Customer Credit Transfer (payments)
- **MT202**: General Financial Institution Transfer
- **MT950**: Statement Message
- **MT910**: Confirmation of Credit

**Message Lifecycle:**
1. PENDING → Message created but not transmitted
2. SENT → Transmitted to SWIFT network
3. CONFIRMED → Acknowledgment received from counterparty
4. RECONCILED → Matched with TRMS transaction
5. UNRECONCILED → Mismatch detected, needs attention

### Reconciliation Strategy
SWIFT messages are reconciled with TRMS transactions using **dual matching**:
- **Transaction ID** must match
- **Amount + Currency** must match

This ensures both systems are synchronized and prevents discrepancies.

### File-Based Scenarios

#### Redemption Report Processing
**Location:** `backend/swift-mock-app/data/redemption-reports/`

Automates manual processing of redemption reports (typically 96 pages):
- CSV format: `AccountID,BeneficiaryName,BeneficiaryAccount,Amount,Currency,Reference`
- AI parses file, extracts redemption requests
- Returns summary: total redemptions, processed count, failed count, total amount
- Errors are logged for manual review

**Example:**
```
User: "Process the redemption report"
AI: Calls processRedemptionReport("redemption_report_latest.csv")
AI: "Processed 7 redemptions totaling $515,000 USD. 0 errors."
```

#### EOD Report Verification
**Location:** `backend/swift-mock-app/data/eod-reports/`

Automates verification of End-of-Day reports in shared drive:
- Expected reports: balance_report, transaction_log, swift_reconciliation, settlement_report
- AI checks existence, validates file format, verifies data completeness
- Returns verification result with passed/failed checks

**Example:**
```
User: "Verify today's EOD reports"
AI: Calls verifyEODReports("2025-10-09")
AI: "Verified 4/4 reports. All checks PASSED. Ready for EOD processing."
```

## EOD (End-of-Day) Processing
The system includes sophisticated EOD processing capabilities that simulate real-world financial operations:

### EOD Workflow
1. **Market Data Status Check**: Verify all pricing feeds (FX rates, equity prices, interest rates)
2. **Transaction Status Validation**: Ensure all transactions are properly validated
3. **SWIFT Reconciliation**: Check all SWIFT messages are reconciled with transactions
4. **Rate Reset Processing**: Check for missing LIBOR/EURIBOR fixings on floating rate instruments
5. **Report Verification**: Validate all required EOD reports exist and are complete
6. **Readiness Assessment**: Comprehensive analysis with required actions
7. **Automated Proposals**: AI can suggest rate fixings based on market data

### Example EOD Interaction
```
User: "Can we run EOD?"
AI: Analyzes market data, transaction statuses, SWIFT reconciliation, missing rate resets
    Verifies EOD reports in shared drive
    Provides detailed readiness report with specific actions required
    Offers automated solutions: rate proposals, validation reports, reconciliation
```

### EOD Data Models
- `MarketDataStatus`: Track completeness of market data feeds
- `TransactionStatusSummary`: Overview of transaction validation states
- `RateReset`: Missing floating rate instrument fixings
- `EODCheckResult`: Comprehensive readiness assessment
- `EODReportVerificationResult`: Report completeness validation
- `ReconciliationResult`: SWIFT vs TRMS reconciliation status

## UI/UX Pattern
The frontend follows a Google-like interaction pattern:
1. Initial search-style interface with large input field
2. Smooth animation transition to chat interface after first query
3. Real-time responses via REST API and WebSocket support
4. Message bubbles with proper user/AI distinction
5. Suggested queries:
   - "Show me all USD accounts"
   - "Check balance for ACC-001-USD"
   - "Transfer $50,000 from ACC-001-USD to ACC-010-USD"
   - "Send via SWIFT"
   - "Process the redemption report"
   - "Verify today's EOD reports"
   - "Check unreconciled SWIFT messages"
6. Rich formatting for EOD reports with status indicators and action recommendations

## Example End-to-End Workflows

### Workflow 1: Complete Payment Cycle (TRMS + SWIFT)
```
User: "Show me the position of account ACC-001-USD"
AI: → checkAccountBalance()
AI: "Account ACC-001-USD has balance: $1,250,000 USD"

User: "Transfer $50,000 to ACC-010-USD"
AI: → bookTransaction()
AI: "Transaction TRX-88912 completed successfully"

User: "Send via SWIFT"
AI: → sendSwiftPayment(transactionId="TRX-88912")
AI: "SWIFT MT103 message SWIFT-MSG-991 sent. Status: CONFIRMED"

User: "Did the SWIFT message get confirmed?"
AI: → checkSwiftMessageStatus("SWIFT-MSG-991")
AI: "Yes, confirmed at 15:45:22. Ready for reconciliation."
```

### Workflow 2: Redemption Report Automation
```
User: "I received the redemption report, can you process it?"
AI: → processRedemptionReport("redemption_report_latest.csv")
AI: "Processed 7 redemptions totaling $515,000 USD
     - ACC-001-USD: 2 redemptions, $175,000
     - ACC-002-EUR: 2 redemptions, €165,000
     - ACC-003-GBP: 2 redemptions, £75,000
     - ACC-004-JPY: 1 redemption, ¥10,000,000
     All processed successfully. Would you like me to create the transactions?"
```

### Workflow 3: EOD Readiness Check
```
User: "Can we run EOD today?"
AI: → checkEODReadiness()
AI: → verifyEODReports("2025-10-09")
AI: → getUnreconciledMessages()
AI: "EOD Readiness Assessment:
     ✅ Market data complete
     ✅ All transactions validated
     ✅ EOD reports verified (4/4 passed)
     ⚠️  3 SWIFT messages unreconciled
     ❌ 2 rate fixings missing (GBP-3M, EUR-6M)

     Recommendation: Resolve unreconciled messages and propose rate fixings before EOD."
```

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