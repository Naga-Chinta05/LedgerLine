import React, { createContext, useContext, useState, useEffect } from 'react';
import { authApi } from '../api/auth';
import { useToast } from './ToastContext';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(() => {
    const saved = localStorage.getItem('ledgerline_user');
    return saved ? JSON.parse(saved) : null;
  });
  const [token, setToken] = useState(() => localStorage.getItem('ledgerline_token') || null);
  const [loading, setLoading] = useState(true);
  const { addToast } = useToast();

  useEffect(() => {
    const verifyUser = async () => {
      if (token) {
        try {
          const profile = await authApi.getMe();
          const userData = {
            id: profile.id,
            email: profile.email,
            fullName: profile.fullName,
            role: profile.role,
          };
          setUser(userData);
          localStorage.setItem('ledgerline_user', JSON.stringify(userData));
        } catch (err) {
          console.error('Session verification failed', err);
          localStorage.removeItem('ledgerline_token');
          localStorage.removeItem('ledgerline_user');
          setToken(null);
          setUser(null);
        }
      }
      setLoading(false);
    };

    verifyUser();
  }, [token]);

  const login = async (email, password) => {
    try {
      const data = await authApi.login({ email, password });
      const userData = {
        id: data.userId,
        email: data.email,
        fullName: data.fullName,
        role: data.role,
      };

      localStorage.setItem('ledgerline_token', data.token);
      localStorage.setItem('ledgerline_user', JSON.stringify(userData));
      setToken(data.token);
      setUser(userData);
      addToast(`Welcome back, ${data.fullName}!`, 'success');
      return userData;
    } catch (err) {
      const msg = err.response?.data?.message || 'Login failed. Please check credentials.';
      addToast(msg, 'error');
      throw err;
    }
  };

  const register = async (email, password, fullName, role = 'ACCOUNT_HOLDER') => {
    try {
      const data = await authApi.register({ email, password, fullName, role });
      const userData = {
        id: data.userId,
        email: data.email,
        fullName: data.fullName,
        role: data.role,
      };

      localStorage.setItem('ledgerline_token', data.token);
      localStorage.setItem('ledgerline_user', JSON.stringify(userData));
      setToken(data.token);
      setUser(userData);
      addToast('Registration successful! Bank account created.', 'success');
      return userData;
    } catch (err) {
      const msg = err.response?.data?.message || 'Registration failed.';
      addToast(msg, 'error');
      throw err;
    }
  };

  const demoLogin = async (type) => {
    let email = '';
    let pass = '';
    switch (type) {
      case 'ADMIN':
        email = 'admin@ledgerline.com';
        pass = 'Admin@123';
        break;
      case 'AUDITOR':
        email = 'auditor@ledgerline.com';
        pass = 'Auditor@123';
        break;
      case 'CUSTOMER_2':
        email = 'jane.smith@example.com';
        pass = 'User@123';
        break;
      case 'CUSTOMER_1':
      default:
        email = 'john.doe@example.com';
        pass = 'User@123';
        break;
    }
    return login(email, pass);
  };

  const logout = () => {
    localStorage.removeItem('ledgerline_token');
    localStorage.removeItem('ledgerline_user');
    setToken(null);
    setUser(null);
    addToast('Logged out successfully', 'info');
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        loading,
        isAuthenticated: !!token && !!user,
        isAdmin: user?.role === 'ADMIN',
        isAuditor: user?.role === 'AUDITOR' || user?.role === 'ADMIN',
        login,
        register,
        demoLogin,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used within AuthProvider');
  return context;
};

