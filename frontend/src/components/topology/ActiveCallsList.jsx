import React from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { ChevronRight, ChevronDown, Clock, CheckCircle2, XCircle, Loader2 } from 'lucide-react';

/**
 * ActiveCallsList - Displays active and recent service calls
 *
 * Shows:
 * - Currently executing function calls
 * - Recent completed calls with timing
 * - Expandable details with request/response info
 */
const ActiveCallsList = ({ activeCalls = [], recentEvents = [], expandedCall, onToggleExpand }) => {
  // Show active calls and all recent events from the last request
  const allCalls = [...activeCalls, ...recentEvents];

  if (allCalls.length === 0) {
    return (
      <div className="h-full flex items-center justify-center p-4 text-center text-gray-500 dark:text-gray-400 text-sm">
        No active calls. Send a message to see the magic happen!
      </div>
    );
  }

  return (
    <div className="h-full flex flex-col overflow-hidden">
      <div className="flex-shrink-0 p-3 border-b border-gray-200 dark:border-gray-700">
        <h4 className="text-xs font-semibold text-gray-500 dark:text-gray-400 uppercase">
          {activeCalls.length > 0 ? 'Active Now' : 'Last Request'}
        </h4>
      </div>
      <div className="flex-1 overflow-y-auto p-3 space-y-2">
        <AnimatePresence mode="popLayout">
          {allCalls.map(call => (
            <CallItem
              key={call.id}
              call={call}
              isExpanded={expandedCall === call.id}
              onToggle={() => onToggleExpand(expandedCall === call.id ? null : call.id)}
            />
          ))}
        </AnimatePresence>
      </div>
    </div>
  );
};

const CallItem = ({ call, isExpanded, onToggle }) => {
  const statusConfig = {
    active: {
      icon: Loader2,
      color: 'text-blue-600 dark:text-blue-400',
      bg: 'bg-blue-50 dark:bg-blue-900/20',
      label: 'Running'
    },
    completed: {
      icon: CheckCircle2,
      color: 'text-green-600 dark:text-green-400',
      bg: 'bg-green-50 dark:bg-green-900/20',
      label: 'Success'
    },
    failed: {
      icon: XCircle,
      color: 'text-red-600 dark:text-red-400',
      bg: 'bg-red-50 dark:bg-red-900/20',
      label: 'Failed'
    }
  };

  const config = statusConfig[call.status] || statusConfig.active;
  const StatusIcon = config.icon;

  return (
    <motion.div
      layout
      initial={{ opacity: 0, y: -10 }}
      animate={{ opacity: 1, y: 0 }}
      exit={{ opacity: 0, y: -10 }}
      className={`rounded-lg border border-gray-200 dark:border-gray-700 ${config.bg} overflow-hidden`}
    >
      {/* Header */}
      <button
        onClick={onToggle}
        className="w-full px-3 py-2 flex items-center justify-between hover:bg-white/50 dark:hover:bg-gray-800/50 transition-colors"
      >
        <div className="flex items-center gap-2 flex-1 min-w-0">
          <StatusIcon
            className={`w-4 h-4 flex-shrink-0 ${config.color} ${
              call.status === 'active' ? 'animate-spin' : ''
            }`}
          />
          <div className="flex-1 min-w-0 text-left">
            <div className="text-sm font-medium text-gray-800 dark:text-gray-200 truncate">
              {call.function || call.type || 'API Call'}
            </div>
            <div className="text-xs text-gray-500 dark:text-gray-400 flex items-center gap-2">
              <span>{config.label}</span>
              {call.duration && (
                <>
                  <span>•</span>
                  <span className="flex items-center gap-1">
                    <Clock className="w-3 h-3" />
                    {call.duration}ms
                  </span>
                </>
              )}
            </div>
          </div>
        </div>
        <motion.div
          animate={{ rotate: isExpanded ? 90 : 0 }}
          transition={{ duration: 0.2 }}
        >
          <ChevronRight className="w-4 h-4 text-gray-400" />
        </motion.div>
      </button>

      {/* Expanded Details */}
      <AnimatePresence>
        {isExpanded && (
          <motion.div
            initial={{ height: 0, opacity: 0 }}
            animate={{ height: 'auto', opacity: 1 }}
            exit={{ height: 0, opacity: 0 }}
            transition={{ duration: 0.2 }}
            className="border-t border-gray-200 dark:border-gray-700"
          >
            <div className="p-3 space-y-2 text-xs">
              {/* Service Path */}
              {call.path && (
                <div>
                  <div className="font-semibold text-gray-700 dark:text-gray-300 mb-1">
                    Path
                  </div>
                  <div className="text-gray-600 dark:text-gray-400 font-mono">
                    {call.path}
                  </div>
                </div>
              )}

              {/* Parameters */}
              {call.params && (
                <div>
                  <div className="font-semibold text-gray-700 dark:text-gray-300 mb-1">
                    Parameters
                  </div>
                  <div className="bg-gray-100 dark:bg-gray-800 rounded p-2 font-mono text-gray-600 dark:text-gray-400">
                    {JSON.stringify(call.params, null, 2)}
                  </div>
                </div>
              )}

              {/* Response */}
              {call.response && (
                <div>
                  <div className="font-semibold text-gray-700 dark:text-gray-300 mb-1">
                    Response
                  </div>
                  <div className="bg-gray-100 dark:bg-gray-800 rounded p-2 font-mono text-gray-600 dark:text-gray-400 max-h-32 overflow-y-auto">
                    {typeof call.response === 'string'
                      ? call.response
                      : JSON.stringify(call.response, null, 2)}
                  </div>
                </div>
              )}

              {/* Error */}
              {call.error && (
                <div>
                  <div className="font-semibold text-red-700 dark:text-red-400 mb-1">
                    Error
                  </div>
                  <div className="bg-red-100 dark:bg-red-900/20 rounded p-2 text-red-600 dark:text-red-400">
                    {call.error}
                  </div>
                </div>
              )}
            </div>
          </motion.div>
        )}
      </AnimatePresence>
    </motion.div>
  );
};

export default ActiveCallsList;
