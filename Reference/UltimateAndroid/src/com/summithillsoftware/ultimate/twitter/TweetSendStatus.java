package com.summithillsoftware.ultimate.twitter;

public enum TweetSendStatus {
	OK,
	RejectedRetweet,
	RejectedRateLimitExceeded,
	RejectedForUnkownReason,
	RejectedBadCredentials,
	CommunicationsError,
	UnknownError,
	NotStarted
}
