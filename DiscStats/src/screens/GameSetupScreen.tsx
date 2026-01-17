// src/screens/GameSetupScreen.tsx
import React, { useState, useEffect } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, Alert, TextInput, ScrollView, Switch, TextStyle, ViewStyle } from 'react-native';
import { Picker } from '@react-native-picker/picker';
import { useNavigation } from '@react-navigation/native';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';

import { RootStackParamList } from '../navigation/types';
import { getTeams, createGame } from '../services/database';
import { Team } from '../models';
import { useTheme } from '../context/ThemeContext';

export default function GameSetupScreen() {
  const navigation = useNavigation<NativeStackNavigationProp<RootStackParamList>>();
  const { theme, currentScheme } = useTheme();
  const isDark = (theme === 'system' ? currentScheme : theme) === 'dark';

  // --- State ---
  const [teams, setTeams] = useState<Team[]>([]);
  const [ourTeamId, setOurTeamId] = useState<string>('');
  const [opponentName, setOpponentName] = useState('');
  const [tournamentName, setTournamentName] = useState('');
  const [scoreLimit, setScoreLimit] = useState('15');
  
  // Format
  const [playersPerPoint, setPlayersPerPoint] = useState(7);
  const [pullingFirst, setPullingFirst] = useState(true); // True = Defense start
  
  // Gender Rules
  const [genderRule, setGenderRule] = useState<'none' | 'ratio_rule_a'>('none');
  const [ratioAMajority, setRatioAMajority] = useState<'male' | 'female'>('male'); 

  useEffect(() => {
    loadTeams();
  }, []);

  const loadTeams = async () => {
    const loaded = await getTeams();
    setTeams(loaded);
    if (loaded.length > 0) setOurTeamId(loaded[0].id);
  };

  const handleStartGame = async () => {
    if (!ourTeamId || !opponentName.trim()) {
      Alert.alert('Missing Info', 'Please select a team and enter an opponent name.');
      return;
    }

    try {
      const gameId = await createGame(ourTeamId, opponentName.trim(), {
        tournamentName: tournamentName.trim() || undefined,
        scoreLimit: parseInt(scoreLimit) || undefined,
        playersPerPoint,
        genderRule,
        // Only save initialRatio if Rule A is active AND it's an odd-number format
        initialRatio: (genderRule === 'ratio_rule_a' && (playersPerPoint % 2 !== 0))
          ? (ratioAMajority === 'male' ? 'male_majority' : 'female_majority')
          : undefined,
        pullingFirst
      });
      navigation.replace('GameTracker', { gameId });
    } catch (e) {
      Alert.alert('Error', 'Failed to create game');
    }
  };

  // --- Helper to Render Ratio Choice ---
  const renderRatioSection = () => {
    if (genderRule !== 'ratio_rule_a') return null;

    const isEven = playersPerPoint % 2 === 0;
    const half = playersPerPoint / 2;

    if (isEven) {
      return (
        <View style={styles.infoBox}>
          <Text style={{ color: isDark ? '#ccc' : '#666', fontStyle: 'italic' }}>
            Even format ({playersPerPoint}v{playersPerPoint}). Ratio fixed at {half}M : {half}F.
          </Text>
        </View>
      );
    }

    // Odd Logic (5v5 or 7v7)
    const majorityVal = Math.ceil(playersPerPoint / 2);
    const minorityVal = Math.floor(playersPerPoint / 2);
    
    // Dynamic Styles for Segmented Control
    const activeSegmentStyle: ViewStyle = { backgroundColor: '#2196F3' };
    const inactiveSegmentStyle: ViewStyle = { backgroundColor: isDark ? '#333' : '#eee' };
    const activeTextStyle: TextStyle = { color: '#fff' };
    const inactiveTextStyle: TextStyle = { color: isDark ? '#fff' : '#000' };

    return (
      <View style={styles.subSection}>
        <Text style={[styles.label, { fontSize: 14, color: isDark ? '#ccc' : '#555', marginBottom: 8 }]}>
          Ratio for Point 1 (Ratio A):
        </Text>
        <View style={styles.segmentedContainer}>
          <TouchableOpacity 
            style={[styles.segmentBtn, ratioAMajority === 'male' ? activeSegmentStyle : inactiveSegmentStyle]}
            onPress={() => setRatioAMajority('male')}
          >
            <Text style={[styles.segmentText, ratioAMajority === 'male' ? activeTextStyle : inactiveTextStyle]}>
              {majorityVal} Male / {minorityVal} Female
            </Text>
          </TouchableOpacity>
          <TouchableOpacity 
            style={[styles.segmentBtn, ratioAMajority === 'female' ? activeSegmentStyle : inactiveSegmentStyle]}
            onPress={() => setRatioAMajority('female')}
          >
            <Text style={[styles.segmentText, ratioAMajority === 'female' ? activeTextStyle : inactiveTextStyle]}>
              {minorityVal} Male / {majorityVal} Female
            </Text>
          </TouchableOpacity>
        </View>
        <Text style={{ marginTop: 8, fontSize: 12, color: isDark ? '#888' : '#666' }}>
          Sequence: A, B, B, A, A...
        </Text>
      </View>
    );
  };

  // Styles
  const bgStyle = { backgroundColor: isDark ? '#121212' : '#fff' };
  const textStyle = { color: isDark ? '#fff' : '#000' };
  const inputBg = { backgroundColor: isDark ? '#333' : '#f5f5f5', color: isDark ? '#fff' : '#000' };
  const placeholderColor = isDark ? '#888' : '#999';

  return (
    <ScrollView style={[styles.container, bgStyle]} contentContainerStyle={{ padding: 24, paddingBottom: 60 }}>
      <Text style={[styles.title, textStyle]}>New Game Setup</Text>

      {/* --- BASICS --- */}
      <Text style={[styles.sectionTitle, textStyle]}>Match Info</Text>
      
      <Text style={[styles.label, textStyle]}>Our Team</Text>
      <View style={[styles.pickerContainer, { backgroundColor: isDark ? '#333' : '#f0f0f0' }]}>
        <Picker
          selectedValue={ourTeamId}
          onValueChange={setOurTeamId}
          style={{ color: isDark ? '#fff' : '#000' }}
          dropdownIconColor={isDark ? '#fff' : '#000'}
        >
          {teams.map(t => <Picker.Item key={t.id} label={t.name} value={t.id} />)}
        </Picker>
      </View>

      <Text style={[styles.label, textStyle, { marginTop: 12 }]}>Opponent Name</Text>
      <TextInput 
        style={[styles.input, inputBg]} 
        placeholder="e.g. Rival FC" 
        placeholderTextColor={placeholderColor}
        value={opponentName} 
        onChangeText={setOpponentName} 
      />

      <Text style={[styles.label, textStyle, { marginTop: 12 }]}>Tournament / Event</Text>
      <TextInput 
        style={[styles.input, inputBg]} 
        placeholder="e.g. Regionals 2026" 
        placeholderTextColor={placeholderColor}
        value={tournamentName} 
        onChangeText={setTournamentName} 
      />

      <Text style={[styles.label, textStyle, { marginTop: 12 }]}>Score Limit</Text>
      <TextInput 
        style={[styles.input, inputBg]} 
        placeholder="15" 
        placeholderTextColor={placeholderColor}
        value={scoreLimit} 
        onChangeText={setScoreLimit} 
        keyboardType="numeric"
      />

      {/* --- FORMAT --- */}
      <Text style={[styles.sectionTitle, textStyle]}>Format</Text>
      
      <Text style={[styles.label, textStyle]}>Players on Field</Text>
      <View style={styles.segmentedContainer}>
        {[4, 5, 6, 7].map(num => (
          <TouchableOpacity 
            key={num} 
            style={[
              styles.segmentBtn, 
              playersPerPoint === num ? { backgroundColor: '#2196F3' } : { backgroundColor: isDark ? '#333' : '#eee' }
            ]}
            onPress={() => setPlayersPerPoint(num)}
          >
            <Text style={[
              styles.segmentText, 
              playersPerPoint === num ? { color: '#fff' } : textStyle
            ]}>{num}</Text>
          </TouchableOpacity>
        ))}
      </View>

      <Text style={[styles.label, textStyle, { marginTop: 16 }]}>Starting Possession</Text>
      <View style={styles.segmentedContainer}>
        <TouchableOpacity 
          style={[
            styles.segmentBtn, 
            pullingFirst ? { backgroundColor: '#2196F3' } : { backgroundColor: isDark ? '#333' : '#eee' }
          ]}
          onPress={() => setPullingFirst(true)}
        >
          <Text style={[styles.segmentText, pullingFirst ? { color: '#fff' } : textStyle]}>We Pull</Text>
        </TouchableOpacity>
        <TouchableOpacity 
          style={[
            styles.segmentBtn, 
            !pullingFirst ? { backgroundColor: '#2196F3' } : { backgroundColor: isDark ? '#333' : '#eee' }
          ]}
          onPress={() => setPullingFirst(false)}
        >
          <Text style={[styles.segmentText, !pullingFirst ? { color: '#fff' } : textStyle]}>We Receive</Text>
        </TouchableOpacity>
      </View>

      {/* --- GENDER RULES --- */}
      <Text style={[styles.sectionTitle, textStyle]}>Gender Rules</Text>
      
      <View style={styles.row}>
        <Text style={[styles.label, textStyle, { flex: 1 }]}>Use WFDF Ratio Rule A?</Text>
        <Switch 
          value={genderRule === 'ratio_rule_a'}
          onValueChange={(val) => setGenderRule(val ? 'ratio_rule_a' : 'none')}
        />
      </View>

      {renderRatioSection()}

      {/* --- START --- */}
      <TouchableOpacity style={styles.startButton} onPress={handleStartGame}>
        <Text style={styles.startButtonText}>Start Game</Text>
      </TouchableOpacity>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  title: { fontSize: 32, fontWeight: 'bold', marginBottom: 8, textAlign: 'center' },
  pickerContainer: { borderRadius: 8, overflow: 'hidden' },
  input: { borderRadius: 8, padding: 12, fontSize: 16 },
  label: { fontSize: 16, marginBottom: 8 },
  sectionTitle: { fontSize: 18, fontWeight: 'bold', marginTop: 24, marginBottom: 12 },
  
  row: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', marginBottom: 12 },
  subSection: { marginLeft: 12, paddingLeft: 12, borderLeftWidth: 2, borderLeftColor: '#2196F3', marginTop: 8 },
  infoBox: { padding: 12, backgroundColor: 'rgba(128,128,128,0.1)', borderRadius: 8, marginTop: 8 },
  
  segmentedContainer: { flexDirection: 'row', borderRadius: 8, overflow: 'hidden', height: 44 },
  segmentBtn: { flex: 1, justifyContent: 'center', alignItems: 'center' },
  segmentText: { fontWeight: '600' },

  startButton: {
    marginTop: 40,
    backgroundColor: '#2196F3',
    paddingVertical: 16,
    borderRadius: 12,
    alignItems: 'center',
    elevation: 4
  },
  startButtonText: { color: '#fff', fontSize: 20, fontWeight: 'bold' }
});