import React, { useState, useEffect, useRef } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { Send, Search, Loader, AlertCircle, Wifi, WifiOff } from 'lucide-react';
import TrmsApiService from './services/api';
import { EnhancedMessageBubble, SuggestedActions, QuickActions } from './components/MessageFormatters';
import { FinancialTimestamp } from './components/FinancialFormatters';

// --- Main App Component ---
// This is the root component of our application.
// It manages the overall state, such as the current view (search or chat)
// and the list of messages in the conversation.
const App = () => {
  const [view, setView] = useState('search');
  const [messages, setMessages] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [connectionStatus, setConnectionStatus] = useState('unknown');
  const [useWebSocket, setUseWebSocket] = useState(false); // Disabled by default since backend doesn't support socket.io
  const [error, setError] = useState(null);

  // Initialize connection status check
  useEffect(() => {
    const checkConnection = async () => {
      const health = await TrmsApiService.healthCheck();
      setConnectionStatus(health.success ? 'connected' : 'disconnected');
    };
    
    checkConnection();
    
    // Check connection every 30 seconds
    const interval = setInterval(checkConnection, 30000);
    
    return () => clearInterval(interval);
  }, []);
  
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
          setError('WebSocket connection failed');
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

  const handleSendMessage = async (messageText) => {
    if (!messageText.trim()) return;
    
    setError(null);
    const userMessage = {
      id: Date.now().toString(),
      text: messageText,
      sender: 'user',
      timestamp: new Date()
    };
    
    setMessages(prev => [...prev, userMessage]);
    if (view === 'search') {
      setView('chat');
    }
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

  return (
    <div className="bg-gray-100 dark:bg-gray-900 font-sans h-screen flex flex-col">
                  
      {/* Error Display */}
      {error && (
        <ErrorBanner error={error} onDismiss={() => setError(null)} />
      )}
      
      {/* Main Content Area */}
      <div className="flex-1 flex flex-col overflow-hidden">
        <AnimatePresence mode="wait">
          {view === 'search' ? (
            <SearchView key="search" onSubmit={handleSendMessage} />
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
      </div>
    </div>
  );
};

// --- Search View Component ---
// This component is the initial landing page. It displays a large search
// input field, mimicking a modern search engine's homepage.
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
    "Show me all USD accounts",
    "Check balance for ACC-001-USD", 
    "Can we run End-of-Day?",
    "Transfer $50,000 from ACC-001-USD to ACC-002-USD",
    "Propose missing rate fixings"
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
          TRMS AI Assistant
        </motion.h1>
        <motion.p 
          className="text-gray-500 dark:text-gray-400 mb-8"
          initial={{ y: -20, opacity: 0 }}
          animate={{ y: 0, opacity: 1 }}
          transition={{ delay: 0.2, duration: 0.4}}
        >
          Your intelligent Treasury and Risk Management copilot.
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

// --- Chat View Component ---
// This component displays the main chat interface, including the message
// history and the input form for sending new messages.
const ChatView = ({ messages, onNewMessage, isLoading, connectionStatus }) => {
  const [input, setInput] = useState('');
  const messagesEndRef = useRef(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(scrollToBottom, [messages]);

  const handleSubmit = (e) => {
    e.preventDefault();
    if (input.trim()) {
      onNewMessage(input);
      setInput('');
    }
  };

  return (
    <motion.div
      className="flex flex-col flex-1 w-full max-w-sm sm:max-w-2xl md:max-w-4xl lg:max-w-6xl xl:max-w-7xl mx-auto bg-white dark:bg-gray-800 shadow-2xl overflow-hidden"
      initial={{ opacity: 0, y: 50 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.5, ease: "easeInOut" }}
    >
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
      
      <main className="flex-1 overflow-y-auto p-4 sm:p-6 md:p-8 lg:p-10 xl:p-12 space-y-6 bg-gray-50 dark:bg-gray-900 scrollbar-thin">
        {messages.length === 0 && (
          <WelcomeMessage />
        )}
        
        {messages.map((message) => (
          <EnhancedMessageBubble key={message.id} message={message} />
        ))}
        
        {isLoading && <LoadingBubble />}
        
        {/* Suggested Actions after AI response */}
        {messages.length > 0 && !isLoading && (
          <SuggestedActions 
            suggestions={getContextualSuggestions(messages[messages.length - 1])}
            onSuggestionClick={onNewMessage}
          />
        )}
        
        <div ref={messagesEndRef} />
      </main>
      
      <footer className="p-4 sm:p-6 md:p-8 lg:p-10 xl:p-12 border-t border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800">
        <form onSubmit={handleSubmit} className="relative">
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
    </motion.div>
  );
};

// Note: MessageBubble component has been replaced with EnhancedMessageBubble from MessageFormatters.jsx


// --- Loading Bubble Component ---
// Displays a typing indicator while waiting for the AI's response.
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

// --- Connection Status Bar Component ---
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

// --- Error Banner Component ---
const ErrorBanner = ({ error, onDismiss }) => (
  <div className="bg-red-50 dark:bg-red-900/20 border-l-4 border-red-500 p-4 flex items-center justify-between">
    <div className="flex items-center gap-2">
      <AlertCircle size={16} className="text-red-500" />
      <span className="text-red-800 dark:text-red-200">{error}</span>
    </div>
    <button
      onClick={onDismiss}
      className="text-red-500 hover:text-red-700 text-lg font-bold"
    >
      ×
    </button>
  </div>
);

// --- Welcome Message Component ---
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
      Welcome to TRMS AI Assistant
    </h3>
    <p className="text-gray-600 dark:text-gray-400 mb-6 max-w-md mx-auto">
      I can help you with treasury operations, account management, EOD processing, and financial data analysis.
    </p>
    <div className="text-sm text-gray-500 dark:text-gray-400">
      Try asking about accounts, balances, transactions, or EOD readiness.
    </div>
  </motion.div>
);

// --- Utility Functions ---
const getContextualSuggestions = (lastMessage) => {
  if (!lastMessage || lastMessage.sender === 'user') return [];
  
  const content = lastMessage.text.toLowerCase();
  
  if (content.includes('account') && content.includes('usd')) {
    return [
      'Check balance for ACC-001-USD',
      'Transfer funds between accounts',
      'Show account transaction history'
    ];
  }
  
  if (content.includes('eod') || content.includes('end-of-day')) {
    return [
      'Propose missing rate fixings',
      'Validate new transactions',
      'Check market data status'
    ];
  }
  
  if (content.includes('transaction') || content.includes('transfer')) {
    return [
      'Show recent transactions',
      'Check account balances',
      'Run transaction validation report'
    ];
  }
  
  return [
    'Show me all USD accounts',
    'Can we run End-of-Day?',
    'What\'s the current system status?'
  ];
};

export default App;
