import React from 'react';
import { NavLink } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  LayoutDashboard,
  ArrowRightLeft,
  Receipt,
  Scale,
  Users,
  ShieldCheck,
} from 'lucide-react';

const Sidebar = () => {
  const { user, isAdmin, isAuditor } = useAuth();

  const navItemStyle = ({ isActive }) => ({
    display: 'flex',
    alignItems: 'center',
    gap: '0.75rem',
    padding: '0.75rem 1rem',
    borderRadius: '0.625rem',
    fontSize: '0.875rem',
    fontWeight: 600,
    textDecoration: 'none',
    transition: 'all 0.15s ease',
    backgroundColor: isActive ? 'var(--bg-surface)' : 'transparent',
    color: isActive ? 'var(--primary-400)' : 'var(--text-secondary)',
    borderLeft: isActive ? '3px solid var(--primary-500)' : '3px solid transparent',
  });

  return (
    <aside style={{
      width: '260px',
      backgroundColor: 'var(--bg-elevated)',
      borderRight: '1px solid var(--border-subtle)',
      display: 'flex',
      flexDirection: 'column',
      padding: '1.25rem 0.85rem',
      gap: '0.5rem',
      minHeight: 'calc(100vh - 65px)',
      transition: 'background-color 0.2s ease, border-color 0.2s ease',
    }}>
      <div style={{ padding: '0 0.5rem 0.5rem', fontSize: '0.75rem', fontWeight: 700, color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
        Banking
      </div>

      <NavLink to="/dashboard" style={navItemStyle}>
        <LayoutDashboard size={18} />
        <span>Dashboard</span>
      </NavLink>

      <NavLink to="/transfers" style={navItemStyle}>
        <ArrowRightLeft size={18} />
        <span>Money Movement</span>
      </NavLink>

      <NavLink to="/transactions" style={navItemStyle}>
        <Receipt size={18} />
        <span>Ledger & History</span>
      </NavLink>

      {isAuditor && (
        <>
          <div style={{ padding: '1.25rem 0.5rem 0.5rem', fontSize: '0.75rem', fontWeight: 700, color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
            Compliance & Audit
          </div>

          <NavLink to="/reconciliation" style={navItemStyle}>
            <Scale size={18} />
            <span>Ledger Reconciliation</span>
          </NavLink>

          <NavLink to="/audit-logs" style={navItemStyle}>
            <ShieldCheck size={18} />
            <span>Audit Trail</span>
          </NavLink>
        </>
      )}

      {isAdmin && (
        <>
          <div style={{ padding: '1.25rem 0.5rem 0.5rem', fontSize: '0.75rem', fontWeight: 700, color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
            Administration
          </div>

          <NavLink to="/admin/accounts" style={navItemStyle}>
            <Users size={18} />
            <span>Manage Accounts</span>
          </NavLink>
        </>
      )}

      <div style={{ marginTop: 'auto', padding: '1rem 0.75rem', backgroundColor: 'var(--bg-card)', borderRadius: '0.75rem', border: '1px solid var(--border-subtle)' }}>
        <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', fontWeight: 600 }}>System Status</div>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', marginTop: '0.35rem' }}>
          <div style={{ width: '8px', height: '8px', borderRadius: '50%', backgroundColor: 'var(--primary-400)', boxShadow: '0 0 8px var(--primary-400)' }} />
          <span style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', fontWeight: 600 }}>Zero-Sum Ledger Active</span>
        </div>
      </div>
    </aside>
  );
};

export default Sidebar;
