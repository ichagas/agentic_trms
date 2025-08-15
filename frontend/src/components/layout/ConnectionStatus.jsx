import React from 'react';
import { Wifi, WifiOff } from 'lucide-react';

/**
 * ConnectionStatus Component - Connection status bar with WebSocket toggle
 * 
 * Features:
 * - Full-width status bar with appropriate color coding
 * - Connection status indicator with icon and text
 * - WebSocket toggle checkbox (disabled when disconnected)
 * - Responsive design with proper spacing
 * 
 * @param {string} status - Connection status ('connected', 'disconnected', etc.)
 * @param {boolean} useWebSocket - Whether WebSocket is enabled
 * @param {Function} onToggleWebSocket - WebSocket toggle handler
 */
const ConnectionStatusBar = ({ status, useWebSocket, onToggleWebSocket }) => {
  if (status === 'unknown') return null;
  
  return (
    <div className={`px-4 py-2 text-sm flex items-center justify-between ${
      status === 'connected' 
        ? 'bg-green-50 text-green-800 dark:bg-green-900/20 dark:text-green-200 border-green-200 dark:border-green-700'
        : 'bg-yellow-50 text-yellow-800 dark:bg-yellow-900/20 dark:text-yellow-200 border-yellow-200 dark:border-yellow-700'
    } border-b`}>
      <div className="flex items-center gap-2">
        {status === 'connected' ? (
          <><Wifi size={14} /> Connected to TRMS Backend</>
        ) : (
          <><WifiOff size={14} /> Backend Unavailable - Using Mock Responses</>
        )}
      </div>
      
      <div className="flex items-center gap-3">
        <label className="flex items-center gap-2 text-xs">
          <input
            type="checkbox"
            checked={useWebSocket}
            onChange={onToggleWebSocket}
            className="rounded"
            disabled={status !== 'connected'}
          />
          WebSocket
        </label>
      </div>
    </div>
  );
};

export default ConnectionStatusBar;