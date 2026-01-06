package com.summithillsoftware.ultimate.ui.support;

import java.io.File;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.CheckBox;

import com.summithillsoftware.ultimate.R;
import com.summithillsoftware.ultimate.UltimateApplication;
import com.summithillsoftware.ultimate.ui.UltimateActivity;

public class SupportActivity extends UltimateActivity {
	
	// widgets
	private CheckBox includeLogFilesCheckbox;
	private CheckBox includeTeamsCheckbox;
	private Button sendEMailButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_support);
		connectWidgets();
		registerListeners();
	}

	private void connectWidgets() {
		includeLogFilesCheckbox = (CheckBox)findViewById(R.id.settingsFragment).findViewById(R.id.includeLogFilesCheckbox);
		includeTeamsCheckbox = (CheckBox)findViewById(R.id.settingsFragment).findViewById(R.id.includeTeamsCheckbox);	
		sendEMailButton = (Button)findViewById(R.id.settingsFragment).findViewById(R.id.sendEMailButton);
	}
	
	private void registerListeners() {
		sendEMailButton.setOnLongClickListener(new OnLongClickListener() { 
	        @Override
	        public boolean onLongClick(View v) {
	        	goToDeveloperActivity();
	            return true;
	        }
	    });
	}
	
	public void sendEmailClicked(View v) {
		showEmailCompose();
	}
	
	private void showEmailCompose() {
		boolean includeLogs = includeLogFilesCheckbox.isChecked();
		boolean includeTeams = includeTeamsCheckbox.isChecked();
		
		Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);  
		emailIntent.setType("plain/text"); 
		emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,new String[] { "support@ultianalytics.com" });
		
		if (includeLogs || includeTeams) {
			File attachment = null;
			attachment = UltimateApplication.current().createZipForSupport(includeTeams);
			if (attachment != null) {
				Uri uri = Uri.fromFile(attachment);
				emailIntent.putExtra(Intent.EXTRA_STREAM, uri);
			}
		}
		
		startActivity(emailIntent);  
		finish();
	}
	
	private void goToDeveloperActivity() {
		startActivity(new Intent(this, DeveloperActivity.class));
	}

}
