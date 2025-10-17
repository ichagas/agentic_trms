# TRMS & SWIFT Dashboard Implementation Plan
## Hackathon Demo Enhancement

### 🎯 Goal
Create **2 dedicated dashboard pages** that consolidate real-time backend status for TRMS and SWIFT systems, allowing users to visualize system state changes during AI agent interactions.

---

## 📋 Requirements Analysis

### TRMS Dashboard Page
**Route:** `/dashboard/trms`

**Components to Display:**
1. **Accounts Overview**
   - List of all accounts with balances (USD, EUR, GBP, JPY)
   - Real-time balance updates after transactions
   - Color-coded by currency
   - Quick balance totals per currency

2. **Transaction Listing**
   - Recent booked CASH transactions
   - Show: Transaction ID, From/To accounts, Amount, Currency, Status, Timestamp
   - Highlight recent transactions (last 5 minutes)
   - Auto-refresh when new transactions occur

3. **Pending Validations**
   - Transactions waiting for validation
   - "2-step validation" indicator (AI proposed → Human approved)
   - Action buttons for manual approval/rejection
   - Pending count badge

4. **EOD Status Panel**
   - Ready/Not Ready indicator with color
   - Market data completeness status
   - Missing rate fixings count
   - Blocking issues list
   - Last check timestamp

### SWIFT Dashboard Page
**Route:** `/dashboard/swift`

**Components to Display:**
1. **Daily EOD Checks**
   - Settlement results received (✓/✗)
   - Settlement links status (✓/✗)
   - All positions settled (✓/✗)
   - Mismatch detection (count)
   - Overall readiness score

2. **SWIFT Message Status**
   - Recent SWIFT messages (MT103, MT202)
   - Status: PENDING → SENT → CONFIRMED → RECONCILED
   - Message ID, Account, Amount, Beneficiary
   - Visual flow indicator showing status progression

3. **Reconciliation Status**
   - Total messages
   - Reconciled count (✓)
   - Unreconciled count (!)
   - Pending count (⏳)
   - Last reconciliation timestamp

4. **Reports Status**
   - EOD reports in G Drive
   - Report verification status (✓/✗)
   - Files: balance_report, transaction_log, swift_reconciliation, settlement_report
   - File timestamps and sizes

5. **Redemption Report Status**
   - Latest report filename
   - Status: Received → Verified → Processed
   - Mismatch count (if any)
   - Total redemptions processed
   - Total amount

---

## 🏗️ Architecture Design

### Minimal Changes Approach

#### Backend Changes (Minimal)
**No new endpoints needed!** Reuse existing functions:

**TRMS Dashboard Data:**
- `GET /api/v1/accounts` (already exists in TRMS mock)
- `GET /api/v1/transactions` (already exists in TRMS mock)
- `GET /api/v1/eod/status` (already exists in TRMS mock)
- AI function: `checkEODReadiness()`
- AI function: `proposeRateFixings()`

**SWIFT Dashboard Data:**
- `GET /api/v1/swift/messages` (already exists in SWIFT mock)
- `GET /api/v1/swift/reconciliation/status` (already exists in SWIFT mock)
- AI function: `getUnreconciledMessages()`
- AI function: `verifyEODReports()`
- AI function: `processRedemptionReport()`

#### Frontend Changes

**New Files to Create:**
```
frontend/src/
├── components/
│   └── dashboards/
│       ├── TrmsDashboard.jsx          (NEW)
│       ├── SwiftDashboard.jsx         (NEW)
│       ├── AccountsPanel.jsx          (NEW)
│       ├── TransactionsPanel.jsx      (NEW)
│       ├── EODStatusPanel.jsx         (NEW)
│       ├── SwiftMessagesPanel.jsx     (NEW)
│       ├── ReconciliationPanel.jsx    (NEW)
│       ├── ReportsPanel.jsx           (NEW)
│       └── RedemptionPanel.jsx        (NEW)
├── hooks/
│   ├── useTrmsData.js                 (NEW - polling hook)
│   └── useSwiftData.js                (NEW - polling hook)
└── App.jsx                            (MODIFY - add routes)
```

**Existing Files to Modify:**
- `App.jsx` - Add routing for `/dashboard/trms` and `/dashboard/swift`
- `Header.jsx` - Add navigation links to dashboards

---

## 📐 Implementation Plan

### Phase 1: Setup & Routing (30 min)

