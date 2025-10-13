# SWIFT Integration Guide

## Overview
This guide describes the SWIFT mock system integration with the TRMS AI Agent POC, including new scenarios for payment processing, reconciliation, and automated report handling.

## Architecture

### System Components
```
┌─────────────────┐
│   Frontend      │  Port 5174 - React UI with Google-like interface
│   (React)       │
└────────┬────────┘
         │
┌────────▼────────┐
│  AI Backend     │  Port 8080 - Spring AI with ChatClient
│  (Spring AI)    │
│                 │  TrmsFunctions (5 AI functions)
│                 │  SwiftFunctions (8 AI functions)
└────┬────────┬───┘
     │        │
┌────▼────┐ ┌▼─────────────┐
│ TRMS    │ │ SWIFT Mock   │
│ Mock    │ │ Port 8091    │
│ 8090    │ │              │
│         │ │ Features:    │
│ - Accts │ │ - MT103/202  │
│ - Txns  │ │ - Reconcile  │
│ - EOD   │ │ - Reports    │
│ - Rates │ │ - Files      │
└─────────┘ └──────────────┘
```

## SWIFT Mock System

### Capabilities
- **SWIFT Messaging**: MT103, MT202, MT950, MT910 message types
- **Reconciliation**: Dual matching (Transaction ID + Amount/Currency)
- **File Processing**: Redemption reports (CSV), EOD report verification
- **Status Tracking**: PENDING → SENT → CONFIRMED → RECONCILED
- **BIC Codes**: Fictional (MOCKUS33XXX, TESTGB2LXXX)

### Data Storage
- **In-Memory**: ConcurrentHashMap for messages, settlements, payments
- **File-Based**: Local directories for reports
  - Redemption: `backend/swift-mock-app/data/redemption-reports/`
  - EOD Reports: `backend/swift-mock-app/data/eod-reports/`

### API Endpoints
**Base URL:** `http://localhost:8091/api/v1/swift`

- `POST /messages` - Send SWIFT message
- `GET /messages/{id}/status` - Check message status
- `GET /messages/account/{accountId}` - Get messages by account
- `GET /messages/transaction/{txnId}` - Get messages by transaction
- `GET /messages/unreconciled` - Get unreconciled messages
- `POST /messages/reconcile` - Reconcile messages
- `POST /reports/redemptions/process?fileName={name}` - Process redemption report
- `GET /reports/eod/verify?reportDate={date}` - Verify EOD reports
- `GET /settlements/account/{accountId}` - Get settlements

**Documentation:** http://localhost:8091/swagger-ui.html

## AI Functions

### SWIFT Functions (SwiftFunctions.java)

#### 1. sendSwiftPayment
**Purpose:** Send MT103 payment message via SWIFT network

**Parameters:**
- accountId: Source account (e.g., "ACC-001-USD")
- transactionId: TRMS transaction ID (e.g., "TRX-12345")
- amount: Payment amount (e.g., 50000.00)
- currency: Currency code (e.g., "USD")
- receiverBIC: Receiver bank BIC (e.g., "TESTGB2LXXX")
- beneficiaryName: Beneficiary name
- beneficiaryAccount: Beneficiary account number

**Returns:** SwiftMessage with ID, status, timestamps

**Example:**
```
User: "Send $50,000 via SWIFT for transaction TRX-12345"
AI: → sendSwiftPayment(...)
AI: "SWIFT MT103 message SWIFT-MSG-00123 sent successfully. Status: CONFIRMED"
```

#### 2. checkSwiftMessageStatus
**Purpose:** Check status of a specific SWIFT message

**Parameters:**
- messageId: SWIFT message ID (e.g., "SWIFT-MSG-00123")

**Returns:** MessageStatusResponse with status, reconciliation details

**Example:**
```
User: "Check status of SWIFT-MSG-00123"
AI: → checkSwiftMessageStatus("SWIFT-MSG-00123")
AI: "Message status: CONFIRMED at 14:35:22. Reconciled with TRX-12345."
```

#### 3. getSwiftMessagesByAccount
**Purpose:** Get all SWIFT messages for an account

**Parameters:**
- accountId: Account ID (e.g., "ACC-001-USD")

**Returns:** List of SwiftMessage

**Example:**
```
User: "Show me all SWIFT messages for ACC-001-USD"
AI: → getSwiftMessagesByAccount("ACC-001-USD")
AI: "Found 5 SWIFT messages for ACC-001-USD (3 sent today)"
```

#### 4. getSwiftMessagesByTransaction
**Purpose:** Get SWIFT messages linked to a TRMS transaction

**Parameters:**
- transactionId: Transaction ID (e.g., "TRX-12345")

**Returns:** List of SwiftMessage

**Example:**
```
User: "Did we send SWIFT for transaction TRX-12345?"
AI: → getSwiftMessagesByTransaction("TRX-12345")
AI: "Yes, SWIFT-MSG-00123 sent at 14:30, confirmed at 14:35"
```

