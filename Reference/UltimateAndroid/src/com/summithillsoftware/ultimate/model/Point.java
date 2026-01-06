package com.summithillsoftware.ultimate.model;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.summithillsoftware.ultimate.util.UniqueTimestampGenerator;

public class Point implements Externalizable {
	private static final long serialVersionUID = 1l;
	private static final byte OBJECT_STATE_VERSION = 1;
	
	private static final String JSON_SUMMARY = "summary";
	private static final String JSON_EVENTS = "events";
	private static final String JSON_LINE = "line";
	private static final String JSON_SUBSTITUTIONS = "substitutions";
	private static final String JSON_START_SECONDS = "startSeconds";
	private static final String JSON_END_SECONDS = "endSeconds";
	
	private List<Event> events;
	private List<Player> line;
	private List<PlayerSubstitution> substitutions;
	private int timeStartedSeconds; // since epoch
	private int timeEndedSeconds; // since epoch	
	private transient PointSummary summary;
	
	public Point() {
		super();
		events = new ArrayList<Event>();
		line = new ArrayList<Player>();
		substitutions = new ArrayList<PlayerSubstitution>();
	}
	
	public void addEvent(Event event) {
		int now = UniqueTimestampGenerator.current().uniqueTimeIntervalSinceReferenceDateSeconds();
		// first event of point?
		if (events.size() == 0) {
	        // if O-line, reduce the start time to account for the pull
	        if (!event.isPull()) {
	            now -= 5;  // assume 5 seconds
	        }
	        timeStartedSeconds = now;
		}
		event.setTimestamp(now);
		timeEndedSeconds = UniqueTimestampGenerator.current().uniqueTimeIntervalSinceReferenceDateSeconds();
		events.add(event);
	}
	
	public Event getEventAtMostRecentIndex(int index) {
		// events are stored in ascending order but we are being asked for an index in descending order
		if (events.size() > index ) {
			return events.get(events.size() - index - 1);
		} else {
			return null;
		}
	}
	
	public Event getLastEvent() {
		if (events.size() > 0) {
			return events.get(events.size() -1);
		} else {
			return null;
		}
	}
	
	public List<Event>getEventsInMostRecentOrder() {
		List<Event> eventsList = new ArrayList<Event>(events);
		Collections.reverse(eventsList);
		return eventsList;
	}
	
	public Event getLastPlayEvent() {
		for (int i = events.size() - 1; i >= 0; i--) {
			Event evt = events.get(i);
			if (evt.isPlayEvent()) {
				return evt;
			}
		}
		return null;
	}

	public List<Event> getLastEvents(int numberToRetrieve) {
		int actualNumber = Math.min(events.size(),numberToRetrieve);
		if (actualNumber > 0) {
			List<Event> subList = new ArrayList<Event>((List<Event>) events.subList(events.size() - actualNumber, events.size()));
			Collections.reverse(subList);
			return subList;
		} else {
			return Collections.emptyList();
		}
	}
	
	public void removeLastEvent() {
		if (events.size() > 0) {
			events.remove(events.size() -1);
		}
	}
	
	public boolean isFinished() {
		return events.size() > 0 && getLastEvent().isFinalEventOfPoint();
	}
	
	public boolean isFinishedWithGoal() {
		return isFinished() && getLastEvent().isGoal();
	}
	
	public boolean isOurPoint() {
		return getLastPlayEvent().isOurGoal();
	}
	
	public boolean isTheirPoint() {
		return getLastPlayEvent().isTheirGoal();
	}
	
	public int numberOfEvents() {
		return events.size();
	}

	public Set<Player>playersInEntirePoint() {
		HashSet<Player> players = new HashSet<Player>(line);
		if (substitutions.size() > 0) {
			for (PlayerSubstitution sub : substitutions) {
				players.remove(sub.getFromPlayer());
				players.remove(sub.getToPlayer());
			}
		}
		return players;
	}
	
	public Set<Player>playersInPartOfPoint() {
		HashSet<Player> players = new HashSet<Player>();
		if (substitutions.size() > 0) {
			for (PlayerSubstitution sub : substitutions) {
				players.add(sub.getFromPlayer());
				players.add(sub.getToPlayer());
			}
		}
		return players;
	}
	
	public boolean isPeriodEnd() {
		return events.size() > 0 && getLastEvent().isPeriodEnd();
	}
	
	public boolean isOnlyPeriodEnd() {
		return events.size() == 1 && getLastEvent().isPeriodEnd();
	}
	
	public CessationEvent getPeriodEnd() {
		return isPeriodEnd() ? (CessationEvent)getLastEvent() : null;
	}
	
	public void useSharedPlayers() {
		for (Event evt : events) {
			evt.useSharedPlayers();
		}
		for (PlayerSubstitution sub : substitutions) {
			sub.useSharedPlayers();
		}
	}
	
	public Set<Player> getPlayers() {
		HashSet<Player> players = new HashSet<Player>();
		players.addAll(line);
		for (Event event : events) {
			players.addAll(event.getPlayers());
		}
		for (PlayerSubstitution sub : substitutions) {
			players.addAll(sub.getPlayers());
		}
		return players;
	}
	
