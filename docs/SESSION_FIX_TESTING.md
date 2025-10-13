# Session Context Fix - Testing Guide

## What Was Fixed

**Problem**: Every chat message was creating a new sessionId, preventing conversation context from being maintained.

**Root Cause**: The frontend was **NOT sending sessionId** in API requests. The backend would generate a new UUID for every request.

**Solution**:
1. Frontend now maintains a `currentSessionId` variable
2. First request sends `sessionId: null` → Backend generates new UUID and returns it
3. Subsequent requests send the received sessionId → Backend uses same session
4. Context is preserved across multiple messages!

## Files Changed

### `/frontend/src/services/api.js`
- Added `currentSessionId` variable to store session
- Updated `sendMessage()` to include sessionId in request body
- Store sessionId from backend response
- Added `clearSession()` method to reset conversation
- Added `getSessionId()` method to inspect current session

## Testing Instructions

### Test 1: Verify SessionId Persistence

1. **Start the frontend**:
   ```bash
   cd frontend && npm start
   ```

2. **Open browser DevTools** → Console tab

3. **Send first message**: "Show me all USD accounts"
   - Look for log: `[API] Session ID: 550e8400-e29b-41d4-a716-446655440000`
   - Copy this sessionId

4. **Send second message**: "What's the balance of the first one?"
   - Look for log with **SAME sessionId**
   - Should see: `[API] Session ID: 550e8400-e29b-41d4-a716-446655440000` (identical)

5. **✅ SUCCESS**: If sessionId is the same, context persistence is working!

---

### Test 2: Verify Context in Backend

1. **Get your sessionId from browser console** (see Test 1)

2. **Check conversation history**:
   ```bash
   curl http://localhost:8080/api/chat/debug/session/550e8400-e29b-41d4-a716-446655440000
   ```

3. **Expected output**:
   ```json
   {
     "sessionId": "550e8400-e29b-41d4-a716-446655440000",
     "messageCount": 4,
     "messages": [
       {
         "role": "USER",
         "content": "Show me all USD accounts",
         "timestamp": "2025-10-13T10:00:00"
       },
       {
         "role": "ASSISTANT",
         "content": "[Response with account list]",
         "timestamp": "2025-10-13T10:00:02"
       },
       {
         "role": "USER",
         "content": "What's the balance of the first one?",
         "timestamp": "2025-10-13T10:01:00"
       },
       {
         "role": "ASSISTANT",
         "content": "[Response about ACC-001-USD balance]",
         "timestamp": "2025-10-13T10:01:02"
       }
     ]
   }
   ```

4. **✅ SUCCESS**: All messages are stored under the same sessionId!

---

### Test 3: Verify AI Context Retention

This tests if the AI actually uses the context to answer follow-up questions.

1. **Message 1**: "Show me all USD accounts"
   - AI should list accounts (e.g., ACC-001-USD, ACC-002-USD, etc.)

2. **Message 2**: "What's the balance of the first one?"
   - **Without context**: AI would say "What account?" or fail
   - **With context**: AI should understand "first one" = ACC-001-USD and show balance

3. **Message 3**: "Transfer $50,000 from that account to ACC-002-USD"
   - **Without context**: AI would say "Which account?"
   - **With context**: AI should understand "that account" = ACC-001-USD

4. **✅ SUCCESS**: AI references previous messages correctly!

---

### Test 4: Verify New Session After Refresh

1. **Note your current sessionId** from browser console

2. **Refresh the page** (F5 or Cmd+R)

3. **Send a new message**: "Show me EUR accounts"
   - Look for `[API] Session ID:` in console
   - SessionId should be **DIFFERENT** from before

4. **✅ EXPECTED BEHAVIOR**: New session after refresh (as designed)
   - Frontend doesn't persist sessionId to localStorage
   - Each page load = new conversation

---

## Debug Commands

### List all active sessions:
```bash
curl http://localhost:8080/api/chat/debug/sessions
```

### Get specific session:
```bash
curl http://localhost:8080/api/chat/debug/session/{sessionId}
```

### Clear a session:
```bash
curl -X DELETE http://localhost:8080/api/chat/debug/session/{sessionId}
```

---

## Troubleshooting

### Issue: Still seeing new sessionId on every request

**Check 1**: Verify frontend is sending sessionId
```javascript
// In browser DevTools → Network tab
// Click on /api/chat request
// Check "Payload" tab, should see:
{
  "message": "your message",
  "sessionId": "550e8400-...",  // Should be present and consistent
  "timestamp": "..."
}
```

**Check 2**: Verify backend is using sessionId from request
```bash
# Check backend logs for:
Processing chat request - Session: {sessionId}, Message: ...
# SessionId should match across requests
```

### Issue: AI not using context

**Check**: Verify messages are in history
```bash
curl http://localhost:8080/api/chat/debug/session/{sessionId}
# Should show all messages, not just the latest one
```

**Check**: Verify contextual prompt is being built
```bash
# Look in backend logs for:
# TrmsAiService should build contextual prompt with "Previous conversation context:"
```

---

## Expected Behavior Summary

### ✅ CORRECT (After Fix):
- First message → Backend generates UUID → Returns it
- Second message → Frontend sends same UUID → Backend reuses session
- Third message → Frontend sends same UUID → Backend has full context
- Console shows: Same sessionId logged multiple times

### ❌ INCORRECT (Before Fix):
- First message → Backend generates UUID-1
- Second message → Frontend sends null → Backend generates UUID-2
- Third message → Frontend sends null → Backend generates UUID-3
- Console shows: Different sessionId every time

---

## Browser Console Monitoring

To monitor session behavior, paste this in browser console:

```javascript
// Monitor all API calls
window.addEventListener('fetch', (e) => {
  console.log('[FETCH]', e);
});

// Get current session from API service
console.log('Current Session:', TrmsApiService?.getSessionId());
```

---

## Next Steps (Optional Enhancements)

If you want to persist sessions across page refreshes:

1. Store sessionId in localStorage
2. Retrieve on app startup
3. Optional: Add "New Conversation" button to clear session

This would require changes to App.jsx and api.js, but is **NOT** currently implemented to keep frontend simple.
