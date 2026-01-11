// src/screens/TeamDetailsScreen.tsx
/**
 * Team details screen.
 * Shows players for a selected team + add/edit player modal.
 * Delete is nested in edit modal for safety.
 * Offline-first with DB integration.
 * Fully cross-platform (flexbox).
 */
import React, { useState, useEffect } from 'react';
import { View, Text, FlatList, TouchableOpacity, Modal, TextInput, StyleSheet, Alert, Pressable } from 'react-native';
import { Picker } from '@react-native-picker/picker';
import { RouteProp, useNavigation, useRoute } from '@react-navigation/native'; // ← Added useRoute
import { NativeStackNavigationProp } from '@react-navigation/native-stack';

import { getPlayersForTeam, addPlayer, updatePlayer, deletePlayer } from '../services/database';
import { Player } from '../models';
import { RootStackParamList } from '../navigation/types';

type Props = {
  route: RouteProp<RootStackParamList, 'TeamDetails'>;
};

export default function TeamDetailsScreen({ route }: Props) {
  const navigation = useNavigation<NativeStackNavigationProp<RootStackParamList>>();
  const { params } = useRoute<RouteProp<RootStackParamList, 'TeamDetails'>>(); // ← Fresh params

  const teamId = params.teamId;
  const teamName = params.teamName;
  const editingPlayerId = params.editingPlayerId;

  const [players, setPlayers] = useState<Player[]>([]);
  const [modalVisible, setModalVisible] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  const [editingPlayer, setEditingPlayer] = useState<Player | null>(null);
  const [newPlayerName, setNewPlayerName] = useState('');
  const [newJersey, setNewJersey] = useState('');
  const [newGender, setNewGender] = useState<'male' | 'female' | 'other'>('other');

  // Load players on mount
  useEffect(() => {
    loadPlayers();
  }, [teamId]);

  // Handle edit trigger from PlayerProfile (only once)
  useEffect(() => {
    if (editingPlayerId && players.length > 0) {
      const playerToEdit = players.find(p => p.id === editingPlayerId);
      if (playerToEdit) {
        startEdit(playerToEdit);
        // Clear param to prevent re-trigger
        navigation.setParams({ editingPlayerId: undefined });
      }
    }
  }, [editingPlayerId, players, navigation]);

  const loadPlayers = async () => {
    try {
      const loadedPlayers = await getPlayersForTeam(teamId);
      setPlayers(loadedPlayers);
    } catch (error) {
      Alert.alert('Error', 'Failed to load players');
    }
  };

  const startEdit = (player: Player) => {
    setIsEditing(true);
    setEditingPlayer(player);
    setNewPlayerName(player.name);
    setNewJersey(player.jerseyNumber?.toString() || '');
    setNewGender(player.gender);
    setModalVisible(true);
  };

  const handleSavePlayer = async () => {
    if (!newPlayerName.trim()) {
      Alert.alert('Error', 'Player name is required');
      return;
    }

    try {
      if (isEditing && editingPlayer) {
        await updatePlayer(editingPlayer.id, {
          name: newPlayerName.trim(),
          jerseyNumber: newJersey ? parseInt(newJersey) : undefined,
          gender: newGender,
        });
      } else {
        await addPlayer(teamId, newPlayerName.trim(), newJersey ? parseInt(newJersey) : undefined, newGender);
      }

      resetModal();
      loadPlayers(); // Refresh list
      if (isEditing) {
        navigation.pop(); // Pop PlayerProfile after edit
      }
    } catch (error) {
      Alert.alert('Error', 'Failed to save player');
    }
  };

  const resetModal = () => {
    setNewPlayerName('');
    setNewJersey('');
    setNewGender('other');
    setIsEditing(false);
    setEditingPlayer(null);
    setModalVisible(false);
  };

  const handleDeletePlayer = () => {
    if (!editingPlayer) return;

    Alert.alert('Confirm Delete', 'Delete this player? This cannot be undone.', [
      { text: 'Cancel' },
      {
        text: 'Delete',
        style: 'destructive',
        onPress: async () => {
          try {
            await deletePlayer(editingPlayer.id);
            resetModal();
            loadPlayers(); // Refresh list
            navigation.pop(); // Pop PlayerProfile after delete
          } catch (error) {
            Alert.alert('Error', 'Failed to delete player');
          }
        },
      },
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
            onPress={() => navigation.navigate('PlayerProfile', { player: item, teamName, teamId })}
          >
            <Text style={styles.playerName}>{item.name}</Text>
            <Text style={styles.playerDetails}>
              #{item.jerseyNumber || '?'} • {item.gender}
            </Text>
          </Pressable>
        )}
        ListEmptyComponent={<Text style={styles.emptyText}>No players yet. Add one!</Text>}
      />

      {/* Floating Add Button */}
      <TouchableOpacity style={styles.addButton} onPress={resetModal}>
        <Text style={styles.addButtonText}>+</Text>
      </TouchableOpacity>

      {/* Add/Edit Player Modal */}
      <Modal visible={modalVisible} animationType="slide" transparent>
        <View style={styles.modalOverlay}>
          <View style={styles.modalContent}>
            <Text style={styles.modalTitle}>{isEditing ? 'Edit Player' : 'New Player'}</Text>
            <TextInput
              style={styles.input}
              placeholder="Player Name"
              value={newPlayerName}
              onChangeText={setNewPlayerName}
              autoFocus
            />
            <TextInput
              style={styles.input}
              placeholder="Jersey Number (optional, -999 to 999)"
              value={newJersey}
              onChangeText={(text) => {
                const cleaned = text.replace(/[^0-9-]/g, '');
                if (cleaned === '' || cleaned === '-') {
                  setNewJersey(cleaned);
                  return;
                }
                const num = parseInt(cleaned);
                if (num >= -999 && num <= 999) {
                  setNewJersey(cleaned);
                }
              }}
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
              <TouchableOpacity style={styles.cancelButton} onPress={resetModal}>
                <Text style={styles.cancelText}>Cancel</Text>
              </TouchableOpacity>
              {isEditing && (
                <TouchableOpacity style={styles.deleteButton} onPress={handleDeletePlayer}>
                  <Text style={styles.deleteText}>Delete</Text>
                </TouchableOpacity>
              )}
              <TouchableOpacity style={styles.saveButton} onPress={handleSavePlayer}>
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
  addButtonText: { color: '#fff', fontSize: 40, fontWeight: 'bold', lineHeight: 40, textAlign: 'center' },

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
  modalButtons: { flexDirection: 'row', justifyContent: 'space-between', gap: 12 },
  cancelButton: { flex: 1, padding: 12, backgroundColor: '#ddd', borderRadius: 8, alignItems: 'center' },
  cancelText: { color: '#333', fontWeight: '600' },
  deleteButton: { flex: 1, padding: 12, backgroundColor: '#f44336', borderRadius: 8, alignItems: 'center' },
  deleteText: { color: '#fff', fontWeight: '600' },
  saveButton: { flex: 1, padding: 12, backgroundColor: '#2196F3', borderRadius: 8, alignItems: 'center' },
  saveText: { color: '#fff', fontWeight: '600' },
});