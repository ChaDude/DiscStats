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

// DEVELOPMENT FLAG: Toggle detailed event logging (set to false for production/clean logs)
const DEBUG_EVENTS: boolean = true;

// ---------------------
// Sample seed data
// ---------------------

const mockPlayers: Player[] = [
  { id: generateId(), name: 'Alex', jerseyNumber: 7, teamId: 'team-us', gender: 'male' },
  { id: generateId(), name: 'Sam', jerseyNumber: 13, teamId: 'team-us', gender: 'female' },
  { id: generateId(), name: 'Jordan', jerseyNumber: 21, teamId: 'team-us', gender: 'other' },
  { id: generateId(), name: 'Taylor', jerseyNumber: 5, teamId: 'team-us', gender: 'female' },
  { id: generateId(), name: 'Casey', jerseyNumber: 42, teamId: 'team-us', gender: 'male' },
  { id: generateId(), name: 'Riley', jerseyNumber: 8, teamId: 'team-us', gender: 'other' },
  { id: generateId(), name: 'Jamie', jerseyNumber: 3, teamId: 'team-us', gender: 'male' },
  { id: generateId(), name: 'Morgan', jerseyNumber: 17, teamId: 'team-us', gender: 'female' },
  { id: generateId(), name: 'Pat', jerseyNumber: 9, teamId: 'team-us', gender: 'other' },
  { id: generateId(), name: 'Chris', jerseyNumber: 22, teamId: 'team-us', gender: 'male' },
  { id: generateId(), name: 'Lee', jerseyNumber: 11, teamId: 'team-us', gender: 'female' },
  { id: generateId(), name: 'Drew', jerseyNumber: 4, teamId: 'team-us', gender: 'male' },
  { id: generateId(), name: 'Quinn', jerseyNumber: 19, teamId: 'team-us', gender: 'other' },
  { id: generateId(), name: 'Kim', jerseyNumber: 6, teamId: 'team-us', gender: 'female' },
  { id: generateId(), name: 'Opponent Player', jerseyNumber: 0, teamId: 'team-opp', gender: 'male' },
];

const mockTeams: Team[] = [
  { id: 'team-us', name: 'Disc Hawks', players: mockPlayers.filter(p => p.teamId === 'team-us').map(p => p.id) },
  { id: 'team-opp', name: 'Rival Flyers', players: mockPlayers.filter(p => p.teamId === 'team-opp').map(p => p.id) },
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
    events: [],
    scoringTeam: null,
    forcedPossession: null,
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
  if (point.scoringTeam !== null) throw new Error('Cannot add events to ended point');

  const newEvent: Event = {
    id: generateId(),
    pointId: point.id,
    timestamp: new Date().toISOString(),
    type,
    actorId,
    receiverId,
    defenderId,
  };
  point.events.push(newEvent);

  // DEVELOPMENT LOGGING: show every event + current possession after it
  if (DEBUG_EVENTS) {
    const current = getCurrentPossession(point);
    const actor = actorId ? mockPlayers.find(p => p.id === actorId)?.name ?? actorId : '';
    const receiver = receiverId ? mockPlayers.find(p => p.id === receiverId)?.name ?? receiverId : '';
    const defender = defenderId ? mockPlayers.find(p => p.id === defenderId)?.name ?? defenderId : '';

    console.log(
      `Event: ${type} | Possession now: ${current}` +
      (actor ? ` (actor: ${actor})` : '') +
      (receiver ? ` (receiver: ${receiver})` : '') +
      (defender ? ` (defender: ${defender})` : '')
    );
  }

  // Possession-based scoring
  const possession = getCurrentPossession(point);

  if (type === 'callahan') {
    point.scoringTeam = possession === 'us' ? 'them' : 'us';
  } else if (type === 'goal') {
    point.scoringTeam = possession;
  }

  // Auto-update game score
  const game = mockGames.find(g => g.id === point.gameId);
  if (game && point.scoringTeam !== null) {
    if (point.scoringTeam === 'us') game.ourScore += 1;
    else game.opponentScore += 1;
  }

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

export const overridePossession = (
  point: Point,
  newPossession: 'us' | 'them',
  notes?: string
) => {
  point.forcedPossession = newPossession;
  addEventToPoint(point, 'correction');

  if (DEBUG_EVENTS) {
    console.log(`Possession manually overridden to ${newPossession} (forced)`);
  }
};

// ---------------------
// Possession Helpers
// ---------------------

export const isTurnover = (type: EventType): boolean =>
  ['throwaway', 'drop', 'stall-out', 'block'].includes(type);

export const getCurrentPossession = (point: Point): 'us' | 'them' => {
  if (point.forcedPossession !== undefined && point.forcedPossession !== null) {
    return point.forcedPossession;
  }

  let possession = point.startingTeam;
  point.events.forEach(event => {
    if (isTurnover(event.type)) {
      possession = possession === 'us' ? 'them' : 'us';
    }
  });

  return possession;
};

export { mockPlayers, mockTeams };