// src/navigation/types.ts
/**
 * Navigation types for the DiscStats app.
 * Defines route names and params for type-safe navigation.
 */
import { NativeStackNavigationProp } from '@react-navigation/native-stack';
import { Player } from '../models';

export type RootStackParamList = {
  // Top-level Screens
  Root: undefined; // The Tab Navigator container
  
  // Tabs (Optional to list here, but good for global nav)
  Home: undefined;
  Games: undefined;
  Stats: undefined;
  Settings: undefined;

  // Teams Stack
  TeamsList: undefined;
  TeamDetails: { teamId: string; teamName: string; editingPlayerId?: string };
  PlayerProfile: { player: Player; teamName: string; teamId: string };

  // Game Flow
  GameSetup: undefined;
  GameTracker: { gameId: string };
};

export type RootStackNavigationProp = NativeStackNavigationProp<RootStackParamList>;