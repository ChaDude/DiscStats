package com.summithillsoftware.ultimate.cloud;

import static com.android.volley.DefaultRetryPolicy.DEFAULT_BACKOFF_MULT;
import static com.android.volley.DefaultRetryPolicy.DEFAULT_MAX_RETRIES;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.webkit.CookieManager;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.summithillsoftware.ultimate.UltimateApplication;
import com.summithillsoftware.ultimate.model.Game;
import com.summithillsoftware.ultimate.model.GameDescription;
import com.summithillsoftware.ultimate.model.Preferences;
import com.summithillsoftware.ultimate.model.Team;
import com.summithillsoftware.ultimate.util.UltimateLogger;


public class CloudClient {
	public static final String HOST = "www.ultianalytics.com";
//	private static final String HOST = "local.appspot.com:8888";
//	private static final String HOST = "local.appspot.com:8890"; // tcp monitor
	public static final String SCHEME_HOST = "http://" + HOST;
	public static final String ADMIN_URL = SCHEME_HOST + "/team/admin";
	private static final String JSON_TEAM_CLOUD_ID = "cloudId";
	private static final String JSON_TEAM_ID = "teamId";
	private static final int SOCKET_TIMEOUT_SECONDS = 45;
	
	private static CloudClient Current;
	
	private RequestQueue queue;
	
	static {
		Current = new CloudClient();
	}
	
	private CloudClient() {
		queue = Volley.newRequestQueue(UltimateApplication.current());
	}
	
	public static CloudClient current() {
		return Current;
	}
	
	public void clearExistingAuthentication() {
		Preferences.current().setCloudAuthenticationCookie(null);
		Preferences.current().setCloudEMail(null);
	    CookieManager cookieManager = CookieManager.getInstance();
	    cookieManager.removeAllCookie();	
	    cookieManager.removeSessionCookie();
	}
	
	public String websiteUrlForCloudId(String cloudId) {
		return SCHEME_HOST + "/team/" + cloudId + "/main";
	}
	
	public void submitRetrieveTeams(final CloudResponseHandler responseHandler) {
		UltimateJsonArrayRequest request = new UltimateJsonArrayRequest(getUrl("/rest/mobile/teams"), new Response.Listener<JSONArray>() {
			@Override
			public void onResponse(final JSONArray jsonArray) {
				List<Team> teams = new  ArrayList<Team>();
				try {
					for (int i = 0; i < jsonArray.length(); i++) {
						JSONObject teamJson = jsonArray.getJSONObject(i);
						Team team = Team.fromJsonObject(teamJson);
						teams.add(team);
					}
					responseHandler.onResponse(CloudResponseStatus.Ok, teams);
				} catch (JSONException e) {
					UltimateLogger.logError( "Unable to convert JSON teams to Team objects", e);
					responseHandler.onResponse(CloudResponseStatus.MarshallingError, null);
				}
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				handleError(responseHandler, error);
			}
		});
		
		addRequestToQueue(request, responseHandler);
	}
	
	public void submitRetrieveGameDescriptions(final String teamCloudId, final CloudResponseHandler responseHandler) {
		UltimateJsonArrayRequest request = new UltimateJsonArrayRequest(getUrl("/rest/mobile/team/" + teamCloudId + "/games"), new Response.Listener<JSONArray>() {
			@Override
			public void onResponse(final JSONArray jsonArray) {
				List<GameDescription> games = new  ArrayList<GameDescription>();
				try {
					for (int i = 0; i < jsonArray.length(); i++) {
						JSONObject gameDescriptionJson = jsonArray.getJSONObject(i);
						GameDescription gameDescription = GameDescription.fromJsonObject(gameDescriptionJson);
						games.add(gameDescription);
					}
					responseHandler.onResponse(CloudResponseStatus.Ok, games);
				} catch (JSONException e) {
					UltimateLogger.logError( "Unable to convert JSON game descriptions to GameDescription objects", e);
					responseHandler.onResponse(CloudResponseStatus.MarshallingError, null);
				}
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				handleError(responseHandler, error);
			}
		});
		
		addRequestToQueue(request, responseHandler);
	}
	
