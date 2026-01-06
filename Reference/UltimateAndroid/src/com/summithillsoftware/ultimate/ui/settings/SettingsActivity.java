package com.summithillsoftware.ultimate.ui.settings;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.summithillsoftware.ultimate.R;
import com.summithillsoftware.ultimate.cloud.CloudClient;
import com.summithillsoftware.ultimate.model.Preferences;
import com.summithillsoftware.ultimate.ui.UltimateActivity;

public class SettingsActivity extends UltimateActivity {
	
	// widgets
	private TextView currentSignin;
	private View signedOffInstructions;
	private Button signoffButton;
	private Button adminSiteButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		connectWidgets();
		populateView();
	}

	private void connectWidgets() {
		currentSignin = (TextView)findViewById(R.id.settingsFragment).findViewById(R.id.currentSignin);
		signedOffInstructions = (View)findViewById(R.id.settingsFragment).findViewById(R.id.signedOffInstructions);
		signoffButton = (Button)findViewById(R.id.settingsFragment).findViewById(R.id.signoffButton);
		adminSiteButton = (Button)findViewById(R.id.settingsFragment).findViewById(R.id.adminSiteButton);
	}
	
	private void populateView() {
		boolean isSignedIn = Preferences.current().hasCloudEMail();
		currentSignin.setText(isSignedIn ? Preferences.current().getCloudEMail() : getString(R.string.label_settings_current_signed_off));
		signedOffInstructions.setVisibility(isSignedIn ? View.GONE : View.VISIBLE);
		signoffButton.setVisibility(isSignedIn ? View.VISIBLE : View.GONE);		
		adminSiteButton.setText(CloudClient.ADMIN_URL);
	}
	
	public void signoffClicked(View v) {
		CloudClient.current().clearExistingAuthentication();
		populateView();
	}
	
	public void adminClicked(View v) {
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(CloudClient.ADMIN_URL));
		startActivity(browserIntent);
	}

}
