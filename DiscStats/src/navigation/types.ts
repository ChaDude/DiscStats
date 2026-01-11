// src/navigation/types.ts
/**
 * Navigation types for the DiscStats app.
 * Defines route names and params for type-safe navigation.
 */
import { NativeStackNavigationProp } from '@react-navigation/native-stack';

export type RootStackParamList = {
  Home: undefined;
  GameTracker: undefined;
};

export type RootStackNavigationProp = NativeStackNavigationProp<RootStackParamList>;