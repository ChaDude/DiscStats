import React from 'react';
import { View, Text, Button, StyleSheet } from 'react-native';

/**
 * Home screen of the DiscStats app.
 * Displays the main title and a button to start a new game.
 */
const HomeScreen = () => {
  return (
    <View style={styles.container}>
      <Text style={styles.title}>DiscStats</Text>
      <Button title="Start New Game" onPress={() => {}} />
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  title: {
    fontSize: 24,
    marginBottom: 20,
  },
});

export default HomeScreen;