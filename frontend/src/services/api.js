import axios from 'axios';
import io from 'socket.io-client';

// API Configuration
const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';
const WS_URL = import.meta.env.VITE_WS_URL || 'http://localhost:8080';

// Axios instance configuration
const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 60000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor
apiClient.interceptors.request.use(
  (config) => {
    console.log(`[API] ${config.method?.toUpperCase()} ${config.url}`);
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor
apiClient.interceptors.response.use(
  (response) => {
    console.log(`[API] Response ${response.status} for ${response.config.url}`);
    return response;
  },
  (error) => {
    console.error('[API] Error:', error.response?.data || error.message);
    
    if (error.response?.status === 500) {
      error.userMessage = 'Server error. Please try again later.';
    } else if (error.response?.status === 404) {
      error.userMessage = 'Service not found. Please check if the backend is running.';
    } else if (error.code === 'ECONNREFUSED') {
      error.userMessage = 'Cannot connect to server. Please ensure the backend is running on port 8080.';
    } else if (error.code === 'TIMEOUT') {
      error.userMessage = 'Request timed out. Please try again.';
    } else {
      error.userMessage = error.response?.data?.message || 'An unexpected error occurred.';
    }
    
    return Promise.reject(error);
  }
);

// WebSocket connection management
let socket = null;
const socketListeners = new Map();

const connectWebSocket = () => {
  if (socket?.connected) return socket;
  
  // Note: The backend doesn't support socket.io, so WebSocket will fail and fallback to HTTP
  socket = io(WS_URL, {
    transports: ['websocket', 'polling'],
    timeout: 5000, // Reduced timeout to fail faster
    reconnection: false, // Don't auto-reconnect since backend doesn't support WebSocket
  });

  socket.on('connect', () => {
    console.log('[WebSocket] Connected to server');
  });

  socket.on('disconnect', (reason) => {
    console.log('[WebSocket] Disconnected:', reason);
  });

  socket.on('connect_error', (error) => {
    console.error('[WebSocket] Connection error:', error);
  });

  return socket;
};

const disconnectWebSocket = () => {
  if (socket) {
    socket.disconnect();
    socket = null;
    socketListeners.clear();
  }
};

// Session ID management
let currentSessionId = null;

// API Service
class TrmsApiService {
  /**
   * Health check to verify backend connectivity
   */
  static async healthCheck() {
    try {
      const response = await apiClient.get('/api/chat/health');
      return {
        success: true,
        data: response.data,
        timestamp: new Date().toISOString(),
      };
    } catch (error) {
      return {
        success: false,
        error: error.userMessage || error.message,
        timestamp: new Date().toISOString(),
      };
    }
  }

  /**
   * Send a chat message to the AI backend
   * @param {string} message - The user message
   * @param {boolean} experimentalMode - Whether to use experimental LLM mode
   * @returns {Promise<Object>} Response from AI
   */
  static async sendMessage(message, experimentalMode = false) {
    try {
      const response = await apiClient.post('/api/chat', {
        message: message.trim(),
        sessionId: currentSessionId, // Include sessionId in request
        experimentalMode: experimentalMode, // Include experimental mode flag
        timestamp: new Date().toISOString(),
      });

      // Store sessionId from backend response
      if (response.data.sessionId) {
        currentSessionId = response.data.sessionId;
        console.log('[API] Session ID:', currentSessionId);
      }

      return {
        success: true,
        message: response.data.message || response.data.response,
        sessionId: response.data.sessionId,
        timestamp: response.data.timestamp || new Date().toISOString(),
        metadata: response.data.metadata,
      };
    } catch (error) {
      // Fallback to mock responses for development
      if (error.code === 'ECONNREFUSED' || error.response?.status >= 500) {
        console.warn('[API] Backend unavailable, falling back to mock responses');
        return this.getMockResponse(message);
      }

      throw new Error(error.userMessage || 'Failed to send message');
    }
  }

  /**
   * Clear the current session (start fresh conversation)
   */
  static clearSession() {
    console.log('[API] Clearing session:', currentSessionId);
    currentSessionId = null;
  }

  /**
   * Get the current session ID
   */
  static getSessionId() {
    return currentSessionId;
  }

  /**
   * Setup WebSocket for real-time chat
   * @param {Function} onMessage - Callback for incoming messages
   * @param {Function} onError - Callback for errors
   */
  static setupWebSocket(onMessage, onError) {
    const ws = connectWebSocket();
    
    // Remove existing listeners to avoid duplicates
    ws.off('chat-message');
    ws.off('error');
    
    ws.on('chat-message', (data) => {
      console.log('[WebSocket] Received message:', data);
      onMessage(data);
    });
    
    ws.on('error', (error) => {
      console.error('[WebSocket] Error:', error);
      onError(error);
    });
    
    return ws;
  }

  /**
   * Send message via WebSocket
   * @param {string} message - The message to send
   */
  static sendWebSocketMessage(message) {
    if (socket?.connected) {
      socket.emit('chat-message', {
        message: message.trim(),
        timestamp: new Date().toISOString(),
      });
    } else {
      throw new Error('WebSocket not connected');
    }
  }

  /**
   * Cleanup WebSocket connections
   */
  static cleanup() {
    disconnectWebSocket();
  }

  /**
   * Mock responses for development/fallback
   * @param {string} message - The input message
   * @returns {Object} Mock response
   */
  static getMockResponse(message) {
    const lowerMessage = message.toLowerCase();
    
    let responseMessage = "I'm sorry, I don't understand that request. Please try asking about:\n\n• **Account queries**: 'Show me all USD accounts'\n• **Balance checks**: 'What's the balance for ACC-001-USD?'\n• **Transactions**: 'Transfer $50,000 from ACC-001-USD to ACC-002-USD'\n• **EOD processing**: 'Can we run End-of-Day?'\n• **Rate fixings**: 'Propose missing rate fixings'";

    if (lowerMessage.includes('usd accounts') || lowerMessage.includes('show accounts')) {
      responseMessage = `## USD Accounts Found\n\nI found **3 active USD accounts** in the system:\n\n### 🏦 Trading Account USD (ACC-001-USD)\n- **Type**: TRADING\n- **Status**: 🟢 ACTIVE\n- **Balance**: $1,234,567.89\n- **Last Updated**: ${new Date().toLocaleString()}\n\n### 🏦 Settlement Account USD (ACC-002-USD)\n- **Type**: SETTLEMENT\n- **Status**: 🟢 ACTIVE\n- **Balance**: $2,456,789.12\n- **Last Updated**: ${new Date().toLocaleString()}\n\n### 🏦 Collateral Account USD (ACC-003-USD)\n- **Type**: COLLATERAL\n- **Status**: 🟡 PENDING_APPROVAL\n- **Balance**: $500,000.00\n- **Last Updated**: ${new Date().toLocaleString()}\n\n---\n*Total USD Holdings: $4,191,356.01*`;
      
    } else if (lowerMessage.includes('balance') && lowerMessage.includes('acc-001-usd')) {
      responseMessage = `## Account Balance Details\n\n### 🏦 Trading Account USD (ACC-001-USD)\n\n**💰 Current Balance**: $1,234,567.89\n**📊 Available Balance**: $1,199,567.89\n**🔒 Reserved Funds**: $35,000.00\n\n**Recent Activity**:\n- Last Transaction: 2 hours ago\n- Transaction Type: WIRE_TRANSFER_IN\n- Amount: +$50,000.00\n\n**Account Details**:\n- Account Type: TRADING\n- Status: 🟢 ACTIVE\n- Currency: USD\n- Last Updated: ${new Date().toLocaleString()}\n\n---\n*All balances are real-time and include pending settlements.*`;
      
    } else if (lowerMessage.includes('transfer') && lowerMessage.includes('50000')) {
      responseMessage = `## Transaction Confirmation\n\n### 💸 Transfer Request Processed\n\n**Transaction Details**:\n- **Transaction ID**: TXN-${Date.now()}\n- **Type**: INTERNAL_TRANSFER\n- **Amount**: $50,000.00\n- **From**: ACC-001-USD (Trading Account)\n- **To**: ACC-002-USD (Settlement Account)\n- **Status**: 🟢 COMPLETED\n- **Execution Time**: ${new Date().toLocaleString()}\n\n**Updated Balances**:\n- **ACC-001-USD**: $1,184,567.89 (-$50,000.00)\n- **ACC-002-USD**: $2,506,789.12 (+$50,000.00)\n\n**Settlement Details**:\n- Value Date: ${new Date().toLocaleDateString()}\n- Reference: SETTLEMENT_${Date.now()}\n- Confirmation Sent: ✅\n\n---\n*Transaction completed successfully and has been recorded in the audit trail.*`;
      
    } else if (lowerMessage.includes('eod') || lowerMessage.includes('end-of-day') || lowerMessage.includes('end of day')) {
      responseMessage = `## End-of-Day Readiness Assessment\n\n### 📊 EOD Status: ⚠️ **PENDING ACTIONS REQUIRED**\n\n---\n\n### 1️⃣ Market Data Status\n**📈 FX Rates**: 🟢 **COMPLETE** (284/284)\n- EUR/USD, GBP/USD, JPY/USD: ✅\n- All major pairs current as of 16:00 GMT\n\n**📊 Equity Prices**: ⚠️ **INCOMPLETE** (197/205)\n- Missing: AAPL, GOOGL, MSFT, AMZN, TSLA, META, NFLX, NVDA\n- **Action Required**: Obtain missing equity prices\n\n**💹 Interest Rates**: 🟢 **COMPLETE** (45/45)\n- USD-LIBOR, EUR-EURIBOR, GBP-SONIA: ✅\n\n---\n\n### 2️⃣ Transaction Validation Status\n**Total Transactions**: 1,247\n- 🟢 **VALIDATED**: 1,189 (95.3%)\n- 🟡 **NEW**: 23 (1.8%) - *Require validation*\n- 🔴 **PROPOSAL**: 35 (2.8%) - *Pending approval*\n\n---\n\n### 3️⃣ Rate Reset Analysis\n**Floating Rate Instruments**: 156 total\n- 🟢 **Fixed**: 155\n- 🔴 **MISSING**: 1 USD-LIBOR-3M fixing\n\n---\n\n### 📋 Required Actions:\n1. **🔴 CRITICAL**: Import missing USD-LIBOR-3M fixing\n2. **🟡 HIGH**: Validate 23 new transactions\n3. **🟡 MEDIUM**: Review 35 proposal transactions\n4. **🟡 MEDIUM**: Obtain 8 missing equity prices\n\n### 🤖 AI Recommendations:\n- **A)** Generate validation report for new transactions\n- **B)** Auto-propose missing rate fixings based on market data\n- **C)** Request equity price updates from data vendor\n\n**Estimated Time to EOD Ready**: 45 minutes\n\n---\n*Assessment completed at ${new Date().toLocaleString()}*`;
      
    } else if (lowerMessage.includes('rate fixing') || lowerMessage.includes('missing rate')) {
      responseMessage = `## Rate Fixing Proposals\n\n### 🔧 Missing Rate Analysis\n\n**Missing Fixing Identified**:\n- **Index**: USD-LIBOR-3M\n- **Value Date**: ${new Date().toLocaleDateString()}\n- **Instruments Affected**: 1 floating rate bond\n- **Notional Impact**: $10,000,000\n\n---\n\n### 💡 AI-Generated Proposals\n\n**Recommendation 1**: Use Previous Day + Spread\n- **Rate**: 5.234% (yesterday: 5.210% + spread: 0.024%)\n- **Confidence**: 🟢 HIGH (95%)\n- **Rationale**: Historical volatility analysis suggests minimal overnight movement\n\n**Recommendation 2**: Market Interpolation\n- **Rate**: 5.241% (interpolated from 1M: 5.180%, 6M: 5.320%)\n- **Confidence**: 🟡 MEDIUM (78%)\n- **Rationale**: Yield curve interpolation based on available tenors\n\n**Recommendation 3**: Vendor Estimate\n- **Rate**: 5.238% (Bloomberg BFIX estimate)\n- **Confidence**: 🟢 HIGH (92%)\n- **Rationale**: Third-party market data provider estimate\n\n---\n\n### 🎯 Recommended Action\n**Primary Choice**: Use Vendor Estimate (5.238%)\n- Most reliable source\n- Consistent with market expectations\n- Meets regulatory requirements\n\n**Impact Analysis**:\n- **P&L Impact**: ~$240 (minimal)\n- **Risk Impact**: Low - within acceptable tolerance\n- **Regulatory**: Compliant with fixing procedures\n\n### ⚡ Quick Actions:\n- **APPLY**: Implement recommended fixing\n- **REVIEW**: Schedule manual review post-EOD\n- **AUDIT**: Log decision rationale\n\n---\n*Proposal generated at ${new Date().toLocaleString()}*`;
    }

    return {
      success: true,
      message: responseMessage,
      timestamp: new Date().toISOString(),
      isMock: true,
    };
  }
}

export default TrmsApiService;