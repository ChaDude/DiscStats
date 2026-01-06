package com.summithillsoftware.ultimate.ui.twitter;

import java.util.ArrayList;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBar.TabListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;

import com.summithillsoftware.ultimate.R;
import com.summithillsoftware.ultimate.model.Preferences;
import com.summithillsoftware.ultimate.twitter.TweetQueue;
import com.summithillsoftware.ultimate.twitter.TwitterClient;
import com.summithillsoftware.ultimate.ui.UltimateActivity;

public class TwitterActivity extends UltimateActivity implements TabListener, ViewPager.OnPageChangeListener {
	private ViewPager viewPager;
	private TwitterPagerAdapter pageAdapter;
	private ArrayList<Tab> tabs = new ArrayList<Tab>();
	private MenuItem logoutMenuItem;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_twitter);
		connectWidgets();
		configureTabs();
	    if (savedInstanceState != null) {
	    	getSupportActionBar().setSelectedNavigationItem(savedInstanceState.getInt("tab", 0));
	    }
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.twitter, menu);
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
	    super.onPrepareOptionsMenu(menu);
	    logoutMenuItem = menu.findItem(R.id.action_twitter_logout);
	    updateLogoutMenu();
	    return true;
	}
	
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("tab", getActionBar().getSelectedNavigationIndex());
    }
	
	@Override
	protected void onStart() {
		super.onStart();
		if (!TwitterClient.current().isSignedIn()) {
			showSignonDialog();
		} else {
			populateView();
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			return navigateUp();
		case R.id.action_twitter_logout:
			TwitterClient.current().clearTwitterCredentials();
			showSignonDialog();
			populateView();
			return true;
		}		
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onDestroy() {
		super.onPause();
		TweetQueue.current().setTweetListener(null);  // clear tweet listening
	}
	
	public void signonDismissed() {
		if (isSignedIn()) {
			populateView();
		} else {
			finish();
		}
	}
	
	private void populateView() {
		populateTitle();
		updateLogoutMenu();
	}
	
	private void populateTitle() {
		String moniker = Preferences.current().getTwitterMoniker();
		moniker = moniker == null ? "" : " @" + moniker;
		setTitle(getString(R.string.title_activity_twitter) + moniker);
	}
	
	private void updateLogoutMenu() {
		if (logoutMenuItem != null) {
			logoutMenuItem.setVisible(isSignedIn());
		}
	}
	
	private void connectWidgets() {
		viewPager = (ViewPager)findViewById(R.id.twitterFragment).findViewById(R.id.pager);
		pageAdapter = new TwitterPagerAdapter(getSupportFragmentManager());
		viewPager.setAdapter(pageAdapter);
		viewPager.setOnPageChangeListener(this);
	}
	
	private void configureTabs() {
		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);        
        Tab tab = getSupportActionBar().newTab().setText(R.string.tab_twitter_tweet).setTabListener(this);
        tab.setTag("tweet");
        tabs.add(tab);
        getSupportActionBar().addTab(tab, true);
        tab = getSupportActionBar().newTab().setText(R.string.tab_twitter_tweet_log).setTabListener(this);
        tab.setTag("log");
        tabs.add(tab);
        getSupportActionBar().addTab(tab, false);
        tab = getSupportActionBar().newTab().setText(R.string.tab_twitter_auto_tweet).setTabListener(this);
        tab.setTag("auto");
        tabs.add(tab);
        getSupportActionBar().addTab(tab, false);
	}
	
	
	private boolean isSignedIn() {
		return TwitterClient.current().isSignedIn();
	}
	
	private void showSignonDialog() {
	    FragmentManager fragmentManager = getSupportFragmentManager();
	    FragmentTransaction ft = fragmentManager.beginTransaction();
	    Fragment prev = fragmentManager.findFragmentByTag("dialog");
	    if (prev != null) {
	        ft.remove(prev);
	    }
	    
	    TwitterSignonDialog signonDialog = new TwitterSignonDialog();
	    signonDialog.show(fragmentManager, "dialog");
	    
	    ft.commit();
	}

	/* ActionBar.TabListener */

	@Override
	public void onTabReselected(Tab arg0, FragmentTransaction arg1) {
		// no-op
		
	}

	@Override
    public void onTabSelected(Tab tab, FragmentTransaction ft) {
        for (int i=0; i < tabs.size(); i++) {
            if (tabs.get(i).getTag().equals(tab.getTag())) {
                viewPager.setCurrentItem(i);
                // dismiss the keyboard
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(viewPager.getApplicationWindowToken(), 0);
            }
        }
    }

	@Override
	public void onTabUnselected(Tab arg0, FragmentTransaction arg1) {
		// no-op
		
	}

	
	/*   ViewPager.OnPageChangeListener */
	
	@Override
	public void onPageScrollStateChanged(int state) {
		// no-op
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
		// no-op
	}

	@Override
    public void onPageSelected(int position) {
		getSupportActionBar().setSelectedNavigationItem(position);
    }




}
