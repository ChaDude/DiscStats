// src/services/database.ts
/**
 * SQLite database service for DiscStats (offline-first).
 * Uses modern expo-sqlite API (openDatabaseSync + async methods).
 */
import * as SQLite from 'expo-sqlite';
import { generateId } from '../utils/generateId';
import { Player, Team, Game, Point, Event, EventType } from '../models';

// Global DB instance
let db: SQLite.SQLiteDatabase | null = null;

// Lazy open (only once)
const getDb = (): SQLite.SQLiteDatabase => {
  if (!db) {
    db = SQLite.openDatabaseSync('discstats.db');
  }
  return db;
};

// Close DB connection (for dev reset)
const closeDb = async () => {
  if (db) {
    await db.closeAsync();
    db = null;
    console.log('Dev mode: Closed DB connection before deletion');
  }
};

// One-time schema + defaults
export const initDatabase = async (): Promise<void> => {
  // Dev-only: Force fresh DB on every app start
  if (__DEV__) {
    try {
      await closeDb();
      await SQLite.deleteDatabaseAsync('discstats.db');
      console.log('Dev mode: Deleted existing DB for fresh seed');
    } catch (error) {
      console.warn('Dev mode DB delete failed (may be first run or harmless):', error);
    }
  }

  try {
    const database = getDb();

    // --- TEAMS & PLAYERS ---
    await database.execAsync(`
      CREATE TABLE IF NOT EXISTS teams (
        id TEXT PRIMARY KEY NOT NULL,
        name TEXT NOT NULL
      );
    `);

    await database.execAsync(`
      CREATE TABLE IF NOT EXISTS players (
        id TEXT PRIMARY KEY NOT NULL,
        name TEXT NOT NULL,
        jerseyNumber INTEGER,
        gender TEXT CHECK(gender IN ('male', 'female', 'other')) NOT NULL,
        teamId TEXT NOT NULL,
        FOREIGN KEY(teamId) REFERENCES teams(id) ON DELETE CASCADE
      );
    `);

    // --- GAMES ---
    await database.execAsync(`
      CREATE TABLE IF NOT EXISTS games (
        id TEXT PRIMARY KEY NOT NULL,
        date TEXT NOT NULL,
        ourTeamId TEXT NOT NULL,
        opponentName TEXT NOT NULL, 
        tournamentName TEXT,
        scoreLimit INTEGER,
        playersPerPoint INTEGER DEFAULT 7,
        genderRule TEXT DEFAULT 'none',
        initialRatio TEXT,
        pullingFirst INTEGER DEFAULT 0,
        ourScore INTEGER DEFAULT 0,
        opponentScore INTEGER DEFAULT 0,
        FOREIGN KEY(ourTeamId) REFERENCES teams(id)
      );
    `);

    // --- POINTS ---
    await database.execAsync(`
      CREATE TABLE IF NOT EXISTS points (
        id TEXT PRIMARY KEY NOT NULL,
        gameId TEXT NOT NULL,
        pointNumber INTEGER NOT NULL,
        startingTeam TEXT CHECK(startingTeam IN ('us', 'them')) NOT NULL,
        scoringTeam TEXT CHECK(scoringTeam IN ('us', 'them')),
        FOREIGN KEY(gameId) REFERENCES games(id) ON DELETE CASCADE
      );
    `);

    // --- POINT PLAYERS (The "Line") ---
    await database.execAsync(`
      CREATE TABLE IF NOT EXISTS point_players (
        pointId TEXT NOT NULL,
        playerId TEXT NOT NULL,
        PRIMARY KEY (pointId, playerId),
        FOREIGN KEY(pointId) REFERENCES points(id) ON DELETE CASCADE,
        FOREIGN KEY(playerId) REFERENCES players(id) ON DELETE CASCADE
      );
    `);

    // --- EVENTS ---
    await database.execAsync(`
      CREATE TABLE IF NOT EXISTS events (
        id TEXT PRIMARY KEY NOT NULL,
        pointId TEXT NOT NULL,
        timestamp TEXT NOT NULL,
        type TEXT NOT NULL,
        actorId TEXT,
        receiverId TEXT,
        defenderId TEXT,
        FOREIGN KEY(pointId) REFERENCES points(id) ON DELETE CASCADE
      );
    `);

    // Check if teams exist (seed defaults only once)
    const countResult = await database.getFirstAsync<{ count: number }>(
      'SELECT COUNT(*) as count FROM teams'
    );

    if (countResult?.count === 0) {
      await seedDefaults();
      console.log('Default teams & players seeded');
    }

    console.log('Database initialized successfully');
  } catch (error) {
    console.error('Database init failed:', error);
    throw error;
  }
};

