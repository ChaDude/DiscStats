package com.summithillsoftware.ultimate.twitter;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import android.net.Uri;

import com.summithillsoftware.ultimate.cloud.CloudClient;
import com.summithillsoftware.ultimate.model.Preferences;
import com.summithillsoftware.ultimate.util.UltimateLogger;

/*
	Application settings
	
	Your application's API keys are used to authenticate requests to the Twitter Platform.
	Access level: Read, write, and direct messages
	Callback URL: http://www.ultimate-numbers.com/rest/view/twitter-oauth-callback
	Sign in with Twitter: No
	App-only authentication: https://api.twitter.com/oauth2/token
	Request token URL: https://api.twitter.com/oauth/request_token
	Authorize URL: https://api.twitter.com/oauth/authorize
	Access token URL: https://api.twitter.com/oauth/access_token
	
	API key: 60ShZrkvkw28IqELlstXXA
	API secret: QXabIEDdPW1HZC3hm3JkOVpD8vhXLbO5kKOjakbKivg
	Owner: velohomme_shs
	Owner ID: 2351746723

 */

public class TwitterClient {
	public static final String TWITTER_CALLBACK_PATH = "/twitter-oauth-callback.jsp";
	public static final String TWITTER_CALLBACK_URL = CloudClient.SCHEME_HOST + TWITTER_CALLBACK_PATH;
	public static final String TWITTER_API_CONSUMER_KEY = "60ShZrkvkw28IqELlstXXA";
	public static final String TWITTER_API_CONSUMER_SECRET = "QXabIEDdPW1HZC3hm3JkOVpD8vhXLbO5kKOjakbKivg";
	public static final String APP_ONLY_AUTH_URL = "https://api.twitter.com/oauth2/token";
	public static final String REQUEST_TOKEN_URL = "https://api.twitter.com/oauth/request_token";
	public static final String AUTHORIZE_URL = "https://api.twitter.com/oauth2/token";
	public static final String ACCESS_TOKEN_URL = "https://api.twitter.com/oauth/access_token";
	
	// Twitter oauth urls
    static final String URL_TWITTER_AUTH = "auth_url";
    static final String URL_TWITTER_OAUTH_VERIFIER = "oauth_verifier";
    static final String URL_TWITTER_OAUTH_TOKEN = "oauth_token";
	
	private static TwitterClient Current;
	
	private Twitter twitter;
	private RequestToken requestToken;
	
	static {
		Current = new TwitterClient();
	}
	
	private TwitterClient() {

	}
	
	public static TwitterClient current() {
		return Current;
	}
	
	public boolean isSignedIn() {
		String moniker = getTwitterMoniker();
		return moniker != null && !moniker.isEmpty();
	}
	
	public boolean isAuthenticationCallbackUrl(String url) {
		return url.contains(TWITTER_CALLBACK_PATH);
	}
	
	public boolean isAuthenticationCancelledCallbackUrl(String url) {
		if (isAuthenticationCallbackUrl(url)) {
			Uri uri =  Uri.parse(url);
			return uri.getQueryParameter(URL_TWITTER_OAUTH_VERIFIER) == null;
		}
		return false;
	}
	
	// IMPORTANT: Do not call on UI thread
	public String getAuthenticationURL() {
	    TwitterFactory factory = new TwitterFactory(getTwitterConfiguration());
	    Twitter twitter = factory.getInstance();
	    try {
	    	requestToken = twitter.getOAuthRequestToken(TWITTER_CALLBACK_URL);
	    	return requestToken.getAuthenticationURL();
	    } catch (TwitterException e) {
            // Error in updating status
            UltimateLogger.logError("Twitter oAuth failure...unable to get authentication request token for the app", e);
            return null;
        }
	}
	
	// IMPORTANT: Do not call on UI thread
	public boolean setTwitterCredentialsFromCallbackUrl(String callbackUrl) {
		AccessToken accessToken;
		try {
			Uri uri =  Uri.parse(callbackUrl);
			String verifier = uri.getQueryParameter(URL_TWITTER_OAUTH_VERIFIER);
			accessToken = getTwitter().getOAuthAccessToken(requestToken, verifier);
		} catch (TwitterException e) {
			UltimateLogger.logError("Twitter error: unable to getOAuthAccessToken", e);
			return false;
		}
		try {
			setTwitterCredentials(accessToken.getToken(), accessToken.getTokenSecret(), twitter.getScreenName());
			return true;
		} catch (Exception e) {
			UltimateLogger.logError("Twitter error: could not glean token and secret from twitter response", e);
			requestToken = null;
			return false;
		}
	}
	
