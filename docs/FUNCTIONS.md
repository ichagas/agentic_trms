TRMS System Functions (TrmsFunctions.java) - 5 Functions

| Function              | Purpose                                   | Key Parameters                           | Returns                                   |
|-----------------------|-------------------------------------------|------------------------------------------|-------------------------------------------|
| getAccountsByCurrency | Filter accounts by currency code          | currency (USD, EUR, GBP, JPY)            | List of accounts with balances            |
| checkAccountBalance   | Get detailed balance for specific account | accountId                                | Account balance with pending transactions |
| bookTransaction       | Process transfer between accounts         | fromAccount, toAccount, amount, currency | Transaction details with ID and status    |
| checkEODReadiness     | Verify system ready for End-of-Day        | None                                     | EOD status with blocking issues           |
| proposeRateFixings    | Get missing interest rate resets          | None                                     | List of rate fixings needed for EOD       |

---
SWIFT System Functions (SwiftFunctions.java) - 8 Functions

| Function                      | Purpose                                  | Key Parameters                                  | Returns                                            |
|-------------------------------|------------------------------------------|-------------------------------------------------|----------------------------------------------------|
| sendSwiftPayment              | Send MT103 payment message               | accountId, tranId, amount, ccy, recBIC, details | SWIFT message with ID and status                   |
| checkSwiftMessageStatus       | Check status of SWIFT message            | messageId                                       | Status: PENDING, SENT, CONFIRMED, RECONCILED, etc. |
| getSwiftMessagesByAccount     | Get all messages for an account          | accountId                                       | List of SWIFT messages (MT103, MT202)              |
| getSwiftMessagesByTransaction | Link SWIFT messages to TRMS transaction  | transactionId                                   | SWIFT messages for that transaction.               |
| reconcileSwiftMessages        | Match SWIFT with TRMS transactions       | accountId, autoReconcile flag                   | Reconciliation summary (matched/unmatched)         |
| getUnreconciledMessages       | Find messages needing attention          | None                                            | Unreconciled SWIFT messages                        |
| processRedemptionReport       | Automate 96-page redemption report entry | fileName                                        | Summary: count, failed, total amount.              |
| verifyEODReports              | Validate EOD reports in shared drive     | reportDate (YYYY-MM-DD)                         | Verification result (passed/failed checks)         |
