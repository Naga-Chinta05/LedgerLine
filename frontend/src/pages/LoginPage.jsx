import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import ThemeSwitcher from '../components/ThemeSwitcher';
import { Landmark, ArrowRight, ShieldCheck, UserCheck, ShieldAlert } from 'lucide-react';

const LoginPage = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const { login, demoLogin } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      await login(email, password);
      navigate('/dashboard');
    } catch (err) {
      // toast shown in context
    } finally {
      setLoading(false);
    }
  };

  const handleDemo = async (role) => {
    setLoading(true);
    try {
      await demoLogin(role);
      navigate('/dashboard');
    } catch (err) {
      // toast shown
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{
      minHeight: '100vh',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      padding: '1.5rem',
      background: 'var(--hero-gradient)',
      position: 'relative',
      transition: 'background 0.3s ease',
    }}>
      {/* Theme Switcher in top right */}
      <div style={{ position: 'absolute', top: '1.25rem', right: '1.5rem' }}>
        <ThemeSwitcher />
      </div>

      <div style={{ maxWidth: '440px', width: '100%' }}>
        {/* Brand header */}
        <div style={{ textAlign: 'center', marginBottom: '2rem' }}>
          <div style={{
            display: 'inline-flex',
            alignItems: 'center',
            justifyContent: 'center',
            background: 'var(--primary-gradient)',
            width: '54px',
            height: '54px',
            borderRadius: '16px',
            boxShadow: '0 8px 24px var(--primary-glow)',
            marginBottom: '1rem',
            transition: 'all 0.2s ease',
          }}>
            <Landmark size={30} color="#FFFFFF" />
          </div>
          <h1 style={{ fontSize: '1.75rem', fontWeight: 800, color: 'var(--text-primary)', letterSpacing: '-0.02em' }}>
            Ledger<span style={{ color: 'var(--primary-400)' }}>Line</span>
          </h1>
          <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem', marginTop: '0.25rem' }}>
            Secure Digital Banking & Double-Entry Ledger System
          </p>
        </div>

        {/* Demo Fast Logins Box */}
        <div className="card" style={{ marginBottom: '1.25rem', padding: '1rem', background: 'var(--bg-card)', border: '1px solid var(--border-subtle)' }}>
          <div style={{ fontSize: '0.75rem', fontWeight: 700, color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.05em', marginBottom: '0.65rem' }}>
            Quick 1-Click Demo Evaluation Sign In:
          </div>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.5rem' }}>
            <button
              type="button"
              onClick={() => handleDemo('CUSTOMER_1')}
              className="btn btn-outline btn-sm"
              style={{ justifyContent: 'flex-start', fontSize: '0.75rem' }}
              disabled={loading}
            >
              <UserCheck size={14} color="#34D399" />
              <span>Customer John</span>
            </button>
            <button
              type="button"
              onClick={() => handleDemo('CUSTOMER_2')}
              className="btn btn-outline btn-sm"
              style={{ justifyContent: 'flex-start', fontSize: '0.75rem' }}
              disabled={loading}
            >
              <UserCheck size={14} color="var(--primary-400)" />
              <span>Customer Jane</span>
            </button>
            <button
              type="button"
              onClick={() => handleDemo('ADMIN')}
              className="btn btn-outline btn-sm"
              style={{ justifyContent: 'flex-start', fontSize: '0.75rem' }}
              disabled={loading}
            >
              <ShieldAlert size={14} color="#F87171" />
              <span>Administrator</span>
            </button>
            <button
              type="button"
              onClick={() => handleDemo('AUDITOR')}
              className="btn btn-outline btn-sm"
              style={{ justifyContent: 'flex-start', fontSize: '0.75rem' }}
              disabled={loading}
            >
              <ShieldCheck size={14} color="#FBBF24" />
              <span>Lead Auditor</span>
            </button>
          </div>
        </div>

        {/* Login form */}
        <div className="card">
          <h2 style={{ fontSize: '1.25rem', fontWeight: 700, color: 'var(--text-primary)', marginBottom: '1.25rem' }}>
            Sign In to your account
          </h2>

          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label className="form-label">Email Address</label>
              <input
                type="email"
                placeholder="you@example.com"
                className="form-input"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
              />
            </div>

            <div className="form-group">
              <label className="form-label">Password</label>
              <input
                type="password"
                placeholder="••••••••"
                className="form-input"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
            </div>

            <button
              type="submit"
              className="btn btn-primary"
              style={{ width: '100%', marginTop: '0.5rem' }}
              disabled={loading}
            >
              {loading ? 'Authenticating...' : 'Sign In'}
              <ArrowRight size={16} />
            </button>
          </form>

          <div style={{ textAlign: 'center', marginTop: '1.5rem', fontSize: '0.875rem', color: 'var(--text-secondary)' }}>
            Don't have an account?{' '}
            <Link to="/register" style={{ color: 'var(--primary-400)', fontWeight: 600, textDecoration: 'none' }}>
              Create an account
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
};

export default LoginPage;
