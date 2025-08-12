# TRMS POC Deployment Guide

## Quick Start (Recommended)

The easiest way to start all services is using the provided startup script:

```bash
cd trms-poc
./start-all.sh
```

This script will:
1. Check if required ports (8080, 8081, 5173) are available
2. Start all three services in the correct order
3. Display service URLs
4. Wait for user input to stop all services

## Manual Deployment

If you prefer to start services manually or need to troubleshoot:

### 1. Start the Legacy TRMS System

```bash
cd trms-poc/backend/trms-mock
mvn spring-boot:run
```

**Expected Output:**
```
Initialized 6 sample accounts
Initialized 2 sample transactions
Sample data initialized successfully!
Started TrmsMockApplication in 3.456 seconds
```

**Health Check:**
```bash
curl http://localhost:8080/api/trms/health
```

### 2. Start the AI Service

```bash
cd trms-poc/backend/trms-ai
mvn spring-boot:run
```

**Expected Output:**
```
Started TrmsAiApplication in 2.123 seconds
```

**Health Check:**
```bash
curl http://localhost:8081/api/chat/health
```

### 3. Start the React Frontend

```bash
cd trms-poc/frontend/trms-chat
pnpm run dev --host
```

**Expected Output:**
```
VITE v6.3.5  ready in 683 ms
➜  Local:   http://localhost:5173/
➜  Network: http://169.254.0.21:5173/
```

## Accessing the Application

Once all services are running:

1. Open your web browser
2. Navigate to http://localhost:5173
3. You should see the TRMS AI Assistant landing page

## Testing the Application

Try these sample queries to test functionality:

### Account Queries
- "Show me all USD accounts"
- "Show me EUR accounts"
- "List all accounts"

### Balance Checks
- "Check balance for ACC-001-USD"
- "What's the balance of ACC-003-EUR?"
- "Show balance for ACC-005-GBP"

### Transaction Queries
- "Show recent transactions"
- "List all transactions"
- "Show transaction history"

### Help
- "What can you help me with?"
- "Help"

## Troubleshooting

### Port Already in Use
If you get "Port already in use" errors:

```bash
# Check what's using the ports
lsof -i :8080
lsof -i :8081
lsof -i :5173

# Kill processes if needed
kill <PID>
```

### Java/Maven Issues
If you get Java or Maven errors:

```bash
# Check Java version (should be 17+)
java -version

# Check Maven version (should be 3.8+)
mvn -version

# Clean and rebuild
mvn clean compile
```

### React Dependencies Issues
If the React app fails to start:

```bash
cd frontend/trms-chat
rm -rf node_modules pnpm-lock.yaml
pnpm install
```

### Database Issues
If you get H2 database errors:

```bash
# H2 is in-memory, so just restart the TRMS mock service
# The database will be recreated automatically
```

## Service URLs

- **Frontend**: http://localhost:5173
- **AI Service**: http://localhost:8081
- **TRMS Mock**: http://localhost:8080

## API Documentation

### TRMS Mock System (Port 8080)

#### Health Check
```bash
GET /api/trms/health
```

#### Get Accounts
```bash
# All accounts
GET /api/trms/accounts

# Filter by currency
GET /api/trms/accounts?currency=USD
```

#### Get Account Balance
```bash
GET /api/trms/accounts/ACC-001-USD/balance
```

#### Get Transactions
```bash
GET /api/trms/transactions
```

### AI Service (Port 8081)

#### Health Check
```bash
GET /api/chat/health
```

#### Send Chat Message
```bash
POST /api/chat
Content-Type: application/json

{
  "message": "Show me all USD accounts",
  "conversation_id": "optional-conversation-id"
}
```

## Stopping Services

### Using the Startup Script
If you used `./start-all.sh`, simply press Enter when prompted.

### Manual Stop
```bash
# Find and kill Java processes
ps aux | grep "spring-boot:run"
kill <PID1> <PID2>

# Find and kill Vite process
ps aux | grep "vite"
kill <PID>
```

## Production Considerations

This is a POC/hackathon implementation. For production deployment:

1. **Use proper application servers** (e.g., Tomcat, Jetty) instead of embedded servers
2. **Add reverse proxy** (e.g., Nginx) for the frontend
3. **Implement proper logging** and monitoring
4. **Add authentication** and authorization
5. **Use production database** instead of H2
6. **Add SSL/TLS** certificates
7. **Implement proper error handling** and recovery
8. **Add health checks** and monitoring
9. **Use environment variables** for configuration
10. **Add proper testing** and CI/CD pipeline

## Architecture Notes

The current setup uses:
- Spring Boot embedded servers for backend services
- Vite development server for frontend
- H2 in-memory database for data storage
- Direct HTTP communication between services

This is suitable for development and demonstration but should be enhanced for production use.

