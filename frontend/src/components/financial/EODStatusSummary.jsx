import React from 'react';
import clsx from 'clsx';
import { DataCompleteness, FinancialTimestamp } from './FinancialFormatters';

/**
 * EODStatusSummary Component - End-of-Day status display
 * 
 * Features:
 * - EOD readiness status with color-coded indicators
 * - Market data completeness progress display
 * - Real-time timestamp showing last update
 * - Responsive design with proper spacing
 * - Professional financial interface styling
 * 
 * @param {string} content - Message content to parse for EOD status information
 */
const EODStatusSummary = ({ content }) => {
  // Parse key metrics from content
  const eodReady = content.includes('✅') && !content.includes('⚠️');
  const marketDataMatch = content.match(/(\d+)\/(\d+)/);
  
  return (
    <div className="bg-gray-50 dark:bg-gray-800 rounded-lg p-4 border border-gray-200 dark:border-gray-700">
      <div className="flex items-center justify-between mb-3">
        <h4 className="font-semibold text-gray-900 dark:text-gray-100">
          EOD Readiness Status
        </h4>
        <span className={clsx(
          'px-3 py-1 rounded-full text-sm font-medium',
          eodReady 
            ? 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200'
            : 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200'
        )}>
          {eodReady ? '✅ Ready' : '⚠️ Pending Actions'}
        </span>
      </div>
      
      {marketDataMatch && (
        <div className="space-y-2">
          <DataCompleteness
            current={parseInt(marketDataMatch[1])}
            total={parseInt(marketDataMatch[2])}
            label="Market Data"
            size="sm"
          />
        </div>
      )}
      
      <div className="mt-3 text-xs text-gray-600 dark:text-gray-400">
        Last updated: <FinancialTimestamp timestamp={new Date()} showRelative={true} />
      </div>
    </div>
  );
};

export default EODStatusSummary;