import { useEffect, useState } from 'react';
import { FlatList, Pressable, StyleSheet } from 'react-native';
import { useRouter } from 'expo-router';

import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { IconSymbol } from '@/components/ui/icon-symbol';
import { storage } from '@/services/storage';
import { Game } from '@/models/types';

export default function GamesScreen() {
  const router = useRouter();
  const [games, setGames] = useState<Game[]>([]);

  useEffect(() => {
    loadGames();
  }, []);

  const loadGames = async () => {
    const loadedGames = await storage.loadGames();
    setGames(loadedGames);
  };

  const handleAddGame = () => {
    router.push('/game/new');
  };

  const renderGameItem = ({ item }: { item: Game }) => (
    <Pressable style={styles.gameItem} onPress={() => alert(`Selected game vs ${item.opponentName}`)}>
      <ThemedText type="defaultSemiBold">{item.opponentName}</ThemedText>
      <ThemedText>{item.points.length} points</ThemedText>
    </Pressable>
  );

  return (
    <ThemedView style={styles.container}>
      <ThemedView style={styles.header}>
        <ThemedText type="title">Games</ThemedText>
        <Pressable style={styles.addButton} onPress={handleAddGame}>
          <IconSymbol name="plus" size={24} color="black" />
        </Pressable>
      </ThemedView>
      <FlatList
        data={games}
        renderItem={renderGameItem}
        keyExtractor={(item) => item.gameId}
        ListEmptyComponent={<ThemedText>No games yet. Add your first game!</ThemedText>}
        contentContainerStyle={styles.listContainer}
      />
    </ThemedView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 16,
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 16,
  },
  addButton: {
    padding: 8,
  },
  listContainer: {
    flexGrow: 1,
  },
  gameItem: {
    padding: 16,
    marginBottom: 8,
    borderRadius: 8,
    backgroundColor: 'rgba(0,0,0,0.05)',
  },
});
