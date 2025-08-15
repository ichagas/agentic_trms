import React from 'react';
import { Wifi, WifiOff } from 'lucide-react';

/**
 * Header Component - Chat header with AI avatar and connection status
 * 
 * Displays:
 * - AI avatar with gradient background
 * - TRMS AI Assistant title and subtitle
 * - Connection status indicator with appropriate styling
 * 
 * @param {string} connectionStatus - Connection status ('connected', 'disconnected', etc.)
 */
const Header = ({ connectionStatus }) => {
  return (
    <header className="p-4 sm:p-6 md:p-8 lg:p-10 xl:p-12 border-b border-gray-200 dark:border-gray-700 flex justify-between items-center bg-white dark:bg-gray-800">
      <div className="flex items-center gap-3">
        <div className="w-8 h-8 rounded-full bg-gradient-to-br from-blue-500 to-purple-600 flex items-center justify-center font-bold text-white text-sm">
          AI
        </div>
        <div>
          <h2 className="text-xl font-semibold text-gray-800 dark:text-gray-200">TRMS AI Assistant</h2>
          <p className="text-xs text-gray-500 dark:text-gray-400">Treasury & Risk Management System</p>
        </div>
      </div>
      
      <div className="flex items-center gap-2">
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