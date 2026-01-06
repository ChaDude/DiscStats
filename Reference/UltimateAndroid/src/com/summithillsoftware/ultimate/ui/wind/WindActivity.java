package com.summithillsoftware.ultimate.ui.wind;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.summithillsoftware.ultimate.R;
import com.summithillsoftware.ultimate.model.Game;
import com.summithillsoftware.ultimate.model.Wind;
import com.summithillsoftware.ultimate.ui.UltimateActivity;
import com.summithillsoftware.ultimate.ui.wind.WindDirectionView.OnWindDirectionChangedListener;

public class WindActivity extends UltimateActivity {
	public static final String WIND_STATE = "Wind";
	public static final String NEW_STATE = "IsNew";
	private ImageButton buttonDirectionRight;
	private ImageButton buttonDirectionLeft;
	private TextView windSpeed;
	private SeekBar windSpeedSeekBar;
	private TextView directionLabel;
	private WindDirectionView directionView;
	private Button lookupSpeedButton;
	private Button saveButton;
	
	private Wind wind;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wind);
		if (savedInstanceState != null) {
			wind = (Wind) savedInstanceState.getSerializable(WIND_STATE);
		}
		if (wind == null) {
			wind = (Wind) getIntent().getSerializableExtra(WIND_STATE);
		}
		connectWidgets();
		registerWidgetListeners();
		populateView();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(WIND_STATE, getWind());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.wind, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}
	
	private void connectWidgets() {
		buttonDirectionRight = (ImageButton)findViewById(R.id.windFragment).findViewById(R.id.buttonDirectionRight);
		buttonDirectionLeft = (ImageButton)findViewById(R.id.windFragment).findViewById(R.id.buttonDirectionLeft);
		windSpeedSeekBar = (SeekBar)findViewById(R.id.windFragment).findViewById(R.id.windSpeedSeekBar);
		lookupSpeedButton = (Button)findViewById(R.id.windFragment).findViewById(R.id.lookupSpeedButton);
		windSpeed = (TextView)findViewById(R.id.windFragment).findViewById(R.id.windSpeed);
		directionView = (WindDirectionView)findViewById(R.id.windFragment).findViewById(R.id.directionView);
		directionLabel = (TextView)findViewById(R.id.windFragment).findViewById(R.id.directionLabel);
		directionView.setTextSize(directionLabel.getTextSize()); 
		saveButton = (Button)findViewById(R.id.windFragment).findViewById(R.id.saveButton);
	}
	
	private void registerWidgetListeners() {
		buttonDirectionRight.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				handleFirstPullDirectionButtonTap(true);
			}
		});
		buttonDirectionLeft.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				handleFirstPullDirectionButtonTap(false);
			}
		});
		directionView.setChangeListener(new OnWindDirectionChangedListener() {
			@Override
			public void onWindDirectionChanged(int degreesFromNorth) {
				getWind().setDirectionDegrees(degreesFromNorth);
				saveChanges();
				updateSaveEnablement();
			}
		});
		windSpeedSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				getWind().setMph(progress);
				updateWindSpeed(false);
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
				// no-op
			}

			public void onStopTrackingTouch(SeekBar seekBar) {
				saveChanges();
			}
		});
		lookupSpeedButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View paramView) {
				openWindspeedLookupSiteInBrowser();
			}
		});
	}
	
	private void handleFirstPullDirectionButtonTap(boolean isFirstPullLeftToRight) {
		getWind().setFirstPullLeftToRight(isFirstPullLeftToRight);
		populateView();
		saveChanges();
	}
	
	private void populateView() {
		updateFirstPullDirectionArrow();
		updateWindSpeed(true);
		updateWindDirection();
		updateSaveEnablement();
	}

	private void updateFirstPullDirectionArrow() {
		boolean isRight = getWind() == null || getWind().isFirstPullLeftToRight();
		buttonDirectionRight.setSelected(isRight);
		buttonDirectionLeft.setSelected(!isRight);
	}
	
	private void updateWindSpeed(boolean initial) {
		windSpeed.setText(Integer.toString(getWind().getMph()));
		if (initial) {
			windSpeedSeekBar.setProgress(getWind().getMph());
		}
		updateSaveEnablement();
	}
	
	private void updateWindDirection() {
		directionView.setDegreesFromNorth(getWind().getDirectionDegrees());
	}
	
	private void openWindspeedLookupSiteInBrowser() {
		String website = "http://i.wund.com";
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(website));
		startActivity(browserIntent);
	}
	
	private Wind getWind() {
		if (wind == null) {
			wind = new Wind();
		}
		return wind;
	}
	
	private void saveChanges() {
		if (Game.current().hasBeenSaved()) {
			Game.current().save();
		}
	}
	
	private void updateSaveEnablement() {
		boolean isSaveable = isNewWind() ? getWind().getDirectionDegrees() > -1 && getWind().getMph() > 0 : true;
		saveButton.setEnabled(isSaveable);
	}
	
	public void saveClicked(View v) {
		Game.current().setWind(getWind());
		if (Game.current().hasBeenSaved()) {
			Game.current().save();
		}
		finish();
	}

	public void cancelClicked(View v) {
		finish();
	}
	
	private boolean isNewWind() {
		return getIntent().getBooleanExtra(NEW_STATE, false);
	}

}
