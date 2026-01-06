package com.summithillsoftware.ultimate.ui.game.line;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.gesture.Gesture;
import android.gesture.GestureOverlayView;
import android.gesture.GestureOverlayView.OnGesturePerformedListener;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.SlidingDrawer;
import android.widget.SlidingDrawer.OnDrawerCloseListener;
import android.widget.SlidingDrawer.OnDrawerOpenListener;
import android.widget.TextView;

import com.summithillsoftware.ultimate.R;
import com.summithillsoftware.ultimate.UltimateApplication;
import com.summithillsoftware.ultimate.model.Game;
import com.summithillsoftware.ultimate.model.Player;
import com.summithillsoftware.ultimate.model.PlayerSubstitution;
import com.summithillsoftware.ultimate.model.SubstitutionReason;
import com.summithillsoftware.ultimate.model.Team;
import com.summithillsoftware.ultimate.stats.PlayerStat;
import com.summithillsoftware.ultimate.stats.PlayerStatisticsCalculator;
import com.summithillsoftware.ultimate.ui.DefaultAnimatorListener;
import com.summithillsoftware.ultimate.ui.Size;
import com.summithillsoftware.ultimate.ui.UltimateActivity;
import com.summithillsoftware.ultimate.ui.UltimateDialogFragment;
import com.summithillsoftware.ultimate.ui.UltimateGestureHelper;
import com.summithillsoftware.ultimate.ui.ViewHelper;
import com.summithillsoftware.ultimate.ui.game.action.GameActionActivity;
import com.summithillsoftware.ultimate.util.SoundPlayer;
import com.summithillsoftware.ultimate.util.UltimateLogger;
import com.summithillsoftware.ultimate.util.UniqueTimestampGenerator;

@SuppressWarnings("deprecation")
public class LineDialogFragment extends UltimateDialogFragment {
	private static int BUTTON_MARGIN = 2;
	private static String LINE_STATE_PROPERTY = "line";
	private Map<String, PlayerStat> pointsPerPlayer;
	private Map<String, Float> pointsPlayedFactorPerPlayer;
	
	// widgets
	private Button lastLineButton;
	private Button clearButton;
	private RadioGroup changeTypeRadioGroup;
	private RadioGroup substitutionTypeRadioGroup;
	private SlidingDrawer substitutesSlidingDrawer;
	private ViewGroup benchContainerView;
	private GestureOverlayView benchContainerGestureOverlay;
	private GestureOverlayView substitutionsDrawerGestureOverlay;
	private View toolbar;
	private View headerSeperator;
	private ViewGroup benchOverlay;
	private ViewGroup lineFieldPlayers;
	private ViewGroup lineBenchPlayers;
	private ImageView errorImageView;
	private TextView fieldContainerLabel;
	
