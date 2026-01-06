package com.summithillsoftware.ultimate.model;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

public class PlayerSubstitution implements Externalizable {
	private static final long serialVersionUID = 1l;
	private static final byte OBJECT_STATE_VERSION = 1;	
	
	private static final String JSON_FROM_PLAYER = "fromPlayer";
	private static final String JSON_TO_PLAYER = "toPlayer";
	private static final String JSON_SUB_REASON = "reason";
	private static final String JSON_SUB_REASON_INJURY = "injury";
	private static final String JSON_SUB_REASON_OTHER = "other";
	private static final String JSON_TIMESTAMP = "timestamp";

	private Player fromPlayer;
	private Player toPlayer;
	private SubstitutionReason reason;
	private int timestamp;
	
	public void useSharedPlayers() {
		fromPlayer = Player.replaceWithSharedPlayer(fromPlayer);
		toPlayer = Player.replaceWithSharedPlayer(toPlayer);
	}
	
	public Set<Player> getPlayers() {
		HashSet<Player> players = new HashSet<Player>();
		if (fromPlayer != null) {
			players.add(fromPlayer);
		}
		if (toPlayer != null) {
			players.add(toPlayer);
		}
		return players;
	}
	
	public Player getFromPlayer() {
		return fromPlayer;
	}
	public void setFromPlayer(Player fromPlayer) {
		this.fromPlayer = fromPlayer;
	}
	public SubstitutionReason getReason() {
		return reason;
	}
	public void setReason(SubstitutionReason reason) {
		this.reason = reason;
	}
	public int getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(int timestamp) {
		this.timestamp = timestamp;
	}
	public Player getToPlayer() {
		return toPlayer;
	}
	public void setToPlayer(Player toPlayer) {
		this.toPlayer = toPlayer;
	}
	
	@Override
	public String toString() {
		return "PlayerSubstitution [fromPlayer=" + fromPlayer + ", toPlayer="
				+ toPlayer + ", reason=" + reason + ", timestamp=" + timestamp
				+ "]";
	}

	@Override
	public int hashCode() {
		return timestamp;
	}
	
	public static Comparator<PlayerSubstitution> TimestampAndNameComparator = new Comparator<PlayerSubstitution>() {
		public int compare(PlayerSubstitution playerSub1, PlayerSubstitution playerSub2) {
			if (playerSub1.getTimestamp() == playerSub2.getTimestamp()) {
				return playerSub1.getFromPlayer().getName().compareTo(playerSub2.getFromPlayer().getName());
			}
			// descending order
			return Integer.valueOf(playerSub2.getTimestamp()).compareTo(Integer.valueOf(playerSub1.getTimestamp()));
		}
	};
	
	@Override
	public void readExternal(ObjectInput input) throws IOException, ClassNotFoundException {
		@SuppressWarnings("unused")
		byte version = input.readByte();  // if vars change use this to decide how far to read
		fromPlayer = (Player)input.readObject();
		toPlayer = (Player)input.readObject();
		reason = (SubstitutionReason)input.readObject();
		timestamp = input.readInt();
	}

	@Override
	public void writeExternal(ObjectOutput output) throws IOException {
		output.writeByte(OBJECT_STATE_VERSION);
		output.writeObject(fromPlayer);
		output.writeObject(toPlayer);
		output.writeObject(reason);
		output.writeInt(timestamp);
	}

	public JSONObject toJsonObject() throws JSONException {
		JSONObject jsonObject = new JSONObject();
		if (fromPlayer != null) {
			jsonObject.put(JSON_FROM_PLAYER, fromPlayer.getName());	
		}
		if (toPlayer != null) {
			jsonObject.put(JSON_TO_PLAYER, toPlayer.getName());	
		}
		if (reason != null) {
			jsonObject.put(JSON_SUB_REASON, reason == SubstitutionReason.SubstitutionReasonInjury ? JSON_SUB_REASON_INJURY : JSON_SUB_REASON_OTHER);	
		}
		jsonObject.put(JSON_TIMESTAMP, timestamp);
		return jsonObject;
	}
	
	public static PlayerSubstitution fromJsonObject(JSONObject jsonObject) throws JSONException {
		if (jsonObject == null) {
			return null;
		} else {
			PlayerSubstitution sub = new PlayerSubstitution();
			if (jsonObject.has(JSON_FROM_PLAYER)) {
				String playerName = jsonObject.getString(JSON_FROM_PLAYER);
				sub.fromPlayer = Team.getPlayerNamed(playerName);
			}
			if (jsonObject.has(JSON_TO_PLAYER)) {
				String playerName = jsonObject.getString(JSON_TO_PLAYER);
				sub.toPlayer = Team.getPlayerNamed(playerName);				
			}			
			if (jsonObject.has(JSON_SUB_REASON)) {
				sub.reason = jsonObject.getString(JSON_SUB_REASON).equals(JSON_SUB_REASON_INJURY) ? 
						SubstitutionReason.SubstitutionReasonInjury : SubstitutionReason.SubstitutionReasonOther;
			}
			if (jsonObject.has(JSON_TIMESTAMP)) {
				sub.timestamp = jsonObject.getInt(JSON_TIMESTAMP);
			}

			return sub;
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PlayerSubstitution other = (PlayerSubstitution) obj;
		if (fromPlayer == null) {
			if (other.fromPlayer != null)
				return false;
		} else if (!fromPlayer.equals(other.fromPlayer))
			return false;
		if (reason != other.reason)
			return false;
		if (timestamp != other.timestamp)
			return false;
		if (toPlayer == null) {
			if (other.toPlayer != null)
				return false;
		} else if (!toPlayer.equals(other.toPlayer))
			return false;
		return true;
	}


	
}
