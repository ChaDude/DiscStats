// src/screens/HomeScreen.tsx
/**
 * Home screen of the DiscStats app.
 * Displays the main title and a button to start a new game.
 * Fully cross-platform (flexbox, no fixed sizes).
 */
import React from 'react';
import { View, Text, TouchableOpacity, StyleSheet } from 'react-native';
import { useNavigation } from '@react-navigation/native';

import { RootStackNavigationProp } from '../navigation/types';
import { useTheme } from '../context/ThemeContext';

export default function HomeScreen() {
  const navigation = useNavigation<RootStackNavigationProp>();
  const { theme, currentScheme } = useTheme();
  const effectiveScheme = theme === 'system' ? currentScheme : theme;
  const isDark = effectiveScheme === 'dark';

  return (
    <View style={[
      styles.container,
      { backgroundColor: isDark ? '#121212' : '#fff' }
    ]}>
      <Text style={[
        styles.title,
        { color: isDark ? '#ffffff' : '#333333' }
      ]}>DiscStats</Text>
      <Text style={[
        styles.subtitle,
        { color: isDark ? '#bbbbbb' : '#666666' }
      ]}>
        Ultimate Frisbee Game Tracker
      </Text>

      <TouchableOpacity
        style={[
          styles.startButton,
          { backgroundColor: isDark ? '#1e88e5' : '#2196f3' }
        ]}
        onPress={() => navigation.navigate('GameTracker')}
      >
        <Text style={styles.buttonText}>Start New Game</Text>
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 20,
  },
  title: {
    fontSize: 48,
    fontWeight: 'bold',
    marginBottom: 8,
  },
  subtitle: {
    fontSize: 20,
    marginBottom: 40,
    textAlign: 'center',
  },
  startButton: {
    paddingVertical: 16,
    paddingHorizontal: 40,
    borderRadius: 12,
    elevation: 3,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.25,
    shadowRadius: 3.84,
  },
  buttonText: {
    color: '#ffffff',
    fontSize: 20,
    fontWeight: '600',
  },
});