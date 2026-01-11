// src/screens/PlayerProfileScreen.tsx
/**
 * Player profile screen.
 * Displays player details + edit button (reuses modal from TeamDetails).
 * Navigated from TeamDetails on player tap.
 */
import React from 'react';
import { View, Text, TouchableOpacity, StyleSheet, Alert } from 'react-native'; // ← Added Alert
import { RouteProp } from '@react-navigation/native';
import { useNavigation } from '@react-navigation/native';

import { Player } from '../models';

type PlayerProfileRouteParams = {
  player: Player;
  teamName: string; // For context
};

type Props = {
  route: RouteProp<{ PlayerProfile: PlayerProfileRouteParams }, 'PlayerProfile'>;
};

export default function PlayerProfileScreen({ route }: Props) {
  const { player, teamName } = route.params;
  const navigation = useNavigation();

  return (
    <View style={styles.container}>
      <Text style={styles.title}>{player.name}</Text>

      <View style={styles.detailRow}>
        <Text style={styles.label}>Jersey Number:</Text>
        <Text style={styles.value}>#{player.jerseyNumber || 'N/A'}</Text>
      </View>

      <View style={styles.detailRow}>
        <Text style={styles.label}>Gender:</Text>
        <Text style={styles.value}>{player.gender}</Text>
      </View>

      <View style={styles.detailRow}>
        <Text style={styles.label}>Team:</Text>
        <Text style={styles.value}>{teamName}</Text>
      </View>

      <TouchableOpacity
        style={styles.editButton}
        onPress={() => {
          // Navigate back to TeamDetails and trigger edit modal there
          navigation.goBack();
          // We'll handle modal trigger in TeamDetails (via params or global state later)
          Alert.alert('Edit', 'Edit functionality coming soon');
        }}
      >
        <Text style={styles.editButtonText}>Edit Player</Text>
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#fff', padding: 20 },
  title: { fontSize: 32, fontWeight: 'bold', textAlign: 'center', marginVertical: 20 },
  detailRow: { flexDirection: 'row', justifyContent: 'space-between', paddingVertical: 12, borderBottomWidth: 1, borderBottomColor: '#eee' },
  label: { fontSize: 18, fontWeight: '600', color: '#333' },
  value: { fontSize: 18, color: '#666' },
  editButton: {
    marginTop: 40,
    backgroundColor: '#2196F3',
    padding: 16,
    borderRadius: 12,
    alignItems: 'center',
  },
  editButtonText: { color: '#fff', fontSize: 18, fontWeight: '600' },
});