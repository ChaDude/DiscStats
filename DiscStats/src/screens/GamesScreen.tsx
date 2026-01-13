import React from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { useTheme } from '../context/ThemeContext';

export default function GamesScreen() {
  const { theme, currentScheme } = useTheme();
  const isDark = (theme === 'system' ? currentScheme : theme) === 'dark';

  return (
    <View style={[styles.container, { backgroundColor: isDark ? '#121212' : '#fff' }]}>
      <Text style={[styles.title, { color: isDark ? '#fff' : '#000' }]}>Games</Text>
      <Text style={{ color: isDark ? '#bbb' : '#666' }}>Coming soon: Game list, new game, details</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, justifyContent: 'center', alignItems: 'center' },
  title: { fontSize: 32, fontWeight: 'bold', marginBottom: 16 },
});