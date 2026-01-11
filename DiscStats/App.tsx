// App.tsx
/**
 * Root entry point for DiscStats app.
 * Temporary: runs specific engine test on mount for verification.
 * Remove useEffect block after testing.
 */
import React, { useEffect } from 'react';
import { View, Text, StyleSheet } from 'react-native';

// Import the current exported test function
import { runSpecificEngineTest } from './src/tests/stressTest';

export default function App() {
  useEffect(() => {
    console.log('App mounted - running specific engine test...');
    try {
      runSpecificEngineTest();  // ← Correct function name
      console.log('Specific engine test complete');
    } catch (error) {
      console.error('Test crashed:', error);
    }
  }, []);

  return (
    <View style={styles.container}>
      <Text style={styles.title}>DiscStats</Text>
      <Text style={styles.subtitle}>Engine Test Mode</Text>
      <Text style={styles.info}>Check Metro terminal for test results</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, justifyContent: 'center', alignItems: 'center', backgroundColor: '#fff', padding: 20 },
  title: { fontSize: 36, fontWeight: 'bold', marginBottom: 16 },
  subtitle: { fontSize: 20, color: '#4CAF50', marginBottom: 12 },
  info: { fontSize: 16, color: '#666', textAlign: 'center' },
});