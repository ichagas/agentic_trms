# Conversation Memory Debug Endpoints

This document explains how to debug and inspect conversation context for active sessions.

## Overview

The TRMS AI backend now maintains conversation context per session. Each chat request includes a `sessionId` that allows the AI to remember previous interactions. These debug endpoints help you inspect what the system remembers.

## Debug Endpoints

### 1. List All Active Sessions

Get statistics about all active conversation sessions.

**Endpoint:** `GET /api/chat/debug/sessions`

**Example:**
```bash
curl http://localhost:8080/api/chat/debug/sessions
```

**Response:**
```json
{
  "activeSessionCount": 3,
  "statistics": "Active sessions: 3, Total messages: 12, Avg messages/session: 4.0"
}
```

---

### 2. Get Session Context

View the complete conversation history for a specific session, including all messages, timestamps, and function calls.

**Endpoint:** `GET /api/chat/debug/session/{sessionId}`

**Example:**
```bash
# Replace abc-123 with your actual sessionId
curl http://localhost:8080/api/chat/debug/session/abc-123
```

**Response:**
```json
{
  "sessionId": "abc-123",
  "startTime": "2025-10-13T09:30:00",
  "lastAccessTime": "2025-10-13T09:35:00",
  "messageCount": 4,
  "messages": [
    {
      "role": "USER",
      "content": "Show me all USD accounts",
      "timestamp": "2025-10-13T09:30:00"
    },
    {
      "role": "ASSISTANT",
      "content": "Here are the USD accounts: ACC-001-USD, ACC-002-USD...",
      "timestamp": "2025-10-13T09:30:02",
      "functionCalls": "getAccountsByCurrency"
    },
    {
      "role": "USER",
      "content": "What's the balance of the first one?",
      "timestamp": "2025-10-13T09:35:00"
    },
    {
      "role": "ASSISTANT",
      "content": "ACC-001-USD has a balance of $1,250,000 USD",
      "timestamp": "2025-10-13T09:35:02",
      "functionCalls": "checkAccountBalance"
    }
  ]
}
```

**Session Not Found Response:**
```json
{
  "error": "Session not found",
  "sessionId": "invalid-id",
  "message": "No conversation history exists for this session ID"
}
```

---

### 3. Clear Session History

Delete the conversation history for a specific session (useful for testing or privacy).

**Endpoint:** `DELETE /api/chat/debug/session/{sessionId}`

**Example:**
```bash
curl -X DELETE http://localhost:8080/api/chat/debug/session/abc-123
```

**Response:**
```json
{
  "message": "Session cleared successfully",
  "sessionId": "abc-123"
}
```

---

## How to Find Your SessionId

The `sessionId` is returned in every chat response. You can find it in:

1. **Browser DevTools (Network Tab)**:
   - Open DevTools → Network tab
   - Send a chat message
   - Look for the `/api/chat` POST request
   - Check the response JSON for `sessionId` field

2. **Backend Logs**:
   - Look for log entries like: `Processing chat request - Session: abc-123, Message: ...`

3. **ChatResponse Object**:
   - Every response includes: `{ "response": "...", "sessionId": "abc-123", ... }`

---

## Debugging Workflow

### Example: Testing Context Retention

1. **Send first message** (frontend or curl):
   ```bash
   curl -X POST http://localhost:8080/api/chat \
     -H "Content-Type: application/json" \
     -d '{"message": "Show me all USD accounts"}'
   ```

   Response will include: `"sessionId": "550e8400-e29b-41d4-a716-446655440000"`

2. **Check what was saved**:
   ```bash
   curl http://localhost:8080/api/chat/debug/session/550e8400-e29b-41d4-a716-446655440000
   ```

   You should see 2 messages (USER and ASSISTANT).

3. **Send follow-up message with same sessionId**:
   ```bash
   curl -X POST http://localhost:8080/api/chat \
     -H "Content-Type: application/json" \
     -d '{
       "message": "What is the balance of the first account?",
       "sessionId": "550e8400-e29b-41d4-a716-446655440000"
     }'
   ```

4. **Check updated context**:
   ```bash
   curl http://localhost:8080/api/chat/debug/session/550e8400-e29b-41d4-a716-446655440000
   ```

   You should now see 4 messages showing the full conversation.

---

## Configuration

### Memory Limits
- **Max messages per session**: 20 (older messages are automatically removed)
- **Session idle timeout**: 60 minutes
- **Cleanup schedule**: Every 15 minutes

### Customization
These values are configured in `ConversationMemory.java`:
- `MAX_MESSAGES_PER_CONVERSATION = 20`
- `SESSION_IDLE_TIMEOUT_MINUTES = 60`
- `@Scheduled(fixedRate = 900000)` // 15 minutes

---

## Tips

1. **Each browser refresh creates a new session** (unless you implement localStorage persistence in frontend)

2. **SessionId is generated if not provided**: If you don't send a `sessionId` in the request, the backend generates a new UUID

3. **Context is built from recent messages**: The system includes the last 6-8 messages in the contextual prompt sent to the AI

4. **Function calls are tracked**: You can see which TRMS/SWIFT functions were executed in each response

5. **Sessions auto-expire**: Inactive sessions are removed after 60 minutes to save memory

---

## Troubleshooting

**Q: Session not found even though I just sent a message**
- A: Make sure you're using the exact sessionId from the response
- Check for typos or extra whitespace
- The session might have expired (60-minute timeout)

**Q: Messages are missing from the history**
- A: Sessions are limited to 20 messages. Older messages are removed automatically
- Check if the session was cleared or expired

**Q: No active sessions found**
- A: Sessions expire after 60 minutes of inactivity
- Scheduled cleanup removes expired sessions every 15 minutes
- Send a chat message to create a new session
