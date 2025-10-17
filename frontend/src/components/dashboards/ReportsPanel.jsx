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
      <div className="bg-white dark:bg-gray-800 rounded-lg shadow-sm border border-gray-200 dark:border-gray-700 p-6">
        <h2 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">
          EOD Reports
        </h2>
        <div className="text-center py-8 text-gray-500 dark:text-gray-400">
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
    <div className="bg-white dark:bg-gray-800 rounded-lg shadow-sm border border-gray-200 dark:border-gray-700 p-6">
      <div className="flex items-center justify-between mb-6">
        <h2 className="text-lg font-semibold text-gray-900 dark:text-white">
          EOD Reports
        </h2>
        <div className={`px-3 py-1 rounded-full text-xs font-medium ${
          verified
            ? 'bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-400'
            : 'bg-red-100 text-red-800 dark:bg-red-900/30 dark:text-red-400'
        }`}>
          {verified ? '✓ Verified' : '✗ Not Verified'}
        </div>
      </div>

      {/* Summary */}
      <div className="mb-6 p-4 bg-gray-50 dark:bg-gray-700/50 rounded-lg">
        <div className="flex items-center justify-between">
          <div className="text-center flex-1">
            <div className="text-2xl font-bold text-gray-900 dark:text-white">
              {passedReports}/{totalReports}
            </div>
            <div className="text-xs text-gray-500 dark:text-gray-400">
              Reports Passed
            </div>
          </div>
          {failedReports > 0 && (
            <>
              <div className="w-px h-12 bg-gray-300 dark:bg-gray-600" />
              <div className="text-center flex-1">
                <div className="text-2xl font-bold text-red-600 dark:text-red-400">
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
      <div className="space-y-3">
        {reports.map((report, index) => {
          const isPassed = report.status === 'PASSED';
          return (
            <div
              key={index}
              className={`p-3 rounded-lg border transition-all ${
                isPassed
                  ? 'border-green-200 dark:border-green-800 bg-green-50 dark:bg-green-900/20'
                  : 'border-red-200 dark:border-red-800 bg-red-50 dark:bg-red-900/20'
              }`}
            >
              <div className="flex items-start justify-between">
                <div className="flex-1">
                  <div className="flex items-center space-x-2">
                    <span className={`text-lg ${
                      isPassed ? 'text-green-600 dark:text-green-400' : 'text-red-600 dark:text-red-400'
                    }`}>
                      {isPassed ? '✓' : '✗'}
                    </span>
                    <span className="text-sm font-medium text-gray-900 dark:text-white">
                      {report.name}
                    </span>
                  </div>
                  <div className="mt-2 flex items-center space-x-4 text-xs text-gray-500 dark:text-gray-400">
                    {report.size && (
                      <span className="flex items-center">
                        <svg className="w-3 h-3 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                        </svg>
                        {report.size}
                      </span>
                    )}
                    {report.timestamp && (
                      <span className="flex items-center">
                        <svg className="w-3 h-3 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                        </svg>
                        {new Date(report.timestamp).toLocaleTimeString()}
                      </span>
                    )}
                  </div>
                </div>
              </div>
            </div>
          );
        })}
      </div>

      {/* Last Verification Time */}
      {eodReports.lastVerification && (
        <div className="mt-6 pt-4 border-t border-gray-200 dark:border-gray-700">
          <div className="text-xs text-gray-500 dark:text-gray-400 flex items-center">
            <svg className="w-3 h-3 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4" />
            </svg>
            Last verified: {new Date(eodReports.lastVerification).toLocaleString()}
          </div>
        </div>
      )}
    </div>
  );
};

export default ReportsPanel;
