package com.summithillsoftware.ultimate.ui.game.action;

import java.util.List;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.Space;
import android.widget.TextView;

import com.summithillsoftware.ultimate.R;
import com.summithillsoftware.ultimate.model.Action;
import com.summithillsoftware.ultimate.model.DefenseEvent;
import com.summithillsoftware.ultimate.model.Event;
import com.summithillsoftware.ultimate.model.Game;
import com.summithillsoftware.ultimate.model.OffenseEvent;
import com.summithillsoftware.ultimate.model.Player;
import com.summithillsoftware.ultimate.model.PointEvent;
import com.summithillsoftware.ultimate.ui.UltimateFragment;

public class GameActionFieldFragment extends UltimateFragment implements GameActionEventListener {
	private GameActionEventListener gameActionEventListener;
	
	// widgets
	private GameActionButton throwawayButton;
	private GameActionButton opponentThrowawayButton;
	private GameActionButton opponentGoalButton;
	private TextView pickInitialPlayerInstructionsTextView;
	private Space defenseRightFiller;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_game_action_field, null);
		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		addPlayerFragments();
		connectWidgets(view);
		super.onViewCreated(view, savedInstanceState);
	}
	
	@Override
	public void onStart() {
		super.onStart();
		registerWidgetListeners();
		populateView();
	}
	
	public void refresh() {
		populateView();
	}
	
	private void addPlayerFragments() {
		if (getAnonymousPlayerFragment() == null) {  // don't add if already there
			FragmentTransaction ft = getChildFragmentManager().beginTransaction();
			for (int i = 0; i <= 7; i++) {
				GameActionPlayerFragment playerFragment = new GameActionPlayerFragment();
				ft.add(R.id.playerFragments, playerFragment, fragmentTagForPlayer(i));
			}
			ft.commit(); 
		}
	}
	
	private void connectWidgets(View view) {
		throwawayButton = (GameActionButton)view.findViewById(R.id.throwawayButton);
		opponentThrowawayButton = (GameActionButton)view.findViewById(R.id.opponentThrowawayButton);
		opponentGoalButton = (GameActionButton)view.findViewById(R.id.opponentGoalButton);
		pickInitialPlayerInstructionsTextView = (TextView)view.findViewById(R.id.pickInitialPlayerInstructionsTextView);
		defenseRightFiller = (Space)view.findViewById(R.id.defenseRightFiller);
	}
	
	private void populateView() {
		List<Player> line = Game.current().currentLineSorted();
		for (int i = 0; i < 7; i++) {
			if (line.size() > i) {
				getPlayerFragment(i).setPlayer(line.get(i));
			} else {
				getPlayerFragment(i).setPlayer(null);
			}
		}
		getAnonymousPlayerFragment().setPlayer(Player.anonymous());
		refreshButtons();
	}
	
	private void refreshButtons() {
		boolean isOffense = Game.current().arePlayingOffense();
		boolean isFirstEventOfPoint = !Game.current().isPointInProgess();		
		if (isOffense) {
			opponentGoalButton.setVisibility(View.GONE);
			throwawayButton.setVisibility(View.GONE);  // will be made visible later if a passer is selected
			opponentThrowawayButton.setVisibility(View.GONE);  
			pickInitialPlayerInstructionsTextView.setVisibility(View.VISIBLE);  // will be made invisible later if a passer is selected
			defenseRightFiller.setVisibility(View.GONE);
		} else {
			opponentGoalButton.setVisibility(isFirstEventOfPoint ? View.GONE : View.VISIBLE);			
			throwawayButton.setVisibility(View.GONE);
			opponentThrowawayButton.setVisibility(isFirstEventOfPoint ? View.GONE : View.VISIBLE);  
			pickInitialPlayerInstructionsTextView.setVisibility(View.GONE);
			defenseRightFiller.setVisibility(View.VISIBLE);
		}
		for (int i = 0; i <= 7; i++) {
			GameActionPlayerFragment playerFragment = getPlayerFragment(i);
			playerFragment.setOffense(isOffense);
			playerFragment.setFirstPointOfEvent(isFirstEventOfPoint);
		}
		
		Player selectedPlayer = null;
		if (isOffense && !isFirstEventOfPoint) {
			Event lastEvent = Game.current().getLastEvent();
			if (lastEvent != null && lastEvent.getAction() == Action.Catch) {
				selectedPlayer = ((OffenseEvent)lastEvent).getReceiver();
			}
		} 
		updateSelectedPasser(selectedPlayer);
	}
	
 	private void updateSelectedPasser(Player selectedPlayer) {
 		for (int i = 0; i <= 7; i++) {
 			GameActionPlayerFragment playerFragment = getPlayerFragment(i);
			playerFragment.setSelected(playerFragment.isFragmentForPlayer(selectedPlayer));
 			playerFragment.setInitialPlayerBeenSelected(selectedPlayer != null);
 		}
 		if (selectedPlayer != null) {
 			throwawayButton.setVisibility(View.VISIBLE);
 			pickInitialPlayerInstructionsTextView.setVisibility(View.GONE);
 		}
 	}
        
	private GameActionPlayerFragment getAnonymousPlayerFragment() {
		return getPlayerFragment(7);
	}
	
	private void registerWidgetListeners() {
		throwawayButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				handleThrowawayPressed(false);
			}
		});
		throwawayButton.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				handleThrowawayPressed(true);
				return true;
			}
		});
		opponentThrowawayButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				handleThrowawayPressed(false);
			}
		});
		opponentThrowawayButton.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				handleThrowawayPressed(true);
				return true;
			}
		});
		opponentGoalButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				handleOpponentGoalPressed(false);
			}
		});
		opponentGoalButton.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				handleOpponentGoalPressed(true);
				return true;
			}
		});
		for (int i = 0; i <= 7; i++) {
			GameActionPlayerFragment playerFragment = getPlayerFragment(i);
			playerFragment.setGameActionEventListener(this);
		}
	}
	
	private GameActionPlayerFragment getSelectedPlayerFragment() {
		for (int i = 0; i <= 7; i++) {
			GameActionPlayerFragment fragment = getPlayerFragment(i);
			if (fragment.isSelected()) {
				return fragment;
			}
		}
		return null;
	}
	
	private void handleThrowawayPressed(boolean isPotential) {
		if (Game.current().arePlayingOffense()) {
			GameActionPlayerFragment selectedFragment = getSelectedPlayerFragment();
			if (selectedFragment != null && selectedFragment.getPlayer() != null) {
				notifyNewEvent(new OffenseEvent(Action.Throwaway, selectedFragment.getPlayer()), isPotential);
			}
		} else {
			notifyNewEvent(new DefenseEvent(Action.Throwaway, Player.anonymous()), isPotential);
		}
	}

	private void handleOpponentGoalPressed(boolean isPotential) {
		notifyNewEvent(new DefenseEvent(Action.Goal, Player.anonymous()), isPotential);
	}

	private GameActionPlayerFragment getPlayerFragment(int i) {
		return (GameActionPlayerFragment)getChildFragmentManager().findFragmentByTag(fragmentTagForPlayer(i));
	}
	
	private String fragmentTagForPlayer(int i) {
		return "PlayerFrag" + i;
	}

	public GameActionEventListener getGameActionEventListener() {
		return gameActionEventListener;
	}

	public void setGameActionEventListener(
			GameActionEventListener gameActionEventListener) {
		this.gameActionEventListener = gameActionEventListener;
	}
	
	private void notifyNewEvent(Event event, boolean isPotential) {
		if (gameActionEventListener != null) {
			if (isPotential) {
				gameActionEventListener.potentialNewEvent(event);
			} else {
				gameActionEventListener.newEvent(event);
			}
		}
	}
	
	private void notifyPotentialNewEvent(Event event) {
		if (gameActionEventListener != null) {
			gameActionEventListener.potentialNewEvent(event);
		}
	}
	
	@Override
	public void newEvent(Event event) {
		populatePlayerOne(event);
		notifyNewEvent(event, false);
	}

	@Override
	public void onEventEditRequest(PointEvent event) {
		// no-op...field fragment doesn't care
	}
	
	@Override
	public void potentialNewEvent(Event event) {
		populatePlayerOne(event);
		notifyPotentialNewEvent(event);
	}
	
	@Override
	public void removeLastEvent() {
		// no-op...don't remove events in this fragment
	}

	@Override
	public void initialOffensePlayerSelected(Player selectedPlayer) {
		if (Game.current().arePlayingOffense()) {
			updateSelectedPasser(selectedPlayer);
		}
	}

	@Override
	public void timeoutInfoRequested() {
		// no-op...don't handle timeout info in this fragment
	}
	
	@Override
	public void cessationRequested() {
		// no-op...don't handle cessations in this fragment
	}

	private void populatePlayerOne(Event event) {
		GameActionPlayerFragment selectedFragment = getSelectedPlayerFragment();
		if (event.isOffense()) {
			if (selectedFragment != null) {
				((OffenseEvent)event).setPasser(selectedFragment.getPlayer());
			}
		} else {
			if (selectedFragment != null && event.isD()) {
				((DefenseEvent)event).setDefender(selectedFragment.getPlayer());
			}
		}
	}

}
