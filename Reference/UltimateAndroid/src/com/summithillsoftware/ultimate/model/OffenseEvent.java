package com.summithillsoftware.ultimate.model;

import static com.summithillsoftware.ultimate.model.Action.Callahan;
import static com.summithillsoftware.ultimate.model.Action.Catch;
import static com.summithillsoftware.ultimate.model.Action.Drop;
import static com.summithillsoftware.ultimate.model.Action.Goal;
import static com.summithillsoftware.ultimate.model.Action.MiscPenalty;
import static com.summithillsoftware.ultimate.model.Action.Stall;
import static com.summithillsoftware.ultimate.model.Action.Throwaway;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.summithillsoftware.ultimate.R;
import com.summithillsoftware.ultimate.util.StringUtil;

public class OffenseEvent extends Event {
	private static final long serialVersionUID = 1l;
	private static final String JSON_PASSER = "passer";
	private static final String JSON_RECEIVER = "receiver";

	public static final EnumSet<Action> OFFENSE_ACTIONS = EnumSet.of(
			Catch,
			Drop,
			Goal,
			Throwaway,
			Callahan,
			Stall,
			MiscPenalty);
	
	private Player passer;
	private Player receiver;
	
	public OffenseEvent() {
	}
	
	public OffenseEvent(Action action, Player passer) {
		this(action, passer, null);
	}

	public OffenseEvent(Action action, Player passer, Player receiver) {
		super(action);
		this.passer = passer;
		this.receiver = receiver;
	}
	
	public OffenseEvent(OffenseEvent event) {
		super(event);
		passer = event.passer;
		receiver = event.receiver;
	}
	
	public boolean isOurGoal() {
		return getAction() == Goal;
	}
	
	public boolean isTheirGoal() {
		return getAction() == Callahan;
	}
	
	public boolean isGoal() {
		return isOurGoal() || isTheirGoal();
	}
	
	public boolean isCallahan() {
		return getAction() == Callahan;
	}
	
	public boolean isFinalEventOfPoint() {
		return isGoal();
	}

	public boolean isOffense() {
		return true;
	}
	
	public boolean isPlayEvent() {
		return true;
	}
	
	@Override
	public boolean isCatch() {
		return getAction() == Catch;
	}
	
	@Override
	public boolean isDrop() {
		return getAction() == Drop;
	}
	
	@Override
	public boolean isTurnover() {
	    return getAction() == Drop || getAction() == Throwaway || getAction() == Stall || getAction() == MiscPenalty || getAction() == Callahan;
	}

	@Override
	public boolean isNextEventOffense() {
		return getAction() == Catch || getAction() == Callahan;
	}

	@Override
	public List<Player> getPlayers() {
		List<Player> players = new ArrayList<Player>();
		if (passer != null) {
			players.add(passer);
		}
		if (receiver != null) {
			players.add(receiver);
		}
	    return players;
	}
	
	public boolean isAnonymous() {
	    return isPasserAnonymous() && isReceiverAnonymous();
	}
	
	public boolean isPasserAnonymous() {
	    return (passer == null || passer.isAnonymous());
	}
	
	public boolean isReceiverAnonymous() {
	    return (receiver == null || receiver.isAnonymous());
	}

	@Override
	protected String getDescriptionForTeamAndOpponent(String teamName,String opponentName) {
		String ourTeam = StringUtil.getString(R.string.event_description_our_team);
	    switch(getAction()) {
	        case Catch: {
	            if (isAnonymous()) {
	            	// {team} pass
	            	return getString(R.string.event_description_pass,(teamName == null ? ourTeam : teamName));
	            } else if (isReceiverAnonymous()) {
	            	// {passer} pass
	            	return getString(R.string.event_description_pass,getPasser().getName());
	            } else if (isPasserAnonymous()) {
	            	// Pass to {passer}
	            	return getString(R.string.event_description_pass_to, getReceiver().getName());
	            } else {
	            	// {passer} to {receiver}
	            	return getString(R.string.event_description_pass_from_to, getPasser().getName(), getReceiver().getName());
	            }
	        }
	        case Drop: {
	            if (isAnonymous()) {
	            	// {{team} drop
	            	return getString(R.string.event_description_drop,(teamName == null ? ourTeam : teamName));
	            } else if (isReceiverAnonymous()) {
	            	// {{passer} pass dropped
	            	return getString(R.string.event_description_drop_from,getPasser().getName());
	            } else if (isPasserAnonymous()) {
	            	// {{receiver} dropped pass            	
	            	return getString(R.string.event_description_drop_to, getReceiver().getName());
	            } else {
	            	// {{receiver} dropped from {passer}
	            	return getString(R.string.event_description_drop_from_to, getReceiver().getName(), getPasser().getName());    
	            }
	        }
	        case Throwaway:{
	        	return isAnonymous() ? 
	        			getString(R.string.event_description_throwaway,(teamName == null ? ourTeam : teamName)) : 
	        			getString(R.string.event_description_throwaway,getPasser().getName());
	        }
	        case Stall:{
	        	return isAnonymous() ? 
	        			getString(R.string.event_description_stalled,(teamName == null ? ourTeam : teamName)) : 
	        			getString(R.string.event_description_stalled,getPasser().getName());
	        }
	        case MiscPenalty:{
	        	return isAnonymous() ? 
	        			getString(R.string.event_description_penalized,(teamName == null ? ourTeam : teamName)) : 
	        			getString(R.string.event_description_penalized,getPasser().getName());
	        }
	        case Callahan:{
	        	return isAnonymous() ? 
	        			getString(R.string.event_description_callahaned,(teamName == null ? ourTeam : teamName)) : 
	        			getString(R.string.event_description_callahaned,getPasser().getName());
	        }
	        case Goal: {
	            if (isAnonymous()) {
	            	// {team} goal
	            	return getString(R.string.event_description_o_goal,(teamName == null ? ourTeam : teamName));
	            } else if (isReceiverAnonymous()) {
	            	// {passer} pass for goal
	            	return getString(R.string.event_description_o_goal_from,getPasser().getName());	            	
	            } else if (isPasserAnonymous()) {
	            	// {receiver} goal
	            	return getString(R.string.event_description_o_goal, getReceiver().getName());
	            } else {
	            	// {team} goal ({getPasser()} to {receiver})
	            	return getString(R.string.event_description_o_goal_from_to, (teamName == null ? ourTeam : teamName), getPasser().getName(), getReceiver().getName());
	            }
	        }
	        default:
	            return "";
	    }
	}

