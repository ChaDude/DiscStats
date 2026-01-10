// src/services/mockData.ts
/**
 * Mock in-memory data service for prototyping game/point/event flows.
 * Now stores full Event objects in points for accurate possession inference.
 */
import { generateId } from '../utils/generateId';

import {
  Game,
  Point,
  Event,
  EventType,
  Player,
  Team,
} from '../models';

// ---------------------
// Sample seed data
// ---------------------
const mockPlayers: Player[] = [
  { id: generateId(), name: 'Alex', jerseyNumber: 7, teamId: 'team-us', gender: 'male' },
  { id: generateId(), name: 'Sam', jerseyNumber: 13, teamId: 'team-us', gender: 'female' },
  { id: generateId(), name: 'Jordan', jerseyNumber: 21, teamId: 'team-us', gender: 'other' },
];

const mockTeams: Team[] = [
  { id: 'team-us', name: 'Disc Hawks', players: mockPlayers.map(p => p.id) },
  { id: 'team-opp', name: 'Rival Flyers', players: [] },
];

let mockGames: Game[] = [];

// ---------------------
// CRUD Helpers
// ---------------------

export const createGame = (ourTeamId: string, opponentTeamId: string): Game => {
  const newGame: Game = {
    id: generateId(),
    date: new Date().toISOString(),
    ourTeamId,
    opponentTeamId,
    ourScore: 0,
    opponentScore: 0,
    points: [],
  };
  mockGames.push(newGame);
  return newGame;
};

export const addPointToGame = (gameId: string, startingTeam: 'us' | 'them'): Point => {
  const game = mockGames.find(g => g.id === gameId);
  if (!game) throw new Error('Game not found');

  const newPoint: Point = {
    id: generateId(),
    gameId,
    pointNumber: game.points.length + 1,
    startingTeam,
    events: [],  // Now full Event[]
    scoringTeam: null,
  };
  game.points.push(newPoint.id);
  return newPoint;
};

export const addEventToPoint = (
  point: Point,
  type: EventType,
  actorId?: string,
  receiverId?: string,
  defenderId?: string
): Event => {
  const newEvent: Event = {
    id: generateId(),
    pointId: point.id,
    timestamp: new Date().toISOString(),
    type,
    actorId,
    receiverId,
    defenderId,
  };
  point.events.push(newEvent);  // Push full object

  if (type === 'goal') point.scoringTeam = 'us';
  if (type === 'callahan') point.scoringTeam = 'them';

  return newEvent;
};

export const getGameById = (id: string): Game | undefined =>
  mockGames.find(g => g.id === id);

export const resetMockData = () => { mockGames = []; };

// ---------------------
// Undo & Override
// ---------------------

export const undoLastEvent = (point: Point): Event | null => {
  if (point.events.length === 0) return null;
  const last = point.events.pop()!;
  if (point.scoringTeam !== null) point.scoringTeam = null;
  return last;
};

export const overridePossession = (point: Point, newPossession: 'us' | 'them', notes?: string) => {
  addEventToPoint(point, 'correction' as EventType);
  // In UI: force possession state
};

// ---------------------
// Possession Helpers
// ---------------------

export const isTurnover = (type: EventType): boolean =>
  ['throwaway', 'drop', 'stall-out', 'block'].includes(type);

export const getCurrentPossession = (point: Point): 'us' | 'them' => {
  let possession = point.startingTeam;

  // Now fully accurate using real event types
  point.events.forEach(event => {
    if (isTurnover(event.type)) {
      possession = possession === 'us' ? 'them' : 'us';
    }
  });

  return possession;
};

export { mockPlayers, mockTeams };