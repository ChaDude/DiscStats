package com.summithillsoftware.ultimate.cloud;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.summithillsoftware.ultimate.model.Preferences;

public class UltimateJsonObjectRequest extends JsonObjectRequest {

	public UltimateJsonObjectRequest(int method, String url,
			JSONObject jsonRequest, Listener<JSONObject> listener,
			ErrorListener errorListener) {
		super(method, url, jsonRequest, listener, errorListener);
	}

	public UltimateJsonObjectRequest(String url, JSONObject jsonRequest,
			Listener<JSONObject> listener, ErrorListener errorListener) {
		super(url, jsonRequest, listener, errorListener);
	}

	
	@Override
	public Map<String, String> getHeaders() throws AuthFailureError {
		Map<String, String> headers = new HashMap<String, String>();
		String authCookie = Preferences.current().getCloudAuthenticationCookie();
		if (authCookie != null) {
			headers.put("Cookie", Preferences.current().getCloudAuthenticationCookie());
		}
		headers.put("Accept", "application/json");
		return headers;
	}
	
	// overridden to handle a blank response string from service
    @Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString =
                new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            if (jsonString.isEmpty()) {
            	
            } else {
            	return super.parseNetworkResponse(response);
            }
            return Response.success(null, HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } 
    }
}
