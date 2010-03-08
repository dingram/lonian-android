package com.lonian.android.oauth;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;
import oauth.signpost.exception.OAuthException;
import oauth.signpost.http.HttpRequest;
import oauth.signpost.signature.HmacSha1MessageSigner;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class OAuthClient {
	private static final String TAG = "com.lonian.android.OAuthClient"; 
	static final String REQUEST_TOKEN_ENDPOINT_URL = "http://api.lonian.com/v1/oauth/request_token"; 
	static final String ACCESS_TOKEN_ENDPOINT_URL  = "http://api.lonian.com/v1/oauth/access_token"; 
	static final String AUTHORIZE_ENDPOINT_URL     = "http://lonian.com/oauth/authorize"; 
	static final String CONSUMER_KEY    = "300e1b1ef27001e9c6a5ba91a49aca6704b8e5ee1";
	static final String CONSUMER_SECRET = "3a8598219d8f4348fa3520a09dd0dbd1";
	static final String CALLBACK_URL    = "lonian://oauth-callback";
	protected static final String OPT_OAUTH_R_TOKEN  = "oauth.request_token";
	protected static final String OPT_OAUTH_R_SECRET = "oauth.request_secret";
	protected static final String OPT_OAUTH_U_TOKEN  = "oauth.user_token";
	protected static final String OPT_OAUTH_U_SECRET = "oauth.user_secret";

	protected OAuthConsumer consumer = null;
	protected OAuthProvider provider = null;
	protected Context context = null;
	protected SharedPreferences prefs = null;
	
	protected static OAuthClient instance = null;

	protected OAuthClient(Context context) {
		this.context = context;
		consumer = new CommonsHttpOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);
		consumer.setMessageSigner(new HmacSha1MessageSigner());
		provider = new CommonsHttpOAuthProvider(
			REQUEST_TOKEN_ENDPOINT_URL,
			ACCESS_TOKEN_ENDPOINT_URL,
			AUTHORIZE_ENDPOINT_URL
		);
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
		if (isAuthorized()) {
			restoreTokens(consumer);
		}
	}
	
	public static OAuthClient getInstance() {
		return instance;
	}
	
	public static OAuthClient getInstance(Context context) {
		if (instance == null) {
			instance = new OAuthClient(context);
		}
		return instance;
	}
	
	public OAuthConsumer getConsumer() {
		Log.v(TAG, "getConsumer()");
		return consumer;
	}
	
	public OAuthProvider getProvider() {
		Log.v(TAG, "getProvider()");
		return provider;
	}
	
	void restoreTokens(OAuthConsumer consumer) {
		String[] tokens = getOAuthUserTokens();
		Log.v(TAG, "Restoring tokens: " + tokens[0] + " and " + tokens[1]);
		consumer.setTokenWithSecret(tokens[0], tokens[1]);
	}

	public String[] getOAuthUserTokens() {
		String token  = prefs.getString(OPT_OAUTH_U_TOKEN,  null);
		String secret = prefs.getString(OPT_OAUTH_U_SECRET, null);
		return new String[] { token, secret };
	}

	public boolean isAuthorized() {
		String[] tokens = getOAuthUserTokens();
		return (tokens[0] != null && tokens[1] != null);
	}
	
	public void deauth() {
		Log.v(TAG, "Deauth request");
		setOAuthUserTokens(null, null);
	}

	void setOAuthUserTokens(String token, String secret) {
		prefs.edit()
			.putString(OPT_OAUTH_U_TOKEN,  token )
			.putString(OPT_OAUTH_U_SECRET, secret)
			.commit();
	}

	public String getAuthorizeUrl() {
		// fetches a request token from the service provider and builds
		// a url based on AUTHORIZE_WEBSITE_URL and CALLBACK_URL to
		// which your app must now send the user
		String url = null;

		try {
			url = provider.retrieveRequestToken(consumer, CALLBACK_URL);
		} catch (OAuthException e) {
			e.printStackTrace();
		}

		Log.v(TAG, "Auth URL: "+url);
		return url;
	}
	
	public void verifyAccess(String verificationCode) {
        try {
    		Log.v(TAG, "Retrieving access token");
			provider.retrieveAccessToken(consumer, verificationCode);
	        
	        setOAuthUserTokens(consumer.getToken(), consumer.getTokenSecret());
		} catch (OAuthException e) {
			e.printStackTrace();
		}
	}

	public HttpRequest sign(HttpRequest request) throws OAuthException {
		Log.v(TAG, "Signing request");
		return consumer.sign(request);
	}

	public HttpRequest sign(Object request) throws OAuthException {
		Log.v(TAG, "Signing request");
		return consumer.sign(request);
	}

	public String sign(String request) throws OAuthException {
		Log.v(TAG, "Signing request");
		return consumer.sign(request);
	}

}