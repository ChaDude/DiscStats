package com.summithillsoftware.ultimate.util;

import com.summithillsoftware.ultimate.UltimateApplication;

public class StringUtil {
	private static StringUtil Current;
	
	static {
		Current = new StringUtil();
	}
	
	private StringUtil() {
		super();
	}
	
	public static StringUtil current() {
		return Current;
	}
	
	public final static String getString(int resId) {
		return UltimateApplication.current().getString(resId);
	}
	
	public final static String getString(int resId, Object...formatArgs) {
		return UltimateApplication.current().getString(resId, formatArgs);
	}
	
	public String capitalizeWord(String s) {
		return Character.toUpperCase(s.charAt(0)) + s.substring(1);
	}


}
