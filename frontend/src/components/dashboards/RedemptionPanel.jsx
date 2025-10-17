import React from 'react';

/**
 * Redemption Panel Component
 *
 * Displays redemption report processing status
 *
 * @param {Object} redemptionStatus - Redemption report status data
 */
const RedemptionPanel = ({ redemptionStatus }) => {
  if (!redemptionStatus) {
    return (
      <div className="bg-white dark:bg-gray-800 rounded-lg shadow-sm border border-gray-200 dark:border-gray-700 p-6">
        <h2 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">
          Redemption Report
        </h2>
        <div className="text-center py-8 text-gray-500 dark:text-gray-400">
          Loading redemption data...
        </div>
      </div>
    );
  }

  const status = redemptionStatus.status || 'UNKNOWN';
  const fileName = redemptionStatus.fileName;
  const totalRedemptions = redemptionStatus.totalRedemptions || 0;
  const processedCount = redemptionStatus.processedCount || 0;
  const failedCount = redemptionStatus.failedCount || 0;
  const totalAmount = redemptionStatus.totalAmount || 0;
  const currency = redemptionStatus.currency || 'USD';
  const mismatchCount = redemptionStatus.mismatchCount || 0;

  // Get status flow step
  const getStatusStep = (currentStatus) => {
    const statusMap = {
      'RECEIVED': 1,
      'VERIFIED': 2,
      'PROCESSED': 3
    };
    return statusMap[currentStatus] || 0;
  };

  const currentStep = getStatusStep(status);

  // Format currency
  const formatCurrency = (amount, curr) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: curr,
      minimumFractionDigits: 0
    }).format(amount);
  };

  return (
    <div className="bg-white dark:bg-gray-800 rounded-lg shadow-sm border border-gray-200 dark:border-gray-700 p-6">
      <div className="flex items-center justify-between mb-6">
        <h2 className="text-lg font-semibold text-gray-900 dark:text-white">
          Redemption Report
        </h2>
        <div className={`px-3 py-1 rounded-full text-xs font-medium ${
          status === 'PROCESSED'
            ? 'bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-400'
            : status === 'VERIFIED'
            ? 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900/30 dark:text-yellow-400'
            : 'bg-blue-100 text-blue-800 dark:bg-blue-900/30 dark:text-blue-400'
        }`}>
          {status}
        </div>
      </div>

      {/* Status Flow */}
      <div className="mb-6">
        <div className="flex items-center justify-between">
          <div className="text-center flex-1">
            <div className={`w-10 h-10 mx-auto rounded-full flex items-center justify-center mb-2 ${
              currentStep >= 1
                ? 'bg-blue-500 text-white'
                : 'bg-gray-200 dark:bg-gray-700 text-gray-400'
            }`}>
              <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
                <path fillRule="evenodd" d="M6 2a2 2 0 00-2 2v12a2 2 0 002 2h8a2 2 0 002-2V7.414A2 2 0 0015.414 6L12 2.586A2 2 0 0010.586 2H6zm5 6a1 1 0 10-2 0v3.586l-1.293-1.293a1 1 0 10-1.414 1.414l3 3a1 1 0 001.414 0l3-3a1 1 0 00-1.414-1.414L11 11.586V8z" clipRule="evenodd" />
              </svg>
            </div>
            <div className={`text-xs font-medium ${
              currentStep >= 1 ? 'text-blue-600 dark:text-blue-400' : 'text-gray-400'
            }`}>
              Received
            </div>
          </div>

          <div className={`flex-1 h-0.5 ${
            currentStep >= 2 ? 'bg-blue-500' : 'bg-gray-300 dark:bg-gray-600'
          }`} />

          <div className="text-center flex-1">
            <div className={`w-10 h-10 mx-auto rounded-full flex items-center justify-center mb-2 ${
              currentStep >= 2
                ? 'bg-yellow-500 text-white'
                : 'bg-gray-200 dark:bg-gray-700 text-gray-400'
            }`}>
              <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
                <path fillRule="evenodd" d="M6.267 3.455a3.066 3.066 0 001.745-.723 3.066 3.066 0 013.976 0 3.066 3.066 0 001.745.723 3.066 3.066 0 012.812 2.812c.051.643.304 1.254.723 1.745a3.066 3.066 0 010 3.976 3.066 3.066 0 00-.723 1.745 3.066 3.066 0 01-2.812 2.812 3.066 3.066 0 00-1.745.723 3.066 3.066 0 01-3.976 0 3.066 3.066 0 00-1.745-.723 3.066 3.066 0 01-2.812-2.812 3.066 3.066 0 00-.723-1.745 3.066 3.066 0 010-3.976 3.066 3.066 0 00.723-1.745 3.066 3.066 0 012.812-2.812zm7.44 5.252a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
              </svg>
            </div>
            <div className={`text-xs font-medium ${
              currentStep >= 2 ? 'text-yellow-600 dark:text-yellow-400' : 'text-gray-400'
            }`}>
              Verified
            </div>
          </div>

          <div className={`flex-1 h-0.5 ${
            currentStep >= 3 ? 'bg-green-500' : 'bg-gray-300 dark:bg-gray-600'
          }`} />

          <div className="text-center flex-1">
            <div className={`w-10 h-10 mx-auto rounded-full flex items-center justify-center mb-2 ${
              currentStep >= 3
                ? 'bg-green-500 text-white'
                : 'bg-gray-200 dark:bg-gray-700 text-gray-400'
            }`}>
              <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
                <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
              </svg>
            </div>
            <div className={`text-xs font-medium ${
              currentStep >= 3 ? 'text-green-600 dark:text-green-400' : 'text-gray-400'
            }`}>
              Processed
            </div>
          </div>
        </div>
      </div>

      {/* File Info */}
      {fileName && (
        <div className="mb-4 p-3 bg-gray-50 dark:bg-gray-700/50 rounded-lg">
          <div className="text-xs text-gray-500 dark:text-gray-400 mb-1">File</div>
          <div className="text-sm font-medium text-gray-900 dark:text-white font-mono">
            {fileName}
          </div>
        </div>
      )}

      {/* Statistics */}
      <div className="grid grid-cols-2 gap-4 mb-4">
        <div className="p-3 bg-blue-50 dark:bg-blue-900/20 rounded-lg">
          <div className="text-2xl font-bold text-blue-600 dark:text-blue-400">
            {totalRedemptions}
          </div>
          <div className="text-xs text-gray-600 dark:text-gray-400">
            Total Redemptions
          </div>
        </div>
        <div className="p-3 bg-green-50 dark:bg-green-900/20 rounded-lg">
          <div className="text-2xl font-bold text-green-600 dark:text-green-400">
            {processedCount}
          </div>
          <div className="text-xs text-gray-600 dark:text-gray-400">
            Processed
          </div>
        </div>
      </div>

      {/* Total Amount */}
      <div className="p-4 bg-gradient-to-r from-blue-50 to-green-50 dark:from-blue-900/20 dark:to-green-900/20 rounded-lg mb-4">
        <div className="text-xs text-gray-600 dark:text-gray-400 mb-1">Total Amount</div>
        <div className="text-2xl font-bold text-gray-900 dark:text-white">
          {formatCurrency(totalAmount, currency)}
        </div>
      </div>

      {/* Errors/Mismatches */}
      {(failedCount > 0 || mismatchCount > 0) && (
        <div className="p-3 bg-red-50 dark:bg-red-900/20 rounded-lg border border-red-200 dark:border-red-800">
          <div className="flex items-center justify-between">
            <div className="flex items-center text-red-700 dark:text-red-400">
              <svg className="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              <span className="text-sm font-medium">Issues Detected</span>
            </div>
            <div className="text-right">
              {failedCount > 0 && (
                <div className="text-sm font-semibold text-red-700 dark:text-red-400">
                  {failedCount} failed
                </div>
              )}
              {mismatchCount > 0 && (
                <div className="text-xs text-red-600 dark:text-red-400">
                  {mismatchCount} mismatches
                </div>
              )}
            </div>
          </div>
        </div>
      )}

      {/* Last Processed Time */}
      {redemptionStatus.lastProcessed && (
        <div className="mt-4 pt-4 border-t border-gray-200 dark:border-gray-700">
          <div className="text-xs text-gray-500 dark:text-gray-400">
            Last processed: {new Date(redemptionStatus.lastProcessed).toLocaleString()}
          </div>
        </div>
      )}
    </div>
  );
};

export default RedemptionPanel;
