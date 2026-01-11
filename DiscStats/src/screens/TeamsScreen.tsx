// src/screens/TeamsScreen.tsx
/**
 * Teams tab screen.
 * Lists all teams with real player count from DB, add new team modal.
 * Uses database service for offline persistence.
 */
import React, { useState, useEffect, useCallback } from 'react';
import { View, Text, FlatList, TouchableOpacity, Modal, TextInput, StyleSheet, Alert } from 'react-native';
import { useNavigation, useFocusEffect } from '@react-navigation/native';
import { Ionicons } from '@expo/vector-icons'; // For disc icon (if needed later)

import { getTeams, addTeam, getPlayersForTeam } from '../services/database';
import { Team } from '../models';
import { RootStackNavigationProp } from '../navigation/types';

export default function TeamsScreen() {
  const [teams, setTeams] = useState<Team[]>([]);
  const [playerCounts, setPlayerCounts] = useState<Record<string, number>>({});
  const [modalVisible, setModalVisible] = useState(false);
  const [newTeamName, setNewTeamName] = useState('');
  const navigation = useNavigation<RootStackNavigationProp>();

  const loadTeamsAndCounts = useCallback(async () => {
    try {
      const loadedTeams = await getTeams();
      setTeams(loadedTeams);

      const counts: Record<string, number> = {};
      for (const team of loadedTeams) {
        const players = await getPlayersForTeam(team.id);
        counts[team.id] = players.length;
      }
      setPlayerCounts(counts);
    } catch (error) {
      Alert.alert('Error', 'Failed to load teams');
    }
  }, []);

  useEffect(() => {
    loadTeamsAndCounts();
  }, [loadTeamsAndCounts]);

  useFocusEffect(
    useCallback(() => {
      loadTeamsAndCounts();
    }, [loadTeamsAndCounts])
  );

  const handleAddTeam = async () => {
    if (!newTeamName.trim()) {
      Alert.alert('Error', 'Team name is required');
      return;
    }

    try {
      await addTeam(newTeamName.trim());
      setNewTeamName('');
      setModalVisible(false);
      loadTeamsAndCounts(); // Refresh list + counts
    } catch (error) {
      Alert.alert('Error', 'Failed to add team');
    }
  };

  return (
    <View style={styles.container}>
      <FlatList
        data={teams}
        keyExtractor={(item) => item.id}
        renderItem={({ item }) => (
          <TouchableOpacity
            style={styles.teamItem}
            onPress={() => navigation.navigate('TeamDetails', { teamId: item.id, teamName: item.name })}
          >
            <Text style={styles.teamName}>{item.name}</Text>
            <Text style={styles.playerCount}>
              Players: {playerCounts[item.id] ?? 'Loading...'}
            </Text>
          </TouchableOpacity>
        )}
        ListEmptyComponent={<Text style={styles.emptyText}>No teams yet. Add one!</Text>}
      />

      {/* Floating Add Button - simple "+" symbol */}
      <TouchableOpacity style={styles.addButton} onPress={() => setModalVisible(true)}>
        <Text style={styles.addButtonText}>+</Text>
      </TouchableOpacity>

      {/* Add Team Modal */}
      <Modal visible={modalVisible} animationType="slide" transparent>
        <View style={styles.modalOverlay}>
          <View style={styles.modalContent}>
            <Text style={styles.modalTitle}>New Team</Text>
            <TextInput
              style={styles.input}
              placeholder="Team Name"
              value={newTeamName}
              onChangeText={setNewTeamName}
              autoFocus
            />
            <View style={styles.modalButtons}>
              <TouchableOpacity style={styles.cancelButton} onPress={() => setModalVisible(false)}>
                <Text style={styles.cancelText}>Cancel</Text>
              </TouchableOpacity>
              <TouchableOpacity style={styles.saveButton} onPress={handleAddTeam}>
                <Text style={styles.saveText}>Save</Text>
              </TouchableOpacity>
            </View>
          </View>
        </View>
      </Modal>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#fff' },
  teamItem: {
    padding: 16,
    borderBottomWidth: 1,
    borderBottomColor: '#eee',
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  teamName: { fontSize: 18, fontWeight: '600' },
  playerCount: { fontSize: 14, color: '#666' },
  emptyText: { textAlign: 'center', marginTop: 40, fontSize: 16, color: '#888' },

  addButton: {
    position: 'absolute',
    bottom: 20,
    right: 20,
    backgroundColor: '#2196F3',
    width: 60,
    height: 60,
    borderRadius: 30,
    justifyContent: 'center',
    alignItems: 'center',
    elevation: 5,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.3,
    shadowRadius: 3,
  },
  addButtonText: { color: '#fff', fontSize: 40, fontWeight: 'bold', lineHeight: 40, textAlign: 'center' }, // Adjusted for better alignment

  modalOverlay: {
    flex: 1,
    backgroundColor: 'rgba(0,0,0,0.5)',
    justifyContent: 'center',
    alignItems: 'center',
  },
  modalContent: {
    backgroundColor: '#fff',
    padding: 20,
    borderRadius: 12,
    width: '80%',
  },
  modalTitle: { fontSize: 20, fontWeight: 'bold', marginBottom: 16 },
  input: {
    borderWidth: 1,
    borderColor: '#ddd',
    borderRadius: 8,
    padding: 12,
    marginBottom: 16,
    fontSize: 16,
  },
  modalButtons: { flexDirection: 'row', justifyContent: 'space-between' },
  cancelButton: { padding: 12, backgroundColor: '#ddd', borderRadius: 8 },
  cancelText: { color: '#333' },
  saveButton: { padding: 12, backgroundColor: '#2196F3', borderRadius: 8 },
  saveText: { color: '#fff', fontWeight: '600' },
});