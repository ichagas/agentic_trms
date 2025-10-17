import React from 'react';
import { Link } from 'react-router-dom';
import useSwiftData from '../hooks/useSwiftData';
import SwiftMessagesPanel from '../components/dashboards/SwiftMessagesPanel';
import ReconciliationPanel from '../components/dashboards/ReconciliationPanel';
import ReportsPanel from '../components/dashboards/ReportsPanel';
import RedemptionPanel from '../components/dashboards/RedemptionPanel';

/**
 * SWIFT Dashboard Page
 *
 * Provides real-time visibility into SWIFT system state:
 * - SWIFT message status and progression
 * - Reconciliation summary
 * - EOD report verification
 * - Redemption report processing
 */
const SwiftDashboard = () => {
  const {
    messages,
    reconciliationStatus,
    eodReports,
    redemptionStatus,
    loading,
    error,
    refresh,
    recentMessages
  } = useSwiftData();

  return (
    <div className="w-full h-full bg-gray-50 dark:bg-gray-900 overflow-auto">
      {/* Navigation Header */}
      <div className="bg-white dark:bg-gray-800 border-b border-gray-200 dark:border-gray-700 px-6 py-4">
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-6">
            <h1 className="text-2xl font-bold text-gray-900 dark:text-white">
              SWIFT Dashboard
            </h1>
            <nav className="flex space-x-4">
              <Link
                to="/"
                className="text-sm text-gray-600 dark:text-gray-300 hover:text-blue-600 dark:hover:text-blue-400 transition-colors"
              >
                Chat
              </Link>
              <Link
                to="/dashboard/trms"
                className="text-sm text-gray-600 dark:text-gray-300 hover:text-blue-600 dark:hover:text-blue-400 transition-colors"
              >
                TRMS
              </Link>
              <span className="text-sm text-blue-600 dark:text-blue-400 font-semibold">
                SWIFT
              </span>
            </nav>
          </div>
          <div className="flex items-center space-x-4">
            {/* Auto-refresh indicator */}
            <div className="text-xs text-gray-500 dark:text-gray-400">
              <span className="inline-block w-2 h-2 bg-green-500 rounded-full mr-1 animate-pulse" />
              Auto-refresh: 5s
            </div>
            <button
              onClick={refresh}
              disabled={loading}
              className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors text-sm font-medium disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {loading ? 'Refreshing...' : 'Refresh Now'}
            </button>
          </div>
        </div>
      </div>

      {/* Dashboard Content */}
      <div className="p-6">
        {error && (
          <div className="mb-6 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-lg p-4">
            <div className="flex items-center">
              <svg className="w-5 h-5 text-red-500 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              <span className="text-red-700 dark:text-red-400 text-sm">
                Error loading data: {error.message}
              </span>
            </div>
          </div>
        )}

        {loading && !messages.length ? (
          <div className="text-center py-20">
            <div className="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mb-4" />
            <h2 className="text-xl font-semibold text-gray-700 dark:text-gray-300 mb-2">
              Loading SWIFT Dashboard...
            </h2>
            <p className="text-gray-500 dark:text-gray-400">
              Fetching data from SWIFT backend
            </p>
          </div>
        ) : (
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            {/* Left Column - SWIFT Messages */}
            <div className="lg:col-span-2">
              <SwiftMessagesPanel
                messages={messages}
                recentMessages={recentMessages}
              />
            </div>

            {/* Right Column - Status Panels */}
            <div className="lg:col-span-1 space-y-6">
              <ReconciliationPanel reconciliationStatus={reconciliationStatus} />
              <ReportsPanel eodReports={eodReports} />
              <RedemptionPanel redemptionStatus={redemptionStatus} />
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default SwiftDashboard;