**Step 1.1: Install React Router**
```bash
cd frontend
npm install react-router-dom
```

**Step 1.2: Update App.jsx**
Add routing support:
```jsx
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import TrmsDashboard from './components/dashboards/TrmsDashboard';
import SwiftDashboard from './components/dashboards/SwiftDashboard';

// Wrap app with BrowserRouter
// Add routes for /dashboard/trms and /dashboard/swift
```

**Step 1.3: Update Header.jsx**
Add dashboard navigation:
```jsx
<nav>
  <Link to="/">Chat</Link>
  <Link to="/dashboard/trms">TRMS Dashboard</Link>
  <Link to="/dashboard/swift">SWIFT Dashboard</Link>
</nav>
```

---

### Phase 2: TRMS Dashboard (2 hours)

**Step 2.1: Create Data Fetching Hook (useTrmsData.js)**
```javascript
export function useTrmsData() {
  const [accounts, setAccounts] = useState([]);
  const [transactions, setTransactions] = useState([]);
  const [eodStatus, setEodStatus] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Fetch data every 5 seconds
    const interval = setInterval(fetchData, 5000);
    fetchData(); // Initial fetch
    return () => clearInterval(interval);
  }, []);

  const fetchData = async () => {
    // GET http://localhost:8090/api/v1/accounts
    // GET http://localhost:8090/api/v1/transactions
    // GET http://localhost:8090/api/v1/eod/status
  };

  return { accounts, transactions, eodStatus, loading, refresh: fetchData };
}
```

**Step 2.2: Create AccountsPanel Component**
```jsx
function AccountsPanel({ accounts }) {
  // Group accounts by currency
  // Display in card layout
  // Show balance totals
  // Highlight changes (use flash animation)
}
```

**Step 2.3: Create TransactionsPanel Component**
```jsx
function TransactionsPanel({ transactions }) {
  // Table with: ID, From, To, Amount, Currency, Status, Time
  // Highlight recent (< 5 min) with green background
  // Sort by timestamp (newest first)
  // Limit to last 20 transactions
}
```

**Step 2.4: Create EODStatusPanel Component**
```jsx
function EODStatusPanel({ eodStatus }) {
  // Big status indicator: READY (green) / NOT READY (red)
  // List of checks: Market Data (✓), Transactions (✓), Rate Fixings (✗)
  // Blocking issues list
  // Last check timestamp
}
```

**Step 2.5: Assemble TrmsDashboard**
```jsx
export default function TrmsDashboard() {
  const { accounts, transactions, eodStatus, loading, refresh } = useTrmsData();

  return (
    <div className="dashboard-grid">
      <AccountsPanel accounts={accounts} />
      <TransactionsPanel transactions={transactions} />
      <EODStatusPanel eodStatus={eodStatus} />
      <button onClick={refresh}>Refresh Now</button>
    </div>
  );
}
```

---

### Phase 3: SWIFT Dashboard (2 hours)

**Step 3.1: Create Data Fetching Hook (useSwiftData.js)**
```javascript
export function useSwiftData() {
  const [messages, setMessages] = useState([]);
  const [reconciliation, setReconciliation] = useState(null);
  const [reports, setReports] = useState(null);
  const [redemption, setRedemption] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const interval = setInterval(fetchData, 5000);
    fetchData();
    return () => clearInterval(interval);
  }, []);

  const fetchData = async () => {
    // GET http://localhost:8091/api/v1/swift/messages
    // GET http://localhost:8091/api/v1/swift/reconciliation/status
    // Call AI functions via chat API (optional)
  };

  return { messages, reconciliation, reports, redemption, loading, refresh: fetchData };
}
```

**Step 3.2: Create SwiftMessagesPanel**
```jsx
function SwiftMessagesPanel({ messages }) {
  // Table: Message ID, Type, Account, Amount, Status, Timestamp
  // Status progression visual: PENDING → SENT → CONFIRMED → RECONCILED
  // Color-coded by status
  // Filter by status (dropdown)
}
```

**Step 3.3: Create ReconciliationPanel**
```jsx
function ReconciliationPanel({ reconciliation }) {
  // Pie chart or progress bar:
  //   - Reconciled (green)
  //   - Unreconciled (red)
  //   - Pending (yellow)
  // Total count
  // Last reconciliation timestamp
  // "Reconcile Now" action button
}
```

