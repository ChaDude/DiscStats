package com.summithillsoftware.ultimate.ui.stats;

import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.summithillsoftware.ultimate.R;
import com.summithillsoftware.ultimate.stats.PlayerStat;

public class PlayerStatisticListAdapter extends BaseAdapter {
	private List<PlayerStat> playerStats;
	private Context context;

	public PlayerStatisticListAdapter(Context context) {
		super();
		this.context = context;
		resetPlayers();
	}
	
	public void resetPlayers() {
		playerStats = null;
		notifyDataSetChanged();
	}
	
	private List<PlayerStat>getSortedPlayerStats() {
		if (playerStats == null) {
			playerStats = Collections.emptyList();
		}
		return playerStats;
	}

	@Override
	public int getCount() {
		return getSortedPlayerStats().size();
	}

	@Override
	public Object getItem(int position) {
		return getSortedPlayerStats().get(position);
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
			rowView = inflater.inflate(R.layout.rowlayout_statistics_player, null);
		}
	
		TextView playerNameTextView = (TextView)rowView.findViewById(R.id.text_player_description);
		TextView statTextView = (TextView)rowView.findViewById(R.id.text_player_stat);
		
		PlayerStat playerStat = getSortedPlayerStats().get(index);
		playerNameTextView.setText(playerStat.getPlayer().getName());
		statTextView.setText(playerStat.statAsString());
		
		return rowView;
	}

	public void setPlayerStats(List<PlayerStat> playerStats) {
		this.playerStats = playerStats;
		notifyDataSetChanged();
	}
	
}
