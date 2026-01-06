package com.summithillsoftware.ultimate.model;

public class PointEvent {
	private Event event;
	private Point point;
	
	public PointEvent(Point point, Event event) {
		super();
		this.event = event;
		this.point = point;
	}

	public Event getEvent() {
		return event;
	}

	public void setEvent(Event event) {
		this.event = event;
	}

	public Point getPoint() {
		return point;
	}

	public void setPoint(Point point) {
		this.point = point;
	}

	@Override
	public String toString() {
		return "PointEvent [event=" + event + ", point=" + point + "]";
	}

}
