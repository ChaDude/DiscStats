package com.summithillsoftware.ultimate.model;

import java.io.Externalizable;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.summithillsoftware.ultimate.UltimateApplication;
import com.summithillsoftware.ultimate.util.AtomicFile;
import com.summithillsoftware.ultimate.util.UltimateLogger;

public class Team implements Externalizable {
	private static final long serialVersionUID = 1l;
	private static final byte OBJECT_STATE_VERSION = 1;
	
	private static final String JSON_TEAM_ID = "teamId";
	private static final String JSON_CLOUD_ID = "cloudId";
	private static final String JSON_NAME = "name";
	private static final String JSON_PLAYERS = "players";
	private static final String JSON_IS_MIXED = "mixed";
	private static final String JSON_IS_DISPLAYING_PLAYER_NUMBER = "displayPlayerNumber";
	private static final String JSON_IS_LEAGUEVINE_PLAYERS = "playersAreLeaguevine";
	private static final String JSON_LEAGUEVINE_JSON = "leaguevineJson";
	
	static final String DEFAULT_TEAM_NAME = "My Team";
	private static final String FILE_NAME_PREFIX = "team-";
	private static Team Current;
	
	private String teamId;	
	private String name;
	private List<Player>players;
	private boolean isMixed;
	private boolean isDisplayingPlayerNumber;
	private String cloudId;
	private String leaguevineJson;
	private boolean isPlayersAreLeaguevine;
	
	public Team() {
		super();
		players = new ArrayList<Player>();
		teamId = generateUniqueFileName();

	}
	
	public static Team current() {
		synchronized (FILE_NAME_PREFIX) {
			if (Current == null) {
				String currentTeamFileName = Preferences.current().getCurrentTeamFileName(); 
				if (currentTeamFileName != null) {
					try {
						Current = read(currentTeamFileName);
					} catch (Exception e) {
						List<TeamDescription> teams = retrieveTeamDescriptions();
						if (teams.size() > 0) {
							String otherTeamId = teams.get(0).getTeamId();
							Current = Team.read(otherTeamId);
							Preferences.current().setCurrentTeamFileName(otherTeamId);
							Preferences.current().save();
						}
					}
				}
				if (Current == null) {
					Team newTeam = new Team();
					newTeam.name = DEFAULT_TEAM_NAME;
					newTeam.save();
					Preferences.current().setCurrentTeamFileName(newTeam.teamId);
					Preferences.current().save();
				}
			}
			return Current;
		}
	}
	
	public static void setCurrentTeamId(String newCurrentTeamId) {
		synchronized (FILE_NAME_PREFIX) {		
			if (Current != null && !Current.teamId.equals(newCurrentTeamId)) {
				Game.setCurrentGame(null);  // clear current game if switching teams
			}
			if (Current == null || !Current.teamId.equals(newCurrentTeamId)) {
				if (newCurrentTeamId != null) {
					Current = read(newCurrentTeamId);
					if (Current == null) {
						throw new RuntimeException("Team has not been saved");
					}
				}
				Preferences.current().setCurrentTeamFileName(newCurrentTeamId);
				Preferences.current().save();
			}
		}
	}
	
	public static void setCurrentTeam(Team team) {
		synchronized (FILE_NAME_PREFIX) {
			if (team != null && !team.hasBeenSaved()) {
				throw new RuntimeException("Team has not been saved");
			}
			if (team == null || (Current != null && !Current.teamId.equals(team.getTeamId()))) {
				Game.setCurrentGame(null);  // clear current game if switching teams
			}
			if (Current == null || team == null || !Current.teamId.equals(team.getTeamId())) {
				Current = team;
				Preferences.current().setCurrentTeamFileName(team == null ? null : team.getTeamId());
				Preferences.current().save();
			}
		}
	}
	
	public static boolean isCurrentTeam(String otherTeamId) {
		synchronized (FILE_NAME_PREFIX) {
			return otherTeamId.equals(Preferences.current().getCurrentTeamFileName());
		}
	}

