import React from 'react';
import EODStatusSummary from './EODStatusSummary';
import AccountSummaryTable from './AccountSummaryTable';
import TransactionSummary from './TransactionSummary';

/**
 * FinancialDataRenderer Component - Renders financial data components based on content analysis
 * 
 * This component analyzes the message content and conditionally renders appropriate
 * financial data components such as EOD status, account summaries, or transaction details.
 * 
 * @param {string} content - Message content to analyze for financial data patterns
 */
const FinancialDataRenderer = ({ content }) => {
  if (!content) return null;
  
  const components = [];
  
  // Check for EOD readiness data
  if (content.includes('EOD Status') && content.includes('Market Data')) {
    components.push(
      <EODStatusSummary key="eod-summary" content={content} />
    );
  }
  
  // Check for account listings
  if (content.includes('USD Accounts') || content.includes('Account USD')) {
    components.push(
      <AccountSummaryTable key="accounts" content={content} />
    );
  }
  
  // Check for transaction data
  if (content.includes('Transaction ID') || content.includes('Transfer Request')) {
    components.push(
      <TransactionSummary key="transaction" content={content} />
    );
  }
  
  return (
    <div className="mt-4 space-y-4">
      {components}
    </div>
  );
};

export default FinancialDataRenderer;