import React from 'react';

/**
 * SuggestedActions Component - Display suggested queries with click handlers
 * 
 * Features:
 * - Gradient background with blue/indigo theme
 * - Responsive button layout with proper spacing
 * - Hover effects and transition animations
 * - Professional TRMS-themed styling
 * - Dark mode support
 * 
 * @param {Array} suggestions - Array of suggestion strings
 * @param {Function} onSuggestionClick - Callback when user clicks a suggestion
 */
export const SuggestedActions = ({ suggestions = [], onSuggestionClick }) => {
  if (!suggestions.length) return null;
  
  return (
    <div className="mt-4 p-4 sm:p-6 md:p-8 bg-gradient-to-r from-blue-50 to-indigo-50 dark:from-blue-900/20 dark:to-indigo-900/20 rounded-lg border border-blue-200 dark:border-blue-700">
      <h4 className="text-sm font-semibold text-blue-900 dark:text-blue-100 mb-3">
        💡 Try these queries:
      </h4>
      <div className="flex flex-wrap gap-2 sm:gap-3 md:gap-4">
        {suggestions.map((suggestion, index) => (
          <button
            key={index}
            onClick={() => onSuggestionClick(suggestion)}
            className="px-3 py-1.5 bg-white dark:bg-gray-800 border border-blue-200 dark:border-blue-600 text-blue-700 dark:text-blue-300 rounded-lg text-sm hover:bg-blue-50 dark:hover:bg-blue-900/30 transition-colors"
          >
            {suggestion}
          </button>
        ))}
      </div>
    </div>
  );
};

/**
 * QuickActions Component - Grid of quick action buttons for common operations
 * 
 * Features:
 * - Responsive grid layout (2-5 columns based on screen size)
 * - Emoji icons for visual identification
 * - Predefined common TRMS operations
 * - Hover effects and professional styling
 * - Touch-friendly design for mobile devices
 * 
 * @param {Function} onActionClick - Callback when user clicks an action
 */
export const QuickActions = ({ onActionClick }) => {
  const actions = [
    { id: 'accounts', label: '💰 View Accounts', query: 'Show me all USD accounts with their current balances' },
    { id: 'eod-both', label: '🎯 EOD Check', query: 'Can we run EOD? Check both TRMS and SWIFT readiness' },
    { id: 'transfer', label: '💸 Book Transfer', query: 'Transfer $50,000 from ACC-001-USD to ACC-002-USD' },
    { id: 'swift-validate', label: '🔐 SWIFT Only', query: 'Is SWIFT ready for EOD? Validate all systems' },
    { id: 'rates', label: '📈 Rate Fixings', query: 'Propose missing rate fixings for all instruments' },
    { id: 'verification', label: '✅ EOD Reports', query: 'Verify today\'s EOD reports in the shared drive' },
    { id: 'redemption', label: '📄 Redemption', query: 'Process the latest redemption report from the shared drive' },
    { id: 'reconcile', label: '🔄 Reconcile', query: 'Check unreconciled SWIFT messages and reconcile them' },
  ];
  
  return (
    <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-4 xl:grid-cols-4 gap-2 sm:gap-3 md:gap-4 mt-4">
      {actions.map((action) => (
        <button
          key={action.id}
          onClick={() => onActionClick(action.query)}
          className="p-3 bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-lg text-sm text-gray-700 dark:text-gray-300 hover:bg-blue-50 dark:hover:bg-blue-900/20 hover:border-blue-300 dark:hover:border-blue-600 transition-all text-left group"
        >
          <div className="font-medium group-hover:text-blue-600 dark:group-hover:text-blue-400 transition-colors">{action.label}</div>
        </button>
      ))}
    </div>
  );
};