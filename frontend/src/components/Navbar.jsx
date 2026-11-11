import React from 'react';
import { useAuth } from '../context/AuthContext';
import ThemeSwitcher from './ThemeSwitcher';
import { Landmark, LogOut } from 'lucide-react';

const Navbar = () => {
  const { user, logout } = useAuth();

  return (
    <header style={{
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'space-between',
      padding: '1rem 1.75rem',
      backgroundColor: 'var(--bg-elevated)',
      borderBottom: '1px solid var(--border-subtle)',
      position: 'sticky',
      top: 0,
      zIndex: 50,
      transition: 'background-color 0.2s ease, border-color 0.2s ease',
    }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
        <div style={{
          background: 'var(--primary-gradient)',
          width: '38px',
          height: '38px',
          borderRadius: '10px',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          boxShadow: '0 4px 14px var(--primary-glow)',
          transition: 'all 0.2s ease',
        }}>
          <Landmark size={22} color="#FFFFFF" />
        </div>
        <div>
          <span style={{ fontSize: '1.25rem', fontWeight: 800, letterSpacing: '-0.02em', color: 'var(--text-primary)' }}>
            Ledger<span style={{ color: 'var(--primary-400)' }}>Line</span>
          </span>
          <span style={{
            display: 'block',
            fontSize: '0.65rem',
            fontWeight: 600,
            color: 'var(--text-muted)',
            letterSpacing: '0.06em',
            textTransform: 'uppercase'
          }}>
            Secure Digital Ledger
          </span>
        </div>
      </div>

      <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
        <ThemeSwitcher />

        {user && (
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.85rem' }}>
            <div style={{ textAlign: 'right' }}>
              <div style={{ fontSize: '0.875rem', fontWeight: 600, color: 'var(--text-primary)' }}>
                {user.fullName}
              </div>
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'flex-end', gap: '0.35rem' }}>
                <span className={`badge ${user.role === 'ADMIN' ? 'badge-danger' :
                    user.role === 'AUDITOR' ? 'badge-warning' : 'badge-active'
                  }`} style={{ fontSize: '0.65rem', padding: '0.15rem 0.5rem' }}>
                  {user.role}
                </span>
              </div>
            </div>

            <button
              onClick={logout}
              className="btn btn-outline btn-sm"
              title="Logout"
              style={{ display: 'flex', alignItems: 'center', gap: '0.35rem' }}
            >
              <LogOut size={15} />
              <span>Sign out</span>
            </button>
          </div>
        )}
      </div>
    </header>
  );
};

export default Navbar;