/** Seed default teams and players */
const seedDefaults = async () => {
  const divisions = [
    { nameSuffix: ' Mixed', gender: 'mixed' },
    { nameSuffix: ' Open', gender: 'male' },
    { nameSuffix: ' Women\'s', gender: 'female' },
  ];

  for (const div of divisions) {
    const teamAId = generateId();
    const teamBId = generateId();

    // Team A
    await db!.runAsync(
      'INSERT INTO teams (id, name) VALUES (?, ?)',
      [teamAId, 'Team A' + div.nameSuffix]
    );

    // Team B
    await db!.runAsync(
      'INSERT INTO teams (id, name) VALUES (?, ?)',
      [teamBId, 'Team B' + div.nameSuffix]
    );

    // Generate 24 players per team
    for (let t = 0; t < 2; t++) {
      const teamId = t === 0 ? teamAId : teamBId;
      for (let i = 1; i <= 24; i++) {
        const name = `Player ${String.fromCharCode(65 + t)}-${i}`;
        const jersey = i < 10 ? `0${i}` : `${i}`; 
        let gender: 'male' | 'female' | 'other' = 'male';
        if (div.gender === 'mixed') {
          gender = i % 3 === 0 ? 'other' : i % 2 === 0 ? 'female' : 'male'; 
        } else if (div.gender === 'female') {
          gender = 'female';
        }

        await db!.runAsync(
          'INSERT INTO players (id, name, jerseyNumber, gender, teamId) VALUES (?, ?, ?, ?, ?)',
          [generateId(), name, parseInt(jersey), gender, teamId]
        );
      }
    }
  }
};

/* ============================
   TEAM & PLAYER FUNCTIONS 
   ============================ */

export const getTeams = async (): Promise<Team[]> => {
  const rows = await db!.getAllAsync<Team>('SELECT * FROM teams');
  return rows;
};

export const addTeam = async (name: string): Promise<string> => {
  const id = generateId();
  await db!.runAsync('INSERT INTO teams (id, name) VALUES (?, ?)', [id, name]);
  return id;
};

export const getPlayersForTeam = async (teamId: string): Promise<Player[]> => {
  const rows = await db!.getAllAsync<Player>(
    'SELECT * FROM players WHERE teamId = ? ORDER BY jerseyNumber ASC',
    [teamId]
  );
  return rows;
};

export const addPlayer = async (
  teamId: string,
  name: string,
  jerseyNumber?: number,
  gender: 'male' | 'female' | 'other' = 'other'
): Promise<string> => {
  const id = generateId();
  await db!.runAsync(
    'INSERT INTO players (id, name, jerseyNumber, gender, teamId) VALUES (?, ?, ?, ?, ?)',
    [id, name, jerseyNumber ?? null, gender, teamId]
  );
  return id;
};

export const deletePlayer = async (playerId: string): Promise<void> => {
  await db!.runAsync('DELETE FROM players WHERE id = ?', [playerId]);
};

export const deleteTeam = async (teamId: string): Promise<void> => {
  await db!.runAsync('DELETE FROM teams WHERE id = ?', [teamId]);
};

export const updatePlayer = async (
  playerId: string,
  updates: Partial<Pick<Player, 'name' | 'jerseyNumber' | 'gender'>>
): Promise<void> => {
  const { name, jerseyNumber, gender } = updates;
  await db!.runAsync(
    `UPDATE players SET 
      name = COALESCE(?, name),
      jerseyNumber = COALESCE(?, jerseyNumber),
      gender = COALESCE(?, gender)
    WHERE id = ?`,
    [name ?? null, jerseyNumber ?? null, gender ?? null, playerId]
  );
};

export const getPlayerById = async (id: string): Promise<Player | null> => {
  const row = await db!.getFirstAsync<Player>(
    'SELECT * FROM players WHERE id = ?',
    [id]
  );
  return row || null;
};

/* ============================
   GAME & POINT FUNCTIONS 
   ============================ */

export const createGame = async (
  ourTeamId: string, 
  opponentName: string,
  settings: {
    tournamentName?: string;
    scoreLimit?: number;
    playersPerPoint: number;
    genderRule: 'none' | 'ratio_rule_a';
    initialRatio?: 'male_majority' | 'female_majority';
    pullingFirst: boolean;
  }
): Promise<string> => {
  const database = getDb();
  const id = generateId();
  const date = new Date().toISOString().split('T')[0]; // YYYY-MM-DD
  
  await database.runAsync(
    `INSERT INTO games (
      id, date, ourTeamId, opponentName, tournamentName, scoreLimit, 
      playersPerPoint, genderRule, initialRatio, pullingFirst, 
      ourScore, opponentScore
    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, 0)`,
    [
      id, date, ourTeamId, opponentName, 
      settings.tournamentName ?? null, 
      settings.scoreLimit ?? null,
      settings.playersPerPoint,
      settings.genderRule,
      settings.initialRatio ?? null,
      settings.pullingFirst ? 1 : 0
    ]
  );
  return id;
};

