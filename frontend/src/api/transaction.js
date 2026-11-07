import api from './axios';

export const transactionApi = {
  deposit: async (data, idempotencyKey) => {
    const headers = idempotencyKey ? { 'Idempotency-Key': idempotencyKey } : {};
    const res = await api.post('/transactions/deposit', data, { headers });
    return res.data;
  },

  withdraw: async (data, idempotencyKey) => {
    const headers = idempotencyKey ? { 'Idempotency-Key': idempotencyKey } : {};
    const res = await api.post('/transactions/withdraw', data, { headers });
    return res.data;
  },

  transfer: async (data, idempotencyKey) => {
    const headers = idempotencyKey ? { 'Idempotency-Key': idempotencyKey } : {};
    const res = await api.post('/transactions/transfer', data, { headers });
    return res.data;
  },

  getMyTransactions: async (page = 0, size = 10) => {
    const res = await api.get(`/transactions?page=${page}&size=${size}`);
    return res.data;
  },

  getTransactionById: async (id) => {
    const res = await api.get(`/transactions/${id}`);
    return res.data;
  },

  getAccountTransactions: async (accountId, page = 0, size = 10) => {
    const res = await api.get(`/transactions/account/${accountId}?page=${page}&size=${size}`);
    return res.data;
  },

  getAllTransactions: async (page = 0, size = 15) => {
    const res = await api.get(`/admin/transactions?page=${page}&size=${size}`);
    return res.data;
  },
};

