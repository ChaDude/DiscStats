package com.summithillsoftware.ultimate.model;

import static com.summithillsoftware.ultimate.model.Action.Callahan;
import static com.summithillsoftware.ultimate.model.Action.Catch;
import static com.summithillsoftware.ultimate.model.Action.Drop;
import static com.summithillsoftware.ultimate.model.Action.EndOfFirstQuarter;
import static com.summithillsoftware.ultimate.model.Action.EndOfFourthQuarter;
import static com.summithillsoftware.ultimate.model.Action.EndOfOvertime;
import static com.summithillsoftware.ultimate.model.Action.EndOfThirdQuarter;
import static com.summithillsoftware.ultimate.model.Action.GameOver;
import static com.summithillsoftware.ultimate.model.Action.Halftime;
import static com.summithillsoftware.ultimate.model.Action.MiscPenalty;
import static com.summithillsoftware.ultimate.model.Action.Pull;
import static com.summithillsoftware.ultimate.model.Action.PullOb;
import static com.summithillsoftware.ultimate.model.Action.Stall;
import static com.summithillsoftware.ultimate.model.Action.Throwaway;
import static com.summithillsoftware.ultimate.model.Action.Timeout;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Constructor;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.summithillsoftware.ultimate.R;
import com.summithillsoftware.ultimate.UltimateApplication;
import com.summithillsoftware.ultimate.util.UltimateLogger;

public abstract class Event implements Externalizable {
	private static final long serialVersionUID = 1l;
	private static final byte OBJECT_STATE_VERSION = 1;
	
	private static final String JSON_EVENT_TYPE = "type";
	private static final String JSON_EVENT_TYPE_CESSATION = "Cessation";
	private static final String JSON_EVENT_TYPE_OFFENSE = "Offense";
	private static final String JSON_EVENT_TYPE_DEFENSE = "Defense";
	private static final String JSON_EVENT_TIMESTAMP = "timestamp";
	private static final String JSON_IS_HALFTIME_CAUSE = "halftimeCause";
	protected static final String JSON_ACTION = "action";
	protected static final String JSON_DETAILS = "details";
	
	public static final EnumSet<Action> CESSATION_ACTIONS = EnumSet.of(
			EndOfFirstQuarter,
			Halftime,
			EndOfThirdQuarter,
			EndOfFourthQuarter,
			EndOfOvertime,
			GameOver,
			Timeout);
	
	public static final EnumSet<Action> TURNOVER_ACTIONS = EnumSet.of(
			Drop,
			Throwaway,
			Stall,
			MiscPenalty,
			Callahan);
		
	private Action action;
	private long timestamp;
	private Map<String, Object> details;
	private boolean isHalftimeCause;
	
	public Event() {
		super();
		timestamp = System.currentTimeMillis();
	}
	
	public Event(Action action) {
		this();
		this.action = action;
	}
	
	public Event(Event event) {
		super();
		action = event.action;
		timestamp = event.timestamp;
		if (event.details != null) {
			details = new HashMap<String, Object>(event.details); 
		}
		isHalftimeCause = event.isHalftimeCause;
	}

	public Action getAction() {
		return action;
	}

