package com.summithillsoftware.ultimate.ui.cloud;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
import com.summithillsoftware.ultimate.model.GameDescription;
import com.summithillsoftware.ultimate.util.DateUtil;
import com.summithillsoftware.ultimate.workflow.GameDownloadWorkflow;

public class CloudGamesSelectionListAdapter extends BaseAdapter {
	private List<GameDescription> sortedGames;
	private Context context;

	public CloudGamesSelectionListAdapter(Context context) {
		super();
		this.context = context;
		resetGames();
	}
	
	public void resetGames() {
		sortedGames = null;
		notifyDataSetChanged();
	}
	
	private List<GameDescription>getSortedGames() {
		if (sortedGames == null) {
			
			GameDownloadWorkflow workflow = (GameDownloadWorkflow)UltimateApplication.current().getActiveWorkflow();
			sortedGames = workflow.getGamesAvailable();
			Collections.sort(sortedGames,GameDescription.GameDescriptionListComparator);
		}
		return sortedGames;
	}

	@Override
	public int getCount() {
		return getSortedGames().size();
	}

	@Override
	public Object getItem(int position) {
		return getSortedGames().get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int index, View reusableRowView, ViewGroup parent) {
		View rowView = reusableRowView;
		if (reusableRowView == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			rowView = inflater.inflate(R.layout.rowlayout_games, null);
		}
	
		TextView opponentNameView = (TextView)rowView.findViewById(R.id.text_game_opponent_name);
		TextView dateView = (TextView)rowView.findViewById(R.id.text_game_date);
		TextView tournamentView = (TextView)rowView.findViewById(R.id.text_game_tournament_name);
		
		GameDescription game = getSortedGames().get(index);
		opponentNameView.setText(game.getOpponentName());
		if (game.getStartDate() != null) {
			String startDateTime = DateFormat.getTimeInstance().format(game.getStartDate());
			if (DateUtil.isToday(game.getStartDate())) {
				startDateTime = UltimateApplication.current().getString(R.string.common_today) + " " + startDateTime;
			} else {
				startDateTime = SimpleDateFormat.getDateInstance().format(game.getStartDate()) + " " + startDateTime;
			}
			dateView.setText(startDateTime);
		}
		if (!game.getTournamentName().trim().isEmpty()) {
			String tournamentDescription = UltimateApplication.current().getString(R.string.label_game_at_tournament) + " " + game.getTournamentName();
			tournamentView.setText(tournamentDescription);
			tournamentView.setVisibility(View.VISIBLE);
		} else {
			tournamentView.setVisibility(View.GONE);
		}
		
		return rowView;
	}
	
}
