import React, { useState, useEffect, useCallback } from 'react';
import { accountApi } from '../api/account';
import { useToast } from '../context/ToastContext';
import { Users, Lock, Unlock, AlertOctagon, RefreshCw, Search } from 'lucide-react';

const AdminAccountsPage = () => {
  const [accounts, setAccounts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [searchTerm, setSearchTerm] = useState('');
  const [actionLoadingId, setActionLoadingId] = useState(null);
  const { addToast } = useToast();

  const loadAccounts = useCallback(async () => {
    setLoading(true);
    try {
      const res = await accountApi.getAllAccounts(page, 15);
      setAccounts(res.content || []);
      setTotalPages(res.totalPages || 0);
    } catch (err) {
      console.error('Failed to load admin accounts', err);
      addToast('Failed to load accounts list', 'error');
    } finally {
      setLoading(false);
    }
  }, [page]);

  useEffect(() => {
    loadAccounts();
  }, [loadAccounts]);

  const handleStatusChange = async (accountId, newStatus) => {
    const reason = prompt(`Enter reason for updating account status to ${newStatus}:`, 'Administrative review');
    if (!reason) return;

    setActionLoadingId(accountId);
    try {
      await accountApi.updateAccountStatus(accountId, newStatus, reason);
      addToast(`Account status updated to ${newStatus}`, 'success');
      loadAccounts();
    } catch (err) {
      const msg = err.response?.data?.message || 'Failed to update account status';
      addToast(msg, 'error');
    } finally {
      setActionLoadingId(null);
    }
  };

  const filteredAccounts = accounts.filter((acc) => {
    if (!searchTerm.trim()) return true;
    const term = searchTerm.toLowerCase();
    return (
      acc.accountNumber?.toLowerCase().includes(term) ||
      acc.ownerName?.toLowerCase().includes(term) ||
      acc.ownerEmail?.toLowerCase().includes(term)
    );
  });

  return (
    <div>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '2rem' }}>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
            <div style={{ background: 'rgba(239, 68, 68, 0.15)', color: '#F87171', padding: '0.5rem', borderRadius: '0.625rem' }}>
              <Users size={24} />
            </div>
            <h1 style={{ fontSize: '1.75rem', fontWeight: 800, color: '#FFFFFF', letterSpacing: '-0.02em' }}>
              System Account Management
            </h1>
          </div>
          <p style={{ color: '#94A3B8', fontSize: '0.875rem', marginTop: '0.35rem' }}>
            Administrator control: Inspect and enforce account lifecycle states
          </p>
        </div>

        <button onClick={loadAccounts} className="btn btn-secondary btn-sm">
          <RefreshCw size={15} className={loading ? 'animate-spin' : ''} />
          <span>Refresh</span>
        </button>
      </div>

      {/* Search Bar */}
      <div className="card" style={{ padding: '1rem', marginBottom: '1.5rem' }}>
        <div style={{ position: 'relative', maxWidth: '400px' }}>
          <Search size={16} color="#64748B" style={{ position: 'absolute', left: '0.75rem', top: '50%', transform: 'translateY(-50%)' }} />
          <input
            type="text"
            placeholder="Search by account number, owner, or email..."
            className="form-input"
            style={{ paddingLeft: '2.25rem' }}
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>
      </div>

      {/* Accounts Table */}
      <div className="table-container">
        <table className="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Account Number</th>
              <th>Account Holder</th>
              <th>Email</th>
              <th>Balance</th>
              <th>Status</th>
              <th>Created At</th>
              <th>Admin Action</th>
            </tr>
          </thead>
          <tbody>
            {filteredAccounts.length === 0 ? (
              <tr>
                <td colSpan="8" style={{ textAlign: 'center', padding: '3rem', color: '#64748B' }}>
                  No accounts found.
                </td>
              </tr>
            ) : (
              filteredAccounts.map((acc) => (
                <tr key={acc.id}>
                  <td className="font-mono" style={{ color: '#64748B' }}>#{acc.id}</td>
                  <td className="font-mono" style={{ color: '#38BDF8', fontWeight: 600 }}>{acc.accountNumber}</td>
                  <td style={{ fontWeight: 600, color: '#FFFFFF' }}>{acc.ownerName}</td>
                  <td style={{ color: '#94A3B8', fontSize: '0.8125rem' }}>{acc.ownerEmail}</td>
                  <td className="font-mono" style={{ fontWeight: 700, color: '#34D399' }}>
                    ₹{parseFloat(acc.balance).toLocaleString('en-IN', { minimumFractionDigits: 2 })}
                  </td>
                  <td>
                    <span className={`badge ${
                      acc.status === 'ACTIVE' ? 'badge-active' :
                      acc.status === 'FROZEN' ? 'badge-frozen' : 'badge-closed'
                    }`}>
                      {acc.status}
                    </span>
                  </td>
                  <td style={{ color: '#94A3B8', fontSize: '0.8125rem' }}>
                    {new Date(acc.createdAt).toLocaleDateString()}
                  </td>
                  <td>
                    <div style={{ display: 'flex', gap: '0.4rem' }}>
                      {acc.status === 'ACTIVE' ? (
                        <button
                          onClick={() => handleStatusChange(acc.id, 'FROZEN')}
                          className="btn btn-outline btn-sm"
                          style={{ color: '#FBBF24', borderColor: '#78350F' }}
                          disabled={actionLoadingId === acc.id}
                          title="Freeze account"
                        >
                          <Lock size={13} />
                          <span>Freeze</span>
                        </button>
                      ) : (
                        <button
                          onClick={() => handleStatusChange(acc.id, 'ACTIVE')}
                          className="btn btn-outline btn-sm"
                          style={{ color: '#34D399', borderColor: '#064E3B' }}
                          disabled={actionLoadingId === acc.id}
                          title="Activate account"
                        >
                          <Unlock size={13} />
                          <span>Activate</span>
                        </button>
                      )}

                      {acc.status !== 'CLOSED' && (
                        <button
                          onClick={() => handleStatusChange(acc.id, 'CLOSED')}
                          className="btn btn-outline btn-sm"
                          style={{ color: '#F87171', borderColor: '#7F1D1D' }}
                          disabled={actionLoadingId === acc.id}
                          title="Close account permanently"
                        >
                          <AlertOctagon size={13} />
                          <span>Close</span>
                        </button>
                      )}
                    </div>
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

export default AdminAccountsPage;

