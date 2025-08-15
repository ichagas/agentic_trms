import { useState, useEffect } from 'react';
import TrmsApiService from '../services/api';

/**
 * useConnectionStatus Hook - Manages backend connection monitoring
 * 
 * This hook handles:
 * - Initial connection status check
 * - Periodic health checks every 30 seconds
 * - Connection status state management
 * - Cleanup on component unmount
 * 
 * @returns {string} connectionStatus - Current connection status ('connected', 'disconnected', 'unknown')
 */
const useConnectionStatus = () => {
  const [connectionStatus, setConnectionStatus] = useState('unknown');

  useEffect(() => {
    const checkConnection = async () => {
      try {
        const health = await TrmsApiService.healthCheck();
        setConnectionStatus(health.success ? 'connected' : 'disconnected');
      } catch (error) {
        console.error('Connection check failed:', error);
        setConnectionStatus('disconnected');
      }
    };
    
    // Initial check
    checkConnection();
    
    // Check connection every 30 seconds
    const interval = setInterval(checkConnection, 30000);
    
    // Cleanup interval on unmount
    return () => clearInterval(interval);
  }, []);

  return connectionStatus;
};

export default useConnectionStatus;