export const getGameById = async (gameId: string): Promise<Game | null> => {
  const database = getDb();
  const game = await database.getFirstAsync<any>('SELECT * FROM games WHERE id = ?', [gameId]);
  if (!game) return null;

  // Convert SQLite integers (0/1) back to booleans/types where needed
  const convertedGame: Game = {
    ...game,
    pullingFirst: game.pullingFirst === 1,
    points: [] // Populated below
  };

  const points = await database.getAllAsync<{ id: string }>(
    'SELECT id FROM points WHERE gameId = ? ORDER BY pointNumber ASC',
    [gameId]
  );
  
  convertedGame.points = points.map(p => p.id);
  return convertedGame;
};

/** Adds a new point and records the "Line" (active players). */
export const addPoint = async (
  gameId: string, 
  startingTeam: 'us' | 'them', 
  linePlayerIds: string[]
): Promise<string> => {
  const database = getDb();
  
  // 1. Get next point number
  const lastPoint = await database.getFirstAsync<{ pointNumber: number }>(
    'SELECT pointNumber FROM points WHERE gameId = ? ORDER BY pointNumber DESC LIMIT 1',
    [gameId]
  );
  const nextNumber = (lastPoint?.pointNumber || 0) + 1;
  const pointId = generateId();

  // 2. Create Point
  await database.runAsync(
    'INSERT INTO points (id, gameId, pointNumber, startingTeam) VALUES (?, ?, ?, ?)',
    [pointId, gameId, nextNumber, startingTeam]
  );

  // 3. Save the Line
  if (linePlayerIds.length > 0) {
    for (const playerId of linePlayerIds) {
      await database.runAsync(
        'INSERT INTO point_players (pointId, playerId) VALUES (?, ?)',
        [pointId, playerId]
      );
    }
  }

  return pointId;
};

export const getPointById = async (pointId: string): Promise<Point | null> => {
  const database = getDb();
  const point = await database.getFirstAsync<Point>('SELECT * FROM points WHERE id = ?', [pointId]);
  if (!point) return null;

  const events = await database.getAllAsync<Event>(
    'SELECT * FROM events WHERE pointId = ? ORDER BY timestamp ASC',
    [pointId]
  );

  return {
    ...point,
    events: events,
  };
};

/** Get the players who were on the line for a specific point. */
export const getLineForPoint = async (pointId: string): Promise<Player[]> => {
  const database = getDb();
  const rows = await database.getAllAsync<Player>(
    `SELECT p.* FROM players p
     JOIN point_players pp ON p.id = pp.playerId
     WHERE pp.pointId = ?
     ORDER BY p.jerseyNumber ASC`,
    [pointId]
  );
  return rows;
};

/** Adds a single player to an existing point (for substitutions). */
export const addPlayerToPoint = async (pointId: string, playerId: string): Promise<void> => {
  const database = getDb();
  // Use INSERT OR IGNORE to prevent crash if duplicate
  await database.runAsync(
    'INSERT OR IGNORE INTO point_players (pointId, playerId) VALUES (?, ?)',
    [pointId, playerId]
  );
};

/* ============================
   EVENT FUNCTIONS 
   ============================ */

export const addEvent = async (
  pointId: string, 
  type: EventType, 
  actorId?: string, 
  receiverId?: string, 
  defenderId?: string
): Promise<string> => {
  const database = getDb();
  const id = generateId();
  const timestamp = new Date().toISOString();
  
  await database.runAsync(
    `INSERT INTO events (id, pointId, timestamp, type, actorId, receiverId, defenderId) 
     VALUES (?, ?, ?, ?, ?, ?, ?)`,
    [id, pointId, timestamp, type, actorId ?? null, receiverId ?? null, defenderId ?? null]
  );

  return id;
};

export const deleteLastEvent = async (pointId: string): Promise<void> => {
  const database = getDb();
  const lastEvent = await database.getFirstAsync<{ id: string }>(
    'SELECT id FROM events WHERE pointId = ? ORDER BY timestamp DESC LIMIT 1',
    [pointId]
  );
  
  if (lastEvent) {
    await database.runAsync('DELETE FROM events WHERE id = ?', [lastEvent.id]);
  }
};

/** Deletes a point and all associated events/line data (via cascade). */
export const deletePoint = async (pointId: string): Promise<void> => {
  const database = getDb();
  await database.runAsync('DELETE FROM points WHERE id = ?', [pointId]);
};

/** Deletes an entire game (used when cancelling setup or deleting from list). */
export const deleteGame = async (gameId: string): Promise<void> => {
  const database = getDb();
  await database.runAsync('DELETE FROM games WHERE id = ?', [gameId]);
};