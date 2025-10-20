/**
 * Message Processing Utilities
 * 
 * This module contains utility functions for processing and analyzing message content,
 * including parsing AI thinking content and enhancing financial data display.
 */

/**
 * Parse thinking tags from message content
 * 
 * Extracts AI reasoning/thinking content wrapped in <think></think> tags
 * and separates it from the main message content.
 * 
 * @param {string} content - Raw message content that may contain thinking tags
 * @returns {Object} - Object with mainContent and thinkingContent properties
 */
export const parseThinkingContent = (content) => {
  if (!content) return { mainContent: '', thinkingContent: null };
  
  // Extract thinking content using regex
  const thinkingRegex = /<think>([\s\S]*?)<\/think>/gi;
  let thinkingContent = null;
  let mainContent = content;
  
  const matches = [...content.matchAll(thinkingRegex)];
  if (matches.length > 0) {
    // Extract thinking content (combine if multiple think tags)
    thinkingContent = matches.map(match => match[1].trim()).join('\n\n');
    
    // Remove thinking tags from main content
    mainContent = content.replace(thinkingRegex, '').trim();
  }
  
  return { mainContent, thinkingContent };
};

/**
 * Process content to enhance financial data display
 * 
 * Applies markdown formatting enhancements to financial data patterns
 * such as currency amounts and percentages for better visual presentation.
 * 
 * @param {string} content - Message content to process
 * @returns {string} - Processed content with enhanced financial formatting
 */
export const processFinancialContent = (content) => {
  if (!content) return '';
  
  // Enhanced currency formatting in markdown
  let processed = content;
  
  // Replace currency patterns with enhanced formatting
  processed = processed.replace(
    /\$([0-9,]+\.?[0-9]*)/g,
    '**$$$1**'
  );
  
  // Enhance percentage displays
  processed = processed.replace(
    /([0-9]+\.?[0-9]*)%/g,
    '**$1%**'
  );
  
  return processed;
};

/**
 * Generate contextual suggestions based on the last AI message
 * 
 * Analyzes the content of the most recent AI response to provide relevant
 * follow-up questions and actions that the user might want to take.
 * 
 * @param {Object} lastMessage - The most recent message object
 * @returns {Array} - Array of contextual suggestion strings
 */
export const getContextualSuggestions = (lastMessage) => {
  if (!lastMessage || lastMessage.sender === 'user') return [];

  const content = lastMessage.text.toLowerCase();

  // After showing accounts
  if (content.includes('account') && content.includes('usd')) {
    return [
      'Check balance for ACC-001-USD',
      'Transfer $50,000 from ACC-001-USD to ACC-002-USD',
      'Can we run EOD? Check both systems'
    ];
  }

  // After EOD check showing blockers
  if ((content.includes('eod') || content.includes('end-of-day')) &&
      (content.includes('not ready') || content.includes('blocking') || content.includes('missing'))) {
    return [
      'Propose missing rate fixings',
      'Verify today\'s EOD reports',
      'Check unreconciled SWIFT messages'
    ];
  }

  // After EOD check showing ready status
  if ((content.includes('eod') || content.includes('end-of-day')) &&
      (content.includes('ready') || content.includes('passed'))) {
    return [
      'Process the redemption report',
      'Show transaction history',
      'View all account balances'
    ];
  }

  // After SWIFT-specific responses
  if (content.includes('swift') && (content.includes('ready') || content.includes('validation'))) {
    return [
      'Check unreconciled SWIFT messages',
      'Verify today\'s EOD reports',
      'Reconcile SWIFT messages automatically'
    ];
  }

  // After booking transaction
  if ((content.includes('transaction') || content.includes('transfer')) && content.includes('pending')) {
    return [
      'Go to TRMS Dashboard to approve',
      'Check transaction status',
      'Show recent transactions'
    ];
  }

  // After VALIDATED transaction or SWIFT sent
  if (content.includes('validated') || content.includes('swift') && content.includes('sent')) {
    return [
      'Can we run EOD now?',
      'Check SWIFT message status',
      'Reconcile SWIFT messages'
    ];
  }

  // After rate fixings proposed
  if (content.includes('rate') && (content.includes('fixing') || content.includes('reset'))) {
    return [
      'Verify today\'s EOD reports',
      'Check EOD readiness now',
      'Show market data status'
    ];
  }

  // Default suggestions
  return [
    'Can we run EOD? Check both systems',
    'Show me all USD accounts',
    'Transfer $50,000 from ACC-001-USD to ACC-002-USD'
  ];
};