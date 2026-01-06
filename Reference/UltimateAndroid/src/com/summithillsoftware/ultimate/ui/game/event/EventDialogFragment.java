package com.summithillsoftware.ultimate.ui.game.event;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.summithillsoftware.ultimate.R;
import com.summithillsoftware.ultimate.model.Action;
import com.summithillsoftware.ultimate.model.DefenseEvent;
import com.summithillsoftware.ultimate.model.Event;
import com.summithillsoftware.ultimate.model.Game;
import com.summithillsoftware.ultimate.model.OffenseEvent;
import com.summithillsoftware.ultimate.model.Player;
import com.summithillsoftware.ultimate.ui.Refreshable;
import com.summithillsoftware.ultimate.ui.UltimateActivity;
import com.summithillsoftware.ultimate.ui.UltimateDialogFragment;
import com.summithillsoftware.ultimate.util.UltimateLogger;

public class EventDialogFragment extends UltimateDialogFragment {
	
	// widgets
	private TextView eventTypeNotEditableTextView;
	private ImageButton doneButton; 
	private ImageButton cancelButton; 
	private View playersSeparator;
	private EventPlayerSelectionListView playerOneListView; 
	private EventPlayerSelectionListView playerTwoListView;  
	private TextView eventTypeTextView;
	private TextView fromToTextView;
	private RadioGroup radioGroupEventAction;
	private RadioButton radioButtonEventAction1;
	private RadioButton radioButtonEventAction2;
	private RadioButton radioButtonEventAction3;
	private RadioButton radioButtonEventAction4;
	private View hangtimeView;
	private EditText hangtimeTextView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
		View view =  inflater.inflate(R.layout.fragment_event, container, false);
		connectWidgets(view);
		initializeListView(playerOneListView);
		initializeListView(playerTwoListView);		
		clearView();
        registerWidgetListeners();
		return view;
    }
  
    /** The system calls this only when creating the layout in a dialog. */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setCanceledOnTouchOutside(false);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        registerDialogCancelListener(dialog);
        return dialog;
    }
    
	@Override
	public void onStart() {
		super.onStart();
		populateView();
	}
	
	private void connectWidgets(View view) {
		eventTypeNotEditableTextView = (TextView)view.findViewById(R.id.eventTypeNotEditableTextView);
		doneButton = (ImageButton)view.findViewById(R.id.doneButton);
		cancelButton = (ImageButton)view.findViewById(R.id.cancelButton);
		playersSeparator = (View)view.findViewById(R.id.playersSeparator);
		playerOneListView = (EventPlayerSelectionListView)view.findViewById(R.id.playerOneListView);
		playerTwoListView = (EventPlayerSelectionListView)view.findViewById(R.id.playerTwoListView);
		fromToTextView = (TextView)view.findViewById(R.id.fromToTextView);
		eventTypeTextView = (TextView)view.findViewById(R.id.eventTypeTextView);
		radioGroupEventAction = (RadioGroup)view.findViewById(R.id.radioGroupEventAction);
		radioButtonEventAction1 = (RadioButton)view.findViewById(R.id.radioButtonEventAction1);
		radioButtonEventAction2 = (RadioButton)view.findViewById(R.id.radioButtonEventAction2);
		radioButtonEventAction3 = (RadioButton)view.findViewById(R.id.radioButtonEventAction3);
		radioButtonEventAction4 = (RadioButton)view.findViewById(R.id.radioButtonEventAction4);
		hangtimeView = (View)view.findViewById(R.id.hangtimeView);
		hangtimeTextView = (EditText)view.findViewById(R.id.hangtimeTextView);
	}
	
    private void clearView() {
    	eventTypeNotEditableTextView.setVisibility(View.GONE);
    	playerOneListView.setVisibility(View.GONE);
    	playerTwoListView.setVisibility(View.GONE);
    	fromToTextView.setVisibility(View.GONE);
		radioGroupEventAction.setVisibility(View.GONE);
		radioButtonEventAction1.setVisibility(View.GONE);
		radioButtonEventAction2.setVisibility(View.GONE);
		radioButtonEventAction3.setVisibility(View.GONE);
		radioButtonEventAction4.setVisibility(View.GONE);   
		hangtimeView.setVisibility(View.GONE);
    }
	
    private void populateView() {
    	configureForEventType(replacementEvent());
    	updatePlayerSelections();
    }
    
    private void updatePlayerSelections() {
		if (replacementEvent().isOffense()) {
			playerOneListView.setSelectedPlayer(((OffenseEvent)replacementEvent()).getPasser());
			playerTwoListView.setSelectedPlayer(((OffenseEvent)replacementEvent()).getReceiver());
		} else if (replacementEvent().isDefense()) {
			playerOneListView.setSelectedPlayer(((DefenseEvent)replacementEvent()).getDefender());
		}
    }
    
	private void initializeListView(ListView listView) {
		EventPlayerSelectionListAdapter adaptor = new EventPlayerSelectionListAdapter(this.getActivity());
		listView.setAdapter(adaptor);
	}
 
	private void registerWidgetListeners() {
		doneButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	if (validateUpdates()) {
	            	updateEvent();
	        		notifyEventChange();
	            	dismissDialog();
            	}
            }
        });
		cancelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	dismissDialog();
            }
        });
		radioGroupEventAction.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup radioGroup, int paramInt) {
				handleEventTypeChange(radioGroup);
			}
		});
		playerOneListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (playerOneListView.getSelectedPlayer(position) == null) { // "show all" button selected
	            	playerOneListView.showAllPlayers();
	            	updatePlayerSelections();
				} else {
					handlePlayerOneSelection(parent, view, position, id);
				}
			}
		});	
		playerTwoListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (playerTwoListView.getSelectedPlayer(position) == null) { // "show all" button selected
					playerTwoListView.showAllPlayers();
	            	updatePlayerSelections();
				} else {
					handlePlayerTwoSelection(parent, view, position, id);
				}
				
			}
		});				
	
	}
	
	private void handleEventTypeChange(RadioGroup radioGroup) {
		try {
			switch (radioGroup.getCheckedRadioButtonId()) {
			case R.id.radioButtonEventAction1:
				replacementEvent().setAction((Action)radioButtonEventAction1.getTag());
				break;
			case R.id.radioButtonEventAction2:
				replacementEvent().setAction((Action)radioButtonEventAction2.getTag());
				break;
			case R.id.radioButtonEventAction3:
				replacementEvent().setAction((Action)radioButtonEventAction3.getTag());
				break;
			case R.id.radioButtonEventAction4:
				replacementEvent().setAction((Action)radioButtonEventAction4.getTag());
				break;
			default:
				break;
			}
			configureForEventType(replacementEvent());
		} catch (Exception e) {
			dismiss();
		}
	}
	
	private void handlePlayerOneSelection(AdapterView<?> parent, View view, int position, long id) {
		Player selectedPlayer = playerOneListView.getSelectedPlayer(position);
		if (replacementEvent().isOffense()) {
			((OffenseEvent)replacementEvent()).setPasser(selectedPlayer);
		} else if (replacementEvent().isDefense()) {
			((DefenseEvent)replacementEvent()).setDefender(selectedPlayer);
		}
		playerOneListView.setSelectedPlayer(selectedPlayer);		
		// don't allow same person to be selected in both lists (with the exception of anonymous)
		if (!selectedPlayer.isAnonymous() && playerTwoListView.getVisibility() == View.VISIBLE && replacementEvent().isOffense()) {
			if (((OffenseEvent)replacementEvent()).getReceiver().equals(selectedPlayer)) {
				((OffenseEvent)replacementEvent()).setReceiver(Player.anonymous());
				playerTwoListView.setSelectedPlayer(((OffenseEvent)replacementEvent()).getReceiver());
			} 
		}
	}
	
	private void handlePlayerTwoSelection(AdapterView<?> parent, View view, int position, long id) {
		Player selectedPlayer = playerTwoListView.getSelectedPlayer(position);
		if (replacementEvent().isOffense()) {
			((OffenseEvent)replacementEvent()).setReceiver(selectedPlayer);
		} 	
		playerTwoListView.setSelectedPlayer(selectedPlayer);
		// don't allow same person to be selected in both lists (with the exception of anonymous)
		if (!selectedPlayer.isAnonymous() && replacementEvent().isOffense()) {
			if (((OffenseEvent)replacementEvent()).getPasser().equals(selectedPlayer)) {
				((OffenseEvent)replacementEvent()).setPasser(Player.anonymous());
				playerOneListView.setSelectedPlayer(((OffenseEvent)replacementEvent()).getPasser());
			} 
		}
	}
	
	private boolean validateUpdates() {
		if (replacementEvent().isPullIb()) {
			if (hangtimeMillisecondsFromTextView() > 60000) {
				alertInvalidHangtime();
				return false;
			}
		}
		return true;
	}
	
	private void updateEvent() {
		originalEvent().setAction(replacementEvent().getAction());
		if (originalEvent().isOffense()) {
			((OffenseEvent)originalEvent()).setPasser(((OffenseEvent)replacementEvent()).getPasser());
			((OffenseEvent)originalEvent()).setReceiver(((OffenseEvent)replacementEvent()).getReceiver());
		} else if (originalEvent().isDefense()) {
			((DefenseEvent)originalEvent()).setDefender(((DefenseEvent)replacementEvent()).getDefender());
			if (replacementEvent().isPullIb()) {
				((DefenseEvent)originalEvent()).setPullHangtimeMilliseconds(hangtimeMillisecondsFromTextView());
			}
		}
		Game.current().save();
	}
	
	private void notifyEventChange() {
    	((Refreshable)getActivity()).refresh();
	}
	
	private void registerDialogCancelListener(Dialog dialog) {
		dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface arg0) {
				// Nothing yet...just coded in case we want to do something here later
			}
		});
	}
	
	private void dismissDialog() {
		// make sure no crash
		try {
			dismiss();
		} catch (Exception e) {
			UltimateLogger.logWarning( "Error dismissing dialog", e);
		}
	}
	
	private void configureForEventType(Event event) {
		if (event.isOffense()) {
			switch (event.getAction()) {
			case Catch:
				eventTypeTextView.setText(R.string.label_event_category_catch);
				configurePlayerListsVisibility(true, true, false);
				break;
			case Goal:
				eventTypeTextView.setText(R.string.label_event_category_our_goal);
				configurePlayerListsVisibility(true, true, false);
				break;				
			case Throwaway:
				eventTypeTextView.setText(R.string.label_event_category_our_turnover);
				configurePlayerListsVisibility(true, false, true);
				configureEventTypeRadioGroupForOffenseTurnover(Action.Throwaway);
				break;	
			case Drop:
				eventTypeTextView.setText(R.string.label_event_category_our_turnover);
				configurePlayerListsVisibility(true, true, true);
				configureEventTypeRadioGroupForOffenseTurnover(Action.Drop);
				break;	
			case Stall:
				eventTypeTextView.setText(R.string.label_event_category_our_turnover);
				configurePlayerListsVisibility(true, false, true);
				configureEventTypeRadioGroupForOffenseTurnover(Action.Stall);
				break;	
			case MiscPenalty:
				eventTypeTextView.setText(R.string.label_event_category_our_turnover);
				configurePlayerListsVisibility(true, false, true);
				configureEventTypeRadioGroupForOffenseTurnover(Action.MiscPenalty);
				break;		
			case Callahan:
				eventTypeTextView.setText(R.string.label_event_category_callahaned);
				configurePlayerListsVisibility(true, false, false);
				break;				
			default:
				break;
			}
		} else if (event.isDefense()) {
			switch (event.getAction()) {
			case Pull:
				eventTypeTextView.setText(R.string.label_event_category_pull);
				configurePlayerListsVisibility(true, false, true);
				configureEventTypeRadioGroupForPull(Action.Pull);
				showPullHangtimeEntryField(true);
				break;
			case PullOb:
				eventTypeTextView.setText(R.string.label_event_category_pull);
				configurePlayerListsVisibility(true, false, true);
				configureEventTypeRadioGroupForPull(Action.PullOb);
				showPullHangtimeEntryField(false);
				break;
			case Goal:
				eventTypeTextView.setText(R.string.label_event_category_their_goal);
				configurePlayerListsVisibility(false, false, false);
				showEventTypeNotEditable();
				break;		
			case De:
				eventTypeTextView.setText(R.string.label_event_category_their_turnover);
				configurePlayerListsVisibility(true, false, true);
				configureEventTypeRadioGroupForDefenseTurnover(Action.De);
				break;		
			case Throwaway:
				eventTypeTextView.setText(R.string.label_event_category_their_turnover);
				configureEventTypeRadioGroupForDefenseTurnover(Action.Throwaway);
				configurePlayerListsVisibility(false, false, false);
				break;			
			case Callahan:
				eventTypeTextView.setText(R.string.label_event_category_callahan);
				configurePlayerListsVisibility(true, false, false);
				break;					
			default:
				break;
			}
		} else if (event.isCessationEvent()) {
			showEventTypeNotEditable();
			configurePlayerListsVisibility(false, false, false);
			switch (event.getAction()) {
			case EndOfFirstQuarter:
				eventTypeTextView.setText(R.string.label_event_category_end_1st_qtr);
				break;
			case Halftime:
				eventTypeTextView.setText(R.string.label_event_category_haltime);
				break;
			case EndOfThirdQuarter:
				eventTypeTextView.setText(R.string.label_event_category_end_3rd_qtr);
				break;
			case EndOfFourthQuarter:
				eventTypeTextView.setText(R.string.label_event_category_end_4th_qtr);
				break;
			case EndOfOvertime:
				eventTypeTextView.setText(R.string.label_event_category_end_overtime);
				break;
			case GameOver:
				eventTypeTextView.setText(R.string.label_event_category_gameover);
				break;				
			default:
				break;
			}
		}
	}
	
	private void configurePlayerListsVisibility(boolean showPlayerOneList, boolean showPlayerTwoList, boolean showSeparator) {
		playersSeparator.setVisibility(showSeparator ? View.VISIBLE : View.GONE);
		playerOneListView.setVisibility(showPlayerOneList ? View.VISIBLE : View.GONE);
		fromToTextView.setVisibility(showPlayerTwoList ? View.VISIBLE : View.GONE);
		playerTwoListView.setVisibility(showPlayerTwoList ? View.VISIBLE : View.GONE);
	}
	
	private void configureEventTypeRadioGroupForOffenseTurnover(Action selectedAction) {
		radioGroupEventAction.setVisibility(View.VISIBLE);
		showEventTypeRadioButton(radioButtonEventAction1, Action.Drop, R.string.label_event_type_drop, selectedAction);
		showEventTypeRadioButton(radioButtonEventAction2, Action.Throwaway, R.string.label_event_type_throwaway, selectedAction);
		showEventTypeRadioButton(radioButtonEventAction3, Action.MiscPenalty, R.string.label_event_type_misc_penalty, selectedAction);
		showEventTypeRadioButton(radioButtonEventAction4, Action.Stall, R.string.label_event_type_stall, selectedAction);
	}
	
	private void configureEventTypeRadioGroupForPull(Action selectedAction) {
		radioGroupEventAction.setVisibility(View.VISIBLE);		
		showEventTypeRadioButton(radioButtonEventAction1, Action.Pull, R.string.label_event_type_pull, selectedAction);
		showEventTypeRadioButton(radioButtonEventAction2, Action.PullOb, R.string.label_event_type_pullob, selectedAction);
	}
	
	private void configureEventTypeRadioGroupForDefenseTurnover(Action selectedAction) {
		radioGroupEventAction.setVisibility(View.VISIBLE);		
		showEventTypeRadioButton(radioButtonEventAction1, Action.De, R.string.label_event_type_d, selectedAction);
		showEventTypeRadioButton(radioButtonEventAction2, Action.Throwaway, R.string.label_event_type_throwaway, selectedAction);
	}	

	private void showEventTypeRadioButton(RadioButton button, Action action, int textId, Action selectedAction) {
		button.setVisibility(View.VISIBLE);
		button.setText(textId);
		button.setTag(action);
		button.setChecked(action == selectedAction);
	}
	
	private void showPullHangtimeEntryField(boolean show) {
		hangtimeView.setVisibility(show ? View.VISIBLE : View.GONE);
		if (show && replacementEvent().isDefense()) {
			hangtimeTextView.setText(((DefenseEvent)replacementEvent()).getFormattedPullHangtimeSeconds());
		}
	}
	
	private void showEventTypeNotEditable() {
		eventTypeNotEditableTextView.setVisibility(View.VISIBLE);
	}
		
	private int hangtimeMillisecondsFromTextView() {
		int hangtimeMs = 0;
		try {
			String hangtimeString = hangtimeTextView.getText() == null ? "" : hangtimeTextView.getText().toString().trim();
			if (!hangtimeString.isEmpty()) {
				float hangtime = Float.parseFloat(hangtimeTextView.getText().toString());
				hangtimeMs = (int)(hangtime * 1000f);
				if (hangtimeMs < 0) {
					hangtimeMs = 0;
				}
			}
		} catch (Exception e) {
			UltimateLogger.logError( "Unable to format hangtime", e);
		}
		return hangtimeMs;	
	}
	
	private Game game() {
		return Game.current();
	}
	
	private Event originalEvent() {
		return game().getSelectedEvent().getEvent();
	}
	
	private Event replacementEvent() {
		return game().getSelectedEvent().getReplacementEvent();
	}
	
	private void alertInvalidHangtime() {
		((UltimateActivity)getActivity()).displayErrorMessage(
				getString(R.string.alert_event_invalid_hangtime_title),
				getString(R.string.alert_event_invalid_hangtime_message));
	}
}
