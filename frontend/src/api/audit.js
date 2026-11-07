import api from './axios';

export const auditApi = {
  getAuditLogs: async (page = 0, size = 20) => {
    const res = await api.get(`/audit/logs?page=${page}&size=${size}`);
    return res.data;
  },
};

