# Multi-Function Workflow Support

## Overview

The TRMS AI backend now supports **multi-function workflows** that execute multiple operations in sequence. These workflows combine multiple API calls to automate complex business processes.

## Problem Solved

**Before**: The system could only execute ONE function per request
- "Show USD accounts" ✅ (single function)
- "Transfer money AND send via SWIFT" ❌ (would only do transfer, not SWIFT)

**After**: The system can execute MULTIPLE functions in one request
- "Transfer $75,000 from ACC-001-USD to ACC-002-USD and send via SWIFT" ✅ (2 functions)
- "Run comprehensive EOD check" ✅ (3 functions: EOD readiness + SWIFT reconciliation + Report verification)

---

## Supported Multi-Function Workflows

### 1. Transfer + SWIFT Payment Workflow

**Trigger Keywords**: `transfer` + `swift` + amount

**Example Queries**:
- "Transfer $75,000 from ACC-001-USD to ACC-002-USD and send payment via SWIFT"
- "Send $50,000 via SWIFT from ACC-003-USD"

**Functions Executed**:
1. `bookTransaction` - Books the transfer in TRMS
2. `sendSwiftPayment` - Sends MT103 SWIFT message

**Output**:
```
💸 EXECUTING TRANSFER + SWIFT PAYMENT WORKFLOW

📝 Transfer Details Extracted:
   From: ACC-001-USD
   Amount: 75,000
   Currency: USD

💰 Step 1/2: Booking transaction in TRMS...
   Transaction ID: TXN-1728838453000

📤 Step 2/2: Sending SWIFT payment message...
   SWIFT Message ID: SWIFT-MSG-1728838453000
   Status: SENT

✅ WORKFLOW COMPLETED SUCCESSFULLY
```

---

### 2. Comprehensive EOD Check Workflow

**Trigger Keywords**: `comprehensive` OR `full` OR `complete` + `eod` OR `check`

**Example Queries**:
- "Run comprehensive EOD check including market data, transactions, and SWIFT reconciliation"
- "Do a full EOD check"
- "Complete EOD readiness check"

**Functions Executed**:
1. `checkEODReadiness` - Check TRMS EOD status (market data, transactions, rate fixings)
2. `getUnreconciledMessages` - Check SWIFT reconciliation status
3. `verifyEODReports` - Verify EOD reports in shared drive

**Output**: Shows complete EOD status across all systems

---

### 3. SWIFT Reconciliation + EOD Check Workflow

**Trigger Keywords**: `reconcile` + `eod` OR `check`

**Example Queries**:
- "Reconcile SWIFT messages and check EOD readiness"
- "Check if we can run EOD after reconciliation"

**Functions Executed**:
1. `getUnreconciledMessages` - Identify unreconciled SWIFT messages
2. `reconcileSwiftMessages` - Attempt automatic reconciliation
3. `checkEODReadiness` - Verify EOD readiness after reconciliation

---

### 4. Complete EOD Preparation Workflow

**Trigger Keywords**: `prepare` + `eod`

**Example Queries**:
- "Prepare for End-of-Day processing"
- "Prepare EOD"

**Functions Executed**:
1. `checkEODReadiness` - Check current EOD status
2. `proposeRateFixings` - Generate rate fixing proposals for missing resets
3. `checkEODReadiness` - Re-check to see improvements

**Use Case**: Automated EOD preparation with rate fixing proposals

---

### 5. Cross-Currency Portfolio Analysis

**Trigger Keywords**: `portfolio` OR `cash position` OR `all currencies` + `currency`

**Example Queries**:
- "Show me our complete cash position across all currencies"
- "Portfolio analysis for all currencies"

**Functions Executed**:
1. `getAccountsByCurrency("USD")` - Get all USD accounts
2. `getAccountsByCurrency("EUR")` - Get all EUR accounts
3. `getAccountsByCurrency("GBP")` - Get all GBP accounts
4. `getAccountsByCurrency("JPY")` - Get all JPY accounts

**Output**: Consolidated view of accounts across USD, EUR, GBP, JPY

---

### 6. EOD Issue Resolution Workflow

**Trigger Keywords**: (`what` OR `fix` OR `resolve`) + `blocking` + `eod`

**Example Queries**:
- "What's blocking EOD and how can we fix it?"
- "Resolve EOD blockers"

**Functions Executed**:
1. `checkEODReadiness` - Identify what's blocking EOD
2. `proposeRateFixings` - Generate solutions for rate fixing issues
3. `checkEODReadiness` - Verify issues were resolved

---

### 7. SWIFT EOD Validation Workflow

**Trigger Keywords**: `swift` + `eod` OR `readiness` OR `validate`

**Example Queries**:
- "Validate SWIFT system for EOD"
- "Is SWIFT ready for End-of-Day?"
- "Check SWIFT EOD readiness"

**Functions Executed**:
1. `verifyEODReports` - Check EOD reports in shared drive
2. `getUnreconciledMessages` - Check for unreconciled SWIFT messages
3. SWIFT settlement verification

