import React, { useState, useEffect } from 'react';
import { accountApi } from '../api/account';
import { transactionApi } from '../api/transaction';
import { useToast } from '../context/ToastContext';
import {
  ArrowRightLeft,
  CheckCircle2,
  AlertCircle,
  ShieldCheck,
  Zap,
} from 'lucide-react';

const TransfersPage = () => {
  const [accounts, setAccounts] = useState([]);
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
    accountApi.getMyAccounts().then((accs) => {
      setAccounts(accs);
      if (accs.length > 0) {
        setSourceAccountNumber(accs[0].accountNumber);
      }
    }).catch((err) => console.error('Failed to load accounts', err));
  }, []);

  // Live lookup of recipient account
  useEffect(() => {
    const trimmed = destinationAccountNumber.trim();
    if (!trimmed || trimmed.length < 5) {
      setRecipientInfo(null);
      setRecipientError('');
      return;
    }

    if (trimmed.toLowerCase() === sourceAccountNumber.toLowerCase()) {
      setRecipientInfo(null);
      setRecipientError('Source and destination accounts must be distinct.');
      return;
    }

    const timer = setTimeout(async () => {
      setLookupLoading(true);
      setRecipientError('');
      try {
        const dest = await accountApi.getAccountByNumber(trimmed);
        if (dest.status !== 'ACTIVE') {
          setRecipientError(`Account status is ${dest.status}. Transfers unavailable.`);
          setRecipientInfo(null);
        } else {
          setRecipientInfo(dest);
        }
      } catch (err) {
        setRecipientInfo(null);
        setRecipientError('Recipient account number not found.');
      } finally {
        setLookupLoading(false);
      }
    }, 400);

    return () => clearTimeout(timer);
  }, [destinationAccountNumber, sourceAccountNumber]);

  const currentSource = accounts.find((a) => a.accountNumber === sourceAccountNumber);
  const currentBal = currentSource ? parseFloat(currentSource.balance) : 0;
  const transferAmt = parseFloat(amount) || 0;
  const remainingBal = currentBal - transferAmt;

  const handleTransfer = async (e) => {
    e.preventDefault();
    if (transferAmt <= 0) {
      addToast('Please enter an amount greater than 0', 'warning');
      return;
    }

    if (transferAmt > currentBal) {
      addToast(`Insufficient funds in source account!`, 'error');
      return;
    }

    if (!recipientInfo) {
      addToast('Please ensure a valid recipient is selected', 'warning');
      return;
    }

    setLoading(true);
    try {
      const idempotencyKey = 'xfer-page-' + Date.now() + '-' + Math.random().toString(36).substring(2, 9);
      await transactionApi.transfer({
        sourceAccountNumber,
        destinationAccountNumber: destinationAccountNumber.trim(),
        amount: transferAmt,
        description: description.trim() || 'Fund Transfer',
      }, idempotencyKey);

      addToast(`Transferred ₹${transferAmt.toFixed(2)} to ${recipientInfo.ownerName} successfully!`, 'success');
      setAmount('');
      setDestinationAccountNumber('');
      setRecipientInfo(null);

      // Refresh accounts
      const refreshed = await accountApi.getMyAccounts();
      setAccounts(refreshed);
    } catch (err) {
      const msg = err.response?.data?.message || 'Transfer failed.';
      addToast(msg, 'error');
    } finally {
      setLoading(false);
    }
  };

  const presetAmounts = [500, 1000, 5000, 10000];

  return (
    <div style={{ maxWidth: '800px', margin: '0 auto' }}>
      <div style={{ marginBottom: '2rem' }}>
        <h1 style={{ fontSize: '1.75rem', fontWeight: 800, color: '#FFFFFF', letterSpacing: '-0.02em' }}>
          Money Movement & Transfers
        </h1>
        <p style={{ color: '#94A3B8', fontSize: '0.875rem', marginTop: '0.25rem' }}>
          Execute atomic, concurrency-controlled double-entry transfers across accounts
        </p>
      </div>

      <div className="card">
        <form onSubmit={handleTransfer}>
          {/* Source Account */}
          <div className="form-group">
            <label className="form-label">Debit From (Source Account)</label>
            <select
              className="form-select font-mono"
              value={sourceAccountNumber}
              onChange={(e) => setSourceAccountNumber(e.target.value)}
              required
            >
              {accounts.map((acc) => (
                <option key={acc.id} value={acc.accountNumber} disabled={acc.status !== 'ACTIVE'}>
                  {acc.accountNumber} &mdash; ₹{parseFloat(acc.balance).toLocaleString('en-IN', { minimumFractionDigits: 2 })} ({acc.status})
                </option>
              ))}
            </select>
          </div>

          {/* Destination Account */}
          <div className="form-group">
            <label className="form-label">Credit To (Destination Account Number)</label>
            <input
              type="text"
              placeholder="e.g. LL-1001001001 or LL-2002002002"
              className="form-input font-mono"
              value={destinationAccountNumber}
              onChange={(e) => setDestinationAccountNumber(e.target.value)}
              required
            />

            {lookupLoading && (
              <div style={{ fontSize: '0.75rem', color: '#94A3B8', marginTop: '0.35rem' }}>
                Verifying destination account in ledger...
              </div>
            )}

            {recipientInfo && (
              <div style={{
                display: 'flex',
                alignItems: 'center',
                gap: '0.5rem',
                fontSize: '0.875rem',
                color: '#34D399',
                marginTop: '0.45rem',
                background: 'rgba(16, 185, 129, 0.12)',
                padding: '0.5rem 0.85rem',
                borderRadius: '0.5rem',
                border: '1px solid rgba(16, 185, 129, 0.25)'
              }}>
                <CheckCircle2 size={18} />
                <span>Recipient Verified: <strong>{recipientInfo.ownerName}</strong> &bull; Currency: {recipientInfo.currency}</span>
              </div>
            )}

            {recipientError && (
              <div style={{
                display: 'flex',
                alignItems: 'center',
                gap: '0.5rem',
                fontSize: '0.875rem',
                color: '#F87171',
                marginTop: '0.45rem',
                background: 'rgba(239, 68, 68, 0.12)',
                padding: '0.5rem 0.85rem',
                borderRadius: '0.5rem',
                border: '1px solid rgba(239, 68, 68, 0.25)'
              }}>
                <AlertCircle size={18} />
                <span>{recipientError}</span>
              </div>
            )}
          </div>

          {/* Amount */}
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

          {/* Remarks */}
          <div className="form-group">
            <label className="form-label">Description / Purpose</label>
            <input
              type="text"
              placeholder="e.g. Services payment, Rent, Split bill"
              className="form-input"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
            />
          </div>

          {/* Live Simulator Panel */}
          {transferAmt > 0 && (
            <div style={{
              background: '#0B0F19',
              padding: '1rem',
              borderRadius: '0.75rem',
              border: '1px solid #1E293B',
              marginBottom: '1.5rem',
              fontSize: '0.875rem'
            }}>
              <div style={{ fontWeight: 700, color: '#E2E8F0', marginBottom: '0.5rem', display: 'flex', alignItems: 'center', gap: '0.35rem' }}>
                <Zap size={16} color="#38BDF8" />
                <span>Double-Entry Execution Preview</span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', color: '#94A3B8', marginBottom: '0.35rem' }}>
                <span>Source Account Debit:</span>
                <span className="font-mono" style={{ color: '#F87171' }}>- ₹{transferAmt.toLocaleString('en-IN', { minimumFractionDigits: 2 })}</span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', color: '#94A3B8', marginBottom: '0.35rem' }}>
                <span>Destination Account Credit:</span>
                <span className="font-mono" style={{ color: '#34D399' }}>+ ₹{transferAmt.toLocaleString('en-IN', { minimumFractionDigits: 2 })}</span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', borderTop: '1px solid #1E293B', paddingTop: '0.5rem', fontWeight: 700 }}>
                <span>Resulting Available Balance:</span>
                <span className="font-mono" style={{ color: remainingBal < 0 ? '#EF4444' : '#34D399' }}>
                  ₹{remainingBal.toLocaleString('en-IN', { minimumFractionDigits: 2 })}
                </span>
              </div>
            </div>
          )}

          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', fontSize: '0.75rem', color: '#64748B' }}>
              <ShieldCheck size={16} color="#10B981" />
              <span>Protected by idempotency keys and ascending-order pessimistic locks.</span>
            </div>

            <button
              type="submit"
              className="btn btn-primary"
              disabled={loading || !recipientInfo || transferAmt <= 0 || remainingBal < 0}
              style={{ minWidth: '180px' }}
            >
              <ArrowRightLeft size={16} />
              <span>{loading ? 'Processing...' : 'Authorize Transfer'}</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default TransfersPage;

