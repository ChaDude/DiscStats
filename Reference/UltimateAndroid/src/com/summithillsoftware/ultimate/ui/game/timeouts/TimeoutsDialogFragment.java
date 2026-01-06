package com.summithillsoftware.ultimate.ui.game.timeouts;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.summithillsoftware.ultimate.R;
import com.summithillsoftware.ultimate.model.Game;
import com.summithillsoftware.ultimate.model.Preferences;
import com.summithillsoftware.ultimate.model.TimeoutDetails;
import com.summithillsoftware.ultimate.ui.Refreshable;
import com.summithillsoftware.ultimate.ui.UltimateActivity;
import com.summithillsoftware.ultimate.ui.UltimateDialogFragment;

public class TimeoutsDialogFragment extends UltimateDialogFragment {
	private static final String IS_ACTION_MODE = "actionMode";

	// widgets
	private ImageButton doneButton;
	private Spinner timeoutsPerHalfQuotaSpinner;
	private Spinner timeoutFloatersQuotaSpinner;
	private TextView timeoutsTakenFirstHalfTextView;
	private TextView timeoutsTakenSecondHalfLabel;
	private TextView timeoutsTakenSecondHalfTextView;
	private TextView timeoutsAvailableTextView;
	private Button takeTimeoutButton;
	private Button undoTimeoutButton;
	private TextView timeoutsTakenLabel;
	private View timeoutsTakenView;	
	private View actionView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_timeouts, container,
				false);
		connectWidgets(view);
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
		registerWidgetListeners();
	}

	private void connectWidgets(View view) {

		doneButton = (ImageButton) view.findViewById(R.id.doneButton);
		timeoutsPerHalfQuotaSpinner = (Spinner) view.findViewById(R.id.timeoutsPerHalfQuotaSpinner);
		timeoutFloatersQuotaSpinner = (Spinner) view.findViewById(R.id.timeoutFloatersQuotaSpinner);	
		timeoutsTakenFirstHalfTextView = (TextView) view.findViewById(R.id.timeoutsTakenFirstHalfTextView);	
		timeoutsTakenSecondHalfLabel = (TextView) view.findViewById(R.id.timeoutsTakenSecondHalfLabel);	
		timeoutsTakenSecondHalfTextView = (TextView) view.findViewById(R.id.timeoutsTakenSecondHalfTextView);	
		timeoutsAvailableTextView = (TextView) view.findViewById(R.id.timeoutsAvailableTextView);	
		takeTimeoutButton = (Button) view.findViewById(R.id.takeTimeoutButton);
		undoTimeoutButton = (Button) view.findViewById(R.id.undoTimeoutButton);		
		actionView = (View) view.findViewById(R.id.actionView);
		timeoutsTakenLabel = (TextView) view.findViewById(R.id.timeoutsTakenLabel);
		timeoutsTakenView = (View) view.findViewById(R.id.timeoutsTakenView);

	}

	private void populateView() {
	    boolean is2ndHalf = game().isAfterHalftime();
	    
	    // populate fields
		populateQuotaSpinner(timeoutsPerHalfQuotaSpinner, timeoutDetails().getQuotaPerHalf());
		populateQuotaSpinner(timeoutFloatersQuotaSpinner, timeoutDetails().getQuotaFloaters());
		timeoutsTakenFirstHalfTextView.setText(Integer.toString(timeoutDetails().getTakenFirstHalf()));
		timeoutsTakenSecondHalfTextView.setText(Integer.toString(timeoutDetails().getTakenSecondHalf()));		
		timeoutsAvailableTextView.setText(Integer.toString(game().availableTimeouts()));
		
		// decorate
		timeoutsAvailableTextView.setTextColor(getResources().getColor(game().availableTimeouts() == 0 ? R.color.Red : R.color.White));
	    
	    // hide stuff that is not applicable
	    actionView.setVisibility(isActionMode() ? View.VISIBLE : View.GONE);
	    if (isActionMode()) {
		    takeTimeoutButton.setVisibility(game().availableTimeouts() > 0 ? View.VISIBLE : View.GONE);
		    undoTimeoutButton.setVisibility(shouldShowUndoButton() ? View.VISIBLE : View.GONE);
		    timeoutsTakenLabel.setVisibility(game().hasBeenSaved() ? View.VISIBLE : View.GONE);
		    timeoutsTakenView.setVisibility(game().hasBeenSaved() ? View.VISIBLE : View.GONE);
		    timeoutsTakenSecondHalfLabel.setVisibility(is2ndHalf ? View.VISIBLE : View.INVISIBLE);
		    timeoutsTakenSecondHalfTextView.setVisibility(is2ndHalf ? View.VISIBLE : View.INVISIBLE);
	    }
	}
	
	private boolean shouldShowUndoButton() {
		if (game().isHalftime()) {
			return timeoutDetails().getTakenFirstHalf() > 0 || timeoutDetails().getTakenSecondHalf() > 0;
		} else if (game().isAfterHalftime()) {
			return timeoutDetails().getTakenSecondHalf() > 0;
		} else {
			return timeoutDetails().getTakenFirstHalf() > 0;
		}
	}
	
	private void populateQuotaSpinner(Spinner spinner, int selection) {
        Integer[] optionsList = new Integer[]{0,1,2,3,4};
        ArrayAdapter<Integer> dataAdapter = new ArrayAdapter<Integer>(this.getActivity(), android.R.layout.simple_spinner_item, optionsList);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
        spinner.setSelection(selection);
	}

	private void registerWidgetListeners() {
		doneButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				doneButtonPressed();
			}
		});
		takeTimeoutButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
			    if (game().isHalftime()) {
			        promptForApplyDuringHalf();
			    } else if (game().isAfterHalftimeStarted()) {
			    	timeoutDetails().incrTakenSecondHalf();
			    	populateView();
			    } else {
			    	timeoutDetails().incrTakenFirstHalf();
			    	populateView();
			    }
			}
		});		
		undoTimeoutButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
			    if (game().isHalftime()) {
			    	if (timeoutDetails().getTakenFirstHalf() > 0 && timeoutDetails().getTakenSecondHalf() > 0) {
			    		promptForWhichHalfForUndo();
			    	} else {
			    		promptForUndoConfirm();
			    	}
			    } else {
			    	promptForUndoConfirm();
			    }
			}
		});			
		timeoutsPerHalfQuotaSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
		    @Override
		    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
		    	if (timeoutDetails().getQuotaPerHalf() != position) { // avoids circular problem (changing value fires the event)
			        timeoutDetails().setQuotaPerHalf(position);
			        populateView();
		    	}
		    }
		    @Override
		    public void onNothingSelected(AdapterView<?> parentView) { 
		        // no-op
		    }
		});
		timeoutFloatersQuotaSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
		    @Override
		    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
		    	if (timeoutDetails().getQuotaFloaters() != position) { // avoids circular problem (changing value fires the event)
			        timeoutDetails().setQuotaFloaters(position);
			        populateView();
		    	}
		    }
		    @Override
		    public void onNothingSelected(AdapterView<?> parentView) {
		        // no-op
		    }
		});		
	}
	
	private void updatePreferences() {
		Preferences.current().setTimeoutsPerHalf(timeoutDetails().getQuotaPerHalf());
		Preferences.current().setTimeoutFloatersPerGame(timeoutDetails().getQuotaFloaters());
		Preferences.current().save();
	}

	private void registerDialogCancelListener(Dialog dialog) {
		dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface arg0) {
				// Nothing yet...just coded in case we want to do something here
				// later
			}
		});
	}
	
	private void doneButtonPressed() {
		updatePreferences();			
		if (getActivity() instanceof Refreshable) {
			((Refreshable)getActivity()).refresh();
		}
		if (game().hasBeenSaved()) {
			game().save();
		}
		dismiss();		
	}
	
	private void promptForApplyDuringHalf() {
		((UltimateActivity)getActivity()).displayThreeButtonDialog(
				getString(R.string.alert_timeout_which_half_title),
				getString(R.string.alert_timeout_which_half_message),
				getString(R.string.button_timeouts_which_half_first),
				getString(R.string.button_timeouts_which_half_second),
				getString(R.string.button_cancel),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						timeoutDetails().incrTakenFirstHalf();
						populateView();
					}
				},
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						timeoutDetails().incrTakenSecondHalf();
						populateView();
					}					
				},
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// no-op
					}
				});
	}
	
	private void promptForWhichHalfForUndo() {
		((UltimateActivity)getActivity()).displayThreeButtonDialog(
				getString(R.string.alert_timeout_undo_title),
				getString(R.string.alert_timeout_which_half_undo_message),
				getString(R.string.button_timeouts_which_half_first),
				getString(R.string.button_timeouts_which_half_second),
				getString(R.string.button_cancel),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						timeoutDetails().decrTakenFirstHalf();
						populateView();
					}
				},
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						timeoutDetails().decrTakenSecondHalf();
						populateView();
					}					
				},
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// no-op
					}
				});
	}
	
	private void promptForUndoConfirm() {
		((UltimateActivity)getActivity()).displayConfirmDialog(
				getString(R.string.alert_timeout_undo_title),
				getString(R.string.alert_timeout_undo_confirm_message),
				getString(R.string.button_ok),
				getString(R.string.button_cancel),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (game().isAfterHalftime()) {
							timeoutDetails().decrTakenSecondHalf();
						} else {
							timeoutDetails().decrTakenFirstHalf();
						}
						populateView();
					}
				},
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// no-op
					}
				});
	}
    
	private TimeoutDetails timeoutDetails() {
		return game().getTimeoutDetails();
	}
	
	private Game game() {
		return Game.current();
	}

	public boolean isActionMode() {
		Bundle fragmentStateBundle = getArguments();
		return fragmentStateBundle == null ? true : fragmentStateBundle.getBoolean(IS_ACTION_MODE, true);
	}

	public void setActionMode(boolean isActionMode) {
		Bundle fragmentStateBundle = getArguments();
		if (fragmentStateBundle == null) {
			fragmentStateBundle = new Bundle();
			setArguments(fragmentStateBundle);
		}
		fragmentStateBundle.putBoolean(IS_ACTION_MODE, isActionMode);
	}


}
