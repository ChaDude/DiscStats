package com.summithillsoftware.ultimate.ui;

import android.graphics.Point;
import android.graphics.Rect;
import android.view.View;

public class GraphicsUtil {
	
	/* 
	 * Given a rectangle, compute the point at which a line (angle specified by degrees)
	 * drawn from the center of the rectangle intersects the rectangle.  0 degrees is north.
	 * Inset the point by the radialInset value.  "radialInset" means the point is moved 
	 * this amount closer to the center point of the rectangle.
	 */
	public final static Point pointOnRectAtAngleWithInset(Rect rect, int degrees, int radialInset) {
		int distanceToIntersect = distanceFromCenterToIntersect(rect, degrees);
		Point rectMid = new Point((int)rect.exactCenterX(), (int)rect.exactCenterY());
		Point point = calcPointOnCirle(rectMid, distanceToIntersect - radialInset, degrees);
		return point;
	}
	
	/* 
	 * Given a rectangle, compute the distance from the center of the rectangle to the side that
	 * a line, starting at the center point and continuing at angle degrees, will intersect it.
	 */
	public final static int distanceFromCenterToIntersect(Rect rect, int degrees) {
		Point intersectOnRect = pointOnRectAtAngle(rect, degrees);
		Point rectMid = new Point((int)rect.exactCenterX(), (int)rect.exactCenterY());
		return (int)lengthOfLine(intersectOnRect, rectMid);
	}
	
	/* 
	 * Given a rectangle, compute the point at which a line (angle specified by degrees)
	 * drawn from the center of the rectangle intersects the rectangle.  0 degrees is north.
	 */
	public final static Point pointOnRectAtAngle(Rect rect, int degrees) {
		// 1.) find a distant point that will intersect the rect
		int distantPointLineLength = Math.max(rect.height(), rect.width());  // length is not important as long as it is beyond border of rect
		Point rectMid = new Point((int)rect.exactCenterX(), (int)rect.exactCenterY());
		Point distantPoint = calcPointOnCirle(rectMid, distantPointLineLength, degrees);
		
		// 2.) check each side of the rect until we find the intersection
		
		// our line at angle degrees
		int x1 = rectMid.x;
		int y1 = rectMid.y;
		int x2 = distantPoint.x;
		int y2 = distantPoint.y;
		
		// now compare to each side
		int x3, x4, y3, y4;
		Point horizontalIntersection = null;
		Point verticalIntersection = null;
		
		// horizontal (top and bottom)
		if (distantPoint.y < rectMid.y) {
			// top
			x3 = rect.left;
			y3 = rect.top;
			x4 = rect.right;
			y4 = rect.top;
			horizontalIntersection = intersection(x1,y1,x2,y2, x3, y3, x4,y4);
		} else {
			// bottom
			x3 = rect.left;
			y3 = rect.bottom;
			x4 = rect.right;
			y4 = rect.bottom;
			horizontalIntersection = intersection(x1,y1,x2,y2, x3, y3, x4,y4);
		}
		
		// vertical (right and left)
		if (distantPoint.x > rectMid.x) {
			// right
			x3 = rect.right;
			y3 = rect.top;
			x4 = rect.right;
			y4 = rect.bottom;
			verticalIntersection = intersection(x1,y1,x2,y2, x3, y3, x4,y4);
		} else {
			// left
			x3 = rect.left;
			y3 = rect.top;
			x4 = rect.left;
			y4 = rect.bottom;
			verticalIntersection = intersection(x1,y1,x2,y2, x3, y3, x4,y4);
		}
		
		if (horizontalIntersection == null && verticalIntersection != null) {
			return verticalIntersection;
		} else if (verticalIntersection == null && horizontalIntersection != null) {
			return horizontalIntersection;
		} else if (verticalIntersection != null && horizontalIntersection != null) {
			double distanceToHorizontal = lengthOfLine(horizontalIntersection, rectMid);
			double distanceToVertical = lengthOfLine(verticalIntersection, rectMid);
			return distanceToHorizontal < distanceToVertical ? horizontalIntersection : verticalIntersection;
		} else {
			return null;  // should never happen
		}
	}
	
	public static final double lengthOfLine(Point p1, Point p2) {
		// Pythagorean Theorem
		double side1length = Math.abs(p2.x - p1.x);
		double side2length = Math.abs(p2.y - p1.y);
		double length = Math.sqrt(Math.pow(side1length, 2) + Math.pow(side2length, 2));
		return length;
	}
	
	public static final Rect rectForView(View view) {
		return new Rect((int)view.getX(), (int)view.getY(), (int)view.getX() + view.getWidth(), (int)view.getY() + view.getHeight());
	}
	
	// north = 0 degrees
	public static final Point calcPointOnCirle(Point centerPoint, int radius, int degrees) {
	    double radians = Math.toRadians(degrees - 90);
	    return new Point(centerPoint.x + (int)Math.round(radius * Math.cos(radians)), centerPoint.y + (int)Math.round(radius * Math.sin(radians)));
	}
	
	/**
	   * Computes the intersection between two lines. The calculated point is approximate, 
		 * since integers are used. If you need a more precise result, use doubles
		 * everywhere. 
		 * (c) 2007 Alexander Hristov. Use Freely (LGPL license). http://www.ahristov.com
		 *
	   * @param x1 Point 1 of Line 1
	   * @param y1 Point 1 of Line 1
	   * @param x2 Point 2 of Line 1
	   * @param y2 Point 2 of Line 1
	   * @param x3 Point 1 of Line 2
	   * @param y3 Point 1 of Line 2
	   * @param x4 Point 2 of Line 2
	   * @param y4 Point 2 of Line 2
	   * @return Point where the segments intersect, or null if they don't
	   */
	public static final Point intersection(
		int x1,int y1,int x2,int y2, 
		int x3, int y3, int x4,int y4
		) {
	  int d = (x1-x2)*(y3-y4) - (y1-y2)*(x3-x4);
	  if (d == 0) { 
		  return null;
	  }
	    
	  int xi = ((x3-x4)*(x1*y2-y1*x2)-(x1-x2)*(x3*y4-y3*x4))/d;
	  int yi = ((y3-y4)*(x1*y2-y1*x2)-(y1-y2)*(x3*y4-y3*x4))/d;
	    
	  return new Point(xi,yi);
	}

}
