package com.summithillsoftware.ultimate.ui.twitter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class TwitterPagerAdapter extends FragmentPagerAdapter {
	private Fragment tweetFragment;
	private Fragment logFragment;
	private Fragment autoTweetFragment;

	public TwitterPagerAdapter(FragmentManager fm) {
		super(fm);
	}
	
	@Override
	public Fragment getItem(int index) {
		switch (index) {
        case 0:
            return getTweetFragment();
        case 1:
            return getTweetLogFragment();
        case 2:
            return getAutoTweetFragment();
        }
 
        return null;
	}

	@Override
	public int getCount() {
		return 3;
	}
	
	private Fragment getTweetFragment() {
		if (tweetFragment == null) {
			tweetFragment = new TweetFragment();
		}
		return tweetFragment;
	}
	
	private Fragment getTweetLogFragment() {
		if (logFragment == null) {
			logFragment = new TweetLogFragment();
		}
		return logFragment;
	}
	
	private Fragment getAutoTweetFragment() {
		if (autoTweetFragment == null) {
			autoTweetFragment = new AutoTweetFragment();
		}
		return autoTweetFragment;
	}



}
