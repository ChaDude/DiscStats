package com.summithillsoftware.ultimate.ui.game.events;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Space;
import android.widget.TextView;

import com.summithillsoftware.ultimate.R;
import com.summithillsoftware.ultimate.UltimateApplication;
import com.summithillsoftware.ultimate.model.Event;
import com.summithillsoftware.ultimate.model.EventHolder;
import com.summithillsoftware.ultimate.model.Game;
import com.summithillsoftware.ultimate.model.Player;
import com.summithillsoftware.ultimate.model.PlayerSubstitution;
import com.summithillsoftware.ultimate.model.Point;
import com.summithillsoftware.ultimate.model.Score;

public class EventsListAdapter extends BaseAdapter {
	private static final Integer EVENT_ROW = 0;
	private static final Integer POINT_ROW = 1;	
	private List<Object> pointsAndEvents;
	private Context context;

	public EventsListAdapter(Context context) {
		super();
		this.context = context;
		resetPointsAndEvents();
	}
	
	public void resetPointsAndEvents() {
		pointsAndEvents = null;
		notifyDataSetChanged();
	}
	
	private List<Object>getSortedPointsAndEvents() {
		if (pointsAndEvents == null) {
			pointsAndEvents = new ArrayList<Object>();
			for (Point point : game().getPointInMostRecentOrder()) {
				pointsAndEvents.add(point);
				for (Event event : point.getEventsInMostRecentOrder()) {
					pointsAndEvents.add(event);
				}
			}
		}
		return pointsAndEvents;
	}

	@Override
	public int getCount() {
		return getSortedPointsAndEvents().size();
	}

	@Override
	public Object getItem(int position) {
		return getSortedPointsAndEvents().get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int index, View reusableRowView, ViewGroup parent) {
		View rowView = reusableRowView;
		Object eventOrPoint = getSortedPointsAndEvents().get(index);
		boolean isEvent = eventOrPoint instanceof Event;
		if (reusableRowView == null || reusableRowView.getTag().equals(EVENT_ROW) != isEvent) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			rowView = inflater.inflate(isEvent ? R.layout.rowlayout_events_event : R.layout.rowlayout_events_point, null);
			rowView.setTag(isEvent ? EVENT_ROW : POINT_ROW);
			rowView.setEnabled(isEvent);
		}
	
		if (isEvent) {
			Event event = (Event)eventOrPoint;
			
			TextView eventDescriptionTextView = (TextView)rowView.findViewById(R.id.text_event_description);
			eventDescriptionTextView.setText(event.toString());
			ImageView imageView = (ImageView)rowView.findViewById(R.id.image_event);
			imageView.setImageResource(event.image());
			Space indentSpace = (Space)rowView.findViewById(R.id.space_defense_indent);		
			indentSpace.setVisibility(event.isOffense() ? View.GONE : View.VISIBLE);
		} else {
			Point point = (Point)eventOrPoint;
			
			TextView scoreDescriptionTextView = (TextView)rowView.findViewById(R.id.text_score_description);
			scoreDescriptionTextView.setText(scoreDescription(point, index));
			Score score = point.getSummary().getScore();
			int scoreColor = context.getResources().getColor(score.isTie() ? R.color.Black : (score.isOurLead() ? R.color.Green : R.color.Red));
			scoreDescriptionTextView.setTextColor(scoreColor);
			TextView lineDescriptionTextView = (TextView)rowView.findViewById(R.id.text_line_description);
			lineDescriptionTextView.setText(pointDescription(point));
		}
	
		return rowView;
	}
	
	public EventHolder getSelectedEvent(int position) {
		Object eventOrPoint = getItem(position);
		if (eventOrPoint instanceof Event) {
			EventHolder holder = new EventHolder((Event)eventOrPoint);
			for (int i = position - 1; i >= 0; i--) {
				eventOrPoint = getItem(i);
				if (eventOrPoint instanceof Point) {
					holder.setPoint((Point)eventOrPoint);
					holder.setFirstPoint(i == 0);
					break;
				}
			}
			if (holder.validate()) {
				return holder;
			}
		}
		return null;
	}
	
	private Game game() {
		return Game.current();
	}
	
	private String scoreDescription(Point point, int index) {
		if (index == 0 && !(point.isFinished())) {
			return UltimateApplication.current().getString(R.string.point_description_current_point);
		} else {
			return point.getSummary().getScore().format(context, true);
		}
	}
	
	private String pointDescription(Point point) {
		if (point.getLine().isEmpty()) {
			return "";
		} else {
	        if (game().isTimeBasedGame() && point.isOnlyPeriodEnd()) {
	        	return "";
	        } else {
	        	String lineType = UltimateApplication.current().getString(point.getSummary().isOline() ? 
	        			R.string.common_o_line : R.string.common_d_line);
	            return lineType + ": " + lineDescription(point);
	        }
	    }
	}
	
	private String lineDescription(Point point) {
		Set<String> names = new HashSet<String>();
		for (Player player : point.getPlayers()) {
			names.add(player.getName());
		}
		for (PlayerSubstitution substitution : point.getSubstitutions()) {
			if (names.contains(substitution.getFromPlayer().getName())) {
				names.remove(substitution.getFromPlayer().getName());
				names.add(UltimateApplication.current().getString(R.string.line_player_partial, substitution.getFromPlayer().getName()));
			}
			if (names.contains(substitution.getToPlayer().getName())) {
				names.remove(substitution.getToPlayer().getName());
				names.add(UltimateApplication.current().getString(R.string.line_player_partial, substitution.getToPlayer().getName()));
			}			
		}
		List<String> sortedList = new ArrayList<String>(names);
		Collections.sort(sortedList);
		return TextUtils.join(", ", sortedList);
	}
}
