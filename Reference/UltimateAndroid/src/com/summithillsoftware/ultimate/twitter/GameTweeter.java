package com.summithillsoftware.ultimate.twitter;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.text.TextUtils;

import com.summithillsoftware.ultimate.UltimateApplication;
import com.summithillsoftware.ultimate.model.Action;
import com.summithillsoftware.ultimate.model.DefenseEvent;
import com.summithillsoftware.ultimate.model.Event;
import com.summithillsoftware.ultimate.model.Game;
import com.summithillsoftware.ultimate.model.OffenseEvent;
import com.summithillsoftware.ultimate.model.Player;
import com.summithillsoftware.ultimate.model.Point;
import com.summithillsoftware.ultimate.model.Preferences;
import com.summithillsoftware.ultimate.model.Score;
import com.summithillsoftware.ultimate.model.Team;

public class GameTweeter {
	private static GameTweeter Current;
	
	private DateFormat timeFormatter;
	
	static {
		Current = new GameTweeter();
	}
	
	private GameTweeter() {
		timeFormatter = android.text.format.DateFormat.getTimeFormat(UltimateApplication.current());
	}
	
	public static GameTweeter current() {
		return Current;
	}
	
	public AutoTweetLevel getTweetLevel() {
		AutoTweetLevel level = Preferences.current().getTweetLevel();
		return level == null ? AutoTweetLevel.NONE : level;
	}
	
	public boolean isAutoTweeting() {
		return getTweetLevel() != AutoTweetLevel.NONE;
	}

	public void setTweetLevel(AutoTweetLevel tweetLevel) {
		Preferences.current().setTweetLevel(tweetLevel);
		Preferences.current().save();
	}
	
	public void tweetGameEvent(Event event, Point point, boolean isUndo) {
		if (isTweetingEvents()) {
			if (event.isHalftime()) { // only occurs for time-based games
				tweetHalftime(event, isUndo);
			} else if (point.getEvents().size() == 1) {
				tweetFirstEventOfPoint(event, point, isUndo);
			} else {
				tweetEvent(event, point, isUndo);
			}
		}
	}
	
	public void tweetGameOver() {
		if (isTweetingEvents()) {
			tweet(createTweet(gameOverTweetMessage()));
		}
	}
	
	private void tweetEvent(Event event, Point point, boolean isUndo) {
		Game game = game();
        tweetFirstEventOfGameIfNecessary(event, point, isUndo);
		if (event.isGoal()) {
			String message = goalTweetMessage(event, isUndo);
			Tweet tweet = createTweet(message, event, isUndo);
			tweet(tweet);
			if (!game.isTimeBasedGame() && game.isNextEventImmediatelyAfterHalftime()) {
				String halftimeMessage = halftimeTweetMessageIsUndo(isUndo);
				Tweet halftimeTweet = createTweet(halftimeMessage, event, isUndo);
				tweet(halftimeTweet);
			}
		} else if (event.isTurnover() && getTweetLevel() == AutoTweetLevel.TURNOVERS) {
			String message = turnoverTweetMessage(event, isUndo);
			Tweet tweet = createTweet(message, event, isUndo);
			tweet.setOptional(true);
			tweet(tweet);
			updateGameTweeted(event, isUndo);
		}
	}
	
	private void tweetFirstEventOfPoint(Event event, Point point, boolean isUndo) {
		tweetFirstEventOfGameIfNecessary(event, point, isUndo);
		String message = pointBeginTweetMessage(event, point, isUndo);
		Tweet tweet = createTweet(message, event, isUndo);
		tweet.setOptional(true);
		tweet(tweet);
		if (event.isGoal() || event.isTurnover()) {
			tweetEvent(event, point, isUndo);
		}
		updateGameTweeted(event, isUndo);
	}
	
	private void tweetFirstEventOfGameIfNecessary(Event event, Point point, boolean isUndo) {
		if (!hasGameBeenTweeted() || (isUndo && game().getFirstEventTweeted().equals(event))) {
			String message = game().isFirstPoint(point) ? gameBeginTweetMessage(event, isUndo) : gameBeginInProgressTweetMessage(event, isUndo);
			Tweet tweet = createTweet(message, event, isUndo);
			tweet(tweet);	
			updateGameTweeted(event, isUndo);
		}
	}
	
	
	private boolean hasGameBeenTweeted() {
		return game().getFirstEventTweeted() != null;
	}

	private void updateGameTweeted(Event event, boolean isUndo) {
		if (isUndo) {
			if (event.equals(game().getFirstEventTweeted())) {
				game().setFirstEventTweeted(null);
			}
		} else if (game().getFirstEventTweeted() == null) {
			game().setFirstEventTweeted(event);
		}
	}

	private String getGameScoreDescription() {
	    Score score = game().getScore();
	    return score.getOurs() + "-" + score.getTheirs() + (score.getOurs() == score.getTheirs() ? "" : score.getOurs() > score.getTheirs() ?
	            " " + Team.current().getName() : " " + game().getOpponentName());
	}
	
