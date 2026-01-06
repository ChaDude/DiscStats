package com.summithillsoftware.ultimate.model;

import org.json.JSONException;
import org.json.JSONObject;

public class PointSummary {
	private static final String JSON_LINE_TYPE = "lineType";
	private static final String JSON_IS_FINISHED = "finished";
	private static final String JSON_ELAPSED_TIME = "elapsedTime";
	private static final String JSON_SCORE = "score";
	public static final String JSON_SCORE_OURS = "ours";
	public static final String JSON_SCORE_THEIRS = "theirs";
	
	private Score score;
	private boolean isOline;
	private boolean isFinished;
	private boolean isAfterHalftime;
	private long elapsedSeconds;
	private Point previousPoint;
	
	public Score getScore() {
		return score;
	}
	public void setScore(Score score) {
		this.score = score;
	}
	public boolean isOline() {
		return isOline;
	}
	public void setOline(boolean isOline) {
		this.isOline = isOline;
	}
	public boolean isFinished() {
		return isFinished;
	}
	public void setFinished(boolean isFinished) {
		this.isFinished = isFinished;
	}
	public boolean isAfterHalftime() {
		return isAfterHalftime;
	}
	public void setAfterHalftime(boolean isAfterHalftime) {
		this.isAfterHalftime = isAfterHalftime;
	}
	public long getElapsedSeconds() {
		return elapsedSeconds;
	}
	public void setElapsedSeconds(long elapsedSeconds) {
		this.elapsedSeconds = elapsedSeconds;
	}
	public Point getPreviousPoint() {
		return previousPoint;
	}
	public void setPreviousPoint(Point previousPoint) {
		this.previousPoint = previousPoint;
	}
	@Override
	public String toString() {
		return "PointSummary [score=" + score + ", isOline=" + isOline
				+ ", isFinished=" + isFinished + ", isAfterHalftime="
				+ isAfterHalftime + ", elapsedSeconds=" + elapsedSeconds
				+ ", previousPoint=" + previousPoint + "]";
	}
	
	public JSONObject toJsonObject() throws JSONException {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put(JSON_LINE_TYPE, isOline ? "O" : "D");
		jsonObject.put(JSON_IS_FINISHED, isFinished);
		jsonObject.put(JSON_ELAPSED_TIME, elapsedSeconds);
		if (getScore() != null) {
			JSONObject scoreAsJson = new JSONObject();
			scoreAsJson.put(JSON_SCORE_OURS, getScore().getOurs());
			scoreAsJson.put(JSON_SCORE_THEIRS, getScore().getTheirs());		
			jsonObject.put(JSON_SCORE, scoreAsJson);
		}
		return jsonObject;
	}
	
	public static PointSummary fromJsonObject(JSONObject jsonObject) throws JSONException {
		if (jsonObject == null) {
			return null;
		} else {
			PointSummary pointSummary = new PointSummary();
			if (jsonObject.has(JSON_LINE_TYPE)) {
				pointSummary.isOline = jsonObject.getString(JSON_LINE_TYPE).equals("O");
			}
			if (jsonObject.has(JSON_IS_FINISHED)) {
				pointSummary.isFinished = jsonObject.getBoolean(JSON_IS_FINISHED);
			}
			if (jsonObject.has(JSON_ELAPSED_TIME)) {
				pointSummary.elapsedSeconds = jsonObject.getInt(JSON_ELAPSED_TIME);
			}
			if (jsonObject.has(JSON_SCORE)) {
				JSONObject scoreAsJson = jsonObject.getJSONObject(JSON_SCORE);
				int ourScore = scoreAsJson.getInt(JSON_SCORE_OURS);
				int theirScore = scoreAsJson.getInt(JSON_SCORE_THEIRS);				
				pointSummary.score = new Score(ourScore, theirScore);
			}			

			return pointSummary;
		}
	}

}
