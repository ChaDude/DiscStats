package com.summithillsoftware.ultimate.ui.game.specialevent;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.summithillsoftware.ultimate.R;
import com.summithillsoftware.ultimate.model.Action;
import com.summithillsoftware.ultimate.model.DefenseEvent;
import com.summithillsoftware.ultimate.model.Event;
import com.summithillsoftware.ultimate.model.OffenseEvent;
import com.summithillsoftware.ultimate.model.Player;
import com.summithillsoftware.ultimate.model.Team;
import com.summithillsoftware.ultimate.ui.UltimateDialogFragment;
import com.summithillsoftware.ultimate.ui.game.action.GameActionActivity;

public class SpecialEventDialogFragment extends UltimateDialogFragment {
	private static final String PLAYER_NAME1_ARG = "playerName1";
	private static final String PLAYER_NAME2_ARG = "playerName2";
	private static final String ACTION_ARG = "action";
	private static final String IS_OFFENSE_ARG = "isO";

	// widgets
	private TextView eventTypeDescriptionLabel;
	private Button cancelButton;
	private Button saveButton;
	private RadioGroup eventChoiceRadioGroup;
	private RadioButton eventChoice1;
	private RadioButton eventChoice2;
	private RadioButton eventChoice3;
	private RadioButton eventChoice4;

	public void setOriginalEvent(Event event) {
		Bundle args = new Bundle();
		args.putString(ACTION_ARG, event.getAction().name());
		if (event.getPlayerOne() != null) {
			args.putString(PLAYER_NAME1_ARG, event.getPlayerOne().getName());
		}
		if (event.getPlayerTwo() != null) {
			args.putString(PLAYER_NAME2_ARG, event.getPlayerTwo().getName());
		}
		args.putBoolean(IS_OFFENSE_ARG, event.isOffense());
		setArguments(args);
	}

	private Event getOriginalEvent() {
		Action action = Action.valueOf(getArguments().getString(
				ACTION_ARG));
		if (getArguments().getBoolean(IS_OFFENSE_ARG, false)) {
			OffenseEvent event = new OffenseEvent(action, getPlayerOne());
			event.setReceiver(getPlayerTwo());
			return event;
		} else {
			DefenseEvent event = new DefenseEvent(action, getPlayerOne());
			return event;
		}
	}

	private Player getPlayerOne() {
		String playerName = getArguments().getString(PLAYER_NAME1_ARG);
		if (playerName == null) {
			return null;
		} else {
			return Team.getPlayerNamed(playerName);
		}
	}

	private Player getPlayerTwo() {
		String playerName = getArguments().getString(PLAYER_NAME2_ARG);
		if (playerName == null) {
			return null;
		} else {
			return Team.getPlayerNamed(playerName);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_specialevent, container,
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
		eventTypeDescriptionLabel = (TextView) view
				.findViewById(R.id.eventTypeDescriptionLabel);
		saveButton = (Button) view.findViewById(R.id.saveButton);
		cancelButton = (Button) view.findViewById(R.id.cancelButton);
		eventChoiceRadioGroup = (RadioGroup) view
				.findViewById(R.id.eventChoiceRadioGroup);
		eventChoice1 = (RadioButton) view.findViewById(R.id.eventChoice1);
		eventChoice2 = (RadioButton) view.findViewById(R.id.eventChoice2);
		eventChoice3 = (RadioButton) view.findViewById(R.id.eventChoice3);
		eventChoice4 = (RadioButton) view.findViewById(R.id.eventChoice4);
	}

	private void populateView() {
		Event event = getOriginalEvent();
		eventChoice1.setText(event.toString());
		if (event.isD()) {

			eventTypeDescriptionLabel
					.setText(getString(R.string.label_game_specialevent_d));
			eventTypeDescriptionLabel.setVisibility(View.VISIBLE);
			
			event.setAction(Action.Callahan);
			eventChoice2.setText(event.toString());
			eventChoice2.setVisibility(View.VISIBLE);

		} else if (event.isOffenseThrowaway()) {

			eventTypeDescriptionLabel
					.setText(getString(R.string.label_game_specialevent_turnover));
			eventTypeDescriptionLabel.setVisibility(View.VISIBLE);

			event.setAction(Action.Stall);
			eventChoice2.setText(event.toString());
			eventChoice2.setVisibility(View.VISIBLE);

			event.setAction(Action.MiscPenalty);
			eventChoice3.setText(event.toString());
			eventChoice3.setVisibility(View.VISIBLE);

			event.setAction(Action.Callahan);
			eventChoice4.setText(event.toString());
			eventChoice4.setVisibility(View.VISIBLE);
		}
		eventChoice1.setChecked(true);
	}

	private Event createEventToAdd() {
		Event event = getOriginalEvent();
		if (event.isD()) {
			if (eventChoiceRadioGroup.getCheckedRadioButtonId() == R.id.eventChoice1) {
				return event;
			} else {
				event.setAction(Action.Callahan);
				return event;
			}
		} else if (event.isOffenseThrowaway()) {
			switch (eventChoiceRadioGroup.getCheckedRadioButtonId()) {
			case R.id.eventChoice2:
				event.setAction(Action.Stall);
				break;
			case R.id.eventChoice3:
				event.setAction(Action.MiscPenalty);
				break;
			case R.id.eventChoice4:
				event.setAction(Action.Callahan);
				break;
			default:
				break;
			}
			return event;
		} else {
			return event;
		}
	}

	private void registerWidgetListeners() {
		saveButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				((GameActionActivity) getActivity()).newEvent(createEventToAdd());
				dismiss();
			}
		});
		cancelButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				dismiss();
			}
		});
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


}
