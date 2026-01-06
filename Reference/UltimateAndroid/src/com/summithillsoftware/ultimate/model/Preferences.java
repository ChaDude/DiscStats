package com.summithillsoftware.ultimate.model;

import java.io.Externalizable;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import com.summithillsoftware.ultimate.UltimateApplication;
import com.summithillsoftware.ultimate.twitter.AutoTweetLevel;
import com.summithillsoftware.ultimate.util.AtomicFile;

public class Preferences implements Externalizable {
	private static final long serialVersionUID = 1l;
	private static final byte OBJECT_STATE_VERSION = 1;
	
	public static final int MINIMUM_GAME_POINT = 9;
	public static final int MAXIMUM_GAME_POINT = 17;
	
	private static final int DEFAULT_GAME_POINT = 13;
	private static final int DEFAULT_TIMEOUTS_PER_HALF = 2;
	private static final int DEFAULT_FLOATERS_PER_GAME = 0;
	
	private static final String PREFERENCES_FILE_NAME = "preferences.ser";
	private static Preferences Current;

	private String currentTeamFileName;
	private String currentGameFileName;
	private String tournamentName;
	private int gamePoint = DEFAULT_GAME_POINT;
	private int timeoutsPerHalf = DEFAULT_TIMEOUTS_PER_HALF;
	private int timeoutFloatersPerGame = DEFAULT_FLOATERS_PER_GAME;
	private String cloudEMail;
	private String cloudAuthenticationCookie;
	private String twitterOAuthUserAccessToken;
	private String twitterOAuthUserAccessTokenSecret;
	private String twitterMoniker;
	private AutoTweetLevel tweetLevel;

	static {
		Current = restore();
		if (Current == null) {
			Current = new Preferences();
		}
	}
	
	public static Preferences current() {
		return Current;
	}
	
	public void save() {
		synchronized (PREFERENCES_FILE_NAME) {
			File file = getFile();
			AtomicFile.writeObject(this, file);
		}
	}
	
	private static Preferences restore() {
		// will answer NULL if error or not found
		synchronized (PREFERENCES_FILE_NAME) {
			
			// will answer NULL if error or not found
			Preferences preferences = null;
			File existingFile = getFile();
			if (existingFile != null && AtomicFile.exists(existingFile)) {
				preferences = (Preferences)AtomicFile.readObject(existingFile);
			}
			return preferences;
		}
	}
	
	public String getCurrentTeamFileName() {
		return currentTeamFileName;
	}
	public void setCurrentTeamFileName(String currentTeamFileName) {
		this.currentTeamFileName = currentTeamFileName;
	}
	public String getCurrentGameFileName() {
		return currentGameFileName;
	}
	public void setCurrentGameFileName(String currentGameFileName) {
		this.currentGameFileName = currentGameFileName;
	}
	public String getTournamentName() {
		return tournamentName;
	}
	public void setTournamentName(String tournamentName) {
		this.tournamentName = tournamentName;
	}
	public int getGamePoint() {
		return gamePoint;
	}
	public void setGamePoint(int gamePoint) {
		this.gamePoint = gamePoint;
	}
	public int getTimeoutsPerHalf() {
		return timeoutsPerHalf;
	}
	public void setTimeoutsPerHalf(int timeoutsPerHalf) {
		this.timeoutsPerHalf = timeoutsPerHalf;
	}
	public int getTimeoutFloatersPerGame() {
		return timeoutFloatersPerGame;
	}
	public void setTimeoutFloatersPerGame(int timeoutFloatersPerGame) {
		this.timeoutFloatersPerGame = timeoutFloatersPerGame;
	}
	
	private static File getFile() {
		return new File(UltimateApplication.current().getFilesDir(), PREFERENCES_FILE_NAME);
	}

	public void readExternal(ObjectInput input) throws IOException, ClassNotFoundException {
		@SuppressWarnings("unused")
		byte version = input.readByte();  // if vars change use this to decide how far to read
		currentTeamFileName = (String)input.readObject();
		currentGameFileName = (String)input.readObject();
		tournamentName = (String)input.readObject();
		gamePoint = input.readInt();
		timeoutsPerHalf = input.readInt();
		timeoutFloatersPerGame = input.readInt();
		cloudEMail = (String)input.readObject();
		cloudAuthenticationCookie = (String)input.readObject();
		twitterOAuthUserAccessToken = (String)input.readObject();
		twitterOAuthUserAccessTokenSecret = (String)input.readObject();
		twitterMoniker = (String)input.readObject();
		tweetLevel = (AutoTweetLevel)input.readObject();
	}

	public void writeExternal(ObjectOutput output) throws IOException {
		output.writeByte(OBJECT_STATE_VERSION);
		output.writeObject(currentTeamFileName);
		output.writeObject(currentGameFileName);
		output.writeObject(tournamentName);
		output.writeInt(gamePoint);
		output.writeInt(timeoutsPerHalf);
		output.writeInt(timeoutFloatersPerGame);
		output.writeObject(cloudEMail);
		output.writeObject(cloudAuthenticationCookie);
		output.writeObject(twitterOAuthUserAccessToken);
		output.writeObject(twitterOAuthUserAccessTokenSecret);
		output.writeObject(twitterMoniker);
		output.writeObject(tweetLevel);
	}

	public String getCloudEMail() {
		return cloudEMail;
	}
	
	public boolean hasCloudEMail() {
		return cloudEMail != null && !cloudEMail.trim().isEmpty();
	}

	public void setCloudEMail(String cloudEMail) {
		this.cloudEMail = cloudEMail;
	}

	public String getCloudAuthenticationCookie() {
		return cloudAuthenticationCookie;
	}

	public void setCloudAuthenticationCookie(String cloudAuthenticationCookie) {
		this.cloudAuthenticationCookie = cloudAuthenticationCookie;
	}

	public String getTwitterOAuthUserAccessToken() {
		return twitterOAuthUserAccessToken;
	}

	public void setTwitterOAuthUserAccessToken(String twitterOAuthUserAccessToken) {
		this.twitterOAuthUserAccessToken = twitterOAuthUserAccessToken;
	}

	public String getTwitterOAuthUserAccessTokenSecret() {
		return twitterOAuthUserAccessTokenSecret;
	}

	public void setTwitterOAuthUserAccessTokenSecret(
			String twitterOAuthUserAccessTokenSecret) {
		this.twitterOAuthUserAccessTokenSecret = twitterOAuthUserAccessTokenSecret;
	}

	public String getTwitterMoniker() {
		return twitterMoniker;
	}

	public void setTwitterMoniker(String twitterMoniker) {
		this.twitterMoniker = twitterMoniker;
	}

	public AutoTweetLevel getTweetLevel() {
		return tweetLevel;
	}

	public void setTweetLevel(AutoTweetLevel tweetLevel) {
		this.tweetLevel = tweetLevel;
	}
}

