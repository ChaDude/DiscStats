package com.summithillsoftware.ultimate.model;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import android.content.Context;

import com.summithillsoftware.ultimate.R;

public class Score implements Externalizable {
	private static final long serialVersionUID = 1l;
	private static final byte OBJECT_STATE_VERSION = 1;
	private int ours;
	private int theirs;
	
	public Score() {
		super();
	}
	
	public Score(int ours, int theirs) {
		super();
		this.ours = ours;
		this.theirs = theirs;
	}
	
	public int getOurs() {
		return ours;
	}
	
	public void setOurs(int ours) {
		this.ours = ours;
	}
	
	public void incOurs() {
		this.ours++;
	}
	
	public int getTheirs() {
		return theirs;
	}
	
	public void setTheirs(int theirs) {
		this.theirs = theirs;
	}
	
	public void incTheirs() {
		this.theirs++;
	}

	public String formatted() {
		return ours + "-" + theirs;
	}
	
	public boolean isOurLead() {
		return ours > theirs;
	}
	
	public boolean isTie() {
		return ours == theirs;
	}
	
	public boolean isTheirLead() {
		return theirs > ours;
	}
	
	public int getLeadingScore() {
		return Math.max(ours, theirs);
	}
	
	public int getTrailingScore() {
		return Math.min(ours, theirs);
	}
	
	public Score copy() {
		return new Score(ours, theirs);
	}

	public String format(Context context, boolean twoLines) {
		String scoreFormatted = getOurs() + "-" + getTheirs() + " ";
		String lineSeparator = twoLines ? "\n" : "";
		if (isOurLead()) {
			scoreFormatted += lineSeparator + "(" + context.getString(R.string.common_us) + ")";
		} else if (isTheirLead()) {
			scoreFormatted += lineSeparator + "(" + context.getString(R.string.common_them) + ")";
		} else {
			scoreFormatted += lineSeparator + "(" + context.getString(R.string.common_tied) + ")";
		} 
		return scoreFormatted;
	}
	
	public void readExternal(ObjectInput input) throws IOException, ClassNotFoundException {
		@SuppressWarnings("unused")
		byte version = input.readByte();  // if vars change use this to decide how far to read
		ours = input.readInt();
		theirs = input.readInt();
	}

	public void writeExternal(ObjectOutput output) throws IOException {
		output.writeByte(OBJECT_STATE_VERSION);
		output.writeInt(ours);
		output.writeInt(theirs);
	}

}
