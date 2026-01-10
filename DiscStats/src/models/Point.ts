// src/models/Point.ts
/** Defines a point in a game, sequencing events. */
import { Event } from './Event';  // Import full Event type

export interface Point {
  id: string;                    // Unique identifier
  gameId: string;                // Reference to game
  pointNumber: number;           // Sequential number (1, 2, ...)
  startingTeam: 'us' | 'them';   // Who starts with possession
  events: Event[];               // Full event objects in order (upgrade from IDs)
  scoringTeam: 'us' | 'them' | null; // Null until point ends
}