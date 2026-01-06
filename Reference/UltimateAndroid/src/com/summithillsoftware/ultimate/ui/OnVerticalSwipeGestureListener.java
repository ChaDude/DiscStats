package com.summithillsoftware.ultimate.ui;

import android.view.MotionEvent;

public abstract class OnVerticalSwipeGestureListener extends DefaultOnGestureListener {
	private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    
	@Override
	public boolean onDown(MotionEvent e) {
		// return true so that we become a condidate for onFling
		return true;  
	}
	
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        try {
            // swiping top to bottom
            if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
            	onVerticalSwipe(false);
                return true;
            // swiping bottom to top                
            } else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
            	onVerticalSwipe(true);
            	return true;
            }
        } catch (Exception e) {
            // no-op
        }
        return false;
    }
    
    public abstract void onVerticalSwipe(boolean isTopToBottom);


}
