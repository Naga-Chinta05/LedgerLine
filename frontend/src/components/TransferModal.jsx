import React, { useState, useEffect } from 'react';
import { transactionApi } from '../api/transaction';
import { accountApi } from '../api/account';
import { useToast } from '../context/ToastContext';
import { X, ArrowRightLeft, CheckCircle2, AlertCircle, ShieldCheck } from 'lucide-react';

const TransferModal = ({ isOpen, onClose, accounts = [], initialSourceAccount = null, onSuccess }) => {
  const [sourceAccountNumber, setSourceAccountNumber] = useState('');
  const [destinationAccountNumber, setDestinationAccountNumber] = useState('');
  const [amount, setAmount] = useState('');
  const [description, setDescription] = useState('Payment transfer');
  const [recipientInfo, setRecipientInfo] = useState(null);
  const [recipientError, setRecipientError] = useState('');
  const [loading, setLoading] = useState(false);
  const [lookupLoading, setLookupLoading] = useState(false);
  const { addToast } = useToast();

  useEffect(() => {
    if (initialSourceAccount) {
      setSourceAccountNumber(initialSourceAccount.accountNumber);
    } else if (accounts.length > 0) {
      setSourceAccountNumber(accounts[0].accountNumber);
    }
  }, [initialSourceAccount, accounts, isOpen]);

  // Recipient live lookup
  useEffect(() => {
    const trimmed = destinationAccountNumber.trim();
    if (!trimmed || trimmed.length < 5) {
      setRecipientInfo(null);
      setRecipientError('');
      return;
    }

    if (trimmed.toLowerCase() === sourceAccountNumber.toLowerCase()) {
      setRecipientInfo(null);
      setRecipientError('Cannot transfer funds to the same account');
      return;
    }

    const timer = setTimeout(async () => {
      setLookupLoading(true);
      setRecipientError('');
      try {
        const dest = await accountApi.getAccountByNumber(trimmed);
        if (dest.status !== 'ACTIVE') {
          setRecipientError(`Account is ${dest.status}. Transfers not allowed.`);
          setRecipientInfo(null);
        } else {
          setRecipientInfo(dest);
        }
      } catch (err) {
        setRecipientInfo(null);
        setRecipientError('Account number not found in LedgerLine registry');
      } finally {
        setLookupLoading(false);
      }
    }, 450);

    return () => clearTimeout(timer);
  }, [destinationAccountNumber, sourceAccountNumber]);

  if (!isOpen) return null;

  const currentSource = accounts.find((a) => a.accountNumber === sourceAccountNumber) || initialSourceAccount;
  const currentBal = currentSource ? parseFloat(currentSource.balance) : 0;
  const transferAmt = parseFloat(amount) || 0;
  const remainingBal = currentBal - transferAmt;

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (transferAmt <= 0) {
      addToast('Please enter a valid transfer amount', 'warning');
      return;
    }

    if (transferAmt > currentBal) {
      addToast(`Insufficient funds! Available balance: ₹${currentBal.toFixed(2)}`, 'error');
      return;
    }

    if (!recipientInfo) {
      addToast('Please verify a valid destination account', 'warning');
      return;
    }

    setLoading(true);
    try {
      const idempotencyKey = 'xfer-' + Date.now() + '-' + Math.random().toString(36).substring(2, 9);
      await transactionApi.transfer({
        sourceAccountNumber,
        destinationAccountNumber: destinationAccountNumber.trim(),
        amount: transferAmt,
        description: description.trim() || 'Fund Transfer',
      }, idempotencyKey);

      addToast(`Successfully transferred ₹${transferAmt.toFixed(2)} to ${recipientInfo.ownerName}`, 'success');
      setAmount('');
      setDestinationAccountNumber('');
      setRecipientInfo(null);
      onSuccess();
      onClose();
    } catch (err) {
      const msg = err.response?.data?.message || 'Transfer failed';
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
            <div style={{ background: 'rgba(56, 189, 248, 0.15)', color: '#38BDF8', padding: '0.4rem', borderRadius: '0.5rem' }}>
              <ArrowRightLeft size={20} />
            </div>
            <h3 style={{ fontSize: '1.125rem', fontWeight: 700 }}>Transfer Money</h3>
          </div>
          <button onClick={onClose} className="btn btn-outline btn-sm" style={{ padding: '0.25rem 0.5rem' }}>
            <X size={16} />
          </button>
        </div>

        <form onSubmit={handleSubmit}>
          <div className="modal-body">
            {/* Source Account */}
            <div className="form-group">
              <label className="form-label">Pay From (Source Account)</label>
              <select
                className="form-select font-mono"
                value={sourceAccountNumber}
                onChange={(e) => setSourceAccountNumber(e.target.value)}
                required
              >
                {accounts.map((acc) => (
                  <option key={acc.id} value={acc.accountNumber} disabled={acc.status !== 'ACTIVE'}>
                    {acc.accountNumber} - ₹{parseFloat(acc.balance).toLocaleString('en-IN', { minimumFractionDigits: 2 })} ({acc.status})
                  </option>
                ))}
              </select>
            </div>

            {/* Destination Account */}
            <div className="form-group">
              <label className="form-label">Recipient Account Number</label>
              <input
                type="text"
                placeholder="e.g. LL-2002002002"
                className="form-input font-mono"
                value={destinationAccountNumber}
                onChange={(e) => setDestinationAccountNumber(e.target.value)}
                required
              />

              {lookupLoading && (
                <div style={{ fontSize: '0.75rem', color: '#94A3B8', marginTop: '0.25rem' }}>
                  Looking up recipient...
                </div>
              )}

              {recipientInfo && (
                <div style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: '0.4rem',
                  fontSize: '0.8125rem',
                  color: '#34D399',
                  marginTop: '0.35rem',
                  background: 'rgba(16, 185, 129, 0.1)',
                  padding: '0.4rem 0.75rem',
                  borderRadius: '0.5rem',
                  border: '1px solid rgba(16, 185, 129, 0.2)'
                }}>
                  <CheckCircle2 size={16} />
                  <span>Verified: <strong>{recipientInfo.ownerName}</strong> ({recipientInfo.currency})</span>
                </div>
              )}

              {recipientError && (
                <div style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: '0.4rem',
                  fontSize: '0.8125rem',
                  color: '#F87171',
                  marginTop: '0.35rem',
                  background: 'rgba(239, 68, 68, 0.1)',
                  padding: '0.4rem 0.75rem',
                  borderRadius: '0.5rem',
                  border: '1px solid rgba(239, 68, 68, 0.2)'
                }}>
                  <AlertCircle size={16} />
                  <span>{recipientError}</span>
                </div>
              )}
            </div>

            {/* Transfer Amount */}
            <div className="form-group">
              <label className="form-label">Transfer Amount (₹ INR)</label>
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
              />
            </div>

            {/* Description */}
            <div className="form-group">
              <label className="form-label">Remarks / Description</label>
              <input
                type="text"
                placeholder="e.g. Invoice settlement, Dinner share"
                className="form-input"
                value={description}
                onChange={(e) => setDescription(e.target.value)}
              />
            </div>

            {/* Live Calculation Preview */}
            {transferAmt > 0 && (
              <div style={{
                background: '#0B0F19',
                padding: '0.85rem 1rem',
                borderRadius: '0.625rem',
                border: '1px solid #1E293B',
                marginBottom: '1rem',
                fontSize: '0.8125rem'
              }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', color: '#94A3B8', marginBottom: '0.25rem' }}>
                  <span>Source Balance:</span>
                  <span>₹{currentBal.toLocaleString('en-IN', { minimumFractionDigits: 2 })}</span>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between', color: '#F87171', marginBottom: '0.25rem' }}>
                  <span>Deduction:</span>
                  <span>- ₹{transferAmt.toLocaleString('en-IN', { minimumFractionDigits: 2 })}</span>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between', fontWeight: 700, color: remainingBal < 0 ? '#EF4444' : '#34D399', borderTop: '1px solid #1E293B', paddingTop: '0.35rem' }}>
                  <span>Balance After Transfer:</span>
                  <span>₹{remainingBal.toLocaleString('en-IN', { minimumFractionDigits: 2 })}</span>
                </div>
              </div>
            )}

            <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', fontSize: '0.75rem', color: '#64748B' }}>
              <ShieldCheck size={14} color="#38BDF8" />
              <span>Deadlock-free ascending ID locking & balanced double-entry guarantee.</span>
            </div>
          </div>

          <div className="modal-footer">
            <button type="button" onClick={onClose} className="btn btn-secondary" disabled={loading}>
              Cancel
            </button>
            <button
              type="submit"
              className="btn btn-primary"
              disabled={loading || !recipientInfo || transferAmt <= 0 || remainingBal < 0}
            >
              {loading ? 'Executing Transfer...' : 'Send Money'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default TransferModal;

