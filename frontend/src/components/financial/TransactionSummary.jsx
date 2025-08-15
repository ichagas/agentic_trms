import React from 'react';
import { CurrencyDisplay, TransactionStatus } from './FinancialFormatters';

/**
 * TransactionSummary Component - Transaction data display
 * 
 * Features:
 * - Blue-themed styling for transaction information
 * - Transaction ID display with monospace formatting
 * - Currency amount display with proper formatting
 * - Transaction status indicator
 * - Integration with financial formatting utilities
 * 
 * @param {string} content - Message content to parse for transaction details
 */
const TransactionSummary = ({ content }) => {
  // Parse transaction details from content
  const txnIdMatch = content.match(/TXN-(\d+)/);
  const amountMatch = content.match(/\$([0-9,]+\.?[0-9]*)/);
  
  return (
    <div className="bg-blue-50 dark:bg-blue-900/20 rounded-lg p-4 border border-blue-200 dark:border-blue-700">
      <div className="flex items-center justify-between mb-2">
        <h4 className="font-semibold text-blue-900 dark:text-blue-100">
          Transaction Summary
        </h4>
        <TransactionStatus status="COMPLETED" size="sm" />
      </div>
      
      {txnIdMatch && (
        <div className="text-sm text-blue-800 dark:text-blue-200">
          Transaction ID: <code className="bg-blue-100 dark:bg-blue-800 px-1 rounded">{txnIdMatch[0]}</code>
        </div>
      )}
      
      {amountMatch && (
        <div className="mt-2">
          <CurrencyDisplay amount={amountMatch[1]} size="lg" />
        </div>
      )}
    </div>
  );
};

export default TransactionSummary;