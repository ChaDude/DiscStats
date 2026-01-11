// src/screens/TeamDetailsScreen.tsx
/**
 * Team details screen.
 * Shows players for a selected team + add player form.
 * MVP placeholder - expand with DB integration next.
 */
import React from 'react';
import { View, Text, StyleSheet } from 'react-native';

import { RouteProp } from '@react-navigation/native';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';
import { RootStackParamList } from '../navigation/types';

type Props = {
  route: RouteProp<RootStackParamList, 'TeamDetails'>;
  navigation: NativeStackNavigationProp<RootStackParamList, 'TeamDetails'>;
};

export default function TeamDetailsScreen({ route }: Props) {
  const { teamId, teamName } = route.params;

  return (
    <View style={styles.container}>
      <Text style={styles.title}>{teamName}</Text>
      <Text style={styles.subtitle}>Team ID: {teamId}</Text>
      <Text>Players list + add player form (coming soon)</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, justifyContent: 'center', alignItems: 'center', backgroundColor: '#fff' },
  title: { fontSize: 32, fontWeight: 'bold', marginBottom: 8 },
  subtitle: { fontSize: 18, color: '#666', marginBottom: 16 },
});