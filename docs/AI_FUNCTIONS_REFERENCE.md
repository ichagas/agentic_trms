# TRMS AI Backend - Function Reference

This document provides a comprehensive reference of all AI-callable functions provided by the trms-ai-backend service. These functions enable natural language interactions with both TRMS (Treasury and Risk Management System) and SWIFT messaging systems.

## Overview

The trms-ai-backend exposes **13 functions** split across two systems:
- **5 TRMS Functions** - Core treasury operations (accounts, balances, transactions, EOD)
- **8 SWIFT Functions** - Payment messaging, reconciliation, and report processing

All functions are implemented using Spring AI's `@Bean` and `@Description` annotations, allowing the LLM to automatically select and invoke them based on user queries.

---

## TRMS System Functions

### 1. getAccountsByCurrency

**Purpose:** Filter accounts by currency code

**Description:**
Get accounts filtered by currency from the TRMS system. Pass a 3-letter currency code (e.g., USD, EUR, GBP) to filter accounts, or leave empty to get all accounts. Returns account details including account number, name, type, status, and current balance.

**Parameters:**
- `currency` (String): 3-letter currency code (USD, EUR, GBP, JPY)

**Returns:** `List<Account>`
- Account number
- Account name
- Account type
- Status (ACTIVE, INACTIVE)
- Current balance

**Implementation:** `TrmsFunctions.java:39`

**Example Use Case:**
```
User: "Show me all USD accounts"
AI: Calls getAccountsByCurrency("USD")
```

---

### 2. checkAccountBalance

**Purpose:** Get detailed balance for a specific account

**Description:**
Check the balance and details for a specific account by account ID. Returns comprehensive balance information including available balance, pending transactions, and last update timestamp. Use this to verify account balances before transactions or for reporting purposes.

**Parameters:**
- `accountId` (String): Account identifier (e.g., "ACC-001-USD")

**Returns:** `AccountBalance`
- Account ID
- Available balance
- Pending transactions
- Currency
- Last update timestamp

**Implementation:** `TrmsFunctions.java:61`

**Example Use Case:**
```
User: "Check balance for ACC-001-USD"
AI: Calls checkAccountBalance("ACC-001-USD")
```

---

### 3. bookTransaction

**Purpose:** Process transfer between accounts

**Description:**
Book a financial transaction between two accounts in the TRMS system. Requires source account ID, destination account ID, amount, and currency. The transaction will be processed immediately and returns transaction details including transaction ID, status, and reference number. Validates account existence and sufficient balance before processing.

**Parameters:**
- `fromAccount` (String): Source account ID
- `toAccount` (String): Destination account ID
- `amount` (Double): Transfer amount
- `currency` (String): Currency code (must match account currencies)

**Returns:** `Transaction`
- Transaction ID
- Status (COMPLETED, PENDING, FAILED)
- Reference number
- Timestamp

**Implementation:** `TrmsFunctions.java:85`

**Example Use Case:**
```
User: "Transfer $50,000 from ACC-001-USD to ACC-002-USD"
AI: Calls bookTransaction("ACC-001-USD", "ACC-002-USD", 50000.0, "USD")
```

---

### 4. checkEODReadiness

**Purpose:** Verify system ready for End-of-Day processing

**Description:**
Check the End of Day (EOD) processing readiness status in the TRMS system. Returns comprehensive status including whether EOD can be run, last run date, next scheduled run, any blocking issues, completed checks, and pending tasks. Use this to determine if the system is ready for daily closing procedures.

**Parameters:** None

**Returns:** `EODStatus`
- Ready flag (boolean)
- Status message
- Last run date
- Next scheduled run
- Blocking issues (list)
- Completed checks
- Pending tasks

**Implementation:** `TrmsFunctions.java:114`

**Example Use Case:**
```
User: "Can we run EOD today?"
AI: Calls checkEODReadiness()
```

---

### 5. proposeRateFixings

**Purpose:** Get missing interest rate resets for EOD

**Description:**
Get proposed rate fixings for missing interest rate resets in the TRMS system. Returns a list of currency/tenor combinations that need rate fixes for EOD processing, including current rates, proposed rates, reset dates, and data sources. Use this to identify and resolve missing market data before running EOD procedures.

