/**
 * Financial Data Processing Utilities
 * 
 * This module contains utility functions for processing financial data,
 * analyzing patterns in messages, and extracting meaningful financial information.
 */

/**
 * Extract currency amounts from text content
 * 
 * @param {string} content - Text content to analyze
 * @returns {Array} - Array of currency amounts found
 */
export const extractCurrencyAmounts = (content) => {
  if (!content) return [];
  
  const currencyRegex = /\$([0-9,]+\.?[0-9]*)/g;
  const matches = [...content.matchAll(currencyRegex)];
  
  return matches.map(match => ({
    fullMatch: match[0],
    amount: match[1].replace(/,/g, ''),
    numericValue: parseFloat(match[1].replace(/,/g, ''))
  }));
};

/**
 * Extract account identifiers from text content
 * 
 * @param {string} content - Text content to analyze
 * @returns {Array} - Array of account IDs found
 */
export const extractAccountIds = (content) => {
  if (!content) return [];
  
  const accountRegex = /ACC-(\d{3})-([A-Z]{3})/g;
  const matches = [...content.matchAll(accountRegex)];
  
  return matches.map(match => ({
    fullId: match[0],
    number: match[1],
    currency: match[2]
  }));
};

/**
 * Extract transaction IDs from text content
 * 
 * @param {string} content - Text content to analyze
 * @returns {Array} - Array of transaction IDs found
 */
export const extractTransactionIds = (content) => {
  if (!content) return [];
  
  const txnRegex = /TXN-(\d+)/g;
  const matches = [...content.matchAll(txnRegex)];
  
  return matches.map(match => ({
    fullId: match[0],
    number: match[1]
  }));
};

/**
 * Analyze content for financial data patterns
 * 
 * @param {string} content - Message content to analyze
 * @returns {Object} - Object containing analysis results
 */
export const analyzeFinancialContent = (content) => {
  if (!content) {
    return {
      hasFinancialData: false,
      currencies: [],
      accounts: [],
      transactions: [],
      patterns: []
    };
  }
  
  const currencies = extractCurrencyAmounts(content);
  const accounts = extractAccountIds(content);
  const transactions = extractTransactionIds(content);
  
  const patterns = [];
  
  // Check for common financial patterns
  if (content.toLowerCase().includes('eod') || content.toLowerCase().includes('end-of-day')) {
    patterns.push('EOD_PROCESSING');
  }
  
  if (content.toLowerCase().includes('balance')) {
    patterns.push('BALANCE_INQUIRY');
  }
  
  if (content.toLowerCase().includes('transfer')) {
    patterns.push('FUND_TRANSFER');
  }
  
  if (content.toLowerCase().includes('rate fixing') || content.toLowerCase().includes('market data')) {
    patterns.push('MARKET_DATA');
  }
  
  return {
    hasFinancialData: currencies.length > 0 || accounts.length > 0 || transactions.length > 0,
    currencies,
    accounts,
    transactions,
    patterns
  };
};

/**
 * Format large numbers with appropriate suffixes (K, M, B)
 * 
 * @param {number} value - Numeric value to format
 * @param {number} precision - Decimal places to show
 * @returns {string} - Formatted string with suffix
 */
export const formatLargeNumber = (value, precision = 1) => {
  if (value === null || value === undefined || isNaN(value)) {
    return 'N/A';
  }
  
  const absValue = Math.abs(value);
  
  if (absValue >= 1000000000) {
    return `${(value / 1000000000).toFixed(precision)}B`;
  } else if (absValue >= 1000000) {
    return `${(value / 1000000).toFixed(precision)}M`;
  } else if (absValue >= 1000) {
    return `${(value / 1000).toFixed(precision)}K`;
  }
  
  return value.toString();
};

/**
 * Calculate percentage change between two values
 * 
 * @param {number} oldValue - Previous value
 * @param {number} newValue - Current value
 * @returns {Object} - Object with percentage and direction
 */
export const calculatePercentageChange = (oldValue, newValue) => {
  if (oldValue === 0 || oldValue === null || oldValue === undefined) {
    return { percentage: 0, direction: 'neutral' };
  }
  
  const change = ((newValue - oldValue) / oldValue) * 100;
  const direction = change > 0 ? 'positive' : change < 0 ? 'negative' : 'neutral';
  
  return {
    percentage: Math.abs(change),
    direction,
    formatted: `${change > 0 ? '+' : ''}${change.toFixed(2)}%`
  };
};