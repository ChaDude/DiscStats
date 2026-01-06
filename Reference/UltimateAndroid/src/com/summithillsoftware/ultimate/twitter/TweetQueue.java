package com.summithillsoftware.ultimate.twitter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import android.os.Handler;
import android.os.Looper;

import com.summithillsoftware.ultimate.UltimateApplication;
import com.summithillsoftware.ultimate.util.UltimateLogger;

public class TweetQueue {
	private static int TWEET_SEND_DELAY_SECONDS = 15;   
	private static int TWEET_QUEUE_PROCESSING_DELAY_SECONDS = 5;
	
	private static long TWEET_SEND_DELAY_MS = TWEET_SEND_DELAY_SECONDS * 1000; 
	private static int TWEET_QUEUE_PROCESSING_DELAY_MS = TWEET_QUEUE_PROCESSING_DELAY_SECONDS * 1000;

	private static int TWEET_LOG_SIZE = 30;
	private static TweetQueue Current;
	private Handler handler;
	private Queue<Tweet> waitingQueue;
	private List<Tweet> recentsList;
	
	private String tweetListenerMutex = "ListenerMutex";
	private TweetListener tweetListener;
	
	static {
		Current = new TweetQueue();
	}
	
	private TweetQueue() {
		createHandler();
		waitingQueue = new ConcurrentLinkedQueue<Tweet>();
		recentsList = Collections.synchronizedList(new ArrayList<Tweet>());
	}
	
	public static TweetQueue current() {
		return Current;
	}

	private void createHandler() {
		Thread thread = new Thread() {
		    public void run() {
		        Looper.prepare();
		        handler = new Handler();
		        Looper.loop();
		    }
		};
		thread.start();
	}
	
	public void submitTweet(String text) {
		submitTweet(new Tweet(text));
	}
	
	public void submitTweet(Tweet tweet) {
		queueTweet(tweet);
	}
	
	// returns the recent tweets in order: most recent first
	public List<Tweet>recentTweets() {
		List<Tweet> recents = new ArrayList<Tweet>(recentsList);
		Collections.reverse(recents);
		return recents;
	}
	
	// returns waiting in order: most recent first
	public List<Tweet>waitingTweets() {
		List<Tweet> tweets = new ArrayList<Tweet>(waitingQueue);
		Collections.reverse(tweets);
		return tweets;
	}
	
	private void queueTweet(final Tweet tweet) {
		tweet.setProgressStatus(TweetProgressStatus.Queued);
		waitingQueue.add(tweet);
		schueduleNextQueueProcessing();
		notifyTweetStateChange();
	}
	
	private synchronized void processWaitingTweets() {
		cleanupCompletedList();
		attemptCancelOfUndos();
		while (!waitingQueue.isEmpty() && isTimeToSend(waitingQueue.peek())) {
			Tweet tweet = waitingQueue.poll();  // get and remove from queue
			processWaitingTweet(tweet);
		}
		schueduleNextQueueProcessing();
	}
	
	private void processWaitingTweet(Tweet tweet) {
		if (!tweet.isCancelled()) {
			recentsList.add(tweet);
			if (shouldSkipTweet(tweet)) {
				tweet.setProgressStatus(TweetProgressStatus.Skipped);
			} else {
				tweet.setProgressStatus(TweetProgressStatus.Sending);
				sendTweet(tweet);
				tweet.setProgressStatus(TweetProgressStatus.Sent);
			}
			notifyTweetStateChange();
		}
	}
	
	private void schueduleNextQueueProcessing() {
		long pauseTimeUntilNextProcessing = TWEET_QUEUE_PROCESSING_DELAY_MS;
		Tweet futureTweet = waitingQueue.peek();
		if (futureTweet != null) {
			pauseTimeUntilNextProcessing = (futureTweet.getTime() + TWEET_SEND_DELAY_MS) - System.currentTimeMillis();
			pauseTimeUntilNextProcessing = Math.max(pauseTimeUntilNextProcessing, 1);
		}
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				processWaitingTweets();
			}
		}, pauseTimeUntilNextProcessing);
			
	}
	
	private boolean isTimeToSend(Tweet tweet) {
		return tweet.getTime() < (System.currentTimeMillis() - TWEET_SEND_DELAY_MS);
	}
	
	// runs on background looper thread
	private void sendTweet(Tweet tweet) {
		TwitterClient.current().tweet(tweet);
		UltimateLogger.logError("Tweet sent.  Status is " + tweet.getSendStatus() + " tweet is " + tweet.getText());
	}
	
	// if an undo arrives before the original event is sent to Twitter  then cancel them both
	private void attemptCancelOfUndos() {
		for (Tweet waitingTweet : waitingQueue) {
			if (waitingTweet.isUndo()) {
				for (Tweet otherTweet : waitingQueue) {
					if (waitingTweet.isUndoOfTweet(otherTweet)) {
						waitingTweet.setProgressStatus(TweetProgressStatus.Cancelled);
						otherTweet.setProgressStatus(TweetProgressStatus.Cancelled);
					}
				}
			}
		}
	}
	
	private boolean shouldSkipTweet(Tweet tweet) {
		// TODO...finish this 
		// (rate limiting logic)
		return false;
	}

	public void setTweetListener(TweetListener tweetListener) {
		synchronized (tweetListenerMutex) {
			this.tweetListener = tweetListener;
		}
	}
	
	private void cleanupCompletedList() {
		if (recentsList.size() > TWEET_LOG_SIZE) {
			recentsList = recentsList.subList(recentsList.size() - TWEET_LOG_SIZE, recentsList.size());
		}
	}
	
	private void notifyTweetStateChange() {
		synchronized (tweetListenerMutex) {
			if (tweetListener != null) {
				UltimateApplication.current().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						tweetListener.tweetStateChanged();
					}
				});
			}
		}
	}

	// TweetListener wil be called back on the main thread
	public static interface TweetListener {
		public void tweetStateChanged();
	}

	
}
