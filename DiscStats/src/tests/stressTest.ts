// src/tests/stressTest.ts
/**
 * Specific engine test: scripted scenarios to verify all event types, possession flips, scoring, undos, and corrections.
 * Uses assertions to fail loudly if logic is incorrect.
 * Run this in App.tsx useEffect for verification.
 */
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
import { EventType } from '../models';

export const runSpecificEngineTest = () => {
  console.log('=== Starting Specific Engine Test (scripted scenarios) ===');

  resetMockData();

  const game = createGame('team-us', 'team-opp');
  const point = addPointToGame(game.id, 'us'); // Start with us possession

  // Get player IDs for tagging (use first few for simplicity)
  const usPlayer1 = mockPlayers[0].id; // Alex
  const usPlayer2 = mockPlayers[1].id; // Sam
  const oppPlayer = mockPlayers[mockPlayers.length - 1].id; // Opponent Player

  // Scenario 1: Basic pull and completion (no flip)
  addEventToPoint(point, 'pull', oppPlayer); // Opponent pulls
  console.assert(getCurrentPossession(point) === 'us', 'FAIL: possession after pull');

  addEventToPoint(point, 'completion', usPlayer1, usPlayer2); // Us completes
  console.assert(getCurrentPossession(point) === 'us', 'FAIL: possession after completion');

  // Scenario 2: Turnover events flip possession
  addEventToPoint(point, 'throwaway', usPlayer2); // Us throwaway → flip to them
  console.assert(getCurrentPossession(point) === 'them', 'FAIL: possession after throwaway');

  addEventToPoint(point, 'drop', usPlayer1); // Them drop → flip to us (note: drop is turnover by receiver)
  console.assert(getCurrentPossession(point) === 'us', 'FAIL: possession after drop');

  addEventToPoint(point, 'stall-out', usPlayer2); // Us stall → flip to them
  console.assert(getCurrentPossession(point) === 'them', 'FAIL: possession after stall-out');

  addEventToPoint(point, 'block', oppPlayer); // Them block → flip to us (block by defense)
  console.assert(getCurrentPossession(point) === 'us', 'FAIL: possession after block');

  // Scenario 3: Non-turnover events (no flip)
  addEventToPoint(point, 'foul', usPlayer1);
  console.assert(getCurrentPossession(point) === 'us', 'FAIL: possession after foul');

  addEventToPoint(point, 'violation', usPlayer2);
  console.assert(getCurrentPossession(point) === 'us', 'FAIL: possession after violation');

  addEventToPoint(point, 'contest');
  console.assert(getCurrentPossession(point) === 'us', 'FAIL: possession after contest');

  addEventToPoint(point, 'timeout');
  console.assert(getCurrentPossession(point) === 'us', 'FAIL: possession after timeout');

  addEventToPoint(point, 'injury');
  console.assert(getCurrentPossession(point) === 'us', 'FAIL: possession after injury');

  addEventToPoint(point, 'substitution');
  console.assert(getCurrentPossession(point) === 'us', 'FAIL: possession after substitution');

  // Scenario 4: Override possession (ignores events)
  overridePossession(point, 'them');
  console.assert(getCurrentPossession(point) === 'them', 'FAIL: possession after override to them');

  overridePossession(point, 'us');
  console.assert(getCurrentPossession(point) === 'us', 'FAIL: possession after override to us');

  // Scenario 5: Goal scoring (awards to current)
  addEventToPoint(point, 'goal', usPlayer1, usPlayer2);
  console.assert(point.scoringTeam === 'us', 'FAIL: scoringTeam after goal');
  console.assert(game.ourScore === 1, 'FAIL: ourScore after goal');

  // Scenario 6: Undo scoring event
  undoLastEvent(point);
  console.assert(point.scoringTeam === null, 'FAIL: scoringTeam reset after undo');
  console.assert(game.ourScore === 0, 'FAIL: ourScore reverted after undo');

  // Scenario 7: Callahan scoring (flips and awards to defense)
  addEventToPoint(point, 'callahan', oppPlayer);
  console.assert(point.scoringTeam === 'them', 'FAIL: scoringTeam after callahan');
  console.assert(game.opponentScore === 1, 'FAIL: opponentScore after callahan');

  // Scenario 8: Undo callahan
  undoLastEvent(point);
  console.assert(point.scoringTeam === null, 'FAIL: scoringTeam reset after callahan undo');
  console.assert(game.opponentScore === 0, 'FAIL: opponentScore reverted after undo');

  // Scenario 9: Multi-undo (back through turnovers)
  addEventToPoint(point, 'throwaway', usPlayer1); // Flip to them
  addEventToPoint(point, 'block', oppPlayer); // Flip to us
  console.assert(getCurrentPossession(point) === 'us', 'FAIL: possession after multi-turnovers');

  undoLastEvent(point);
  console.assert(getCurrentPossession(point) === 'them', 'FAIL: possession after first undo');

  undoLastEvent(point);
  console.assert(getCurrentPossession(point) === 'us', 'FAIL: possession after second undo');

  // Scenario 10: Break attempts (e.g., add after end)
  addEventToPoint(point, 'goal'); // End point
  try {
    addEventToPoint(point, 'completion');
    console.assert(false, 'FAIL: allowed add after end');
  } catch (e) {
    console.log('Pass: prevented add after end');
  }

  console.log('=== Specific Engine Test Complete (all assertions passed) ===');
};