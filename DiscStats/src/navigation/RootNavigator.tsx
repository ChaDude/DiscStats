// src/navigation/RootNavigator.tsx
import React from 'react';
import { NavigationContainer, DefaultTheme, DarkTheme } from '@react-navigation/native';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { Ionicons } from '@expo/vector-icons';
import { RootStackParamList } from './types';
import { useTheme } from '../context/ThemeContext'; //

import HomeScreen from '../screens/HomeScreen';
import TeamsScreen from '../screens/TeamsScreen';
import TeamDetailsScreen from '../screens/TeamDetailsScreen';
import PlayerProfileScreen from '../screens/PlayerProfileScreen';
import GamesScreen from '../screens/GamesScreen';
import StatsScreen from '../screens/StatsScreen';
import SettingsScreen from '../screens/SettingsScreen'; // Import the Settings Screen

const Tab = createBottomTabNavigator();
const Stack = createNativeStackNavigator<RootStackParamList>();

// Helper to get dark mode status
const useIsDark = () => {
  const { theme, currentScheme } = useTheme();
  return (theme === 'system' ? currentScheme : theme) === 'dark';
};

const TeamsStack = () => {
  const isDark = useIsDark();
  return (
    <Stack.Navigator
      screenOptions={{
        headerStyle: { backgroundColor: isDark ? '#121212' : '#fff' },
        headerTintColor: isDark ? '#fff' : '#000',
        headerTitleStyle: { fontWeight: 'bold' },
      }}
    >
      <Stack.Screen name="TeamsList" component={TeamsScreen} options={{ title: 'Teams' }} />
      <Stack.Screen name="TeamDetails" component={TeamDetailsScreen} options={{ title: 'Team Details' }} />
      <Stack.Screen name="PlayerProfile" options={{ title: 'Player Profile' }}>
        {(props) => <PlayerProfileScreen {...props} />}
      </Stack.Screen>
    </Stack.Navigator>
  );
};

const RootNavigator = () => {
  const isDark = useIsDark();
  const navigationTheme = isDark ? DarkTheme : DefaultTheme;

  return (
    <NavigationContainer theme={navigationTheme}>
      <Tab.Navigator
        initialRouteName="Home"
        screenOptions={({ route }) => ({
          headerStyle: { backgroundColor: isDark ? '#121212' : '#fff' },
          headerTintColor: isDark ? '#fff' : '#000',
          tabBarIcon: ({ focused, color, size }) => {
            let iconName: any = 'help-circle-outline';

            if (route.name === 'Home') iconName = focused ? 'home' : 'home-outline';
            if (route.name === 'Teams') iconName = focused ? 'people' : 'people-outline';
            if (route.name === 'Games') iconName = focused ? 'disc' : 'disc-outline';
            if (route.name === 'Stats') iconName = focused ? 'bar-chart' : 'bar-chart-outline';
            if (route.name === 'Settings') iconName = focused ? 'settings' : 'settings-outline';

            return <Ionicons name={iconName} size={size} color={color} />;
          },
          tabBarActiveTintColor: isDark ? '#90caf9' : '#2196f3',
          tabBarInactiveTintColor: isDark ? '#888' : 'gray',
          tabBarStyle: { 
            backgroundColor: isDark ? '#1e1e1e' : '#fff',
            borderTopColor: isDark ? '#333' : '#eee',
          },
        })}
      >
        <Tab.Screen name="Home" component={HomeScreen} options={{ headerShown: false }} />
        <Tab.Screen name="Teams" component={TeamsStack} options={{ headerShown: false }} />
        <Tab.Screen name="Games" component={GamesScreen} />
        <Tab.Screen name="Stats" component={StatsScreen} />
        <Tab.Screen name="Settings" component={SettingsScreen} />
      </Tab.Navigator>
    </NavigationContainer>
  );
};

export default RootNavigator;