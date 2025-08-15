import { useState, useEffect } from 'react';
import TrmsApiService from '../services/api';

/**
 * useMessageHandler Hook - Manages message sending and WebSocket communication
 * 
 * This hook handles:
 * - Message sending via REST API or WebSocket
 * - WebSocket setup and cleanup
 * - Loading states during message processing
 * - Error handling for failed messages
 * - Message state management
 * 
 * @param {boolean} useWebSocket - Whether to use WebSocket for communication
 * @returns {Object} - Object containing message functions and state
 */
const useMessageHandler = (useWebSocket = false) => {
  const [messages, setMessages] = useState([]);
  const [isLoading, setIsLoading] = useState(false);

  // Setup WebSocket if enabled
  useEffect(() => {
    if (useWebSocket) {
      const ws = TrmsApiService.setupWebSocket(
        (data) => {
          const aiMessage = {
            id: Date.now().toString(),
            text: data.message,
            sender: 'ai',
            timestamp: new Date(),
            metadata: data.metadata
          };
          setMessages(prev => [...prev, aiMessage]);
          setIsLoading(false);
        },
        (error) => {
          console.error('WebSocket error:', error);
          const errorMessage = {
            id: Date.now().toString(),
            text: 'WebSocket connection failed',
            sender: 'ai',
            isError: true,
            timestamp: new Date()
          };
          setMessages(prev => [...prev, errorMessage]);
          setIsLoading(false);
        }
      );
      
      return () => {
        if (ws) {
          ws.disconnect();
        }
      };
    }
  }, [useWebSocket]);
  
  // Cleanup on unmount
  useEffect(() => {
    return () => {
      TrmsApiService.cleanup();
    };
  }, []);

  /**
   * Send a message using the configured communication method
   * 
   * @param {string} messageText - The message text to send
   * @returns {Promise} - Promise that resolves when message is sent
   */
  const sendMessage = async (messageText) => {
    if (!messageText.trim()) return;
    
    const userMessage = {
      id: Date.now().toString(),
      text: messageText,
      sender: 'user',
      timestamp: new Date()
    };
    
    setMessages(prev => [...prev, userMessage]);
    setIsLoading(true);

    try {
      if (useWebSocket) {
        // Send via WebSocket
        TrmsApiService.sendWebSocketMessage(messageText);
      } else {
        // Send via REST API
        const response = await TrmsApiService.sendMessage(messageText);
        const aiMessage = {
          id: (Date.now() + 1).toString(),
          text: response.message,
          sender: 'ai',
          timestamp: new Date(),
          isMock: response.isMock,
          metadata: response.metadata
        };
        setMessages(prev => [...prev, aiMessage]);
        setIsLoading(false);
      }
    } catch (error) {
      console.error('Error sending message:', error);
      const errorMessage = {
        id: (Date.now() + 1).toString(),
        text: error.message || "Sorry, I couldn't process your request. Please try again later.",
        sender: 'ai',
        isError: true,
        timestamp: new Date()
      };
      setMessages(prev => [...prev, errorMessage]);
      setIsLoading(false);
    }
  };

  /**
   * Clear all messages from the conversation
   */
  const clearMessages = () => {
    setMessages([]);
  };

  return {
    messages,
    isLoading,
    sendMessage,
    clearMessages
  };
};

export default useMessageHandler;