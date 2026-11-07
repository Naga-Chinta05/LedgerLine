import React, { useState, useEffect, useCallback } from 'react';
import { transactionApi } from '../api/transaction';
import { accountApi } from '../api/account';
import TransactionDetailsModal from '../components/TransactionDetailsModal';
import {
  Receipt,
  Search,
  Filter,
  ArrowDownLeft,
  ArrowUpRight,
  ArrowRightLeft,
  Eye,
  RefreshCw,
} from 'lucide-react';

const TransactionsPage = () => {
  const [transactions, setTransactions] = useState([]);
  const [accounts, setAccounts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [selectedTx, setSelectedTx] = useState(null);

  // Filters
  const [typeFilter, setTypeFilter] = useState('ALL');
  const [searchTerm, setSearchTerm] = useState('');

  const loadData = useCallback(async () => {
    setLoading(true);
    try {
      const [accs, txRes] = await Promise.all([
        accountApi.getMyAccounts(),
        transactionApi.getMyTransactions(page, 15),
      ]);
      setAccounts(accs);
      setTransactions(txRes.content || []);
      setTotalPages(txRes.totalPages || 0);
    } catch (err) {
      console.error('Failed to load transaction history', err);
    } finally {
      setLoading(false);
    }
  }, [page]);

  useEffect(() => {
    loadData();
  }, [loadData]);

  // Client-side filtering
  const filteredTransactions = transactions.filter((tx) => {
    if (typeFilter !== 'ALL' && tx.type !== typeFilter) return false;
    if (searchTerm.trim()) {
      const term = searchTerm.toLowerCase();
      const matchDesc = tx.description?.toLowerCase().includes(term);
      const matchSrc = tx.sourceAccountNumber?.toLowerCase().includes(term);
      const matchDst = tx.destinationAccountNumber?.toLowerCase().includes(term);
      const matchId = tx.id.toString().includes(term);
      return matchDesc || matchSrc || matchDst || matchId;
    }
    return true;
  });

  return (
    <div>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '2rem' }}>
        <div>
          <h1 style={{ fontSize: '1.75rem', fontWeight: 800, color: '#FFFFFF', letterSpacing: '-0.02em' }}>
            Financial Transactions & Ledger
          </h1>
          <p style={{ color: '#94A3B8', fontSize: '0.875rem', marginTop: '0.25rem' }}>
            Auditable transaction logs backed by balanced double-entry accounting
          </p>
        </div>

        <button onClick={loadData} className="btn btn-secondary btn-sm" title="Refresh">
          <RefreshCw size={15} className={loading ? 'animate-spin' : ''} />
          <span>Refresh</span>
        </button>
      </div>

      {/* Filter Bar */}
      <div className="card" style={{ padding: '1rem', marginBottom: '1.5rem', display: 'flex', flexWrap: 'wrap', gap: '1rem', alignItems: 'center', justifyContent: 'space-between' }}>
        <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap', flex: 1, minWidth: '280px' }}>
          <div style={{ position: 'relative', flex: 1, minWidth: '200px' }}>
            <Search size={16} color="#64748B" style={{ position: 'absolute', left: '0.75rem', top: '50%', transform: 'translateY(-50%)' }} />
            <input
              type="text"
              placeholder="Search by ID, remarks, account number..."
              className="form-input"
              style={{ paddingLeft: '2.25rem', paddingRight: '0.75rem', paddingBottom: '0.5rem', paddingTop: '0.5rem' }}
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </div>

          <select
            className="form-select"
            style={{ width: 'auto', paddingBottom: '0.5rem', paddingTop: '0.5rem' }}
            value={typeFilter}
            onChange={(e) => setTypeFilter(e.target.value)}
          >
            <option value="ALL">All Transaction Types</option>
            <option value="DEPOSIT">Deposits</option>
            <option value="WITHDRAWAL">Withdrawals</option>
            <option value="TRANSFER">Transfers</option>
          </select>
        </div>

        <div style={{ fontSize: '0.8125rem', color: '#94A3B8' }}>
          Showing <strong>{filteredTransactions.length}</strong> records
        </div>
      </div>

      {/* Transactions Table */}
      <div className="table-container">
        <table className="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Type</th>
              <th>Amount</th>
              <th>Source / Recipient</th>
              <th>Description</th>
              <th>Status</th>
              <th>Timestamp</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            {filteredTransactions.length === 0 ? (
              <tr>
                <td colSpan="8" style={{ textAlign: 'center', padding: '3rem', color: '#64748B' }}>
                  No transactions match the selected filters.
                </td>
              </tr>
            ) : (
              filteredTransactions.map((tx) => (
                <tr
                  key={tx.id}
                  onClick={() => setSelectedTx(tx)}
                  style={{ cursor: 'pointer' }}
                >
                  <td className="font-mono" style={{ color: '#64748B' }}>
                    #{tx.id}
                  </td>
                  <td>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', fontWeight: 600 }}>
                      {tx.type === 'DEPOSIT' && <ArrowDownLeft size={16} color="#34D399" />}
                      {tx.type === 'WITHDRAWAL' && <ArrowUpRight size={16} color="#F87171" />}
                      {tx.type === 'TRANSFER' && <ArrowRightLeft size={16} color="#38BDF8" />}
                      <span>{tx.type}</span>
                    </div>
                  </td>
                  <td className="font-mono" style={{
                    fontWeight: 700,
                    color: tx.type === 'DEPOSIT' ? '#34D399' :
                           tx.type === 'WITHDRAWAL' ? '#F87171' : '#E2E8F0'
                  }}>
                    ₹{parseFloat(tx.amount).toLocaleString('en-IN', { minimumFractionDigits: 2 })}
                  </td>
                  <td>
                    {tx.type === 'TRANSFER' ? (
                      <div style={{ fontSize: '0.8125rem' }}>
                        <span className="font-mono" style={{ color: '#94A3B8' }}>{tx.sourceAccountNumber}</span>
                        {' → '}
                        <span className="font-mono" style={{ color: '#38BDF8', fontWeight: 600 }}>{tx.destinationAccountNumber}</span>
                      </div>
                    ) : tx.sourceAccountNumber ? (
                      <span className="font-mono" style={{ fontSize: '0.8125rem' }}>{tx.sourceAccountNumber}</span>
                    ) : (
                      <span className="font-mono" style={{ fontSize: '0.8125rem' }}>{tx.destinationAccountNumber}</span>
                    )}
                  </td>
                  <td style={{ color: '#CBD5E1', fontSize: '0.8125rem', maxWidth: '200px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                    {tx.description || 'N/A'}
                  </td>
                  <td>
                    <span className={`badge ${
                      tx.status === 'POSTED' ? 'badge-posted' :
                      tx.status === 'PENDING' ? 'badge-pending' : 'badge-danger'
                    }`}>
                      {tx.status}
                    </span>
                  </td>
                  <td style={{ color: '#94A3B8', fontSize: '0.8125rem' }}>
                    {new Date(tx.createdAt).toLocaleString()}
                  </td>
                  <td>
                    <button
                      onClick={(e) => {
                        e.stopPropagation();
                        setSelectedTx(tx);
                      }}
                      className="btn btn-outline btn-sm"
                      style={{ padding: '0.25rem 0.5rem' }}
                    >
                      <Eye size={14} />
                      <span>Ledger</span>
                    </button>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {/* Pagination Controls */}
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

      {/* Double-Entry Ledger Details Modal */}
      <TransactionDetailsModal
        isOpen={!!selectedTx}
        onClose={() => setSelectedTx(null)}
        transaction={selectedTx}
      />
    </div>
  );
};

export default TransactionsPage;

