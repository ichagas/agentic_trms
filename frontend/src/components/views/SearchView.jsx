import React, { useState } from 'react';
import { motion } from 'framer-motion';
import { Search } from 'lucide-react';
import { SuggestedActions, QuickActions } from '../ui/SuggestedActions';

/**
 * SearchView Component - Initial landing page
 * 
 * This component displays a Google-like search interface with:
 * - Large search input field
 * - Suggested actions for common queries
 * - Quick action buttons for frequent operations
 * 
 * @param {Function} onSubmit - Callback when user submits a query
 */
const SearchView = ({ onSubmit }) => {
  const [input, setInput] = useState('');

  const handleSubmit = (e) => {
    e.preventDefault();
    if (input.trim()) {
      onSubmit(input);
    }
  };

  const handleSuggestionClick = (suggestion) => {
    onSubmit(suggestion);
  };

  const suggestions = [
    "Transfer $100,000 from ACC-001-USD to ACC-002-USD and send via SWIFT",
    "Can we run End-of-Day? Check SWIFT reconciliation status",
    "Show me all unreconciled SWIFT messages and reconcile them",
    "Check balance for ACC-001-USD, then transfer $50,000 to ACC-010-USD",
    "Process the redemption report and verify today's EOD reports"
  ];

  return (
    <motion.div
      className="flex flex-col items-center justify-center flex-1 p-4 text-center"
      initial={{ opacity: 0, scale: 0.95 }}
      animate={{ opacity: 1, scale: 1 }}
      exit={{ opacity: 0, scale: 0.95 }}
      transition={{ duration: 0.3 }}
    >
      <div className="w-full max-w-2xl">
        <motion.h1 
          className="text-5xl md:text-6xl font-light text-gray-800 dark:text-gray-200 mb-4"
          initial={{ y: -20, opacity: 0 }}
          animate={{ y: 0, opacity: 1 }}
          transition={{ delay: 0.1, duration: 0.4}}
        >
          NACC - NextAgent
        </motion.h1>
        <motion.p 
          className="text-gray-500 dark:text-gray-400 mb-8"
          initial={{ y: -20, opacity: 0 }}
          animate={{ y: 0, opacity: 1 }}
          transition={{ delay: 0.2, duration: 0.4}}
        >
          Your Intelligent Operational copilot.
        </motion.p>
        <form onSubmit={handleSubmit} className="relative flex justify-center w-full">
          <motion.div
            className="relative w-full max-w-2xl"
            initial={{ y: 20, opacity: 0 }}
            animate={{ y: 0, opacity: 1 }}
            transition={{ delay: 0.3, duration: 0.4}}
          >
            <Search className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400" />
            <input
              type="text"
              value={input}
              onChange={(e) => setInput(e.target.value)}
              placeholder="Ask about accounts, EOD checks, transactions..."
              className="w-full py-4 pl-12 pr-28 text-lg bg-white dark:bg-gray-800 border border-gray-300 dark:border-gray-600 rounded-full shadow-md focus:ring-2 focus:ring-blue-500 focus:outline-none transition-shadow text-gray-800 dark:text-gray-200"
              autoFocus
            />
            <button
              type="submit"
              className="absolute right-2 top-1/2 -translate-y-1/2 px-6 py-2 bg-blue-600 text-white rounded-full font-semibold hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 transition-colors disabled:bg-gray-400"
              disabled={!input.trim()}
            >
              Ask
            </button>
          </motion.div>
        </form>
        <motion.div 
          className="mt-8"
          initial={{ y: 20, opacity: 0 }}
          animate={{ y: 0, opacity: 1 }}
          transition={{ delay: 0.4, duration: 0.4}}
        >
          <SuggestedActions 
            suggestions={suggestions.slice(0, 3)}
            onSuggestionClick={handleSuggestionClick}
          />
          
          <div className="mt-6">
            <QuickActions onActionClick={handleSuggestionClick} />
          </div>
        </motion.div>
      </div>
    </motion.div>
  );
};

export default SearchView;