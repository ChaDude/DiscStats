package com.summithillsoftware.ultimate.stats;

import java.util.List;
import java.util.Map;

import com.summithillsoftware.ultimate.model.DefenseEvent;
import com.summithillsoftware.ultimate.model.Event;
import com.summithillsoftware.ultimate.model.Game;
import com.summithillsoftware.ultimate.model.OffenseEvent;
import com.summithillsoftware.ultimate.model.Player;
import com.summithillsoftware.ultimate.model.Point;

public class StatsEventDetails {
	private Game game;
	private Point point;
	private Event event;
	private boolean isFirstEventOfPoint;
	private boolean isOlinePoint;
	private Map<String, PlayerStat> accumulatedStats;
	private List<Player> line;
	
	public Game getGame() {
		return game;
	}
	public void setGame(Game game) {
		this.game = game;
	}
	public Point getPoint() {
		return point;
	}
	public void setPoint(Point point) {
		this.point = point;
	}
	public Event getEvent() {
		return event;
	}
	public OffenseEvent getOffenseEvent() {
		return (OffenseEvent)event;
	}
	public DefenseEvent getDefenseEvent() {
		return (DefenseEvent)event;
	}
	public boolean isOffense() {
		return event.isOffense();
	}
	public boolean isDefense() {
		return event.isDefense();
	}	
	public void setEvent(Event event) {
		this.event = event;
	}
	public boolean isFirstEventOfPoint() {
		return isFirstEventOfPoint;
	}
	public void setFirstEventOfPoint(boolean isFirstEventOfPoint) {
		this.isFirstEventOfPoint = isFirstEventOfPoint;
	}
	public boolean isOlinePoint() {
		return isOlinePoint;
	}
	public boolean isDlinePoint() {
		return !isOlinePoint;
	}	
	public void setOlinePoint(boolean isOlinePoint) {
		this.isOlinePoint = isOlinePoint;
	}
	public Map<String, PlayerStat> getAccumulatedStats() {
		return accumulatedStats;
	}
	public void setAccumulatedStats(Map<String, PlayerStat> accumulatedStats) {
		this.accumulatedStats = accumulatedStats;
	}
	public List<Player> getLine() {
		return line;
	}
	public void setLine(List<Player> line) {
		this.line = line;
	}
}
