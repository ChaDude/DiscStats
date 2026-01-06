package com.summithillsoftware.ultimate.ui.twitter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.summithillsoftware.ultimate.R;
import com.summithillsoftware.ultimate.ui.UltimateFragment;

public class TweetLogFragment extends UltimateFragment {
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_twitter_tweetlog, null);
		return view;
	}
	
	@Override
	public void onStart() {
		super.onStart();
		initializeListView();
	}
	
	private void initializeListView() {
		TweetsListAdapter adaptor = new TweetsListAdapter(this.getActivity());
		getTweetsListView().setAdapter(adaptor);
		adaptor.resetTweets();
	}
	
	private ListView getTweetsListView() {
		return (ListView)getView().findViewById(R.id.tweetList);
	}



}