	// IMPORTANT: Do not call on UI thread
	public void tweet(Tweet tweet) {
		Twitter twitter = getAuthenticatedTwitter();
		if (twitter == null) {
			tweet.setCompletionStatus(TweetSendStatus.RejectedBadCredentials);
		} else {
			try {
				Status status = getAuthenticatedTwitter().updateStatus(tweet.getText());
				tweet.setCompletionStatus(TweetSendStatus.OK);
				tweet.setLimitStatus(status.getRateLimitStatus());
			} catch (TwitterException e) {
				if (e.getStatusCode() == 403) {
					if (e.getErrorCode() == 187) {
						tweet.setCompletionStatus(TweetSendStatus.RejectedRetweet);
					} else if (e.getErrorCode() == 88 || e.getErrorCode() == 185) {
						tweet.setCompletionStatus(TweetSendStatus.RejectedRateLimitExceeded);
					} else {
						tweet.setCompletionStatus(TweetSendStatus.RejectedForUnkownReason);
					}
				} else if (e.getStatusCode() == 429) {
					tweet.setCompletionStatus(TweetSendStatus.RejectedRateLimitExceeded);				
				} else if (e.getStatusCode() == 401) {
					tweet.setCompletionStatus(TweetSendStatus.RejectedBadCredentials);
				} else if (e.getStatusCode() == 401) {
					tweet.setCompletionStatus(TweetSendStatus.UnknownError);
				}
				UltimateLogger.logError("Tweet failed" + tweet , e);
			} catch (Exception e) {
				UltimateLogger.logError("Error trying to tweet", e);
				tweet.setCompletionStatus(TweetSendStatus.UnknownError);
			}
		}
	}
    
	public void clearTwitterCredentials() {
		Preferences.current().setTwitterOAuthUserAccessToken(null);
		Preferences.current().setTwitterOAuthUserAccessTokenSecret(null);
		Preferences.current().setTwitterMoniker(null);
		Preferences.current().save();
		UltimateLogger.logInfo("Twitter credentials cleared for this user");
	}
	
	private void setTwitterCredentials(String twitterOAuthUserAccessToken, String twitterOAuthUserAccessTokenSecret, String moniker) {
		Preferences.current().setTwitterOAuthUserAccessToken(twitterOAuthUserAccessToken);
		Preferences.current().setTwitterOAuthUserAccessTokenSecret(twitterOAuthUserAccessTokenSecret);
		Preferences.current().setTwitterMoniker(moniker);
		Preferences.current().save();
		UltimateLogger.logInfo("Twitter credentials saved for this user");
	}

	public String getTwitterMoniker() {
		return Preferences.current().getTwitterMoniker();
	}
	
	public void setTwitterMoniker(String twitterMoniker) {
		Preferences.current().setTwitterMoniker(twitterMoniker);
		Preferences.current().save();
	}
	
	private String getTwitterOAuthUserAccessTokenSecret() {
		return Preferences.current().getTwitterOAuthUserAccessTokenSecret();
	}
	
	private String getTwitterOAuthUserAccessToken() {
		return Preferences.current().getTwitterOAuthUserAccessToken();
	}
	
	private Twitter getTwitter() {
		if (twitter == null) {
			TwitterFactory factory = new TwitterFactory(getTwitterConfiguration());
			twitter = factory.getInstance();
		}
		return twitter;
	}
	
	private Twitter getAuthenticatedTwitter() {
	    Twitter authenticatedTwitter;
		try {
			authenticatedTwitter = getTwitter();
			AccessToken accessToken = new AccessToken(getTwitterOAuthUserAccessToken(), getTwitterOAuthUserAccessTokenSecret());
			authenticatedTwitter.setOAuthAccessToken(accessToken);
			return authenticatedTwitter;
		} catch (Exception e) {
			UltimateLogger.logError("Unable to get authentication data to tweet", e);
			return null;
		}
	}
	
	private Configuration getTwitterConfiguration() {
		ConfigurationBuilder builder = new ConfigurationBuilder();	
		
	    builder.setOAuthConsumerKey(TWITTER_API_CONSUMER_KEY);
	    builder.setOAuthConsumerSecret(TWITTER_API_CONSUMER_SECRET);
	    Configuration configuration = builder.build();
	    
	    return configuration;
	}

}
