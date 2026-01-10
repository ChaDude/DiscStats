/** Defines event types for ultimate frisbee actions. */
export type EventType = 
  | 'pull'       // Start of point
  | 'pull-out'   // Pull out of bounds
  | 'completion' // Successful throw/catch
  | 'throwaway'  // Turnover on throw error
  | 'drop'       // Turnover on receiver drop
  | 'stall-out'  // Turnover on stall count
  | 'block'      // Defensive block ('D')
  | 'goal'       // Score
  | 'callahan';  // Defensive goal on interception

/** Defines an event in a point, tagged to players. */
export interface Event {
  id: string;          // Unique identifier
  pointId: string;     // Reference to point
  timestamp: string;   // ISO format for ordering
  type: EventType;     // Action type
  actorId?: string;    // Primary player (thrower/puller)
  receiverId?: string; // For completions/goals
  defenderId?: string; // For blocks/stalls
}