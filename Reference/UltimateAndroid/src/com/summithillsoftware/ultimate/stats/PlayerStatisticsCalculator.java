package com.summithillsoftware.ultimate.stats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.summithillsoftware.ultimate.model.Action;
import com.summithillsoftware.ultimate.model.DefenseEvent;
import com.summithillsoftware.ultimate.model.Event;
import com.summithillsoftware.ultimate.model.Game;
import com.summithillsoftware.ultimate.model.OffenseEvent;
import com.summithillsoftware.ultimate.model.Player;
import com.summithillsoftware.ultimate.model.Point;
import com.summithillsoftware.ultimate.model.Team;

public class PlayerStatisticsCalculator {
	private static Comparator<PlayerStat> playerStatComparator = new Comparator<PlayerStat>() {
		public int compare(final PlayerStat playerStat1, final PlayerStat playerStat2) {
			return Float.compare(playerStat2.sortValue(), playerStat1.sortValue());
		}
	};
	
	// Answers a "factor" (float between 0 and 1) for each player which reflects a player's played points vs. the rest of the team.
	public static Map<String, Float> pointsPlayedFactorPerPlayer(Game game) {
		Map<String, PlayerStat> pointsPerPlayer = pointsPerPlayer(game, true, true);
		Map<String, Float> pointFactorPerPlayer = new HashMap<String, Float>();
		
		// find highest points played
		float playersMaxPoint = 0;
		for (PlayerStat pointsPlayedStat : pointsPerPlayer.values()) {
			playersMaxPoint = Math.max(playersMaxPoint, pointsPlayedStat.getFloatValue());
		}
		
		// for each player, calculate his factor
		for (Player player : game.getPlayers()) {
			PlayerStat playerStat = getStatForPlayer(player, pointsPerPlayer, StatNumericType.FLOAT);
			float pointsPlayed = playerStat.getFloatValue();
			float factor = playersMaxPoint == 0 ? 0 : pointsPlayed / (float)playersMaxPoint;
			pointFactorPerPlayer.put(player.getId(), factor);
		}
		
		return pointFactorPerPlayer;
	}
	
	// Answer a dictionary (key=player id, value=PlayerStat) of all of the players' number of points in the game
	public static Map<String, PlayerStat> pointsPerPlayer(Game game, boolean includeO, boolean includeD) {
		Map<String, PlayerStat> pointsPerPlayer = new HashMap<String, PlayerStat>();
		for (Point point : game.getPoints()) {
			boolean isOLine = game.isPointOline(point);
			if ((isOLine && includeO) || (!isOLine && includeD)) {
				for (Player player : point.playersInEntirePoint()) {
					updatePointsPerPlayer(player, pointsPerPlayer, 1.0f);
				}
				for (Player player : point.playersInPartOfPoint()) {
					updatePointsPerPlayer(player, pointsPerPlayer, .5f);
				}	
			}
		}
		return pointsPerPlayer;
	}
	
	/****   Stats Accumulator Methods *****/
	
	
	public static List<PlayerStat> pointsPerPlayer(final Game game, final boolean includeO, final boolean includeD, final boolean includeTournament) {
		StatsAccumulator statsAccumulator = new StatsAccumulator() {
			@Override
			public void updateStats(StatsEventDetails eventDetails) {
				if (eventDetails.isFirstEventOfPoint()) {
					if ((eventDetails.isOlinePoint() && includeO) || (eventDetails.isDlinePoint() && includeD)) {
						for (Player player : eventDetails.getPoint().playersInEntirePoint()) {
							PlayerStat playerStat = getStatForPlayer(player, eventDetails.getAccumulatedStats(), StatNumericType.FLOAT);
							playerStat.incrFloatValue();
						}
						for (Player player : eventDetails.getPoint().playersInPartOfPoint()) {
							PlayerStat playerStat = getStatForPlayer(player, eventDetails.getAccumulatedStats(), StatNumericType.FLOAT);
							playerStat.setFloatValue(playerStat.getFloatValue() + .5f);
						}						
					}
				}
			}
		};
		return accumulateStatsPerPlayer(game, includeTournament, statsAccumulator, StatNumericType.FLOAT);
	
	}

	public static List<PlayerStat> throwsPerPlayer(final Game game, final boolean includeTournament) {
		StatsAccumulator statsAccumulator = new StatsAccumulator() {
			@Override
			public void updateStats(StatsEventDetails eventDetails) {
				if (eventDetails.isOffense()) {
					OffenseEvent event = eventDetails.getOffenseEvent();
					if (event.isCatch() || event.isDrop() || event.isThrowaway() || event.isCallahan() || event.isGoal()) {
						PlayerStat playerStat = getStatForPlayer(event.getPasser(), eventDetails.getAccumulatedStats(), StatNumericType.INTEGER);
						playerStat.incrIntValue();
					}
				}
			}
		};
		return accumulateStatsPerPlayer(game, includeTournament, statsAccumulator, StatNumericType.INTEGER);
	}
	
