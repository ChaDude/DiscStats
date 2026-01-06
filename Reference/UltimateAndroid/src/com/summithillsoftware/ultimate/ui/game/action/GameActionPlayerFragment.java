package com.summithillsoftware.ultimate.ui.game.action;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.summithillsoftware.ultimate.R;
import com.summithillsoftware.ultimate.model.Action;
import com.summithillsoftware.ultimate.model.DefenseEvent;
import com.summithillsoftware.ultimate.model.Event;
import com.summithillsoftware.ultimate.model.OffenseEvent;
import com.summithillsoftware.ultimate.model.Player;
import com.summithillsoftware.ultimate.ui.UltimateFragment;

public class GameActionPlayerFragment extends UltimateFragment {
	private Player player;
	private boolean isOffense;
	private boolean isFirstPointOfEvent;
	private boolean hasInitialPlayerBeenSelected;
	private boolean isSelected;
	private GameActionEventListener gameActionEventListener;
	
	// widgets
	private GameActionInitiatorPlayerButton initiatorPlayerButton;
	private ImageView passingDirectionArrow;
	private GameActionButton catchButton;
	private GameActionButton dropButton;
	private GameActionButton goalButton;
	private GameActionButton pullButton;
	private GameActionButton deButton;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_game_action_player, null);
		connectWidgets(view);
		registerWidgetListeners();
		populateView();
		return view;
	}
	
	private void connectWidgets(View view) {
		initiatorPlayerButton = (GameActionInitiatorPlayerButton)view.findViewById(R.id.initiatorPlayerButton);
		passingDirectionArrow = (ImageView)view.findViewById(R.id.passingDirectionArrow);
		catchButton = (GameActionButton)view.findViewById(R.id.catchButton);
		dropButton = (GameActionButton)view.findViewById(R.id.dropButton);
		goalButton = (GameActionButton)view.findViewById(R.id.goalButton);
		pullButton = (GameActionButton)view.findViewById(R.id.pullButton);
		deButton = (GameActionButton)view.findViewById(R.id.deButton);
	}
	
	private void registerWidgetListeners() {
		registerActionButtonListener(catchButton, Action.Catch);
		registerActionButtonListener(dropButton, Action.Drop);
		registerActionButtonListener(goalButton, Action.Goal);
		registerActionButtonListener(pullButton, Action.Pull);
		registerActionButtonListener(deButton, Action.De);
		
		initiatorPlayerButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				handleOffensePasserSelected();
			}
		});
		
	}
	
	private void populateView() {
		if (getView() != null) {
			configureButtonVisibility();
			if (player != null) {
				initiatorPlayerButton.setPlayer(player);
				initiatorPlayerButton.setSelected(isSelected);
			} 
		}
	}
	
	
	
	private void configureButtonVisibility() {
		initiatorPlayerButton.setVisibility((player != null) ? View.VISIBLE : View.INVISIBLE);
		passingDirectionArrow.setVisibility((player != null) && isOffense && hasInitialPlayerBeenSelected && !isSelected() ? View.VISIBLE : View.GONE);
		catchButton.setVisibility((player != null) && isOffense && hasInitialPlayerBeenSelected && !isSelected() ? View.VISIBLE : View.GONE);
		dropButton.setVisibility((player != null) && isOffense && hasInitialPlayerBeenSelected && !isSelected() ? View.VISIBLE : View.GONE);
		goalButton.setVisibility((player != null) && isOffense && hasInitialPlayerBeenSelected && !isSelected() ? View.VISIBLE : View.GONE);
		pullButton.setVisibility((player != null) && !isOffense && isFirstPointOfEvent ? View.VISIBLE : View.GONE);
		deButton.setVisibility((player != null) && !isOffense && !isFirstPointOfEvent ? View.VISIBLE : View.GONE);
	}
	
	private void registerActionButtonListener(Button button, final Action action) {
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				handleActionClicked(action, false);
			}
		});
		button.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				handleActionClicked(action, true);
				return true;
			}
		});
	}
	
	private void handleActionClicked(Action action, boolean isLongPress) {
		switch (action) {
		case Catch:
			notifyNewEvent(new OffenseEvent(action, Player.anonymous(), player), isLongPress);
			break;
		case Drop:
			notifyNewEvent(new OffenseEvent(action, Player.anonymous(), player), isLongPress);
			break;
		case Goal:
			if (isOffense) {
				notifyNewEvent(new OffenseEvent(action, Player.anonymous(), player), isLongPress);
			} else {
				notifyNewEvent(new DefenseEvent(action, Player.anonymous()), isLongPress);
			}
			break;		
		case Throwaway:
			if (isOffense) {
				notifyNewEvent(new OffenseEvent(action, Player.anonymous()), isLongPress);
			} else {
				notifyNewEvent(new DefenseEvent(action, Player.anonymous()), isLongPress);
			}
			break;		
		case Pull:
			notifyNewEvent(new DefenseEvent(action, player), isLongPress);
			break;					
		case De:
			notifyNewEvent(new DefenseEvent(action, player), isLongPress);
			break;				
		default:
			break;
		}
	}
	
	private void handleOffensePasserSelected() {
		notifyOffensePasserSelected();
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
	
	private void notifyOffensePasserSelected() {
		if (gameActionEventListener != null) {
			gameActionEventListener.initialOffensePlayerSelected(this.player);
		}
	}
	public boolean isFragmentForPlayer(Player aPlayer) {
		return player != null && player.equals(aPlayer);
	}
	
	
	public Player getPlayer() {
		return player;
	}
	public void setPlayer(Player player) {
		this.player = player;
		populateView();
	}
	public boolean isOffense() {
		return isOffense;
	}
	public void setOffense(boolean isOffense) {
		this.isOffense = isOffense;
		populateView();
	}
	public void setInitialPlayerBeenSelected(boolean initalPlayerSelected) {
		this.hasInitialPlayerBeenSelected = initalPlayerSelected;
		populateView();
	}
	public boolean isSelected() {
		return isSelected;
	}
	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
		initiatorPlayerButton.setSelected(this.isSelected);
		populateView();
	}
	public GameActionEventListener getGameActionEventListener() {
		return gameActionEventListener;
	}
	public void setGameActionEventListener(
			GameActionEventListener gameActionEventListener) {
		this.gameActionEventListener = gameActionEventListener;
	}

	public void setFirstPointOfEvent(boolean isFirstPointOfEvent) {
		this.isFirstPointOfEvent = isFirstPointOfEvent;
	}

}