	public static List<TeamDescription> retrieveTeamDescriptions() {
		ArrayList<TeamDescription> descriptions = new ArrayList<TeamDescription>();
		for (String teamFileName : getAllTeamFileNames()) {
			try {
				Team team = read(teamFileName);
				TeamDescription description = new TeamDescription(teamFileName,
						team.getName(), team.getCloudId());
				descriptions.add(description);
			} catch (Exception e) {
				// skip the team
			}
		}
		return descriptions;
	}
	
	public static int numberOfTeams() {
		return getAllTeamFileNames().size();
	}
	
	public static TeamDescription getDefaultTeamDescription() {
		for (TeamDescription teamDescription : retrieveTeamDescriptions()) {
			if (teamDescription.isDefaultTeamName()) {
				return teamDescription;
			}
		}
		return null;
	}
	
	public static void removeDefaultTeamIfNoLongerRequired() {
		if (numberOfTeams() > 1) {
			TeamDescription defaultTeam = getDefaultTeamDescription();
			if (defaultTeam != null && defaultTeam.canBeSafelyRemove()) {
				Team.delete(defaultTeam.getTeamId());
			}
		}
	}
	
	public static boolean isDuplicateTeamName(String newTeamName, Team teamToIgnore) {
		for (TeamDescription existingTeam : retrieveTeamDescriptions()) {
			if (teamToIgnore == null) {
				if (existingTeam.getName().equalsIgnoreCase(newTeamName)) {
					return true;
				}
			} else if (!existingTeam.getTeamId().equals(teamToIgnore.getTeamId())) {
				if (!existingTeam.getTeamId().equals(teamToIgnore.getTeamId()) && existingTeam.getName().equalsIgnoreCase(newTeamName)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static String getTeamIdForCloudId(String cloudId) {
		for (TeamDescription team : retrieveTeamDescriptions()) {
			if (team.getCloudId().equals(cloudId)) {
				return team.getTeamId();
			}
		}
		return null;
	}
	
	private static List<String> getAllTeamFileNames() {

		File teamsDir = getTeamsDir();
		if (teamsDir != null && teamsDir.exists() && teamsDir.isDirectory()) {
			Set<String> teamFileNames = AtomicFile.findFileNames(teamsDir, FILE_NAME_PREFIX);
			return new ArrayList<String>(teamFileNames);
		} else {
			return Collections.emptyList();
		}

	}
	
	private static Team read(String teamId) {
		// will answer NULL if error or not found
		Team team = null;
		File existingFile = getFile(teamId);
		if (existingFile != null && AtomicFile.exists(existingFile)) {
			team = (Team)AtomicFile.readObject(existingFile);
		}
		return team;
	}
	
	private static String generateUniqueFileName() {
		return FILE_NAME_PREFIX + java.util.UUID.randomUUID().toString();
	}
	
	private static File getFile(String teamId) {
		if (teamId == null) {
			return null;
		}
		return new File(getTeamsDir(), teamId);
	}
	
	private static File getTeamsDir() {
		return UltimateApplication.current().getFilesDir();
	}
	
	public static void delete(String teamId) {
		// delete the associated games
		Game.deleteAllGamesForTeam(teamId);
		
		// move "current" to another team		
		if (isCurrentTeam(teamId)) {
			for (TeamDescription team : retrieveTeamDescriptions()) {
				if (!team.getTeamId().equals(teamId)) {
					setCurrentTeamId(teamId);
					break;
				}
			}
		}
		
		// delete the team
		File file = getFile(teamId);
		boolean didDelete = AtomicFile.delete(file);
		if (!didDelete) {
			UltimateLogger.logError( "Attempted to delete team file but it did not delete");
		}
	}
	
	public void save() {
		File file = getFile(teamId);
		AtomicFile.writeObject(this, file);
	}

	public void delete() {
		Team.delete(this.getTeamId());
	}

	public boolean hasBeenSaved() {
		return AtomicFile.exists(getFile(this.getTeamId()));
	}
	
	public boolean isDefaultTeamName() {
		return getName() != null && getName().equalsIgnoreCase(DEFAULT_TEAM_NAME);
	}
	
	public String getTeamId() {
		return teamId;
	}
	public void setTeamId(String teamId) {
		this.teamId = teamId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<Player> getPlayers() {
		return players;
	}
	public void setPlayers(List<Player> players) {
		this.players = players;
	}
	public boolean isMixed() {
		return isMixed;
	}
	public void setMixed(boolean isMixed) {
		this.isMixed = isMixed;
	}
	public boolean isDisplayingPlayerNumber() {
		return isDisplayingPlayerNumber;
	}
	public void setDisplayingPlayerNumber(boolean isDisplayingPlayerNumber) {
		this.isDisplayingPlayerNumber = isDisplayingPlayerNumber;
	}
	public String getCloudId() {
		return cloudId;
	}
	public void setCloudId(String cloudId) {
		this.cloudId = cloudId;
	}
	public boolean hasCloudId() {
		return cloudId != null && !cloudId.trim().isEmpty();
	}
	
	public String getLeaguevineJson() {
		return leaguevineJson;
	}
	public void setLeaguevineJson(String leaguevineJson) {
		this.leaguevineJson = leaguevineJson;
	}
	public boolean isPlayersAreLeaguevine() {
		return isPlayersAreLeaguevine;
	}
	public void setPlayersAreLeaguevine(boolean isPlayersAreLeaguevine) {
		this.isPlayersAreLeaguevine = isPlayersAreLeaguevine;
	}
	public List<Player> getPlayersSorted() {
		List<Player> playersSorted = getPlayers();
		Collections.sort(playersSorted, new Comparator<Player>() {
			@Override
			public int compare(Player lhs, Player rhs) {
				return lhs.getName().compareToIgnoreCase(rhs.getName());
			}
		});
		return playersSorted;
	}
	public Player getPlayer(String playerName) {
		for (Player player : getPlayers()) {
			if (playerName.equalsIgnoreCase(player.getName())) {
				return player;
			}
		}
		return null;
	}
	public static Player getPlayerNamed(String playerName) {
		if (playerName == null) {
			return Player.anonymous();
		}
		Player player = Team.current().getPlayer(playerName);
		if (player == null) {
			player = new Player(playerName);
			Team.current().addPlayer(player);
		}
		return player;
	}
	
	public boolean addPlayer(Player player) {
		if (player.isAnonymous()) {
			return false;
		} else {
			Boolean replaced = removePlayer(player);
			getPlayers().add(player); 
			return replaced;
		}
	}
	
	public boolean removePlayer(Player player) {
		Boolean replaced = getPlayers().remove(player);
		return replaced;
	}
	
	public boolean isDuplicatePlayerName(String newPlayerName, Player playerToIgnore) {
		for (Player player : getPlayers()) {
			if (playerToIgnore == null || playerToIgnore != player) {
				if (player.getName().equalsIgnoreCase(newPlayerName)) {
					return true;
				}
			} 
		}
		return false;
	}
	
	public List<Player> getDefaultLine() {
		List<Player> sortedPlayers = getPlayersSorted(); 
		List<Player> line = new ArrayList<Player>();
	    int maleCount = 0;
	    int femaleCount = 0;
	    for (Player player : sortedPlayers) {
	    	if (!player.isAnonymous()) {
				if (isMixed()) {
					if (player.isMale() && maleCount < 4) {
						line.add(player);
						maleCount++;
					} else if (!player.isMale() && femaleCount < 4) {
						line.add(player);
						femaleCount++;
					}
				} else {
					line.add(player);
				}
				if (line.size() >= 7) {
					break;
				}
	    	}
		}
	    return line;
	}
	
	public void ensureValid() {
		for (Player player : new ArrayList<Player>(getPlayers())) {
			if (player.isAnonymous()) {
				removePlayer(player);
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void readExternal(ObjectInput input) throws IOException, ClassNotFoundException {
		@SuppressWarnings("unused")
		byte version = input.readByte();  // if vars change use this to decide how far to read
		teamId = (String)input.readObject();
		name = (String)input.readObject();
		cloudId = (String)input.readObject();
		isMixed = input.readBoolean();
		isDisplayingPlayerNumber = input.readBoolean();		
		players = (List<Player>)input.readObject();
		isPlayersAreLeaguevine = input.readBoolean();	
		leaguevineJson = (String)input.readObject();
	}

	@Override
	public void writeExternal(ObjectOutput output) throws IOException {
		output.writeByte(OBJECT_STATE_VERSION);
		output.writeObject(teamId);
		output.writeObject(name);
		output.writeObject(cloudId);
		output.writeBoolean(isMixed);
		output.writeBoolean(isDisplayingPlayerNumber);
		output.writeObject(players);
		output.writeBoolean(isPlayersAreLeaguevine);
		output.writeObject(leaguevineJson);
	}

	public JSONObject toJsonObject() throws JSONException {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put(JSON_TEAM_ID, teamId);
		jsonObject.put(JSON_CLOUD_ID, cloudId);
		jsonObject.put(JSON_NAME, name);
		if (players != null && !players.isEmpty()) {
			JSONArray jsonPlayers = new JSONArray();
			for (Player player : players) {
				if (!player.isAnonymous()) {
					jsonPlayers.put(player.toJsonObject());
				}
			}
			jsonObject.put(JSON_PLAYERS, jsonPlayers);
		}
		jsonObject.put(JSON_IS_MIXED, isMixed);
		jsonObject.put(JSON_IS_DISPLAYING_PLAYER_NUMBER, isDisplayingPlayerNumber);
		jsonObject.put(JSON_IS_LEAGUEVINE_PLAYERS, isPlayersAreLeaguevine);
		jsonObject.put(JSON_LEAGUEVINE_JSON, leaguevineJson);
		return jsonObject;
	}
	
	public static Team fromJsonObject(JSONObject jsonObject) throws JSONException {
		if (jsonObject == null) {
			return null;
		} else {
			Team team = new Team();
			if (jsonObject.has(JSON_TEAM_ID)) {
				team.setTeamId(jsonObject.getString(JSON_TEAM_ID));
			}
			if (jsonObject.has(JSON_CLOUD_ID)) {
				team.setCloudId(jsonObject.getString(JSON_CLOUD_ID));
			}
			if (jsonObject.has(JSON_NAME)) {
				team.setName(jsonObject.getString(JSON_NAME));
			}
			if (jsonObject.has(JSON_PLAYERS)) {
				JSONArray jsonArray = jsonObject.getJSONArray(JSON_PLAYERS);
				for (int i = 0; i < jsonArray.length(); i++) {
					Player player = Player.fromJsonObject((JSONObject)jsonArray.get(i));
					if (player.getName() != null && !player.getName().trim().isEmpty() && !player.isAnonymous()) {
						team.players.add(player);
					}
				}
			}
			if (jsonObject.has(JSON_IS_MIXED)) {
				team.setMixed(jsonObject.getBoolean(JSON_IS_MIXED));
			}
			if (jsonObject.has(JSON_IS_DISPLAYING_PLAYER_NUMBER)) {
				team.setDisplayingPlayerNumber(jsonObject.getBoolean(JSON_IS_DISPLAYING_PLAYER_NUMBER));
			}
			if (jsonObject.has(JSON_IS_LEAGUEVINE_PLAYERS)) {
				team.setPlayersAreLeaguevine(jsonObject.getBoolean(JSON_IS_LEAGUEVINE_PLAYERS));
			}
			if (jsonObject.has(JSON_LEAGUEVINE_JSON)) {
				team.setLeaguevineJson(jsonObject.getString(JSON_LEAGUEVINE_JSON));
			}

			return team;
		}
	}
	
	public static Comparator<Team> TeamListComparator = new Comparator<Team>() {
		public int compare(Team team1, Team team2) {
			return team1.getName().compareTo(team2.getName());
		}
	};
}
