import { useState, useEffect, useCallback } from 'react';

const SWIFT_BASE_URL = 'http://localhost:8091/api/v1/swift';
const POLL_INTERVAL = 5000; // 5 seconds

/**
 * Custom hook for fetching SWIFT dashboard data with automatic polling
 *
 * @returns {Object} SWIFT data and loading state
 * @returns {Array} returns.messages - List of SWIFT messages
 * @returns {Object} returns.reconciliationStatus - Reconciliation summary
 * @returns {Object} returns.eodReports - EOD report verification results
 * @returns {Object} returns.redemptionStatus - Redemption report status
 * @returns {boolean} returns.loading - Loading state
 * @returns {Error} returns.error - Error object if fetch fails
 * @returns {Function} returns.refresh - Manual refresh function
 */
export function useSwiftData() {
  const [messages, setMessages] = useState([]);
  const [reconciliationStatus, setReconciliationStatus] = useState(null);
  const [eodReports, setEodReports] = useState(null);
  const [redemptionStatus, setRedemptionStatus] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  /**
   * Fetch all SWIFT data from backend
   */
  const fetchData = useCallback(async () => {
    try {
      // Fetch SWIFT messages
      const messagesResponse = await fetch(`${SWIFT_BASE_URL}/messages`);
      if (!messagesResponse.ok) throw new Error('Failed to fetch SWIFT messages');
      const messagesData = await messagesResponse.json();

      // Fetch reconciliation status by triggering reconciliation check
      const reconciliationResponse = await fetch(`${SWIFT_BASE_URL}/messages/reconcile`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ autoReconcile: false })
      });
      if (!reconciliationResponse.ok) throw new Error('Failed to fetch reconciliation status');
      const reconciliationResult = await reconciliationResponse.json();

      // Transform reconciliation result to status format
      const reconciliationData = {
        totalMessages: reconciliationResult.totalMessages,
        reconciledCount: reconciliationResult.reconciledCount,
        unreconciledCount: reconciliationResult.unreconciledCount,
        pendingCount: reconciliationResult.pendingCount,
        summary: reconciliationResult.summary,
        lastReconciliation: new Date().toISOString()
      };

      // Fetch EOD reports verification from backend
      // Use local date, not UTC to avoid timezone issues
      const now = new Date();
      const today = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`;
      let eodReportsData = null;

      try {
        const eodResponse = await fetch(`${SWIFT_BASE_URL}/reports/eod/verify?reportDate=${today}`);
        if (eodResponse.ok) {
          const eodResult = await eodResponse.json();

          // Transform backend response to frontend format
          eodReportsData = {
            verified: eodResult.complete || false,
            totalReports: eodResult.totalChecks || 0,
            passedReports: eodResult.passedChecks || 0,
            failedReports: eodResult.failedChecks || 0,
            reports: (eodResult.checks || []).map(check => ({
              name: check.reportName,
              status: check.status,
              timestamp: new Date().toISOString(),
              exists: check.exists,
              details: check.details
            })),
            lastVerification: new Date().toISOString(),
            summary: eodResult.summary
          };
        } else {
          throw new Error('EOD verification endpoint not available');
        }
      } catch (eodError) {
        console.warn('EOD verification failed, using empty state:', eodError.message);
        // Provide empty state if verification fails
        eodReportsData = {
          verified: false,
          totalReports: 0,
          passedReports: 0,
          failedReports: 0,
          reports: [],
          lastVerification: new Date().toISOString()
        };
      }

      // Fetch redemption status (mock data for now)
      const redemptionData = {
        status: 'PROCESSED',
        fileName: 'redemption_report_latest.csv',
        totalRedemptions: 7,
        processedCount: 7,
        failedCount: 0,
        totalAmount: 515000,
        currency: 'USD',
        mismatchCount: 0,
        lastProcessed: new Date().toISOString()
      };

      // Update state
      setMessages(messagesData);
      setReconciliationStatus(reconciliationData);
      setEodReports(eodReportsData);
      setRedemptionStatus(redemptionData);
      setError(null);
      setLoading(false);

      console.log('[SWIFT]', new Date().toLocaleTimeString(), 'Data fetched:', {
        messages: messagesData.length,
        reconciled: reconciliationData?.reconciledCount,
        unreconciled: reconciliationData?.unreconciledCount
      });

    } catch (err) {
      console.error('Error fetching SWIFT data:', err);
      setError(err);
      setLoading(false);
    }
  }, []);

  /**
   * Setup polling effect
   */
  useEffect(() => {
    // Initial fetch
    fetchData();

    // Setup interval for polling
    const interval = setInterval(() => {
      // Only poll if document is visible
      if (document.visibilityState === 'visible') {
        fetchData();
      }
    }, POLL_INTERVAL);

    // Cleanup on unmount
    return () => clearInterval(interval);
  }, [fetchData]);

  /**
   * Manual refresh function
   */
  const refresh = useCallback(() => {
    setLoading(true);
    fetchData();
  }, [fetchData]);

  /**
   * Get messages by status
   */
  const getMessagesByStatus = useCallback((status) => {
    return messages.filter(msg => msg.status === status);
  }, [messages]);

  /**
   * Get recent messages (within last hour)
   */
  const getRecentMessages = useCallback(() => {
    const oneHourAgo = Date.now() - (60 * 60 * 1000);
    return messages.filter(msg => {
      const msgTime = new Date(msg.sentTimestamp || msg.timestamp).getTime();
      return msgTime > oneHourAgo;
    });
  }, [messages]);

  return {
    messages,
    reconciliationStatus,
    eodReports,
    redemptionStatus,
    loading,
    error,
    refresh,
    getMessagesByStatus,
    recentMessages: getRecentMessages()
  };
}

export default useSwiftData;
