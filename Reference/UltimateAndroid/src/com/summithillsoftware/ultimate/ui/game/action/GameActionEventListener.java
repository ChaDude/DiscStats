package com.summithillsoftware.ultimate.ui.game.action;

import com.summithillsoftware.ultimate.model.Event;
import com.summithillsoftware.ultimate.model.Player;
import com.summithillsoftware.ultimate.model.PointEvent;

public interface GameActionEventListener {
	
	public void newEvent(Event event);
	
	public void potentialNewEvent(Event event);
	
	public void removeLastEvent();
	
	public void onEventEditRequest(PointEvent event);
	
	public void initialOffensePlayerSelected(Player player);
	
	public void timeoutInfoRequested();
	
	public void cessationRequested();

}
