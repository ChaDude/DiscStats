package com.summithillsoftware.ultimate.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.LinearLayout;

public class GestureDetectorLinearLayout extends LinearLayout {
	private GestureDetector gestureDetector;

	public GestureDetectorLinearLayout(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}

	public GestureDetectorLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public GestureDetectorLinearLayout(Context context) {
		super(context);
	}

	public GestureDetector getGestureDetector() {
		return gestureDetector;
	}

	public void setGestureDetector(GestureDetector gestureDetector) {
		this.gestureDetector = gestureDetector;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		if (gestureDetector == null) {
			return super.onInterceptTouchEvent(event);
		} else {
			return super.onInterceptTouchEvent(event) && gestureDetector.onTouchEvent(event);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (gestureDetector == null) {
			return super.onTouchEvent(event);
		} else {
			return gestureDetector.onTouchEvent(event);
		}
	}

}
