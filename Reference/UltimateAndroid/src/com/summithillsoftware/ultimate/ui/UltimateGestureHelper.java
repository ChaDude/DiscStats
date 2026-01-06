package com.summithillsoftware.ultimate.ui;

import java.util.ArrayList;

import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.Prediction;

import com.summithillsoftware.ultimate.R;
import com.summithillsoftware.ultimate.UltimateApplication;
import com.summithillsoftware.ultimate.util.UltimateLogger;

public class UltimateGestureHelper {
	public static String SWIPE_RIGHT = "swiperight";
	public static String SWIPE_LEFT = "swipeleft";	
	public static String SWIPE_UP = "swipeup";
	public static String SWIPE_DOWN = "swipedown";		
	
	private static UltimateGestureHelper Current;
	
	GestureLibrary gestureLibrary;

	static {
		Current = new UltimateGestureHelper();
		if (Current == null) {
			Current = new UltimateGestureHelper();
		}
	}
	
	public static UltimateGestureHelper current() {
		return Current;
	}
	
	public boolean isSwipeRight(Gesture gesture) {
		return isHorizontalSwipe(gesture, true);
	}
	
	public boolean isSwipeLeft(Gesture gesture) {
		return isHorizontalSwipe(gesture, false);
	}
	
	public boolean isSwipeUp(Gesture gesture) {
		return isVerticalSwipe(gesture, true);
	}
	
	public boolean isSwipeDown(Gesture gesture) {
		return isVerticalSwipe(gesture, false);
	}
	
	private boolean isHorizontalSwipe(Gesture gesture, boolean isRight) {
		ArrayList<Prediction> predictions = getGestureLibrary().recognize(gesture);
		if (predictions.size() > 0) {
			Prediction topPrediction = predictions.get(0);
			return topPrediction.name.equals(isRight ? SWIPE_RIGHT : SWIPE_LEFT) && topPrediction.score > 1.0;
	    } 
		return false;
	}
	
	private boolean isVerticalSwipe(Gesture gesture, boolean isUp) {
		ArrayList<Prediction> predictions = getGestureLibrary().recognize(gesture);
		if (predictions.size() > 0) {
			Prediction topPrediction = predictions.get(0);
			return topPrediction.name.equals(isUp ? SWIPE_UP : SWIPE_DOWN) && topPrediction.score > 1.0;
	    } 
		return false;
	}	
	
	private GestureLibrary getGestureLibrary() {
		if (gestureLibrary == null) {
			gestureLibrary = GestureLibraries.fromRawResource(UltimateApplication.current(), R.raw.gestures);
	        if (!gestureLibrary.load()) {
	        	UltimateLogger.logError( "Failed to load gestures");
	         } 
		}
		return gestureLibrary;
	}

}
