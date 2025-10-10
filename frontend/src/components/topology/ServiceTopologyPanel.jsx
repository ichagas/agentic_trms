import React, { useState, useEffect, useCallback } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { Activity, Zap, CheckCircle2, XCircle, Clock, ChevronRight } from 'lucide-react';
import ServiceGraph from './ServiceGraph';
import ActiveCallsList from './ActiveCallsList';

/**
 * ServiceTopologyPanel - Shows real-time service communication visualization
 *
 * This component displays:
 * - Live service topology graph (Frontend, AI Backend, TRMS, SWIFT)
 * - Active API calls with timing information
 * - Request/response flow animations
 * - Function call tracking
 *
 * @param {boolean} isVisible - Whether the panel is visible
 * @param {Array} activeCalls - Array of currently active service calls
 * @param {Array} recentEvents - Array of recent service events
 */
const ServiceTopologyPanel = ({ isVisible = true, activeCalls = [], recentEvents = [] }) => {
  const [expandedCall, setExpandedCall] = useState(null);
  const [stats, setStats] = useState({
    totalCalls: 0,
    successRate: 100,
    avgResponseTime: 0
  });

  // Calculate statistics from recent events
  useEffect(() => {
    if (recentEvents.length > 0) {
      const completed = recentEvents.filter(e => e.status === 'completed');
      const failed = recentEvents.filter(e => e.status === 'failed');
      const total = completed.length + failed.length;

      const avgTime = completed.length > 0
        ? completed.reduce((sum, e) => sum + (e.duration || 0), 0) / completed.length
        : 0;

      setStats({
        totalCalls: total,
        successRate: total > 0 ? Math.round((completed.length / total) * 100) : 100,
        avgResponseTime: Math.round(avgTime)
      });
    }
  }, [recentEvents]);

  if (!isVisible) return null;

  return (
    <motion.div
      className="hidden lg:flex flex-col h-full w-[30rem] xl:w-[36rem] flex-shrink-0 bg-gray-50 dark:bg-gray-900 border-l border-gray-200 dark:border-gray-700"
      initial={{ x: 400, opacity: 0 }}
      animate={{ x: 0, opacity: 1 }}
      exit={{ x: 400, opacity: 0 }}
      transition={{ duration: 0.3, ease: "easeOut" }}
    >
      {/* Header */}
      <div className="p-4 border-b border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800">
        <div className="flex items-center justify-between mb-3">
          <div className="flex items-center gap-2">
            <Activity className="w-5 h-5 text-blue-600" />
            <h3 className="font-semibold text-gray-800 dark:text-gray-200">
              Service Topology
            </h3>
          </div>
          <div className="flex items-center gap-2">
            <div className="w-2 h-2 bg-green-500 rounded-full animate-pulse" />
            <span className="text-xs text-gray-500 dark:text-gray-400">Live</span>
          </div>
        </div>

        {/* Stats Bar */}
        <div className="grid grid-cols-3 gap-2 text-center">
          <div className="bg-blue-50 dark:bg-blue-900/20 rounded px-2 py-1">
            <div className="text-xs text-gray-500 dark:text-gray-400">Calls</div>
            <div className="text-sm font-semibold text-blue-600 dark:text-blue-400">
              {stats.totalCalls}
            </div>
          </div>
          <div className="bg-green-50 dark:bg-green-900/20 rounded px-2 py-1">
            <div className="text-xs text-gray-500 dark:text-gray-400">Success</div>
            <div className="text-sm font-semibold text-green-600 dark:text-green-400">
              {stats.successRate}%
            </div>
          </div>
          <div className="bg-purple-50 dark:bg-purple-900/20 rounded px-2 py-1">
            <div className="text-xs text-gray-500 dark:text-gray-400">Avg Time</div>
            <div className="text-sm font-semibold text-purple-600 dark:text-purple-400">
              {stats.avgResponseTime}ms
            </div>
          </div>
        </div>
      </div>

      {/* Service Graph - 60% height */}
      <div className="h-[60%] overflow-hidden relative bg-gradient-to-br from-gray-50 to-gray-100 dark:from-gray-900 dark:to-gray-800">
        <ServiceGraph activeCalls={activeCalls} recentEvents={recentEvents} />
      </div>

      {/* Active Calls List - 40% height */}
      <div className="h-[40%] border-t border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800">
        <ActiveCallsList
          activeCalls={activeCalls}
          recentEvents={recentEvents}
          expandedCall={expandedCall}
          onToggleExpand={setExpandedCall}
        />
      </div>
    </motion.div>
  );
};

export default ServiceTopologyPanel;
