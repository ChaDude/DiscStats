export enum Action {
  Catch,
  Drop,
  Goal,
  Throwaway,
  Pull,
  De,
  Callahan,
  PullOb,
  Stall,
  MiscPenalty,
  EndOfFirstQuarter,
  Halftime,
  EndOfThirdQuarter,
  EndOfFourthQuarter,
  EndOfOvertime,
  GameOver,
  Timeout,
}

export enum PlayerPosition {
  Any,
  // Add other positions if needed
}

export interface Player {
  name: string;
  number: string;
  isMale: boolean;
  position: PlayerPosition;
  isAbsent: boolean;
}

export interface Team {
  teamId: string;
  name: string;
  players: Player[];
  isMixed: boolean;
  isDisplayingPlayerNumber: boolean;
  cloudId?: string;
}

export interface PlayerSubstitution {
  fromPlayer: Player;
  toPlayer: Player;
  timestamp: number;
}

export interface Event {
  action: Action;
  timestamp: number;
  details?: { [key: string]: any };
  isOffense?: boolean;
  isDefense?: boolean;
  playerOne?: Player;
  playerTwo?: Player;
}

export interface Point {
  events: Event[];
  line: Player[];
  substitutions: PlayerSubstitution[];
  timeStartedSeconds: number;
  timeEndedSeconds: number;
}

export interface Score {
  ours: number;
  theirs: number;
}

export interface PointSummary {
  score: Score;
  isOline: boolean;
  isAfterHalftime: boolean;
  elapsedSeconds: number;
}

export interface Game {
  gameId: string;
  opponentName: string;
  tournamentName: string;
  startDateTime: Date;
  gamePoint: number;
  isFirstPointOline: boolean;
  points: Point[];
  currentLine: Player[];
  timeoutDetails?: TimeoutDetails;
}

export interface TimeoutDetails {
  quotaPerHalf: number;
  quotaFloaters: number;
  takenFirstHalf: number;
  takenSecondHalf: number;
}

export interface GameDescription {
  gameId: string;
  opponentName: string;
  tournamentName: string;
  startDateTime: Date;
  score: Score;
}

export interface Preferences {
  gamePoint: number;
  timeoutsPerHalf: number;
  timeoutFloatersPerGame: number;
  currentTeamFileName?: string;
  currentGameFileName?: string;
}