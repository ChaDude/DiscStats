package com.summithillsoftware.ultimate.ui.player;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.summithillsoftware.ultimate.R;
import com.summithillsoftware.ultimate.model.Player;
import com.summithillsoftware.ultimate.model.Team;

public class PlayersListAdapter extends BaseAdapter {
	private List<Player> sortedPlayers;
	private Context context;

	public PlayersListAdapter(Context context) {
		super();
		this.context = context;
		resetPlayers();
	}
	
	public void resetPlayers() {
		sortedPlayers = null;
		notifyDataSetChanged();
	}
	
	private List<Player>getSortedPlayers() {
		if (sortedPlayers == null) {
			sortedPlayers = Team.current().getPlayersSorted();
		}
		return sortedPlayers;
	}

	@Override
	public int getCount() {
		return getSortedPlayers().size();
	}

	@Override
	public Object getItem(int position) {
		return getSortedPlayers().get(position);
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
			rowView = inflater.inflate(R.layout.rowlayout_players, null);
		}
	
		TextView playerNameTextView = (TextView)rowView.findViewById(R.id.text_players_description);
		ImageView playerImageView = (ImageView)rowView.findViewById(R.id.icon_player);
		
		Player player = getSortedPlayers().get(index);
		playerNameTextView.setText(player.getName());
		playerNameTextView.setTextColor(parent.getResources().getColor(player.isAbsent() ? R.color.Gray : R.color.White));
		if (player.getNumber() != null && player.getNumber().trim().length() > 0) {
			TextView playerNumberTextView = (TextView)rowView.findViewById(R.id.text_players_number);
			playerNumberTextView.setText(" (" + player.getNumber() + ")");
			playerNumberTextView.setTextColor(parent.getResources().getColor(player.isAbsent() ? R.color.Gray : R.color.White));
		}
		playerImageView.setImageResource(player.isAbsent() ? R.drawable.ic_action_user_absent : R.drawable.social_person);
		
		return rowView;
	}
	
}
