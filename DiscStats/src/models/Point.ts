/** Defines a point in a game, sequencing events. */
export interface Point {
  id: string;          // Unique identifier
  gameId: string;      // Reference to game
  pointNumber: number; // Sequential number (1, 2, ...)
  startingTeam: 'us' | 'them'; // Who starts with possession
  events: string[];    // Array of event IDs in order
  scoringTeam: 'us' | 'them' | null; // Null until point ends
}