// src/navigation/types.ts
/**
 * Navigation types for the DiscStats app.
 * Defines route names and params for type-safe navigation.
 */
import { NativeStackNavigationProp } from '@react-navigation/native-stack';

export type RootStackParamList = {
  Home: undefined;
  TeamsList: undefined; // Teams tab list
  TeamDetails: { teamId: string; teamName: string }; // Required for type-safe navigate
  Games: undefined;
  Stats: undefined;
  GameTracker: undefined; // For future GameTracker navigation
};

export type RootStackNavigationProp = NativeStackNavigationProp<RootStackParamList>;