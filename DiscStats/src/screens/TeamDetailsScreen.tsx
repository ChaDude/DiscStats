// src/screens/TeamDetailsScreen.tsx
import React, { useState, useEffect, useCallback } from 'react';
import { View, Text, FlatList, TouchableOpacity, Modal, TextInput, StyleSheet, Alert, Pressable } from 'react-native';
import { Picker } from '@react-native-picker/picker';
import { RouteProp, useNavigation, useRoute, useFocusEffect } from '@react-navigation/native';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';

import { getPlayersForTeam, addPlayer, updatePlayer, deletePlayer } from '../services/database';
import { Player } from '../models';
import { RootStackParamList } from '../navigation/types';

export default function TeamDetailsScreen() {
  const navigation = useNavigation<NativeStackNavigationProp<RootStackParamList>>();
  const { params } = useRoute<RouteProp<RootStackParamList, 'TeamDetails'>>();

  const teamId = params.teamId;
  const teamName = params.teamName;
  const editingPlayerId = params.editingPlayerId;

  const [players, setPlayers] = useState<Player[]>([]);
  const [modalVisible, setModalVisible] = useState(false);
  
  // Form State
  const [isEditing, setIsEditing] = useState(false);
  const [editingPlayer, setEditingPlayer] = useState<Player | null>(null);
  const [newPlayerName, setNewPlayerName] = useState('');
  const [newJersey, setNewJersey] = useState('');
  const [newGender, setNewGender] = useState<'male' | 'female' | 'other'>('other');

  // REFRESH: Reload list when screen focuses (e.g. coming back from Profile)
  useFocusEffect(
    useCallback(() => {
      loadPlayers();
    }, [teamId])
  );

  // TRIGGER EDIT: Handle param from PlayerProfile
  useEffect(() => {
    if (editingPlayerId && players.length > 0) {
      const playerToEdit = players.find(p => p.id === editingPlayerId);
      if (playerToEdit) {
        startEdit(playerToEdit);
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
    // Convert number to string for input; handle null/undefined
    setNewJersey(player.jerseyNumber !== undefined && player.jerseyNumber !== null ? player.jerseyNumber.toString() : '');
    setNewGender(player.gender);
    setModalVisible(true);
  };

  const handleOpenAddModal = () => {
    setNewPlayerName('');
    setNewJersey('');
    setNewGender('other');
    setIsEditing(false);
    setEditingPlayer(null);
    setModalVisible(true);
  };

  const handleCloseModal = () => {
    setModalVisible(false);
    if (isEditing) {
      navigation.goBack();
    }
  };

  const handleSavePlayer = async () => {
    if (!newPlayerName.trim()) {
      Alert.alert('Error', 'Player name is required');
      return;
    }

    // Convert input string to integer (or undefined if empty)
    // Note: '00' parses to 0, which is correct for INTEGER storage
    const jerseyInt = newJersey.trim() === '' ? undefined : parseInt(newJersey, 10);

    try {
      if (isEditing && editingPlayer) {
        await updatePlayer(editingPlayer.id, {
          name: newPlayerName.trim(),
          jerseyNumber: jerseyInt,
          gender: newGender,
        });
        
        setModalVisible(false);
        navigation.goBack(); // Return to PlayerProfile

      } else {
        await addPlayer(teamId, newPlayerName.trim(), jerseyInt, newGender);
        setModalVisible(false);
        loadPlayers(); 
      }
    } catch (error) {
      Alert.alert('Error', 'Failed to save player');
    }
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
            setModalVisible(false);
            navigation.pop(2); 
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
            {/* FIX: Use ?? instead of || so that "0" is displayed instead of "?" */}
            <Text style={styles.playerDetails}>
              #{item.jerseyNumber ?? '?'} • {item.gender}
            </Text>
          </Pressable>
        )}
        ListEmptyComponent={<Text style={styles.emptyText}>No players yet. Add one!</Text>}
      />

      <TouchableOpacity style={styles.addButton} onPress={handleOpenAddModal}>
        <Text style={styles.addButtonText}>+</Text>
      </TouchableOpacity>

      <Modal visible={modalVisible} animationType="slide" transparent onRequestClose={handleCloseModal}>
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
            
            {/* UPDATED JERSEY INPUT */}
            <TextInput
              style={styles.input}
              placeholder="Jersey Number (-999 to 999)"
              value={newJersey}
              onChangeText={(text) => {
                // 1. Regex check: Only digits and optional leading minus.
                // This prevents "9-9", spaces, or decimal points.
                if (!/^-?\d*$/.test(text)) {
                  return;
                }

                // 2. Allow intermediate states (empty or just minus sign)
                if (text === '' || text === '-') {
                  setNewJersey(text);
                  return;
                }

                // 3. Range check (-999 to 999)
                const num = parseInt(text, 10);
                if (!isNaN(num) && num >= -999 && num <= 999) {
                  setNewJersey(text);
                }
              }}
              keyboardType="numeric" // Use "numbers-and-punctuation" on iOS if negative needed? "numeric" usually supports minus on Android.
            />

            <Text style={styles.label}>Gender</Text>
            <Picker
              selectedValue={newGender}
              onValueChange={(itemValue) => setNewGender(itemValue)}
              style={styles.picker}
            >
              <Picker.Item label="Male" value="male" />
              <Picker.Item label="Female" value="female" />
              <Picker.Item label="Other" value="other" />
            </Picker>
            <View style={styles.modalButtons}>
              <TouchableOpacity style={styles.cancelButton} onPress={handleCloseModal}>
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
  playerItem: { padding: 16, borderBottomWidth: 1, borderBottomColor: '#eee', flexDirection: 'row', justifyContent: 'space-between' },
  playerName: { fontSize: 18, fontWeight: '600' },
  playerDetails: { fontSize: 14, color: '#666' },
  emptyText: { textAlign: 'center', marginTop: 40, fontSize: 16, color: '#888' },
  addButton: { position: 'absolute', bottom: 20, right: 20, backgroundColor: '#2196F3', width: 60, height: 60, borderRadius: 30, justifyContent: 'center', alignItems: 'center', elevation: 5 },
  addButtonText: { color: '#fff', fontSize: 40, fontWeight: 'bold', lineHeight: 40 },
  modalOverlay: { flex: 1, backgroundColor: 'rgba(0,0,0,0.5)', justifyContent: 'center', alignItems: 'center' },
  modalContent: { backgroundColor: '#fff', padding: 20, borderRadius: 12, width: '80%' },
  modalTitle: { fontSize: 20, fontWeight: 'bold', marginBottom: 16 },
  input: { borderWidth: 1, borderColor: '#ddd', borderRadius: 8, padding: 12, marginBottom: 16, fontSize: 16 },
  label: { fontSize: 16, color: '#333', marginBottom: 8 },
  picker: { backgroundColor: '#f9f9f9', borderRadius: 8, marginBottom: 16 },
  modalButtons: { flexDirection: 'row', gap: 12 },
  cancelButton: { flex: 1, padding: 12, backgroundColor: '#ddd', borderRadius: 8, alignItems: 'center' },
  cancelText: { color: '#333', fontWeight: '600' },
  deleteButton: { flex: 1, padding: 12, backgroundColor: '#f44336', borderRadius: 8, alignItems: 'center' },
  deleteText: { color: '#fff', fontWeight: '600' },
  saveButton: { flex: 1, padding: 12, backgroundColor: '#2196F3', borderRadius: 8, alignItems: 'center' },
  saveText: { color: '#fff', fontWeight: '600' },
});