	public void setAction(Action action) {
		this.action = action;
		ensureValid();
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public Map<String, Object> getDetails() {
		if (details == null) {
			details= new HashMap<String, Object>();
		}
		return details;
	}
	
	public boolean isHalftime() {
		return false;
	}

	public boolean isHalftimeCause() {
		return isHalftimeCause;
	}

	public void setHalftimeCause(boolean isHalftimeCause) {
		this.isHalftimeCause = isHalftimeCause;
	    if (Game.hasCurrentGame()) {
	        Game.current().clearPointSummaries();
	    }
	}

	public boolean isOffense() {
		return false;
	}
	
	public boolean isDefense() {
		return false;
	}
	
	public boolean isGoal() {
		return false;
	}

	public boolean isD() {
		return false;
	}
	
	public boolean isCatch() {
		return false;
	}

	public boolean isDrop() {
		return false;
	}
	
	public boolean isThrowaway() {
		return action == Throwaway;
	}
	
	public boolean isOffenseThrowaway() {
		return action == Throwaway && isOffense();
	}
	
	public boolean isDefenseThrowaway() {
		return action == Throwaway && isDefense();
	}
	
	public boolean isOurGoal() {
		return false;
	}
	
	public boolean isTheirGoal() {
		return false;
	}
	
	public abstract boolean isTurnover();
	
	public boolean isPull() {
		return false;
	}

	public boolean isPullIb() {
		return false;
	}
	
	public boolean isPullOb() {
		return false;
	}
	
	public boolean isCallahan() {
		return false;
	}
	
	public boolean isFinalEventOfPoint() {
		return false;
	}

	public boolean isCessationEvent() {
		return false;
	}
	
	public boolean isPlayEvent() {
		return false;
	}
	
	public boolean isPeriodEnd() {
		return false;
	}
	
	public boolean causesDirectionChange() {
		return !(action == Catch || action == Pull || action == PullOb);
	}
	
	public boolean causesLineChange() {
		return isGoal();
	}
	
	public abstract boolean isNextEventOffense();
	
	public abstract List<Player> getPlayers();
	
	public abstract Player getPlayerOne();
	
	public abstract Player getPlayerTwo();
	
	public boolean isAnonymous() {
		return false;
	}
	
	protected void ensureValid() {
		// no-op...subclasses can re-implement
	}

	public Player playerOne() {
		return null;
	}
	
	public Player playerTwo() {
		return null;
	}
	
	public void setDetailIntValue(String key, int value) {
		getDetails().put(key, value);
	}
	
	public void setDetailBooleanValue(String key, boolean value) {
		getDetails().put(key, value);
	}
	
	public int getDetailIntValue(String key, int defaultValue) {
		Integer value = (Integer)getDetails().get(key);
		return value == null ? defaultValue : value.intValue();
	}
	
	public boolean getDetailBooleanValue(String key, boolean defaultValue) {
		Boolean value = (Boolean)getDetails().get(key);
		return value == null ? defaultValue : value.booleanValue();
	}

	protected abstract String getDescriptionForTeamAndOpponent(String teamName, String opponentName);

	protected String getDescription() {
		return getDescriptionForTeamAndOpponent(Team.current().getName(), Game.hasCurrentGame() ? Game.current().getOpponentName() : "");
	}
	
	public String toString() {
		return getDescription();
	}
	
	public String getString(int resId) {
		return UltimateApplication.current().getString(resId);
	}
	
	public String getString(int resId, Object...formatArgs) {
		return UltimateApplication.current().getString(resId, formatArgs);
	}
	
	public int image() {
		return R.drawable.unknown_event;
	}
	
	public int imageMonochrome() {
		return image();
	}
	
	public Event copy() {
		Class<? extends Event> clz = this.getClass(); 
		try {
			Constructor<?> cons = clz.getConstructor(clz);
			return (Event)cons.newInstance(this);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public abstract void useSharedPlayers();
	
	@SuppressWarnings("unchecked")
	@Override
	public void readExternal(ObjectInput input) throws IOException, ClassNotFoundException {
		@SuppressWarnings("unused")
		byte version = input.readByte();  // if vars change use this to decide how far to read
		action = (Action)input.readObject();
		details = (Map<String, Object>)input.readObject();
		timestamp = input.readLong();
		isHalftimeCause = input.readBoolean();
	}

	@Override
	public void writeExternal(ObjectOutput output) throws IOException {
		output.writeByte(OBJECT_STATE_VERSION);
		output.writeObject(action);
		output.writeObject(details);
		output.writeLong(timestamp);
		output.writeBoolean(isHalftimeCause);
	}

	public JSONObject toJsonObject() throws JSONException {
		JSONObject jsonObject = new JSONObject();
		if (isHalftimeCause) {
			jsonObject.put(JSON_IS_HALFTIME_CAUSE, isHalftimeCause);
		}
		jsonObject.put(JSON_EVENT_TIMESTAMP, timestamp);
		String typeAsString = null;
		if (isOffense()) {
			typeAsString = JSON_EVENT_TYPE_OFFENSE;
		} else if (isDefense()) {
			typeAsString = JSON_EVENT_TYPE_DEFENSE;
		} else {
			typeAsString = JSON_EVENT_TYPE_CESSATION;
		}
		jsonObject.put(JSON_EVENT_TYPE, typeAsString);

		return jsonObject;
	}
	
	protected void addEventDetailToJsonObject(JSONObject eventJsonObject, String detailName, Object detailValue) throws JSONException {
		if (!eventJsonObject.has(JSON_DETAILS)) {
			eventJsonObject.put(JSON_DETAILS, new JSONObject());
		} 
		JSONObject detailsObject = eventJsonObject.getJSONObject(JSON_DETAILS);
		detailsObject.put(detailName, detailValue);
	}
	
	public static Event fromJsonObject(JSONObject jsonObject) throws JSONException {
		if (jsonObject == null) {
			return null;
		} else {
			String type = jsonObject.getString(JSON_EVENT_TYPE);
			if (type.equals(JSON_EVENT_TYPE_OFFENSE)) {
				return OffenseEvent.eventfromJsonObject(jsonObject);
			} else if (type.equals(JSON_EVENT_TYPE_DEFENSE)) {
				return DefenseEvent.eventfromJsonObject(jsonObject);
			} else if (type.equals(JSON_EVENT_TYPE_CESSATION)) {
				return CessationEvent.eventfromJsonObject(jsonObject);
			} else {
				UltimateLogger.logError( "No valid event type in json");
				return null;
			}
		}
	}
	
	protected static Integer retrieveIntEventDetailFromJsonObject(JSONObject eventJsonObject, String detailName) throws JSONException {
		if (!eventJsonObject.has(JSON_DETAILS)) {
			return null;
		} 
		JSONObject detailsObject = eventJsonObject.getJSONObject(JSON_DETAILS);
		if (detailsObject.has(detailName)) {
			return Integer.valueOf(detailsObject.getInt(detailName));
		} else {
			return null;
		}
	}
	
	protected static void populateGeneralPropertiesFromJsonObject(Event event, JSONObject jsonObject) throws JSONException {
		if (jsonObject.has(JSON_EVENT_TIMESTAMP)) {
			event.timestamp = jsonObject.getLong(JSON_EVENT_TIMESTAMP);
		}
		if (jsonObject.has(JSON_IS_HALFTIME_CAUSE)) {
			event.isHalftimeCause = jsonObject.getBoolean(JSON_IS_HALFTIME_CAUSE);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (timestamp ^ (timestamp >>> 32));
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
		Event other = (Event) obj;
		if (timestamp != other.timestamp)
			return false;
		return true;
	}


}
