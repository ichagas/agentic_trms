import React, { useState } from 'react';
import ReactMarkdown from 'react-markdown';
import { motion } from 'framer-motion';
import { ChevronDown, ChevronRight, Brain } from 'lucide-react';

/**
 * ThinkingSection Component - Collapsible AI reasoning display
 * 
 * Features:
 * - Collapsible interface with expand/collapse animation
 * - Brain icon and proper styling for reasoning content
 * - Blue-themed design to differentiate from main content
 * - Smooth height animation using Framer Motion
 * - Markdown rendering for formatted reasoning text
 * - Touch-friendly interaction design
 * 
 * @param {string} content - The AI's reasoning/thinking content to display
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

export default ThinkingSection;