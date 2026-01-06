package com.summithillsoftware.ultimate.ui.twitter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.summithillsoftware.ultimate.R;
import com.summithillsoftware.ultimate.twitter.Tweet;
import com.summithillsoftware.ultimate.twitter.TweetQueue;

public class TweetsListAdapter extends BaseAdapter implements TweetQueue.TweetListener {
	private List<Tweet> recentTweets;
	private Context context;

	public TweetsListAdapter(Context context) {
		super();
		this.context = context;
		TweetQueue.current().setTweetListener(this);
		resetTweets();
	}
	
	public void resetTweets() {
		recentTweets = null;
		notifyDataSetChanged();
	}
	
	private List<Tweet>getRecentTweets() {
		if (recentTweets == null) {
			
			// add the waiting tweets
			recentTweets = TweetQueue.current().waitingTweets();
			
			// add the completed tweets (filter out the cancelled tweets)
			List<Tweet>completedTweets = new ArrayList<Tweet>();
			for (Tweet tweet : TweetQueue.current().recentTweets()) {
				if (!tweet.isCancelled() && !recentTweets.contains(tweet)) {
					completedTweets.add(tweet);
				}
			}
			
			recentTweets.addAll(completedTweets);
		}
		return recentTweets;
	}

	@Override
	public int getCount() {
		return getRecentTweets().size();
	}

	@Override
	public Object getItem(int position) {
		return getRecentTweets().get(position);
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
			rowView = inflater.inflate(R.layout.rowlayout_tweets, null);
		}
		
		Tweet tweet = getRecentTweets().get(index);
		
		// tweet text
		TextView tweetTextView = (TextView)rowView.findViewById(R.id.tweetText);
		tweetTextView.setText(getTweetText(tweet));
		
		boolean tweetHadError = tweet.hadSendError();
		tweetTextView.setTextColor(context.getResources().getColor(tweetHadError ? R.color.Red : R.color.White));
		
		// "since" time
		TextView tweetSinceView = (TextView)rowView.findViewById(R.id.tweetSince);
		if (tweet.isWaiting()) {
			tweetSinceView.setText("");
		} else {
			tweetSinceView.setText(getSinceText(tweet));
		}
		
		return rowView;
	}
	
	private String getTweetText(Tweet tweet) {
		if (tweet.isSkipped()) {
			return context.getString(R.string.twitter_description_skipped) + ": " + tweet.getText();
		} else if (tweet.isWaiting()) {
			return context.getString(R.string.twitter_description_waiting) + ": " + tweet.getText();	
		} else if (tweet.isSending()) {
			return context.getString(R.string.twitter_description_sending) + ": " + tweet.getText();				
		} else {
			switch (tweet.getSendStatus()) {
			case RejectedRetweet:
				return context.getString(R.string.twitter_description_rejected_duplicate) + ": " + tweet.getText();
			case RejectedRateLimitExceeded:
				return context.getString(R.string.twitter_description_rejected_rate_limit) + ": " + tweet.getText();
			case RejectedForUnkownReason:
				return context.getString(R.string.twitter_description_rejected) + ": " + tweet.getText();
			case RejectedBadCredentials:
				return context.getString(R.string.twitter_description_rejected_bad_credentials) + ": " + tweet.getText();
			case CommunicationsError:
				return context.getString(R.string.twitter_description_rejected_comm_error) + ": " + tweet.getText();
			case UnknownError:
				return context.getString(R.string.twitter_description_unknown) + ": " + tweet.getText();
			default:
				return tweet.getText();
			}
		}
	}
	
	private String getSinceText(Tweet tweet) {
	    long now = System.currentTimeMillis();
	    long secondsSince = (now - tweet.getTime()) / 1000;
	    if (secondsSince < 60) {
	    	return Long.toString(secondsSince) + context.getString(R.string.twitter_description_seconds);
	    } else if (secondsSince < 3600) {
	    	long minutesSince = secondsSince / 60;
	    	return Long.toString(minutesSince) + context.getString(R.string.twitter_description_minutes);
	    } else if (secondsSince < 86400) {
	    	long hoursSince = secondsSince / 3600;
	    	return Long.toString(hoursSince) + context.getString(R.string.twitter_description_hours);
	    } else if (secondsSince < 2592000) {
	    	long daysSince = secondsSince / 86400;
	    	return Long.toString(daysSince) + context.getString(R.string.twitter_description_days);
	    } else {
	        return context.getString(R.string.twitter_description_old);
	    }
	}

	/*  TweetQueue.TweetListener */
	
	@Override
	public void tweetStateChanged() {
		resetTweets();
	}

}
