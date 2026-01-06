package com.summithillsoftware.ultimate.twitter;

public class TwitterTimestampGenerator {
	private static TwitterTimestampGenerator Current;
	
	private long lastEventTimeIntervalSinceReferenceDateMillis;
	
	static {
		Current = new TwitterTimestampGenerator();
	}
	
	public static TwitterTimestampGenerator current() {
		return Current;
	}
	
	public long uniqueTimeIntervalSinceReferenceDateMilliseconds() {
		long now = System.currentTimeMillis();
		long newTimestamp = Math.max(now, lastEventTimeIntervalSinceReferenceDateMillis);
		if (newTimestamp == lastEventTimeIntervalSinceReferenceDateMillis) {
			newTimestamp++; // 
		}
		lastEventTimeIntervalSinceReferenceDateMillis = newTimestamp;
		return newTimestamp;
	}


}
