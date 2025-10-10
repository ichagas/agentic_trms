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
      // Detect function calls from message content
      const functionCalls = detectFunctionCalls(lastMessage.text);

      if (functionCalls.length > 0) {
        // Clear previous events - will collect all from current request
        setRecentEvents([]);

        // Process each function call sequentially (wait for each to complete before starting next)
        functionCalls.forEach((fn, idx) => {
          // Each call takes 2.5 seconds total (2s animation + 0.5s gap)
          const startDelay = idx * 2500;

          setTimeout(() => {
            const target = fn.name.toLowerCase().includes('swift') ? 'swift' : 'trms';
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
              // Add to recent events (keep all from this request)
              setRecentEvents(prev => [...prev, {
                id: eventId,
                function: fn.name,
                params: fn.params,
                status: 'completed',
                duration: Math.floor(Math.random() * 150) + 50,
                timestamp: new Date(),
                path: fn.path,
                response: fn.response
              }]);
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

    // TRMS Functions
    if (text.includes('accounts') && text.includes('USD')) {
      functions.push({
        name: 'getAccountsByCurrency',
        params: { currency: 'USD' },
        path: 'AI Backend → TRMS Mock',
        response: 'Retrieved account data'
      });
    }

    if (text.includes('balance') && text.match(/ACC-\d+-\w+/)) {
      const accountId = text.match(/ACC-\d+-\w+/)?.[0];
      functions.push({
        name: 'checkAccountBalance',
        params: { accountId },
        path: 'AI Backend → TRMS Mock',
        response: 'Balance information retrieved'
      });
    }

    if (text.toLowerCase().includes('eod') || text.toLowerCase().includes('end of day')) {
      functions.push({
        name: 'checkEODReadiness',
        params: {},
        path: 'AI Backend → TRMS Mock',
        response: 'EOD status retrieved'
      });
    }

    if (text.includes('transaction') || text.includes('transfer')) {
      functions.push({
        name: 'bookTransaction',
        params: { /* extracted from text */ },
        path: 'AI Backend → TRMS Mock',
        response: 'Transaction completed'
      });
    }

    // SWIFT Functions
    if (text.toLowerCase().includes('swift') && text.toLowerCase().includes('payment')) {
      functions.push({
        name: 'sendSwiftPayment',
        params: {},
        path: 'AI Backend → SWIFT Mock',
        response: 'SWIFT message sent'
      });
    }

    if (text.toLowerCase().includes('reconcil')) {
      functions.push({
        name: 'reconcileSwiftMessages',
        params: {},
        path: 'AI Backend → SWIFT Mock',
        response: 'Reconciliation completed'
      });
    }

    if (text.toLowerCase().includes('redemption') && text.toLowerCase().includes('report')) {
      functions.push({
        name: 'processRedemptionReport',
        params: { fileName: 'redemption_report_latest.csv' },
        path: 'AI Backend → SWIFT Mock',
        response: 'Report processed'
      });
    }

    if (text.toLowerCase().includes('verify') && text.toLowerCase().includes('eod report')) {
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
