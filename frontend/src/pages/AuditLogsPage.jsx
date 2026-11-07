import React, { useState, useEffect, useCallback } from 'react';
import { auditApi } from '../api/audit';
import { ShieldCheck, RefreshCw } from 'lucide-react';

const AuditLogsPage = () => {
  const [logs, setLogs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const fetchLogs = useCallback(async () => {
    setLoading(true);
    try {
      const res = await auditApi.getAuditLogs(page, 20);
      setLogs(res.content || []);
      setTotalPages(res.totalPages || 0);
    } catch (err) {
      console.error('Failed to load audit logs', err);
    } finally {
      setLoading(false);
    }
  }, [page]);

  useEffect(() => {
    fetchLogs();
  }, [fetchLogs]);

  return (
    <div>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '2rem' }}>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
            <div style={{ background: 'rgba(245, 158, 11, 0.15)', color: '#FBBF24', padding: '0.5rem', borderRadius: '0.625rem' }}>
              <ShieldCheck size={24} />
            </div>
            <h1 style={{ fontSize: '1.75rem', fontWeight: 800, color: '#FFFFFF', letterSpacing: '-0.02em' }}>
              Immutable Compliance Audit Trail
            </h1>
          </div>
          <p style={{ color: '#94A3B8', fontSize: '0.875rem', marginTop: '0.35rem' }}>
            Permanent chronological record of every security and financial operation
          </p>
        </div>

        <button onClick={fetchLogs} className="btn btn-secondary btn-sm">
          <RefreshCw size={15} className={loading ? 'animate-spin' : ''} />
          <span>Refresh</span>
        </button>
      </div>

      <div className="table-container">
        <table className="data-table">
          <thead>
            <tr>
              <th>Log ID</th>
              <th>Action</th>
              <th>Actor (Email)</th>
              <th>Target Entity</th>
              <th>Details</th>
              <th>Client IP</th>
              <th>Timestamp</th>
            </tr>
          </thead>
          <tbody>
            {logs.length === 0 ? (
              <tr>
                <td colSpan="7" style={{ textAlign: 'center', padding: '3rem', color: '#64748B' }}>
                  No audit logs recorded yet.
                </td>
              </tr>
            ) : (
              logs.map((log) => (
                <tr key={log.id}>
                  <td className="font-mono" style={{ color: '#64748B' }}>#{log.id}</td>
                  <td>
                    <span className={`badge ${
                      log.action === 'LOGIN' || log.action === 'REGISTER' ? 'badge-active' :
                      log.action === 'DEPOSIT' || log.action === 'TRANSFER' ? 'badge-posted' :
                      log.action.includes('STATUS') ? 'badge-warning' : 'badge-danger'
                    }`}>
                      {log.action}
                    </span>
                  </td>
                  <td>
                    <div style={{ fontWeight: 600, color: '#E2E8F0' }}>
                      {log.actorEmail || 'System'}
                    </div>
                    {log.actorUserId && (
                      <div className="font-mono" style={{ fontSize: '0.75rem', color: '#64748B' }}>
                        User #{log.actorUserId}
                      </div>
                    )}
                  </td>
                  <td>
                    <div style={{ fontSize: '0.8125rem', color: '#CBD5E1' }}>
                      {log.entityType} {log.entityId ? `#${log.entityId}` : ''}
                    </div>
                  </td>
                  <td style={{ fontSize: '0.8125rem', color: '#94A3B8', maxWidth: '300px' }}>
                    {log.details || 'N/A'}
                  </td>
                  <td className="font-mono" style={{ fontSize: '0.75rem', color: '#64748B' }}>
                    {log.ipAddress}
                  </td>
                  <td style={{ color: '#94A3B8', fontSize: '0.8125rem' }}>
                    {new Date(log.timestamp).toLocaleString()}
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {totalPages > 1 && (
        <div style={{ display: 'flex', justifyContent: 'center', gap: '0.5rem', marginTop: '1.5rem' }}>
          <button
            onClick={() => setPage((p) => Math.max(0, p - 1))}
            className="btn btn-secondary btn-sm"
            disabled={page === 0}
          >
            Previous
          </button>
          <span style={{ display: 'flex', alignItems: 'center', padding: '0 0.75rem', fontSize: '0.875rem', color: '#94A3B8' }}>
            Page {page + 1} of {totalPages}
          </span>
          <button
            onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
            className="btn btn-secondary btn-sm"
            disabled={page >= totalPages - 1}
          >
            Next
          </button>
        </div>
      )}
    </div>
  );
};

export default AuditLogsPage;

