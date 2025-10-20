import { useState, useEffect, useCallback, useRef } from 'react';

/**
 * useServiceEvents Hook - Tracks service communication events
 *
 * This hook monitors:
 * - Active API calls to the AI backend
 * - Function executions (TRMS and SWIFT)
 * - Request/response timing
 * - Service health status
 *
 * For now, it extracts events from message metadata
 * Future: Can be enhanced with Server-Sent Events (SSE) for real-time streaming
 *
 * @param {Array} messages - Chat messages array
 * @returns {Object} - Service events state and handlers
 */
const useServiceEvents = (messages = []) => {
  const [activeCalls, setActiveCalls] = useState([]);
  const [recentEvents, setRecentEvents] = useState([]);
  const callIdCounter = useRef(0);

  // Extract events from messages
  useEffect(() => {
    if (messages.length === 0) return;

    const lastMessage = messages[messages.length - 1];

    // When user sends a message, create an active call (Frontend → AI Backend)
    if (lastMessage.sender === 'user') {
      const callId = `call-${Date.now()}-${callIdCounter.current++}`;

      setActiveCalls([{
        id: callId,
        type: 'chat',
        status: 'active',
        timestamp: new Date(),
        userMessage: lastMessage.text,
        source: 'frontend',
        target: 'ai-backend'
      }]);
    }

    // When AI responds, process the function calls
    if (lastMessage.sender === 'ai' && !lastMessage.isError) {
      // First, try to use metadata from backend (preferred approach)
      let functionCalls = [];

      if (lastMessage.metadata && lastMessage.metadata.functionCalls) {
        // Backend provided function metadata - use it directly (even if empty array)
        // Define SWIFT functions (all others are TRMS functions)
        const swiftFunctions = [
          'sendSwiftPayment',
          'checkSwiftMessageStatus',
          'getSwiftMessagesByAccount',
          'getSwiftMessagesByTransaction',
          'reconcileSwiftMessages',
          'getUnreconciledMessages',
          'processRedemptionReport',
          'verifyEODReports'
        ];

        // If backend says no functions were called (empty array), respect that
        // Don't fall back to pattern detection
        functionCalls = lastMessage.metadata.functionCalls.map(fnName => ({
          name: fnName,
          params: {},
          path: swiftFunctions.includes(fnName) ? 'AI Backend → SWIFT Mock' : 'AI Backend → TRMS Mock',
          response: 'Data retrieved'
        }));
      } else {
        // Fallback to pattern detection ONLY if no metadata at all
        functionCalls = detectFunctionCalls(lastMessage.text);
      }

      if (functionCalls.length > 0) {
        // Keep historical events - accumulate all requests over time
        // Process each function call sequentially (wait for each to complete before starting next)
        functionCalls.forEach((fn, idx) => {
          // Each call takes 2.5 seconds total (2s animation + 0.5s gap)
          const startDelay = idx * 2500;

          setTimeout(() => {
            // Determine target from the path (which already has correct SWIFT/TRMS routing)
            const target = fn.path && fn.path.includes('SWIFT') ? 'swift' : 'trms';
            const eventId = `event-${Date.now()}-${idx}`;

            // Step 1: Show active call (AI Backend → TRMS/SWIFT)
            setActiveCalls([{
              id: eventId,
              function: fn.name,
              status: 'active',
              timestamp: new Date(),
              source: 'ai-backend',
              target: target
            }]);

            // Step 2: Complete the call after 2 seconds
            setTimeout(() => {
              setActiveCalls([]);
              // Add to recent events at the top (newest first - stack behavior)
              setRecentEvents(prev => [{
                id: eventId,
                function: fn.name,
                params: fn.params,
                status: 'completed',
                duration: Math.floor(Math.random() * 150) + 50,
                timestamp: new Date(),
                path: fn.path,
                response: fn.response
              }, ...prev]);
            }, 2000);
          }, startDelay);
        });
      } else {
        // No function calls, just clear active calls
        setActiveCalls([]);
      }
    }
  }, [messages]);

  /**
   * Detect function calls from AI response text
   * This is a simple pattern matcher - can be enhanced with backend metadata
   */
  const detectFunctionCalls = (text) => {
    const functions = [];
    const lowerText = text.toLowerCase();

    // Only detect actual function executions, not descriptions
    // Look for specific data patterns that indicate actual API responses

    // Check for actual account data (not just mentions of accounts)
    // Pattern: Multiple ACC-XXX-CUR account IDs or structured account data
    const accountMatches = text.match(/ACC-\d+-[A-Z]{3}/g);
    if (accountMatches && accountMatches.length >= 2) {
      // Multiple accounts returned = getAccountsByCurrency was called
      const currency = accountMatches[0].split('-')[2];
      functions.push({
        name: 'getAccountsByCurrency',
        params: { currency },
        path: 'AI Backend → TRMS Mock',
        response: 'Retrieved account data'
      });
    }

    // Check for specific account balance (single account with balance info)
    if (text.match(/ACC-\d+-\w+/) && lowerText.match(/balance.*\$[\d,]+/)) {
      const accountId = text.match(/ACC-\d+-\w+/)?.[0];
      functions.push({
        name: 'checkAccountBalance',
        params: { accountId },
        path: 'AI Backend → TRMS Mock',
        response: 'Balance information retrieved'
      });
    }

    // Check for transaction confirmation (must have transaction ID)
    if (text.match(/TRX-\d+/) || (lowerText.includes('successfully') && lowerText.includes('transferred'))) {
      functions.push({
        name: 'bookTransaction',
        params: {},
        path: 'AI Backend → TRMS Mock',
        response: 'Transaction completed'
      });
    }

    // Check for EOD status data (must have specific status indicators AND actual data)
    if ((lowerText.includes('eod readiness') || lowerText.includes('end of day readiness'))
        && lowerText.match(/is\s+(not\s+)?ready|status:\s+/)) {
      functions.push({
        name: 'checkEODReadiness',
        params: {},
        path: 'AI Backend → TRMS Mock',
        response: 'EOD status retrieved'
      });
    }

    // SWIFT Functions - require specific message IDs or data

    // Check for SWIFT message data (must have SWIFT-MSG-XXX ID)
    if (text.match(/SWIFT-MSG-\d+/)) {
      if (lowerText.includes('sent') || lowerText.includes('payment')) {
        functions.push({
          name: 'sendSwiftPayment',
          params: {},
          path: 'AI Backend → SWIFT Mock',
          response: 'SWIFT message sent'
        });
      }
    }

    // Check for reconciliation results (must have specific data)
    if (lowerText.includes('reconcil') && (lowerText.includes('matched') || lowerText.includes('unmatched'))) {
      functions.push({
        name: 'reconcileSwiftMessages',
        params: {},
        path: 'AI Backend → SWIFT Mock',
        response: 'Reconciliation completed'
      });
    }

    // Check for redemption report processing (must have processed count)
    if (lowerText.includes('redemption') && (lowerText.includes('processed') || lowerText.match(/\d+\s+redemption/))) {
      functions.push({
        name: 'processRedemptionReport',
        params: { fileName: 'redemption_report_latest.csv' },
        path: 'AI Backend → SWIFT Mock',
        response: 'Report processed'
      });
    }

    // Check for EOD report verification (must have verification results)
    if (lowerText.includes('report') && lowerText.includes('verif') && (lowerText.includes('passed') || lowerText.includes('failed'))) {
      functions.push({
        name: 'verifyEODReports',
        params: {},
        path: 'AI Backend → SWIFT Mock',
        response: 'Reports verified'
      });
    }

    return functions;
  };

  /**
   * Manually add a service event (for future backend integration)
   */
  const addServiceEvent = useCallback((event) => {
    if (event.status === 'active') {
      setActiveCalls(prev => [...prev, event]);
    } else {
      setRecentEvents(prev => [event, ...prev].slice(0, 10));
      setActiveCalls(prev => prev.filter(call => call.id !== event.id));
    }
  }, []);

  /**
   * Clear all events
   */
  const clearEvents = useCallback(() => {
    setActiveCalls([]);
    setRecentEvents([]);
  }, []);

  return {
    activeCalls,
    recentEvents,
    addServiceEvent,
    clearEvents
  };
};

export default useServiceEvents;
