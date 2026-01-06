package com.summithillsoftware.ultimate.model;

import static com.summithillsoftware.ultimate.model.Action.Callahan;
import static com.summithillsoftware.ultimate.model.Action.De;
import static com.summithillsoftware.ultimate.model.Action.Goal;
import static com.summithillsoftware.ultimate.model.Action.Pull;
import static com.summithillsoftware.ultimate.model.Action.PullOb;
import static com.summithillsoftware.ultimate.model.Action.Throwaway;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.summithillsoftware.ultimate.R;
import com.summithillsoftware.ultimate.util.StringUtil;

public class DefenseEvent extends Event {
	private static final long serialVersionUID = 1l;
	private static final String JSON_DEFENDER = "defender";
	private static final String JSON_HANGTIME = "hangtime";
	private static final String HANGTIME_DETAIL_PROPERTY_NAME = JSON_HANGTIME;
	
	public static final EnumSet<Action> DEFENSE_ACTIONS = EnumSet.of(
			Pull,
			PullOb,
			Goal,
			Throwaway,
			De,
			Callahan);
	
	private Player defender;
	
	public DefenseEvent() {
	}
	
	public DefenseEvent(Action action, Player defender) {
		super(action);
		this.defender = defender;
	}
	
	public DefenseEvent(DefenseEvent event) {
		super(event);
		defender = event.defender;
	}
	
	public boolean isOurGoal() {
		return getAction() == Callahan;
	}
	
	public boolean isTheirGoal() {
		return getAction() == Goal;
	}
	
	public boolean isGoal() {
		return isOurGoal() || isTheirGoal();
	}
	
	public boolean isCallahan() {
		return getAction() == Callahan;
	}
	
	public boolean 	isD() {
		return getAction() == De;
	}
	
	public boolean isPull() {
		return getAction() == Pull || getAction() == PullOb;
	}
	
	public boolean isPullIb() {
		return getAction() == Pull;
	}
	
	public boolean isPullOb() {
		return getAction() == PullOb;
	}
	
	public boolean isFinalEventOfPoint() {
		return isGoal();
	}
	
	public boolean isDefense() {
		return true;
	}
	
	public boolean isPlayEvent() {
		return true;
	}
	
	@Override
	public boolean isTurnover() {
	    return getAction() == De || getAction() == Throwaway || getAction() == Callahan;
	}

	@Override
	public boolean isNextEventOffense() {
		return !isCallahan() && (isTurnover() || isGoal());
	}

	@Override
	public List<Player> getPlayers() {
		List<Player> players = new ArrayList<Player>();
		if (defender != null) {
			players.add(defender);
		}
	    return players;
	}
	
	public boolean isAnonymous() {
		return (defender == null || defender.isAnonymous());
	}

	@Override
	protected String getDescriptionForTeamAndOpponent(String teamName,String opponentName) {
		String ourTeam = StringUtil.getString(R.string.event_description_our_team);
	    switch(getAction()) {
	        case Pull: {
	        	String hangtimeString = getFormattedPullHangtimeSeconds();
	        	hangtimeString = hangtimeString == null ? "" : 
	        		" (" + getString(R.string.event_description_pull_seconds_short, hangtimeString) + ")";
	            if (isAnonymous()) {
	            	// {team} pull ({seconds} sec)
	            	return getString(R.string.event_description_pull,(teamName == null ? ourTeam : teamName), hangtimeString);
	            } else {
	            	// Pull from {player} ({seconds} sec)	
	            	return getString(R.string.event_description_pull_from,getDefender().getName(), hangtimeString);	            	
	            }
	        }
	        case PullOb: {
	            if (isAnonymous()) {
	            	// {team} OB pull
	            	return getString(R.string.event_description_pull_ob,(teamName == null ? ourTeam : teamName));
	            } else {
	            	// OB Pull from {player}
	            	return getString(R.string.event_description_pull_ob_from,getDefender().getName());	
	            }
	        }
	        case Goal: {
	        	// Opponent goal || {opponent} Goal
	        	return opponentName == null ? getString(R.string.event_description_opponent_goal) : getString(R.string.event_description_opponent_goal_by, opponentName);
	        }
	        case Throwaway:{
	        	// Opponent throwaway || {opponent} throwaway	        	
	        	return opponentName == null ? getString(R.string.event_description_opponent_throwaway) : getString(R.string.event_description_opponent_throwaway_by,opponentName);
	        }
	        case De: {
	        	// {team|our} D || D by {player} 	        	
	        	return defender == null ? getString(R.string.event_description_d, (teamName == null ? ourTeam : teamName)) : getString(R.string.event_description_d_by,getDefender().getName());
	        }
	        case Callahan: {
	        	// {team|our} Callahan || Callahan by {player} 	        	
	        	return defender == null ? getString(R.string.event_description_callahan, (teamName == null ? ourTeam : teamName)) : getString(R.string.event_description_callahan_by,getDefender().getName());	        	
	        }
	        default:
	            return "";
	    }
	}

