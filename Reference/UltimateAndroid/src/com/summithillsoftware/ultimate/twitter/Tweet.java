package com.summithillsoftware.ultimate.twitter;

import twitter4j.RateLimitStatus;

import com.summithillsoftware.ultimate.model.Event;

public class Tweet {
	private String text;
	private long time;
	private TweetProgressStatus progressStatus;
	private TweetSendStatus sendStatus;
	private RateLimitStatus limitStatus;
	private Event event;
	private boolean isUndo;
	private boolean isOptional;
	
	public Tweet() {
		super();
		time = TwitterTimestampGenerator.current().uniqueTimeIntervalSinceReferenceDateMilliseconds();
		progressStatus = TweetProgressStatus.Queued;
		sendStatus = TweetSendStatus.NotStarted;
	}
	
	public Tweet(String text, Event event) {
		this(text);
		this.event = event;
	}
	
	public Tweet(String text) {
		this();
		this.text = text;
	}
	
	public synchronized boolean isAdHoc() {
		return event == null;
	}
	
	public synchronized boolean isUndoOfTweet(Tweet otherTweet) {
		if (isUndo && event != null) {
			return event.equals(otherTweet.event);
		}
		return false;
	}
	
	public synchronized boolean isCancelled() {
		return progressStatus == TweetProgressStatus.Cancelled;
	}
	
	public synchronized boolean isSkipped() {
		return progressStatus == TweetProgressStatus.Skipped;
	}
	
	public synchronized boolean isWaiting() {
		return progressStatus == TweetProgressStatus.Queued;
	}
	
	public synchronized boolean isSending() {
		return progressStatus == TweetProgressStatus.Sending;
	}
	
	public synchronized boolean isSentAndAccepted() {
		return progressStatus == TweetProgressStatus.Sent && sendStatus == TweetSendStatus.OK;
	}
	
	public synchronized boolean hadSendError() {
		return !(sendStatus == TweetSendStatus.OK || sendStatus == TweetSendStatus.NotStarted);
	}
	
	public synchronized String getText() {
		return text;
	}

	public synchronized void setText(String text) {
		this.text = text;
	}

	public synchronized TweetSendStatus getSendStatus() {
		return sendStatus;
	}

	public synchronized void setCompletionStatus(TweetSendStatus sendStatus) {
		this.sendStatus = sendStatus;
	}
	
	public synchronized boolean wasSendSuccessful() {
		return sendStatus == TweetSendStatus.OK;
	}

	public synchronized RateLimitStatus getLimitStatus() {
		return limitStatus;
	}

	public synchronized void setLimitStatus(RateLimitStatus limitStatus) {
		this.limitStatus = limitStatus;
	}
	
	public synchronized long getTime() {
		return time;
	}

	public synchronized Event getEvent() {
		return event;
	}

	public synchronized void setEvent(Event event) {
		this.event = event;
	}
	
	public synchronized long getId() {
		return time;
	}

	@Override
	public synchronized int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (time ^ (time >>> 32));
		return result;
	}

	@Override
	public synchronized boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Tweet other = (Tweet) obj;
		if (time != other.time)
			return false;
		return true;
	}

	public synchronized TweetProgressStatus getProgressStatus() {
		return progressStatus;
	}

	public synchronized void setProgressStatus(TweetProgressStatus progressStatus) {
		this.progressStatus = progressStatus;
	}

	public synchronized boolean isUndo() {
		return isUndo;
	}

	public synchronized void setUndo(boolean isUndo) {
		this.isUndo = isUndo;
	}

	public synchronized boolean isOptional() {
		return isOptional;
	}

	public synchronized void setOptional(boolean isOptional) {
		this.isOptional = isOptional;
	}

}
