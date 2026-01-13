// src/screens/TeamsScreen.tsx
/**
 * Teams tab screen.
 * Lists all teams with real player count from DB, add new team modal.
 * Uses database service for offline persistence.
 */
import React, { useState, useEffect, useCallback } from 'react';
import { View, Text, FlatList, TouchableOpacity, Modal, TextInput, StyleSheet, Alert } from 'react-native';
import { useNavigation, useFocusEffect } from '@react-navigation/native';

import { getTeams, addTeam, getPlayersForTeam } from '../services/database';
import { Team } from '../models';
import { RootStackNavigationProp } from '../navigation/types';
import { useTheme } from '../context/ThemeContext';

export default function TeamsScreen() {
  const [teams, setTeams] = useState<Team[]>([]);
  const [playerCounts, setPlayerCounts] = useState<Record<string, number>>({});
  const [modalVisible, setModalVisible] = useState(false);
  const [newTeamName, setNewTeamName] = useState('');
  const navigation = useNavigation<RootStackNavigationProp>();
  const { theme, currentScheme } = useTheme();
  const effectiveScheme = theme === 'system' ? currentScheme : theme;
  const isDark = effectiveScheme === 'dark';

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
      loadTeamsAndCounts();
    } catch (error) {
      Alert.alert('Error', 'Failed to add team');
    }
  };

  return (
    <View style={[styles.container, { backgroundColor: isDark ? '#121212' : '#fff' }]}>
      <FlatList
        data={teams}
        keyExtractor={(item) => item.id}
        renderItem={({ item }) => (
          <TouchableOpacity
            style={[
              styles.teamItem,
              { backgroundColor: isDark ? '#1e1e1e' : '#ffffff', borderBottomColor: isDark ? '#333333' : '#eeeeee' }
            ]}
            onPress={() => navigation.navigate('TeamDetails', { teamId: item.id, teamName: item.name })}
          >
            <Text style={[styles.teamName, { color: isDark ? '#ffffff' : '#333333' }]}>{item.name}</Text>
            <Text style={[styles.playerCount, { color: isDark ? '#bbbbbb' : '#666666' }]}>
              Players: {playerCounts[item.id] ?? 'Loading...'}
            </Text>
          </TouchableOpacity>
        )}
        ListEmptyComponent={<Text style={[styles.emptyText, { color: isDark ? '#bbbbbb' : '#888888' }]}>No teams yet. Add one!</Text>}
      />

      <TouchableOpacity style={[styles.addButton, { backgroundColor: isDark ? '#1e88e5' : '#2196f3' }]} onPress={() => setModalVisible(true)}>
        <Text style={styles.addButtonText}>+</Text>
      </TouchableOpacity>

      <Modal visible={modalVisible} animationType="slide" transparent>
        <View style={styles.modalOverlay}>
          <View style={[styles.modalContent, { backgroundColor: isDark ? '#1e1e1e' : '#ffffff' }]}>
            <Text style={[styles.modalTitle, { color: isDark ? '#ffffff' : '#333333' }]}>New Team</Text>
            <TextInput
              style={[styles.input, { backgroundColor: isDark ? '#333333' : '#ffffff', color: isDark ? '#ffffff' : '#333333', borderColor: isDark ? '#555555' : '#dddddd' }]}
              placeholder="Team Name"
              placeholderTextColor={isDark ? '#bbbbbb' : '#999999'}
              value={newTeamName}
              onChangeText={setNewTeamName}
              autoFocus
            />
            <View style={styles.modalButtons}>
              <TouchableOpacity style={[styles.cancelButton, { backgroundColor: isDark ? '#444444' : '#dddddd' }]} onPress={() => setModalVisible(false)}>
                <Text style={[styles.cancelText, { color: isDark ? '#ffffff' : '#333333' }]}>Cancel</Text>
              </TouchableOpacity>
              <TouchableOpacity style={[styles.saveButton, { backgroundColor: isDark ? '#1e88e5' : '#2196f3' }]} onPress={handleAddTeam}>
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
  container: { flex: 1 },
  teamItem: {
    padding: 16,
    borderBottomWidth: 1,
  },
  teamName: { fontSize: 18, fontWeight: '600' },
  playerCount: { fontSize: 14 },
  emptyText: { textAlign: 'center', marginTop: 40, fontSize: 16 },

  addButton: {
    position: 'absolute',
    bottom: 20,
    right: 20,
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
  addButtonText: { color: '#ffffff', fontSize: 40, fontWeight: 'bold', lineHeight: 40, textAlign: 'center' },

  modalOverlay: {
    flex: 1,
    backgroundColor: 'rgba(0,0,0,0.5)',
    justifyContent: 'center',
    alignItems: 'center',
  },
  modalContent: {
    padding: 20,
    borderRadius: 12,
    width: '80%',
  },
  modalTitle: { fontSize: 20, fontWeight: 'bold', marginBottom: 16 },
  input: {
    borderWidth: 1,
    borderRadius: 8,
    padding: 12,
    marginBottom: 16,
    fontSize: 16,
  },
  modalButtons: { flexDirection: 'row', justifyContent: 'space-between' },
  cancelButton: { padding: 12, borderRadius: 8 },
  saveButton: { padding: 12, borderRadius: 8 },
  cancelText: { fontWeight: '600' },
  saveText: { color: '#ffffff', fontWeight: '600' },
});