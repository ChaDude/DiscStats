// src/services/mockData.ts
// Mock in-memory data service for prototyping game/point/event flows
// Replace with real Firebase service later

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
  {
    id: 'team-us',
    name: 'Disc Hawks',
    players: mockPlayers.map((p) => p.id),
  },
  {
    id: 'team-opp',
    name: 'Rival Flyers',
    players: [],
  },
];

let mockGames: Game[] = [];

// ---------------------
// CRUD Helpers
// ---------------------

/** Creates a new game and returns it */
export const createGame = (
  ourTeamId: string,
  opponentTeamId: string
): Game => {
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

/** Adds a new point to a game and returns it */
export const addPointToGame = (
  gameId: string,
  startingTeam: 'us' | 'them'
): Point => {
  const game = mockGames.find((g) => g.id === gameId);
  if (!game) throw new Error('Game not found');

  const newPoint: Point = {
    id: generateId(),
    gameId,
    pointNumber: game.points.length + 1,
    startingTeam,
    events: [],
    scoringTeam: null,
  };
  game.points.push(newPoint.id);
  return newPoint;
};

/** Adds an event to a point and returns it */
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
  point.events.push(newEvent.id);

  // Basic side effects (expand later)
  if (type === 'goal') {
    point.scoringTeam = 'us'; // Assume 'us' scored for simplicity
  } else if (type === 'callahan') {
    point.scoringTeam = 'them';
  }

  return newEvent;
};

/** Simple getter for a game by ID */
export const getGameById = (id: string): Game | undefined => {
  return mockGames.find((g) => g.id === id);
};

/** Reset all mock data (useful for testing) */
export const resetMockData = () => {
  mockGames = [];
};

// ---------------------
// New: Undo and Possession Helpers
// ---------------------

/** Undoes the last event in a point, reversing side effects */
export const undoLastEvent = (point: Point): Event | null => {
  if (point.events.length === 0) return null;

  const lastEventId = point.events.pop()!;
  // Reverse side effects (e.g., reset scoringTeam if it was set by this event)
  if (point.scoringTeam !== null) {
    point.scoringTeam = null; // Simple reset; refine for specific types later
  }

  // Return the removed event (for logging/UI)
  return { id: lastEventId } as Event; // Stub; in real, fetch full event
};

/** Manually overrides possession by adding a correction event */
export const overridePossession = (
  point: Point,
  newPossession: 'us' | 'them',
  notes?: string
) => {
  addEventToPoint(point, 'correction' as EventType, undefined, undefined, undefined); // Logs as special event
  // In UI, use this to flip state without altering stats
  // Notes can be stored in event for audit
};

// Export seed data for convenience
export { mockPlayers, mockTeams };