// src/navigation/RootNavigator.tsx
/**
 * Root navigator for the DiscStats app.
 * Uses bottom tabs with Expo vector icons (no native build needed).
 */
import React from 'react';
import { NavigationContainer } from '@react-navigation/native';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { Ionicons } from '@expo/vector-icons';

import HomeScreen from '../screens/HomeScreen';
import TeamsScreen from '../screens/TeamsScreen';
import GamesScreen from '../screens/GamesScreen';
import StatsScreen from '../screens/StatsScreen';

const Tab = createBottomTabNavigator();

const RootNavigator = () => {
  return (
    <NavigationContainer>
      <Tab.Navigator
        initialRouteName="Home"
        screenOptions={({ route }) => ({
          tabBarIcon: ({ focused, color, size }) => {
            let iconName: keyof typeof Ionicons.glyphMap = 'help-circle-outline';

            switch (route.name) {
              case 'Home':
                iconName = focused ? 'home' : 'home-outline';
                break;
              case 'Teams':
                iconName = focused ? 'people' : 'people-outline';
                break;
              case 'Games':
                iconName = focused ? 'disc' : 'disc-outline'; // ← Best frisbee icon
                break;
              case 'Stats':
                iconName = focused ? 'bar-chart' : 'bar-chart-outline';
                break;
            }

            return <Ionicons name={iconName} size={size} color={color} />;
          },
          tabBarActiveTintColor: '#fff',
          tabBarInactiveTintColor: '#888',
          tabBarStyle: {
            backgroundColor: '#333',
            borderTopWidth: 0,
          },
          tabBarShowLabel: true,
        })}
      >
        <Tab.Screen name="Home" component={HomeScreen} />
        <Tab.Screen name="Teams" component={TeamsScreen} />
        <Tab.Screen name="Games" component={GamesScreen} />
        <Tab.Screen name="Stats" component={StatsScreen} />
      </Tab.Navigator>
    </NavigationContainer>
  );
};

export default RootNavigator;