package com.summithillsoftware.ultimate.ui.team;

import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.summithillsoftware.ultimate.R;
import com.summithillsoftware.ultimate.model.Team;
import com.summithillsoftware.ultimate.model.TeamDescription;

public class TeamsListAdaptor extends BaseAdapter {
	private Context context;
	private List<TeamDescription> teams;

	public TeamsListAdaptor(Context context) {
		super();
		this.context = context;
		resetTeams();
	}
	
	public void resetTeams() {
		teams = Team.retrieveTeamDescriptions();
		Collections.sort(teams,TeamDescription.TeamDescriptionListComparator);
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
		
		TeamDescription team = teams.get(index);
	
		TextView teamNameTextView = (TextView)rowView.findViewById(R.id.text_team_name);
		TextView cloudIdTextView = (TextView)rowView.findViewById(R.id.text_team_cloud_id);
		TextView cloudIdTextViewLabel = (TextView)rowView.findViewById(R.id.text_team_cloud_id_label);
		boolean showCloudId = team.getCloudId() != null && !team.getCloudId().trim().isEmpty();
		cloudIdTextView.setVisibility(showCloudId ? View.VISIBLE : View.GONE);
		cloudIdTextViewLabel.setVisibility(showCloudId ? View.VISIBLE : View.GONE);
		
		teamNameTextView.setText(team.getName());
		cloudIdTextView.setText(team.getCloudId());	
		
		return rowView;
	}
	




}