#### 5. reconcileSwiftMessages
**Purpose:** Reconcile SWIFT messages with TRMS transactions

**Parameters:**
- accountId: Account to reconcile (optional, null for all)
- autoReconcile: Automatically mark as reconciled if match found

**Returns:** ReconciliationResult with counts and issues

**Example:**
```
User: "Reconcile SWIFT messages"
AI: → reconcileSwiftMessages(null, true)
AI: "Reconciled 12 messages. 3 unreconciled. 2 pending."
```

#### 6. getUnreconciledMessages
**Purpose:** Find SWIFT messages that need reconciliation

**Returns:** List of unreconciled SwiftMessage

**Example:**
```
User: "Show unreconciled SWIFT messages"
AI: → getUnreconciledMessages()
AI: "Found 3 unreconciled messages requiring attention"
```

#### 7. processRedemptionReport
**Purpose:** Process redemption report file (automates 96-page manual entry)

**Parameters:**
- fileName: Report filename (e.g., "redemption_report_latest.csv")

**Returns:** RedemptionReportResult with processed items, errors

**File Format:**
```csv
AccountID,BeneficiaryName,BeneficiaryAccount,Amount,Currency,Reference
ACC-001-USD,John Doe,987654321,50000.00,USD,RED-001
```

**Example:**
```
User: "Process the redemption report"
AI: → processRedemptionReport("redemption_report_latest.csv")
AI: "Processed 7 redemptions totaling $515,000. 0 errors."
```

#### 8. verifyEODReports
**Purpose:** Verify EOD reports exist and are valid

**Parameters:**
- reportDate: Date in YYYY-MM-DD format (e.g., "2025-10-09")

**Returns:** EODReportVerificationResult with checks passed/failed

**Expected Reports:**
- balance_report_{date}.csv
- transaction_log_{date}.csv
- swift_reconciliation_{date}.csv
- settlement_report_{date}.csv

**Example:**
```
User: "Verify today's EOD reports"
AI: → verifyEODReports("2025-10-09")
AI: "4/4 reports verified. All checks PASSED."
```

## Demo Scenarios

### Scenario 1: End-to-End Payment Workflow
**Goal:** Demonstrate complete payment lifecycle from TRMS to SWIFT

**Steps:**
1. Check account balance
2. Book transaction in TRMS
3. Send SWIFT payment message
4. Check SWIFT confirmation
5. Verify reconciliation

**Script:**
```
User: "What's the balance of ACC-001-USD?"
AI: "$1,250,000 USD available"

User: "Transfer $50,000 to ACC-010-USD"
AI: "Transaction TRX-88912 booked successfully"

User: "Send via SWIFT to bank TESTGB2LXXX"
AI: "SWIFT MT103 SWIFT-MSG-00991 sent. Status: CONFIRMED"

User: "Is it reconciled?"
AI: "Yes, reconciled with TRX-88912. Match on transaction ID and amount."
```

### Scenario 2: Redemption Report Processing
**Goal:** Automate 96-page manual data entry

**Steps:**
1. User receives redemption report via email
2. Places file in redemption-reports directory
3. AI processes file
4. AI creates transactions (optional)
5. AI sends SWIFT messages (optional)

**Script:**
```
User: "I received the redemption report, can you process it?"
AI: "Processing redemption_report_latest.csv..."
AI: "Found 7 redemptions:
     - 2 USD: $175,000
     - 2 EUR: €165,000
     - 2 GBP: £75,000
     - 1 JPY: ¥10,000,000
     Total: $515,000 equivalent
     Would you like me to create the transactions?"

User: "Yes, create transactions and send SWIFT"
AI: "Created 7 transactions. Sent 7 SWIFT messages. All confirmed."
```

### Scenario 3: EOD Report Verification
**Goal:** Automate manual EOD report validation

**Steps:**
1. User requests EOD verification
2. AI checks all required reports exist
3. AI validates file format and data
4. AI reports any issues

**Script:**
```
User: "Can you verify today's EOD reports?"
AI: "Verifying EOD reports for 2025-10-09..."
AI: "✅ balance_report_2025-10-09.csv - PASSED
     ✅ transaction_log_2025-10-09.csv - PASSED
     ✅ swift_reconciliation_2025-10-09.csv - PASSED
     ✅ settlement_report_2025-10-09.csv - PASSED
     All 4/4 checks passed. Ready for EOD processing."
```

### Scenario 4: SWIFT Message Verification
**Goal:** Check if payment was sent and confirmed

**Script:**
```
User: "Did we send SWIFT for transaction TRX-12345?"
AI: "Yes, SWIFT-MSG-00789 sent at 10:30 AM"

User: "What's the status?"
AI: "Status: CONFIRMED at 10:35 AM
     Reconciliation: MATCHED
     Beneficiary: Global Bank AG
     Amount: $100,000 USD"

User: "Show all unreconciled messages"
AI: "3 messages need reconciliation:
     - SWIFT-MSG-00791: No matching transaction
     - SWIFT-MSG-00792: Amount mismatch ($50K vs $55K)
     - SWIFT-MSG-00793: Pending confirmation"
```

