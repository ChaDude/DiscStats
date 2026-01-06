package com.summithillsoftware.ultimate.ui.callout;

import java.io.Externalizable;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashSet;
import java.util.Set;

import com.summithillsoftware.ultimate.UltimateApplication;
import com.summithillsoftware.ultimate.util.AtomicFile;
import com.summithillsoftware.ultimate.util.UltimateLogger;

public class CalloutTracker implements Externalizable {
	
	// DO NOT CHANGE THE NUMBER....JUST ADD NEW ONES
	public static final int CALLOUT_OUR_TEAMS_ONLY = 1;
	public static final int CALLOUT_BUTTON_COLOR = 2;
	public static final int CALLOUT_ACTION_LONG_PRESS_THROWAWAY = 3;
	public static final int CALLOUT_ACTION_TAP_TO_CORRECT = 4;
	public static final int CALLOUT_SWIPE_UP_TO_SEE_MORE = 6;
	public static final int CALLOUT_UNDO_BUTTON = 7;
	public static final int CALLOUT_SIGNON_TO_SERVER = 8;
	public static final int CALLOUT_STATS_AVAIL_ON_SERVER = 9;
	public static final int CALLOUT_WEBSITE_LINK_TEAM = 10;
	public static final int CALLOUT_WELCOME = 11;
	public static final int CALLOUT_WEBSITE_LINK_GAME = 12;
	
	/*******************************/
	
	private static final long serialVersionUID = 1l;
	private static final byte OBJECT_STATE_VERSION = 1;
	private static final String FILE_NAME = "callout_tracker.ser";
	private static CalloutTracker Current;
	
	private Set<Integer> displayedCallouts = new HashSet<Integer>();
	
	static {
		Current = restore();
		if (Current == null) {
			Current = new CalloutTracker();
		}
	}
	
	public CalloutTracker() {
	}
	
	public static CalloutTracker current() {
		return Current;
	}
	
	public void clear() {
		displayedCallouts.clear();
		save();
	}
	
	public boolean hasCalloutBeenShown(int calloutId) {
		return displayedCallouts.contains(calloutId);
	}
	
	public void setCalloutShown(int calloutId) {
		displayedCallouts.add(calloutId);
		save();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void readExternal(ObjectInput input) throws IOException, ClassNotFoundException {
		@SuppressWarnings("unused")
		byte version = input.readByte();  // if vars change use this to decide how far to read
		displayedCallouts = (Set<Integer>)input.readObject();

	}

	@Override
	public void writeExternal(ObjectOutput output) throws IOException {
		output.writeByte(OBJECT_STATE_VERSION);
		output.writeObject(displayedCallouts);
	}
	
	private static CalloutTracker restore() {
		// will answer NULL if error or not found
		synchronized (FILE_NAME) {
			// will answer NULL if error or not found
			CalloutTracker calloutTracker = null;
			try {
				calloutTracker = null;
				File existingFile = getFile();
				if (existingFile != null && AtomicFile.exists(existingFile)) {
					calloutTracker = (CalloutTracker)AtomicFile.readObject(existingFile);
				}
			} catch (Exception e) {
				UltimateLogger.logError("Unable to restore callout tracker",e);
				AtomicFile.delete(getFile());
			}
			return calloutTracker;
		}
	}
	
	private static File getFile() {
		return new File(UltimateApplication.current().getFilesDir(), FILE_NAME);
	}
	
	private void save() {
		synchronized (FILE_NAME) {
			try {
				File file = getFile();
				AtomicFile.writeObject(this, file);
			} catch (Exception e) {
				UltimateLogger.logError("Unable to save callout tracker",e);
			}
		}
	}




}