**Output**: Comprehensive SWIFT system validation with pass/fail for each check

---

### 8. Show Accounts + Check Balances Workflow

**Trigger Keywords**: `show` + `account` + `balance`

**Example Queries**:
- "Show me all USD accounts with their balances"
- "Display accounts and balances for EUR"

**Functions Executed**:
1. `getAccountsByCurrency` - Get accounts for specified currency
2. Balance information included in account data

---

## How It Works

### Pattern Matching Order

The system checks for multi-function workflows **BEFORE** single-function calls:

1. Check for multi-function workflow patterns (Scenarios 1-8)
2. If no workflow matches, check for single function patterns
3. If no function matches, let AI generate conversational response

### Execution Tracking

All workflows track executed functions:
```java
executedFunctions.get().add("checkEODReadiness");
executedFunctions.get().add("proposeRateFixings");
```

This allows the frontend to display which functions were called.

---

## Testing Multi-Function Workflows

### Test 1: Transfer + SWIFT
```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Transfer $75,000 from ACC-001-USD to ACC-002-USD and send via SWIFT",
    "sessionId": "test-session-1"
  }'
```

**Expected**: Should see 2 functions executed: `bookTransaction`, `sendSwiftPayment`

### Test 2: Comprehensive EOD Check
```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Run comprehensive EOD check",
    "sessionId": "test-session-2"
  }'
```

**Expected**: Should see 3 functions executed: `checkEODReadiness`, `getUnreconciledMessages`, `verifyEODReports`

### Test 3: Cross-Currency Portfolio
```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Show me cash position across all currencies",
    "sessionId": "test-session-3"
  }'
```

**Expected**: Should see 4 functions executed: `getAccountsByCurrency` (4 times for USD, EUR, GBP, JPY)

---

## Frontend Quick Actions

The frontend includes these multi-function queries as quick action buttons:

1. **💸 Payment Flow**: "Transfer $75,000 from ACC-001-USD to ACC-002-USD and send payment via SWIFT"
2. **🔍 Full Check**: "Run comprehensive EOD check including market data, transactions, and SWIFT reconciliation"
3. **🔄 SWIFT Status**: "Check unreconciled SWIFT messages and reconcile them automatically"

---

## Adding New Workflows

To add a new multi-function workflow:

### Step 1: Add Pattern Matcher
In `TrmsAiService.java`, add a new condition in `tryExecuteFunction()`:

```java
// Scenario 9: Your New Workflow
if (message.contains("keyword1") && message.contains("keyword2")) {
    logger.info("Executing Your New Workflow");
    return executeYourNewWorkflow(message);
}
```

### Step 2: Implement Workflow Method
Add the workflow implementation:

```java
private String executeYourNewWorkflow(String message) {
    StringBuilder result = new StringBuilder();
    result.append("🚀 YOUR NEW WORKFLOW\n\n");

    try {
        // Step 1: Execute first function
        result.append("Step 1/3: ...\n");
        String step1Result = executeFirstFunction();
        result.append(step1Result).append("\n\n");

        // Step 2: Execute second function
        result.append("Step 2/3: ...\n");
        String step2Result = executeSecondFunction();
        result.append(step2Result).append("\n\n");

        // Step 3: Execute third function
        result.append("Step 3/3: ...\n");
        String step3Result = executeThirdFunction();
        result.append(step3Result).append("\n\n");

        result.append("✅ WORKFLOW COMPLETED\n");

    } catch (Exception e) {
        result.append("❌ Error: ").append(e.getMessage());
    }

    return result.toString();
}
```

### Step 3: Update Frontend
Add a new quick action button in `SuggestedActions.jsx`:

```javascript
{
  id: 'your-workflow',
  label: '🚀 Your Workflow',
  query: 'Your trigger query that matches pattern'
}
```

---

## Limitations

1. **Pattern-based**: Workflows are triggered by keyword patterns, not true NLU
2. **Sequential Execution**: Functions run in sequence, not parallel
3. **No Conditional Logic**: Workflows can't branch based on results (yet)
4. **Limited Error Handling**: If one step fails, the workflow continues

---

## Future Enhancements

1. **Dynamic Function Chaining**: Let AI decide which functions to call and in what order
2. **Conditional Workflows**: Branch based on function results
3. **Parallel Execution**: Execute independent functions simultaneously
4. **Rollback Support**: Undo changes if workflow fails mid-execution
5. **User Confirmation**: Ask user before executing multi-step workflows

---

## Summary

The system now supports **8 multi-function workflows** that automate complex business processes:

✅ Transfer + SWIFT Payment
✅ Comprehensive EOD Check
✅ SWIFT Reconciliation + EOD Check
✅ Complete EOD Preparation
✅ Cross-Currency Portfolio Analysis
✅ EOD Issue Resolution
✅ SWIFT EOD Validation
✅ Show Accounts + Balances

These workflows significantly reduce the number of manual steps required for common operations!
