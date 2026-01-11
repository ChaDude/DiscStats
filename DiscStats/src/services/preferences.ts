// src/services/preferences.ts
/**
 * Simple AsyncStorage wrapper for app preferences.
 * Persists user settings across sessions.
 */
import AsyncStorage from '@react-native-async-storage/async-storage';

const KEYS = {
  THEME: 'theme', // 'light' | 'dark' | 'system'
  AUTO_REFRESH: 'autoRefresh', // boolean
} as const;

export type ThemeMode = 'light' | 'dark' | 'system';

/** Get stored theme mode (defaults to system) */
export const getThemeMode = async (): Promise<ThemeMode> => {
  try {
    const value = await AsyncStorage.getItem(KEYS.THEME);
    return (value as ThemeMode) || 'system';
  } catch {
    return 'system';
  }
};

/** Set theme mode */
export const setThemeMode = async (mode: ThemeMode) => {
  try {
    await AsyncStorage.setItem(KEYS.THEME, mode);
  } catch (error) {
    console.warn('Failed to save theme:', error);
  }
};

/** Get auto-refresh preference (defaults to true) */
export const getAutoRefresh = async (): Promise<boolean> => {
  try {
    const value = await AsyncStorage.getItem(KEYS.AUTO_REFRESH);
    return value === null ? true : value === 'true';
  } catch {
    return true;
  }
};

/** Set auto-refresh preference */
export const setAutoRefresh = async (enabled: boolean) => {
  try {
    await AsyncStorage.setItem(KEYS.AUTO_REFRESH, enabled.toString());
  } catch (error) {
    console.warn('Failed to save auto-refresh:', error);
  }
};