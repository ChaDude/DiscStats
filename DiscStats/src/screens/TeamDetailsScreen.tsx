// src/screens/TeamDetailsScreen.tsx
import React, { useState, useEffect, useCallback } from 'react';
import { View, Text, FlatList, TouchableOpacity, Modal, TextInput, StyleSheet, Alert, Pressable } from 'react-native';
import { Picker } from '@react-native-picker/picker';
import { RouteProp, useNavigation, useRoute, useFocusEffect } from '@react-navigation/native';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';

import { getPlayersForTeam, addPlayer, updatePlayer, deletePlayer } from '../services/database';
import { Player } from '../models';
import { RootStackParamList } from '../navigation/types';
import { useTheme } from '../context/ThemeContext'; //

export default function TeamDetailsScreen() {
  const navigation = useNavigation<NativeStackNavigationProp<RootStackParamList>>();
  const { params } = useRoute<RouteProp<RootStackParamList, 'TeamDetails'>>();
  
  // Theme Hooks
  const { theme, currentScheme } = useTheme();
  const isDark = (theme === 'system' ? currentScheme : theme) === 'dark';
  
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

  useFocusEffect(
    useCallback(() => { loadPlayers(); }, [teamId])
  );

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
    if (isEditing) navigation.goBack();
  };

  const handleSavePlayer = async () => {
    if (!newPlayerName.trim()) {
      Alert.alert('Error', 'Player name is required');
      return;
    }
    const jerseyInt = newJersey.trim() === '' ? undefined : parseInt(newJersey, 10);

    try {
      if (isEditing && editingPlayer) {
        await updatePlayer(editingPlayer.id, {
          name: newPlayerName.trim(),
          jerseyNumber: jerseyInt,
          gender: newGender,
        });
        setModalVisible(false);
        navigation.goBack();
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

  // Dynamic Styles
  const bgStyle = { backgroundColor: isDark ? '#121212' : '#fff' };
  const textStyle = { color: isDark ? '#fff' : '#000' };
  const subTextStyle = { color: isDark ? '#bbb' : '#666' };
  const itemBorder = { borderBottomColor: isDark ? '#333' : '#eee' };
  const modalBg = { backgroundColor: isDark ? '#1e1e1e' : '#fff' };
  const inputBg = { 
    backgroundColor: isDark ? '#333' : '#fff', 
    color: isDark ? '#fff' : '#000',
    borderColor: isDark ? '#555' : '#ddd'
  };
  const placeholderColor = isDark ? '#aaa' : '#999';

  return (
    <View style={[styles.container, bgStyle]}>
      <Text style={[styles.title, textStyle]}>{teamName}</Text>

      <FlatList
        data={players}
        keyExtractor={item => item.id}
        renderItem={({ item }) => (
          <Pressable
            style={[styles.playerItem, itemBorder]}
            onPress={() => navigation.navigate('PlayerProfile', { player: item, teamName, teamId })}
          >
            <Text style={[styles.playerName, textStyle]}>{item.name}</Text>
            <Text style={[styles.playerDetails, subTextStyle]}>
              #{item.jerseyNumber ?? '?'} • {item.gender}
            </Text>
          </Pressable>
        )}
        ListEmptyComponent={<Text style={[styles.emptyText, subTextStyle]}>No players yet. Add one!</Text>}
      />

      <TouchableOpacity 
        style={[styles.addButton, { backgroundColor: isDark ? '#1e88e5' : '#2196f3' }]} 
        onPress={handleOpenAddModal}
      >
        <Text style={styles.addButtonText}>+</Text>
      </TouchableOpacity>

      <Modal visible={modalVisible} animationType="slide" transparent onRequestClose={handleCloseModal}>
        <View style={styles.modalOverlay}>
          <View style={[styles.modalContent, modalBg]}>
            <Text style={[styles.modalTitle, textStyle]}>{isEditing ? 'Edit Player' : 'New Player'}</Text>
            
            <TextInput
              style={[styles.input, inputBg]}
              placeholder="Player Name"
              placeholderTextColor={placeholderColor}
              value={newPlayerName}
              onChangeText={setNewPlayerName}
              autoFocus
            />
            
            <TextInput
              style={[styles.input, inputBg]}
              placeholder="Jersey Number (-999 to 999)"
              placeholderTextColor={placeholderColor}
              value={newJersey}
              onChangeText={(text) => {
                if (!/^-?\d*$/.test(text)) return;
                if (text === '' || text === '-') { setNewJersey(text); return; }
                const num = parseInt(text, 10);
                if (!isNaN(num) && num >= -999 && num <= 999) setNewJersey(text);
              }}
              keyboardType="numeric"
            />

            <Text style={[styles.label, textStyle]}>Gender</Text>
            <View style={[styles.pickerContainer, { backgroundColor: isDark ? '#333' : '#f9f9f9' }]}>
              <Picker
                selectedValue={newGender}
                onValueChange={(itemValue) => setNewGender(itemValue)}
                dropdownIconColor={isDark ? '#fff' : '#000'}
                style={{ color: isDark ? '#fff' : '#000' }}
              >
                <Picker.Item label="Male" value="male" />
                <Picker.Item label="Female" value="female" />
                <Picker.Item label="Other" value="other" />
              </Picker>
            </View>

            <View style={styles.modalButtons}>
              <TouchableOpacity style={[styles.cancelButton, { backgroundColor: isDark ? '#444' : '#ddd' }]} onPress={handleCloseModal}>
                <Text style={[styles.cancelText, { color: isDark ? '#fff' : '#333' }]}>Cancel</Text>
              </TouchableOpacity>
              {isEditing && (
                <TouchableOpacity style={styles.deleteButton} onPress={handleDeletePlayer}>
                  <Text style={styles.deleteText}>Delete</Text>
                </TouchableOpacity>
              )}
              <TouchableOpacity style={[styles.saveButton, { backgroundColor: isDark ? '#1e88e5' : '#2196f3' }]} onPress={handleSavePlayer}>
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
  title: { fontSize: 32, fontWeight: 'bold', margin: 16, textAlign: 'center' },
  playerItem: { padding: 16, borderBottomWidth: 1, flexDirection: 'row', justifyContent: 'space-between' },
  playerName: { fontSize: 18, fontWeight: '600' },
  playerDetails: { fontSize: 14 },
  emptyText: { textAlign: 'center', marginTop: 40, fontSize: 16 },
  addButton: { position: 'absolute', bottom: 20, right: 20, width: 60, height: 60, borderRadius: 30, justifyContent: 'center', alignItems: 'center', elevation: 5 },
  addButtonText: { color: '#fff', fontSize: 40, fontWeight: 'bold', lineHeight: 40 },
  modalOverlay: { flex: 1, backgroundColor: 'rgba(0,0,0,0.5)', justifyContent: 'center', alignItems: 'center' },
  modalContent: { padding: 20, borderRadius: 12, width: '80%' },
  modalTitle: { fontSize: 20, fontWeight: 'bold', marginBottom: 16 },
  input: { borderWidth: 1, borderRadius: 8, padding: 12, marginBottom: 16, fontSize: 16 },
  label: { fontSize: 16, marginBottom: 8 },
  pickerContainer: { borderRadius: 8, marginBottom: 16, overflow: 'hidden' },
  modalButtons: { flexDirection: 'row', gap: 12 },
  cancelButton: { flex: 1, padding: 12, borderRadius: 8, alignItems: 'center' },
  cancelText: { fontWeight: '600' },
  deleteButton: { flex: 1, padding: 12, backgroundColor: '#f44336', borderRadius: 8, alignItems: 'center' },
  deleteText: { color: '#fff', fontWeight: '600' },
  saveButton: { flex: 1, padding: 12, borderRadius: 8, alignItems: 'center' },
  saveText: { color: '#fff', fontWeight: '600' },
});