// src/screens/PlayerProfileScreen.tsx
/**
 * Player profile screen.
 * Displays player details + edit button that triggers edit modal in TeamDetails.
 * Navigated from TeamDetails on player tap.
 */
import React from 'react';
import { View, Text, TouchableOpacity, StyleSheet } from 'react-native';
import { RouteProp, useNavigation } from '@react-navigation/native';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';

import { RootStackParamList } from '../navigation/types';
import { Player } from '../models';

type PlayerProfileRouteParams = {
  player: Player;
  teamName: string;
  teamId: string; // ← Required for passing back
};

type Props = {
  route: RouteProp<RootStackParamList, 'PlayerProfile'>;
};

export default function PlayerProfileScreen({ route }: Props) {
  const { player, teamName, teamId } = route.params;
  const navigation = useNavigation<NativeStackNavigationProp<RootStackParamList>>();

  const handleEdit = () => {
    navigation.navigate('TeamDetails', {
      teamId,
      teamName,
      editingPlayerId: player.id // Trigger edit modal
    });
  };

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

      <TouchableOpacity style={styles.editButton} onPress={handleEdit}>
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