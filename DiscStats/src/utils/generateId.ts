// src/utils/generateId.ts
/**
 * Generates a random UUID using Expo's built-in crypto module.
 * This is the recommended way in Expo/React Native (no polyfill needed).
 */
import * as Crypto from 'expo-crypto';

export const generateId = (): string => {
  return Crypto.randomUUID();
};