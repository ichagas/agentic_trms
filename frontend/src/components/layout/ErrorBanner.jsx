import React from 'react';
import { AlertCircle } from 'lucide-react';

/**
 * ErrorBanner Component - Error display with dismiss functionality
 * 
 * Features:
 * - Red-themed error styling with left border
 * - AlertCircle icon for visual emphasis
 * - Dismiss button (X) to close the error
 * - Dark mode support
 * 
 * @param {string} error - Error message to display
 * @param {Function} onDismiss - Callback when user dismisses the error
 */
const ErrorBanner = ({ error, onDismiss }) => (
  <div className="bg-red-50 dark:bg-red-900/20 border-l-4 border-red-500 p-4 flex items-center justify-between">
    <div className="flex items-center gap-2">
      <AlertCircle size={16} className="text-red-500" />
      <span className="text-red-800 dark:text-red-200">{error}</span>
    </div>
    <button
      onClick={onDismiss}
      className="text-red-500 hover:text-red-700 text-lg font-bold"
    >
      ×
    </button>
  </div>
);

export default ErrorBanner;