	public static List<PlayerStat> goalsPerPlayer(final Game game, final boolean includeTournament) {
		StatsAccumulator statsAccumulator = new StatsAccumulator() {
			@Override
			public void updateStats(StatsEventDetails eventDetails) {
				if (eventDetails.isOffense() && eventDetails.getEvent().isGoal()) {
					OffenseEvent event = eventDetails.getOffenseEvent();
					PlayerStat playerStat = getStatForPlayer(event.getReceiver(), eventDetails.getAccumulatedStats(), StatNumericType.INTEGER);
					playerStat.incrIntValue();
				} else if (eventDetails.isDefense() && eventDetails.getEvent().isCallahan()) {
					DefenseEvent event = eventDetails.getDefenseEvent();
					PlayerStat playerStat = getStatForPlayer(event.getDefender(), eventDetails.getAccumulatedStats(), StatNumericType.INTEGER);
					playerStat.incrIntValue();
				}
			}
		};
		return accumulateStatsPerPlayer(game, includeTournament, statsAccumulator, StatNumericType.INTEGER);
	}
	
	public static List<PlayerStat> assistsPerPlayer(final Game game, final boolean includeTournament) {
		StatsAccumulator statsAccumulator = new StatsAccumulator() {
			@Override
			public void updateStats(StatsEventDetails eventDetails) {
				if (eventDetails.isOffense() && eventDetails.getEvent().isGoal()) {
					OffenseEvent event = eventDetails.getOffenseEvent();
					PlayerStat playerStat = getStatForPlayer(event.getPasser(), eventDetails.getAccumulatedStats(), StatNumericType.INTEGER);
					playerStat.incrIntValue();
				} 
			}
		};
		return accumulateStatsPerPlayer(game, includeTournament, statsAccumulator, StatNumericType.INTEGER);
	}
	
	public static List<PlayerStat> dropsPerPlayer(final Game game, final boolean includeTournament) {
		StatsAccumulator statsAccumulator = new StatsAccumulator() {
			@Override
			public void updateStats(StatsEventDetails eventDetails) {
				if (eventDetails.getEvent().isDrop()) {
					OffenseEvent event = eventDetails.getOffenseEvent();
					PlayerStat playerStat = getStatForPlayer(event.getReceiver(), eventDetails.getAccumulatedStats(), StatNumericType.INTEGER);
					playerStat.incrIntValue();
				} 
			}
		};
		return accumulateStatsPerPlayer(game, includeTournament, statsAccumulator, StatNumericType.INTEGER);
	}

	public static List<PlayerStat> throwawaysPerPlayer(final Game game, final boolean includeTournament) {
		StatsAccumulator statsAccumulator = new StatsAccumulator() {
			@Override
			public void updateStats(StatsEventDetails eventDetails) {
				if (eventDetails.isOffense() && (eventDetails.getEvent().isThrowaway() || eventDetails.getEvent().isCallahan())) {
					OffenseEvent event = eventDetails.getOffenseEvent();
					PlayerStat playerStat = getStatForPlayer(event.getPasser(), eventDetails.getAccumulatedStats(), StatNumericType.INTEGER);
					playerStat.incrIntValue();
				} 
			}
		};
		return accumulateStatsPerPlayer(game, includeTournament, statsAccumulator, StatNumericType.INTEGER);
	}
	
	public static List<PlayerStat> stallsPerPlayer(final Game game, final boolean includeTournament) {
		StatsAccumulator statsAccumulator = new StatsAccumulator() {
			@Override
			public void updateStats(StatsEventDetails eventDetails) {
				if (eventDetails.isOffense() && eventDetails.getEvent().getAction() == Action.Stall) {
					OffenseEvent event = eventDetails.getOffenseEvent();
					PlayerStat playerStat = getStatForPlayer(event.getReceiver(), eventDetails.getAccumulatedStats(), StatNumericType.INTEGER);
					playerStat.incrIntValue();
				} 
			}
		};
		return accumulateStatsPerPlayer(game, includeTournament, statsAccumulator, StatNumericType.INTEGER);
	}
	
	public static List<PlayerStat> miscPenaltiesPerPlayer(final Game game, final boolean includeTournament) {
		StatsAccumulator statsAccumulator = new StatsAccumulator() {
			@Override
			public void updateStats(StatsEventDetails eventDetails) {
				if (eventDetails.isOffense() && eventDetails.getEvent().getAction() == Action.MiscPenalty) {
					OffenseEvent event = eventDetails.getOffenseEvent();
					PlayerStat playerStat = getStatForPlayer(event.getReceiver(), eventDetails.getAccumulatedStats(), StatNumericType.INTEGER);
					playerStat.incrIntValue();
				} 
			}
		};
		return accumulateStatsPerPlayer(game, includeTournament, statsAccumulator, StatNumericType.INTEGER);
	}
	
