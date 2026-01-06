package com.summithillsoftware.ultimate.ui.player;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.summithillsoftware.ultimate.R;
import com.summithillsoftware.ultimate.model.Player;
import com.summithillsoftware.ultimate.model.Team;
import com.summithillsoftware.ultimate.ui.UltimateActivity;

public class PlayersActivity extends UltimateActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_players);
		setupActionBar();  // Show the Up button in the action bar.
		registerPlayerSelectedListener();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.players, menu);
		return true;
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		setTitle(getString(R.string.title_activity_players) + " - " + Team.current().getName());
		if (Team.current().getPlayers().size() == 0) {
			Toast toast = Toast.makeText(getApplicationContext(), getString(R.string.toast_players_no_players_yet), Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		getPlayersListViewAdapter().resetPlayers();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			return navigateUp();
		case R.id.action_add:
			goToPlayerActivity(null);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private ListView getPlayersListView() {
		return (ListView)findViewById(R.id.playersFragment).findViewById(R.id.listview_players);
	}
	
	private PlayersListAdapter getPlayersListViewAdapter() {
		return (PlayersListAdapter)getPlayersListView().getAdapter();
	}
	
	private void registerPlayerSelectedListener() {
		getPlayersListView().setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Player player = (Player)getPlayersListViewAdapter().getItem(position);
				goToPlayerActivity(player);
			}

		});
	}
	
	private void goToPlayerActivity(Player player) {
		Intent intent = createPlayerIntent();
		if (player == null) {
			intent.putExtra(PlayerActivity.NEW_PLAYER, true);
		} else {
			intent.putExtra(PlayerActivity.PLAYER_NAME, player.getName());
		}
		startActivity(intent);
	}
	
	protected Intent createPlayerIntent() {
		return new Intent(this, PlayerActivity.class);
	}
	
}
