package com.summithillsoftware.ultimate.util;

public class UniqueTimestampGenerator {
	private static UniqueTimestampGenerator Current;
	
	private int lastEventTimeIntervalSinceReferenceDateSeconds;
	
	static {
		Current = new UniqueTimestampGenerator();
	}
	
	public static UniqueTimestampGenerator current() {
		return Current;
	}
	
	public int uniqueTimeIntervalSinceReferenceDateSeconds() {
		int now = nowSeconds();
		int newTimestamp = Math.max(now, lastEventTimeIntervalSinceReferenceDateSeconds);
		if (newTimestamp == lastEventTimeIntervalSinceReferenceDateSeconds) {
			newTimestamp += 2; // increment by 2 so that insertions can be made for special events
		}
		lastEventTimeIntervalSinceReferenceDateSeconds = newTimestamp;
		return newTimestamp;
	}
	
	private int nowSeconds() {
		int now = (int)(System.currentTimeMillis() / 1000.0f);
		return now;
	}


}
