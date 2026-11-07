import React, { useState, useEffect, useCallback } from 'react';
import { useAuth } from '../context/AuthContext';
import { accountApi } from '../api/account';
import { transactionApi } from '../api/transaction';
import AccountCard from '../components/AccountCard';
import DepositModal from '../components/DepositModal';
import WithdrawModal from '../components/WithdrawModal';
import TransferModal from '../components/TransferModal';
import NewAccountModal from '../components/NewAccountModal';
import TransactionDetailsModal from '../components/TransactionDetailsModal';
import {
  Wallet,
  ArrowRightLeft,
  Plus,
  RefreshCw,
  Clock,
  ArrowDownLeft,
  ArrowUpRight,
  Eye,
  CheckCircle2,
} from 'lucide-react';

const DashboardPage = () => {
  const { user } = useAuth();
  const [accounts, setAccounts] = useState([]);
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(true);

  // Modal states
  const [selectedAccount, setSelectedAccount] = useState(null);
  const [isDepositOpen, setIsDepositOpen] = useState(false);
  const [isWithdrawOpen, setIsWithdrawOpen] = useState(false);
  const [isTransferOpen, setIsTransferOpen] = useState(false);
  const [isNewAccountOpen, setIsNewAccountOpen] = useState(false);
  const [selectedTx, setSelectedTx] = useState(null);

  const loadData = useCallback(async () => {
    setLoading(true);
    try {
      const [accs, txs] = await Promise.all([
        accountApi.getMyAccounts(),
        transactionApi.getMyTransactions(0, 7),
      ]);
      setAccounts(accs);
      setTransactions(txs.content || []);
    } catch (err) {
      console.error('Failed to load dashboard data', err);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadData();
  }, [loadData]);

  const totalBalance = accounts.reduce((sum, a) => sum + (parseFloat(a.balance) || 0), 0);
  const activeAccountsCount = accounts.filter((a) => a.status === 'ACTIVE').length;

  const handleDepositClick = (acc) => {
    setSelectedAccount(acc);
    setIsDepositOpen(true);
  };

  const handleWithdrawClick = (acc) => {
    setSelectedAccount(acc);
    setIsWithdrawOpen(true);
  };

  const handleTransferClick = (acc) => {
    setSelectedAccount(acc || accounts[0]);
    setIsTransferOpen(true);
  };

  return (
    <div>
      {/* Header with Greeting & Refresh */}
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '2rem' }}>
        <div>
          <h1 style={{ fontSize: '1.75rem', fontWeight: 800, color: '#FFFFFF', letterSpacing: '-0.02em' }}>
            Welcome back, {user?.fullName} 👋
          </h1>
          <p style={{ color: '#94A3B8', fontSize: '0.875rem', marginTop: '0.25rem' }}>
            Role: <strong style={{ color: '#38BDF8' }}>{user?.role}</strong> &bull; Double-entry financial overview
          </p>
        </div>

        <div style={{ display: 'flex', gap: '0.75rem' }}>
          <button onClick={loadData} className="btn btn-secondary btn-sm" title="Refresh data">
            <RefreshCw size={15} className={loading ? 'animate-spin' : ''} />
            <span>Refresh</span>
          </button>
          <button onClick={() => setIsNewAccountOpen(true)} className="btn btn-primary btn-sm">
            <Plus size={16} />
            <span>New Account</span>
          </button>
        </div>
      </div>

      {/* Stats Summary Cards */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))',
        gap: '1.25rem',
        marginBottom: '2rem'
      }}>
        {/* Total Net Worth */}
        <div className="card" style={{ background: 'linear-gradient(145deg, #131E33, #0D1524)' }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '0.5rem' }}>
            <span style={{ fontSize: '0.8125rem', color: '#94A3B8', fontWeight: 600 }}>Total Available Liquidity</span>
            <div style={{ background: 'rgba(59, 130, 246, 0.15)', color: '#60A5FA', padding: '0.4rem', borderRadius: '0.5rem' }}>
              <Wallet size={18} />
            </div>
          </div>
          <div style={{ fontSize: '1.875rem', fontWeight: 800, color: '#FFFFFF', letterSpacing: '-0.02em' }}>
            ₹{totalBalance.toLocaleString('en-IN', { minimumFractionDigits: 2 })}
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.35rem', fontSize: '0.75rem', color: '#34D399', marginTop: '0.5rem' }}>
            <CheckCircle2 size={13} />
            <span>Balanced across {accounts.length} bank accounts</span>
          </div>
        </div>

        {/* Active Accounts */}
        <div className="card">
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '0.5rem' }}>
            <span style={{ fontSize: '0.8125rem', color: '#94A3B8', fontWeight: 600 }}>Active Accounts</span>
            <div style={{ background: 'rgba(16, 185, 129, 0.15)', color: '#34D399', padding: '0.4rem', borderRadius: '0.5rem' }}>
              <CheckCircle2 size={18} />
            </div>
          </div>
          <div style={{ fontSize: '1.875rem', fontWeight: 800, color: '#FFFFFF', letterSpacing: '-0.02em' }}>
            {activeAccountsCount} <span style={{ fontSize: '1rem', color: '#64748B', fontWeight: 500 }}>/ {accounts.length} Total</span>
          </div>
          <div style={{ fontSize: '0.75rem', color: '#94A3B8', marginTop: '0.5rem' }}>
            Non-negative balance constraint active
          </div>
        </div>

        {/* Quick Money Movement Action Card */}
        <div className="card" style={{ display: 'flex', flexDirection: 'column', justifyContent: 'center' }}>
          <div style={{ fontSize: '0.8125rem', color: '#94A3B8', fontWeight: 600, marginBottom: '0.5rem' }}>
            Fast Action
          </div>
          <button
            onClick={() => handleTransferClick(accounts[0])}
            className="btn btn-primary"
            style={{ width: '100%' }}
            disabled={accounts.length === 0}
          >
            <ArrowRightLeft size={16} />
            <span>Transfer Money Instantly</span>
          </button>
        </div>
      </div>

      {/* Bank Accounts Section */}
      <div style={{ marginBottom: '2.5rem' }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1rem' }}>
          <h2 style={{ fontSize: '1.25rem', fontWeight: 700, color: '#FFFFFF' }}>
            Your Banking Accounts
          </h2>
          <span style={{ fontSize: '0.8125rem', color: '#94A3B8' }}>
            Pessimistic write locked for financial consistency
          </span>
        </div>

        {accounts.length === 0 && !loading ? (
          <div className="card" style={{ textAlign: 'center', padding: '3rem 1.5rem' }}>
            <p style={{ color: '#94A3B8', marginBottom: '1rem' }}>No bank accounts provisioned yet.</p>
            <button onClick={() => setIsNewAccountOpen(true)} className="btn btn-primary">
              <Plus size={16} />
              <span>Create Your First Account</span>
            </button>
          </div>
        ) : (
          <div style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fill, minmax(320px, 1fr))',
            gap: '1.25rem'
          }}>
            {accounts.map((acc) => (
              <AccountCard
                key={acc.id}
                account={acc}
                onDeposit={handleDepositClick}
                onWithdraw={handleWithdrawClick}
                onTransfer={handleTransferClick}
              />
            ))}
          </div>
        )}
      </div>

      {/* Recent Transactions Section */}
      <div>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1rem' }}>
          <div>
            <h2 style={{ fontSize: '1.25rem', fontWeight: 700, color: '#FFFFFF' }}>
              Recent Transactions
            </h2>
            <p style={{ color: '#94A3B8', fontSize: '0.8125rem', marginTop: '0.15rem' }}>
              Click any transaction row to inspect its double-entry ledger lines
            </p>
          </div>
        </div>

        <div className="table-container">
          <table className="data-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Type</th>
                <th>Amount</th>
                <th>Source / Destination</th>
                <th>Status</th>
                <th>Date & Time</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {transactions.length === 0 ? (
                <tr>
                  <td colSpan="7" style={{ textAlign: 'center', padding: '2rem', color: '#64748B' }}>
                    No financial transactions recorded yet.
                  </td>
                </tr>
              ) : (
                transactions.map((tx) => (
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
      </div>

      {/* Modals */}
      <DepositModal
        isOpen={isDepositOpen}
        onClose={() => setIsDepositOpen(false)}
        account={selectedAccount}
        onSuccess={loadData}
      />

      <WithdrawModal
        isOpen={isWithdrawOpen}
        onClose={() => setIsWithdrawOpen(false)}
        account={selectedAccount}
        onSuccess={loadData}
      />

      <TransferModal
        isOpen={isTransferOpen}
        onClose={() => setIsTransferOpen(false)}
        accounts={accounts}
        initialSourceAccount={selectedAccount}
        onSuccess={loadData}
      />

      <NewAccountModal
        isOpen={isNewAccountOpen}
        onClose={() => setIsNewAccountOpen(false)}
        onSuccess={loadData}
      />

      <TransactionDetailsModal
        isOpen={!!selectedTx}
        onClose={() => setSelectedTx(null)}
        transaction={selectedTx}
      />
    </div>
  );
};

export default DashboardPage;

