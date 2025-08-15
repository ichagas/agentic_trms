import React from 'react';
import { motion } from 'framer-motion';

/**
 * LoadingBubble Component - Typing indicator with animated dots
 * 
 * Features:
 * - AI avatar consistent with other message bubbles
 * - Three animated dots with staggered timing
 * - Smooth fade-in animation
 * - Professional loading indicator styling
 */
const LoadingBubble = () => (
  <motion.div
    className="flex items-end gap-2 justify-start"
    initial={{ opacity: 0 }}
    animate={{ opacity: 1 }}
    transition={{ delay: 0.1 }}
  >
    <div className="w-8 h-8 rounded-full bg-gray-300 dark:bg-gray-600 flex items-center justify-center font-bold text-gray-600 dark:text-gray-300 flex-shrink-0">
      AI
    </div>
    <div className="px-4 py-3 rounded-2xl rounded-bl-lg bg-gray-200 dark:bg-gray-700">
      <div className="flex items-center justify-center gap-1.5">
        <motion.div
          className="w-2 h-2 bg-gray-500 rounded-full"
          animate={{ y: [0, -4, 0] }}
          transition={{ duration: 0.8, repeat: Infinity, ease: "easeInOut" }}
        />
        <motion.div
          className="w-2 h-2 bg-gray-500 rounded-full"
          animate={{ y: [0, -4, 0] }}
          transition={{ duration: 0.8, repeat: Infinity, ease: "easeInOut", delay: 0.1 }}
        />
        <motion.div
          className="w-2 h-2 bg-gray-500 rounded-full"
          animate={{ y: [0, -4, 0] }}
          transition={{ duration: 0.8, repeat: Infinity, ease: "easeInOut", delay: 0.2 }}
        />
      </div>
    </div>
  </motion.div>
);

export default LoadingBubble;