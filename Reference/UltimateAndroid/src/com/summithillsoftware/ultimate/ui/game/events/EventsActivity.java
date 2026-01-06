package com.summithillsoftware.ultimate.ui.game.events;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.summithillsoftware.ultimate.R;
import com.summithillsoftware.ultimate.model.EventHolder;
import com.summithillsoftware.ultimate.model.Game;
import com.summithillsoftware.ultimate.ui.Refreshable;
import com.summithillsoftware.ultimate.ui.UltimateActivity;
import com.summithillsoftware.ultimate.ui.game.event.EventDialogFragment;

public class EventsActivity extends UltimateActivity implements Refreshable {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_events);
		// Show the Up button in the action bar.
		setupActionBar();
		updateTitle();
		registerListeners();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.events, menu);
		return true;
	}
	
	private void registerListeners() {
		getEventsListView().setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				EventHolder selectedEvent = ((EventsListAdapter)getEventsListView().getAdapter()).getSelectedEvent(position);
				if (selectedEvent != null) {
					Game.current().setSelectedEvent(selectedEvent);
					showEventDialog();
				}
			}

		});
	}

	private void updateTitle() {
		String title = getString(R.string.title_activity_events);
		setTitle(title + " (" + getString(R.string.common_versus_short) + " " + game().getOpponentName() + " )");
	}
	
	private void showEventDialog() {
	    FragmentManager fragmentManager = getSupportFragmentManager();
	    EventDialogFragment eventDialog = new EventDialogFragment();
	    eventDialog.show(fragmentManager, "dialog");
	}
	
	public void refresh() {
		((EventsListAdapter)getEventsListView().getAdapter()).resetPointsAndEvents();
	}
	
	private ListView getEventsListView() {
		return (ListView)findViewById(R.id.eventsFragment).findViewById(R.id.listview_events);
	}
	
	private Game game() {
		return Game.current();
	}
}
