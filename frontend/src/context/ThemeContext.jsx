import React, { createContext, useContext, useState, useEffect } from 'react';

const ThemeContext = createContext(null);

export const THEMES = [
  {
    id: 'emerald',
    name: 'Emerald Mint',
    primary: '#10B981',
    accent: '#34D399',
    bg: '#060B0E',
    description: 'Modern Fintech & Digital Wealth (Default)',
  },
  {
    id: 'indigo',
    name: 'Electric Indigo',
    primary: '#6366F1',
    accent: '#818CF8',
    bg: '#080914',
    description: 'Mercury & Stripe Neo-Banking',
  },
  {
    id: 'cyan',
    name: 'Cyber Cyan',
    primary: '#06B6D4',
    accent: '#38BDF8',
    bg: '#050B11',
    description: 'High-Tech Financial Ledger',
  },
  {
    id: 'gold',
    name: 'Executive Gold',
    primary: '#F59E0B',
    accent: '#FBBF24',
    bg: '#0A0A0C',
    description: 'Private Wealth & Luxury Obsidian',
  },
  {
    id: 'sapphire',
    name: 'Sapphire Blue',
    primary: '#3B82F6',
    accent: '#60A5FA',
    bg: '#0B0F19',
    description: 'Classic Corporate Banking',
  },
];

export const ThemeProvider = ({ children }) => {
  const [theme, setThemeState] = useState(() => {
    return localStorage.getItem('ledgerline_theme') || 'emerald';
  });

  const setTheme = (newTheme) => {
    setThemeState(newTheme);
    localStorage.setItem('ledgerline_theme', newTheme);
    document.documentElement.setAttribute('data-theme', newTheme);
  };

  useEffect(() => {
    document.documentElement.setAttribute('data-theme', theme);
  }, [theme]);

  return (
    <ThemeContext.Provider value={{ theme, setTheme, themes: THEMES }}>
      {children}
    </ThemeContext.Provider>
  );
};

export const useTheme = () => {
  const context = useContext(ThemeContext);
  if (!context) {
    throw new Error('useTheme must be used within a ThemeProvider');
  }
  return context;
};

