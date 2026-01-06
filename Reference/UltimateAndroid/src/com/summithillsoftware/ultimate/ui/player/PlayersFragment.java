package com.summithillsoftware.ultimate.ui.player;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.summithillsoftware.ultimate.R;
import com.summithillsoftware.ultimate.ui.UltimateFragment;

public class PlayersFragment extends UltimateFragment {
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_players, container);
		initializeListView((ListView)view.findViewById(R.id.listview_players));
		return view;
	}

	private void initializeListView(ListView listView) {
		PlayersListAdapter adaptor = new PlayersListAdapter(this.getActivity());
		listView.setAdapter(adaptor);
	}
	
}

