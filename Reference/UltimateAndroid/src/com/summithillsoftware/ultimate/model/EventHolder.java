package com.summithillsoftware.ultimate.model;

import com.summithillsoftware.ultimate.util.UltimateLogger;

public class EventHolder {
	private Event event;
	private Event replacementEvent;
	private Point point;
	private boolean isFirstPoint;
	
	public EventHolder(Event event) {
		super();
		setEvent(event);
	}
	
	public EventHolder(PointEvent pointEvent) {
		this(pointEvent.getEvent());
		setPoint(pointEvent.getPoint());
	}
	
	public boolean validate() {
		if (event == null) {
			UltimateLogger.logError( "Invalid event holder...no event");
			return false;
		}
		if (point == null) {
			UltimateLogger.logError( "Invalid event holder...no point");
			return false;
		}
		return true;
	}
	
	public Event getEvent() {
		return event;
	}
	public void setEvent(Event event) {
		this.event = event;
		this.replacementEvent = event.copy();
	}
	public Point getPoint() {
		return point;
	}
	public void setPoint(Point point) {
		this.point = point;
	}
	public boolean isFirstPoint() {
		return isFirstPoint;
	}
	public void setFirstPoint(boolean isFirstPoint) {
		this.isFirstPoint = isFirstPoint;
	}

	public Event getReplacementEvent() {
		return replacementEvent;
	}

	public void setReplacementEvent(Event replacementEvent) {
		this.replacementEvent = replacementEvent;
	}


}
