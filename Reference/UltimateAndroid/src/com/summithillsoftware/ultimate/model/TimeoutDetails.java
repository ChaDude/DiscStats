package com.summithillsoftware.ultimate.model;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.json.JSONException;
import org.json.JSONObject;

public class TimeoutDetails implements Externalizable {
	private static final long serialVersionUID = 1l;
	private static final byte OBJECT_STATE_VERSION = 1;
	
	private static final String JSON_QUOTA_PER_HALF = "quotaPerHalf";
	private static final String JSON_QUOTA_FLOATERS = "quotaFloaters";
	private static final String JSON_TAKEN_FIRST_HALF = "takenFirstHalf";
	private static final String JSON_TAKEN_SECOND_HALF = "takenSecondHalf";

	private int quotaPerHalf;
	private int quotaFloaters;
	private int takenFirstHalf;
	private int takenSecondHalf;
	
	public int getQuotaPerHalf() {
		return quotaPerHalf;
	}
	public void setQuotaPerHalf(int quotaPerHalf) {
		this.quotaPerHalf = Math.max(0, quotaPerHalf);
	}
	public int getQuotaFloaters() {
		return quotaFloaters;
	}
	public void setQuotaFloaters(int quotaFloaters) {
		this.quotaFloaters = Math.max(0, quotaFloaters);
	}
	public int getTakenFirstHalf() {
		return takenFirstHalf;
	}
	public void setTakenFirstHalf(int takenFirstHalf) {
		this.takenFirstHalf = Math.max(0, takenFirstHalf);
	}
	public void incrTakenFirstHalf() {
		setTakenFirstHalf(getTakenFirstHalf() + 1);
	}
	public void decrTakenFirstHalf() {
		setTakenFirstHalf(getTakenFirstHalf() - 1);
	}
	public int getTakenSecondHalf() {
		return takenSecondHalf;
	}
	public void setTakenSecondHalf(int takenSecondHalf) {
		this.takenSecondHalf = Math.max(0, takenSecondHalf);
	}
	public void incrTakenSecondHalf() {
		setTakenSecondHalf(getTakenSecondHalf() + 1);
	}
	public void decrTakenSecondHalf() {
		setTakenSecondHalf(getTakenSecondHalf() - 1);
	}

	@Override
	public void readExternal(ObjectInput input) throws IOException, ClassNotFoundException {
		@SuppressWarnings("unused")
		byte version = input.readByte();  // if vars change use this to decide how far to read
		quotaPerHalf = input.readInt();
		quotaFloaters = input.readInt();
		takenFirstHalf = input.readInt();
		takenSecondHalf = input.readInt();
	}

	@Override
	public void writeExternal(ObjectOutput output) throws IOException {
		output.writeByte(OBJECT_STATE_VERSION);
		output.writeInt(quotaPerHalf);
		output.writeInt(quotaFloaters);
		output.writeInt(takenFirstHalf);
		output.writeInt(takenSecondHalf);
	}

	public JSONObject toJsonObject() throws JSONException {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put(JSON_QUOTA_PER_HALF, quotaPerHalf);
		jsonObject.put(JSON_QUOTA_FLOATERS, quotaFloaters);
		jsonObject.put(JSON_TAKEN_FIRST_HALF, takenFirstHalf);
		jsonObject.put(JSON_TAKEN_SECOND_HALF, takenSecondHalf);
		return jsonObject;
	}
	
	public static TimeoutDetails fromJsonObject(JSONObject jsonObject) throws JSONException {
		if (jsonObject == null) {
			return null;
		} else {
			TimeoutDetails timeoutDetails = new TimeoutDetails();
			if (jsonObject.has(JSON_QUOTA_PER_HALF)) {
				timeoutDetails.quotaPerHalf = (jsonObject.getInt(JSON_QUOTA_PER_HALF));
			}
			if (jsonObject.has(JSON_QUOTA_FLOATERS)) {
				timeoutDetails.quotaFloaters = (jsonObject.getInt(JSON_QUOTA_FLOATERS));
			}
			if (jsonObject.has(JSON_TAKEN_FIRST_HALF)) {
				timeoutDetails.takenFirstHalf = (jsonObject.getInt(JSON_TAKEN_FIRST_HALF));
			}
			if (jsonObject.has(JSON_TAKEN_SECOND_HALF)) {
				timeoutDetails.takenSecondHalf = (jsonObject.getInt(JSON_TAKEN_SECOND_HALF));
			}

			return timeoutDetails;
		}
	}
}
