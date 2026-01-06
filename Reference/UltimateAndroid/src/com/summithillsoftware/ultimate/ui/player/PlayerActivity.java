package com.summithillsoftware.ultimate.ui.player;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.summithillsoftware.ultimate.R;
import com.summithillsoftware.ultimate.model.Player;
import com.summithillsoftware.ultimate.model.PlayerPosition;
import com.summithillsoftware.ultimate.model.Team;
import com.summithillsoftware.ultimate.ui.UltimateActivity;

public class PlayerActivity extends UltimateActivity {
	public static final String NEW_PLAYER = "NewPlayer";
	public static final String PLAYER_NAME = "PlayerName";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (isNewPlayer()) {
			this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED);
		}
		setContentView(R.layout.activity_player);
		setupActionBar();  // Show the Up button in the action bar.
		populateView();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.player, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			return navigateUp();
		case R.id.action_delete:
			deleteClicked();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void saveClicked(View v) {
		if (isPlayerValid()) {
			populateAndSaveTeam();
			finish();
		} 
	}
	
	public void saveAndAddClicked(View v) {
		if (isPlayerValid()) {
			populateAndSaveTeam();
			populateView();
		} 
	}

	public void cancelClicked(View v) {
		finish();
	}
	
	private void deleteClicked() {
		confirmAndDelete();
	}
	
	private void confirmAndDelete() {
		displayConfirmDialog(getString(R.string.alert_player_confirm_delete_title), getString(R.string.alert_player_confirm_delete_message, getPlayer().getName()), getString(android.R.string.yes), getString(android.R.string.no), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface paramDialogInterface, int paramInt) {
				Team.current().removePlayer(getPlayer());
				Team.current().save();
				finish();
			}
 		});
	}
	
	private void populateView() {
		getAddAnotherButton().setVisibility(isNewPlayer() ? View.VISIBLE : View.GONE);
		Player player = getPlayer();
		getNameTextView().setText(player.getName());
		getNumberTextView().setText(player.getNumber());
		switch (player.getPosition()) {
		case Cutter:
			getPositionRadioGroup().check(R.id.radio_player_position_cutter);
			break;
		case Handler:
			getPositionRadioGroup().check(R.id.radio_player_position_handler);
			break;
		default:
			getPositionRadioGroup().check(R.id.radio_player_position_any);
			break;
		}
		getGenderRadioGroup().check(player.isMale() ? R.id.radio_player_gender_male : R.id.radio_player_gender_female);
		getAbsentCheckBox().setChecked(player.isAbsent());
		getNameTextView().requestFocus();
	}
	
	private void populateAndSaveTeam() {
		Player player = getPlayer();
		player.setName(getPlayerName());
		player.setNumber(getPlayerNumber());
		switch (getPositionRadioGroup().getCheckedRadioButtonId()) {
		case R.id.radio_player_position_cutter:
			player.setPosition(PlayerPosition.Cutter);
			break;
		case R.id.radio_player_position_handler:
			player.setPosition(PlayerPosition.Handler);
			break;
		default:
			player.setPosition(PlayerPosition.Any);
			break;
		}
		player.setMale(getGenderRadioGroup().getCheckedRadioButtonId() == R.id.radio_player_gender_male);
		player.setAbsent(getAbsentCheckBox().isChecked());
		if (isNewPlayer()) {
			Team.current().getPlayers().add(player);
		}
		Team.current().save();
	}

	private String getPlayerName() {
		return getNameTextView().getText().toString().trim();
	}
	
	private TextView getNameTextView() {
		return (TextView)findViewById(R.id.playerFragment).findViewById(R.id.text_player_name);
	}
	
	private String getPlayerNumber() {
		return getNumberTextView().getText().toString().trim();
	}
	
	private TextView getNumberTextView() {
		return (TextView)findViewById(R.id.playerFragment).findViewById(R.id.text_player_number);
	}
	
	private RadioGroup getPositionRadioGroup() {
		return (RadioGroup)findViewById(R.id.playerFragment).findViewById(R.id.radiogroup_player_position);
	}
	
	private RadioGroup getGenderRadioGroup() {
		return (RadioGroup)findViewById(R.id.playerFragment).findViewById(R.id.radiogroup_player_gender);
	}
	
	private CheckBox getAbsentCheckBox() {
		return (CheckBox)findViewById(R.id.playerFragment).findViewById(R.id.checkbox_player_absent);
	}

	private Button getAddAnotherButton() {
		return (Button)findViewById(R.id.playerFragment).findViewById(R.id.button_save_and_add);
	}
	
	private boolean isNewPlayer() {
		return getIntent().getBooleanExtra(NEW_PLAYER, false);
	}
	
	private boolean isPlayerValid() {
		if (getPlayerName().isEmpty()) {
			displayErrorMessage(getString(R.string.alert_player_name_required_title), getString(R.string.alert_player_name_required_message));
			return false;
		} else if (Team.current().isDuplicatePlayerName(getPlayerName(), getPlayer())) {
			displayErrorMessage(getString(R.string.alert_player_duplicate_name_title), getString(R.string.alert_player_duplicate_name_message));
			return false;
		}
		return true;
	}
	
	private Player getPlayer() {
		if (!isNewPlayer()) {
			String playerName = getIntent().getStringExtra(PLAYER_NAME);
			return Team.getPlayerNamed(playerName);
		} else {
			return new Player();
		}
	}
	
}
