// src/navigation/RootNavigator.tsx
/**
 * Root navigator for the DiscStats app.
 * Bottom tabs for main sections + stack for team/player details.
 */
import React from 'react';
import { NavigationContainer } from '@react-navigation/native';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { Ionicons } from '@expo/vector-icons';
import { RootStackParamList } from './types';

import HomeScreen from '../screens/HomeScreen';
import TeamsScreen from '../screens/TeamsScreen';
import TeamDetailsScreen from '../screens/TeamDetailsScreen';
import PlayerProfileScreen from '../screens/PlayerProfileScreen'; // ← Added missing import
import GamesScreen from '../screens/GamesScreen';
import StatsScreen from '../screens/StatsScreen';

const Tab = createBottomTabNavigator();
const Stack = createNativeStackNavigator<RootStackParamList>();

const TeamsStack = () => (
  <Stack.Navigator>
    <Stack.Screen name="TeamsList" component={TeamsScreen} options={{ title: 'Teams' }} />
    <Stack.Screen name="TeamDetails" component={TeamDetailsScreen} options={{ title: 'Team Details' }} />
    <Stack.Screen name="PlayerProfile" component={PlayerProfileScreen} options={{ title: 'Player Profile' }} />
  </Stack.Navigator>
);

const RootNavigator = () => {
  return (
    <NavigationContainer>
      <Tab.Navigator
        initialRouteName="Home"
        screenOptions={({ route }) => ({
          tabBarIcon: ({ focused, color, size }) => {
            let iconName: any = 'help-circle-outline';

            if (route.name === 'Home') iconName = focused ? 'home' : 'home-outline';
            if (route.name === 'Teams') iconName = focused ? 'people' : 'people-outline';
            if (route.name === 'Games') iconName = focused ? 'disc' : 'disc-outline';
            if (route.name === 'Stats') iconName = focused ? 'bar-chart' : 'bar-chart-outline';

            return <Ionicons name={iconName} size={size} color={color} />;
          },
          tabBarActiveTintColor: '#fff',
          tabBarInactiveTintColor: '#888',
          tabBarStyle: { backgroundColor: '#333', borderTopWidth: 0 },
        })}
      >
        <Tab.Screen name="Home" component={HomeScreen} />
        <Tab.Screen name="Teams" component={TeamsStack} options={{ headerShown: false }} />
        <Tab.Screen name="Games" component={GamesScreen} />
        <Tab.Screen name="Stats" component={StatsScreen} />
      </Tab.Navigator>
    </NavigationContainer>
  );
};

export default RootNavigator;