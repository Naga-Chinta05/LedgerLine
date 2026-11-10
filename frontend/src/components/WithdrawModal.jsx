import React, { useState } from 'react';
import { transactionApi } from '../api/transaction';
import { useToast } from '../context/ToastContext';
import { X, ArrowUpRight, ShieldAlert } from 'lucide-react';

const WithdrawModal = ({ isOpen, onClose, account, onSuccess }) => {
  const [amount, setAmount] = useState('');
  const [description, setDescription] = useState('Account balance withdrawal');
  const [loading, setLoading] = useState(false);
  const { addToast } = useToast();

  if (!isOpen || !account) return null;

  const currentBal = parseFloat(account.balance) || 0;

  const handleSubmit = async (e) => {
    e.preventDefault();
    const val = parseFloat(amount);
    if (isNaN(val) || val <= 0) {
      addToast('Please enter a valid withdrawal amount', 'warning');
      return;
    }

    if (val > currentBal) {
      addToast(`Withdrawal amount exceeds available balance of ₹${currentBal.toFixed(2)}`, 'error');
      return;
    }

    setLoading(true);
    try {
      const idempotencyKey = 'wdr-' + Date.now() + '-' + Math.random().toString(36).substring(2, 8);
      await transactionApi.withdraw({
        accountNumber: account.accountNumber,
        amount: val,
        description: description.trim() || 'Account withdrawal',
      }, idempotencyKey);

      addToast(`Successfully withdrew ₹${val.toFixed(2)} from ${account.accountNumber}`, 'success');
      setAmount('');
      onSuccess();
      onClose();
    } catch (err) {
      const msg = err.response?.data?.message || 'Withdrawal failed';
      addToast(msg, 'error');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
            <div style={{ background: 'rgba(239, 68, 68, 0.15)', color: '#F87171', padding: '0.4rem', borderRadius: '0.5rem' }}>
              <ArrowUpRight size={20} />
            </div>
            <h3 style={{ fontSize: '1.125rem', fontWeight: 700 }}>Withdraw Funds</h3>
          </div>
          <button onClick={onClose} className="btn btn-outline btn-sm" style={{ padding: '0.25rem 0.5rem' }}>
            <X size={16} />
          </button>
        </div>

        <form onSubmit={handleSubmit}>
          <div className="modal-body">
            <div style={{ background: 'var(--bg-elevated)', padding: '0.85rem 1rem', borderRadius: '0.625rem', border: '1px solid var(--border-subtle)', marginBottom: '1.25rem' }}>
              <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', fontWeight: 600 }}>SOURCE ACCOUNT</div>
              <div className="font-mono" style={{ fontSize: '1rem', fontWeight: 700, color: 'var(--primary-400)', marginTop: '0.15rem' }}>
                {account.accountNumber}
              </div>
              <div style={{ fontSize: '0.8125rem', color: '#94A3B8', marginTop: '0.2rem' }}>
                Maximum Available: ₹{currentBal.toLocaleString('en-IN', { minimumFractionDigits: 2 })}
              </div>
            </div>

            <div className="form-group">
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <label className="form-label">Withdrawal Amount (₹ INR)</label>
                <button
                  type="button"
                  onClick={() => setAmount(currentBal.toString())}
                  style={{ background: 'none', border: 'none', color: '#38BDF8', fontSize: '0.75rem', fontWeight: 600, cursor: 'pointer' }}
                >
                  Withdraw Max
                </button>
              </div>
              <input
                type="number"
                step="0.01"
                min="1"
                max={currentBal}
                placeholder="0.00"
                className="form-input"
                style={{ fontSize: '1.25rem', fontWeight: 700 }}
                value={amount}
                onChange={(e) => setAmount(e.target.value)}
                required
                autoFocus
              />
            </div>

            <div className="form-group">
              <label className="form-label">Description / Purpose</label>
              <input
                type="text"
                placeholder="e.g. ATM withdrawal, Bill payment"
                className="form-input"
                value={description}
                onChange={(e) => setDescription(e.target.value)}
              />
            </div>

            <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', fontSize: '0.75rem', color: '#94A3B8' }}>
              <ShieldAlert size={14} color="#F59E0B" />
              <span>Pessimistic DB lock guarantees no concurrent overdraft or double spend.</span>
            </div>
          </div>

          <div className="modal-footer">
            <button type="button" onClick={onClose} className="btn btn-secondary" disabled={loading}>
              Cancel
            </button>
            <button type="submit" className="btn btn-danger" disabled={loading || currentBal <= 0}>
              {loading ? 'Processing...' : 'Confirm Withdrawal'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default WithdrawModal;

