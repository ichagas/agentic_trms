import React, { useState } from 'react';
import ReactMarkdown from 'react-markdown';
import { motion } from 'framer-motion';
import { ChevronDown, ChevronRight, Brain } from 'lucide-react';
import clsx from 'clsx';
import {
  CurrencyDisplay,
  AccountStatus,
  TransactionStatus,
  DataCompleteness,
  FinancialTimestamp,
  MarketDataStatus,
  RiskIndicator,
} from './FinancialFormatters';

/**
 * Enhanced message bubble with financial data formatting
 */
export const EnhancedMessageBubble = ({ message }) => {
  const isUser = message.sender === 'user';
  
  const bubbleVariants = {
    hidden: { opacity: 0, y: 20 },
    visible: { opacity: 1, y: 0 },
  };

  return (
    <motion.div
      className={clsx(
        'flex items-end gap-3',
        isUser ? 'justify-end' : 'justify-start'
      )}
      variants={bubbleVariants}
      initial="hidden"
      animate="visible"
      transition={{ duration: 0.3 }}
    >
      {!isUser && (
        <div className="w-8 h-8 rounded-full bg-gradient-to-br from-blue-500 to-purple-600 flex items-center justify-center font-bold text-white text-sm flex-shrink-0">
          AI
        </div>
      )}
      
      <div
        className={clsx(
          'max-w-xs sm:max-w-md md:max-w-2xl lg:max-w-3xl xl:max-w-4xl px-4 py-3 rounded-2xl shadow-sm',
          isUser
            ? 'bg-blue-600 text-white rounded-br-md'
            : clsx(
                'bg-white dark:bg-gray-800 text-gray-900 dark:text-gray-100 rounded-bl-md border border-gray-200 dark:border-gray-700',
                message.isError && 'bg-red-50 dark:bg-red-900/20 border-red-200 dark:border-red-700'
              )
        )}
      >
        {isUser ? (
          <div className="text-white">
            {message.text}
          </div>
        ) : (
          <EnhancedMarkdownRenderer content={message.text} />
        )}
        
        {/* Timestamp */}
        <div className={clsx(
          'text-xs mt-2 opacity-75',
          isUser ? 'text-blue-100' : 'text-gray-500 dark:text-gray-400'
        )}>
          <FinancialTimestamp 
            timestamp={message.timestamp} 
            format="HH:mm:ss"
            showRelative={true}
          />
          {message.isMock && !isUser && (
            <span className="ml-2 px-1 py-0.5 bg-yellow-100 dark:bg-yellow-900 text-yellow-800 dark:text-yellow-200 rounded text-xs">
              Mock
            </span>
          )}
        </div>
      </div>
      
      {isUser && (
        <div className="w-8 h-8 rounded-full bg-gradient-to-br from-gray-400 to-gray-600 flex items-center justify-center font-bold text-white text-sm flex-shrink-0">
          U
        </div>
      )}
    </motion.div>
  );
};

/**
 * Thinking Section Component - Collapsible reasoning display
 */
const ThinkingSection = ({ content }) => {
  const [isExpanded, setIsExpanded] = useState(false);
  
  if (!content) return null;
  
  return (
    <div className="mb-3 border border-blue-200 dark:border-blue-700 rounded-lg overflow-hidden bg-blue-50/50 dark:bg-blue-900/10">
      <button
        onClick={() => setIsExpanded(!isExpanded)}
        className="w-full px-3 py-2 flex items-center gap-2 text-left text-sm md:text-base font-medium text-blue-700 dark:text-blue-300 hover:bg-blue-100/50 dark:hover:bg-blue-900/20 transition-colors touch-manipulation"
      >
        {isExpanded ? (
          <ChevronDown size={16} className="text-blue-500" />
        ) : (
          <ChevronRight size={16} className="text-blue-500" />
        )}
        <Brain size={16} className="text-blue-500" />
        <span>AI Reasoning Process</span>
        <span className="text-xs text-blue-500 dark:text-blue-400 ml-auto">
          {isExpanded ? 'Hide' : 'Show'}
        </span>
      </button>
      
      <motion.div
        initial={false}
        animate={{
          height: isExpanded ? 'auto' : 0,
          opacity: isExpanded ? 1 : 0
        }}
        transition={{ duration: 0.2, ease: 'easeInOut' }}
        className="overflow-hidden"
      >
        <div className="px-3 pb-3 border-t border-blue-200 dark:border-blue-700 bg-blue-50/30 dark:bg-blue-900/5">
          <div className="pt-2 text-sm md:text-base text-blue-800 dark:text-blue-200 leading-relaxed">
            <ReactMarkdown
              components={{
                p: ({ children }) => <p className="mb-2 last:mb-0">{children}</p>,
                strong: ({ children }) => <strong className="font-semibold">{children}</strong>,
                em: ({ children }) => <em className="italic">{children}</em>,
                code: ({ children }) => (
                  <code className="px-1 py-0.5 bg-blue-200/50 dark:bg-blue-800/50 rounded text-xs font-mono">
                    {children}
                  </code>
                ),
              }}
            >
              {content}
            </ReactMarkdown>
          </div>
        </div>
      </motion.div>
    </div>
  );
};

