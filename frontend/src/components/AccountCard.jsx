import React, { useState } from 'react';
import { Copy, Check, ArrowDownLeft, ArrowUpRight, CreditCard } from 'lucide-react';

const AccountCard = ({ account, onDeposit, onWithdraw, onTransfer }) => {
  const [copied, setCopied] = useState(false);

  const copyAccountNumber = () => {
    navigator.clipboard.writeText(account.accountNumber);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const formattedBalance = new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: account.currency || 'INR',
    minimumFractionDigits: 2,
  }).format(account.balance);

  const statusClass =
    account.status === 'ACTIVE' ? 'badge-active' :
    account.status === 'FROZEN' ? 'badge-frozen' : 'badge-closed';

  return (
    <div className="card" style={{
      display: 'flex',
      flexDirection: 'column',
      justifyContent: 'space-between',
      position: 'relative',
      overflow: 'hidden',
      background: 'var(--card-gradient)',
      border: '1px solid var(--border-subtle)',
    }}>
      {/* Decorative gradient orb */}
      <div style={{
        position: 'absolute',
        top: '-40px',
        right: '-40px',
        width: '120px',
        height: '120px',
        background: 'radial-gradient(circle, var(--primary-glow) 0%, transparent 70%)',
        borderRadius: '50%',
        pointerEvents: 'none'
      }} />

      <div>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <div style={{
              background: 'var(--primary-glow)',
              padding: '0.4rem',
              borderRadius: '0.5rem',
              color: 'var(--primary-400)'
            }}>
              <CreditCard size={18} />
            </div>
            <span style={{ fontSize: '0.8125rem', color: 'var(--text-secondary)', fontWeight: 600 }}>
              Checking Account
            </span>
          </div>

          <span className={`badge ${statusClass}`}>
            {account.status}
          </span>
        </div>

        <div style={{ marginBottom: '1.25rem' }}>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', fontWeight: 600, textTransform: 'uppercase', letterSpacing: '0.05em' }}>
            Current Balance
          </div>
          <div style={{ fontSize: '1.875rem', fontWeight: 800, color: 'var(--text-primary)', letterSpacing: '-0.02em', marginTop: '0.2rem' }}>
            {formattedBalance}
          </div>
        </div>

        <div style={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          background: 'var(--bg-elevated)',
          padding: '0.5rem 0.75rem',
          borderRadius: '0.5rem',
          border: '1px solid var(--border-subtle)',
          marginBottom: '1.25rem'
        }}>
          <div>
            <div style={{ fontSize: '0.65rem', color: 'var(--text-muted)', fontWeight: 600 }}>ACCOUNT NUMBER</div>
            <div className="font-mono" style={{ fontSize: '0.875rem', color: 'var(--text-primary)', fontWeight: 600, letterSpacing: '0.05em' }}>
              {account.accountNumber}
            </div>
          </div>
          <button
            onClick={copyAccountNumber}
            className="btn btn-outline btn-sm"
            style={{ padding: '0.3rem 0.5rem' }}
            title="Copy account number"
          >
            {copied ? <Check size={14} color="var(--primary-400)" /> : <Copy size={14} />}
          </button>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.5rem', paddingTop: '0.5rem' }}>
        <button
          onClick={() => onDeposit(account)}
          className="btn btn-success btn-sm"
          disabled={account.status !== 'ACTIVE'}
          style={{ width: '100%' }}
        >
          <ArrowDownLeft size={15} />
          <span>Deposit</span>
        </button>

        <button
          onClick={() => onWithdraw(account)}
          className="btn btn-secondary btn-sm"
          disabled={account.status !== 'ACTIVE' || account.balance <= 0}
          style={{ width: '100%' }}
        >
          <ArrowUpRight size={15} />
          <span>Withdraw</span>
        </button>
      </div>
    </div>
  );
};

export default AccountCard;