	public static List<PlayerStat> pullsPerPlayer(final Game game, final boolean includeTournament) {
		StatsAccumulator statsAccumulator = new StatsAccumulator() {
			@Override
			public void updateStats(StatsEventDetails eventDetails) {
				if (eventDetails.getEvent().isPull()) {
					DefenseEvent event = eventDetails.getDefenseEvent();
					PlayerStat playerStat = getStatForPlayer(event.getDefender(), eventDetails.getAccumulatedStats(), StatNumericType.INTEGER);
					playerStat.incrIntValue();
				} 
			}
		};
		return accumulateStatsPerPlayer(game, includeTournament, statsAccumulator, StatNumericType.INTEGER);
	}
	
	public static List<PlayerStat> pullsObPerPlayer(final Game game, final boolean includeTournament) {
		StatsAccumulator statsAccumulator = new StatsAccumulator() {
			@Override
			public void updateStats(StatsEventDetails eventDetails) {
				if (eventDetails.getEvent().isPullOb()) {
					DefenseEvent event = eventDetails.getDefenseEvent();
					PlayerStat playerStat = getStatForPlayer(event.getDefender(), eventDetails.getAccumulatedStats(), StatNumericType.INTEGER);
					playerStat.incrIntValue();
				} 
			}
		};
		return accumulateStatsPerPlayer(game, includeTournament, statsAccumulator, StatNumericType.INTEGER);
	}
	
	public static List<PlayerStat> dsPerPlayer(final Game game, final boolean includeTournament) {
		StatsAccumulator statsAccumulator = new StatsAccumulator() {
			@Override
			public void updateStats(StatsEventDetails eventDetails) {
				if (eventDetails.isDefense() && (eventDetails.getEvent().isD() || eventDetails.getEvent().isCallahan())) {
					DefenseEvent event = eventDetails.getDefenseEvent();
					PlayerStat playerStat = getStatForPlayer(event.getDefender(), eventDetails.getAccumulatedStats(), StatNumericType.INTEGER);
					playerStat.incrIntValue();
				} 
			}
		};
		return accumulateStatsPerPlayer(game, includeTournament, statsAccumulator, StatNumericType.INTEGER);
	}

	public static List<PlayerStat> callahansPerPlayer(final Game game, final boolean includeTournament) {
		StatsAccumulator statsAccumulator = new StatsAccumulator() {
			@Override
			public void updateStats(StatsEventDetails eventDetails) {
				if (eventDetails.isDefense() && eventDetails.getEvent().isCallahan()) {
					DefenseEvent event = eventDetails.getDefenseEvent();
					PlayerStat playerStat = getStatForPlayer(event.getDefender(), eventDetails.getAccumulatedStats(), StatNumericType.INTEGER);
					playerStat.incrIntValue();
				} 
			}
		};
		return accumulateStatsPerPlayer(game, includeTournament, statsAccumulator, StatNumericType.INTEGER);
	}
	
	public static List<PlayerStat> callahanedPerPlayer(final Game game, final boolean includeTournament) {
		StatsAccumulator statsAccumulator = new StatsAccumulator() {
			@Override
			public void updateStats(StatsEventDetails eventDetails) {
				if (eventDetails.isOffense() && eventDetails.getEvent().isCallahan()) {
					OffenseEvent event = eventDetails.getOffenseEvent();
					PlayerStat playerStat = getStatForPlayer(event.getReceiver(), eventDetails.getAccumulatedStats(), StatNumericType.INTEGER);
					playerStat.incrIntValue();
				} 
			}
		};
		return accumulateStatsPerPlayer(game, includeTournament, statsAccumulator, StatNumericType.INTEGER);
	}
	
