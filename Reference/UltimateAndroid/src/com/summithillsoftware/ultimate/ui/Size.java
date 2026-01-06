package com.summithillsoftware.ultimate.ui;

public class Size {
	public int width;
	public int height;
	
	public Size(int width, int height) {
		super();
		this.width = width;
		this.height = height;
	}

	@Override
	public String toString() {
		return "Size [width=" + width + ", height=" + height + "]";
	}
}
