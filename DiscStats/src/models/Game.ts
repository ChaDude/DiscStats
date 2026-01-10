/** Defines a game entity, aggregating points and scores. */
export interface Game {
  id: string;          // Unique identifier
  date: string;        // ISO format (e.g., '2026-01-10')
  ourTeamId: string;   // Our team's ID
  opponentTeamId: string; // Opponent's ID
  ourScore: number;    // Running score
  opponentScore: number;
  points: string[];    // Array of point IDs
  tournamentId?: string; // Optional link to tournament
}