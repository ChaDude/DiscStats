package com.summithillsoftware.ultimate.model;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;

public class Player implements Externalizable {
	private static final long serialVersionUID = 1l;
	private static final byte OBJECT_STATE_VERSION = 2;
	
	private static final String JSON_NAME = "name";
	private static final String JSON_POSITION = "position";
	private static final String JSON_NUMBER = "number";
	private static final String JSON_IS_MALE = "male";
	private static final String JSON_IS_ABSENT = "absent";	
	private static final String JSON_LEAGUEVINE_PLAYER = "leaguevinePlayer";
	private static Player ANONYMOUS_PLAYER = null;
	private static final String ANON_NAME = "Anonymous";
	
	private String name;
	private String number;
	private boolean isMale;
	private String leaguevineJson;
	private PlayerPosition position;
	private boolean isAbsent;
	
	static {
		Player.ANONYMOUS_PLAYER = new Player();
		Player.ANONYMOUS_PLAYER.name = ANON_NAME;
	}
	
	public static Player replaceWithSharedPlayer(Player player) {
		if (player == null) {
			return anonymous();
		}
		if (player.isAnonymous()) {
			return anonymous();
		}
		// If the player is in the current team then use that instance.
		int teamPlayerIndex = Team.current().getPlayers().indexOf(player);
		if (teamPlayerIndex > -1) {
			Player teamPlayer = Team.current().getPlayers().get(teamPlayerIndex);
			return teamPlayer;
		} else {
			Team.current().addPlayer(player);
			return player;
		}
	}
	
	public static List<Player>replaceAllWithSharedPlayers(List<Player> players) {
		List<Player> replacementPlayers = new ArrayList<Player>();
		for (Player player : players) {
			replacementPlayers.add(replaceWithSharedPlayer(player));
		}
		return replacementPlayers;
	}
	
	public Player() {
		super();
		name = "";
		number = "";
		position = PlayerPosition.Any;
		isMale = true;
	}
	
	public Player(String name) {
		this();
		this.name = name;
	}
	
	public static Player anonymous() {
		return ANONYMOUS_PLAYER;
	}
	
	public boolean isAnonymous() {
		return ANON_NAME.equals(name);
	}
	
	public String getName() {
		return name;
	}
	public String getId() {
		return name;
	}
	public void setName(String name) {
		if (!this.name.equals(ANON_NAME) && !name.equals(ANON_NAME)) {  // don't allow changing name of anonymous player and don't allow a new player with this name
			this.name = name;
		}
	}
	public String getNumber() {
		return number;
	}
	public void setNumber(String number) {
		this.number = number;
	}
	public String getPlayerNumberDescription() {
		return getNumber() == null || getNumber().isEmpty() ? getName() : getNumber();
	}
	public boolean isMale() {
		return !isAnonymous() && isMale;
	}
	public void setMale(boolean isMale) {
		this.isMale = isMale;
	}
	public boolean isFemale() {
		return !isAnonymous() && !isMale;
	}
	public boolean isAbsent() {
		return isAbsent;
	}
	public void setAbsent(boolean isAbsent) {
		this.isAbsent = isAbsent;
	}	
	public String getLeaguevineJson() {
		return leaguevineJson;
	}
	public void setLeaguevineJson(String leaguevineJson) {
		this.leaguevineJson = leaguevineJson;
	}
	public PlayerPosition getPosition() {
		return position;
	}
	public void setPosition(PlayerPosition position) {
		this.position = position;
	}
	public boolean isDuplicateNewName(String newName, Team team) {
		if (newName.equalsIgnoreCase(name)) {
			return false;
		} else {
			return team.getPlayers().contains(new Player(newName));
		}
	}

	@SuppressLint("DefaultLocale")
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.toLowerCase(Locale.ENGLISH).hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Player other = (Player) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equalsIgnoreCase(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Player [name=" + name + "]";
	}

	public static Comparator<Player> PlayerNameComparator = new Comparator<Player>() {
		public int compare(Player player1, Player player2) {
			return player1.getName().compareTo(player2.getName());
		}
	};
	
	public static Comparator<Player> PlayerNumberComparator = new Comparator<Player>() {
		public int compare(Player player1, Player player2) {
			int player1Number = getNumber(player1.getNumber());
			int player2Number = getNumber(player2.getNumber());
			return Integer.valueOf(player1Number).compareTo(Integer.valueOf(player2Number));
		}
		
		private int getNumber(String numberAsString) {
			if (numberAsString == null || numberAsString.isEmpty()) {
				return 0;
			}
			try {
				return Integer.parseInt(numberAsString);
			} catch (Exception e) {
				return 0;
			}
		}
	};

	@Override
	public void readExternal(ObjectInput input) throws IOException, ClassNotFoundException {
		byte version = input.readByte();  // if vars change use this to decide how far to read
		name = (String)input.readObject();
		number = (String)input.readObject();
		isMale = input.readBoolean();
		leaguevineJson = (String)input.readObject();
		position = (PlayerPosition)input.readObject();
		if (version == 2) {
			isAbsent = input.readBoolean();
		}
	}

	@Override
	public void writeExternal(ObjectOutput output) throws IOException {
		output.writeByte(OBJECT_STATE_VERSION);
		output.writeObject(name);
		output.writeObject(number);
		output.writeBoolean(isMale);
		output.writeObject(leaguevineJson);		
		output.writeObject(position);
		output.writeBoolean(isAbsent);
	}
	
	public JSONObject toJsonObject() throws JSONException {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put(JSON_NAME, name);
		jsonObject.put(JSON_NUMBER, number);
		jsonObject.put(JSON_POSITION, position);
		jsonObject.put(JSON_IS_MALE, isMale);
		jsonObject.put(JSON_IS_ABSENT, isAbsent);
		jsonObject.put(JSON_LEAGUEVINE_PLAYER, leaguevineJson);
		return jsonObject;
	}
	
	public static Player fromJsonObject(JSONObject jsonObject) throws JSONException {
		if (jsonObject == null) {
			return null;
		} else {
			Player player = new Player();
			player.setName(jsonObject.getString(JSON_NAME));
			if (jsonObject.has(JSON_NUMBER)) {
				player.setNumber(jsonObject.getString(JSON_NUMBER));
			}
			if (jsonObject.has(JSON_POSITION)) {
				player.setPosition(PlayerPosition.valueOf(jsonObject.getString(JSON_POSITION)));
			}
			if (jsonObject.has(JSON_IS_MALE)) {
				player.setMale(jsonObject.getBoolean(JSON_IS_MALE));
			}
			if (jsonObject.has(JSON_IS_ABSENT)) {
				player.setAbsent(jsonObject.getBoolean(JSON_IS_ABSENT));
			}			
			if (jsonObject.has(JSON_LEAGUEVINE_PLAYER)) {
				player.setLeaguevineJson(jsonObject.getString(JSON_LEAGUEVINE_PLAYER));
			}			

			return player;
		}
	}


}