	public void submitRetrieveTeam(final String cloudId, final CloudResponseHandler responseHandler) {
		UltimateJsonObjectRequest request = new UltimateJsonObjectRequest(Method.GET, getUrl("/rest/mobile/team/" + cloudId + "?players=true"), null, new Response.Listener<JSONObject>() {
			@Override
			public void onResponse(final JSONObject responseObject) {
				try {
					Team team = Team.fromJsonObject(responseObject);
					responseHandler.onResponse(CloudResponseStatus.Ok, team);
				} catch (Exception e) {
					UltimateLogger.logError( "Unable to convert JSON team to Team object", e);
					responseHandler.onResponse(CloudResponseStatus.MarshallingError, null);
				}
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				handleError(responseHandler, error);
			}
		});
		
		addRequestToQueue(request, responseHandler);
	}
	
	public void submitRetrieveGame(final String teamCloudId, final String gameId, final CloudResponseHandler responseHandler) {
		UltimateJsonObjectRequest request = new UltimateJsonObjectRequest(Method.GET, getUrl("/rest/mobile/team/" + teamCloudId + "/game/" + gameId), null, new Response.Listener<JSONObject>() {
			@Override
			public void onResponse(final JSONObject responseObject) {
				try {
					Game game = Game.fromJsonObject(responseObject);
					responseHandler.onResponse(CloudResponseStatus.Ok, game);
				} catch (Exception e) {
					UltimateLogger.logError( "Unable to convert JSON game to Game object", e);
					responseHandler.onResponse(CloudResponseStatus.MarshallingError, null);
				}
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				handleError(responseHandler, error);
			}
		});
		
		addRequestToQueue(request, responseHandler);
	}
	
	
	// response is the team's cloud id
	public void submitUploadTeam(final Team team, final CloudResponseHandler responseHandler) {
		JSONObject teamAsJson = null;
		try {
			teamAsJson = team.toJsonObject();
		} catch (Exception e1) {
			UltimateLogger.logError( "Unable to convert Team to JSON Object", e1);
			responseHandler.onResponse(CloudResponseStatus.MarshallingError, null);
		}
		if (teamAsJson != null) {
			UltimateJsonObjectRequest request = new UltimateJsonObjectRequest(Method.POST, getUrl("/rest/mobile/team"), teamAsJson, new Response.Listener<JSONObject>() {
				@Override
				public void onResponse(final JSONObject responseObject) {
					try {
						String cloudId = responseObject.getString(JSON_TEAM_CLOUD_ID);
						responseHandler.onResponse(CloudResponseStatus.Ok, cloudId);
					} catch (JSONException e) {
						UltimateLogger.logError( "Unable to get cloud id from upload response", e);
						responseHandler.onResponse(CloudResponseStatus.MarshallingError, null);
					}
				}
			}, new Response.ErrorListener() {
				@Override
				public void onErrorResponse(VolleyError error) {
					handleError(responseHandler, error);
				}
			});
			addRequestToQueue(request, responseHandler);
		}
	}
	
	public void submitUploadGame(final Game game, final Team team, final CloudResponseHandler responseHandler) {
		JSONObject gameAsJson = null;
		try {
			gameAsJson = game.toJsonObject();
			gameAsJson.put(JSON_TEAM_ID, team.getCloudId());
		} catch (Exception e1) {
			UltimateLogger.logError( "Unable to convert Game to JSON Object", e1);
			responseHandler.onResponse(CloudResponseStatus.MarshallingError, null);
		}
		if (gameAsJson != null) {
			UltimateJsonObjectRequest request = new UltimateJsonObjectRequest(Method.POST, getUrl("/rest/mobile/game"), gameAsJson, new Response.Listener<JSONObject>() {
				@Override
				public void onResponse(final JSONObject responseObject) {
					responseHandler.onResponse(CloudResponseStatus.Ok, null);
				}
			}, new Response.ErrorListener() {
				@Override
				public void onErrorResponse(VolleyError error) {
					handleError(responseHandler, error);
				}
			});
			addRequestToQueue(request, responseHandler);
		}
	}
	