**Step 3.4: Create ReportsPanel**
```jsx
function ReportsPanel({ reports }) {
  // List of required EOD reports:
  //   - balance_report.csv (✓ / ✗)
  //   - transaction_log.csv (✓ / ✗)
  //   - swift_reconciliation.csv (✓ / ✗)
  //   - settlement_report.csv (✓ / ✗)
  // File sizes and timestamps
  // Overall verification status
}
```

**Step 3.5: Create RedemptionPanel**
```jsx
function RedemptionPanel({ redemption }) {
  // Status flow: Received → Verified → Processed
  // Current status indicator
  // Filename
  // Total redemptions processed
  // Total amount
  // Mismatch count (if any)
}
```

**Step 3.6: Assemble SwiftDashboard**
```jsx
export default function SwiftDashboard() {
  const { messages, reconciliation, reports, redemption, loading, refresh } = useSwiftData();

  return (
    <div className="dashboard-grid">
      <SwiftMessagesPanel messages={messages} />
      <ReconciliationPanel reconciliation={reconciliation} />
      <ReportsPanel reports={reports} />
      <RedemptionPanel redemption={redemption} />
      <button onClick={refresh}>Refresh Now</button>
    </div>
  );
}
```

---

### Phase 4: 2-Step Validation Feature (1 hour)

**Goal:** Show AI agent proposal → Human approval workflow

**Backend Addition (Optional):**
If you want true validation, add to TRMS mock:
```java
POST /api/v1/transactions/{id}/approve
POST /api/v1/transactions/{id}/reject
```

**Frontend Implementation:**
```jsx
function PendingValidationsPanel({ pendingTransactions, onApprove, onReject }) {
  return (
    <div>
      {pendingTransactions.map(tx => (
        <div key={tx.id} className="pending-transaction">
          <div>AI Proposed: {tx.description}</div>
          <div>Amount: {tx.amount} {tx.currency}</div>
          <button onClick={() => onApprove(tx.id)}>✓ Approve</button>
          <button onClick={() => onReject(tx.id)}>✗ Reject</button>
        </div>
      ))}
    </div>
  );
}
```

