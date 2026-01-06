package com.summithillsoftware.ultimate.ui.game.line;

import java.util.List;
import java.util.Set;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

import com.summithillsoftware.ultimate.R;
import com.summithillsoftware.ultimate.UltimateApplication;
import com.summithillsoftware.ultimate.model.Player;
import com.summithillsoftware.ultimate.model.Team;

public class PlayerLineButton extends Button {
	private Player player;
	private boolean isButtonOnFieldView;
	private boolean playerLineStatusChanged;
	private int playingTimeFactor; // 0-4

	public PlayerLineButton(Context context) {
		super(context);
	} 
	public PlayerLineButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	public PlayerLineButton(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public Player getPlayer() {
		return player;
	}
	
	@Override
	public String toString() {
		return "PlayerLineButton [player=" + player.getName() + ", isOnFieldView="
				+ isButtonOnFieldView + ", tag=" + getTag() +"]";
	}

	public void setPlayer(Player player, int playingTimeFactor) {
		this.player = player;
		this.playingTimeFactor = playingTimeFactor;
		String description = player.isAnonymous() ? 
				UltimateApplication.current().getString(R.string.button_line_field_slot_open) : 
					(Team.current().isDisplayingPlayerNumber() ? player.getPlayerNumberDescription() : player.getName());
		this.setText(description);
	}

	public boolean isButtonOnFieldView() {
		return isButtonOnFieldView;
	}

	public void setButtonOnFieldView(boolean isOnField, List<Player> playersOnField, Set<Player>originalLine) {
		this.isButtonOnFieldView = isOnField;
		updateView(playersOnField, originalLine);
	}
	
	public void updateView(List<Player> playersOnField, Set<Player>originalLine) {
		boolean isPlayerOnField = !player.isAnonymous() && playersOnField.contains(player);
		if (player.isAnonymous() || (!isButtonOnFieldView() && isPlayerOnField)) {
			setEnabled(false);
		} else {
			setEnabled(true);
			updateViewForChangeStatus(isPlayerOnField, originalLine);
			// TODO...show gender and number of points played
		}
		updateViewForEnabledStatus();
	}
	private void updateViewForEnabledStatus() {
		if (isEnabled()) {
			setTextColor(getResources().getColor(R.color.White));
			setShadowLayer(0.6f, 1, 1, 0xFF000000);
		} else {
			setShadowLayer(0.6f, 1, 1, 0x00000000); // hide the shadow by making it transparent alpha
			if (isButtonOnFieldView) {
				setTextColor(getResources().getColor(R.color.DarkOrange));
			} else {
				setTextColor(getResources().getColor(R.color.ultimate_action_bar));
			}
		}
	}
	
	private void updateViewForChangeStatus(boolean isPlayerOnField, Set<Player>originalLine) {
		playerLineStatusChanged = (isPlayerOnField != originalLine.contains(player));
		refreshDrawableState();
	}

	@Override
    protected int[] onCreateDrawableState(int extraSpace) {
        // If the player has changed from/top line/bench or if they have a points played factor then we merge our 
		//  custom style attributes into the existing drawable state so they effect the selectors.
		int[] additionalStyleAttributes = customStyleAttributes();
		if (additionalStyleAttributes.length > 0) {
			final int[] drawableState = super.onCreateDrawableState(extraSpace + additionalStyleAttributes.length);
			mergeDrawableStates(drawableState, additionalStyleAttributes);
			return drawableState;
		} else {
			return super.onCreateDrawableState(extraSpace);
		}
    }
	
	private int[] customStyleAttributes() {
		boolean playerChanged = playerLineStatusChanged && isEnabled();
		switch (playingTimeFactor) {
		case 4:
			return playerChanged ? new int[]{ R.attr.state_changed,  R.attr.playing_time_factor4}: new int[]{ R.attr.playing_time_factor4};
		case 3:
			return playerChanged ? new int[]{ R.attr.state_changed,  R.attr.playing_time_factor3}: new int[]{ R.attr.playing_time_factor3};
		case 2:
			return playerChanged ? new int[]{ R.attr.state_changed,  R.attr.playing_time_factor2}: new int[]{ R.attr.playing_time_factor2};	
		case 1:
			return playerChanged ? new int[]{ R.attr.state_changed,  R.attr.playing_time_factor1}: new int[]{ R.attr.playing_time_factor1};		
		default:
			return playerChanged ? new int[]{ R.attr.state_changed,  R.attr.playing_time_factor1}: new int[]{ R.attr.playing_time_factor0};
		}
	}


}
