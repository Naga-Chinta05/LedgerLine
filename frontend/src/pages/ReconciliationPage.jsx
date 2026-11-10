import React, { useState, useEffect } from 'react';
import { ledgerApi } from '../api/ledger';
import { useToast } from '../context/ToastContext';
import { Scale, CheckCircle2, AlertTriangle, RefreshCw, ShieldCheck, Database } from 'lucide-react';

const ReconciliationPage = () => {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const { addToast } = useToast();

  const fetchReconciliation = async () => {
    setLoading(true);
    try {
      const res = await ledgerApi.getReconciliation();
      setData(res);
      if (res.balanced) {
        addToast('Zero-sum equilibrium verified across all ledger entries!', 'success');
      } else {
        addToast('Warning: Imbalance detected in ledger system!', 'error');
      }
    } catch (err) {
      console.error('Failed to run reconciliation', err);
      addToast('Failed to execute ledger reconciliation audit', 'error');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchReconciliation();
  }, []);

  return (
    <div style={{ maxWidth: '1000px', margin: '0 auto' }}>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '2rem' }}>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
            <div style={{ background: 'var(--primary-glow)', color: 'var(--primary-400)', padding: '0.5rem', borderRadius: '0.625rem' }}>
              <Scale size={24} />
            </div>
            <h1 style={{ fontSize: '1.75rem', fontWeight: 800, color: 'var(--text-primary)', letterSpacing: '-0.02em' }}>
              Double-Entry Ledger Reconciliation
            </h1>
          </div>
          <p style={{ color: '#94A3B8', fontSize: '0.875rem', marginTop: '0.35rem' }}>
            Automated zero-sum equilibrium audit ensuring Σ Debits = Σ Credits across the entire banking system
          </p>
        </div>

        <button onClick={fetchReconciliation} className="btn btn-primary" disabled={loading}>
          <RefreshCw size={16} className={loading ? 'animate-spin' : ''} />
          <span>{loading ? 'Auditing...' : 'Re-Run Verification'}</span>
        </button>
      </div>

      {data && (
        <>
          {/* Main Reconciliation Card */}
          <div className="card" style={{
            background: data.balanced
              ? 'linear-gradient(145deg, #063124, #0B1914)'
              : 'linear-gradient(145deg, #3B1212, #1C0A0A)',
            border: `1px solid ${data.balanced ? '#059669' : '#DC2626'}`,
            marginBottom: '1.5rem',
            padding: '2rem'
          }}>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1.5rem' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                {data.balanced ? (
                  <CheckCircle2 size={32} color="#34D399" />
                ) : (
                  <AlertTriangle size={32} color="#F87171" />
                )}
                <div>
                  <h2 style={{ fontSize: '1.35rem', fontWeight: 800, color: '#FFFFFF' }}>
                    {data.balanced ? 'Zero-Sum Ledger Equilibrium: Confirmed' : 'Ledger Invariant Violation: Imbalance Detected'}
                  </h2>
                  <div style={{ fontSize: '0.8125rem', color: '#CBD5E1', marginTop: '0.2rem' }}>
                    Audit verified at: {new Date(data.verifiedAt).toLocaleString()}
                  </div>
                </div>
              </div>

              <span className={`badge ${data.balanced ? 'badge-success' : 'badge-danger'}`} style={{ fontSize: '0.8125rem', padding: '0.35rem 0.85rem' }}>
                {data.balanced ? 'EQUILIBRIUM ACTIVE' : 'DISCREPANCY DETECTED'}
              </span>
            </div>

            <div style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
              gap: '1.25rem',
              background: 'rgba(0, 0, 0, 0.35)',
              padding: '1.25rem',
              borderRadius: '0.875rem'
            }}>
              <div>
                <div style={{ fontSize: '0.75rem', color: '#94A3B8', fontWeight: 600 }}>TOTAL SYSTEM DEBITS</div>
                <div className="font-mono" style={{ fontSize: '1.65rem', fontWeight: 800, color: '#F87171', marginTop: '0.2rem' }}>
                  ₹{parseFloat(data.totalDebits).toLocaleString('en-IN', { minimumFractionDigits: 2 })}
                </div>
              </div>

              <div>
                <div style={{ fontSize: '0.75rem', color: '#94A3B8', fontWeight: 600 }}>TOTAL SYSTEM CREDITS</div>
                <div className="font-mono" style={{ fontSize: '1.65rem', fontWeight: 800, color: '#34D399', marginTop: '0.2rem' }}>
                  ₹{parseFloat(data.totalCredits).toLocaleString('en-IN', { minimumFractionDigits: 2 })}
                </div>
              </div>

              <div>
                <div style={{ fontSize: '0.75rem', color: '#94A3B8', fontWeight: 600 }}>NET VARIANCE / DIFFERENCE</div>
                <div className="font-mono" style={{ fontSize: '1.65rem', fontWeight: 800, color: data.balanced ? '#38BDF8' : '#F87171', marginTop: '0.2rem' }}>
                  ₹{parseFloat(data.difference).toLocaleString('en-IN', { minimumFractionDigits: 2 })}
                </div>
              </div>
            </div>
          </div>

          {/* Audit Metrics */}
          <div style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))',
            gap: '1.25rem',
            marginBottom: '2rem'
          }}>
            <div className="card">
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: '#94A3B8', fontSize: '0.8125rem', fontWeight: 600 }}>
                <Database size={16} />
                <span>Audited Financial Transactions</span>
              </div>
              <div style={{ fontSize: '1.75rem', fontWeight: 800, color: '#FFFFFF', marginTop: '0.5rem' }}>
                {data.totalTransactions}
              </div>
              <div style={{ fontSize: '0.75rem', color: '#64748B', marginTop: '0.25rem' }}>
                Atomic transfers, deposits & withdrawals
              </div>
            </div>

            <div className="card">
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: '#94A3B8', fontSize: '0.8125rem', fontWeight: 600 }}>
                <ShieldCheck size={16} />
                <span>Total Double-Entry Bookkeeping Lines</span>
              </div>
              <div style={{ fontSize: '1.75rem', fontWeight: 800, color: '#FFFFFF', marginTop: '0.5rem' }}>
                {data.totalLedgerEntries}
              </div>
              <div style={{ fontSize: '0.75rem', color: '#64748B', marginTop: '0.25rem' }}>
                Exact paired debit/credit posting entries
              </div>
            </div>
          </div>

          {/* Principles explanation */}
          <div className="card" style={{ background: 'var(--bg-elevated)', border: '1px solid var(--border-subtle)' }}>
            <h3 style={{ fontSize: '1.125rem', fontWeight: 700, color: 'var(--text-primary)', marginBottom: '0.75rem' }}>
              How LedgerLine Guarantees Financial Correctness:
            </h3>
            <ul style={{ color: '#94A3B8', fontSize: '0.875rem', lineHeight: 1.8, paddingLeft: '1.25rem' }}>
              <li><strong>Zero-Sum Accounting:</strong> Every single financial movement records at least one DEBIT entry and one CREDIT entry of equal value within the exact same database transaction boundary.</li>
              <li><strong>Strict Concurrency Control:</strong> Accounts are locked using JPA pessimistic write locks (`@Lock(LockModeType.PESSIMISTIC_WRITE)`) in ascending ID order, mathematically preventing race conditions and cyclic wait deadlocks.</li>
              <li><strong>Non-Negative Balance Invariant:</strong> Validated both at the application service level and enforced by a PostgreSQL database check constraint (`CHECK (balance &gt;= 0)`).</li>
              <li><strong>Idempotency Enforcement:</strong> Every request accepts a unique client key to guarantee that network hiccups or multiple clicks never result in duplicate financial postings.</li>
            </ul>
          </div>
        </>
      )}
    </div>
  );
};

export default ReconciliationPage;