	public static List<PlayerStat> plusMinusCountPerPlayer(Game game, boolean includeTournament) {
	    /*
	     +/- counters/stats for individual players over the course of a game and a
	     tournament (assists and goals count as +1, drops and throwaways count as -1).
	     D's are a +1.  (thus a defense callahan is +2)
	     */
		StatsAccumulator statsAccumulator = new StatsAccumulator() {
			@Override
			public void updateStats(StatsEventDetails eventDetails) {
				if (eventDetails.isOffense()) {
					OffenseEvent event = eventDetails.getOffenseEvent();
					if (event.isTurnover()) {
						Player player = event.isDrop() ? event.getReceiver() : event.getPasser();
						PlayerStat playerStat = getStatForPlayer(player, eventDetails.getAccumulatedStats(), StatNumericType.INTEGER);
						playerStat.decrIntValue();
					} else if (event.isGoal() && !event.isCallahan()) {
						PlayerStat playerStat = getStatForPlayer(event.getPasser(), eventDetails.getAccumulatedStats(), StatNumericType.INTEGER);
						playerStat.incrIntValue();
						playerStat = getStatForPlayer(event.getReceiver(), eventDetails.getAccumulatedStats(), StatNumericType.INTEGER);
						playerStat.incrIntValue();						
					} 
				} else if (eventDetails.getEvent().isD() || eventDetails.getEvent().isCallahan()) {
					DefenseEvent event = eventDetails.getDefenseEvent();
					PlayerStat playerStat = getStatForPlayer(event.getDefender(), eventDetails.getAccumulatedStats(), StatNumericType.INTEGER);
					playerStat.incrIntValue();
					if (event.isCallahan()) {
						playerStat.incrIntValue();
					}
				}
				
			}
		};
		return accumulateStatsPerPlayer(game, includeTournament, statsAccumulator, StatNumericType.INTEGER);
		
	}
	
	/****  PRIVATE HELPER METHODS ****/
	
	private static List<PlayerStat> accumulateStatsPerPlayer(Game game, boolean includeTournament, StatsAccumulator statsAccumulator, StatNumericType type) {
		Map<String, PlayerStat> statPerPlayer = new HashMap<String, PlayerStat>();
		if (includeTournament && game.getTournamentName() != null && !game.getTournamentName().isEmpty()) {
			String tournament = game.getTournamentName();
			String currentTeamId = Team.current().getTeamId();
			List<String> gameFilesForCurrentTeam = Game.getAllGameFileNames(currentTeamId);
			for (String gameFileId : gameFilesForCurrentTeam) {
				Game anotherGame = Game.read(gameFileId);
				if (anotherGame.getTournamentName().equalsIgnoreCase(tournament)) {
					accumulateStatsPerPlayer(anotherGame, statPerPlayer, statsAccumulator);
				}
			}
		} else {
			accumulateStatsPerPlayer(game, statPerPlayer, statsAccumulator);
		}
		return sortedPlayerStats(statPerPlayer, game, type);
	}
	
	private static List<PlayerStat> sortedPlayerStats(Map<String, PlayerStat> statPerPlayer, Game game, StatNumericType type) {
		// find all of the players
		Set<Player> players = new HashSet<Player>(game.getPlayers());
		players.addAll(Team.current().getPlayers());
		
		// build a list of PlayerStat for every player
		List<PlayerStat> playerStats = new ArrayList<PlayerStat>();
		for (Player player : players) {
			playerStats.add(getStatForPlayer(player, statPerPlayer, type));
		}

		// sort them
		return descendingSortedStats(playerStats);
	}
	
	private static PlayerStat getStatForPlayer(Player player, Map<String, PlayerStat> statsPerPlayer, StatNumericType type) {
		PlayerStat playerStat = statsPerPlayer.get(player.getId());
		if (playerStat == null) {
			playerStat = type == StatNumericType.INTEGER ? new PlayerStat(player, 0) : new PlayerStat(player, 0f);
			statsPerPlayer.put(player.getId(), playerStat);
		}
		return playerStat;
	}
	
	private static List<PlayerStat> descendingSortedStats(List<PlayerStat> stats) {
		Collections.sort(stats, playerStatComparator);
		return stats;
	}
	
	private static void accumulateStatsPerPlayer(Game game, Map<String, PlayerStat> statsPerPlayer, StatsAccumulator accumulator) {
		// Accumulate stats in a statsPerPlayer.  The accumulator is called for each event.
		StatsEventDetails eventDetails = new StatsEventDetails();
		eventDetails.setAccumulatedStats(statsPerPlayer);
		eventDetails.setGame(game);
		for (Point point : game.getPoints()) {
			boolean firstEvent = true;
			eventDetails.setPoint(point);
			eventDetails.setLine(point.getLine());
			eventDetails.setOlinePoint(game.isPointOline(point));
			for (Event event : point.getEvents()) {
				eventDetails.setEvent(event);
				eventDetails.setFirstEventOfPoint(firstEvent);
				accumulator.updateStats(eventDetails);
				firstEvent = false;
			}
		}
	}
	
	private static void updatePointsPerPlayer(Player player, Map<String, PlayerStat> pointsPerPlayer, float increment) {
		PlayerStat playerStat = pointsPerPlayer.get(player.getId());
		if (playerStat == null) {
			playerStat = new PlayerStat(player, increment);
			pointsPerPlayer.put(player.getId(), playerStat);
		} else {
			playerStat.setFloatValue(playerStat.getFloatValue() + increment);
		}
	} 

}
