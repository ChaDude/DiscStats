// src/screens/TeamsScreen.tsx
/**
 * Teams tab screen - MVP placeholder.
 * List teams, add/edit players.
 */
import React from 'react';
import { View, Text, StyleSheet } from 'react-native';

export default function TeamsScreen() {
  return (
    <View style={styles.container}>
      <Text style={styles.title}>Teams</Text>
      <Text>Coming soon: Team list, players, edit</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, justifyContent: 'center', alignItems: 'center' },
  title: { fontSize: 32, fontWeight: 'bold' },
});