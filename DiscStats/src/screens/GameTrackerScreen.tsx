// src/screens/GameTrackerScreen.tsx
import React, { useState, useEffect } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, Alert, FlatList, ActivityIndicator } from 'react-native';
import { RouteProp, useRoute, useNavigation } from '@react-navigation/native';
import { Ionicons } from '@expo/vector-icons';

import { RootStackParamList, RootStackNavigationProp } from '../navigation/types';
import { 
  getGameById, 
  getPlayersForTeam, 
  addPoint, 
  getPointById,
  deletePoint,
  deleteGame // <-- Import this
} from '../services/database';
import { Game, Player, Point } from '../models';
import { useTheme } from '../context/ThemeContext';

export default function GameTrackerScreen() {
  const { params } = useRoute<RouteProp<RootStackParamList, 'GameTracker'>>();
  // Use typed navigation to allow .navigate('Home')
  const navigation = useNavigation<RootStackNavigationProp>();
  const { theme, currentScheme } = useTheme();
  const isDark = (theme === 'system' ? currentScheme : theme) === 'dark';

  // --- State ---
  const [loading, setLoading] = useState(true);
  const [game, setGame] = useState<Game | null>(null);
  const [roster, setRoster] = useState<Player[]>([]);
  
  // Game State
  const [currentPoint, setCurrentPoint] = useState<Point | null>(null); 
  const [selectedLine, setSelectedLine] = useState<string[]>([]);
  const [nextPointNumber, setNextPointNumber] = useState(1);
  const [weArePulling, setWeArePulling] = useState(true);

  // --- Init ---
  useEffect(() => {
    loadGameData();
  }, [params.gameId]);

  const loadGameData = async () => {
    try {
      setLoading(true);
      const g = await getGameById(params.gameId);
      if (!g) {
        Alert.alert('Error', 'Game not found');
        navigation.goBack();
        return;
      }
      setGame(g);
      setNextPointNumber((g.points?.length || 0) + 1);
      const players = await getPlayersForTeam(g.ourTeamId);
      setRoster(players);

      // Resume state logic
      if (g.points && g.points.length > 0) {
        const lastPointId = g.points[g.points.length - 1];
        const lastP = await getPointById(lastPointId);
        
        if (lastP && !lastP.scoringTeam) {
          setCurrentPoint(lastP); // Resume active point
        } else {
          // Setup for next point
          const weScoredLast = lastP?.scoringTeam === 'us';
          setWeArePulling(weScoredLast); 
        }
      } else {
        setWeArePulling(g.pullingFirst);
      }
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  // --- Helper: Ratio Logic ---
  const getRequiredRatio = (): { m: number, f: number } | null => {
    if (!game || game.genderRule !== 'ratio_rule_a' || !game.initialRatio) return null;
    if (game.playersPerPoint % 2 === 0) return { m: game.playersPerPoint/2, f: game.playersPerPoint/2 };

    const n = nextPointNumber;
    const isRatioA = (n === 1) || (Math.floor((n - 2) / 2) % 2 === 1);
    const isMaleMajor = game.initialRatio === 'male_majority';
    const targetMaleMajor = isRatioA ? isMaleMajor : !isMaleMajor;
    
    const major = Math.ceil(game.playersPerPoint / 2);
    const minor = Math.floor(game.playersPerPoint / 2);
    return targetMaleMajor ? { m: major, f: minor } : { m: minor, f: major };
  };
  const ratioTarget = getRequiredRatio();

  // --- Navigation Handlers ---

  const handleExitGame = () => {
    // Logic: If game hasn't started (0 points), "Back" means "Discard & Return to Setup".
    // If game has points, "Back" means "Save & Exit to Home".
    const hasPoints = game && game.points && game.points.length > 0;

    if (!hasPoints) {
      Alert.alert(
        'Cancel Game?',
        'Return to setup? This will discard the current game.',
        [
          { text: 'No', style: 'cancel' },
          { 
            text: 'Yes, Discard', 
            style: 'destructive', 
            onPress: async () => {
               if (game) await deleteGame(game.id);
               navigation.goBack(); 
            }
          }
        ]
      );
    } else {
      Alert.alert(
        'Exit Game?',
        'Your progress is saved. Resume later from the Games tab.',
        [
          { text: 'Cancel', style: 'cancel' },
          { 
            text: 'Exit', 
            onPress: () => navigation.navigate('Home') 
          }
        ]
      );
    }
  };

  const handleUndoPointStart = () => {
    if (!currentPoint) return;
    
    if (currentPoint.events && currentPoint.events.length > 0) {
      Alert.alert('Cannot Edit Line', 'Events have already been recorded. Use Undo button to remove them first.');
      return;
    }

    Alert.alert(
      'Edit Line?',
      'This will cancel the current point and let you re-select the line.',
      [
        { text: 'Keep Playing', style: 'cancel' },
        { 
          text: 'Edit Line', 
          onPress: async () => {
            try {
              await deletePoint(currentPoint.id);
              setCurrentPoint(null); // Go back to line selection
            } catch (e) {
              Alert.alert('Error', 'Failed to cancel point');
            }
          } 
        }
      ]
    );
  };

  const handleStartPoint = async () => {
    if (!game) return;
    if (selectedLine.length === 0) {
      Alert.alert('Empty Line', 'Please select players.');
      return;
    }

    try {
      const startingTeam = weArePulling ? 'them' : 'us';
      const pointId = await addPoint(game.id, startingTeam, selectedLine);
      
      setCurrentPoint({
        id: pointId,
        gameId: game.id,
        pointNumber: nextPointNumber,
        startingTeam,
        events: [],
        scoringTeam: null
      });
      // Refresh local game object to include new point so Back button logic updates
      setGame(prev => prev ? ({ ...prev, points: [...prev.points, pointId] }) : null);
    } catch (e) {
      Alert.alert('Error', 'Failed to start point');
    }
  };

  const handleTogglePlayer = (id: string) => {
    setSelectedLine(prev => prev.includes(id) ? prev.filter(p => p !== id) : [...prev, id]);
  };

  // --- Renders ---

  const renderHeader = (title: string, onBack: () => void, backIcon = "arrow-back") => (
    <View style={[styles.header, { backgroundColor: isDark ? '#1e1e1e' : '#f5f5f5' }]}>
      <TouchableOpacity onPress={onBack} style={styles.backButton}>
        <Ionicons name={backIcon as any} size={24} color={isDark ? '#fff' : '#000'} />
      </TouchableOpacity>
      <Text style={[styles.headerTitle, { color: isDark ? '#fff' : '#000' }]}>{title}</Text>
      <View style={{ width: 24 }} />
    </View>
  );

  const renderLineSelection = () => {
    const selectedPlayers = roster.filter(p => selectedLine.includes(p.id));
    const countM = selectedPlayers.filter(p => p.gender === 'male').length;
    const countF = selectedPlayers.filter(p => p.gender === 'female').length;
    
    const countColor = selectedLine.length === game?.playersPerPoint ? '#4CAF50' : '#FF9800';

    return (
      <View style={styles.container}>
        {renderHeader('Select Line', handleExitGame)}

        <View style={[styles.subHeader, { backgroundColor: isDark ? '#1e1e1e' : '#f5f5f5' }]}>
          <Text style={[styles.pointLabel, { color: isDark ? '#ccc' : '#666' }]}>
            Point {nextPointNumber}
          </Text>
          
          <View style={styles.statusRow}>
            <TouchableOpacity onPress={() => setWeArePulling(!weArePulling)} style={styles.pill}>
              <Text style={styles.pillText}>{weArePulling ? 'WE PULL' : 'WE RECEIVE'}</Text>
            </TouchableOpacity>
            
            <View style={[styles.pill, { backgroundColor: countColor }]}>
              <Text style={styles.pillText}>
                Line: {selectedLine.length} / {game?.playersPerPoint}
              </Text>
            </View>
          </View>

          {ratioTarget && (
            <View style={styles.ratioRow}>
              <Text style={[styles.ratioText, { color: countM === ratioTarget.m ? '#4CAF50' : '#F44336' }]}>
                M: {countM}/{ratioTarget.m}
              </Text>
              <Text style={[styles.ratioText, { color: countF === ratioTarget.f ? '#4CAF50' : '#F44336' }]}>
                F: {countF}/{ratioTarget.f}
              </Text>
            </View>
          )}
        </View>

        <FlatList
          data={roster}
          keyExtractor={item => item.id}
          contentContainerStyle={{ padding: 12, paddingBottom: 100 }}
          renderItem={({ item }) => {
            const isSelected = selectedLine.includes(item.id);
            return (
              <TouchableOpacity 
                style={[
                  styles.playerRow, 
                  { 
                    backgroundColor: isSelected ? (isDark ? '#1e88e5' : '#e3f2fd') : (isDark ? '#333' : '#fff'),
                    borderColor: isDark ? '#555' : '#eee'
                  }
                ]}
                onPress={() => handleTogglePlayer(item.id)}
              >
                <View style={styles.playerInfo}>
                  <Text style={[styles.jersey, { color: isDark ? '#bbb' : '#666' }]}>#{item.jerseyNumber}</Text>
                  <Text style={[styles.name, { color: isDark ? '#fff' : '#000' }]}>{item.name}</Text>
                  <Text style={[styles.gender, { color: isDark ? '#888' : '#999' }]}>({item.gender.charAt(0).toUpperCase()})</Text>
                </View>
                {isSelected && <Ionicons name="checkmark-circle" size={24} color={isDark ? '#fff' : '#2196F3'} />}
              </TouchableOpacity>
            );
          }}
        />

        <View style={[styles.footer, { backgroundColor: isDark ? '#121212' : '#fff' }]}>
          <TouchableOpacity 
            style={[styles.startBtn, { opacity: selectedLine.length > 0 ? 1 : 0.5 }]}
            disabled={selectedLine.length === 0}
            onPress={handleStartPoint}
          >
            <Text style={styles.startBtnText}>Start Point</Text>
          </TouchableOpacity>
        </View>
      </View>
    );
  };

  const renderActiveGame = () => (
    <View style={styles.container}>
      {renderHeader(`Point ${currentPoint?.pointNumber}`, handleUndoPointStart, "create-outline")}
      
      <View style={styles.center}>
        <Text style={{ color: isDark ? '#fff' : '#000', fontSize: 24, marginBottom: 20 }}>
          Game in Progress
        </Text>
        <Text style={{ color: isDark ? '#bbb' : '#666', textAlign: 'center', paddingHorizontal: 40 }}>
          (The active game tracking interface will go here in the next step)
        </Text>
      </View>
    </View>
  );

  if (loading) {
    return (
      <View style={[styles.center, { backgroundColor: isDark ? '#121212' : '#fff' }]}>
        <ActivityIndicator size="large" color="#2196F3" />
      </View>
    );
  }

  return (
    <View style={[styles.container, { backgroundColor: isDark ? '#121212' : '#fff' }]}>
      {currentPoint ? renderActiveGame() : renderLineSelection()}
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  center: { flex: 1, justifyContent: 'center', alignItems: 'center' },
  
  header: { 
    flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', 
    paddingHorizontal: 16, paddingVertical: 12, 
    borderBottomWidth: 1, borderBottomColor: '#333' 
  },
  headerTitle: { fontSize: 18, fontWeight: 'bold' },
  backButton: { padding: 4 },
  
  subHeader: { paddingVertical: 12, alignItems: 'center', borderBottomWidth: 1, borderBottomColor: '#ccc' },
  pointLabel: { fontSize: 14, textTransform: 'uppercase', letterSpacing: 1, marginBottom: 8 },
  
  statusRow: { flexDirection: 'row', gap: 12, marginBottom: 8 },
  pill: { paddingVertical: 6, paddingHorizontal: 12, borderRadius: 16, backgroundColor: '#2196F3' },
  pillText: { color: '#fff', fontWeight: 'bold', fontSize: 14 },
  
  ratioRow: { flexDirection: 'row', gap: 16 },
  ratioText: { fontWeight: '600', fontSize: 14 },

  playerRow: { 
    flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between',
    padding: 16, marginBottom: 8, borderRadius: 8, borderWidth: 1
  },
  playerInfo: { flexDirection: 'row', alignItems: 'center', gap: 10 },
  jersey: { fontSize: 16, fontWeight: 'bold', width: 30 },
  name: { fontSize: 16, fontWeight: '500' },
  gender: { fontSize: 14 },

  footer: { padding: 16, borderTopWidth: 1, borderTopColor: '#333' },
  startBtn: { backgroundColor: '#4CAF50', padding: 16, borderRadius: 12, alignItems: 'center' },
  startBtnText: { color: '#fff', fontSize: 18, fontWeight: 'bold' },
});