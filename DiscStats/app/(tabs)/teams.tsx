import { useEffect, useState } from 'react';
import { FlatList, Pressable, StyleSheet } from 'react-native';
import { useRouter } from 'expo-router';

import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { IconSymbol } from '@/components/ui/icon-symbol';
import { storage } from '@/services/storage';
import { Team } from '@/models/types';

export default function TeamsScreen() {
  const [teams, setTeams] = useState<Team[]>([]);
  const router = useRouter();

  useEffect(() => {
    loadTeams();
  }, []);

  const loadTeams = async () => {
    const loadedTeams = await storage.loadTeams();
    setTeams(loadedTeams);
  };

  const handleAddTeam = () => {
    router.push('/team/new');
  };

  const renderTeamItem = ({ item }: { item: Team }) => (
    <Pressable style={styles.teamItem} onPress={() => alert(`Selected ${item.name}`)}>
      <ThemedText type="defaultSemiBold">{item.name}</ThemedText>
      <ThemedText>{item.players.length} players</ThemedText>
    </Pressable>
  );

  return (
    <ThemedView style={styles.container}>
      <ThemedView style={styles.header}>
        <ThemedText type="title">Teams</ThemedText>
        <Pressable style={styles.addButton} onPress={handleAddTeam}>
          <IconSymbol name="plus" size={24} color="black" />
        </Pressable>
      </ThemedView>
      <FlatList
        data={teams}
        renderItem={renderTeamItem}
        keyExtractor={(item) => item.teamId}
        ListEmptyComponent={<ThemedText>No teams yet. Add your first team!</ThemedText>}
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
  teamItem: {
    padding: 16,
    marginBottom: 8,
    borderRadius: 8,
    backgroundColor: 'rgba(0,0,0,0.05)',
  },
});
