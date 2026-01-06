package com.summithillsoftware.ultimate.util;

import java.util.Calendar;
import java.util.Date;

public class DateUtil {
	
	public static final boolean isToday(Date date) {
		Calendar today = Calendar.getInstance(); 
		Calendar other = Calendar.getInstance();
		other.setTime(date); 
		return (today.get(Calendar.YEAR) == other.get(Calendar.YEAR)
		  && today.get(Calendar.DAY_OF_YEAR) == other.get(Calendar.DAY_OF_YEAR));
	}

}
