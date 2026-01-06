package com.summithillsoftware.ultimate.ui.wind;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.summithillsoftware.ultimate.R;
import com.summithillsoftware.ultimate.util.UltimateLogger;

public class WindDirectionView extends View {
	private static final int SWIPE_MIN_DISTANCE = 120;
    
	private int degreesFromNorth = -1;  // -1 indicates that no wind direction has been set yet
	private Bitmap arrowImage;
	private Paint paint;
	private Point mouseDownPoint;
	private OnWindDirectionChangedListener changeListener;
	private String directionInstructions;
	private float textSize;
	private Rect textRect;

	public WindDirectionView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public WindDirectionView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public WindDirectionView(Context context) {
		super(context);
		init();
	}
	
	private void init() {
		arrowImage = BitmapFactory.decodeResource(getResources(), R.drawable.wind_arrow);
		directionInstructions = getResources().getString(R.string.label_wind_direction_of_wind_instructions);
		initPaint();
	}
	
	private void initPaint() {
		paint = new Paint();
		// text properties
		paint.setTextSize(Math.max(textSize, 14));
		paint.setColor(Color.WHITE);
		paint.setAntiAlias(true);
		textRect = new Rect();  // used during draw...instantiated here for performance reasons
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
	    canvas.save();
		if (degreesFromNorth >= 0) {
			float originX = (getWidth() - arrowImage.getWidth()) / 2; 
			float originY = (getHeight() - arrowImage.getHeight()) / 2; 
			float midX = getWidth() / 2; 
			float midY = getHeight() / 2; 
			
	        int rotateDegrees = (degreesFromNorth - 90) % 360;
	        canvas.rotate(rotateDegrees, midX, midY);
	        
			canvas.drawBitmap(arrowImage, originX, originY, paint);
		} else {
			paint.getTextBounds(directionInstructions, 0, directionInstructions.length(), textRect);
			float x = (canvas.getWidth() - textRect.width()) / 2.0f;
			float y = (canvas.getHeight() - textRect.height()) / 2.0f;
			y += (textRect.height() / 2);  // text origin is the bottom of the text
			canvas.drawText(directionInstructions, x, y, paint); 
		}
	    canvas.restore();
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
	    switch(event.getAction()){
	    case MotionEvent.ACTION_DOWN:
	    	UltimateLogger.logInfo("event...down");
	    	mouseDownPoint = new Point((int)event.getX(), (int)event.getY());
	        break;
	    case MotionEvent.ACTION_UP:
	    	UltimateLogger.logInfo("event...up");
	    	Point mouseUpPoint = new Point((int)event.getX(), (int)event.getY());
			double flingDistance = distanceBetween(mouseDownPoint.x, mouseDownPoint.y, mouseUpPoint.x, mouseUpPoint.y);
            if (flingDistance > SWIPE_MIN_DISTANCE) {
            	degreesFromNorth = calculateDegrees(mouseDownPoint.x, mouseDownPoint.y, mouseUpPoint.x, mouseUpPoint.y);
            	if (changeListener != null) {
            		changeListener.onWindDirectionChanged(degreesFromNorth);
            	}
            	UltimateLogger.logInfo("fling happened.  degrees = " + degreesFromNorth);
            	invalidate();
            } else {
            	UltimateLogger.logInfo("fling not long enough");
            }
	        break;
	    }
	    return true;
	}

	
    private double distanceBetween(float x1, float y1, float x2, float y2) {
    	double aSquared = Math.pow(Math.abs(x2 - x1), 2.0f);
    	double bSquared = Math.pow(Math.abs(y2 - y1), 2.0f);
    	return Math.sqrt(aSquared + bSquared);
    }
    
    private int calculateDegrees(float x1, float y1, float x2, float y2) {
    	float angle = (float) Math.toDegrees(Math.atan2(-(x2 - x1), y2 -  y1));
    	return (int)angle + 180;
    }

	public int getDegreesFromNorth() {
		return degreesFromNorth;
	}

	public void setDegreesFromNorth(int degreesFromNorth) {
		this.degreesFromNorth = degreesFromNorth;
		invalidate();
	}
	
	public void setTextSize(float textSize) {
		this.textSize = textSize;
		initPaint();
		invalidate();
	}
	
	public void setChangeListener(OnWindDirectionChangedListener changeListener) {
		this.changeListener = changeListener;
	}
	
	/* listener interface */
	
	public interface OnWindDirectionChangedListener {
		public void onWindDirectionChanged(int degreesFromNorth);
	}


	

}
