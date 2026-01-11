// App.tsx
/**
 * Root entry point for DiscStats app.
 * Initializes DB + wraps in ThemeProvider for global light/dark mode.
 */
import React, { useEffect } from 'react';

import RootNavigator from './src/navigation/RootNavigator';
import { initDatabase } from './src/services/database';
import { ThemeProvider } from './src/context/ThemeContext';

export default function App() {
  useEffect(() => {
    // Initialize database on mount
    initDatabase()
      .then(() => console.log('Database initialized successfully'))
      .catch(err => console.error('Database init failed:', err));
  }, []);

  return (
    <ThemeProvider>
      <RootNavigator />
    </ThemeProvider>
  );
}