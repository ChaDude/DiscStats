// src/screens/GameTrackerScreen.tsx
/**
 * Live game tracking screen - MVP version.
 * Displays score, possession, event buttons (possession-aware), undo, and override.
 * Uses mockData for state - fully cross-platform (flexbox, no fixed sizes).
 */
import React, { useState, useEffect } from 'react';
import { View, Text, TouchableOpacity, StyleSheet, ScrollView, Alert } from 'react-native';

import {
  createGame,
  addPointToGame,
  addEventToPoint,
  undoLastEvent,
  overridePossession,
  getCurrentPossession,
  mockPlayers,
} from '../services/mockData';
import { Game, Point, EventType } from '../models';

// Simple possession-aware event buttons
const usOffenseEvents: { label: string; type: EventType }[] = [
  { label: 'Completion', type: 'completion' },
  { label: 'Throwaway', type: 'throwaway' },
  { label: 'Goal!', type: 'goal' },
];

const themDefenseEvents: { label: string; type: EventType }[] = [
  { label: 'Block', type: 'block' },
  { label: 'Callahan', type: 'callahan' },
];

const commonEvents: { label: string; type: EventType }[] = [
  { label: 'Timeout', type: 'timeout' },
  { label: 'Foul', type: 'foul' },
];

export default function GameTrackerScreen() {
  const [game, setGame] = useState<Game | null>(null);
  const [currentPoint, setCurrentPoint] = useState<Point | null>(null);

  // Auto-create game & first point on mount
  useEffect(() => {
    const newGame = createGame('team-us', 'team-opp');
    setGame(newGame);

    const firstPoint = addPointToGame(newGame.id, 'us');
    setCurrentPoint(firstPoint);
  }, []);

  const possession = currentPoint ? getCurrentPossession(currentPoint) : 'us';

  const handleEvent = (type: EventType) => {
    if (!currentPoint) return;

    // Placeholder: use first us player for now (later: selector)
    const actorId = mockPlayers[0]?.id;

    try {
      addEventToPoint(currentPoint, type, actorId);
      setCurrentPoint({ ...currentPoint }); // Force re-render
    } catch (e) {
      Alert.alert('Error', (e as Error).message);
    }
  };

  const handleUndo = () => {
    if (!currentPoint || currentPoint.events.length === 0) {
      Alert.alert('Nothing to undo');
      return;
    }
    try {
      undoLastEvent(currentPoint);
      setCurrentPoint({ ...currentPoint });
    } catch (e) {
      Alert.alert('Undo failed', (e as Error).message);
    }
  };

  const handleOverride = (newPoss: 'us' | 'them') => {
    if (!currentPoint) return;
    try {
      overridePossession(currentPoint, newPoss);
      setCurrentPoint({ ...currentPoint });
    } catch (e) {
      Alert.alert('Override failed', (e as Error).message);
    }
  };

  if (!game || !currentPoint) {
    return <Text style={styles.loading}>Loading game...</Text>;
  }

  return (
    <ScrollView style={styles.container}>
      {/* Header / Score */}
      <View style={styles.header}>
        <Text style={styles.title}>Disc Hawks vs Rival Flyers</Text>
        <Text style={styles.score}>
          {game.ourScore} - {game.opponentScore}
        </Text>
        <Text style={styles.pointInfo}>
          Point #{currentPoint.pointNumber} • Possession:{' '}
          <Text style={{ color: possession === 'us' ? '#2196F3' : '#F44336' }}>
            {possession.toUpperCase()}
          </Text>
          {currentPoint.forcedPossession && ' (forced)'}
        </Text>
      </View>

      {/* Event Buttons - Possession Aware */}
      <View style={styles.buttonContainer}>
        {possession === 'us' ? (
          usOffenseEvents.map((e) => (
            <TouchableOpacity key={e.type} style={styles.button} onPress={() => handleEvent(e.type)}>
              <Text style={styles.buttonText}>{e.label}</Text>
            </TouchableOpacity>
          ))
        ) : (
          themDefenseEvents.map((e) => (
            <TouchableOpacity key={e.type} style={styles.button} onPress={() => handleEvent(e.type)}>
              <Text style={styles.buttonText}>{e.label}</Text>
            </TouchableOpacity>
          ))
        )}

        {/* Common buttons always visible */}
        {commonEvents.map((e) => (
          <TouchableOpacity key={e.type} style={styles.commonButton} onPress={() => handleEvent(e.type)}>
            <Text style={styles.commonButtonText}>{e.label}</Text>
          </TouchableOpacity>
        ))}
      </View>

      {/* Controls */}
      <View style={styles.controls}>
        <TouchableOpacity style={styles.controlButton} onPress={handleUndo}>
          <Text style={styles.controlText}>Undo Last</Text>
        </TouchableOpacity>

        <View style={styles.overrideRow}>
          <TouchableOpacity style={[styles.overrideButton, { backgroundColor: '#2196F3' }]} onPress={() => handleOverride('us')}>
            <Text style={styles.overrideText}>Force Us</Text>
          </TouchableOpacity>
          <TouchableOpacity style={[styles.overrideButton, { backgroundColor: '#F44336' }]} onPress={() => handleOverride('them')}>
            <Text style={styles.overrideText}>Force Them</Text>
          </TouchableOpacity>
        </View>
      </View>

      {/* Debug Info */}
      <Text style={styles.debug}>
        Events in point: {currentPoint.events.length}
      </Text>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#fff' },
  loading: { flex: 1, textAlign: 'center', textAlignVertical: 'center', fontSize: 18 },

  header: { padding: 20, alignItems: 'center', backgroundColor: '#f5f5f5' },
  title: { fontSize: 24, fontWeight: 'bold', marginBottom: 8 },
  score: { fontSize: 48, fontWeight: 'bold', marginVertical: 8 },
  pointInfo: { fontSize: 18, color: '#666' },

  buttonContainer: { padding: 20, gap: 12 },
  button: {
    backgroundColor: '#2196F3',
    padding: 16,
    borderRadius: 8,
    alignItems: 'center',
  },
  buttonText: { color: '#fff', fontSize: 18, fontWeight: '600' },

  commonButton: {
    backgroundColor: '#9E9E9E',
    padding: 12,
    borderRadius: 8,
    alignItems: 'center',
  },
  commonButtonText: { color: '#fff', fontSize: 16 },

  controls: { padding: 20, gap: 12 },
  controlButton: {
    backgroundColor: '#FF5722',
    padding: 16,
    borderRadius: 8,
    alignItems: 'center',
  },
  controlText: { color: '#fff', fontSize: 18, fontWeight: '600' },

  overrideRow: { flexDirection: 'row', justifyContent: 'space-between', gap: 12 },
  overrideButton: { flex: 1, padding: 16, borderRadius: 8, alignItems: 'center' },
  overrideText: { color: '#fff', fontSize: 16, fontWeight: '600' },

  debug: { textAlign: 'center', color: '#888', padding: 20, fontSize: 14 },
});