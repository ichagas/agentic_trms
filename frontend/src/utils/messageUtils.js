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
  
  if (content.includes('account') && content.includes('usd')) {
    return [
      'Check balance for ACC-001-USD',
      'Transfer funds between accounts',
      'Show account transaction history'
    ];
  }
  
  if (content.includes('eod') || content.includes('end-of-day')) {
    return [
      'Propose missing rate fixings',
      'Validate new transactions',
      'Check market data status'
    ];
  }
  
  if (content.includes('transaction') || content.includes('transfer')) {
    return [
      'Show recent transactions',
      'Check account balances',
      'Run transaction validation report'
    ];
  }
  
  return [
    'Show me all USD accounts',
    'Can we run End-of-Day?',
    'What\'s the current system status?'
  ];
};