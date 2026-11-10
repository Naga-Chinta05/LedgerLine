import React, { useState } from 'react';
import { transactionApi } from '../api/transaction';
import { useToast } from '../context/ToastContext';
import { X, ArrowDownLeft, ShieldCheck } from 'lucide-react';

const DepositModal = ({ isOpen, onClose, account, onSuccess }) => {
  const [amount, setAmount] = useState('');
  const [description, setDescription] = useState('Account balance deposit');
  const [loading, setLoading] = useState(false);
  const { addToast } = useToast();

  if (!isOpen || !account) return null;

  const handleSubmit = async (e) => {
    e.preventDefault();
    const val = parseFloat(amount);
    if (isNaN(val) || val <= 0) {
      addToast('Please enter a valid deposit amount', 'warning');
      return;
    }

    setLoading(true);
    try {
      const idempotencyKey = 'dep-' + Date.now() + '-' + Math.random().toString(36).substring(2, 8);
      await transactionApi.deposit({
        accountNumber: account.accountNumber,
        amount: val,
        description: description.trim() || 'Account deposit',
      }, idempotencyKey);

      addToast(`Successfully deposited ₹${val.toFixed(2)} into ${account.accountNumber}`, 'success');
      setAmount('');
      onSuccess();
      onClose();
    } catch (err) {
      const msg = err.response?.data?.message || 'Deposit failed';
      addToast(msg, 'error');
    } finally {
      setLoading(false);
    }
  };

  const presetAmounts = [500, 1000, 5000, 10000];

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
            <div style={{ background: 'rgba(16, 185, 129, 0.15)', color: '#34D399', padding: '0.4rem', borderRadius: '0.5rem' }}>
              <ArrowDownLeft size={20} />
            </div>
            <h3 style={{ fontSize: '1.125rem', fontWeight: 700 }}>Deposit Funds</h3>
          </div>
          <button onClick={onClose} className="btn btn-outline btn-sm" style={{ padding: '0.25rem 0.5rem' }}>
            <X size={16} />
          </button>
        </div>

        <form onSubmit={handleSubmit}>
          <div className="modal-body">
            <div style={{ background: 'var(--bg-elevated)', padding: '0.85rem 1rem', borderRadius: '0.625rem', border: '1px solid var(--border-subtle)', marginBottom: '1.25rem' }}>
              <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', fontWeight: 600 }}>TARGET ACCOUNT</div>
              <div className="font-mono" style={{ fontSize: '1rem', fontWeight: 700, color: 'var(--primary-400)', marginTop: '0.15rem' }}>
                {account.accountNumber}
              </div>
              <div style={{ fontSize: '0.8125rem', color: '#94A3B8', marginTop: '0.2rem' }}>
                Available Balance: ₹{parseFloat(account.balance).toLocaleString('en-IN', { minimumFractionDigits: 2 })}
              </div>
            </div>

            <div className="form-group">
              <label className="form-label">Deposit Amount (₹ INR)</label>
              <input
                type="number"
                step="0.01"
                min="1"
                placeholder="0.00"
                className="form-input"
                style={{ fontSize: '1.25rem', fontWeight: 700 }}
                value={amount}
                onChange={(e) => setAmount(e.target.value)}
                required
                autoFocus
              />
            </div>

            <div style={{ display: 'flex', gap: '0.5rem', marginBottom: '1.25rem' }}>
              {presetAmounts.map((preset) => (
                <button
                  type="button"
                  key={preset}
                  onClick={() => setAmount(preset.toString())}
                  className="btn btn-secondary btn-sm"
                  style={{ flex: 1 }}
                >
                  +₹{preset.toLocaleString()}
                </button>
              ))}
            </div>

            <div className="form-group">
              <label className="form-label">Description / Purpose</label>
              <input
                type="text"
                placeholder="e.g. Salary deposit, Savings"
                className="form-input"
                value={description}
                onChange={(e) => setDescription(e.target.value)}
              />
            </div>

            <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', fontSize: '0.75rem', color: '#64748B' }}>
              <ShieldCheck size={14} color="#10B981" />
              <span>Protected by balanced double-entry ledger & idempotency key.</span>
            </div>
          </div>

          <div className="modal-footer">
            <button type="button" onClick={onClose} className="btn btn-secondary" disabled={loading}>
              Cancel
            </button>
            <button type="submit" className="btn btn-success" disabled={loading}>
              {loading ? 'Processing...' : 'Confirm Deposit'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default DepositModal;

