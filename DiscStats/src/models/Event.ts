/** Defines event types for ultimate frisbee actions. */
export type EventType = 
  | 'pull'       // Start of point, defense throws to offense
  | 'pull-out'   // Pull out of bounds
  | 'brick'      // Call to restart after bad pull
  | 'completion' // Successful throw/catch
  | 'throwaway'  // Turnover on throw error
  | 'drop'       // Turnover on receiver drop
  | 'stall-out'  // Turnover on stall count
  | 'block'      // Defensive block ('D')
  | 'goal'       // Score in endzone
  | 'callahan'   // Defensive goal on interception
  | 'foul'       // Contact violation call
  | 'violation'  // Rule infraction (e.g., travel)
  | 'contest'    // Dispute on a call
  | 'timeout'    // Team or injury timeout
  | 'injury'     // Stoppage for injury
  | 'substitution' // Player swap
  | 'correction' // Fix to User Input

/** Defines an event in a point, tagged to players. */
export interface Event {
  id: string;          // Unique identifier
  pointId: string;     // Reference to point
  timestamp: string;   // ISO format for ordering
  type: EventType;     // Action type
  actorId?: string;    // Primary player (thrower/puller/blocker)
  receiverId?: string; // For completions/goals
  defenderId?: string; // For blocks/stalls/fouls
  // Optional: notes?: string; // For call details, add later if needed
}