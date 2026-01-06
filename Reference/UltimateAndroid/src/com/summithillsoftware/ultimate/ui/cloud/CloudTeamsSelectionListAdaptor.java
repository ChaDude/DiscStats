package com.summithillsoftware.ultimate.ui.cloud;

import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.summithillsoftware.ultimate.R;
import com.summithillsoftware.ultimate.UltimateApplication;
import com.summithillsoftware.ultimate.model.Team;
import com.summithillsoftware.ultimate.workflow.TeamDownloadWorkflow;

public class CloudTeamsSelectionListAdaptor extends BaseAdapter {
	private Context context;
	private List<Team> teams;

	public CloudTeamsSelectionListAdaptor(Context context) {
		super();
		this.context = context;
		resetTeams();
	}
	
	public void resetTeams() {
		TeamDownloadWorkflow workflow = (TeamDownloadWorkflow)UltimateApplication.current().getActiveWorkflow();
		teams = workflow.getTeamsAvailable();
		Collections.sort(teams,Team.TeamListComparator);
		notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {
		return teams.size();
	}

	@Override
	public Object getItem(int index) {
		return teams.get(index);
	}

	@Override
	public long getItemId(int index) {
		return index;
	}

	@Override
	public View getView(int index, View reusableRowView, ViewGroup parent) {
		View rowView = reusableRowView;
		if (reusableRowView == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			rowView = inflater.inflate(R.layout.rowlayout_teams, null);
		}
	
		TextView teamNameTextView = (TextView)rowView.findViewById(R.id.text_team_name);
		TextView cloudIdTextView = (TextView)rowView.findViewById(R.id.text_team_cloud_id);
		
		Team team = teams.get(index);
		teamNameTextView.setText(team.getName());
		cloudIdTextView.setText(team.getCloudId());	
		
		return rowView;
	}
	




}
