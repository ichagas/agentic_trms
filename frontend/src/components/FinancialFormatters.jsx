import React from 'react';
import { format } from 'date-fns';
import clsx from 'clsx';

/**
 * Utility function to format currency amounts
 */
export const formatCurrency = (amount, currency = 'USD', options = {}) => {
  const {
    showSign = false,
    abbreviated = false,
    precision = 2,
  } = options;

  if (amount === null || amount === undefined || isNaN(amount)) {
    return 'N/A';
  }

  const numAmount = parseFloat(amount);
  
  // Abbreviate large numbers if requested
  if (abbreviated && Math.abs(numAmount) >= 1000000) {
    const abbreviated = numAmount / 1000000;
    return `${currency} ${abbreviated.toFixed(1)}M`;
  } else if (abbreviated && Math.abs(numAmount) >= 1000) {
    const abbreviated = numAmount / 1000;
    return `${currency} ${abbreviated.toFixed(1)}K`;
  }

  // Format with proper locale
  const formatted = new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: currency,
    minimumFractionDigits: precision,
    maximumFractionDigits: precision,
  }).format(Math.abs(numAmount));

  // Add sign if requested or if negative
  const sign = numAmount < 0 ? '-' : (showSign && numAmount > 0 ? '+' : '');
  
  return `${sign}${formatted}`;
};

/**
 * Component for displaying currency amounts with proper styling
 */
export const CurrencyDisplay = ({ 
  amount, 
  currency = 'USD', 
  size = 'md',
  colorizeSign = true,
  ...options 
}) => {
  const numAmount = parseFloat(amount);
  const isNegative = numAmount < 0;
  const isZero = numAmount === 0;
  
  const sizeClasses = {
    sm: 'text-sm',
    md: 'text-base',
    lg: 'text-lg font-semibold',
    xl: 'text-xl font-bold',
  };

  const colorClasses = colorizeSign ? {
    positive: 'text-green-600 dark:text-green-400',
    negative: 'text-red-600 dark:text-red-400',
    zero: 'text-gray-600 dark:text-gray-400',
  } : {};

  const className = clsx(
    sizeClasses[size],
    'font-mono',
    colorizeSign && isNegative && colorClasses.negative,
    colorizeSign && !isNegative && !isZero && colorClasses.positive,
    colorizeSign && isZero && colorClasses.zero
  );

  return (
    <span className={className}>
      {formatCurrency(amount, currency, options)}
    </span>
  );
};

/**
 * Component for displaying account status with proper indicators
 */
export const AccountStatus = ({ status, size = 'sm' }) => {
  const statusConfig = {
    ACTIVE: { 
      label: 'Active', 
      color: 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200',
      icon: '🟢' 
    },
    PENDING_APPROVAL: { 
      label: 'Pending Approval', 
      color: 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200',
      icon: '🟡' 
    },
    SUSPENDED: { 
      label: 'Suspended', 
      color: 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200',
      icon: '🔴' 
    },
    CLOSED: { 
      label: 'Closed', 
      color: 'bg-gray-100 text-gray-800 dark:bg-gray-900 dark:text-gray-200',
      icon: '⚫' 
    },
  };

  const config = statusConfig[status] || statusConfig.ACTIVE;
  
  const sizeClasses = {
    sm: 'px-2 py-1 text-xs',
    md: 'px-3 py-1 text-sm',
    lg: 'px-4 py-2 text-base',
  };

  return (
    <span className={clsx(
      'inline-flex items-center gap-1 rounded-full font-medium',
      config.color,
      sizeClasses[size]
    )}>
      <span>{config.icon}</span>
      {config.label}
    </span>
  );
};

/**
 * Component for displaying transaction status
 */
export const TransactionStatus = ({ status, size = 'sm' }) => {
  const statusConfig = {
    COMPLETED: { 
      label: 'Completed', 
      color: 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200',
      icon: '✅' 
    },
    PENDING: { 
      label: 'Pending', 
      color: 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200',
      icon: '⏳' 
    },
    FAILED: { 
      label: 'Failed', 
      color: 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200',
      icon: '❌' 
    },
    PROCESSING: { 
      label: 'Processing', 
      color: 'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200',
      icon: '🔄' 
    },
    VALIDATED: { 
      label: 'Validated', 
      color: 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200',
      icon: '🟢' 
    },
    NEW: { 
      label: 'New', 
      color: 'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200',
      icon: '🆕' 
    },
    PROPOSAL: { 
      label: 'Proposal', 
      color: 'bg-purple-100 text-purple-800 dark:bg-purple-900 dark:text-purple-200',
      icon: '📝' 
    },
  };

  const config = statusConfig[status] || statusConfig.PENDING;
  
  const sizeClasses = {
    sm: 'px-2 py-1 text-xs',
    md: 'px-3 py-1 text-sm',
    lg: 'px-4 py-2 text-base',
  };

  return (
    <span className={clsx(
      'inline-flex items-center gap-1 rounded-full font-medium',
      config.color,
      sizeClasses[size]
    )}>
      <span>{config.icon}</span>
      {config.label}
    </span>
  );
};

/**
 * Component for displaying data completeness with progress indicator
 */
