import React from 'react';
import { Wifi, WifiOff } from 'lucide-react';

/**
 * Header Component - Chat header with AI avatar and connection status
 *
 * Displays:
 * - AI avatar with gradient background
 * - TRMS AI Assistant title and subtitle
 * - Connection status indicator with appropriate styling
 * - Optional children (e.g., toggle buttons)
 *
 * @param {string} connectionStatus - Connection status ('connected', 'disconnected', etc.)
 * @param {ReactNode} children - Optional children elements
 */
const Header = ({ connectionStatus, children }) => {
  return (
    <header className="px-4 py-3 sm:px-6 sm:py-3 border-b border-gray-200 dark:border-gray-700 flex justify-between items-center bg-white dark:bg-gray-800">
      <div className="flex items-center gap-3">
        <div className="w-8 h-8 rounded-full bg-gradient-to-br from-blue-500 to-purple-600 flex items-center justify-center font-bold text-white text-sm">
          AI
        </div>
        <div>
          <h2 className="text-xl font-semibold text-gray-800 dark:text-gray-200">NACC</h2>
          <p className="text-xs text-gray-500 dark:text-gray-400">NextAgent Command Control</p>
        </div>
      </div>

      <div className="flex items-center gap-2">
        {children}
        <div className={`flex items-center gap-1 px-2 py-1 rounded-full text-xs ${
          connectionStatus === 'connected'
            ? 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200'
            : 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200'
        }`}>
          {connectionStatus === 'connected' ? (
            <><Wifi size={12} /> Connected</>
          ) : (
            <><WifiOff size={12} /> Offline</>
          )}
        </div>
      </div>
    </header>
  );
};

export default Header;