import React, { useState } from 'react';

/**
 * Transactions Panel Component
 *
 * Displays recent transactions with highlighting for new transactions
 * Allows approval of PENDING transactions
 *
 * @param {Array} transactions - List of transaction objects
 * @param {Array} recentTransactions - List of transactions from last 5 minutes
 * @param {Function} onApprove - Callback when transaction is approved
 */
const TransactionsPanel = ({ transactions = [], recentTransactions = [], onApprove }) => {
  const [approvingTx, setApprovingTx] = useState(null);

    document.title = "TRMS";

  // Handle transaction approval
  const handleApprove = async (transaction) => {
    setApprovingTx(transaction.transactionId);
    try {
      // Call backend to approve transaction
      const response = await fetch(`http://localhost:8090/api/v1/transactions/${transaction.transactionId}/approve`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' }
      });

      if (response.ok) {
        console.log('Transaction approved:', transaction.transactionId);
        if (onApprove) {
          onApprove(transaction);
        }
      } else {
        console.error('Failed to approve transaction');
      }
    } catch (error) {
      console.error('Error approving transaction:', error);
    } finally {
      setApprovingTx(null);
    }
  };
  // Format currency value
  const formatCurrency = (amount, currency) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: currency || 'USD',
      minimumFractionDigits: 2
    }).format(amount);
  };

  // Format timestamp
  const formatTimestamp = (timestamp) => {
    if (!timestamp) return 'N/A';
    const date = new Date(timestamp);
    return date.toLocaleString('en-US', {
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    });
  };

  // Check if transaction is recent
  const isRecent = (txId) => {
    return recentTransactions.some(tx => tx.id === txId);
  };

  // Get status badge color
  const getStatusColor = (status) => {
    switch (status?.toUpperCase()) {
      case 'COMPLETED':
        return 'bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-400';
      case 'PENDING':
        return 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900/30 dark:text-yellow-400';
      case 'FAILED':
        return 'bg-red-100 text-red-800 dark:bg-red-900/30 dark:text-red-400';
      default:
        return 'bg-gray-100 text-gray-800 dark:bg-gray-700 dark:text-gray-300';
    }
  };

  // Sort transactions by createdAt (newest first), prioritize PENDING, limit to 20
  const sortedTransactions = [...transactions]
    .sort((a, b) => {
      // Prioritize PENDING transactions
      if (a.status === 'PENDING' && b.status !== 'PENDING') return -1;
      if (a.status !== 'PENDING' && b.status === 'PENDING') return 1;

      // Then sort by timestamp (createdAt or timestamp)
      const aTime = new Date(a.createdAt || a.timestamp).getTime();
      const bTime = new Date(b.createdAt || b.timestamp).getTime();
      return bTime - aTime;
    })
    .slice(0, 20);

  return (
    <div className="bg-white dark:bg-gray-800 rounded-lg shadow-sm border border-gray-200 dark:border-gray-700 p-4">
      <div className="flex items-center justify-between mb-4">
        <h2 className="text-base font-semibold text-gray-900 dark:text-white">
          Recent Transactions
        </h2>
        <span className="text-xs text-gray-500 dark:text-gray-400">
          {transactions.length} total {recentTransactions.length > 0 && (
            <span className="ml-2 px-2 py-0.5 bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-400 rounded-full text-xs font-medium">
              {recentTransactions.length} new
            </span>
          )}
        </span>
      </div>

      {sortedTransactions.length === 0 ? (
        <div className="text-center py-8 text-gray-500 dark:text-gray-400">
          No transactions available
        </div>
      ) : (
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead>
              <tr className="border-b border-gray-200 dark:border-gray-700">
                <th className="text-left py-2 px-3 text-xs font-semibold text-gray-600 dark:text-gray-400 uppercase tracking-wider">
                  ID
                </th>
                <th className="text-left py-2 px-3 text-xs font-semibold text-gray-600 dark:text-gray-400 uppercase tracking-wider">
                  From
                </th>
                <th className="text-left py-2 px-3 text-xs font-semibold text-gray-600 dark:text-gray-400 uppercase tracking-wider">
                  To
                </th>
                <th className="text-right py-2 px-3 text-xs font-semibold text-gray-600 dark:text-gray-400 uppercase tracking-wider">
                  Amount
                </th>
                <th className="text-center py-2 px-3 text-xs font-semibold text-gray-600 dark:text-gray-400 uppercase tracking-wider">
                  Status
                </th>
                <th className="text-right py-2 px-3 text-xs font-semibold text-gray-600 dark:text-gray-400 uppercase tracking-wider">
                  Time
                </th>
                <th className="text-center py-2 px-3 text-xs font-semibold text-gray-600 dark:text-gray-400 uppercase tracking-wider">
                  Actions
                </th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200 dark:divide-gray-700">
              {sortedTransactions.map(tx => (
                <tr
                  key={tx.id}
                  className={`transition-all duration-300 ${
                    isRecent(tx.id)
                      ? 'bg-green-50 dark:bg-green-900/20 animate-slide-in'
                      : 'hover:bg-gray-50 dark:hover:bg-gray-700/50'
                  }`}
                >
                  <td className="py-2 px-3">
                    <div className="text-sm font-mono text-gray-900 dark:text-white">
                      {tx.id}
                    </div>
                  </td>
                  <td className="py-2 px-3">
                    <div className="text-sm text-gray-700 dark:text-gray-300">
                      {tx.fromAccount || 'N/A'}
                    </div>
                  </td>
                  <td className="py-2 px-3">
                    <div className="text-sm text-gray-700 dark:text-gray-300">
                      {tx.toAccount || 'N/A'}
                    </div>
                  </td>
                  <td className="py-2 px-3 text-right">
                    <div className={`text-sm font-semibold ${
                      isRecent(tx.id)
                        ? 'text-green-600 dark:text-green-400'
                        : 'text-gray-900 dark:text-white'
                    }`}>
                      {formatCurrency(tx.amount, tx.currency)}
                    </div>
                  </td>
                  <td className="py-2 px-3 text-center">
                    <span className={`inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium ${
                      getStatusColor(tx.status)
                    }`}>
                      {tx.status || 'UNKNOWN'}
                    </span>
                  </td>
                  <td className="py-2 px-3 text-right">
                    <div className="text-xs text-gray-500 dark:text-gray-400">
                      {formatTimestamp(tx.createdAt || tx.timestamp)}
                    </div>
                  </td>
                  <td className="py-2 px-3 text-center">
                    {tx.status === 'PENDING' ? (
                      <button
                        onClick={() => handleApprove(tx)}
                        disabled={approvingTx === tx.transactionId}
                        className="inline-flex items-center px-3 py-1 text-xs font-medium rounded-md text-white bg-green-600 hover:bg-green-700 disabled:bg-gray-400 disabled:cursor-not-allowed transition-colors"
                      >
                        {approvingTx === tx.transactionId ? (
                          <>
                            <svg className="animate-spin -ml-0.5 mr-1.5 h-3 w-3 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                              <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                              <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                            </svg>
                            Approving...
                          </>
                        ) : (
                          <>
                            <svg className="w-3 h-3 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                            </svg>
                            Approve
                          </>
                        )}
                      </button>
                    ) : (
                      <span className="text-xs text-gray-400">—</span>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};

export default TransactionsPanel;
