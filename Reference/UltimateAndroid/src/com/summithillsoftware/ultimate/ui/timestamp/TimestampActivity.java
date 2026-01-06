package com.summithillsoftware.ultimate.ui.timestamp;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;

import com.summithillsoftware.ultimate.R;
import com.summithillsoftware.ultimate.model.Game;
import com.summithillsoftware.ultimate.ui.UltimateActivity;

public class TimestampActivity extends UltimateActivity {
	private DatePicker datePicker;
	private TimePicker timePicker;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_timestamp);
		connectWidgets();
		populateView();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.timestamp, menu);
		return true;
	}

	private void connectWidgets() {
		datePicker = (DatePicker)findViewById(R.id.timestampFragment).findViewById(R.id.datePicker);
		timePicker = (TimePicker)findViewById(R.id.timestampFragment).findViewById(R.id.timePicker);
	}
	
	private void populateView() {
		Date startDate = Game.current().getStartDateTime();
		Calendar c = Calendar.getInstance();
		c.setTime(startDate);
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH);
		int day = c.get(Calendar.DAY_OF_MONTH);
		int hour = c.get(Calendar.HOUR_OF_DAY);
		int minute = c.get(Calendar.MINUTE);
 
		datePicker.init(year, month, day, null);
		timePicker.setCurrentHour(hour);
		timePicker.setCurrentMinute(minute);
	}
	
	public void saveClicked(View v) {
		int year = datePicker.getYear();
		int month = datePicker.getMonth();
		int day = datePicker.getDayOfMonth();
		int hour = timePicker.getCurrentHour();
		int minute = timePicker.getCurrentMinute();
		Calendar calendar = new GregorianCalendar (year, month, day, hour, minute);
		Game.current().setStartDateTime(calendar.getTime());
		Game.current().save();
		finish();
	}
	
	public void cancelClicked(View v) {
		finish();
	}
}
