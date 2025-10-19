import React, { useState } from 'react';

/**
 * SWIFT Messages Panel Component
 *
 * Displays SWIFT messages with status progression visualization
 *
 * @param {Array} messages - List of SWIFT message objects
 * @param {Array} recentMessages - Messages from last hour
 */
const SwiftMessagesPanel = ({ messages = [], recentMessages = [] }) => {
  const [filterStatus, setFilterStatus] = useState('ALL');
  const [updatingMessage, setUpdatingMessage] = useState(null);
  const [transactionIdInput, setTransactionIdInput] = useState({});

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
      minute: '2-digit'
    });
  };

  // Check if message is recent
  const isRecent = (msgId) => {
    return recentMessages.some(msg => msg.id === msgId);
  };

  // Get status color and icon
  const getStatusInfo = (status) => {
    switch (status?.toUpperCase()) {
      case 'PENDING':
        return {
          color: 'bg-gray-100 text-gray-800 dark:bg-gray-700 dark:text-gray-300',
          icon: '⏳',
          step: 1
        };
      case 'SENT':
        return {
          color: 'bg-blue-100 text-blue-800 dark:bg-blue-900/30 dark:text-blue-400',
          icon: '📤',
          step: 2
        };
      case 'CONFIRMED':
        return {
          color: 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900/30 dark:text-yellow-400',
          icon: '✓',
          step: 3
        };
      case 'RECONCILED':
        return {
          color: 'bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-400',
          icon: '✓✓',
          step: 4
        };
      case 'UNRECONCILED':
        return {
          color: 'bg-red-100 text-red-800 dark:bg-red-900/30 dark:text-red-400',
          icon: '!',
          step: 3
        };
      case 'FAILED':
        return {
          color: 'bg-red-100 text-red-800 dark:bg-red-900/30 dark:text-red-400',
          icon: '✗',
          step: 0
        };
      default:
        return {
          color: 'bg-gray-100 text-gray-800 dark:bg-gray-700 dark:text-gray-300',
          icon: '?',
          step: 0
        };
    }
  };

  // Filter messages
  const filteredMessages = filterStatus === 'ALL'
    ? messages
    : messages.filter(msg => msg.status === filterStatus);

  // Sort by timestamp (newest first) and limit to 8
  const sortedMessages = [...filteredMessages]
    .sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp))
    .slice(0, 8);

  // Get status counts
  const statusCounts = {
    ALL: messages.length,
    PENDING: messages.filter(m => m.status === 'PENDING').length,
    SENT: messages.filter(m => m.status === 'SENT').length,
    CONFIRMED: messages.filter(m => m.status === 'CONFIRMED').length,
    RECONCILED: messages.filter(m => m.status === 'RECONCILED').length,
    UNRECONCILED: messages.filter(m => m.status === 'UNRECONCILED').length
  };

  // Update SWIFT message transaction ID
  const handleUpdateTransactionId = async (messageId) => {
    const transactionId = transactionIdInput[messageId];

    if (!transactionId || !transactionId.trim()) {
      alert('Please enter a transaction ID');
      return;
    }

    setUpdatingMessage(messageId);

    try {
      const response = await fetch(
        `http://localhost:8091/api/v1/swift/messages/${messageId}/transaction?transactionId=${encodeURIComponent(transactionId.trim())}`,
        {
          method: 'PATCH',
          headers: {
            'Content-Type': 'application/json'
          }
        }
      );

      if (!response.ok) {
        throw new Error('Failed to update transaction ID');
      }

      // Clear input
      setTransactionIdInput(prev => ({ ...prev, [messageId]: '' }));

      // Show success
      alert(`✅ SWIFT message ${messageId} linked to transaction ${transactionId.trim()}`);

      // Refresh page after a short delay
      setTimeout(() => window.location.reload(), 1000);
    } catch (error) {
      console.error('Error updating transaction ID:', error);
      alert(`❌ Error: ${error.message}`);
    } finally {
      setUpdatingMessage(null);
    }
  };

  return (
    <div className="bg-white dark:bg-gray-800 rounded-lg shadow-sm border border-gray-200 dark:border-gray-700 p-4">
      <div className="flex items-center justify-between mb-4">
        <h2 className="text-base font-semibold text-gray-900 dark:text-white">
          SWIFT Messages
        </h2>
        <div className="flex items-center space-x-2">
          {/* Status Filter */}
          <select
            value={filterStatus}
            onChange={(e) => setFilterStatus(e.target.value)}
            className="text-sm border border-gray-300 dark:border-gray-600 rounded-lg px-3 py-1.5 bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-blue-500"
          >
            <option value="ALL">All ({statusCounts.ALL})</option>
            <option value="PENDING">Pending ({statusCounts.PENDING})</option>
            <option value="SENT">Sent ({statusCounts.SENT})</option>
            <option value="CONFIRMED">Confirmed ({statusCounts.CONFIRMED})</option>
            <option value="RECONCILED">Reconciled ({statusCounts.RECONCILED})</option>
            <option value="UNRECONCILED">Unreconciled ({statusCounts.UNRECONCILED})</option>
          </select>
        </div>
      </div>

      {sortedMessages.length === 0 ? (
        <div className="text-center py-4 text-sm text-gray-500 dark:text-gray-400">
          No SWIFT messages {filterStatus !== 'ALL' && `with status "${filterStatus}"`}
        </div>
      ) : (
        <div className="space-y-2 max-h-[600px] overflow-y-auto pr-1">
          {sortedMessages.map(msg => {
            const statusInfo = getStatusInfo(msg.status);
            return (
              <div
                key={msg.id}
                className={`border border-gray-200 dark:border-gray-700 rounded-lg p-3 transition-all duration-300 ${
                  isRecent(msg.id)
                    ? 'bg-blue-50 dark:bg-blue-900/20 ring-2 ring-blue-400 animate-scale-in'
                    : 'hover:bg-gray-50 dark:hover:bg-gray-700/50'
                }`}
              >
                {/* Message Header */}
                <div className="flex items-start justify-between mb-2">
                  <div className="flex-1">
                    <div className="flex items-center space-x-2">
                      <span className="font-mono text-xs font-semibold text-gray-900 dark:text-white">
                        {msg.id}
                      </span>
                      <span className="text-xs px-1.5 py-0.5 bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-400 rounded">
                        {msg.type || 'MT103'}
                      </span>
                    </div>
                    <div className="text-xs text-gray-500 dark:text-gray-400 mt-0.5">
                      {formatTimestamp(msg.timestamp)}
                    </div>
                  </div>
                  <span className={`inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium ${statusInfo.color}`}>
                    <span className="mr-1">{statusInfo.icon}</span>
                    {msg.status}
                  </span>
                </div>

                {/* Message Details */}
                <div className="grid grid-cols-2 gap-2 text-sm">
                  <div>
                    <div className="text-xs text-gray-500 dark:text-gray-400">Account</div>
                    <div className="font-medium text-gray-900 dark:text-white">{msg.accountId || 'N/A'}</div>
                  </div>
                  <div>
                    <div className="text-xs text-gray-500 dark:text-gray-400">Amount</div>
                    <div className="font-semibold text-gray-900 dark:text-white">
                      {formatCurrency(msg.amount, msg.currency)}
                    </div>
                  </div>
                  <div className="col-span-2">
                    <div className="text-xs text-gray-500 dark:text-gray-400">Transaction ID</div>
                    <div className="font-medium text-gray-900 dark:text-white">
                      {msg.transactionId || (
                        <span className="text-red-600 dark:text-red-400 font-semibold">Missing - Needs Manual Link</span>
                      )}
                    </div>
                  </div>
                  {msg.beneficiaryName && (
                    <div className="col-span-2">
                      <div className="text-xs text-gray-500 dark:text-gray-400">Beneficiary</div>
                      <div className="text-gray-900 dark:text-white">{msg.beneficiaryName}</div>
                    </div>
                  )}
                </div>

                {/* Transaction ID Update Section - Only for unreconciled messages */}
                {!msg.transactionId && msg.status !== 'RECONCILED' && (
                  <div className="mt-2 pt-2 border-t border-gray-200 dark:border-gray-700">
                    <div className="bg-yellow-50 dark:bg-yellow-900/20 border border-yellow-200 dark:border-yellow-800 rounded p-2">
                      <div className="text-xs font-semibold text-yellow-800 dark:text-yellow-400 mb-1.5">
                        ⚠️ Manual Reconciliation Required
                      </div>
                      <div className="flex items-center space-x-2">
                        <input
                          type="text"
                          placeholder="Enter Transaction ID (e.g., TXN-ABC123)"
                          value={transactionIdInput[msg.id] || ''}
                          onChange={(e) => setTransactionIdInput(prev => ({ ...prev, [msg.id]: e.target.value }))}
                          disabled={updatingMessage === msg.id}
                          className="flex-1 text-xs border border-gray-300 dark:border-gray-600 rounded px-2 py-1 bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-blue-500 disabled:opacity-50"
                        />
                        <button
                          onClick={() => handleUpdateTransactionId(msg.id)}
                          disabled={updatingMessage === msg.id || !transactionIdInput[msg.id]}
                          className="px-3 py-1 bg-blue-600 text-white text-xs font-medium rounded hover:bg-blue-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed whitespace-nowrap"
                        >
                          {updatingMessage === msg.id ? 'Linking...' : 'Link'}
                        </button>
                      </div>
                      <div className="text-xs text-gray-600 dark:text-gray-400 mt-1.5">
                        Creates audit trail linking SWIFT to internal transaction
                      </div>
                    </div>
                  </div>
                )}

                {/* Status Progression */}
                <div className="mt-2 pt-2 border-t border-gray-200 dark:border-gray-700">
                  <div className="flex items-center justify-between text-xs">
                    <div className={`${statusInfo.step >= 1 ? 'text-blue-600 dark:text-blue-400 font-medium' : 'text-gray-400'}`}>
                      Pending
                    </div>
                    <div className={`flex-1 h-0.5 mx-2 ${statusInfo.step >= 2 ? 'bg-blue-500' : 'bg-gray-300'}`} />
                    <div className={`${statusInfo.step >= 2 ? 'text-blue-600 dark:text-blue-400 font-medium' : 'text-gray-400'}`}>
                      Sent
                    </div>
                    <div className={`flex-1 h-0.5 mx-2 ${statusInfo.step >= 3 ? 'bg-blue-500' : 'bg-gray-300'}`} />
                    <div className={`${statusInfo.step >= 3 ? 'text-yellow-600 dark:text-yellow-400 font-medium' : 'text-gray-400'}`}>
                      Confirmed
                    </div>
                    <div className={`flex-1 h-0.5 mx-2 ${statusInfo.step >= 4 ? 'bg-green-500' : 'bg-gray-300'}`} />
                    <div className={`${statusInfo.step >= 4 ? 'text-green-600 dark:text-green-400 font-medium' : 'text-gray-400'}`}>
                      Reconciled
                    </div>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
};

export default SwiftMessagesPanel;
