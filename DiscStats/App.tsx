// App.tsx
import { useEffect } from 'react';
import RootNavigator from './src/navigation/RootNavigator'; // ← keep your real navigation

// Mock data imports – only for testing
import {
  createGame,
  addPointToGame,
  addEventToPoint,
  getGameById,
  mockPlayers,
} from './src/services/mockData';

export default function App() {
  // Temporary test – runs once when app mounts
  useEffect(() => {
    const runTest = () => {
      console.log('=== Starting mock data test ===');

      const game = createGame('team-us', 'team-opp');
      console.log('Created game:', game);

      const point = addPointToGame(game.id, 'us');
      console.log('Added point:', point);

      const puller = mockPlayers[0];
      addEventToPoint(point, 'pull', puller.id);

      const thrower = mockPlayers[0];
      const catcher = mockPlayers[1];
      addEventToPoint(point, 'completion', thrower.id, catcher.id);

      addEventToPoint(point, 'goal', catcher.id);

      const updatedGame = getGameById(game.id);
      console.log('Final game state:', updatedGame);
      console.log('Point details:', point);

      console.log('=== Mock data test complete ===');
    };

    runTest();
  }, []); // ← important: empty dependency array

  // ← Keep your real app content here!
  return <RootNavigator />;
}