package com.summithillsoftware.ultimate.ui.player;

import android.content.Intent;


public class PlayersActivity_Team extends PlayersActivity {
	
	@Override
	protected Intent createPlayerIntent() {
		return new Intent(this, PlayerActivity_Team.class);
	}
	
}
