package com.summithillsoftware.ultimate.ui.twitter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.summithillsoftware.ultimate.R;
import com.summithillsoftware.ultimate.twitter.AutoTweetLevel;
import com.summithillsoftware.ultimate.twitter.GameTweeter;
import com.summithillsoftware.ultimate.ui.UltimateFragment;

public class AutoTweetFragment extends UltimateFragment {
	
	// widgets 
	private RadioGroup tweetRadioGroup;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_twitter_autotweet, null);
		connectWidgets(view);
		registerWidgetListeners();
		populateView();
		return view;
	}

	
	private void connectWidgets(View view) {
		tweetRadioGroup = (RadioGroup)view.findViewById(R.id.tweetRadioGroup);

	}
	
	private void registerWidgetListeners() {
		tweetRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
				case R.id.tweetMaxRadioButton:
					GameTweeter.current().setTweetLevel(AutoTweetLevel.TURNOVERS);
					break;
				case R.id.tweetMinRadioButton:
					GameTweeter.current().setTweetLevel(AutoTweetLevel.GOALS);
					break;					
				default:
					GameTweeter.current().setTweetLevel(AutoTweetLevel.NONE);
					break;
				}
			}
		});
	}
	
	private void populateView() {
		switch (GameTweeter.current().getTweetLevel()) {
		case TURNOVERS:
			tweetRadioGroup.check(R.id.tweetMaxRadioButton);
			break;			
		case GOALS:
			tweetRadioGroup.check(R.id.tweetMinRadioButton);
			break;
		default:
			tweetRadioGroup.check(R.id.tweetNoneRadioButton);
			break;
		}
	}
}