	public Player getPasser() {
		if (passer == null) {
			passer = Player.anonymous();
		}
		return passer;
	}

	public void setPasser(Player aPasser) {
		this.passer = aPasser == null ? Player.anonymous() : aPasser;
	}

	public Player getReceiver() {
		if (receiver == null) {
			receiver = Player.anonymous();
		}
		return receiver;
	}

	public void setReceiver(Player aReceiver) {
		this.receiver = aReceiver == null ? Player.anonymous() : aReceiver;
	}
	
	protected void ensureValid() {
		if (passer == null) {
			passer = Player.anonymous();
		}
		if (receiver == null || getAction() == Throwaway || getAction() == Stall || getAction() == MiscPenalty || getAction() == Callahan) {
			receiver = Player.anonymous();
		}		
		if (!OFFENSE_ACTIONS.contains(getAction())) {
			throw new InvalidEventException("Invalid action for offense event " + getAction());
		}
	}
	
	public Player getPlayerOne() {
		return getPasser();
	}

	public Player getPlayerTwo() {
		return getReceiver();
	}

	public int image() {
		switch (getAction()) {
		case Goal:
			return R.drawable.goal_green;
		case Callahan:
			return R.drawable.callahan_red;
		case Catch:
			return R.drawable.pass;
		case Throwaway:
			return R.drawable.throwaway;
		case Drop:
			return R.drawable.drop;
		case Stall:
			return R.drawable.stall;
		case MiscPenalty:
			return R.drawable.penalty;			
		default:
			return super.image();
		}
	}
	
	public int imageMonochrome() {
		switch (getAction()) {
		case Goal:
			return R.drawable.goal;
		case Callahan:
			return R.drawable.callahan;
		default:
			return image();
		}
	}
	
	public void useSharedPlayers() {
		passer = Player.replaceWithSharedPlayer(passer);
		receiver = Player.replaceWithSharedPlayer(receiver);
	}
	
	@Override
	public void readExternal(ObjectInput input) throws IOException, ClassNotFoundException {
		super.readExternal(input);
		passer = (Player)input.readObject();
		receiver = (Player)input.readObject();
	}

	@Override
	public void writeExternal(ObjectOutput output) throws IOException {
		super.writeExternal(output);
		output.writeObject(passer);
		output.writeObject(receiver);
	}
	
	public static OffenseEvent eventfromJsonObject(JSONObject jsonObject) throws JSONException {
		String actionAsString = jsonObject.getString(JSON_ACTION);
		Action action = Catch;
		if (actionAsString.equals("Catch")) {
			action = Catch;
		} else if (actionAsString.equals("Drop")) {
			action = Drop;			
		} else if (actionAsString.equals("Goal")) {
			action = Goal;
		} else if (actionAsString.equals("Throwaway")) {
			action = Throwaway;
		} else if (actionAsString.equals("Stall")) {
			action = Stall;
		} else if (actionAsString.equals("MiscPenalty")) {
			action = MiscPenalty;
		} else if (actionAsString.equals("Callahan")) {
			action = Callahan;
		} 
		Player passer = null;
		if (jsonObject.has(JSON_PASSER)) {
			passer = Team.getPlayerNamed(jsonObject.getString(JSON_PASSER));
		}
		Player receiver = null;
		if (jsonObject.has(JSON_RECEIVER)) {
			receiver = Team.getPlayerNamed(jsonObject.getString(JSON_RECEIVER));
		}
		OffenseEvent event = new OffenseEvent(action, passer, receiver);
		populateGeneralPropertiesFromJsonObject(event, jsonObject);
		return event;
	}
	
	public JSONObject toJsonObject() throws JSONException {
		ensureValid();
		JSONObject jsonObject = super.toJsonObject();
		String actionAsString = null;
		switch (getAction()) {
		case Catch:
			actionAsString = "Catch";
			break;
		case Drop:
			actionAsString = "Drop";
			break;
		case Goal:
			actionAsString = "Goal";
			break;
		case Throwaway:
			actionAsString = "Throwaway";
			break;		
		case Stall:
			actionAsString = "Stall";
			break;	
		case MiscPenalty:
			actionAsString = "MiscPenalty";
			break;				
		case Callahan:
			actionAsString = "Callahan";
			break;				
		default:
			actionAsString = "Catch";
			break;
		}
		jsonObject.put(JSON_ACTION, actionAsString);
		jsonObject.put(JSON_PASSER, passer.getName());
		jsonObject.put(JSON_RECEIVER, receiver.getName());
		
		return jsonObject;
	}
	

}
