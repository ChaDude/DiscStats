/** Defines a player entity for stats tracking in ultimate frisbee. */
export interface Player {
  id: string;          // Unique identifier
  name: string;        // Full name
  jerseyNumber?: number; // Optional jersey for display
  teamId: string;      // Reference to team
  gender: 'male' | 'female' | 'other'; // For mixed-division rules; 'other' covers non-binary/etc.
}