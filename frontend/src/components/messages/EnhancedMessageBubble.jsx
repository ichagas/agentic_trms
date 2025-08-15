import React from 'react';
import { motion } from 'framer-motion';
import clsx from 'clsx';
import { FinancialTimestamp } from '../financial/FinancialFormatters';
import EnhancedMarkdownRenderer from '../ui/MarkdownRenderer';

/**
 * EnhancedMessageBubble Component - Message display with financial data formatting
 * 
 * Features:
 * - User/AI message differentiation with styling
 * - Avatar display for both user and AI
 * - Enhanced markdown rendering for AI messages
 * - Timestamp display with relative time
 * - Mock response indicator
 * - Error message styling
 * - Smooth animation on appearance
 * 
 * @param {Object} message - Message object with text, sender, timestamp, metadata
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