import React, { useState } from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { AnimatePresence } from 'framer-motion';
import SearchView from './components/views/SearchView';
import ChatView from './components/views/ChatView';
import ErrorBanner from './components/layout/ErrorBanner';
import TrmsDashboard from './pages/TrmsDashboard';
import SwiftDashboard from './pages/SwiftDashboard';
import useConnectionStatus from './hooks/useConnectionStatus';
import useMessageHandler from './hooks/useMessageHandler';

/**
 * App Component - Root component of the TRMS AI Assistant application
 * 
 * This component manages:
 * - Overall application state (view, error handling)
 * - Connection status monitoring
 * - Message handling and communication
 * - View transitions between search and chat interfaces
 * - WebSocket configuration (disabled by default)
 * 
 * Features:
 * - Responsive design with professional TRMS styling
 * - Smooth view transitions using Framer Motion
 * - Error handling with dismissible error banner
 * - Real-time connection status monitoring
 * - Integrated message processing and display
 */
const App = () => {
  // Application state
  const [view, setView] = useState('search');
  const [useWebSocket] = useState(false); // Disabled by default since backend doesn't support socket.io
  const [error, setError] = useState(null);

  // Custom hooks for connection and messaging
  const connectionStatus = useConnectionStatus();
  const { messages, isLoading, sendMessage } = useMessageHandler(useWebSocket);

  /**
   * Handle message submission from either search or chat view
   * Transitions from search to chat view on first message
   * 
   * @param {string} messageText - The message text to send
   */
  const handleSendMessage = async (messageText) => {
    setError(null);
    
    // Transition to chat view on first message
    if (view === 'search') {
      setView('chat');
    }
    
    try {
      await sendMessage(messageText);
    } catch (error) {
      console.error('Error in handleSendMessage:', error);
      setError(error.message || 'Failed to send message');
    }
  };

  return (
    <BrowserRouter>
      <div className="bg-gray-100 dark:bg-gray-900 font-sans h-screen w-screen flex flex-col overflow-hidden">
        {/* Error Display */}
        {error && (
          <ErrorBanner error={error} onDismiss={() => setError(null)} />
        )}

        {/* Main Content Area */}
        <div className="flex-1 flex w-full overflow-hidden">
          <Routes>
            {/* Chat Interface Routes */}
            <Route path="/" element={
              <AnimatePresence mode="wait">
                {view === 'search' ? (
                  <SearchView
                    key="search"
                    onSubmit={handleSendMessage}
                  />
                ) : (
                  <ChatView
                    key="chat"
                    messages={messages}
                    onNewMessage={handleSendMessage}
                    isLoading={isLoading}
                    connectionStatus={connectionStatus}
                  />
                )}
              </AnimatePresence>
            } />

            {/* Dashboard Routes */}
            <Route path="/dashboard/trms" element={<TrmsDashboard />} />
            <Route path="/dashboard/swift" element={<SwiftDashboard />} />
          </Routes>
        </div>
      </div>
    </BrowserRouter>
  );
};

export default App;