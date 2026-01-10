// App.tsx
/**
 * Root entry point for DiscStats app.
 * Currently minimal: only Home screen + temporary stress test on mount.
 * Navigation is basic until we add more screens.
 */
import React, { useEffect } from 'react';
import { View, Text, Button, StyleSheet } from 'react-native';

// Temporary: import and run the stress test
import { runStressTest } from './src/tests/stressTest';

// Placeholder Home screen (create if missing)
function HomeScreen() {
  return (
    <View style={styles.container}>
      <Text style={styles.title}>DiscStats</Text>
      <Text style={styles.subtitle}>Game Engine Test Mode</Text>
      <Text style={styles.info}>Check Metro terminal for stress test results</Text>
    </View>
  );
}

export default function App() {
  useEffect(() => {
    console.log('App mounted - running game engine stress test...');
    runStressTest();
    console.log('Stress test execution complete');
  }, []);

  return (
    <View style={styles.appContainer}>
      <HomeScreen />
    </View>
  );
}

const styles = StyleSheet.create({
  appContainer: { flex: 1, backgroundColor: '#fff' },
  container: { flex: 1, justifyContent: 'center', alignItems: 'center', padding: 20 },
  title: { fontSize: 32, fontWeight: 'bold', marginBottom: 16 },
  subtitle: { fontSize: 20, marginBottom: 12 },
  info: { fontSize: 16, color: '#666', textAlign: 'center' },
});