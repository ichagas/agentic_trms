import React from 'react';

/**
 * Reconciliation Panel Component
 *
 * Displays SWIFT message reconciliation status summary
 *
 * @param {Object} reconciliationStatus - Reconciliation summary object
 */
const ReconciliationPanel = ({ reconciliationStatus }) => {
  if (!reconciliationStatus) {
    return (
      <div className="bg-white dark:bg-gray-800 rounded-lg shadow-sm border border-gray-200 dark:border-gray-700 p-4">
        <h2 className="text-base font-semibold text-gray-900 dark:text-white mb-3">
          Reconciliation Status
        </h2>
        <div className="text-center py-4 text-sm text-gray-500 dark:text-gray-400">
          Loading reconciliation data...
        </div>
      </div>
    );
  }

  const totalMessages = reconciliationStatus.totalMessages || 0;
  const reconciledCount = reconciliationStatus.reconciledCount || 0;
  const unreconciledCount = reconciliationStatus.unreconciledCount || 0;
  const pendingCount = reconciliationStatus.pendingCount || 0;

  // Calculate percentages
  const reconciledPercent = totalMessages > 0 ? (reconciledCount / totalMessages) * 100 : 0;
  const unreconciledPercent = totalMessages > 0 ? (unreconciledCount / totalMessages) * 100 : 0;
  const pendingPercent = totalMessages > 0 ? (pendingCount / totalMessages) * 100 : 0;

  return (
    <div className="bg-white dark:bg-gray-800 rounded-lg shadow-sm border border-gray-200 dark:border-gray-700 p-4">
      <div className="flex items-center justify-between mb-3">
        <h2 className="text-base font-semibold text-gray-900 dark:text-white">
          Reconciliation Status
        </h2>
        <button className="text-xs px-2 py-1 bg-blue-600 text-white rounded hover:bg-blue-700 transition-colors">
          Reconcile
        </button>
      </div>

      {/* Total Messages */}
      <div className="text-center mb-3">
        <div className="text-2xl font-bold text-gray-900 dark:text-white mb-0.5">
          {totalMessages}
        </div>
        <div className="text-xs text-gray-500 dark:text-gray-400">
          Total Messages
        </div>
      </div>

      {/* Progress Bar */}
      <div className="mb-3">
        <div className="h-2 bg-gray-200 dark:bg-gray-700 rounded-full overflow-hidden flex">
          {reconciledPercent > 0 && (
            <div
              className="bg-green-500 transition-all duration-500"
              style={{ width: `${reconciledPercent}%` }}
              title={`Reconciled: ${reconciledCount}`}
            />
          )}
          {unreconciledPercent > 0 && (
            <div
              className="bg-red-500 transition-all duration-500"
              style={{ width: `${unreconciledPercent}%` }}
              title={`Unreconciled: ${unreconciledCount}`}
            />
          )}
          {pendingPercent > 0 && (
            <div
              className="bg-yellow-500 transition-all duration-500"
              style={{ width: `${pendingPercent}%` }}
              title={`Pending: ${pendingCount}`}
            />
          )}
        </div>
      </div>

      {/* Status Breakdown */}
      <div className="space-y-2">
        {/* Reconciled */}
        <div className="flex items-center justify-between p-2 bg-green-50 dark:bg-green-900/20 rounded">
          <div className="flex items-center">
            <div className="w-2 h-2 bg-green-500 rounded-full mr-1.5" />
            <span className="text-xs font-medium text-gray-900 dark:text-white">
              Reconciled
            </span>
          </div>
          <div className="flex items-center space-x-2">
            <span className="text-sm font-bold text-green-600 dark:text-green-400">
              {reconciledCount}
            </span>
            <span className="text-xs text-gray-500 dark:text-gray-400">
              {reconciledPercent.toFixed(0)}%
            </span>
          </div>
        </div>

        {/* Unreconciled */}
        {unreconciledCount > 0 && (
          <div className="flex items-center justify-between p-2 bg-red-50 dark:bg-red-900/20 rounded">
            <div className="flex items-center">
              <div className="w-2 h-2 bg-red-500 rounded-full mr-1.5 animate-pulse" />
              <span className="text-xs font-medium text-gray-900 dark:text-white">
                Unreconciled
              </span>
            </div>
            <div className="flex items-center space-x-2">
              <span className="text-sm font-bold text-red-600 dark:text-red-400">
                {unreconciledCount}
              </span>
              <span className="text-xs text-gray-500 dark:text-gray-400">
                {unreconciledPercent.toFixed(0)}%
              </span>
            </div>
          </div>
        )}

        {/* Pending */}
        {pendingCount > 0 && (
          <div className="flex items-center justify-between p-2 bg-yellow-50 dark:bg-yellow-900/20 rounded">
            <div className="flex items-center">
              <div className="w-2 h-2 bg-yellow-500 rounded-full mr-1.5" />
              <span className="text-xs font-medium text-gray-900 dark:text-white">
                Pending
              </span>
            </div>
            <div className="flex items-center space-x-2">
              <span className="text-sm font-bold text-yellow-600 dark:text-yellow-400">
                {pendingCount}
              </span>
              <span className="text-xs text-gray-500 dark:text-gray-400">
                {pendingPercent.toFixed(0)}%
              </span>
            </div>
          </div>
        )}
      </div>

      {/* Last Reconciliation Time */}
      {reconciliationStatus.lastReconciliation && (
        <div className="mt-3 pt-3 border-t border-gray-200 dark:border-gray-700">
          <div className="text-xs text-gray-500 dark:text-gray-400">
            Last: {new Date(reconciliationStatus.lastReconciliation).toLocaleTimeString()}
          </div>
        </div>
      )}

      {/* Summary Message */}
      {reconciliationStatus.summary && (
        <div className="mt-2">
          <div className="text-xs text-gray-600 dark:text-gray-400 italic">
            {reconciliationStatus.summary}
          </div>
        </div>
      )}
    </div>
  );
};

export default ReconciliationPanel;