**Parameters:** None

**Returns:** `List<RateReset>`
- Currency code
- Tenor (e.g., "3M", "6M")
- Current rate
- Proposed rate
- Reset date
- Data source
- Status

**Implementation:** `TrmsFunctions.java:137`

**Example Use Case:**
```
User: "What rate fixings are missing?"
AI: Calls proposeRateFixings()
```

---

## SWIFT System Functions

### 6. sendSwiftPayment

**Purpose:** Send MT103 payment message via SWIFT network

**Description:**
Send a payment via SWIFT network. Creates an MT103 message for the payment and transmits it to the beneficiary's bank. Requires account ID, transaction ID, amount, currency, receiver BIC code, beneficiary name and account. Returns the SWIFT message details including message ID and status. Use this after booking a transaction in TRMS to send the payment confirmation.

**Parameters:**
- `accountId` (String): Sender account ID
- `transactionId` (String): Associated TRMS transaction ID
- `amount` (BigDecimal): Payment amount
- `currency` (String): Currency code
- `receiverBIC` (String): Beneficiary bank BIC/SWIFT code
- `beneficiaryName` (String): Beneficiary name
- `beneficiaryAccount` (String): Beneficiary account number

**Returns:** `SwiftMessage`
- Message ID (e.g., "SWIFT-MSG-00001")
- Message type (MT103, MT202, etc.)
- Status (PENDING, SENT, CONFIRMED)
- Timestamp
- Amount and currency

**Implementation:** `SwiftFunctions.java:41`

**Example Use Case:**
```
User: "Send via SWIFT"
AI: Calls sendSwiftPayment() with transaction details from context
```

---

### 7. checkSwiftMessageStatus

**Purpose:** Check status of a SWIFT message

**Description:**
Check the status of a SWIFT message by message ID. Returns detailed status information including whether the message was sent, confirmed, and if it has been reconciled with a TRMS transaction. Status can be: PENDING, SENT, CONFIRMED, FAILED, RECONCILED, or UNRECONCILED. Use this to verify if a payment message was successfully transmitted and acknowledged.

**Parameters:**
- `messageId` (String): SWIFT message ID (e.g., "SWIFT-MSG-00001")

**Returns:** `MessageStatusResponse`
- Message ID
- Current status
- Sent timestamp
- Confirmed timestamp
- Reconciliation status
- Error details (if failed)

**Implementation:** `SwiftFunctions.java:72`

**Example Use Case:**
```
User: "Did the SWIFT message get confirmed?"
AI: Calls checkSwiftMessageStatus("SWIFT-MSG-991")
```

---

### 8. getSwiftMessagesByAccount

**Purpose:** Get all SWIFT messages for an account

**Description:**
Get all SWIFT messages for a specific account. Returns a list of all SWIFT payment messages (MT103, MT202, etc.) that were sent from or received by the specified account. Includes message details such as amount, currency, beneficiary, status, and timestamps. Useful for tracking payment history and reconciliation.

**Parameters:**
- `accountId` (String): Account identifier

**Returns:** `List<SwiftMessage>`
- All SWIFT messages for the account
- Message types, amounts, beneficiaries
- Status and timestamps

**Implementation:** `SwiftFunctions.java:94`

**Example Use Case:**
```
User: "Show me SWIFT messages for ACC-001-USD"
AI: Calls getSwiftMessagesByAccount("ACC-001-USD")
```

---

### 9. getSwiftMessagesByTransaction

**Purpose:** Link SWIFT messages to TRMS transaction

**Description:**
Get SWIFT messages associated with a specific TRMS transaction ID. Returns all SWIFT messages that were sent for a particular transaction. This is useful for verifying that a transaction was communicated via SWIFT and checking the status of the payment instruction.

**Parameters:**
- `transactionId` (String): TRMS transaction identifier

**Returns:** `List<SwiftMessage>`
- SWIFT messages linked to the transaction
- Payment confirmation status

**Implementation:** `SwiftFunctions.java:116`

**Example Use Case:**
```
User: "Find SWIFT messages for transaction TRX-88912"
AI: Calls getSwiftMessagesByTransaction("TRX-88912")
```

