import React from 'react';

/**
 * EOD Reports Panel Component
 *
 * Displays EOD report verification status
 *
 * @param {Object} eodReports - EOD reports verification data
 */
const ReportsPanel = ({ eodReports }) => {
  if (!eodReports) {
    return (
      <div className="bg-white dark:bg-gray-800 rounded-lg shadow-sm border border-gray-200 dark:border-gray-700 p-4">
        <h2 className="text-base font-semibold text-gray-900 dark:text-white mb-3">
          EOD Reports
        </h2>
        <div className="text-center py-4 text-sm text-gray-500 dark:text-gray-400">
          Loading reports data...
        </div>
      </div>
    );
  }

  const reports = eodReports.reports || [];
  const verified = eodReports.verified;
  const passedReports = eodReports.passedReports || 0;
  const failedReports = eodReports.failedReports || 0;
  const totalReports = eodReports.totalReports || reports.length;

  return (
    <div className="bg-white dark:bg-gray-800 rounded-lg shadow-sm border border-gray-200 dark:border-gray-700 p-4">
      <div className="flex items-center justify-between mb-3">
        <h2 className="text-base font-semibold text-gray-900 dark:text-white">
          EOD Reports
        </h2>
        <div className={`px-2 py-0.5 rounded-full text-xs font-medium ${
          verified
            ? 'bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-400'
            : 'bg-red-100 text-red-800 dark:bg-red-900/30 dark:text-red-400'
        }`}>
          {verified ? '✓' : '✗'}
        </div>
      </div>

      {/* Summary */}
      <div className="mb-3 p-2 bg-gray-50 dark:bg-gray-700/50 rounded">
        <div className="flex items-center justify-between">
          <div className="text-center flex-1">
            <div className="text-lg font-bold text-gray-900 dark:text-white">
              {passedReports}/{totalReports}
            </div>
            <div className="text-xs text-gray-500 dark:text-gray-400">
              Passed
            </div>
          </div>
          {failedReports > 0 && (
            <>
              <div className="w-px h-8 bg-gray-300 dark:bg-gray-600" />
              <div className="text-center flex-1">
                <div className="text-lg font-bold text-red-600 dark:text-red-400">
                  {failedReports}
                </div>
                <div className="text-xs text-gray-500 dark:text-gray-400">
                  Failed
                </div>
              </div>
            </>
          )}
        </div>
      </div>

      {/* Report List */}
      <div className="space-y-1.5">
        {reports.map((report, index) => {
          const isPassed = report.status === 'PASSED';
          return (
            <div
              key={index}
              className={`p-2 rounded border transition-all ${
                isPassed
                  ? 'border-green-200 dark:border-green-800 bg-green-50 dark:bg-green-900/20'
                  : 'border-red-200 dark:border-red-800 bg-red-50 dark:bg-red-900/20'
              }`}
            >
              <div className="flex items-center justify-between">
                <div className="flex items-center space-x-1.5 flex-1">
                  <span className={`text-sm ${
                    isPassed ? 'text-green-600 dark:text-green-400' : 'text-red-600 dark:text-red-400'
                  }`}>
                    {isPassed ? '✓' : '✗'}
                  </span>
                  <span className="text-xs font-medium text-gray-900 dark:text-white truncate">
                    {report.name?.replace('_report_', ' ').replace(/_/g, ' ')}
                  </span>
                </div>
                {report.timestamp && (
                  <span className="text-xs text-gray-500 dark:text-gray-400">
                    {new Date(report.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                  </span>
                )}
              </div>
            </div>
          );
        })}
      </div>

      {/* Last Verification Time */}
      {eodReports.lastVerification && (
        <div className="mt-3 pt-3 border-t border-gray-200 dark:border-gray-700">
          <div className="text-xs text-gray-500 dark:text-gray-400">
            Last: {new Date(eodReports.lastVerification).toLocaleTimeString()}
          </div>
        </div>
      )}
    </div>
  );
};

export default ReportsPanel;
