import React from 'react';

/**
 * AccountSummaryTable Component - Account data table display
 * 
 * Features:
 * - Professional table styling with proper borders
 * - Responsive design for financial data
 * - Integration point for parsing account data from content
 * - Consistent with other financial component styling
 * 
 * Note: This component currently shows a placeholder. In a real implementation,
 * it would parse account data from the content and display it in a structured table format.
 * 
 * @param {string} content - Message content containing account information
 */
const AccountSummaryTable = ({ content }) => {
  // This would parse account data from content in a real implementation
  // For now, we'll show a simplified version
  
  return (
    <div className="bg-gray-50 dark:bg-gray-800 rounded-lg p-4 border border-gray-200 dark:border-gray-700">
      <h4 className="font-semibold text-gray-900 dark:text-gray-100 mb-3">
        Account Summary
      </h4>
      <div className="text-sm text-gray-600 dark:text-gray-400">
        Detailed account information displayed above
      </div>
    </div>
  );
};

export default AccountSummaryTable;