import React, { createContext, useContext, useState, useCallback } from 'react';

const ToastContext = createContext(null);

export const ToastProvider = ({ children }) => {
  const [toasts, setToasts] = useState([]);

  const addToast = useCallback((message, type = 'info', duration = 4000) => {
    const id = Date.now() + Math.random().toString(36).substring(2, 6);
    setToasts((prev) => [...prev, { id, message, type }]);

    setTimeout(() => {
      setToasts((prev) => prev.filter((t) => t.id !== id));
    }, duration);
  }, []);

  const removeToast = useCallback((id) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
  }, []);

  return (
    <ToastContext.Provider value={{ addToast }}>
      {children}
      <div style={{
        position: 'fixed',
        bottom: '1.5rem',
        right: '1.5rem',
        display: 'flex',
        flexDirection: 'column',
        gap: '0.75rem',
        zIndex: 9999,
        maxWidth: '400px',
        width: '100%'
      }}>
        {toasts.map((toast) => (
          <div
            key={toast.id}
            onClick={() => removeToast(toast.id)}
            style={{
              padding: '0.875rem 1.25rem',
              borderRadius: '0.75rem',
              background: toast.type === 'success' ? '#064E3B' :
                          toast.type === 'error' ? '#7F1D1D' :
                          toast.type === 'warning' ? '#78350F' : '#1E293B',
              color: toast.type === 'success' ? '#6EE7B7' :
                     toast.type === 'error' ? '#FCA5A5' :
                     toast.type === 'warning' ? '#FDE68A' : '#E2E8F0',
              border: `1px solid ${
                toast.type === 'success' ? '#059669' :
                toast.type === 'error' ? '#DC2626' :
                toast.type === 'warning' ? '#D97706' : '#475569'
              }`,
              boxShadow: '0 10px 25px -5px rgba(0, 0, 0, 0.5)',
              fontSize: '0.875rem',
              fontWeight: 500,
              cursor: 'pointer',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              animation: 'fadeIn 0.2s ease-out'
            }}
          >
            <span>{toast.message}</span>
            <span style={{ marginLeft: '1rem', opacity: 0.6 }}>&times;</span>
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  );
};

export const useToast = () => {
  const context = useContext(ToastContext);
  if (!context) throw new Error('useToast must be used within ToastProvider');
  return context;
};

