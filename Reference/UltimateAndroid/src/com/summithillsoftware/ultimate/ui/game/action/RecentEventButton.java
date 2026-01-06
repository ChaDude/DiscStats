package com.summithillsoftware.ultimate.ui.game.action;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.summithillsoftware.ultimate.R;

public class RecentEventButton extends LinearLayout {

	public RecentEventButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public RecentEventButton(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public RecentEventButton(Context context) {
		super(context);

	}
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		addView(inflater.inflate(R.layout.recent_event_button, null));
	}
	
	public void setEventImage(int resId) {
		((ImageView) findViewById(R.id.imageView)).setImageResource(resId);
	}
	
	public void setEventDescription(String description) {
		((Button) findViewById(R.id.button)).setText(description);
	}
	
	public void setOnClickListener(OnClickListener clickListener) {
		((Button) findViewById(R.id.button)).setOnClickListener(clickListener);
	}

}
