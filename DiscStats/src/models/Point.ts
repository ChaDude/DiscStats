// src/models/Point.ts
/** Defines a point in a game, sequencing events. */
import { Event } from './Event';

export interface Point {
  id: string;                    // Unique identifier
  gameId: string;                // Reference to game
  pointNumber: number;           // Sequential number (1, 2, ...)
  startingTeam: 'us' | 'them';   // Who starts with possession
  events: Event[];               // Full event objects in order
  scoringTeam: 'us' | 'them' | null; // Null until point ends

  // Manual override: takes precedence over event-based calculation
  // Set via overridePossession(); reset on new point or explicit clear
  forcedPossession?: 'us' | 'them' | null;
}