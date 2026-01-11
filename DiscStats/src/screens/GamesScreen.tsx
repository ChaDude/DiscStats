// src/screens/GamesScreen.tsx
/**
 * Games tab screen - MVP placeholder.
 * List games, start new, view details.
 */
import React from 'react';
import { View, Text, StyleSheet } from 'react-native';

export default function GamesScreen() {
  return (
    <View style={styles.container}>
      <Text style={styles.title}>Games</Text>
      <Text>Coming soon: Game list, new game, details</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, justifyContent: 'center', alignItems: 'center' },
  title: { fontSize: 32, fontWeight: 'bold' },
});