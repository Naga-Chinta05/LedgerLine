import React, { useEffect, useState } from 'react';
import { ledgerApi } from '../api/ledger';
import { X, Receipt, CheckCircle, ArrowDownLeft, ArrowUpRight, ArrowRightLeft, Scale } from 'lucide-react';

const TransactionDetailsModal = ({ isOpen, onClose, transaction }) => {
  const [entries, setEntries] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (transaction) {
      if (transaction.ledgerEntries && transaction.ledgerEntries.length > 0) {
        setEntries(transaction.ledgerEntries);
      } else {
        setLoading(true);
        ledgerApi.getTransactionEntries(transaction.id)
          .then((data) => setEntries(data))
          .catch((err) => console.error('Failed to load ledger entries', err))
          .finally(() => setLoading(false));
      }
    }
  }, [transaction]);

  if (!isOpen || !transaction) return null;

  const totalDebits = entries
    .filter((e) => e.entryType === 'DEBIT')
    .reduce((sum, e) => sum + parseFloat(e.amount), 0);

  const totalCredits = entries
    .filter((e) => e.entryType === 'CREDIT')
    .reduce((sum, e) => sum + parseFloat(e.amount), 0);

  const isBalanced = Math.abs(totalDebits - totalCredits) < 0.001 && entries.length > 0;

  const typeIcon =
    transaction.type === 'DEPOSIT' ? <ArrowDownLeft size={20} color="#34D399" /> :
    transaction.type === 'WITHDRAWAL' ? <ArrowUpRight size={20} color="#F87171" /> :
    <ArrowRightLeft size={20} color="#38BDF8" />;

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" style={{ maxWidth: '650px' }} onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
            <div style={{ background: 'var(--bg-surface)', padding: '0.4rem', borderRadius: '0.5rem' }}>
              {typeIcon}
            </div>
            <div>
              <h3 style={{ fontSize: '1.125rem', fontWeight: 700 }}>
                Transaction #{transaction.id}
              </h3>
              <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                {new Date(transaction.createdAt).toLocaleString()}
              </span>
            </div>
          </div>
          <button onClick={onClose} className="btn btn-outline btn-sm" style={{ padding: '0.25rem 0.5rem' }}>
            <X size={16} />
          </button>
        </div>

        <div className="modal-body">
          {/* Summary Box */}
          <div style={{
            display: 'grid',
            gridTemplateColumns: '1fr 1fr',
            gap: '1rem',
            background: 'var(--bg-elevated)',
            padding: '1rem',
            borderRadius: '0.75rem',
            border: '1px solid var(--border-subtle)',
            marginBottom: '1.25rem'
          }}>
            <div>
              <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>TRANSACTION TYPE</div>
              <div style={{ fontWeight: 700, color: 'var(--text-primary)', marginTop: '0.15rem' }}>{transaction.type}</div>
            </div>
            <div>
              <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>AMOUNT</div>
              <div style={{ fontSize: '1.125rem', fontWeight: 800, color: 'var(--primary-400)', marginTop: '0.15rem' }}>
                ₹{parseFloat(transaction.amount).toLocaleString('en-IN', { minimumFractionDigits: 2 })} {transaction.currency}
              </div>
            </div>
            <div>
              <div style={{ fontSize: '0.75rem', color: '#64748B' }}>STATUS</div>
              <div style={{ marginTop: '0.2rem' }}>
                <span className={`badge ${
                  transaction.status === 'POSTED' ? 'badge-posted' :
                  transaction.status === 'PENDING' ? 'badge-pending' : 'badge-danger'
                }`}>
                  {transaction.status}
                </span>
              </div>
            </div>
            <div>
              <div style={{ fontSize: '0.75rem', color: '#64748B' }}>PURPOSE / REMARKS</div>
              <div style={{ fontSize: '0.875rem', color: '#CBD5E1', marginTop: '0.15rem' }}>
                {transaction.description || 'N/A'}
              </div>
            </div>
          </div>

          {/* Double Entry Bookkeeping Table */}
          <div style={{ marginBottom: '1rem' }}>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '0.5rem' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', fontSize: '0.875rem', fontWeight: 700, color: 'var(--text-primary)' }}>
                <Scale size={16} color="var(--primary-400)" />
                <span>Double-Entry Bookkeeping Ledger Lines</span>
              </div>
              {isBalanced && (
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.3rem', fontSize: '0.75rem', color: '#34D399', fontWeight: 700 }}>
                  <CheckCircle size={14} />
                  <span>Balanced & Reconciled (Zero-Sum)</span>
                </div>
              )}
            </div>

            {loading ? (
              <div style={{ textAlign: 'center', padding: '1.5rem', color: 'var(--text-muted)' }}>Loading ledger entries...</div>
            ) : (
              <div className="table-container">
                <table className="data-table">
                  <thead>
                    <tr>
                      <th>Line ID</th>
                      <th>Account / Leg</th>
                      <th>Type</th>
                      <th>Amount</th>
                    </tr>
                  </thead>
                  <tbody>
                    {entries.map((entry) => (
                      <tr key={entry.id}>
                        <td className="font-mono" style={{ color: 'var(--text-muted)' }}>#{entry.id}</td>
                        <td>
                          <div style={{ fontWeight: 600, color: 'var(--text-primary)' }}>{entry.accountLabel}</div>
                          {entry.accountNumber && (
                            <div className="font-mono" style={{ fontSize: '0.75rem', color: 'var(--primary-400)' }}>
                              {entry.accountNumber}
                            </div>
                          )}
                        </td>
                        <td>
                          <span className={`badge ${entry.entryType === 'DEBIT' ? 'badge-debit' : 'badge-credit'}`}>
                            {entry.entryType}
                          </span>
                        </td>
                        <td className="font-mono" style={{
                          fontWeight: 700,
                          color: entry.entryType === 'DEBIT' ? '#F87171' : '#34D399'
                        }}>
                          ₹{parseFloat(entry.amount).toLocaleString('en-IN', { minimumFractionDigits: 2 })}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                  <tfoot>
                    <tr style={{ background: 'var(--bg-elevated)', borderTop: '2px solid var(--border-card)' }}>
                      <td colSpan="2" style={{ fontWeight: 700, color: 'var(--text-secondary)' }}>Sum Verification:</td>
                      <td style={{ fontSize: '0.75rem', fontWeight: 700, color: '#F87171' }}>
                        Debits: ₹{totalDebits.toLocaleString('en-IN', { minimumFractionDigits: 2 })}
                      </td>
                      <td style={{ fontSize: '0.75rem', fontWeight: 700, color: '#34D399' }}>
                        Credits: ₹{totalCredits.toLocaleString('en-IN', { minimumFractionDigits: 2 })}
                      </td>
                    </tr>
                  </tfoot>
                </table>
              </div>
            )}
          </div>
        </div>

        <div className="modal-footer">
          <button type="button" onClick={onClose} className="btn btn-secondary">
            Close
          </button>
        </div>
      </div>
    </div>
  );
};

export default TransactionDetailsModal;