	public void submitVersionCheck(final String appVersion, final CloudResponseHandler responseHandler) {
		String urlSafeAppVersion = appVersion.replace(".", "_");
		UltimateJsonObjectRequest request = new UltimateJsonObjectRequest(Method.GET, getUrl("/rest/mobile/meta/" + urlSafeAppVersion + "?mobile-type=android"), null, new Response.Listener<JSONObject>() {
			@Override
			public void onResponse(final JSONObject responseObject) {
				try {
					CloudMetaInfo metaInfo = CloudMetaInfo.fromJsonObject(responseObject);
					responseHandler.onResponse(CloudResponseStatus.Ok, metaInfo);
				} catch (Exception e) {
					UltimateLogger.logError( "Unable to convert JSON team to CloudMetaInfo object", e);
					responseHandler.onResponse(CloudResponseStatus.MarshallingError, null);
				}
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				handleError(responseHandler, error);
			}
		});
		
		addRequestToQueue(request, responseHandler);
	}
	
	private String getUrl(String relativePath) {
		String fullUrl = SCHEME_HOST + relativePath;
		// add the app version to every request (?ulti_version=android_3_0_0)
		String appVersion = UltimateApplication.current().getAppVersion();
		String urlSafeAppVersion = appVersion.replace(".", "_");
		String appVersionQueryStringParam = "ulti_version=android_" + urlSafeAppVersion;
		String fullUrlWithVersion = fullUrl + (relativePath.contains("?") ? 
					("&" + appVersionQueryStringParam) : 
					("?" + appVersionQueryStringParam));
		return fullUrlWithVersion;
	}
	
	private void handleError(final CloudResponseHandler responseHandler, final VolleyError error) {
		CloudResponseStatus errorStatus = interpretError(error);
		String errorMessage = "Cloud error" + errorStatus;
		if (error.networkResponse != null) {
			errorMessage += " http status=" + error.networkResponse.statusCode;
			errorMessage += " more..." + error.networkResponse.toString();
		}
		UltimateLogger.logError( errorMessage, error);
		responseHandler.onResponse(errorStatus, null);
	}
	
	private CloudResponseStatus interpretError(VolleyError error) {
		if (!isConnected()) {
			return CloudResponseStatus.NotConnectedToInternet;
		} else if (error instanceof TimeoutError) {
			return CloudResponseStatus.Timeout;
		} else if (error instanceof ParseError) {
			return CloudResponseStatus.MarshallingError;
		} else if (error instanceof TimeoutError) {
			return CloudResponseStatus.Timeout;
		} else if (error.networkResponse != null && error.networkResponse.statusCode == 401) {
			return CloudResponseStatus.Unauthorized;
		}
		return CloudResponseStatus.Unknown;
	}
	
	public boolean isConnected() {
		ConnectivityManager cm = (ConnectivityManager) UltimateApplication.current().getSystemService(Context.CONNECTIVITY_SERVICE); 
		NetworkInfo networkInfo = cm.getActiveNetworkInfo();  
		// if no network is available then networkInfo will be null
		if (networkInfo == null) { 
			 return false;
		} 
		return networkInfo.isConnected();
	}
	
	private void addRequestToQueue(Request<?> request, CloudResponseHandler handler) {
		request.setRetryPolicy(new DefaultRetryPolicy(SOCKET_TIMEOUT_SECONDS * 1000, DEFAULT_MAX_RETRIES, DEFAULT_BACKOFF_MULT));
		if (isConnected()) {
			queue.add(request);
		} else {
			handler.onResponse(CloudResponseStatus.NotConnectedToInternet, null);
		}
	}

}
