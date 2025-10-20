import React from 'react';

/**
 * EOD Status Panel Component
 *
 * Displays End-of-Day processing readiness status with detailed checks
 *
 * @param {Object} eodStatus - EOD status object from backend
 */
const EODStatusPanel = ({ eodStatus }) => {
  if (!eodStatus) {
    return (
      <div className="bg-white dark:bg-gray-800 rounded-lg shadow-sm border border-gray-200 dark:border-gray-700 p-6">
        <h2 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">
          EOD Readiness
        </h2>
        <div className="text-center py-8 text-gray-500 dark:text-gray-400">
          Loading EOD status...
        </div>
      </div>
    );
  }

  const isReady = eodStatus.ready || eodStatus.isReady;
  const blockingIssues = eodStatus.blockers || [];
  const warnings = eodStatus.warnings || [];
  const requiredActions = eodStatus.requiredActions || [];
  const overallStatus = eodStatus.overallStatus || 'UNKNOWN';
  const readinessPercentage = eodStatus.readinessPercentage || 0;

  return (
    <div className="bg-white dark:bg-gray-800 rounded-lg shadow-sm border border-gray-200 dark:border-gray-700 p-4">
      <h2 className="text-base font-semibold text-gray-900 dark:text-white mb-4">
        EOD Readiness
      </h2>

      {/* Main Status Indicator */}
      <div className={`flex items-center justify-center p-4 rounded-lg mb-4 ${
        isReady
          ? 'bg-green-50 dark:bg-green-900/20'
          : 'bg-red-50 dark:bg-red-900/20'
      }`}>
        <div className={`w-4 h-4 rounded-full mr-3 ${
          isReady
            ? 'bg-green-500 animate-pulse'
            : 'bg-red-500'
        }`} />
        <div className="text-center">
          <div className={`text-2xl font-bold ${
            isReady
              ? 'text-green-700 dark:text-green-400'
              : 'text-red-700 dark:text-red-400'
          }`}>
            {isReady ? 'READY' : 'NOT READY'}
          </div>
          <div className="text-sm text-gray-600 dark:text-gray-400 mt-1">
            {overallStatus}
          </div>
        </div>
      </div>

      {/* Readiness Summary */}
      {eodStatus.summary && (
        <div className="mb-6 p-4 bg-gray-50 dark:bg-gray-700/50 rounded-lg">
          <div className="text-xs font-semibold text-gray-700 dark:text-gray-300 mb-2">Summary</div>
          <pre className="text-xs text-gray-600 dark:text-gray-400 whitespace-pre-wrap font-mono">
            {eodStatus.summary}
          </pre>
        </div>
      )}

      {/* Status Details */}
      <div className="space-y-4">
        {/* Blocking Issues */}
        {blockingIssues.length > 0 && (
          <div>
            <h3 className="text-sm font-semibold text-red-700 dark:text-red-400 mb-2">
              Blocking Issues ({blockingIssues.length})
            </h3>
            <div className="space-y-2">
              {blockingIssues.map((issue, index) => (
                <div key={index} className="p-3 bg-red-50 dark:bg-red-900/20 rounded-lg border border-red-200 dark:border-red-800">
                  <div className="flex items-start">
                    <svg className="w-5 h-5 text-red-600 dark:text-red-400 mr-2 flex-shrink-0 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                    </svg>
                    <div className="flex-1">
                      <div className="text-sm font-medium text-red-800 dark:text-red-300">
                        {typeof issue === 'string' ? issue : `${issue.type}: ${issue.description}`}
                      </div>
                      {typeof issue === 'object' && issue.resolution && (
                        <div className="text-xs text-red-700 dark:text-red-400 mt-1">
                          {issue.resolution}
                        </div>
                      )}
                      {typeof issue === 'object' && issue.severity && (
                        <div className="text-xs text-red-600 dark:text-red-500 mt-1">
                          Severity: {issue.severity}
                        </div>
                      )}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Warnings */}
        {warnings.length > 0 && (
          <div>
            <h3 className="text-sm font-semibold text-yellow-700 dark:text-yellow-400 mb-2">
              Warnings ({warnings.length})
            </h3>
            <div className="space-y-2">
              {warnings.map((warning, index) => (
                <div key={index} className="p-3 bg-yellow-50 dark:bg-yellow-900/20 rounded-lg border border-yellow-200 dark:border-yellow-800">
                  <div className="flex items-start">
                    <svg className="w-5 h-5 text-yellow-600 dark:text-yellow-400 mr-2 flex-shrink-0 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                    </svg>
                    <div className="flex-1">
                      <div className="text-sm font-medium text-yellow-800 dark:text-yellow-300">
                        {warning.type}: {warning.description}
                      </div>
                      <div className="text-xs text-yellow-700 dark:text-yellow-400 mt-1">
                        {warning.recommendation}
                      </div>
                      <div className="text-xs text-yellow-600 dark:text-yellow-500 mt-1">
                        Severity: {warning.severity}
                      </div>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Required Actions */}
        {requiredActions.length > 0 && (
          <div>
            <h3 className="text-sm font-semibold text-blue-700 dark:text-blue-400 mb-2">
              Required Actions ({requiredActions.length})
            </h3>
            <div className="space-y-2">
              {requiredActions.map((action, index) => (
                <div key={index} className="flex items-start text-sm text-gray-700 dark:text-gray-300 bg-blue-50 dark:bg-blue-900/20 p-2 rounded">
                  <svg className="w-5 h-5 text-blue-600 dark:text-blue-400 mt-0.5 mr-2 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
                  </svg>
                  {action}
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Last Check Time */}
        {eodStatus.checkTime && (
          <div className="pt-4 border-t border-gray-200 dark:border-gray-700">
            <div className="text-xs text-gray-500 dark:text-gray-400">
              Last checked: {new Date(eodStatus.checkTime).toLocaleString()}
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default EODStatusPanel;
