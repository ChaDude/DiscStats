package com.summithillsoftware.ultimate.ui.team;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.summithillsoftware.ultimate.R;
import com.summithillsoftware.ultimate.UltimateApplication;
import com.summithillsoftware.ultimate.model.Team;
import com.summithillsoftware.ultimate.model.TeamDescription;
import com.summithillsoftware.ultimate.ui.Refreshable;
import com.summithillsoftware.ultimate.ui.UltimateActivity;
import com.summithillsoftware.ultimate.ui.ViewHelper;
import com.summithillsoftware.ultimate.ui.ViewHelper.AnchorPosition;
import com.summithillsoftware.ultimate.ui.callout.CalloutTracker;
import com.summithillsoftware.ultimate.ui.callout.CalloutView;
import com.summithillsoftware.ultimate.ui.callout.CalloutView.CalloutAnimationStyle;
import com.summithillsoftware.ultimate.ui.cloud.CloudTeamDownloadDialog;
import com.summithillsoftware.ultimate.workflow.TeamDownloadWorkflow;

public class TeamsActivity extends UltimateActivity implements Refreshable {
	boolean showingTeamOnlyCallout = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_teams);
		registerTeamSelectedListener();
		if (UltimateApplication.current().isAppStartInProgress()) {
			UltimateApplication.current().setAppStartComplete();
			goToTeamActivity();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.teams, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.action_add) {
			if (!showOnlyYourTeamsCallout()) {
				Intent intent = new Intent(this, TeamActivity.class);
				intent.putExtra(TeamActivity.NEW_TEAM, true);
				startActivity(intent);
			}
		}
		if (item.getItemId() == R.id.action_download) {
			showTeamDownloadDialog();
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume() {
		super.onResume();
		populateView();

	}
	
	private void populateView() {
		getTeamsListViewAdapter().resetTeams();
	}
	
	private ListView getTeamsListView() {
		return (ListView)findViewById(R.id.teamsFragment).findViewById(R.id.listview_teams);
	}
	
	private TeamsListAdaptor getTeamsListViewAdapter() {
		return (TeamsListAdaptor)getTeamsListView().getAdapter();
	}
	
	private void registerTeamSelectedListener() {
		getTeamsListView().setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				TeamDescription selectedTeam = (TeamDescription)getTeamsListViewAdapter().getItem(position);
				Team.setCurrentTeamId(selectedTeam.getTeamId());
				goToTeamActivity();
			}

		});
	}
	
	private void goToTeamActivity() {
		startActivity(new Intent(this, TeamActivity.class));
	}
	
	private void showTeamDownloadDialog() {
	    FragmentManager fragmentManager = getSupportFragmentManager();
	    CloudTeamDownloadDialog downloadDialog = new CloudTeamDownloadDialog();
		TeamDownloadWorkflow workflow = new TeamDownloadWorkflow();
	    downloadDialog.setWorkflow(workflow);
	    downloadDialog.show(fragmentManager, "dialog");
	}
	
	private boolean showOnlyYourTeamsCallout() {
		if (showingTeamOnlyCallout || CalloutTracker.current().hasCalloutBeenShown(CalloutTracker.CALLOUT_OUR_TEAMS_ONLY)) {
			return false;
		} else {
			showingTeamOnlyCallout = true;
			final List<CalloutView> callouts = new ArrayList<CalloutView>();
			
			View menuView = findViewById(R.id.action_add);
			if (menuView != null) {
				Point anchor = locationInRootView(menuView);
				anchor = ViewHelper.locationInRect(anchor, menuView.getWidth(), menuView.getHeight(), AnchorPosition.BottomLeft);
		
				CalloutView callout = new CalloutView(this, anchor, 30, 210, R.string.callout_only_your_teams);
				callout.setAnimateStyle(CalloutAnimationStyle.FromRight);  
				callout.setCalloutWidth(200);
				callout.setCalloutTrackerID(CalloutTracker.CALLOUT_OUR_TEAMS_ONLY);
				callouts.add(callout);
				
				showCallouts(callouts);
			}
			
			return true;
		}
	}

	@Override
	public void refresh() {
		populateView();
	}

}
