// src/screens/TeamDetailsScreen.tsx
/**
 * Team details screen.
 * Shows players for a selected team + add player form.
 * Offline-first with DB integration; delete player on long-press.
 * Fully cross-platform (flexbox).
 */
import React, { useState, useEffect } from 'react';
import { View, Text, FlatList, TouchableOpacity, Modal, TextInput, StyleSheet, Alert, Pressable } from 'react-native';
import { Picker } from '@react-native-picker/picker'; // Only this import for Picker
import { RouteProp } from '@react-navigation/native';

import { getPlayersForTeam, addPlayer, deletePlayer } from '../services/database';
import { Player } from '../models';

type TeamDetailsRouteParams = {
  teamId: string;
  teamName: string;
};

type Props = {
  route: RouteProp<{ TeamDetails: TeamDetailsRouteParams }, 'TeamDetails'>;
};

export default function TeamDetailsScreen({ route }: Props) {
  const { teamId, teamName } = route.params;
  const [players, setPlayers] = useState<Player[]>([]);
  const [modalVisible, setModalVisible] = useState(false);
  const [newPlayerName, setNewPlayerName] = useState('');
  const [newJersey, setNewJersey] = useState('');
  const [newGender, setNewGender] = useState<'male' | 'female' | 'other'>('other');

  // Load players on mount
  useEffect(() => {
    loadPlayers();
  }, []);

  const loadPlayers = async () => {
    try {
      const loadedPlayers = await getPlayersForTeam(teamId);
      setPlayers(loadedPlayers);
    } catch (error) {
      Alert.alert('Error', 'Failed to load players');
    }
  };

  const handleAddPlayer = async () => {
    if (!newPlayerName.trim()) {
      Alert.alert('Error', 'Player name is required');
      return;
    }

    try {
      await addPlayer(teamId, newPlayerName.trim(), newJersey ? parseInt(newJersey) : undefined, newGender);
      setNewPlayerName('');
      setNewJersey('');
      setNewGender('other');
      setModalVisible(false);
      loadPlayers(); // Refresh list
    } catch (error) {
      Alert.alert('Error', 'Failed to add player');
    }
  };

  const handleDeletePlayer = (playerId: string) => {
    Alert.alert('Confirm Delete', 'Delete this player?', [
      { text: 'Cancel' },
      { text: 'Delete', style: 'destructive', onPress: async () => {
        try {
          await deletePlayer(playerId);
          loadPlayers(); // Refresh
        } catch (error) {
          Alert.alert('Error', 'Failed to delete player');
        }
      }}
    ]);
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>{teamName}</Text>

      <FlatList
        data={players}
        keyExtractor={item => item.id}
        renderItem={({ item }) => (
          <Pressable
            style={styles.playerItem}
            onPress={() => navigation.navigate('PlayerProfile', { player: item, teamName })}
            onLongPress={() => handleDeletePlayer(item.id)}
          >
            <Text style={styles.playerName}>{item.name}</Text>
            <Text style={styles.playerDetails}>
              #{item.jerseyNumber || '?'} • {item.gender}
            </Text>
          </Pressable>
        )}
        ListEmptyComponent={<Text style={styles.emptyText}>No players yet. Add one!</Text>}
      />

      {/* Floating Add Button - simple "+" symbol */}
      <TouchableOpacity style={styles.addButton} onPress={() => setModalVisible(true)}>
        <Text style={styles.addButtonText}>+</Text>
      </TouchableOpacity>

      {/* Add Player Modal */}
      <Modal visible={modalVisible} animationType="slide" transparent>
        <View style={styles.modalOverlay}>
          <View style={styles.modalContent}>
            <Text style={styles.modalTitle}>New Player</Text>
            <TextInput
              style={styles.input}
              placeholder="Player Name"
              value={newPlayerName}
              onChangeText={setNewPlayerName}
              autoFocus
            />
            <TextInput
              style={styles.input}
              placeholder="Jersey Number (optional)"
              value={newJersey}
              onChangeText={setNewJersey}
              keyboardType="numeric"
            />
            <Text style={styles.label}>Gender</Text>
            <Picker
              selectedValue={newGender}
              onValueChange={(itemValue: 'male' | 'female' | 'other') => setNewGender(itemValue)}
              style={styles.picker}
            >
              <Picker.Item label="Male" value="male" />
              <Picker.Item label="Female" value="female" />
              <Picker.Item label="Other" value="other" />
            </Picker>
            <View style={styles.modalButtons}>
              <TouchableOpacity style={styles.cancelButton} onPress={() => setModalVisible(false)}>
                <Text style={styles.cancelText}>Cancel</Text>
              </TouchableOpacity>
              <TouchableOpacity style={styles.saveButton} onPress={handleAddPlayer}>
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
  title: { fontSize: 32, fontWeight: 'bold', margin: 16, textAlign: 'center' },

  playerItem: {
    padding: 16,
    borderBottomWidth: 1,
    borderBottomColor: '#eee',
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  playerName: { fontSize: 18, fontWeight: '600' },
  playerDetails: { fontSize: 14, color: '#666' },
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
  addButtonText: { color: '#fff', fontSize: 40, fontWeight: 'bold', lineHeight: 40, textAlign: 'center' }, // Clean alignment

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
  label: { fontSize: 16, color: '#333', marginBottom: 8 },
  picker: { backgroundColor: '#f9f9f9', borderRadius: 8, marginBottom: 16 },
  modalButtons: { flexDirection: 'row', justifyContent: 'space-between' },
  cancelButton: { padding: 12, backgroundColor: '#ddd', borderRadius: 8 },
  cancelText: { color: '#333' },
  saveButton: { padding: 12, backgroundColor: '#2196F3', borderRadius: 8 },
  saveText: { color: '#fff', fontWeight: '600' },
});