// src/navigation/types.ts
/**
 * Navigation types for the DiscStats app.
 * Defines route names and params for type-safe navigation.
 */
import { NativeStackNavigationProp } from '@react-navigation/native-stack';
import { Player } from '../models';

export type RootStackParamList = {
  Home: undefined;
  TeamsList: undefined;
  TeamDetails: { teamId: string; teamName: string; editingPlayerId?: string }; // ← Added optional edit trigger
  PlayerProfile: { player: Player; teamName: string; teamId: string }; // ← Added teamId
  Games: undefined;
  Stats: undefined;
  GameTracker: undefined;
};

export type RootStackNavigationProp = NativeStackNavigationProp<RootStackParamList>;