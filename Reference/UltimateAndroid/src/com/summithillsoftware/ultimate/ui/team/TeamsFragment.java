package com.summithillsoftware.ultimate.ui.team;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.summithillsoftware.ultimate.R;
import com.summithillsoftware.ultimate.ui.UltimateFragment;

public class TeamsFragment extends UltimateFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_teams, container);
		initializeListView((ListView)view.findViewById(R.id.listview_teams));
		return view;
	}
	
	private void initializeListView(ListView listView) {
		TeamsListAdaptor adaptor = new TeamsListAdaptor(this.getActivity());
		listView.setAdapter(adaptor);
	}

}