	public PlayerSubstitution lastSubstitution() {
		return substitutions.isEmpty() ? null : substitutions.get(substitutions.size() -1);
	}
	
	public void setEvents(List<Event> events) {
		this.events = events;
	}
	public List<Event> getEvents() {
		return events;
	}
	public List<Player> getLine() {
		return line;
	}
	public void setLine(List<Player> line) {
		this.line = new ArrayList<Player>(line);
	}
	public List<PlayerSubstitution> getSubstitutions() {
		return substitutions;
	}
	public void setSubstitutions(List<PlayerSubstitution> substitutions) {
		this.substitutions = substitutions;
	}
	public int getTimeStartedSeconds() {
		return timeStartedSeconds;
	}
	public void setTimeStartedSeconds(int timeStartedSeconds) {
		this.timeStartedSeconds = timeStartedSeconds;
	}
	public int getTimeEndedSeconds() {
		return timeEndedSeconds;
	}
	public void setTimeEndedSeconds(int timeEndedSeconds) {
		this.timeEndedSeconds = timeEndedSeconds;
	}
	public PointSummary getSummary() {
		return summary;
	}
	public void setSummary(PointSummary summary) {
		this.summary = summary;
	}
	
	@SuppressWarnings("unchecked")
	public void readExternal(ObjectInput input) throws IOException, ClassNotFoundException {
		@SuppressWarnings("unused")
		byte version = input.readByte();  // if vars change use this to decide how far to read
		events = (List<Event>)input.readObject();
		line = (List<Player>)input.readObject();
		substitutions = (List<PlayerSubstitution>)input.readObject();
		timeStartedSeconds = input.readInt();
		timeEndedSeconds = input.readInt();
	}

	public void writeExternal(ObjectOutput output) throws IOException {
		output.writeByte(OBJECT_STATE_VERSION);
		output.writeObject(events);
		output.writeObject(line);
		output.writeObject(substitutions);
		output.writeInt(timeStartedSeconds);
		output.writeInt(timeEndedSeconds);
	}
	
	public JSONObject toJsonObject() throws JSONException {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put(JSON_START_SECONDS, timeStartedSeconds);
		jsonObject.put(JSON_END_SECONDS, timeEndedSeconds);	
		if (events != null && !events.isEmpty()) {
			JSONArray jsonArray = new JSONArray();
			for (Event event : events) {
				jsonArray.put(event.toJsonObject());
			}
			jsonObject.put(JSON_EVENTS, jsonArray);
		}
		if (line != null && !line.isEmpty()) {
			JSONArray jsonArray = new JSONArray();
			for (Player player : line) {
				jsonArray.put(player.getName());
			}
			jsonObject.put(JSON_LINE, jsonArray);
		}
		if (substitutions != null && !substitutions.isEmpty()) {
			JSONArray jsonArray = new JSONArray();
			for (PlayerSubstitution sub : substitutions) {
				jsonArray.put(sub.toJsonObject());
			}
			jsonObject.put(JSON_SUBSTITUTIONS, jsonArray);
		}		
		
		if (summary != null) {
			jsonObject.put(JSON_SUMMARY, summary.toJsonObject());
		}

		return jsonObject;
	}
	
	public static Point fromJsonObject(JSONObject jsonObject) throws JSONException {
		if (jsonObject == null) {
			return null;
		} else {
			Point point = new Point();
			if (jsonObject.has(JSON_START_SECONDS)) {
				point.timeStartedSeconds = jsonObject.getInt(JSON_START_SECONDS);
			}
			if (jsonObject.has(JSON_END_SECONDS)) {
				point.timeEndedSeconds = jsonObject.getInt(JSON_END_SECONDS);
			}			
			if (jsonObject.has(JSON_EVENTS)) {
				JSONArray eventsAsJson = jsonObject.getJSONArray(JSON_EVENTS);
				for (int i = 0; i < eventsAsJson.length(); i++) {
					JSONObject eventAsJson = eventsAsJson.getJSONObject(i);
					point.events.add(Event.fromJsonObject(eventAsJson));
				}
			}
			if (jsonObject.has(JSON_LINE)) {
				JSONArray eventsAsJson = jsonObject.getJSONArray(JSON_LINE);
				for (int i = 0; i < eventsAsJson.length(); i++) {
					String playerName = eventsAsJson.getString(i);
					point.getLine().add(Team.getPlayerNamed(playerName));
				}
			}
			if (jsonObject.has(JSON_SUBSTITUTIONS)) {
				JSONArray eventsAsJson = jsonObject.getJSONArray(JSON_SUBSTITUTIONS);
				for (int i = 0; i < eventsAsJson.length(); i++) {
					JSONObject subAsJson = eventsAsJson.getJSONObject(i);
					point.getSubstitutions().add(PlayerSubstitution.fromJsonObject(subAsJson));
				}
			}

			return point;
		}
	}

	@Override
	public String toString() {
		return "Point [events=" + events + ", line=" + line
				+ ", substitutions=" + substitutions + ", timeStartedSeconds="
				+ timeStartedSeconds + ", timeEndedSeconds=" + timeEndedSeconds
				+ "]";
	}

}
