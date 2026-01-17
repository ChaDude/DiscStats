// src/models/Game.ts
/** Defines a game entity, aggregating points and scores. */
export interface Game {
  id: string;
  date: string;        // ISO format YYYY-MM-DD
  ourTeamId: string;   // FK to Teams
  opponentName: string; // Free text name (we don't track their roster)
  
  // Metadata
  tournamentName?: string;
  scoreLimit?: number;
  
  // Format Settings
  playersPerPoint: number; // 4, 5, 6, 7
  genderRule: 'none' | 'ratio_rule_a';
  
  /** * Defines "Ratio A" for ABBA. 
   * For 7v7: 'male_majority' = 4M/3F, 'female_majority' = 4F/3M.
   * For 5v5: 'male_majority' = 3M/2F, 'female_majority' = 3F/2M.
   * For 4v4/6v6: Ignored (always even).
   */
  initialRatio?: 'male_majority' | 'female_majority'; 
  
  pullingFirst: boolean; // True = We pull first (Defense start)

  // State
  ourScore: number;
  opponentScore: number;
  points: string[];    // Array of point IDs
}