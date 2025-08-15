import React, { useState, useEffect, useRef } from 'react';
import { motion } from 'framer-motion';
import { Send, Loader } from 'lucide-react';
import Header from '../layout/Header';
import Footer from '../layout/Footer';
import WelcomeMessage from '../messages/WelcomeMessage';
import LoadingBubble from '../messages/LoadingBubble';
import { EnhancedMessageBubble } from '../messages/EnhancedMessageBubble';
import { SuggestedActions } from '../ui/SuggestedActions';
import { getContextualSuggestions } from '../../utils/messageUtils';

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
      <Header connectionStatus={connectionStatus} />
      
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
      
      <Footer 
        input={input}
        setInput={setInput}
        onSubmit={handleSubmit}
        isLoading={isLoading}
        connectionStatus={connectionStatus}
      />
    </motion.div>
  );
};

export default ChatView;