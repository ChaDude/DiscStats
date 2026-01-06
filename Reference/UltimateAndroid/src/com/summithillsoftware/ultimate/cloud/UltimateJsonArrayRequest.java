package com.summithillsoftware.ultimate.cloud;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;

import com.android.volley.AuthFailureError;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.JsonArrayRequest;
import com.summithillsoftware.ultimate.model.Preferences;

public class UltimateJsonArrayRequest extends JsonArrayRequest {

	public UltimateJsonArrayRequest(String url, Listener<JSONArray> listener, ErrorListener errorListener) {
		super(url, listener, errorListener);
	}

	@Override
	public Map<String, String> getHeaders() throws AuthFailureError {
		Map<String, String> headers = new HashMap<String, String>();
		String authCookie = Preferences.current().getCloudAuthenticationCookie();
		if (authCookie != null) {
			headers.put("Cookie", Preferences.current().getCloudAuthenticationCookie());
		}
		return headers;
	}
	
}

