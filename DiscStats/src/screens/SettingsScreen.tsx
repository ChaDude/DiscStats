// src/screens/SettingsScreen.tsx
/**
 * Settings / Preferences screen.
 * Toggle light/dark/system mode (applies globally).
 */
import React, { useState, useEffect } from 'react';
import { View, Text, Switch, StyleSheet, TouchableOpacity, Alert } from 'react-native';
import { useTheme } from '../context/ThemeContext';

export default function SettingsScreen() {
  const { theme, setTheme } = useTheme();

  const handleThemeChange = async (newTheme: 'light' | 'dark' | 'system') => {
    await setTheme(newTheme);
    Alert.alert('Theme Updated', 'Restart app for full effect (or toggle tabs to see change)');
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Preferences</Text>

      {/* Theme Section */}
      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Appearance</Text>
        <View style={styles.optionRow}>
          <Text style={styles.optionLabel}>Light Mode</Text>
          <Switch value={theme === 'light'} onValueChange={() => handleThemeChange('light')} />
        </View>
        <View style={styles.optionRow}>
          <Text style={styles.optionLabel}>Dark Mode</Text>
          <Switch value={theme === 'dark'} onValueChange={() => handleThemeChange('dark')} />
        </View>
        <View style={styles.optionRow}>
          <Text style={styles.optionLabel}>System Default</Text>
          <Switch value={theme === 'system'} onValueChange={() => handleThemeChange('system')} />
        </View>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#fff', padding: 20 },
  title: { fontSize: 32, fontWeight: 'bold', marginBottom: 24, textAlign: 'center' },
  section: { marginBottom: 32 },
  sectionTitle: { fontSize: 20, fontWeight: '600', marginBottom: 12 },
  optionRow: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', paddingVertical: 12, borderBottomWidth: 1, borderBottomColor: '#eee' },
  optionLabel: { fontSize: 16, color: '#333' },
});