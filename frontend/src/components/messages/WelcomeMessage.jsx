import React from 'react';
import { motion } from 'framer-motion';

/**
 * WelcomeMessage Component - Initial welcome display for new conversations
 * 
 * Features:
 * - Large AI avatar with gradient background
 * - Welcome title and descriptive text
 * - Helpful suggestions for getting started
 * - Smooth fade-up animation
 * - Responsive design with proper spacing
 */
const WelcomeMessage = () => (
  <motion.div
    initial={{ opacity: 0, y: 20 }}
    animate={{ opacity: 1, y: 0 }}
    className="text-center py-8"
  >
    <div className="w-16 h-16 mx-auto mb-4 rounded-full bg-gradient-to-br from-blue-500 to-purple-600 flex items-center justify-center">
      <span className="text-2xl font-bold text-white">AI</span>
    </div>
    <h3 className="text-xl font-semibold text-gray-800 dark:text-gray-200 mb-2">
      Welcome to NextAgent Command Control
    </h3>
    <p className="text-gray-600 dark:text-gray-400 mb-6 max-w-md mx-auto">
      I can help you with treasury operations, account management, EOD processing, and financial data analysis.
    </p>
    <div className="text-sm text-gray-500 dark:text-gray-400">
      Try asking about accounts, balances, transactions, or EOD readiness.
    </div>
  </motion.div>
);

export default WelcomeMessage;