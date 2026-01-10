// src/tests/stressTest.ts
// Stress test for game model: simulates complex flows with undos/corrections
// Run this in App.tsx useEffect or via node for verification

import {
  createGame,
  addPointToGame,
  addEventToPoint,
  undoLastEvent,
  overridePossession,
  getCurrentPossession,
  getGameById,
  resetMockData,
  mockPlayers,
} from '../services/mockData';

export const runStressTest = () => {
  console.log('=== Starting Stress Test ===');

  resetMockData(); // Clean slate

  // 1. Create game
  const game = createGame('team-us', 'team-opp');
  console.log('Game created:', game.id);

  // 2. Add first point, simulate events with turnover
  const point1 = addPointToGame(game.id, 'us');
  addEventToPoint(point1, 'pull', mockPlayers[0].id); // Defense pulls
  addEventToPoint(point1, 'completion'); // Offense advances
  addEventToPoint(point1, 'throwaway'); // Turnover → flip possession
  console.log('After turnover:', getCurrentPossession(point1)); // Should be 'them'

  // 3. Undo last event, check reversal
  undoLastEvent(point1);
  console.log('After undo:', getCurrentPossession(point1)); // Should revert to 'us'

  // 4. Override possession (correction)
  overridePossession(point1, 'them', 'User error correction');
  console.log('After override:', getCurrentPossession(point1)); // 'them'

  // 5. Finish point with goal, add next point
  addEventToPoint(point1, 'goal');
  const point2 = addPointToGame(game.id, 'them'); // Auto-flip starting
  addEventToPoint(point2, 'stall-out'); // Turnover
  addEventToPoint(point2, 'callahan'); // Score

  // 6. Verify full game state
  const finalGame = getGameById(game.id);
  console.log('Final game:', finalGame);
  console.log('Point 1 events:', point1.events.length); // Expect 4+ (incl correction)
  console.log('Point 2 scoringTeam:', point2.scoringTeam); // 'them'

  console.log('=== Stress Test Complete ===');
};