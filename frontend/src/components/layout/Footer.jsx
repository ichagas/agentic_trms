import React from 'react';
import { Send, Loader } from 'lucide-react';

/**
 * Footer Component - Chat input form and connection warnings
 * 
 * Features:
 * - Text input for user messages with placeholder
 * - Send button with loading state
 * - Disabled states when loading or disconnected
 * - Connection warning message when backend is offline
 * 
 * @param {string} input - Current input value
 * @param {Function} setInput - Input value setter
 * @param {Function} onSubmit - Form submit handler
 * @param {boolean} isLoading - Whether AI is currently responding
 * @param {string} connectionStatus - Backend connection status
 */
const Footer = ({ input, setInput, onSubmit, isLoading, connectionStatus }) => {
  return (
    <footer className="p-4 sm:p-6 md:p-8 lg:p-10 xl:p-12 border-t border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800">
      <form onSubmit={onSubmit} className="relative">
        <input
          type="text"
          value={input}
          onChange={(e) => setInput(e.target.value)}
          placeholder="Ask about accounts, transactions, EOD processing..."
          className="w-full py-3 pl-4 pr-16 bg-gray-100 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 focus:outline-none text-gray-800 dark:text-gray-200 placeholder-gray-500 dark:placeholder-gray-400"
          disabled={isLoading || connectionStatus !== 'connected'}
        />
        <button 
          type="submit" 
          className="absolute right-3 top-1/2 -translate-y-1/2 p-2 bg-blue-600 text-white rounded-full hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:bg-gray-400 disabled:cursor-not-allowed transition-all"
          disabled={isLoading || !input.trim() || connectionStatus !== 'connected'}
        >
          {isLoading ? <Loader className="animate-spin" size={20} /> : <Send size={20} />}
        </button>
      </form>
      
      {connectionStatus !== 'connected' && (
        <div className="mt-2 text-xs text-yellow-600 dark:text-yellow-400 text-center">
          ⚠️ Backend disconnected - using mock responses
        </div>
      )}
    </footer>
  );
};

export default Footer;