import React, { useState } from 'react';
import { accountApi } from '../api/account';
import { useToast } from '../context/ToastContext';
import { X, PlusCircle } from 'lucide-react';

const NewAccountModal = ({ isOpen, onClose, onSuccess }) => {
  const [currency, setCurrency] = useState('INR');
  const [loading, setLoading] = useState(false);
  const { addToast } = useToast();

  if (!isOpen) return null;

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      const newAcc = await accountApi.createAccount({ currency });
      addToast(`New ${newAcc.currency} checking account ${newAcc.accountNumber} created!`, 'success');
      onSuccess();
      onClose();
    } catch (err) {
      const msg = err.response?.data?.message || 'Failed to create new account';
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
            <div style={{ background: 'var(--primary-glow)', color: 'var(--primary-400)', padding: '0.4rem', borderRadius: '0.5rem' }}>
              <PlusCircle size={20} />
            </div>
            <h3 style={{ fontSize: '1.125rem', fontWeight: 700 }}>Open New Bank Account</h3>
          </div>
          <button onClick={onClose} className="btn btn-outline btn-sm" style={{ padding: '0.25rem 0.5rem' }}>
            <X size={16} />
          </button>
        </div>

        <form onSubmit={handleSubmit}>
          <div className="modal-body">
            <p style={{ fontSize: '0.875rem', color: '#94A3B8', marginBottom: '1.25rem' }}>
              Create an additional dedicated digital banking ledger account under your profile.
            </p>

            <div className="form-group">
              <label className="form-label">Currency</label>
              <select
                className="form-select font-mono"
                value={currency}
                onChange={(e) => setCurrency(e.target.value)}
              >
                <option value="INR">INR - Indian Rupee (₹)</option>
              </select>
            </div>

            <div style={{ background: 'var(--bg-elevated)', padding: '0.85rem 1rem', borderRadius: '0.625rem', border: '1px solid var(--border-subtle)', fontSize: '0.8125rem', color: 'var(--text-muted)' }}>
              Account will be immediately provisioned with status <strong style={{ color: '#34D399' }}>ACTIVE</strong> and initial balance of ₹0.00.
            </div>
          </div>

          <div className="modal-footer">
            <button type="button" onClick={onClose} className="btn btn-secondary" disabled={loading}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? 'Creating...' : 'Open Account'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default NewAccountModal;

