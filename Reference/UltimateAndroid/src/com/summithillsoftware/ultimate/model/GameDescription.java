package com.summithillsoftware.ultimate.model;

import java.io.Externalizable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import com.summithillsoftware.ultimate.util.UltimateLogger;

public class GameDescription implements Externalizable {
	private static final long serialVersionUID = 1l;
	private static final byte OBJECT_STATE_VERSION = 1;
	
	private static final String FILE_NAME_PREFIX = "info-about-";
	private static final String JSON_GAME_ID = "gameId";
	private static final String JSON_OPPONENT_NAME = "opponentName";
	private static final String JSON_TOURNAMENT_NAME = "tournamentName";
	private static final String JSON_START_DATE_TIME = "timestamp";
	private static final String JSON_IS_POSITIONAL = "positional";
	public static final String JSON_START_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm";
	
	private String gameId;
	private Date startDate;
	private String opponentName = "";
	private String tournamentName = "";	
	private Score score;
	private boolean isPositional;
	
	public GameDescription() {
		super();
	}
	
	public GameDescription(String gameId) {
		super();
		this.gameId = gameId;
	}
	
	public String getGameId() {
		return gameId;
	}
	public void setGameId(String gameId) {
		this.gameId = gameId;
	}
	public Date getStartDate() {
		return startDate;
	}
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	public String getOpponentName() {
		return opponentName;
	}
	public void setOpponentName(String opponentName) {
		this.opponentName = opponentName;
	}
	public String getTournamentName() {
		return tournamentName;
	}
	public void setTournamentName(String tournamentName) {
		this.tournamentName = tournamentName;
	}
	public Score getScore() {
		return score;
	}
	public void setScore(Score score) {
		this.score = score;
	}

	@Override
	public void writeExternal(ObjectOutput output) throws IOException {
		output.writeByte(OBJECT_STATE_VERSION);
		output.writeObject(gameId);
		output.writeObject(startDate);
		output.writeObject(opponentName);
		output.writeObject(tournamentName);		
		output.writeObject(score);
	}
	
	@Override
	public void readExternal(ObjectInput input) throws IOException, ClassNotFoundException {
		@SuppressWarnings("unused")
		byte version = input.readByte();  // if vars change use this to decide how far to read
		gameId = (String)input.readObject();
		startDate = (Date)input.readObject();
		opponentName = (String)input.readObject();
		tournamentName = (String)input.readObject();
		score = (Score)input.readObject();
	}
	
	public static GameDescription fromJsonObject(JSONObject jsonObject)
			throws JSONException {
		if (jsonObject == null) {
			return null;
		} else {
			String gameId = jsonObject.getString(JSON_GAME_ID);
			if (gameId == null) {
				return null;
			}
			GameDescription game = new GameDescription(gameId);
			if (jsonObject.has(JSON_OPPONENT_NAME)) {
				game.setOpponentName(jsonObject.getString(JSON_OPPONENT_NAME));
			}
			if (jsonObject.has(JSON_TOURNAMENT_NAME)) {
				game.setTournamentName(jsonObject
						.getString(JSON_TOURNAMENT_NAME));
			}
			if (jsonObject.has(JSON_START_DATE_TIME)) {
				SimpleDateFormat parser = new SimpleDateFormat(
						JSON_START_DATE_TIME_FORMAT, Locale.US);
				try {
					game.setStartDate(parser.parse(jsonObject
							.getString(JSON_START_DATE_TIME)));
				} catch (ParseException e) {
					throw new JSONException(e.toString());
				}
			}
			if (jsonObject.has(JSON_IS_POSITIONAL)) {
				game.setPositional(jsonObject
						.getBoolean(JSON_IS_POSITIONAL));
			}
			return game;
		}
	}
	
	public static List<GameDescription> retrieveGameDescriptionsForTeam(String teamId) {
		List<GameDescription> descriptions = new ArrayList<GameDescription>();
		for (String gameFileName : Game.getAllGameFileNames(teamId)) {
			GameDescription gameDesc = null;
			try {
				gameDesc = read(teamId, gameFileName);
			} catch (Exception e) {
				UltimateLogger.logError( "Error trying to read game description file", e);
			}
			if (gameDesc == null) {
				try {
					gameDesc = createGameDescriptionFromGame(Game.read(teamId, gameFileName, false), teamId);
				} catch (Exception e) {
					UltimateLogger.logError( "Error trying to create game description file from game...probably game corruption", e);
				}
			} 
			if (gameDesc != null) {
				descriptions.add(gameDesc);
			}
		}
		Collections.sort(descriptions, GameDescription.GameDescriptionListComparator);
		return descriptions;
	}
	
	public static GameDescription createGameDescriptionFromGame(Game game, String teamId) {
		GameDescription gameDesc = new GameDescription(game.getGameId());
		gameDesc.setOpponentName(game.getOpponentName());
		gameDesc.setTournamentName(game.getTournamentName());
		gameDesc.setScore(game.getScore());
		gameDesc.setStartDate(game.getStartDateTime());
		gameDesc.saveForTeam(teamId);
		return gameDesc;
	}
	
	public static void clearGameDescription(String teamId, String gameId) {
		File file = getFile(teamId, gameId);
		if (file.exists()) {
			boolean didDelete = file.delete();
			if (!didDelete) {
				UltimateLogger.logError( "Attempted to delete game description file but it did not delete");
			}
		}
	}
	
	private static GameDescription read(String teamId, String gameId) {
		// will answer NULL if error or not found
		GameDescription game = null;
		File existingFile = getFile(teamId, gameId);
		if (existingFile != null && existingFile.exists()) {
			FileInputStream fileInputStream = null;
			ObjectInputStream objectInputStream = null;
			try {
				fileInputStream = new FileInputStream(existingFile);
				objectInputStream = new ObjectInputStream(fileInputStream);
				game = (GameDescription) objectInputStream.readObject();
			} catch (Exception e) {
				UltimateLogger.logError( "Error restoring game description from file", e);
				throw new RuntimeException("Could not restore game description", e);
			} finally {
				try {
					objectInputStream.close();
					fileInputStream.close();
				} catch (Exception e2) {
					UltimateLogger.logError("Unable to close files when restoring game description file", e2);
				}
			}
		}
		return game;
	}
	
	public void saveForTeam(String  teamId) {
		FileOutputStream fileOutputStream = null;
		ObjectOutputStream objectOutputStream = null;
		try {
			fileOutputStream = new FileOutputStream(getFile(teamId, getGameId()));
			objectOutputStream = new ObjectOutputStream(fileOutputStream);
			objectOutputStream.writeObject(this);
		} catch (Exception e) {
			UltimateLogger.logError( "Error saving game description file", e);
		} finally {
			try {
				objectOutputStream.close();
				fileOutputStream.close();
			} catch (Exception e2) {
				UltimateLogger.logError( "Unable to close files when saving game description file",
						e2);
			}
		}
	}
	
	private static File getFile(String teamId, String gameId) {
		if (teamId == null || gameId == null) {
			return null;
		}
		return new File(Game.getTeamDir(teamId), FILE_NAME_PREFIX + gameId);
	}
	
	public static Comparator<GameDescription> GameDescriptionListComparator = new Comparator<GameDescription>() {
		public int compare(GameDescription game1, GameDescription game2) {
			return game2.getStartDate().compareTo(game1.getStartDate());
		}
	};

	public boolean isPositional() {
		return isPositional;
	}

	public void setPositional(boolean isPositional) {
		this.isPositional = isPositional;
	}

}
