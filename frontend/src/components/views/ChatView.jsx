import React, { useState, useEffect, useRef } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { Send, Loader, Network, X } from 'lucide-react';
import Header from '../layout/Header';
import Footer from '../layout/Footer';
import WelcomeMessage from '../messages/WelcomeMessage';
import LoadingBubble from '../messages/LoadingBubble';
import { EnhancedMessageBubble } from '../messages/EnhancedMessageBubble';
import { SuggestedActions } from '../ui/SuggestedActions';
import { getContextualSuggestions } from '../../utils/messageUtils';
import ServiceTopologyPanel from '../topology/ServiceTopologyPanel';
import useServiceEvents from '../../hooks/useServiceEvents';

/**
 * ChatView Component - Main chat interface
 * 
 * This component displays the chat interface including:
 * - Header with AI avatar and connection status
 * - Message history with enhanced formatting
 * - Loading indicators and welcome message
 * - Footer with input form and connection warnings
 * - Contextual suggestions based on conversation
 * 
 * @param {Array} messages - Array of message objects
 * @param {Function} onNewMessage - Callback for sending new messages
 * @param {boolean} isLoading - Whether AI is currently responding
 * @param {string} connectionStatus - Backend connection status
 */
const ChatView = ({ messages, onNewMessage, isLoading, connectionStatus }) => {
  const [input, setInput] = useState('');
  const [showTopology, setShowTopology] = useState(false);
  const messagesEndRef = useRef(null);

  // Track service events from messages
  const { activeCalls, recentEvents } = useServiceEvents(messages);

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
      className="flex flex-row flex-1 w-full h-full overflow-hidden"
      initial={{ opacity: 0, y: 50 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.5, ease: "easeInOut" }}
    >
      {/* Main Chat Area */}
      <div className="flex flex-col flex-1 min-w-0 bg-white dark:bg-gray-800">
        <Header connectionStatus={connectionStatus}>
          {/* Topology Toggle Button */}
          <button
            onClick={() => setShowTopology(!showTopology)}
            className="ml-4 p-2 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors"
            title={showTopology ? "Hide Service Graph" : "Show Service Graph"}
          >
            {showTopology ? (
              <X className="w-5 h-5 text-gray-600 dark:text-gray-300" />
            ) : (
              <Network className="w-5 h-5 text-gray-600 dark:text-gray-300" />
            )}
          </button>
        </Header>

        <main className="flex-1 overflow-y-auto p-4 sm:p-6 space-y-4 bg-gray-50 dark:bg-gray-900 scrollbar-thin">
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

        <Footer
          input={input}
          setInput={setInput}
          onSubmit={handleSubmit}
          isLoading={isLoading}
          connectionStatus={connectionStatus}
        />
      </div>

      {/* Service Topology Panel - Right Side */}
      <AnimatePresence>
        {showTopology && (
          <ServiceTopologyPanel
            isVisible={showTopology}
            activeCalls={activeCalls}
            recentEvents={recentEvents}
          />
        )}
      </AnimatePresence>
    </motion.div>
  );
};

export default ChatView;