---

### 10. reconcileSwiftMessages

**Purpose:** Match SWIFT messages with TRMS transactions

**Description:**
Reconcile SWIFT messages with TRMS transactions. Checks all SWIFT messages and matches them with corresponding transactions in the TRMS system. Reconciliation is based on transaction ID, amount, and currency matching. Returns a summary showing how many messages were reconciled, unreconciled, or pending. Can optionally auto-reconcile messages that have clear matches. Use this for EOD processing or to identify discrepancies.

**Parameters:**
- `accountId` (String): Account to reconcile (optional, null for all)
- `autoReconcile` (boolean): Auto-reconcile clear matches

**Returns:** `ReconciliationResult`
- Total messages checked
- Reconciled count
- Unreconciled count
- Pending count
- Summary message
- Discrepancy details

**Implementation:** `SwiftFunctions.java:139`

**Example Use Case:**
```
User: "Reconcile SWIFT messages"
AI: Calls reconcileSwiftMessages(null, true)
```

---

### 11. getUnreconciledMessages

**Purpose:** Find SWIFT messages needing attention

**Description:**
Get all unreconciled SWIFT messages that need attention. Returns messages with status UNRECONCILED or SENT that haven't been matched with TRMS transactions. These messages may indicate payment discrepancies, missing transaction records, or issues that need manual review. Important for EOD reconciliation checks.

**Parameters:** None

**Returns:** `List<SwiftMessage>`
- Unreconciled SWIFT messages
- Messages requiring manual review

**Implementation:** `SwiftFunctions.java:164`

**Example Use Case:**
```
User: "Check unreconciled SWIFT messages"
AI: Calls getUnreconciledMessages()
```

---

### 12. processRedemptionReport

**Purpose:** Automate redemption report processing (96-page manual entry)

**Description:**
Process a redemption report file from the shared drive. The report contains redemption requests that need to be processed - typically 96 pages of account data including account IDs, beneficiary details, amounts, and currencies. The system reads the file, parses all redemption entries, validates the data, and returns a summary of processed items. Specify the filename in the redemption reports directory. Returns count of successful/failed items and total amount. This automates manual data entry.

**Parameters:**
- `fileName` (String): Report filename (e.g., "redemption_report_latest.csv")

**Returns:** `RedemptionReportResult`
- Total redemptions processed
- Successful count
- Failed count
- Total amount
- Summary message
- Error details

**Implementation:** `SwiftFunctions.java:188`

**File Location:** `backend/swift-mock-app/data/redemption-reports/`

**Example Use Case:**
```
User: "Process the redemption report"
AI: Calls processRedemptionReport("redemption_report_latest.csv")
```

---

### 13. verifyEODReports

**Purpose:** Validate EOD reports in shared drive

**Description:**
Verify End-of-Day reports in the shared drive for a specific date. Checks for the existence and validity of all required EOD reports including balance reports, transaction logs, SWIFT reconciliation reports, and settlement reports. Validates that each report file exists, is not empty, and contains expected data. Returns a detailed verification result showing which reports passed/failed and any issues found. Specify the date in YYYY-MM-DD format. Use this to automate manual EOD report verification.

**Parameters:**
- `reportDate` (String): Date in YYYY-MM-DD format (e.g., "2025-10-09")

**Returns:** `EODReportVerificationResult`
- Total reports checked
- Passed count
- Failed count
- Missing reports list
- Validation errors
- Summary message

**Implementation:** `SwiftFunctions.java:212`

**File Location:** `backend/swift-mock-app/data/eod-reports/`

**Expected Reports:**
- balance_report.csv
- transaction_log.csv
- swift_reconciliation.csv
- settlement_report.csv

**Example Use Case:**
```
User: "Verify today's EOD reports"
AI: Calls verifyEODReports("2025-10-09")
```

---

## Function Call Flow Examples

### Example 1: Complete Payment Workflow

