// App.tsx
/**
 * Root entry point for DiscStats app.
 * Initializes DB + applies global theme (light/dark/system).
 */
import React, { useEffect } from 'react';
import { StatusBar, SafeAreaView, View, StyleSheet } from 'react-native';

import RootNavigator from './src/navigation/RootNavigator';
import { initDatabase } from './src/services/database';
import { ThemeProvider, useTheme } from './src/context/ThemeContext';

export default function App() {
  useEffect(() => {
    initDatabase()
      .then(() => console.log('Database initialized successfully'))
      .catch(err => console.error('Database init failed:', err));
  }, []);

  return (
    <ThemeProvider>
      <ThemedApp />
    </ThemeProvider>
  );
}

// Inner component to access theme
function ThemedApp() {
  const { theme, currentScheme } = useTheme();
  const effectiveScheme = theme === 'system' ? currentScheme : theme;
  const isDark = effectiveScheme === 'dark';

  return (
    <SafeAreaView style={{ flex: 1, backgroundColor: isDark ? '#121212' : '#fff' }}>
      <StatusBar
        barStyle={isDark ? 'light-content' : 'dark-content'}
        backgroundColor={isDark ? '#121212' : '#fff'}
      />
      <View style={{ flex: 1 }}>
        <RootNavigator />
      </View>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
});