export const DataCompleteness = ({ current, total, label, size = 'md' }) => {
  const percentage = total > 0 ? (current / total) * 100 : 0;
  const isComplete = current === total;
  const isEmpty = current === 0;
  
  const statusColor = isComplete ? 'text-green-600' : 
                     isEmpty ? 'text-red-600' : 'text-yellow-600';
  
  const progressColor = isComplete ? 'bg-green-500' : 
                       isEmpty ? 'bg-red-500' : 'bg-yellow-500';

  const sizeClasses = {
    sm: 'text-sm h-1',
    md: 'text-base h-2',
    lg: 'text-lg h-3',
  };

  return (
    <div className="space-y-1">
      <div className="flex justify-between items-center">
        <span className={clsx('font-medium', sizeClasses[size])}>
          {label}
        </span>
        <span className={clsx('font-mono', statusColor, sizeClasses[size])}>
          {current}/{total} ({percentage.toFixed(1)}%)
        </span>
      </div>
      <div className={clsx('w-full bg-gray-200 dark:bg-gray-700 rounded-full', sizeClasses[size])}>
        <div 
          className={clsx('rounded-full transition-all duration-300', progressColor, sizeClasses[size])}
          style={{ width: `${percentage}%` }}
        />
      </div>
    </div>
  );
};

/**
 * Component for displaying timestamps in financial context
 */
export const FinancialTimestamp = ({ 
  timestamp, 
  format: formatStr = 'MMM dd, yyyy HH:mm:ss',
  timezone = 'UTC',
  showRelative = false 
}) => {
  if (!timestamp) return <span className="text-gray-400">N/A</span>;
  
  const date = new Date(timestamp);
  const formatted = format(date, formatStr);
  
  // Calculate relative time for recent timestamps
  const now = new Date();
  const diffMinutes = Math.floor((now - date) / (1000 * 60));
  const diffHours = Math.floor(diffMinutes / 60);
  const diffDays = Math.floor(diffHours / 24);
  
  let relative = '';
  if (showRelative) {
    if (diffMinutes < 1) {
      relative = 'just now';
    } else if (diffMinutes < 60) {
      relative = `${diffMinutes}m ago`;
    } else if (diffHours < 24) {
      relative = `${diffHours}h ago`;
    } else if (diffDays < 7) {
      relative = `${diffDays}d ago`;
    }
  }

  return (
    <span className="text-sm text-gray-600 dark:text-gray-400 font-mono">
      {formatted}
      {relative && (
        <span className="ml-2 text-xs text-gray-500">
          ({relative})
        </span>
      )}
      {timezone && (
        <span className="ml-1 text-xs text-gray-500">
          {timezone}
        </span>
      )}
    </span>
  );
};

/**
 * Component for displaying market data status indicators
 */
export const MarketDataStatus = ({ status, label, lastUpdate }) => {
  const statusConfig = {
    COMPLETE: { 
      color: 'text-green-600 dark:text-green-400', 
      bgColor: 'bg-green-50 dark:bg-green-900/20',
      icon: '✅',
      label: 'Complete'
    },
    INCOMPLETE: { 
      color: 'text-yellow-600 dark:text-yellow-400', 
      bgColor: 'bg-yellow-50 dark:bg-yellow-900/20',
      icon: '⚠️',
      label: 'Incomplete'
    },
    STALE: { 
      color: 'text-red-600 dark:text-red-400', 
      bgColor: 'bg-red-50 dark:bg-red-900/20',
      icon: '🔴',
      label: 'Stale'
    },
    UNKNOWN: { 
      color: 'text-gray-600 dark:text-gray-400', 
      bgColor: 'bg-gray-50 dark:bg-gray-900/20',
      icon: '❓',
      label: 'Unknown'
    },
  };

  const config = statusConfig[status] || statusConfig.UNKNOWN;

  return (
    <div className={clsx(
      'flex items-center justify-between p-3 rounded-lg border',
      config.bgColor,
      'border-gray-200 dark:border-gray-700'
    )}>
      <div className="flex items-center gap-2">
        <span className="text-lg">{config.icon}</span>
        <span className="font-medium text-gray-900 dark:text-gray-100">
          {label}
        </span>
      </div>
      <div className="text-right">
        <div className={clsx('font-semibold', config.color)}>
          {config.label}
        </div>
        {lastUpdate && (
          <FinancialTimestamp 
            timestamp={lastUpdate} 
            format="HH:mm:ss"
            showRelative={true}
          />
        )}
      </div>
    </div>
  );
};

/**
 * Component for risk indicators
 */
export const RiskIndicator = ({ level, value, threshold, label }) => {
  const getRiskColor = (level) => {
    switch (level?.toLowerCase()) {
      case 'low':
        return 'text-green-600 dark:text-green-400';
      case 'medium':
        return 'text-yellow-600 dark:text-yellow-400';
      case 'high':
        return 'text-red-600 dark:text-red-400';
      case 'critical':
        return 'text-red-800 dark:text-red-300 font-bold';
      default:
        return 'text-gray-600 dark:text-gray-400';
    }
  };

  const getRiskIcon = (level) => {
    switch (level?.toLowerCase()) {
      case 'low':
        return '🟢';
      case 'medium':
        return '🟡';
      case 'high':
        return '🟠';
      case 'critical':
        return '🔴';
      default:
        return '⚪';
    }
  };

  return (
    <div className="flex items-center justify-between p-2 rounded border border-gray-200 dark:border-gray-700">
      <span className="text-sm font-medium text-gray-700 dark:text-gray-300">
        {label}
      </span>
      <div className="flex items-center gap-2">
        <span className={clsx('text-sm font-semibold', getRiskColor(level))}>
          {getRiskIcon(level)} {level?.toUpperCase() || 'UNKNOWN'}
        </span>
        {value !== undefined && threshold !== undefined && (
          <span className="text-xs text-gray-500">
            ({value}/{threshold})
          </span>
        )}
      </div>
    </div>
  );
};