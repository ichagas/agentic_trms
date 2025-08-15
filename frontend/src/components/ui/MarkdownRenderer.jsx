import React from 'react';
import ReactMarkdown from 'react-markdown';
import ThinkingSection from '../messages/ThinkingSection';
import FinancialDataRenderer from '../financial/FinancialDataRenderer';
import { parseThinkingContent, processFinancialContent } from '../../utils/messageUtils';

/**
 * EnhancedMarkdownRenderer Component - Enhanced markdown rendering with financial components
 * 
 * Features:
 * - Parsing and display of AI thinking/reasoning content
 * - Financial data content preprocessing and enhancement
 * - Custom markdown components with proper styling
 * - Support for tables, lists, code blocks, and headers
 * - Dark mode compatible styling
 * - Integration with financial data rendering
 * 
 * @param {string} content - Raw message content to render
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

export default EnhancedMarkdownRenderer;