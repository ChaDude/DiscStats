import { useState } from 'react';
import { Pressable, StyleSheet, TextInput, ScrollView } from 'react-native';
import { useRouter } from 'expo-router';

import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { storage } from '@/services/storage';
import { Game } from '@/models/types';

export default function NewGameScreen() {
  const [opponentName, setOpponentName] = useState('');
  const [tournamentName, setTournamentName] = useState('');
  const [gamePoint, setGamePoint] = useState(13);
  const [isFirstPointOline, setIsFirstPointOline] = useState(true);
  const router = useRouter();

  const handleCreateGame = async () => {
    if (!opponentName.trim()) {
      alert('Please enter opponent name');
      return;
    }

    const newGame: Game = {
      gameId: `game-${Date.now()}`,
      opponentName,
      tournamentName,
      startDateTime: new Date(),
      gamePoint,
      isFirstPointOline,
      points: [],
      currentLine: [], // TODO: set from team
    };

    const games = await storage.loadGames();
    games.push(newGame);
    await storage.saveGames(games);

    // Navigate back to games or to game scoring
    router.replace('/(tabs)/games');
  };

  return (
    <ScrollView style={styles.container}>
      <ThemedView style={styles.content}>
        <ThemedText type="title">New Game</ThemedText>

        <ThemedView style={styles.field}>
          <ThemedText type="subtitle">Opponent Name</ThemedText>
          <TextInput
            style={styles.input}
            value={opponentName}
            onChangeText={setOpponentName}
            placeholder="Enter opponent name"
          />
        </ThemedView>

        <ThemedView style={styles.field}>
          <ThemedText type="subtitle">Tournament (optional)</ThemedText>
          <TextInput
            style={styles.input}
            value={tournamentName}
            onChangeText={setTournamentName}
            placeholder="Enter tournament name"
          />
        </ThemedView>

        <ThemedView style={styles.field}>
          <ThemedText type="subtitle">Game Point</ThemedText>
          <TextInput
            style={styles.input}
            value={gamePoint.toString()}
            onChangeText={(text) => setGamePoint(parseInt(text) || 13)}
            keyboardType="numeric"
            placeholder="13"
          />
        </ThemedView>

        <ThemedView style={styles.field}>
          <ThemedText type="subtitle">First Point O-Line</ThemedText>
          <Pressable
            style={styles.toggle}
            onPress={() => setIsFirstPointOline(!isFirstPointOline)}
          >
            <ThemedText>{isFirstPointOline ? 'Yes' : 'No'}</ThemedText>
          </Pressable>
        </ThemedView>

        <Pressable style={styles.createButton} onPress={handleCreateGame}>
          <ThemedText type="defaultSemiBold">Create Game</ThemedText>
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