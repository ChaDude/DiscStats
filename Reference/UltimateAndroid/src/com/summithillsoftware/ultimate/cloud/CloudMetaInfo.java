package com.summithillsoftware.ultimate.cloud;

import org.json.JSONException;
import org.json.JSONObject;

public class CloudMetaInfo {
	private static final String JSON_IS_VERSION_ACCEPTABLE = "appVersionAcceptable";
	private static final String JSON_MESSAGE_TO_USER = "messageToUser";
	private boolean isAppVersionAcceptable;
	private String messageToUser;
	
	public boolean isAppVersionAcceptable() {
		return isAppVersionAcceptable;
	}

	public String getMessageToUser() {
		return messageToUser;
	}

	
	public static CloudMetaInfo fromJsonObject(JSONObject jsonObject) throws JSONException {
		if (jsonObject == null) {
			return null;
		} else {
			CloudMetaInfo metaInfo = new CloudMetaInfo();

			if (jsonObject.has(JSON_IS_VERSION_ACCEPTABLE)) {
				metaInfo.isAppVersionAcceptable = jsonObject.getBoolean(JSON_IS_VERSION_ACCEPTABLE);
			}
			
			if (jsonObject.has(JSON_MESSAGE_TO_USER)) {
				metaInfo.messageToUser = jsonObject.getString(JSON_MESSAGE_TO_USER);
			}

			return metaInfo;
		}
	}

}
