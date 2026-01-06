package com.summithillsoftware.ultimate.ui.game.action;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

import com.summithillsoftware.ultimate.R;
import com.summithillsoftware.ultimate.UltimateApplication;
import com.summithillsoftware.ultimate.model.Player;
import com.summithillsoftware.ultimate.model.Team;

public class GameActionInitiatorPlayerButton extends Button {
	private Player player;

	public GameActionInitiatorPlayerButton(Context context) {
		super(context);
	} 
	public GameActionInitiatorPlayerButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	public GameActionInitiatorPlayerButton(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	public void setSelected(boolean selected) {
		super.setSelected(selected);
		updateViewForSelectedStatus();
	}

	public Player getPlayer() {
		return player;
	}
	
	public void setPlayer(Player player) {
		this.player = player;
		if (player != null) {
			String description = player.isAnonymous() ? 
					UltimateApplication.current().getString(R.string.button_action_unknown_player) : 
						(Team.current().isDisplayingPlayerNumber() ? player.getPlayerNumberDescription() : player.getName());
			this.setText(description);
			setTag(player.getName());
		}
	}
	
	private void updateViewForSelectedStatus() {
		setTextColor(getResources().getColor(isSelected() ? R.color.DarkGray : R.color.White));
	}
	

}
