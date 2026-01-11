// src/services/database.ts
/**
 * SQLite database service for DiscStats (offline-first).
 * Uses modern expo-sqlite API (openDatabaseSync + async methods).
 * Call initDatabase() on app start.
 */
import * as SQLite from 'expo-sqlite';
import { generateId } from '../utils/generateId';
import { Player, Team } from '../models';

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
  // Dev-only: Force fresh DB on every app start (remove/comment for production)
  if (__DEV__) {
    try {
      await closeDb(); // Close first to avoid "database open" error
      await SQLite.deleteDatabaseAsync('discstats.db');
      console.log('Dev mode: Deleted existing DB for fresh seed');
    } catch (error) {
      console.warn('Dev mode DB delete failed (may be first run or harmless):', error);
    }
  }

  try {
    const database = getDb();

    // Create tables if not exist
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

/** Seed default teams and players (called once) */
const seedDefaults = async () => {
  // 2 teams per division: mixed, open (men's), women's
  // Each with 24 players
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
        const jersey = i < 10 ? `0${i}` : `${i}`; // 01-24
        let gender: 'male' | 'female' | 'other' = 'male';
        if (div.gender === 'mixed') {
          gender = i % 3 === 0 ? 'other' : i % 2 === 0 ? 'female' : 'male'; // Balanced ~8 each
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

/** Get all teams */
export const getTeams = async (): Promise<Team[]> => {
  const rows = await db!.getAllAsync<Team>('SELECT * FROM teams');
  return rows;
};

/** Add a new team */
export const addTeam = async (name: string): Promise<string> => {
  const id = generateId();
  await db!.runAsync('INSERT INTO teams (id, name) VALUES (?, ?)', [id, name]);
  return id;
};

/** Get players for a team */
export const getPlayersForTeam = async (teamId: string): Promise<Player[]> => {
  const rows = await db!.getAllAsync<Player>(
    'SELECT * FROM players WHERE teamId = ?',
    [teamId]
  );
  return rows;
};

/** Add a player to a team */
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

/** Delete a player by ID */
export const deletePlayer = async (playerId: string): Promise<void> => {
  await db!.runAsync('DELETE FROM players WHERE id = ?', [playerId]);
};
/** Delete a team (and cascade delete players) */
export const deleteTeam = async (teamId: string): Promise<void> => {
  await db!.runAsync('DELETE FROM teams WHERE id = ?', [teamId]);
};
/** Update an existing player */
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