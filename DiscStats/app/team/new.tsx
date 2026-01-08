import { useState } from 'react';
import { Pressable, StyleSheet, TextInput, ScrollView } from 'react-native';
import { useRouter } from 'expo-router';

import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { storage } from '@/services/storage';
import { Team, Player } from '@/models/types';

export default function NewTeamScreen() {
  const [teamName, setTeamName] = useState('');
  const [isMixed, setIsMixed] = useState(false);
  const [displayPlayerNumber, setDisplayPlayerNumber] = useState(true);
  const router = useRouter();

  const handleCreateTeam = async () => {
    if (!teamName.trim()) {
      alert('Please enter team name');
      return;
    }

    const newTeam: Team = {
      teamId: `team-${Date.now()}`,
      name: teamName,
      players: [],
      isMixed,
      isDisplayingPlayerNumber: displayPlayerNumber,
    };

    const teams = await storage.loadTeams();
    teams.push(newTeam);
    await storage.saveTeams(teams);

    router.replace('/(tabs)/teams');
  };

  return (
    <ScrollView style={styles.container}>
      <ThemedView style={styles.content}>
        <ThemedText type="title">New Team</ThemedText>

        <ThemedView style={styles.field}>
          <ThemedText type="subtitle">Team Name</ThemedText>
          <TextInput
            style={styles.input}
            value={teamName}
            onChangeText={setTeamName}
            placeholder="Enter team name"
          />
        </ThemedView>

        <ThemedView style={styles.field}>
          <ThemedText type="subtitle">Mixed Team (4 men, 4 women)</ThemedText>
          <Pressable
            style={styles.toggle}
            onPress={() => setIsMixed(!isMixed)}
          >
            <ThemedText>{isMixed ? 'Yes' : 'No'}</ThemedText>
          </Pressable>
        </ThemedView>

        <ThemedView style={styles.field}>
          <ThemedText type="subtitle">Display Player Numbers</ThemedText>
          <Pressable
            style={styles.toggle}
            onPress={() => setDisplayPlayerNumber(!displayPlayerNumber)}
          >
            <ThemedText>{displayPlayerNumber ? 'Yes' : 'No'}</ThemedText>
          </Pressable>
        </ThemedView>

        <Pressable style={styles.createButton} onPress={handleCreateTeam}>
          <ThemedText type="defaultSemiBold">Create Team</ThemedText>
        </Pressable>
      </ThemedView>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  content: {
    padding: 16,
  },
  field: {
    marginBottom: 16,
  },
  input: {
    borderWidth: 1,
    borderColor: '#ccc',
    padding: 8,
    borderRadius: 4,
    marginTop: 4,
  },
  toggle: {
    padding: 8,
    backgroundColor: 'rgba(0,0,0,0.05)',
    borderRadius: 4,
    marginTop: 4,
  },
  createButton: {
    backgroundColor: '#007AFF',
    padding: 16,
    borderRadius: 8,
    alignItems: 'center',
    marginTop: 20,
  },
});