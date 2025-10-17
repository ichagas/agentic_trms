# TRMS & SWIFT Dashboards - Implementation Tasks

## 🎯 Project Goal

Create two dedicated dashboard pages that provide real-time visibility into TRMS and SWIFT backend systems, allowing hackathon judges to see how AI agent interactions trigger immediate state changes across multiple financial systems.

**Success Criteria:**
- Users can see account balances update in real-time when AI books transactions
- SWIFT message status progression is visible as payments flow through the system
- EOD readiness status updates as AI resolves blocking issues
- All updates happen automatically via polling (no manual refresh needed)
- Demo-ready for side-by-side presentation (chat + dashboards)

---

## 📋 Implementation Tasks

### Phase 1: Project Setup & Routing (30 minutes)

#### Task 1.1: Install Dependencies
- [ ] Navigate to frontend directory: `cd frontend`
- [ ] Install React Router: `npm install react-router-dom`
- [ ] Verify installation in package.json

#### Task 1.2: Create Folder Structure
- [ ] Create `src/components/dashboards/` directory
- [ ] Create `src/hooks/` directory (if doesn't exist)
- [ ] Create `src/pages/` directory for dashboard pages

#### Task 1.3: Setup Routing in App.jsx
- [ ] Import BrowserRouter, Routes, Route from react-router-dom
- [ ] Wrap app content with `<BrowserRouter>`
- [ ] Create Routes for:
  - `/` - Existing chat interface
  - `/dashboard/trms` - TRMS Dashboard
  - `/dashboard/swift` - SWIFT Dashboard
- [ ] Test routing with placeholder components

#### Task 1.4: Add Navigation Header
- [ ] Update Header.jsx to include navigation links
- [ ] Add links: Chat | TRMS Dashboard | SWIFT Dashboard
- [ ] Style navigation with Tailwind CSS (matches existing theme)
- [ ] Test navigation between all routes

**Deliverable:** Working navigation between chat and two dashboard routes

---

### Phase 2: TRMS Dashboard (2 hours)

#### Task 2.1: Create Data Fetching Hook (45 min)
- [ ] Create `src/hooks/useTrmsData.js`
- [ ] Implement state management for:
  - `accounts` - List of all accounts with balances
  - `transactions` - Recent transaction list
  - `eodStatus` - EOD readiness status
  - `loading` - Loading state
- [ ] Implement polling with `useEffect`:
  - Interval: every 5 seconds
  - Clear interval on unmount
- [ ] Fetch data from TRMS mock API:
  - `GET http://localhost:8090/api/v1/accounts`
  - `GET http://localhost:8090/api/v1/transactions`
  - `GET http://localhost:8090/api/v1/eod/status`
- [ ] Add error handling for failed requests
- [ ] Add manual refresh function
- [ ] Test hook with console.log to verify data fetching

#### Task 2.2: Create AccountsPanel Component (30 min)
- [ ] Create `src/components/dashboards/AccountsPanel.jsx`
- [ ] Display accounts grouped by currency (USD, EUR, GBP, JPY)
- [ ] Show for each account:
  - Account ID
  - Account name
  - Current balance
  - Currency
- [ ] Add total balance per currency section
- [ ] Implement flash animation for balance changes (green highlight)
- [ ] Style with Tailwind CSS cards
- [ ] Test with mock data

#### Task 2.3: Create TransactionsPanel Component (30 min)
- [ ] Create `src/components/dashboards/TransactionsPanel.jsx`
- [ ] Display transaction table with columns:
  - Transaction ID
  - From Account
  - To Account
  - Amount
  - Currency
  - Status (badge with color)
  - Timestamp
- [ ] Highlight recent transactions (< 5 minutes) with green background
- [ ] Sort by timestamp (newest first)
- [ ] Limit display to last 20 transactions
- [ ] Add empty state for no transactions
- [ ] Test with mock data

#### Task 2.4: Create EODStatusPanel Component (15 min)
- [ ] Create `src/components/dashboards/EODStatusPanel.jsx`
- [ ] Display large status indicator:
  - READY (green circle + text)
  - NOT READY (red circle + text)
- [ ] Show EOD check details:
  - Market data status (✓/✗)
  - Transaction validation (✓/✗)
  - Rate fixings status (✓/✗)
- [ ] List blocking issues (if any)
- [ ] Show last check timestamp
- [ ] Test with different status states

#### Task 2.5: Assemble TrmsDashboard Page (15 min)
- [ ] Create `src/pages/TrmsDashboard.jsx`
- [ ] Import and use `useTrmsData` hook
- [ ] Layout panels in responsive grid:
  - Accounts Panel (top)
  - Transactions Panel (middle)
  - EOD Status Panel (bottom right)
- [ ] Add "Refresh Now" button (calls manual refresh)
- [ ] Add loading spinner while data loads
- [ ] Test complete dashboard with polling

**Deliverable:** Fully functional TRMS Dashboard showing real-time account, transaction, and EOD data

---

### Phase 3: SWIFT Dashboard (2 hours)

#### Task 3.1: Create Data Fetching Hook (45 min)
- [ ] Create `src/hooks/useSwiftData.js`
- [ ] Implement state management for:
  - `messages` - SWIFT messages list
  - `reconciliationStatus` - Reconciliation summary
  - `eodReports` - EOD report verification
  - `redemptionStatus` - Redemption report status
  - `loading` - Loading state
- [ ] Implement polling with `useEffect` (5 second interval)
- [ ] Fetch data from SWIFT mock API:
  - `GET http://localhost:8091/api/v1/swift/messages`
  - `GET http://localhost:8091/api/v1/swift/reconciliation/status`
- [ ] Add error handling
- [ ] Add manual refresh function
- [ ] Test hook with console.log

#### Task 3.2: Create SwiftMessagesPanel Component (30 min)
- [ ] Create `src/components/dashboards/SwiftMessagesPanel.jsx`
- [ ] Display SWIFT messages table with columns:
  - Message ID
  - Message Type (MT103, MT202, etc.)
  - Account
  - Amount
  - Currency
  - Status (with visual progression)
  - Timestamp
- [ ] Implement status progression visual:
  - PENDING (gray)
  - SENT (blue)
  - CONFIRMED (yellow)
  - RECONCILED (green)
- [ ] Add status filter dropdown
- [ ] Sort by timestamp (newest first)
- [ ] Test with mock data

#### Task 3.3: Create ReconciliationPanel Component (20 min)
- [ ] Create `src/components/dashboards/ReconciliationPanel.jsx`
- [ ] Display reconciliation summary:
  - Total messages count
  - Reconciled count (green badge)
  - Unreconciled count (red badge)
  - Pending count (yellow badge)
- [ ] Add simple progress bar or pie chart visual
- [ ] Show last reconciliation timestamp
- [ ] Add "Reconcile Now" button (optional, can be placeholder)
- [ ] Test with different reconciliation states

#### Task 3.4: Create ReportsPanel Component (20 min)
- [ ] Create `src/components/dashboards/ReportsPanel.jsx`
- [ ] Display EOD reports checklist:
  - balance_report.csv (✓/✗)
  - transaction_log.csv (✓/✗)
  - swift_reconciliation.csv (✓/✗)
  - settlement_report.csv (✓/✗)
- [ ] Show file timestamps and sizes (if available)
- [ ] Display overall verification status
- [ ] Add verification timestamp
- [ ] Test with verified and unverified states

#### Task 3.5: Create RedemptionPanel Component (20 min)
- [ ] Create `src/components/dashboards/RedemptionPanel.jsx`
- [ ] Display redemption report status flow:
  - Status: Received → Verified → Processed
  - Current status indicator (highlight current step)
- [ ] Show redemption details:
  - Filename
  - Total redemptions processed
  - Total amount
  - Mismatch count (if any)
- [ ] Add timestamp of last processing
- [ ] Test with different status states

#### Task 3.6: Assemble SwiftDashboard Page (15 min)
- [ ] Create `src/pages/SwiftDashboard.jsx`
- [ ] Import and use `useSwiftData` hook
- [ ] Layout panels in responsive grid:
  - SWIFT Messages Panel (left, full height)
  - Reconciliation Panel (top right)
  - Reports Panel (middle right)
  - Redemption Panel (bottom right)
- [ ] Add "Refresh Now" button
- [ ] Add loading spinner
- [ ] Test complete dashboard with polling

**Deliverable:** Fully functional SWIFT Dashboard showing real-time message status, reconciliation, and reports

---

### Phase 4: Real-Time Updates & Polish (1 hour)

#### Task 4.1: Implement Change Detection (20 min)
- [ ] Add previous state tracking in useTrmsData hook
- [ ] Compare new data with previous data
- [ ] Mark changed items with `isNew` or `isChanged` flag
- [ ] Pass change flags to components

#### Task 4.2: Add Visual Feedback Animations (20 min)
- [ ] Create CSS animation for balance changes (flash green)
- [ ] Create CSS animation for new transactions (slide in)
- [ ] Create CSS animation for status updates (pulse)
- [ ] Add transition classes to Tailwind config
- [ ] Test animations with manual data changes

#### Task 4.3: Optimize Polling (10 min)
- [ ] Only poll when dashboard is visible (use `document.visibilityState`)
- [ ] Stop polling when user navigates away
- [ ] Add retry logic for failed requests
- [ ] Log polling activity to console (debug mode)

#### Task 4.4: Final Styling & Responsive Design (10 min)
- [ ] Ensure all panels have consistent spacing
- [ ] Test responsive layout on different screen sizes
- [ ] Add proper color scheme (matches TRMS branding)
- [ ] Ensure proper contrast for accessibility
- [ ] Test dark mode compatibility (if applicable)

**Deliverable:** Polished dashboards with smooth animations and optimal performance

---

### Phase 5: Testing & Demo Preparation (30 minutes)

#### Task 5.1: End-to-End Testing (15 min)
- [ ] Test Scenario 1: Transfer Money
  - Open TRMS Dashboard
  - Note initial balances
  - Use chat to transfer money
  - Verify balances update within 5 seconds
  - Verify transaction appears in list
- [ ] Test Scenario 2: SWIFT Payment
  - Open SWIFT Dashboard
  - Use chat to send SWIFT payment
  - Verify new message appears
  - Verify status progression works
  - Verify reconciliation count updates
- [ ] Test Scenario 3: EOD Workflow
  - Check EOD status on TRMS Dashboard
  - Use chat to propose rate fixings
  - Verify EOD status updates
  - Check SWIFT Dashboard EOD reports

#### Task 5.2: Create Demo Script (10 min)
- [ ] Write step-by-step demo script
- [ ] Identify 3 key scenarios to demonstrate
- [ ] Prepare talking points for each scenario
- [ ] Practice demo flow

#### Task 5.3: Create Demo Data (5 min)
- [ ] Ensure TRMS mock has sufficient test accounts
- [ ] Ensure SWIFT mock has sample messages
- [ ] Reset data to clean state before demo
- [ ] Document how to reset demo data

**Deliverable:** Tested dashboards with demo script and clean test data

---

### Optional Phase 6: 2-Step Validation Feature (1 hour)

#### Task 6.1: Add Pending Validation State
- [ ] Add `pendingTransactions` to useTrmsData hook
- [ ] Filter transactions with status "PENDING_APPROVAL"
- [ ] Add polling for pending validations

#### Task 6.2: Create PendingValidationsPanel
- [ ] Create `src/components/dashboards/PendingValidationsPanel.jsx`
- [ ] Display pending transactions with:
  - Transaction details
  - AI proposal badge
  - Approve button (green)
  - Reject button (red)
- [ ] Implement approve/reject handlers (local state for demo)
- [ ] Add visual feedback on approval/rejection

#### Task 6.3: Integrate with TRMS Dashboard
- [ ] Add PendingValidationsPanel to TRMS Dashboard
- [ ] Position above transactions panel
- [ ] Add badge count on navigation
- [ ] Test approval workflow

**Deliverable:** 2-step validation feature showing AI → Human approval workflow

---

## 📊 Progress Tracking

### Phase 1: Project Setup & Routing
- [ ] Dependencies installed
- [ ] Folder structure created
- [ ] Routing configured
- [ ] Navigation header updated

### Phase 2: TRMS Dashboard
- [ ] Data fetching hook completed
- [ ] AccountsPanel completed
- [ ] TransactionsPanel completed
- [ ] EODStatusPanel completed
- [ ] TrmsDashboard page assembled

### Phase 3: SWIFT Dashboard
- [ ] Data fetching hook completed
- [ ] SwiftMessagesPanel completed
- [ ] ReconciliationPanel completed
- [ ] ReportsPanel completed
- [ ] RedemptionPanel completed
- [ ] SwiftDashboard page assembled

### Phase 4: Real-Time Updates & Polish
- [ ] Change detection implemented
- [ ] Visual animations added
- [ ] Polling optimized
- [ ] Final styling completed

### Phase 5: Testing & Demo Preparation
- [ ] End-to-end testing completed
- [ ] Demo script created
- [ ] Demo data prepared

### Phase 6: 2-Step Validation (Optional)
- [ ] Pending validation state added
- [ ] PendingValidationsPanel created
- [ ] Integration with TRMS Dashboard completed

---

## 🎯 Estimated Timeline

| Phase | Time Estimate | Priority |
|-------|---------------|----------|
| Phase 1: Setup & Routing | 30 minutes | CRITICAL |
| Phase 2: TRMS Dashboard | 2 hours | CRITICAL |
| Phase 3: SWIFT Dashboard | 2 hours | CRITICAL |
| Phase 4: Polish | 1 hour | HIGH |
| Phase 5: Testing | 30 minutes | HIGH |
| Phase 6: Validation (Optional) | 1 hour | MEDIUM |
| **Total** | **6-7 hours** | |

---

## 🚀 Quick Start

1. **Start with Phase 1** - Get routing working first
2. **Build TRMS Dashboard** - Highest visual impact for demo
3. **Build SWIFT Dashboard** - Completes the integration story
4. **Add polish** - Animations and styling
5. **Test thoroughly** - Practice demo scenarios
6. **Optional features** - Only if time permits

---

## ✅ Definition of Done

Each task is considered complete when:
- [ ] Code is written and tested
- [ ] Component renders without errors
- [ ] Data updates correctly via polling
- [ ] Styling matches design requirements
- [ ] Responsive on desktop and tablet
- [ ] Git commit made with descriptive message

---

## 🎬 Demo Readiness Checklist

Before the hackathon demo:
- [ ] All services running (TRMS, SWIFT, AI Backend, Frontend)
- [ ] Dashboards accessible at correct routes
- [ ] Polling working (5-second refresh confirmed)
- [ ] Test data loaded in TRMS and SWIFT mocks
- [ ] Demo script rehearsed at least once
- [ ] Browser windows positioned (chat + dashboard side-by-side)
- [ ] Backup plan ready (screenshots if live demo fails)

---

**Ready to start implementation? Begin with Phase 1!** 🚀