**For Hackathon (Simplified):**
- Show transactions with status "PENDING_APPROVAL"
- Buttons just update UI locally (don't need backend)
- Simulate approval with local state change

---

### Phase 5: Real-Time Updates (30 min)

**Option A: Polling (Simplest)**
Already implemented in hooks with `setInterval`

**Option B: WebSocket (If time permits)**
Add WebSocket connection to get instant updates

**Recommended for Hackathon:** Use polling every 3-5 seconds

---

### Phase 6: Styling & Polish (1 hour)

**Dashboard Layout:**
```css
.dashboard-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 20px;
  padding: 20px;
}

.dashboard-panel {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.status-indicator {
  display: inline-block;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  margin-right: 8px;
}

.status-ready { background-color: #10b981; }
.status-not-ready { background-color: #ef4444; }
.status-pending { background-color: #f59e0b; }
```

**Animations:**
```javascript
// Highlight new transactions
function TransactionRow({ transaction, isNew }) {
  return (
    <tr className={isNew ? 'flash-green' : ''}>
      {/* ... */}
    </tr>
  );
}
```

---

## 🎬 Demo Flow

### Scenario: Transfer Money Between Accounts

**Step 1:** Open TRMS Dashboard
- Shows ACC-001-USD with balance: $1,000,000
- Shows ACC-002-USD with balance: $500,000

**Step 2:** Open Chat (separate tab/window)
- User: "Transfer $50,000 from ACC-001-USD to ACC-002-USD"
- AI books transaction

**Step 3:** Return to TRMS Dashboard (auto-refresh)
- ACC-001-USD balance: $950,000 (highlighted in green)
- ACC-002-USD balance: $550,000 (highlighted in green)
- New transaction appears in Transaction Listing

**Step 4:** Continue in Chat
- User: "Send via SWIFT"
- AI sends SWIFT message

**Step 5:** Open SWIFT Dashboard
- New SWIFT message appears with status: SENT
- Reconciliation count updates

### Scenario: EOD Processing

**Step 1:** Open SWIFT Dashboard
- EOD Status shows "NOT READY"
- Missing: 2 rate fixings

**Step 2:** Chat
- User: "Prepare for EOD"
- AI proposes rate fixings

**Step 3:** TRMS Dashboard
- EOD Status updates to "READY" (green)
- Rate fixings resolved

**Step 4:** SWIFT Dashboard
- All EOD checks: ✓
- Reports verified: 4/4

---

## 📊 Data Flow Diagram

```
User Chat Input
     ↓
AI Agent (trms-ai-backend:8080)
     ↓
Function Calls
     ├─→ TRMS Mock (8090) → Updates State
     └─→ SWIFT Mock (8091) → Updates State
              ↓
         Backend State Changes
              ↓
    Dashboards Poll Every 5s
              ↓
     UI Updates with Highlights
```

---

## 🚀 Implementation Timeline

**Total Estimated Time: 6-7 hours**

| Phase | Task | Time | Priority |
|-------|------|------|----------|
| 1 | Setup & Routing | 30 min | HIGH |
| 2 | TRMS Dashboard | 2 hours | HIGH |
| 3 | SWIFT Dashboard | 2 hours | HIGH |
| 4 | 2-Step Validation | 1 hour | MEDIUM |
| 5 | Real-Time Updates | 30 min | HIGH |
| 6 | Styling & Polish | 1 hour | MEDIUM |

**Hackathon Priority:**
1. Phase 1-2: TRMS Dashboard (essential)
2. Phase 3: SWIFT Dashboard (essential)
3. Phase 5: Polling updates (essential)
4. Phase 6: Basic styling (nice to have)
5. Phase 4: 2-step validation (if time permits)

---

## ✅ Advantages of This Approach

1. **No Breaking Changes:** Existing chat interface untouched
2. **Minimal Backend Work:** Reuses existing endpoints
3. **Easy to Demo:** Side-by-side windows showing real-time updates
4. **Hackathon Friendly:** Can build incrementally
5. **Visually Impressive:** Shows AI → Backend → UI flow clearly
6. **Production Path:** Can evolve into real monitoring dashboards

---

## 🔧 Technical Decisions

### Why Not Real-Time WebSocket?
- **Reason:** Adds complexity for hackathon
- **Alternative:** Polling every 5 seconds is simple and works
- **Future:** Can upgrade to WebSocket later

### Why Separate Routes?
- **Reason:** Clean separation of concerns
- **Benefit:** Can demo chat + dashboards simultaneously
- **UX:** Users can open dashboards in separate browser tabs

### Why Not Modify Backend?
- **Reason:** Existing REST APIs have all needed data
- **Benefit:** Zero backend changes = less risk
- **Exception:** Optional approval endpoints for 2-step validation

---

## 📦 Deliverables

### For Hackathon Judges:
1. **Live Demo:**
   - Split screen: Chat on left, Dashboard on right
   - Execute transfer → Watch balances update in real-time
   - Show EOD workflow → See status panels update

2. **Screenshots:**
   - Before/after transaction comparison
   - SWIFT message status progression
   - EOD readiness checks

3. **Story:**
   - "Watch how AI agents integrate with legacy systems"
   - "Real-time visibility into financial operations"
   - "2-step AI + Human validation for compliance"

---

## 🎯 Success Criteria

**Dashboard Must Show:**
- ✅ Real-time data updates (via polling)
- ✅ Visual feedback when state changes
- ✅ Clear status indicators (colors, icons)
- ✅ Professional financial UI design
- ✅ Works side-by-side with chat interface

**Demo Must Demonstrate:**
- ✅ AI agent triggers backend state changes
- ✅ Dashboards reflect changes within 5 seconds
- ✅ Multiple systems (TRMS + SWIFT) coordinate
- ✅ EOD workflow completion
- ✅ Transaction + SWIFT message lifecycle

---

## 🚧 Risk Mitigation

**Risk 1:** Polling too frequent → Backend overload
- **Mitigation:** 5-second interval, only poll when dashboard visible

**Risk 2:** Data doesn't refresh
- **Mitigation:** Add manual "Refresh" button as backup

**Risk 3:** Running out of time
- **Mitigation:** Build TRMS dashboard first (higher priority), SWIFT second

**Risk 4:** Styling takes too long
- **Mitigation:** Use Tailwind CSS utility classes for rapid styling

---

## 📝 Next Steps

1. **Approve plan** ✓
2. **Start with Phase 1** (routing setup)
3. **Build TRMS Dashboard** (highest value)
4. **Build SWIFT Dashboard**
5. **Test demo scenarios**
6. **Polish and practice presentation**

---

**Ready to build? Let's start with Phase 1!** 🚀
