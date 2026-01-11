// App.tsx
/**
 * Root entry point for DiscStats app.
 * Initializes SQLite database on mount and renders the main navigation.
 * Keeps everything offline-first with future sync compatibility.
 */
import React, { useEffect } from 'react';

import RootNavigator from './src/navigation/RootNavigator';
import { initDatabase } from './src/services/database'; // Step 3: DB init

export default function App() {
  useEffect(() => {
    // Initialize database (schema + defaults) once on app start
    initDatabase()
      .then(() => console.log('Database initialized successfully'))
      .catch(err => console.error('Database init failed:', err));
  }, []); // Empty deps → runs once on mount

  return <RootNavigator />;
}