### Scenario 5: Combined TRMS + SWIFT EOD Check
**Goal:** Complete EOD readiness assessment

**Script:**
```
User: "Can we run EOD?"
AI: "Checking EOD readiness..."

[TRMS Checks]
AI: "✅ Market data complete (FX, Equity, Rates)
     ✅ All transactions validated
     ⚠️  2 rate fixings missing (GBP-3M, EUR-6M)"

[SWIFT Checks]
AI: "✅ EOD reports verified (4/4)
     ⚠️  3 SWIFT messages unreconciled"

[Summary]
AI: "EOD Status: NOT READY

     Required Actions:
     1. Resolve 3 unreconciled SWIFT messages
     2. Propose rate fixings for GBP-3M and EUR-6M

     Would you like me to:
     - Show unreconciled messages?
     - Propose rate fixings?
     - Generate remediation report?"
```

## Testing

### Quick Start
```bash
# Start all services
./trms-services.sh start

# Check status
./trms-services.sh status

# View SWIFT logs
./trms-services.sh logs swift
```

### Manual Testing

**1. Test SWIFT Message Creation:**
```bash
curl -X POST http://localhost:8091/api/v1/swift/messages \
  -H "Content-Type: application/json" \
  -d '{
    "messageType": "MT103",
    "accountId": "ACC-001-USD",
    "transactionId": "TRX-TEST-001",
    "amount": 50000.00,
    "currency": "USD",
    "receiverBIC": "TESTGB2LXXX",
    "beneficiaryName": "Test Beneficiary",
    "beneficiaryAccount": "12345678"
  }'
```

**2. Test Message Status:**
```bash
curl http://localhost:8091/api/v1/swift/messages/SWIFT-MSG-00001/status
```

**3. Test Reconciliation:**
```bash
curl -X POST http://localhost:8091/api/v1/swift/messages/reconcile \
  -H "Content-Type: application/json" \
  -d '{
    "accountId": "ACC-001-USD",
    "autoReconcile": true
  }'
```

**4. Test Redemption Report:**
```bash
curl -X POST "http://localhost:8091/api/v1/swift/reports/redemptions/process?fileName=redemption_report_latest.csv"
```

**5. Test EOD Verification:**
```bash
curl "http://localhost:8091/api/v1/swift/reports/eod/verify?reportDate=2025-10-09"
```

## File Locations

### Redemption Reports
**Path:** `backend/swift-mock-app/data/redemption-reports/`

**Sample:** `redemption_report_latest.csv`

### EOD Reports
**Path:** `backend/swift-mock-app/data/eod-reports/`

**Files:**
- `balance_report_2025-10-09.csv`
- `transaction_log_2025-10-09.csv`
- `swift_reconciliation_2025-10-09.csv`
- `settlement_report_2025-10-09.csv`

## Troubleshooting

### SWIFT Mock Not Starting
```bash
# Check logs
./trms-services.sh logs swift

# Check port availability
lsof -i :8091

# Verify compilation
cd backend/swift-mock-app
JAVA_HOME=/opt/homebrew/opt/openjdk@21 mvn clean compile
```

### File Not Found Errors
```bash
# Ensure data directories exist
ls -la backend/swift-mock-app/data/

# Check file permissions
chmod 644 backend/swift-mock-app/data/redemption-reports/*.csv
```

### Reconciliation Issues
- Verify transaction IDs match exactly (case-sensitive)
- Check amount and currency match
- Review reconciliation logs in AI backend

## Production Considerations

### Security
- Implement authentication for SWIFT endpoints
- Encrypt sensitive data (BIC codes, account numbers)
- Add audit logging for all SWIFT operations
- Rate limiting on message sending

### Scalability
- Replace in-memory storage with database (PostgreSQL/MongoDB)
- Add message queue for async processing (RabbitMQ/Kafka)
- Implement retry logic for failed messages
- Add distributed caching (Redis)

### Compliance
- Store all SWIFT messages for regulatory retention
- Implement message archival system
- Add compliance reporting endpoints
- Track message amendments and cancellations

### Monitoring
- Add Prometheus metrics for message counts, reconciliation rates
- Set up alerting for unreconciled messages
- Monitor file processing success rates
- Track API response times

## Next Steps

1. **Enhance Reconciliation**
   - Add fuzzy matching for beneficiary names
   - Support partial reconciliation
   - Implement reconciliation rules engine

2. **Additional SWIFT Types**
   - MT940 (Customer Statement)
   - MT202COV (Cover Payment)
   - MT199 (Free Format Message)

3. **Advanced Features**
   - Real-time SWIFT network simulation
   - Message amendments and cancellations
   - Multi-currency settlement netting
   - STP (Straight-Through Processing) metrics

4. **Integration**
   - Connect to real SWIFT Alliance Lite
   - Integrate with actual core banking system
   - Add workflow automation (Camunda)
   - Implement event sourcing for audit trail
