import { useState, useEffect, useCallback, useRef } from 'react';

const TRMS_BASE_URL = 'http://localhost:8090/api/v1';
const POLL_INTERVAL = 5000; // 5 seconds

/**
 * Custom hook for fetching TRMS dashboard data with automatic polling
 *
 * @returns {Object} TRMS data and loading state
 * @returns {Array} returns.accounts - List of all accounts with balances
 * @returns {Array} returns.transactions - Recent transactions
 * @returns {Object} returns.eodStatus - EOD readiness status
 * @returns {boolean} returns.loading - Loading state
 * @returns {Error} returns.error - Error object if fetch fails
 * @returns {Function} returns.refresh - Manual refresh function
 */
export function useTrmsData() {
  const [accounts, setAccounts] = useState([]);
  const [transactions, setTransactions] = useState([]);
  const [eodStatus, setEodStatus] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const previousDataRef = useRef({ accounts: [], transactions: [] });

  /**
   * Fetch all TRMS data from backend
   */
  const fetchData = useCallback(async () => {
    try {
      // Store previous data for change detection (using ref to avoid dependency issues)
      previousDataRef.current = {
        accounts: accounts,
        transactions: transactions
      };

      // Fetch accounts
      const accountsResponse = await fetch(`${TRMS_BASE_URL}/accounts`);
      if (!accountsResponse.ok) throw new Error('Failed to fetch accounts');
      const accountsData = await accountsResponse.json();

      // Fetch balances for each account
      const accountsWithBalances = await Promise.all(
        accountsData.map(async (account) => {
          try {
            const balanceResponse = await fetch(`${TRMS_BASE_URL}/accounts/${account.accountId}/balance`);
            if (balanceResponse.ok) {
              const balanceData = await balanceResponse.json();
              return {
                id: account.accountId,
                name: account.accountName,
                currency: account.currency,
                status: account.status,
                balance: balanceData.currentBalance,
                availableBalance: balanceData.availableBalance,
                reservedBalance: balanceData.reservedBalance,
                ...account
              };
            }
          } catch (err) {
            console.error(`Failed to fetch balance for ${account.accountId}:`, err);
          }
          // Return account without balance if fetch fails
          return {
            id: account.accountId,
            name: account.accountName,
            currency: account.currency,
            status: account.status,
            balance: 0,
            ...account
          };
        })
      );

      // Fetch transactions
      const transactionsResponse = await fetch(`${TRMS_BASE_URL}/transactions`);
      if (!transactionsResponse.ok) throw new Error('Failed to fetch transactions');
      const transactionsData = await transactionsResponse.json();

      // Fetch EOD readiness status
      const eodResponse = await fetch(`${TRMS_BASE_URL}/eod/readiness`);
      if (!eodResponse.ok) throw new Error('Failed to fetch EOD status');
      const eodData = await eodResponse.json();

      // Update state
      setAccounts(accountsWithBalances);
      setTransactions(transactionsData);
      setEodStatus(eodData);
      setError(null);
      setLoading(false);

      console.log('[TRMS]', new Date().toLocaleTimeString(), 'Data fetched:', {
        accounts: accountsWithBalances.length,
        transactions: transactionsData.length,
        eodReady: eodData?.ready
      });

    } catch (err) {
      console.error('Error fetching TRMS data:', err);
      setError(err);
      setLoading(false);
    }
  }, []); // Remove dependencies to prevent infinite loop

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
   * Detect which accounts have changed
   */
  const getChangedAccounts = useCallback(() => {
    const previousAccounts = previousDataRef.current.accounts;
    if (!previousAccounts || previousAccounts.length === 0) {
      return [];
    }

    const changed = [];
    accounts.forEach(account => {
      const prev = previousAccounts.find(a => a.id === account.id);
      if (prev && prev.balance !== account.balance) {
        changed.push(account.id);
      }
    });

    return changed;
  }, [accounts]);

  /**
   * Detect which transactions are new (within last 5 minutes)
   */
  const getRecentTransactions = useCallback(() => {
    const fiveMinutesAgo = Date.now() - (5 * 60 * 1000);
    return transactions.filter(tx => {
      const txTime = new Date(tx.timestamp).getTime();
      return txTime > fiveMinutesAgo;
    });
  }, [transactions]);

  return {
    accounts,
    transactions,
    eodStatus,
    loading,
    error,
    refresh,
    changedAccounts: getChangedAccounts(),
    recentTransactions: getRecentTransactions()
  };
}

export default useTrmsData;
