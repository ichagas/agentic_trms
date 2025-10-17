import React from 'react';

/**
 * Accounts Panel Component
 *
 * Displays all accounts grouped by currency with balances
 * Highlights accounts with recent balance changes
 *
 * @param {Array} accounts - List of account objects
 * @param {Array} changedAccounts - List of account IDs that changed recently
 */
const AccountsPanel = ({ accounts = [], changedAccounts = [] }) => {
  // Group accounts by currency
  const groupedAccounts = accounts.reduce((groups, account) => {
    const currency = account.currency || 'UNKNOWN';
    if (!groups[currency]) {
      groups[currency] = [];
    }
    groups[currency].push(account);
    return groups;
  }, {});

  // Calculate total balance per currency
  const calculateTotal = (accountList) => {
    return accountList.reduce((sum, account) => sum + (account.balance || 0), 0);
  };

  // Format currency value
  const formatCurrency = (amount, currency) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: currency || 'USD',
      minimumFractionDigits: 2
    }).format(amount);
  };

  // Check if account changed recently
  const hasChanged = (accountId) => {
    return changedAccounts.includes(accountId);
  };

  return (
    <div className="bg-white dark:bg-gray-800 rounded-lg shadow-sm border border-gray-200 dark:border-gray-700 p-4">
      <div className="flex items-center justify-between mb-4">
        <h2 className="text-base font-semibold text-gray-900 dark:text-white">
          Account Balances
        </h2>
        <span className="text-xs text-gray-500 dark:text-gray-400">
          {accounts.length} accounts
        </span>
      </div>

      {Object.keys(groupedAccounts).length === 0 ? (
        <div className="text-center py-6 text-gray-500 dark:text-gray-400 text-sm">
          No accounts available
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-3">
          {Object.keys(groupedAccounts).sort().map(currency => (
            <div
              key={currency}
              className="bg-gradient-to-br from-gray-50 to-gray-100 dark:from-gray-700/50 dark:to-gray-800/50 rounded-lg p-3 border border-gray-200 dark:border-gray-600"
            >
              {/* Currency Header with Total */}
              <div className="mb-2 pb-2 border-b border-gray-300 dark:border-gray-600">
                <div className="flex items-center justify-between">
                  <h3 className="text-sm font-bold text-gray-700 dark:text-gray-200 uppercase">
                    {currency}
                  </h3>
                  <span className="text-xs text-gray-500 dark:text-gray-400">
                    {groupedAccounts[currency].length}
                  </span>
                </div>
                <div className="text-xl font-bold text-gray-900 dark:text-white mt-1">
                  {formatCurrency(calculateTotal(groupedAccounts[currency]), currency)}
                </div>
              </div>

              {/* Account List - Compact */}
              <div className="space-y-1.5">
                {groupedAccounts[currency].map(account => (
                  <div
                    key={account.id}
                    className={`p-2 rounded transition-all duration-500 ${
                      hasChanged(account.id)
                        ? 'bg-green-100 dark:bg-green-900/30 ring-2 ring-green-500 shadow-lg shadow-green-500/50 animate-pulse-green scale-105'
                        : 'bg-white/50 dark:bg-gray-900/30 hover:bg-white dark:hover:bg-gray-900/50'
                    }`}
                  >
                    <div className="flex items-center justify-between">
                      <span className="text-sm font-medium text-gray-700 dark:text-gray-300 truncate">
                        {account.id.replace(`-${currency}`, '')}
                      </span>
                      <span className={`text-sm font-semibold ml-2 ${
                        hasChanged(account.id)
                          ? 'text-green-600 dark:text-green-400'
                          : 'text-gray-900 dark:text-white'
                      }`}>
                        {new Intl.NumberFormat('en-US', {
                          style: 'decimal',
                          minimumFractionDigits: 0,
                          maximumFractionDigits: 0
                        }).format(account.balance)}
                      </span>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default AccountsPanel;
