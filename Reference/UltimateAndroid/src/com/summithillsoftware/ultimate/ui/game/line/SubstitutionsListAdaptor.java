package com.summithillsoftware.ultimate.ui.game.line;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.summithillsoftware.ultimate.R;
import com.summithillsoftware.ultimate.model.Game;
import com.summithillsoftware.ultimate.model.PlayerSubstitution;
import com.summithillsoftware.ultimate.model.PlayerSubstitutionSet;

public class SubstitutionsListAdaptor extends BaseAdapter {
	private Context context;
	private List<PlayerSubstitutionSet> playerSubstitutionSets;

	public SubstitutionsListAdaptor(Context context) {
		super();
		this.context = context;
		resetPlayerSubstitutionSets();
	}
	
	public void resetPlayerSubstitutionSets() {
		List<PlayerSubstitution> playerSubs = Game.current().substitutionsForCurrentPoint();
		playerSubstitutionSets = PlayerSubstitutionSet.createSubstitutionSets(playerSubs);
		notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {
		return playerSubstitutionSets.size();
	}

	@Override
	public Object getItem(int index) {
		return playerSubstitutionSets.get(index);
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
			rowView = inflater.inflate(R.layout.rowlayout_substitutions, null);
		}
		
		PlayerSubstitutionSet playerSubstitutionsSet = playerSubstitutionSets.get(index);
		
		String playersOutText = context.getString(R.string.label_line_substitution_out) + ": " + playerSubstitutionsSet.playerNames(true);
		String playersInText = context.getString(R.string.label_line_substitution_in) + ": " + playerSubstitutionsSet.playerNames(false);	
	
		TextView playersOutView = (TextView)rowView.findViewById(R.id.text_line_substitution_set_out);
		TextView playersInView = (TextView)rowView.findViewById(R.id.text_line_substitution_set_in);
		
		playersOutView.setText(playersOutText);
		playersInView.setText(playersInText);	
		
		return rowView;
	}
	
	public void removeMostRecentSubstitutionSetFromGame() {
		if (!playerSubstitutionSets.isEmpty()) {
			PlayerSubstitutionSet lastPlayerSubstitutionsSet = playerSubstitutionSets.get(0);
			for (PlayerSubstitution playerSub : lastPlayerSubstitutionsSet.getSubstitutions()) {
				Game.current().removeSubstitutionForCurrentPoint(playerSub);
			}
			resetPlayerSubstitutionSets();
		}
	}

}
