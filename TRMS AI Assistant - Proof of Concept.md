# TRMS AI Assistant - Proof of Concept

This is a Proof of Concept (POC) implementation of a Treasury and Risk Management System (TRMS) with AI integration, built for a hackathon environment with simplified components.

## Architecture Overview

The POC consists of three main components:

1. **Mocked Legacy TRMS System** (Spring Boot) - Port 8080
2. **AI Integration Service** (Spring Boot) - Port 8081  
3. **React Frontend** (Vite) - Port 5173

```
┌─────────────────┐     ┌──────────────────┐     ┌─────────────────┐
│                 │     │                  │     │                 │
│   React UI      │────▶│  AI Service      │────▶│  Legacy TRMS    │
│   (Port 5173)   │     │  (Port 8081)     │     │  (Port 8080)    │
│                 │     │                  │     │                 │
└─────────────────┘     └──────────────────┘     └─────────────────┘
```

## Features Implemented

### Frontend (React)
- **Google-like Landing Page**: Clean, simple interface with centered input field
- **Animated Transition**: Smooth animation from landing page to chat interface
- **Chat Interface**: Real-time chat with AI assistant
- **Responsive Design**: Works on both desktop and mobile devices
- **Modern UI**: Built with Tailwind CSS and shadcn/ui components

### AI Service (Mocked)
- **Natural Language Processing**: Interprets user queries about accounts, balances, and transactions
- **TRMS Integration**: Connects to the mocked TRMS system to fetch real data
- **Conversation Management**: Maintains conversation context
- **Error Handling**: Graceful error handling and user feedback

### Legacy TRMS System (Mocked)
- **Account Management**: Sample accounts in multiple currencies (USD, EUR, GBP, JPY)
- **Balance Tracking**: Real-time balance information
- **Transaction History**: Sample transaction data
- **RESTful API**: Standard REST endpoints for integration

## Sample Data

The system comes pre-loaded with sample data:

### Accounts
- ACC-001-USD: Trading Account USD ($1,000,000.00)
- ACC-002-USD: Settlement Account USD ($500,000.00)
- ACC-003-EUR: Trading Account EUR (€750,000.00)
- ACC-004-EUR: Settlement Account EUR (€300,000.00)
- ACC-005-GBP: Trading Account GBP (£600,000.00)
- ACC-006-JPY: Trading Account JPY (¥100,000,000.00)

### Sample Transactions
- TXN-SAMPLE01: $50,000 USD transfer between accounts
- TXN-SAMPLE02: €25,000 EUR settlement transfer

## Quick Start

### Prerequisites
- Java 17+
- Maven 3.8+
- Node.js 20+
- pnpm

### Starting the Services

1. **Start the Legacy TRMS System**:
```bash
cd backend/trms-mock
mvn spring-boot:run
```
The service will be available at http://localhost:8080

2. **Start the AI Service**:
```bash
cd backend/trms-ai
mvn spring-boot:run
```
The service will be available at http://localhost:8081

3. **Start the React Frontend**:
```bash
cd trms-poc/frontend/trms-chat
pnpm run dev --host
```
The frontend will be available at http://localhost:5173

### Testing the Application

1. Open http://localhost:5173 in your browser
2. Try these sample queries:
   - "Show me all USD accounts"
   - "Check balance for ACC-001-USD"
   - "Show recent transactions"
   - "What can you help me with?"

## API Endpoints

### Legacy TRMS System (Port 8080)
- `GET /api/trms/health` - Health check
- `GET /api/trms/accounts` - Get all accounts
- `GET /api/trms/accounts?currency=USD` - Get accounts by currency
- `GET /api/trms/accounts/{id}/balance` - Get account balance
- `GET /api/trms/transactions` - Get all transactions
- `POST /api/trms/transactions` - Create new transaction

### AI Service (Port 8081)
- `GET /api/chat/health` - Health check
- `POST /api/chat` - Send chat message

## Limitations (POC/Hackathon Constraints)

1. **Mocked AI**: The AI responses are rule-based, not using actual LLM integration
2. **Simplified TRMS**: Basic CRUD operations only, no complex business logic
3. **No Authentication**: No user authentication or authorization
4. **In-Memory Data**: Uses SQLite for simplicity
5. **No Real-time Updates**: No WebSocket or real-time data synchronization

## Future Enhancements

For a production system, consider:

1. **Real AI Integration**: Replace mocked AI with actual Ollama/LLM integration
2. **Spring Boot Backend**: Implement the actual Spring AI framework as described in the proposal
3. **Authentication & Authorization**: Add proper security layers
4. **Database Integration**: Connect to actual TRMS databases
5. **Real-time Features**: Add WebSocket support for live updates
6. **Advanced UI**: Enhanced charts, dashboards, and reporting features
7. **Error Handling**: Comprehensive error handling and logging
8. **Testing**: Unit tests, integration tests, and end-to-end tests

## Technology Stack

- **Frontend**: React 18, Vite, Tailwind CSS, shadcn/ui, Framer Motion
- **Backend**: Spring Boot 3.2+, Spring AI 0.8.0, Maven
- **Database**: H2 Database (in-memory for POC)

## Project Structure

```
trms-poc/
├── backend/
│   ├── trms-mock/          # Mocked Legacy TRMS System (Spring Boot)
│   │   ├── src/main/java/com/example/trmsmock/
│   │   │   ├── model/      # JPA Entities (Account, Transaction)
│   │   │   ├── repository/ # Spring Data JPA Repositories
│   │   │   ├── controller/ # REST Controllers
│   │   │   └── TrmsMockApplication.java # Spring Boot Application
│   │   ├── src/main/resources/application.properties # Configuration
│   │   └── pom.xml         # Maven build file
│   └── trms-ai/            # AI Integration Service (Spring Boot)
│       ├── src/main/java/com/example/trmsai/
│       │   ├── model/      # Data models
│       │   ├── service/    # AI Service logic
│       │   ├── controller/ # REST Controllers
│       │   └── TrmsAiApplication.java # Spring Boot Application
│       ├── src/main/resources/application.properties # Configuration
│       └── pom.xml         # Maven build file
├── frontend/
│   └── trms-chat/          # React Frontend
│       ├── public/
│       ├── src/
│       │   ├── assets/
│       │   ├── components/
│       │   ├── App.css
│       │   ├── App.jsx
│       │   ├── index.css
│       │   └── main.jsx
│       ├── components.json
│       ├── index.html
│       ├── package.json
│       ├── pnpm-lock.yaml
│       └── vite.config.js
├── start-all.sh            # One-command startup script
├── README.md               # This file
└── DEPLOYMENT.md           # Deployment instructions
```

## Contributing

This is a POC for demonstration purposes. For production use, please refer to the original TRMS SpringAI proposal document for proper implementation guidelines.

