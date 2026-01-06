package com.summithillsoftware.ultimate.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

public class ViewHelper {
	
	public static boolean removeViewFromParent(View view) {
		if (view != null && view.getParent() != null) {
			((ViewGroup)view.getParent()).removeView(view);
			return true;
		}
		return false;
	}
	
	public static List<View> allChildViews(View view) {
		if (view instanceof ViewGroup) {
			List<View> views = new ArrayList<View>();
			for(int i=0; i<((ViewGroup)view).getChildCount(); ++i) {
				views.add(((ViewGroup)view).getChildAt(i));
			}
			return views;
		} else {
			return Collections.emptyList();
		}
	}
	
	public static List<View> allDescendentViews(View view, @SuppressWarnings("rawtypes") Class viewType) {
		List<View> views = new ArrayList<View>();
		addDescendentViews(view, viewType, views);
		return views;
	}
	
	public static void addDescendentViews(View view, @SuppressWarnings("rawtypes") Class viewType, List<View> viewList) {
		if (view.getClass() == viewType) {
			viewList.add(view);
		}
		if (view instanceof ViewGroup) {
			for(int i=0; i<((ViewGroup)view).getChildCount(); ++i) {
				addDescendentViews(((ViewGroup)view).getChildAt(i), viewType, viewList);
			}
		} 
	}
	
	public static int dpAsPixels(int dpValue, Context context) {
		Resources r = context.getResources();
		float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, r.getDisplayMetrics());
		return (int)Math.round(px);
	}
	
	public static Point locationInRootView(View view, View rootView) {
		// find the location of the root view within the window
		Point rootLocation = locationInWindow(rootView);
		// find the location of the view within the window
		Point viewLocation = locationInWindow(view);
		// the view's location within the root is offset by the root's location in the window
		return new Point(viewLocation.x - rootLocation.x, viewLocation.y - rootLocation.y);
	}
	
	public static Point locationInWindow(View view) {
		// find the location of the root view within the window
		int[] viewXAndY = new int[2];
		view.getLocationInWindow(viewXAndY);
		return new Point(viewXAndY[0], viewXAndY[1]);
	}
	
	public static Point locationInRect(Point origin, int width, int height, AnchorPosition position) { 
		switch (position) {
		case TopLeft:
			return new Point(origin);
		case TopMid:
			return new Point(origin.x + (width /2), origin.y);
		case TopRight:
			return new Point(origin.x + width, origin.y);		
		case RightMid:
			return new Point(origin.x + width, origin.y + (height /2));	
		case BottomRight:
			return new Point(origin.x + width, origin.y + height);	
		case BottomMid:
			return new Point(origin.x + (width /2), origin.y + height);	
		case BottomLeft:
			return new Point(origin.x, origin.y + height);
		case LeftMid:
			return new Point(origin.x, origin.y + (height /2));	
		case Middle:
			return new Point(origin.x + (width /2), origin.y + (height /2));			
		default:
			return new Point(origin);
		}
	}
	
	public static Point locationInRect(Point origin, Size size, AnchorPosition position) {
		return locationInRect(origin, size.width, size.height, position);
	}
	
	public static enum AnchorPosition {
		TopLeft,
		TopMid,
		TopRight,
		RightMid,
		BottomRight,
		BottomMid,
		BottomLeft,
		LeftMid,
		Middle
	}

}