/**
 * Enhanced markdown renderer with financial components
 */
const EnhancedMarkdownRenderer = ({ content }) => {
  // Parse thinking content first
  const { mainContent, thinkingContent } = parseThinkingContent(content);
  
  // Pre-process main content to identify financial data patterns
  const processedContent = processFinancialContent(mainContent);
  
  return (
    <div className="prose prose-sm dark:prose-invert max-w-none">
      {/* Render thinking section if it exists */}
      <ThinkingSection content={thinkingContent} />
      
      {/* Render main content */}
      <ReactMarkdown
        components={{
          h1: ({ children }) => (
            <h1 className="text-xl font-bold text-gray-900 dark:text-gray-100 mb-3 pb-2 border-b border-gray-200 dark:border-gray-700">
              {children}
            </h1>
          ),
          h2: ({ children }) => (
            <h2 className="text-lg font-semibold text-gray-900 dark:text-gray-100 mb-3 mt-4">
              {children}
            </h2>
          ),
          h3: ({ children }) => (
            <h3 className="text-base font-semibold text-gray-800 dark:text-gray-200 mb-2 mt-3">
              {children}
            </h3>
          ),
          p: ({ children }) => (
            <p className="mb-2 last:mb-0 text-gray-700 dark:text-gray-300 leading-relaxed">
              {children}
            </p>
          ),
          ul: ({ children }) => (
            <ul className="space-y-1 mb-3">
              {children}
            </ul>
          ),
          li: ({ children }) => (
            <li className="flex items-start gap-2 text-gray-700 dark:text-gray-300">
              <span className="text-blue-500 mt-1.5 text-xs">•</span>
              <span className="flex-1">{children}</span>
            </li>
          ),
          strong: ({ children }) => (
            <strong className="font-semibold text-gray-900 dark:text-gray-100">
              {children}
            </strong>
          ),
          code: ({ children }) => (
            <code className="px-1.5 py-0.5 bg-gray-100 dark:bg-gray-700 text-gray-800 dark:text-gray-200 rounded text-sm font-mono">
              {children}
            </code>
          ),
          hr: () => (
            <hr className="my-4 border-gray-200 dark:border-gray-700" />
          ),
          // Custom component for tables
          table: ({ children }) => (
            <div className="overflow-x-auto mb-4">
              <table className="min-w-full border border-gray-200 dark:border-gray-700 rounded-lg">
                {children}
              </table>
            </div>
          ),
          thead: ({ children }) => (
            <thead className="bg-gray-50 dark:bg-gray-800">
              {children}
            </thead>
          ),
          tbody: ({ children }) => (
            <tbody className="divide-y divide-gray-200 dark:divide-gray-700">
              {children}
            </tbody>
          ),
          tr: ({ children }) => (
            <tr className="hover:bg-gray-50 dark:hover:bg-gray-800/50 transition-colors">
              {children}
            </tr>
          ),
          th: ({ children }) => (
            <th className="px-4 py-2 text-left text-xs font-semibold text-gray-600 dark:text-gray-400 uppercase tracking-wider">
              {children}
            </th>
          ),
          td: ({ children }) => (
            <td className="px-4 py-2 text-sm text-gray-700 dark:text-gray-300">
              {children}
            </td>
          ),
        }}
      >
        {processedContent}
      </ReactMarkdown>
      
      {/* Custom financial components */}
      <FinancialDataRenderer content={mainContent} />
    </div>
  );
};

/**
 * Parse thinking tags from message content
 */
const parseThinkingContent = (content) => {
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
 */
const processFinancialContent = (content) => {
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
 * Render financial data components based on content analysis
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

/**
 * EOD Status Summary Component
 */
const EODStatusSummary = ({ content }) => {
  // Parse key metrics from content
  const eodReady = content.includes('✅') && !content.includes('⚠️');
  const marketDataMatch = content.match(/(\d+)\/(\d+)/);
  
  return (
    <div className="bg-gray-50 dark:bg-gray-800 rounded-lg p-4 border border-gray-200 dark:border-gray-700">
      <div className="flex items-center justify-between mb-3">
        <h4 className="font-semibold text-gray-900 dark:text-gray-100">
          EOD Readiness Status
        </h4>
        <span className={clsx(
          'px-3 py-1 rounded-full text-sm font-medium',
          eodReady 
            ? 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200'
            : 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200'
        )}>
          {eodReady ? '✅ Ready' : '⚠️ Pending Actions'}
        </span>
      </div>
      
      {marketDataMatch && (
        <div className="space-y-2">
          <DataCompleteness
            current={parseInt(marketDataMatch[1])}
            total={parseInt(marketDataMatch[2])}
            label="Market Data"
            size="sm"
          />
        </div>
      )}
      
      <div className="mt-3 text-xs text-gray-600 dark:text-gray-400">
        Last updated: <FinancialTimestamp timestamp={new Date()} showRelative={true} />
      </div>
    </div>
  );
};

/**
 * Account Summary Table Component
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

/**
 * Transaction Summary Component
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

/**
 * Suggested Actions Component
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
 * Quick Action Buttons Component
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