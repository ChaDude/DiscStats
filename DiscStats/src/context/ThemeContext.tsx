// src/context/ThemeContext.tsx
/**
 * Global theme context for light/dark/system mode.
 * Persists user choice + detects system preference.
 * Lean, no extra deps beyond React Native built-ins.
 */
import React, { createContext, useContext, useState, useEffect } from 'react';
import { Appearance, ColorSchemeName } from 'react-native';

import { getThemeMode, setThemeMode, ThemeMode } from '../services/preferences';

type ThemeContextType = {
  theme: ThemeMode;
  setTheme: (mode: ThemeMode) => Promise<void>;
  currentScheme: ColorSchemeName; // 'light' | 'dark' | null
};

const ThemeContext = createContext<ThemeContextType | undefined>(undefined);

export const ThemeProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [theme, setThemeState] = useState<ThemeMode>('system');
  const [currentScheme, setCurrentScheme] = useState<ColorSchemeName>(Appearance.getColorScheme());

  // Load saved preference on mount
  useEffect(() => {
    (async () => {
      const saved = await getThemeMode();
      setThemeState(saved);
    })();
  }, []);

  // Listen for system changes (only when in 'system' mode)
  useEffect(() => {
    const subscription = Appearance.addChangeListener(({ colorScheme }) => {
      setCurrentScheme(colorScheme);
    });
    return () => subscription.remove();
  }, []);

  // Apply theme mode
  const setTheme = async (mode: ThemeMode) => {
    setThemeState(mode);
    await setThemeMode(mode);
  };

  return (
    <ThemeContext.Provider value={{ theme, setTheme, currentScheme }}>
      {children}
    </ThemeContext.Provider>
  );
};

export const useTheme = () => {
  const context = useContext(ThemeContext);
  if (!context) {
    throw new Error('useTheme must be used within ThemeProvider');
  }
  return context;
};