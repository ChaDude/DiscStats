package com.summithillsoftware.ultimate.ui.player;

import android.content.Intent;


public class PlayersActivity_GameAction extends PlayersActivity {
	
	@Override
	protected Intent createPlayerIntent() {
		return new Intent(this, PlayerActivity_GameAction.class);
	}
	
}