```
1. User: "Show me USD accounts"
   → getAccountsByCurrency("USD")

2. User: "Transfer $50,000 from ACC-001-USD to ACC-002-USD"
   → bookTransaction("ACC-001-USD", "ACC-010-USD", 50000.0, "USD")
   → Returns transaction ID: TRX-88912

3. User: "Send via SWIFT"
   → sendSwiftPayment(accountId, "TRX-88912", amount, currency, BIC, beneficiary...)
   → Returns SWIFT message ID: SWIFT-MSG-991

4. User: "Check if SWIFT message was confirmed"
   → checkSwiftMessageStatus("SWIFT-MSG-991")
   → Returns status: CONFIRMED
```

### Example 2: EOD Processing Workflow

```
1. User: "Can we run EOD today?"
   → checkEODReadiness()
   → Returns: NOT_READY (missing rate fixings)

2. User: "What's missing?"
   → proposeRateFixings()
   → Returns: 2 missing rate fixings (GBP-3M, EUR-6M)

3. User: "Check unreconciled SWIFT messages"
   → getUnreconciledMessages()
   → Returns: 3 unreconciled messages

4. User: "Reconcile SWIFT messages"
   → reconcileSwiftMessages(null, true)
   → Returns: 3 reconciled, 0 unreconciled

5. User: "Verify today's EOD reports"
   → verifyEODReports("2025-10-09")
   → Returns: 4/4 reports verified, all PASSED
```

### Example 3: Redemption Report Processing

```
1. User: "I received the redemption report, can you process it?"
   → processRedemptionReport("redemption_report_latest.csv")
   → Returns: Processed 7 redemptions, $515,000 total
```

---

## Technical Implementation

### Function Registration

All functions are registered as Spring beans using the `@Bean` annotation:

```java
@Bean
@Description("Get accounts filtered by currency...")
public Function<GetAccountsByCurrencyRequest, List<Account>> getAccountsByCurrency() {
    return request -> {
        // Function implementation
    };
}
```

### Request/Response Pattern

Each function uses Java records for type-safe parameters:

```java
// Request record
public record GetAccountsByCurrencyRequest(String currency) {}

// Function signature
Function<GetAccountsByCurrencyRequest, List<Account>>
```

### Error Handling

All functions include try-catch blocks with logging:

```java
try {
    // Call backend service
} catch (Exception e) {
    logger.error("Error in function: {}", e.getMessage());
    throw new RuntimeException("Failed to execute: " + e.getMessage());
}
```

### Backend Integration

Functions delegate to backend clients:

- **TrmsFunctions** → `LegacyTrmsClient` → TRMS Mock (port 8090)
- **SwiftFunctions** → `SwiftClient` → SWIFT Mock (port 8091)

---

## Configuration

### Spring AI Function Calling

Functions are auto-discovered by Spring AI's ChatClient through component scanning:

```java
@Service  // Auto-discovered by Spring
public class TrmsFunctions {

    @Bean  // Registered as function callback
    @Description("...")  // Used by LLM for selection
    public Function<Request, Response> functionName() { ... }
}
```

### System Prompt Integration

The system prompt in `TrmsAiService` lists all available functions:

```
You have access to the following TRMS functions:
- getAccountsByCurrency
- checkAccountBalance
- bookTransaction
- checkEODReadiness
- proposeRateFixings

You also have access to SWIFT messaging functions:
- sendSwiftPayment
- checkSwiftMessageStatus
- getSwiftMessagesByAccount
- getSwiftMessagesByTransaction
- reconcileSwiftMessages
- getUnreconciledMessages
- processRedemptionReport
- verifyEODReports
```

---

## Function Testing

### Manual Testing

Test individual functions via REST API:

```bash
# Start services
./trms-services.sh start

# Test via chat endpoint
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{
    "sessionId": "test-session",
    "message": "Show me all USD accounts"
  }'
```

### Function Execution Logging

All function calls are logged with DEBUG level:

```
AI function call: getAccountsByCurrency with currency: USD
Retrieved 3 accounts for currency: USD
```

---

## Summary

| System | Functions | Primary Use Cases |
|--------|-----------|-------------------|
| **TRMS** | 5 | Account management, transactions, EOD processing |
| **SWIFT** | 8 | Payment messaging, reconciliation, report automation |
| **Total** | 13 | Complete treasury operations automation |

All functions support natural language interaction through Spring AI's ChatClient, enabling conversational workflows for complex treasury operations.
