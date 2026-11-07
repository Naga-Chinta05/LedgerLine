import api from './axios';

export const ledgerApi = {
  getTransactionEntries: async (transactionId) => {
    const res = await api.get(`/ledger/transaction/${transactionId}`);
    return res.data;
  },

  getAccountEntries: async (accountId, page = 0, size = 15) => {
    const res = await api.get(`/ledger/account/${accountId}?page=${page}&size=${size}`);
    return res.data;
  },

  getReconciliation: async () => {
    const res = await api.get('/ledger/reconciliation');
    return res.data;
  },
};

