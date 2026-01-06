package com.summithillsoftware.ultimate.ui.game;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.summithillsoftware.ultimate.R;
import com.summithillsoftware.ultimate.ui.UltimateFragment;

public class GamesFragment extends UltimateFragment {
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_games, container);
		initializeListView((ListView)view.findViewById(R.id.listview_games));
		return view;
	}

	private void initializeListView(ListView listView) {
		GamesListAdapter adaptor = new GamesListAdapter(this.getActivity());
		listView.setAdapter(adaptor);
	}
	
}