	private int buttonWidth;
	private int buttonHeight;
	private ArrayList<Player> line = new ArrayList<Player>();
	private Set<Player> originalLine = new HashSet<Player>();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		resetLine();
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
		View view =  inflater.inflate(R.layout.fragment_line, container, false);
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
		calculateButtonSize();
		updateViewWidth();
		populateView();
        registerLastLineButtonClickListener();
        registerClearButtonClickListener();	
        registerDoneButtonClickListener();
        registerChangeModeRadioListener();
        registerUndoSubstitutionClickListener();
        if (doesPointHaveSubstitutions()) {
        	registerBenchContainerSwipeListener();
            registerSlidingDrawerContainerSwipeListener();
        }
        registerLayoutListener();
        registerSlidingDrawerOpenAndCloseListeners();
	}
	
	@Override
	public void onSaveInstanceState(Bundle bundle) {
		super.onSaveInstanceState(bundle);
		bundle.putSerializable(LINE_STATE_PROPERTY, line);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onViewStateRestored(Bundle savedInstanceState) {
		super.onViewStateRestored(savedInstanceState);
		if (savedInstanceState != null) {
			line = (ArrayList<Player>)savedInstanceState.getSerializable(LINE_STATE_PROPERTY);
		}
	}
	
	@Override
	public void onDismiss(DialogInterface dialog) {
		GameActionActivity activity = (GameActionActivity)getActivity();
		super.onDismiss(dialog);
		if (activity != null) {
			activity.refresh();
		}
	}
	
	private void connectWidgets(View view) {
		lineFieldPlayers = (ViewGroup)view.findViewById(R.id.lineFieldPlayers);
		lineBenchPlayers = (ViewGroup)view.findViewById(R.id.lineBenchPlayers);
		lastLineButton = (Button)view.findViewById(R.id.button_last_line);
		clearButton = (Button)view.findViewById(R.id.clear);
		changeTypeRadioGroup = (RadioGroup)view.findViewById(R.id.radio_line_change_type);
		substitutionTypeRadioGroup = (RadioGroup)view.findViewById(R.id.radio_line_substitution_type);
		substitutesSlidingDrawer = (SlidingDrawer)view.findViewById(R.id.subsitutesDrawer);
		benchContainerView = (ViewGroup)view.findViewById(R.id.lineBenchPlayersScrollView);
		benchContainerGestureOverlay = (GestureOverlayView)view.findViewById(R.id.benchGestureOverlay);
		substitutionsDrawerGestureOverlay = (GestureOverlayView)view.findViewById(R.id.substitutionsDrawerContentGestureOverlay);
		toolbar = (View)view.findViewById(R.id.lineButtonToolbar);
		headerSeperator = (View)view.findViewById(R.id.line_change_separator);
		benchOverlay = (ViewGroup)view.findViewById(R.id.benchOverlay);
		errorImageView = (ImageView)view.findViewById(R.id.errorImageView);
		if (doesPointHaveSubstitutions()) {
			initializeSubstitutionsListView(getSubstitutionsList(view));
		}		
	}
	
	private ListView getSubstitutionsList(View view) {
		return (ListView)view.findViewById(R.id.substitutionsList);
	}
	
	private void initializeSubstitutionsListView(View view) {
		SubstitutionsListAdaptor adaptor = new SubstitutionsListAdaptor(this.getActivity());
		getSubstitutionsList(view).setAdapter(adaptor);
	}
	
    private void populateView() {
    	configureViews();
    	populateFieldAndBench();
    	lastLineButton.setText(isPointOline() ? R.string.button_line_last_oline : R.string.button_line_last_dline);
    }
    
    private void populateFieldAndBench() {
    	List<Player> players = new ArrayList<Player>(line);
    	while (players.size() < 7) {
			players.add(Player.anonymous());
		}
    	populateButtonContainer(lineFieldPlayers, players, true);
    	
    	players = new ArrayList<Player>();
    	for (Player player : Team.current().getPlayersSorted()) {
			if (!player.isAbsent()) {
				players.add(player);
			}
		}
    	populateButtonContainer(lineBenchPlayers, players, false);
    }
    
    private void populateButtonContainer(ViewGroup buttonContainer, List<Player> players, boolean isField) {
    	buttonContainer.removeAllViews();
    	int maxButtonsPerRow = playerButtonsPerRow();
    	LinearLayout buttonRowView = addButtonRowLayout(buttonContainer);
    	TextView containerLabel = createButtonContainerLabel(isField ? "Field"  : "Bench");
    	if (isField) {
    		fieldContainerLabel = containerLabel;
    	}
    	addButtonOrLabelToRow(buttonRowView,containerLabel);
    	int numberOfButtonsInRow = 1;  
    	for (Player player : players) {
    		if (numberOfButtonsInRow >= maxButtonsPerRow) {
    			buttonRowView = addButtonRowLayout(buttonContainer);
    			numberOfButtonsInRow = 0;
    		}
    		PlayerLineButtonView button = createLineButton(player);
    		logView(button, "row button[" + numberOfButtonsInRow +"]");
    		button.setButtonOnFieldView(isField, line, originalLine);
    		addButtonOrLabelToRow(buttonRowView, button);
	        numberOfButtonsInRow++;
		}
    }
    
    private void configureViews() {
    	boolean hasPointStarted = hasPointStarted();
    	headerSeperator.setVisibility(hasPointStarted ? View.GONE : View.VISIBLE);
    	changeTypeRadioGroup.setVisibility(hasPointStarted ? View.VISIBLE : View.GONE);  
    	substitutesSlidingDrawer.setVisibility(doesPointHaveSubstitutions() ? View.VISIBLE : View.GONE);
    	configureMode();
    }
    
    private void configureMode() {
    	boolean  isSubstitution = isSubstitution();
    	toolbar.setVisibility(isSubstitution ? View.GONE : View.VISIBLE);
    	substitutionTypeRadioGroup.setVisibility(isSubstitution ? View.VISIBLE : View.GONE);
    }
    
    private LinearLayout addButtonRowLayout(ViewGroup buttonContainer) {
    	LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService( Context.LAYOUT_INFLATER_SERVICE );	
    	LinearLayout buttonRow = (LinearLayout)inflater.inflate(R.layout.line_row, null);
    	buttonContainer.addView(buttonRow);
    	return buttonRow;
    }
    
    private void addButtonOrLabelToRow(LinearLayout buttonRowView, View buttonOrLabel) {
    	LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(buttonWidth, buttonHeight);
        layoutParams.setMargins(BUTTON_MARGIN, BUTTON_MARGIN, BUTTON_MARGIN, BUTTON_MARGIN);
        buttonRowView.addView(buttonOrLabel,layoutParams);
    }
    
    private int playerButtonsPerRow() {
    	return (UltimateApplication.current().isLandscape()) ? 6 : 4;
    }
    
    private TextView createButtonContainerLabel(String name) {
    	TextView label = new TextView(getActivity());
    	label.setText(name);
    	label.setWidth(buttonWidth);
    	label.setPadding(6,0,0,0);
    	label.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
    	return label;
    }
    
    private PlayerLineButtonView createLineButton(Player player) {
    	PlayerLineButtonView button = createPlayerLineButtonView();
		button.setPlayer(player, pointsPlayerForPlayer(player), playingTimeFactorForPlayer(player));
        button.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 playerButtonClicked((PlayerLineButtonView)v);
             }
         });	
        button.setWidth(buttonWidth);
        return button;
    }

	private float playingTimeFactorForPlayer(Player player) {
		Float playedFactor = getPointsPlayedFactorPerPlayer().get(player.getName());
		float factor = playedFactor == null ? 0.0f : playedFactor.floatValue();
		return factor;
	}

	private float pointsPlayerForPlayer(Player player) {
		PlayerStat pointsPlayedStat = getPointsPerPlayer().get(player.getName());
		float pointsPlayed = pointsPlayedStat == null ? 0.0f : pointsPlayedStat.getFloatValue();
		return pointsPlayed;
	}
    
    private PlayerLineButtonView createPlayerLineButtonView() {
    	PlayerLineButtonView button = new PlayerLineButtonView(getActivity());
    	return button;
    }
    
    private void playerButtonClicked(PlayerLineButtonView clickedButton) {
    	if (clickedButton.isButtonOnFieldView()) {  // player on field
    		removePlayerFromField(clickedButton);
    	} else {  // player on bench
    		if (line.size() < 7) {
    			if (validateMoveToField(clickedButton.getPlayer())) {
		    		line.add(clickedButton.getPlayer());
		    		putPlayerOnField(clickedButton);
    			} else {
    				errorSoundAndVibrate();
    			}
    		} else {
    			errorSoundAndVibrate();
    		}
    	}
    }
    
    private void putLineOnField() {
    	for (Player player : line) {
    		if (!player.isAnonymous()) { // shouldn't be anon players in line but just in case
    			putPlayerOnField(getButtonForPlayerName(player, false));
    		}
		}
    }
    
	private void putPlayerOnField(PlayerLineButtonView benchButton) {
		PlayerLineButtonView fieldButton = getButtonForPlayerName(Player.anonymous(), true);  // get an open slot
		if (fieldButton != null) {
			Player player = benchButton.getPlayer();
			if (!player.isAnonymous()) {
				fieldButton.setPlayer(player, pointsPlayerForPlayer(player), playingTimeFactorForPlayer(player));
				fieldButton.updateView(line, originalLine);
				benchButton.updateView(line, originalLine);
			}
		}
	}
    
    private void clearField() {
    	List<View> lineButtons = ViewHelper.allDescendentViews(lineFieldPlayers, PlayerLineButtonView.class);
    	for (View view : lineButtons) {
    		removePlayerFromField((PlayerLineButtonView)view);
		}
    }

	private void removePlayerFromField(PlayerLineButtonView fieldButton) {
		Player player = fieldButton.getPlayer();
		if (player != null && !fieldButton.getPlayer().isAnonymous()) {
			line.remove(fieldButton.getPlayer());
			PlayerLineButtonView benchButton = getButtonForPlayerName(fieldButton.getPlayer(), false);
			fieldButton.setPlayer(Player.anonymous(), 0, 0);
			if (benchButton != null) {
				benchButton.updateView(line, originalLine);
			}
			fieldButton.updateView(line, originalLine);
		}
	}

    private boolean validateMoveToField(Player player) {
    	if (Team.current().isMixed()) {
    		int maleCount = 0;
    		int femaleCount = 0;
    		for (View view : ViewHelper.allDescendentViews(lineFieldPlayers, PlayerLineButtonView.class)) {
				Player linePlayer = ((PlayerLineButtonView)view).getPlayer();
				if (!linePlayer.isAnonymous()) {
					
					if (linePlayer.isMale()) {
						maleCount++;
					} else {
						femaleCount++;
					}
					
					if (maleCount >= 4 && player.isMale()) {
						displayMixedTeamWouldBeOutOfBalanceError(true);
						return false;
					} else if (femaleCount >= 4 && player.isFemale()) {
						displayMixedTeamWouldBeOutOfBalanceError(false);
						return false;
					} 
				}
			}
    		return true;
    	} else {
    		return true;
    	}
    }
    
    private void displayMixedTeamWouldBeOutOfBalanceError(boolean isAtMaleLimit) {
    	SoundPlayer.current().playErrorSound();
    	
    	// TODO...center error image using label's rect
    	
    	fieldContainerLabel.setAlpha(0);
    	errorImageView.setVisibility(View.VISIBLE);
    	errorImageView.setImageResource(isAtMaleLimit ? R.drawable.too_many_males_on_field : R.drawable.too_many_females_on_field);
    	
    	AnimatorSet animationSet = new AnimatorSet();
    	ObjectAnimator errorImageViewHide = ObjectAnimator.ofFloat(errorImageView, View.ALPHA, 0f);
    	ObjectAnimator fieldContainerLabelShow = ObjectAnimator.ofFloat(fieldContainerLabel, View.ALPHA, 1f);
    	animationSet.play(fieldContainerLabelShow).with(errorImageViewHide);
    	animationSet.setDuration(3000);
    	animationSet.addListener(new DefaultAnimatorListener() {
    		@Override
    		public void onAnimationEnd(Animator animation) {
    	    	errorImageView.setVisibility(View.INVISIBLE);
    	    	errorImageView.setAlpha(1f);
    		}
    	});
    	animationSet.start();
    	

    }
    
    private PlayerLineButtonView getButtonForPlayerName(Player player, boolean onField) {
    	ViewGroup containerView = (ViewGroup)getView().findViewById(onField ? R.id.lineFieldPlayers : R.id.lineBenchPlayers);
    	return (PlayerLineButtonView) UltimateActivity.findFirstViewWithTag(containerView, player.getName());
    }

	private void registerLastLineButtonClickListener() {
		lastLineButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	clearField();
            	List<Player> lastLine = LineDialogFragment.this.isPointOline() ? Game.current().getLastOLine() : Game.current().getLastDLine();
            	if (lastLine == null) {
            		lastLine = new ArrayList<Player>();
            	} 
            	line = new ArrayList<Player>(lastLine);
            	putLineOnField();
            }
        });
	}
	
	private void registerClearButtonClickListener() {
		clearButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	clearField();
            }
        });
	}
	
	private void logView(View v, String name) {
		System.out.println(name + " height= " + v.getHeight() + " width=" + v.getWidth()  
				+ " x=" + v.getX()  + " y=" + v.getY() 
				+ " visibility=" + v.getVisibility() + "(View.VISIBLE=" + View.VISIBLE +")");
		if (v instanceof ViewGroup) {
			System.out.println("children:");
			ViewGroup vg = (ViewGroup)v;
			for (int i = 0; i < vg.getChildCount(); i++) {
				logView(vg.getChildAt(i), name + "-child");
			}
		}
	}
	
	private void registerChangeModeRadioListener() {
		changeTypeRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				configureMode();
			}
		});
	}
	
	private void registerDoneButtonClickListener() {
		Button doneButton = (Button)getView().findViewById(R.id.doneButton);
		ImageButton doneImageButton = (ImageButton)getView().findViewById(R.id.doneImageButton);
		OnClickListener listener = new View.OnClickListener() {
            public void onClick(View v) {
            	if (validateChanges()) {
                	if (isSubstitution() && !hasLineChanged()) {
                		confirmDismissInSubstitutionModeWithoutChanges();
                	} else {
                		saveAndDismiss();
                	}
            	}
            }
        };
        doneButton.setOnClickListener(listener);
        doneImageButton.setOnClickListener(listener);
	}
	
	private void registerUndoSubstitutionClickListener() {
		getView().findViewById(R.id.undoLastSubsitutionButton).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	SubstitutionsListAdaptor substitutionListAdaptor = (SubstitutionsListAdaptor)getSubstitutionsList(getView()).getAdapter();
            	substitutionListAdaptor.removeMostRecentSubstitutionSetFromGame();
            	resetLine();
            	populateFieldAndBench();
            	substitutesSlidingDrawer.animateClose();
            }
        });
	}
	
	private void registerLayoutListener() {
		final View view = benchContainerView;
		ViewTreeObserver vto = view.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@SuppressLint("NewApi")
			@Override
			public void onGlobalLayout() {
				ViewTreeObserver observer = view.getViewTreeObserver();
				LineDialogFragment.this.handleBenchContainerLayedOut();
				// remove the listener so we don't keep getting called	
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
					observer.removeOnGlobalLayoutListener(this);
		        } else {
		        	observer.removeGlobalOnLayoutListener(this);
		        }
				
			}
		});
	}
	
	private void registerSlidingDrawerContainerSwipeListener() {  // if swipe right then close the substitutions sliding drawer
		substitutionsDrawerGestureOverlay.addOnGesturePerformedListener(new OnGesturePerformedListener() {
			@Override
			public void onGesturePerformed(GestureOverlayView overlayView, Gesture gesture) {
				if (UltimateGestureHelper.current().isSwipeRight(gesture)) {
					LineDialogFragment.this.substitutesSlidingDrawer.animateClose();
				}
			}
		});
	}
	
	private void registerSlidingDrawerOpenAndCloseListeners() {  
		substitutesSlidingDrawer.setOnDrawerCloseListener(new OnDrawerCloseListener() {
			@Override
			public void onDrawerClosed() {
				enableBenchInteraction(true);
            	configureViews();
			}
		});
		substitutesSlidingDrawer.setOnDrawerOpenListener(new OnDrawerOpenListener() {
			@Override
			public void onDrawerOpened() {
				enableBenchInteraction(false);
			}
		});		
	}
	
	private void registerBenchContainerSwipeListener() {  // if swipe left then open substitutions sliding drawer
		benchContainerGestureOverlay.addOnGesturePerformedListener(new OnGesturePerformedListener() {
			@Override
			public void onGesturePerformed(GestureOverlayView overlayView, Gesture gesture) {
				if (UltimateGestureHelper.current().isSwipeLeft(gesture)) {
					LineDialogFragment.this.substitutesSlidingDrawer.animateOpen();
				}
			}
		});
	}
	
	private void registerDialogCancelListener(Dialog dialog) {
		dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface arg0) {
				// Nothing yet...just coded in case we want to commit changes later
			}
		});
	}
	
	private void handleBenchContainerLayedOut() { // if swipe right then close substitutions sliding drawer
		if (doesPointHaveSubstitutions()) {
			ViewGroup.MarginLayoutParams drawerMarginParams = (ViewGroup.MarginLayoutParams)substitutesSlidingDrawer.getLayoutParams();
			int benchContainerHeight = substitutesSlidingDrawer.getLayoutParams().height = benchContainerView.getHeight();
			substitutesSlidingDrawer.getLayoutParams().height = benchContainerHeight - drawerMarginParams.topMargin - drawerMarginParams.bottomMargin;
		}
	}
	
	private boolean isPointOline() {
		return Game.current().isCurrentlyOline();
	}

	private void updateViewWidth() {
    	LayoutParams params = getView().getLayoutParams();
    	params.width=getPreferredWidth();
    	getView().setLayoutParams(params);
	}
	
	private int getPreferredWidth() {
    	int numberOfButtons = playerButtonsPerRow();
    	int width = numberOfButtons * buttonWidth;
    	width = width + (BUTTON_MARGIN * 2 * numberOfButtons);
    	return width;
	}
	
	private void calculateButtonSize() {
		int numberOfButtons = playerButtonsPerRow();
		Size size = getScreenSize();
		int marginSpace = (BUTTON_MARGIN * 2 * numberOfButtons);
		int availableWidth = size.width - (marginSpace * 4);
		buttonWidth = availableWidth / numberOfButtons;
		
		float textHeight = createPlayerLineButtonView().getTextSize();
		buttonHeight = (int)textHeight * 3;
	}
	
	private void saveLineChangesToGame() {
		if (isSubstitution()) {
			if (validateChanges()) {
				addSubstitutionsToGame();
        		Game.current().save();
			}
		} else {
			Game.current().setCurrentLine(line);
    		Game.current().save();
		}
	}
	
	private void addSubstitutionsToGame() {
		int timestamp = UniqueTimestampGenerator.current().uniqueTimeIntervalSinceReferenceDateSeconds(); // give each sub the same timestamp so we can re-combine them
		Set<Player> lineBefore = new HashSet<Player>(Game.current().getCurrentLine());
		Set<Player> lineAfter = new HashSet<Player>(line);
		List<Player> playersOut = new ArrayList<Player>(lineBefore);
		playersOut.removeAll(lineAfter);
		List<Player> playersIn = new ArrayList<Player>(lineAfter);
		playersIn.removeAll(lineBefore);
		if (playersOut.size() == playersIn.size()) {
			for (int i = 0; i < playersOut.size(); i++) {
				PlayerSubstitution substitution = new PlayerSubstitution();
				substitution.setTimestamp(timestamp);
				substitution.setFromPlayer(playersOut.get(i));
				substitution.setToPlayer(playersIn.get(i));
				substitution.setReason(substitutionTypeRadioGroup.getCheckedRadioButtonId() == R.id.radio_line_substitution_type_injury ? 
						SubstitutionReason.SubstitutionReasonInjury : SubstitutionReason.SubstitutionReasonOther);
				Game.current().addSubstitution(substitution);
			}
		} else {
			UltimateLogger.logError( "Error...sub in/out don't match");
		}
	}
	
	private boolean isSubstitution() {
		return hasPointStarted() && changeTypeRadioGroup.getCheckedRadioButtonId() == R.id.radio_line_change_type_substitution;	
	}
	
	private boolean validateChanges() {
		if (isSubstitution() && line.size() != Game.current().currentLineSorted().size()) {
			displayErrorMessage(getString(R.string.alert_line_mismatched_substitutions_title), getString(R.string.alert_line_mismatched_substitutions_message));
			return false;
		} 
		return true;
	}
	
	private void saveAndDismiss() {
		saveLineChangesToGame();
		dismiss();
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private boolean hasLineChanged() {
		if (line.size() != Game.current().getCurrentLine().size()) {
			return true;
		}
		HashSet currentLine = new HashSet(Game.current().getCurrentLine());
		for (Player player: line) {
			if (!currentLine.contains(player)) {
				return true;
			}
		}
		return false;
	}
	
	private void confirmDismissInSubstitutionModeWithoutChanges() {
		displayConfirmDialog(
				getString(R.string.alert_line_substitutions_no_change_title),
				getString(R.string.alert_line_substitutions_no_change_message),
				getString(android.R.string.ok),
				getString(android.R.string.cancel),				
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface paramDialogInterface, int buttonClicked) {
						saveAndDismiss();
					}
				});
	}
	
	private void enableBenchInteraction(boolean allow) {
		if (allow) {
			benchOverlay.removeAllViews();
		} else {
			View overlay = new View(getActivity());
			overlay.setBackgroundColor(0x00000000);  // transparent
			overlay.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View paramView, MotionEvent paramMotionEvent) {
					return true;  // ignore all touches
				}
			});
	    	ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(benchContainerView.getWidth(), benchContainerView.getHeight());
	    	benchOverlay.addView(overlay,layoutParams);
		}
		
	}
	
	private void resetLine() {
		line = new ArrayList<Player>(Game.current().currentLineSorted());	
		originalLine = new HashSet<Player>(line);
	}
	
	private boolean hasPointStarted() {
		return Game.current().isPointInProgess();
	}
	
	private boolean doesPointHaveSubstitutions() {
		return Game.current().doesCurrentPointHaveSubstitutions() && !Game.current().isCurrentPointFinished();
	}

	public Map<String, PlayerStat> getPointsPerPlayer() {
		if (pointsPerPlayer == null) {
			pointsPerPlayer = PlayerStatisticsCalculator.pointsPerPlayer(Game.current(), true, true);
		}
		return pointsPerPlayer;
	}

	public Map<String, Float> getPointsPlayedFactorPerPlayer() {
		if (pointsPlayedFactorPerPlayer == null) {
			pointsPlayedFactorPerPlayer = PlayerStatisticsCalculator.pointsPlayedFactorPerPlayer(Game.current());
		}
		return pointsPlayedFactorPerPlayer;
	}

}
