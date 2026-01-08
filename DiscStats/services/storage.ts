import AsyncStorage from '@react-native-async-storage/async-storage';
import { Team, Game, Preferences } from '../models/types';

const TEAMS_KEY = 'teams';
const GAMES_KEY = 'games';
const PREFERENCES_KEY = 'preferences';

export const storage = {
  // Teams
  async saveTeams(teams: Team[]): Promise<void> {
    await AsyncStorage.setItem(TEAMS_KEY, JSON.stringify(teams));
  },

  async loadTeams(): Promise<Team[]> {
    const data = await AsyncStorage.getItem(TEAMS_KEY);
    return data ? JSON.parse(data) : [];
  },

  // Games
  async saveGames(games: Game[]): Promise<void> {
    await AsyncStorage.setItem(GAMES_KEY, JSON.stringify(games));
  },

  async loadGames(): Promise<Game[]> {
    const data = await AsyncStorage.getItem(GAMES_KEY);
    return data ? JSON.parse(data) : [];
  },

  // Preferences
  async savePreferences(prefs: Preferences): Promise<void> {
    await AsyncStorage.setItem(PREFERENCES_KEY, JSON.stringify(prefs));
  },

  async loadPreferences(): Promise<Preferences> {
    const data = await AsyncStorage.getItem(PREFERENCES_KEY);
    const defaultPrefs: Preferences = {
      gamePoint: 13,
      timeoutsPerHalf: 2,
      timeoutFloatersPerGame: 1,
    };
    return data ? { ...defaultPrefs, ...JSON.parse(data) } : defaultPrefs;
  },
};