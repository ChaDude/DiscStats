package com.summithillsoftware.ultimate.ui.game;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.summithillsoftware.ultimate.R;
import com.summithillsoftware.ultimate.model.Game;
import com.summithillsoftware.ultimate.model.GameDescription;
import com.summithillsoftware.ultimate.model.Preferences;
import com.summithillsoftware.ultimate.model.Team;
import com.summithillsoftware.ultimate.ui.Refreshable;
import com.summithillsoftware.ultimate.ui.UltimateActivity;
import com.summithillsoftware.ultimate.ui.cloud.CloudGameDownloadDialog;
import com.summithillsoftware.ultimate.util.ObjectStoreError;
import com.summithillsoftware.ultimate.workflow.GameDownloadWorkflow;

public class GamesActivity extends UltimateActivity implements Refreshable {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_games);
		setupActionBar();  // Show the Up button in the action bar.
		registerTeamSelectedListener();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.games, menu);
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
	    super.onPrepareOptionsMenu(menu);
	    menu.findItem(R.id.action_download).setVisible(Team.current().hasCloudId());
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			return navigateUp();
		case R.id.action_add:
			goToGameActivity(true);
			return true;
		}
		if (item.getItemId() == R.id.action_download) {
			showGameDownloadDialog();
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		setTitle(getString(R.string.action_games) + " - " + Team.current().getName());
		if (Game.numberOfGamesForTeam(Team.current().getTeamId()) == 0) {
			Toast toast = Toast.makeText(getApplicationContext(), getString(R.string.toast_games_no_games_yet), Toast.LENGTH_LONG);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		populateView();
	}
	
	private void populateView() {
		getGamesListViewAdapter().resetGames();
	}
	
	private ListView getGamesListView() {
		return (ListView)findViewById(R.id.gamesFragment).findViewById(R.id.listview_games);
	}
	
	private GamesListAdapter getGamesListViewAdapter() {
		return (GamesListAdapter)getGamesListView().getAdapter();
	}
	
	private void registerTeamSelectedListener() {
		getGamesListView().setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				final GameDescription selectedGame = (GameDescription)getGamesListViewAdapter().getItem(position);
				try {
					Game.setCurrentGameId(selectedGame.getGameId());
					goToGameActivity(false);
				} catch (ObjectStoreError err) {
					displayConfirmDialog(
							getString(R.string.alert_file_game_corrupt_title), 
							getString(R.string.alert_file_game_corrupt_message), 
							getString(R.string.alert_file_corrupt_delete),
							getString(R.string.alert_file_corrupt_try_again), 
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface paramDialogInterface, int paramInt) {
									Game.delete(selectedGame.getGameId());
									refresh();
								}
					 		},
					 		new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface paramDialogInterface, int paramInt) {
									// no-op...alert simply closes
								}
					 		});
				}
			}

		});
	}
	
	private void goToGameActivity(boolean isNew) {
		Intent intent = new Intent(GamesActivity.this, GameActivity.class);
		if (isNew) {
			Game newGame = Game.createGame();
			Game.setCurrentGame(newGame);
			newGame.setTournamentName(Preferences.current().getTournamentName());
			newGame.setGamePoint(Preferences.current().getGamePoint());
		}
		startActivity(intent);
	}

	private void showGameDownloadDialog() {
	    FragmentManager fragmentManager = getSupportFragmentManager();
	    CloudGameDownloadDialog downloadDialog = new CloudGameDownloadDialog();
		GameDownloadWorkflow workflow = new GameDownloadWorkflow();
	    downloadDialog.setWorkflow(workflow);
	    downloadDialog.show(fragmentManager, "dialog");
	}
	
	@Override
	public void refresh() {
		populateView();
	}


}
