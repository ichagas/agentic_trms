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
    { id: 'accounts', label: '📊 View Accounts', query: 'Show me all USD accounts' },
    { id: 'eod', label: '🕐 EOD Check', query: 'Can we run End-of-Day?' },
    { id: 'balance', label: '💰 Check Balance', query: 'What\'s the balance for ACC-001-USD?' },
    { id: 'transfer', label: '💸 Transfer', query: 'Transfer $50,000 from ACC-001-USD to ACC-002-USD' },
    { id: 'rates', label: '📈 Rate Fixings', query: 'Propose missing rate fixings' },
  ];
  
  return (
    <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 xl:grid-cols-5 gap-2 sm:gap-3 md:gap-4 mt-4">
      {actions.map((action) => (
        <button
          key={action.id}
          onClick={() => onActionClick(action.query)}
          className="p-3 bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-lg text-sm text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors text-left"
        >
          <div className="font-medium">{action.label}</div>
        </button>
      ))}
    </div>
  );
};