	public Player getDefender() {
		if (defender == null) {
			defender = Player.anonymous();
		}
		return defender;
	}

	public void setDefender(Player defender) {
		this.defender = defender == null ? Player.anonymous() : defender;
	}
	
	protected void ensureValid() {
		if (defender == null || getAction() == Throwaway || getAction() == Goal) {
			defender = Player.anonymous();
		}

		if (!DEFENSE_ACTIONS.contains(getAction())) {
			throw new InvalidEventException("Invalid action for defense event " + getAction());
		}
	}
	
	public Player getPlayerOne() {
		return getDefender();
	}
	
	public Player getPlayerTwo() {
		return null;
	}

	public void setPullHangtimeMilliseconds(int hangtimeMs) {
		setDetailIntValue(HANGTIME_DETAIL_PROPERTY_NAME, hangtimeMs);
	}
	
	public int getPullHangtimeMilliseconds() {
		return getDetailIntValue(HANGTIME_DETAIL_PROPERTY_NAME, 0);
	}
	
	public String getFormattedPullHangtimeSeconds() {
    	int hangTime = getPullHangtimeMilliseconds();
    	if (hangTime > 0) {
    		DecimalFormat formatter = new DecimalFormat("##0.0");
    		float hangtimeSeconds = ((float)hangTime)/1000f;
    		return formatter.format(hangtimeSeconds);
    	} else {
    		return null;
    	}
	}
	
	public int image() {
		switch (getAction()) {
		case Goal:
			return R.drawable.goal_red;
		case Callahan:
			return R.drawable.callahan_green;
		case Pull:
			return R.drawable.pull;
		case PullOb:
			return R.drawable.pull_ob;
		case De:
			return R.drawable.de;
		case Throwaway:
			return R.drawable.throwaway;
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
		defender = Player.replaceWithSharedPlayer(defender);
	}
	
	@Override
	public void readExternal(ObjectInput input) throws IOException, ClassNotFoundException {
		super.readExternal(input);
		defender = (Player)input.readObject();
	}

	@Override
	public void writeExternal(ObjectOutput output) throws IOException {
		super.writeExternal(output);
		output.writeObject(defender);
	}
	
	public static DefenseEvent eventfromJsonObject(JSONObject jsonObject) throws JSONException {
		String actionAsString = jsonObject.getString(JSON_ACTION);
		Action action = Pull;
		if (actionAsString.equals("Pull")) {
			action = Pull;
		} else if (actionAsString.equals("PullOb")) {
				action = PullOb;			
		} else if (actionAsString.equals("D")) {
			action = De;			
		} else if (actionAsString.equals("Goal")) {
			action = Goal;
		} else if (actionAsString.equals("Throwaway")) {
			action = Throwaway;
		} else if (actionAsString.equals("Callahan")) {
			action = Callahan;
		} 
		Player defender = null;
		if (jsonObject.has(JSON_DEFENDER)) {
			defender = Team.getPlayerNamed(jsonObject.getString(JSON_DEFENDER));
		}
		DefenseEvent event = new DefenseEvent(action, defender);
		if (event.isPull()) {
			Integer hangTimeMs = retrieveIntEventDetailFromJsonObject(jsonObject, JSON_HANGTIME);
			if (hangTimeMs != null) {
				event.setPullHangtimeMilliseconds(hangTimeMs.intValue());
			}
		}
		populateGeneralPropertiesFromJsonObject(event, jsonObject);
		return event;
	}
	
	public JSONObject toJsonObject() throws JSONException {
		ensureValid();
		JSONObject jsonObject = super.toJsonObject();
		String actionAsString = null;
		switch (getAction()) {
		case Pull:
			actionAsString = "Pull";
			break;
		case PullOb:
			actionAsString = "PullOb";
			break;
		case De:
			actionAsString = "D";
			break;
		case Goal:
			actionAsString = "Goal";
			break;
		case Throwaway:
			actionAsString = "Throwaway";
			break;		
		case Callahan:
			actionAsString = "Callahan";
			break;				
		default:
			actionAsString = "Pull";
			break;
		}
		jsonObject.put(JSON_ACTION, actionAsString);
		jsonObject.put(JSON_DEFENDER, defender.getName());
		if (isPull() && getPullHangtimeMilliseconds() != 0) {
			addEventDetailToJsonObject(jsonObject, JSON_HANGTIME, Integer.valueOf(getPullHangtimeMilliseconds()));
		}
		return jsonObject;
	}

}
