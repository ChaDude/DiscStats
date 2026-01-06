package com.summithillsoftware.ultimate.ui.stats;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.summithillsoftware.ultimate.R;
import com.summithillsoftware.ultimate.ui.UltimateFragment;

public class StatsFragment extends UltimateFragment {
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_stats, container);
		initializePlayerStatsListView((ListView)view.findViewById(R.id.listview_player_stats));		
		return view;
	}
	
	private void initializePlayerStatsListView(ListView listView) {
		PlayerStatisticListAdapter adaptor = new PlayerStatisticListAdapter(this.getActivity());
		listView.setAdapter(adaptor);
	}
	
}

