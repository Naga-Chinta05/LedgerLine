import api from './axios';

export const accountApi = {
  getMyAccounts: async () => {
    const res = await api.get('/accounts');
    return res.data;
  },

  createAccount: async (accountData) => {
    const res = await api.post('/accounts', accountData);
    return res.data;
  },

  getAccountById: async (id) => {
    const res = await api.get(`/accounts/${id}`);
    return res.data;
  },

  getAccountByNumber: async (accountNumber) => {
    const res = await api.get(`/accounts/number/${encodeURIComponent(accountNumber)}`);
    return res.data;
  },

  updateAccountStatus: async (id, status, reason) => {
    const res = await api.patch(`/accounts/${id}/status`, { status, reason });
    return res.data;
  },

  getAllAccounts: async (page = 0, size = 15) => {
    const res = await api.get(`/admin/accounts?page=${page}&size=${size}`);
    return res.data;
  },
};

