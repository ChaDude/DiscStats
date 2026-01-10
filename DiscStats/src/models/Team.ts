/** Defines a team entity, including player references. */
export interface Team {
  id: string;          // Unique identifier
  name: string;        // Team name
  players: string[];   // Array of player IDs (avoids deep nesting)
  ownerUserId?: string; // For future auth/sharing
}