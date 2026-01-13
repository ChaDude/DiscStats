// src/screens/PlayerProfileScreen.tsx
import React, { useState, useCallback } from 'react';
import { View, Text, TouchableOpacity, StyleSheet } from 'react-native';
import { RouteProp, useNavigation, useFocusEffect } from '@react-navigation/native';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';

import { RootStackParamList } from '../navigation/types';
import { Player } from '../models';
import { getPlayerById } from '../services/database'; 
import { useTheme } from '../context/ThemeContext'; //

type Props = {
  route: RouteProp<RootStackParamList, 'PlayerProfile'>;
};

export default function PlayerProfileScreen({ route }: Props) {
  const { player: initialPlayer, teamName, teamId } = route.params;
  const [player, setPlayer] = useState<Player>(initialPlayer);
  const navigation = useNavigation<NativeStackNavigationProp<RootStackParamList>>();

  // Theme Hooks
  const { theme, currentScheme } = useTheme();
  const isDark = (theme === 'system' ? currentScheme : theme) === 'dark';

  useFocusEffect(
    useCallback(() => {
      let isActive = true;
      const refreshPlayer = async () => {
        try {
          const freshData = await getPlayerById(initialPlayer.id);
          if (isActive && freshData) setPlayer(freshData);
        } catch (error) {
          console.error("Failed to refresh player", error);
        }
      };
      refreshPlayer();
      return () => { isActive = false; };
    }, [initialPlayer.id])
  );

  const handleEdit = () => {
    navigation.navigate('TeamDetails', {
      teamId,
      teamName,
      editingPlayerId: player.id 
    });
  };

  const containerStyle = { backgroundColor: isDark ? '#121212' : '#fff' };
  const textStyle = { color: isDark ? '#fff' : '#000' };
  const labelStyle = { color: isDark ? '#ddd' : '#333' };
  const valueStyle = { color: isDark ? '#aaa' : '#666' };
  const borderStyle = { borderBottomColor: isDark ? '#333' : '#eee' };

  return (
    <View style={[styles.container, containerStyle]}>
      <Text style={[styles.title, textStyle]}>{player.name}</Text>

      <View style={[styles.detailRow, borderStyle]}>
        <Text style={[styles.label, labelStyle]}>Jersey Number:</Text>
        <Text style={[styles.value, valueStyle]}>#{player.jerseyNumber ?? 'N/A'}</Text>
      </View>

      <View style={[styles.detailRow, borderStyle]}>
        <Text style={[styles.label, labelStyle]}>Gender:</Text>
        <Text style={[styles.value, valueStyle]}>{player.gender}</Text>
      </View>

      <View style={[styles.detailRow, borderStyle]}>
        <Text style={[styles.label, labelStyle]}>Team:</Text>
        <Text style={[styles.value, valueStyle]}>{teamName}</Text>
      </View>

      <TouchableOpacity 
        style={[styles.editButton, { backgroundColor: isDark ? '#1e88e5' : '#2196f3' }]} 
        onPress={handleEdit}
      >
        <Text style={styles.editButtonText}>Edit Player</Text>
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, padding: 20 },
  title: { fontSize: 32, fontWeight: 'bold', textAlign: 'center', marginVertical: 20 },
  detailRow: { flexDirection: 'row', justifyContent: 'space-between', paddingVertical: 12, borderBottomWidth: 1 },
  label: { fontSize: 18, fontWeight: '600' },
  value: { fontSize: 18 },
  editButton: {
    marginTop: 40,
    padding: 16,
    borderRadius: 12,
    alignItems: 'center',
  },
  editButtonText: { color: '#fff', fontSize: 18, fontWeight: '600' },
});