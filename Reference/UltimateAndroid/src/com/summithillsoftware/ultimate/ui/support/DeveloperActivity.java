package com.summithillsoftware.ultimate.ui.support;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

import com.summithillsoftware.ultimate.R;
import com.summithillsoftware.ultimate.ui.callout.CalloutTracker;

public class DeveloperActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_developer);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}
	
	public void clearCalloutsTapped(View v) {
		CalloutTracker.current().clear();
	}

}