	private String gameBeginTweetMessage(Event event, boolean isUndo) {
		Game game = game();
		String message = null;
		if (isUndo) {
			message = "New game was a boo-boo...never mind.";
		} else {
			String windDescription = game.getWind() != null && game.getWind().getMph() > 0 ? " Wind: " + game.getWind().getMph() + "mph." : "";
			message = "New game vs. " + game.getOpponentName() + ".  Game point: " + game.getGamePoint() + "." + windDescription;			
		}
		return message;
	}
	
	private String gameBeginInProgressTweetMessage(Event event, boolean isUndo) {
		Game game = game();
		String message = null;
		if (isUndo) {
			message = "New game in progress was a boo-boo...never mind.";
		} else {
			String windDescription = game.getWind() != null && game.getWind().getMph() > 0 ? " Wind: " + game.getWind().getMph() + "mph. " : " ";
			message = "New game in progress vs. " + game.getOpponentName() + ".  Game point: " + game.getGamePoint() + "." + windDescription  + "Current score: " + getGameScoreDescription();			
		}
		return message;
	}
	
	private String goalTweetMessage(Event event, boolean isUndo) {
		Game game = game();
		String message = null;
		if (event.isCallahan()) {
			message = "Callahan " + (event.isOffense() ? game.getOpponentName() : ourTeam());
		} else {
			message = "Goal " + (event.isOurGoal() ? ourTeam() : game.getOpponentName());
		}
		
		if (isUndo) {
			message = message + " was a boo-boo...never mind";
		} else {
			if (event.isOurGoal()) {
				if (event.isCallahan()) {
					message = message + "!!! " + ((DefenseEvent)event).getDefender().getName() + ". " + getGameScoreDescription()  + ".";
				} else {
					message = message + "!!! " + ((OffenseEvent)event).getPasser().getName() + " to " + ((OffenseEvent)event).getReceiver().getName() + ". " + getGameScoreDescription() + ".";
				}
			} else {
				message = message + ". " + getGameScoreDescription();
			}
		}
		return message;
	}
	
	private String halftimeTweetMessageIsUndo(boolean isUndo) {
		return isUndo ? "\"Halftime\" was a boo-boo...never mind." : "Halftime.";
	}
	
	private String turnoverTweetMessage(Event event, boolean isUndo) {
		String team = event.isDefense() && event.isThrowaway() ? game().getOpponentName() : ourTeam(); 
		String message = team + (event.getAction() == Action.De ? " steal" : " lose") + " the disc";
		if (isUndo) {
			message = "\"" + message + "\" was a boo-boo...never mind.";
		} else {
			if (event.isD()) {
				message = message + "!!! (" + event.toString() + ")"; 
			} else {
				String cause;
				switch (event.getAction()) {
		        case Drop:
		            cause = "drop";
		            break;
		        case Throwaway:
		            cause = "throwaway";
		            break;
		        case Stall:
		            cause = "stall";
		            break;
		        case MiscPenalty:
		            cause = "penalty";
		            break;
		        default:
		            cause = "not sure why";
		            break;
				}
				message = message + " (" + cause + ")"; 
			}
		}
		return message;
	}
	
	private String pointBeginTweetMessage(Event event, Point point, boolean isUndo) {
		Game game = game();
		if (isUndo) {
			return "New point was a boo-boo...never mind.";
		} else {
			List<String> names =  new ArrayList<String>();
			for (Player player : point.getLine()) {
				names.add(player.getName());
			}
			String namesAsString = TextUtils.join(", ", names);
			if (game.isPointOline(point)) {
				return "Pull, " +  ourTeam() + " on Offense.  Line: " + namesAsString;
			} else {
				return "Pull, " +  ourTeam() + " on Defense.  Line: " + namesAsString;
			}
		}
	}

	private String gameOverTweetMessage() {
	    return "Game over, " + getGameScoreDescription(); 
	}
	
	private boolean isTweetingEvents() {
		return getTweetLevel() != AutoTweetLevel.NONE;
	}
	
	private Game game() {
		return Game.current();
	}
	
	private String ourTeam() {
		return Team.current().getName();
	}
	
	private String prependTime(String message) {
		return timeFormatter.format(new Date()) + " " + message;
	}
	
	private void tweetHalftime(Event event, boolean isUndo) {
		String halftimeMessage = halftimeTweetMessageIsUndo(isUndo);
		Tweet halftimeTweet = createTweet(halftimeMessage, event, isUndo);
		tweet(halftimeTweet);
	}
	
	private Tweet createTweet(String message) {
		Tweet tweet = new Tweet(prependTime(message));
		return tweet;
	}
	
	private Tweet createTweet(String message, Event event, boolean isUndo) {
		Tweet tweet = createTweet(message);
		tweet.setEvent(event);
		tweet.setUndo(isUndo);
		return tweet;
	}
	
	private void tweet(Tweet tweet) {
		TweetQueue.current().submitTweet(tweet);
	}

}
