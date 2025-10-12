import React, { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import { Server, Cpu, Database, Send } from 'lucide-react';

/**
 * ServiceGraph - Visual representation of service topology
 *
 * Displays the 4-service architecture:
 * - Frontend (React) @ port 5174
 * - AI Backend (Spring Boot) @ port 8080
 * - TRMS Mock (Legacy) @ port 8090
 * - SWIFT Mock @ port 8091
 *
 * Shows animated connections when services communicate
 */
const ServiceGraph = ({ activeCalls = [], recentEvents = [] }) => {
  const [activeConnections, setActiveConnections] = useState([]);

  // Define service nodes
  const services = [
    {
      id: 'frontend',
      name: 'Frontend',
      port: '5174',
      icon: Server,
      color: 'blue',
      position: { x: 50, y: 20 }
    },
    {
      id: 'ai-backend',
      name: 'NACC AI',
      port: '8080',
      icon: Cpu,
      color: 'purple',
      position: { x: 50, y: 50 }
    },
    {
      id: 'trms',
      name: 'FINDUR Mock',
      port: '8090',
      icon: Database,
      color: 'green',
      position: { x: 20, y: 80 }
    },
    {
      id: 'swift',
      name: 'HABS Mock',
      port: '8091',
      icon: Send,
      color: 'orange',
      position: { x: 80, y: 80 }
    }
  ];

  // Track active connections based on active calls
  useEffect(() => {
    const connections = activeCalls.map(call => {
      return {
        id: call.id,
        source: call.source || 'frontend',
        target: call.target || 'ai-backend',
        status: call.status || 'active',
        function: call.function
      };
    });

    setActiveConnections(connections);
  }, [activeCalls]);

  // Service node component
  const ServiceNode = ({ service, isActive }) => {
    const Icon = service.icon;
    const colorClasses = {
      blue: 'bg-blue-100 dark:bg-blue-900/30 border-blue-500 text-blue-600 dark:text-blue-400',
      purple: 'bg-purple-100 dark:bg-purple-900/30 border-purple-500 text-purple-600 dark:text-purple-400',
      green: 'bg-green-100 dark:bg-green-900/30 border-green-500 text-green-600 dark:text-green-400',
      orange: 'bg-orange-100 dark:bg-orange-900/30 border-orange-500 text-orange-600 dark:text-orange-400'
    };

    return (
      <motion.div
        className={`absolute transform -translate-x-1/2 -translate-y-1/2 ${
          isActive ? 'z-10' : 'z-0'
        }`}
        style={{
          left: `${service.position.x}%`,
          top: `${service.position.y}%`
        }}
        animate={{
          scale: isActive ? 1.1 : 1,
          boxShadow: isActive
            ? '0 0 20px rgba(59, 130, 246, 0.5)'
            : '0 2px 4px rgba(0, 0, 0, 0.1)'
        }}
        transition={{ duration: 0.2 }}
      >
        <div
          className={`rounded-lg border-2 p-3 backdrop-blur-sm ${
            colorClasses[service.color]
          } ${isActive ? 'ring-2 ring-offset-2 ring-blue-400' : ''}`}
        >
          <Icon className="w-6 h-6 mb-1" />
          <div className="text-xs font-semibold whitespace-nowrap">{service.name}</div>
          <div className="text-xs opacity-70">:{service.port}</div>
        </div>
      </motion.div>
    );
  };

  // Connection line component
  const ConnectionLine = ({ from, to, status }) => {
    const fromService = services.find(s => s.id === from);
    const toService = services.find(s => s.id === to);

    if (!fromService || !toService) return null;

    const x1 = fromService.position.x;
    const y1 = fromService.position.y;
    const x2 = toService.position.x;
    const y2 = toService.position.y;

    return (
      <svg className="absolute inset-0 w-full h-full pointer-events-none" style={{ zIndex: 5 }}>
        <defs>
          <linearGradient id={`gradient-${from}-${to}`} x1="0%" y1="0%" x2="100%" y2="0%">
            <stop offset="0%" stopColor="#3b82f6" stopOpacity="0.8" />
            <stop offset="100%" stopColor="#8b5cf6" stopOpacity="0.8" />
          </linearGradient>
        </defs>

        {/* Connection line */}
        <motion.line
          x1={`${x1}%`}
          y1={`${y1}%`}
          x2={`${x2}%`}
          y2={`${y2}%`}
          stroke={`url(#gradient-${from}-${to})`}
          strokeWidth="2"
          initial={{ pathLength: 0, opacity: 0 }}
          animate={{ pathLength: 1, opacity: 1 }}
          transition={{ duration: 0.5 }}
        />

        {/* Animated pulse - travels from source to target */}
        <circle r="6" fill="#3b82f6" filter="url(#glow)">
          <animate
            attributeName="cx"
            from={`${x1}%`}
            to={`${x2}%`}
            dur="2s"
            repeatCount="indefinite"
          />
          <animate
            attributeName="cy"
            from={`${y1}%`}
            to={`${y2}%`}
            dur="2s"
            repeatCount="indefinite"
          />
        </circle>

        {/* Glow filter for pulse */}
        <defs>
          <filter id="glow">
            <feGaussianBlur stdDeviation="2" result="coloredBlur"/>
            <feMerge>
              <feMergeNode in="coloredBlur"/>
              <feMergeNode in="SourceGraphic"/>
            </feMerge>
          </filter>
        </defs>
      </svg>
    );
  };

  // Determine which services are currently active
  const activeServiceIds = new Set();
  activeConnections.forEach(conn => {
    activeServiceIds.add(conn.source);
    activeServiceIds.add(conn.target);
  });

  return (
    <div className="relative w-full h-full min-h-[300px]">
      {/* Connection lines */}
      {activeConnections.map((conn, idx) => (
        <ConnectionLine
          key={`${conn.source}-${conn.target}-${idx}`}
          from={conn.source}
          to={conn.target}
          status={conn.status}
        />
      ))}

      {/* Service nodes */}
      {services.map(service => (
        <ServiceNode
          key={service.id}
          service={service}
          isActive={activeServiceIds.has(service.id)}
        />
      ))}

      {/* Legend */}
      <div className="absolute bottom-4 left-4 bg-white dark:bg-gray-800 rounded-lg p-2 shadow-lg border border-gray-200 dark:border-gray-700">
        <div className="text-xs text-gray-500 dark:text-gray-400 mb-1">Legend</div>
        <div className="flex flex-col gap-1 text-xs">
          <div className="flex items-center gap-2">
            <div className="w-3 h-3 bg-blue-500 rounded" />
            <span className="text-gray-700 dark:text-gray-300">Frontend</span>
          </div>
          <div className="flex items-center gap-2">
            <div className="w-3 h-3 bg-purple-500 rounded" />
            <span className="text-gray-700 dark:text-gray-300">AI Backend</span>
          </div>
          <div className="flex items-center gap-2">
            <div className="w-3 h-3 bg-green-500 rounded" />
            <span className="text-gray-700 dark:text-gray-300">TRMS</span>
          </div>
          <div className="flex items-center gap-2">
            <div className="w-3 h-3 bg-orange-500 rounded" />
            <span className="text-gray-700 dark:text-gray-300">SWIFT</span>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ServiceGraph;
