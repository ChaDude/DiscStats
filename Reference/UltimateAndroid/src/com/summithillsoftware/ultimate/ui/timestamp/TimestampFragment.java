package com.summithillsoftware.ultimate.ui.timestamp;

import java.lang.reflect.Field;
import java.util.List;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;

import com.summithillsoftware.ultimate.R;
import com.summithillsoftware.ultimate.ui.UltimateFragment;
import com.summithillsoftware.ultimate.ui.ViewHelper;
import com.summithillsoftware.ultimate.util.UltimateLogger;

public class TimestampFragment extends UltimateFragment {
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_timestamp, container);
		customizeDividerColors(view);
		return view;
	}
	
	private void customizeDividerColors(View view) {
		customizeDividerColor(view.findViewById(R.id.datePicker));
		customizeDividerColor(view.findViewById(R.id.timePicker));
	}
	
	private void customizeDividerColor(View picker) {
	    try {
			List<View> numberPickers = ViewHelper.allDescendentViews(picker, NumberPicker.class);
			for (View numberPicker : numberPickers) {
				// reflectively find the divider field
				Field[] pickerFields = NumberPicker.class.getDeclaredFields();
				for (Field pf : pickerFields) {
					if (pf.getName().equals("mSelectionDivider")) {
						pf.setAccessible(true);
						pf.set(numberPicker, getResources().getDrawable(R.drawable.picker_horizontal_divider));
					}
				}
			}
		} catch (Exception e) {
			UltimateLogger.logWarning("Unable to style the picker divider color", e);
		} 
	}


}
