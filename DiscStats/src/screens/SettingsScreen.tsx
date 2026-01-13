// src/screens/SettingsScreen.tsx
/**
 * Settings / Preferences screen.
 * Toggle light/dark/system mode (applies globally).
 */
import React from 'react';
import { View, Text, Switch, StyleSheet, Alert } from 'react-native';
import { useTheme } from '../context/ThemeContext'; //

export default function SettingsScreen() {
  const { theme, setTheme, currentScheme } = useTheme();

  // Determine effective theme for styling this specific screen
  const isDark = (theme === 'system' ? currentScheme : theme) === 'dark';

  const handleThemeChange = async (newTheme: 'light' | 'dark' | 'system') => {
    await setTheme(newTheme);
    // Optional: You can remove the alert if the UI updates instantly and clearly enough
  };

  // Dynamic Styles
  const containerStyle = { backgroundColor: isDark ? '#121212' : '#fff' };
  const textStyle = { color: isDark ? '#fff' : '#333' };
  const subTextStyle = { color: isDark ? '#bbb' : '#666' };
  const sectionTitleStyle = { color: isDark ? '#90caf9' : '#333' };
  const rowStyle = { borderBottomColor: isDark ? '#333' : '#eee' };

  return (
    <View style={[styles.container, containerStyle]}>
      
      {/* Theme Section */}
      <View style={styles.section}>
        <Text style={[styles.sectionTitle, sectionTitleStyle]}>Appearance</Text>
        
        <View style={[styles.optionRow, rowStyle]}>
          <Text style={[styles.optionLabel, textStyle]}>Light Mode</Text>
          <Switch 
            value={theme === 'light'} 
            onValueChange={() => handleThemeChange('light')} 
          />
        </View>
        
        <View style={[styles.optionRow, rowStyle]}>
          <Text style={[styles.optionLabel, textStyle]}>Dark Mode</Text>
          <Switch 
            value={theme === 'dark'} 
            onValueChange={() => handleThemeChange('dark')} 
          />
        </View>
        
        <View style={[styles.optionRow, rowStyle]}>
          <Text style={[styles.optionLabel, textStyle]}>System Default</Text>
          <Switch 
            value={theme === 'system'} 
            onValueChange={() => handleThemeChange('system')} 
          />
        </View>
      </View>

      <View style={styles.section}>
        <Text style={[styles.sectionTitle, sectionTitleStyle]}>About</Text>
        <View style={[styles.optionRow, rowStyle]}>
            <Text style={[styles.optionLabel, textStyle]}>Version</Text>
            <Text style={[styles.optionLabel, subTextStyle]}>1.0.0</Text>
        </View>
      </View>

    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, padding: 20 },
  section: { marginBottom: 32 },
  sectionTitle: { fontSize: 20, fontWeight: '600', marginBottom: 12 },
  optionRow: { 
    flexDirection: 'row', 
    justifyContent: 'space-between', 
    alignItems: 'center', 
    paddingVertical: 12, 
    borderBottomWidth: 1 
  },
  optionLabel: { fontSize: 16 },
});