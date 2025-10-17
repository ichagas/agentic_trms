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

  // Sort by timestamp (newest first) and limit to 15
  const sortedMessages = [...filteredMessages]
    .sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp))
    .slice(0, 15);

  // Get status counts
  const statusCounts = {
    ALL: messages.length,
    PENDING: messages.filter(m => m.status === 'PENDING').length,
    SENT: messages.filter(m => m.status === 'SENT').length,
    CONFIRMED: messages.filter(m => m.status === 'CONFIRMED').length,
    RECONCILED: messages.filter(m => m.status === 'RECONCILED').length,
    UNRECONCILED: messages.filter(m => m.status === 'UNRECONCILED').length
  };

  return (
    <div className="bg-white dark:bg-gray-800 rounded-lg shadow-sm border border-gray-200 dark:border-gray-700 p-6">
      <div className="flex items-center justify-between mb-6">
        <h2 className="text-lg font-semibold text-gray-900 dark:text-white">
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
        <div className="text-center py-8 text-gray-500 dark:text-gray-400">
          No SWIFT messages {filterStatus !== 'ALL' && `with status "${filterStatus}"`}
        </div>
      ) : (
        <div className="space-y-3">
          {sortedMessages.map(msg => {
            const statusInfo = getStatusInfo(msg.status);
            return (
              <div
                key={msg.id}
                className={`border border-gray-200 dark:border-gray-700 rounded-lg p-4 transition-all duration-300 ${
                  isRecent(msg.id)
                    ? 'bg-blue-50 dark:bg-blue-900/20 ring-2 ring-blue-400 animate-scale-in'
                    : 'hover:bg-gray-50 dark:hover:bg-gray-700/50'
                }`}
              >
                {/* Message Header */}
                <div className="flex items-start justify-between mb-3">
                  <div className="flex-1">
                    <div className="flex items-center space-x-2">
                      <span className="font-mono text-sm font-semibold text-gray-900 dark:text-white">
                        {msg.id}
                      </span>
                      <span className="text-xs px-2 py-0.5 bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-400 rounded">
                        {msg.type || 'MT103'}
                      </span>
                    </div>
                    <div className="text-xs text-gray-500 dark:text-gray-400 mt-1">
                      {formatTimestamp(msg.timestamp)}
                    </div>
                  </div>
                  <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${statusInfo.color}`}>
                    <span className="mr-1">{statusInfo.icon}</span>
                    {msg.status}
                  </span>
                </div>

                {/* Message Details */}
                <div className="grid grid-cols-2 gap-3 text-sm">
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
                  {msg.beneficiaryName && (
                    <div className="col-span-2">
                      <div className="text-xs text-gray-500 dark:text-gray-400">Beneficiary</div>
                      <div className="text-gray-900 dark:text-white">{msg.beneficiaryName}</div>
                    </div>
                  )}
                </div>

                {/* Status Progression */}
                <div className="mt-3 pt-3 border-t border-gray-200 dark:border-gray-700">
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
