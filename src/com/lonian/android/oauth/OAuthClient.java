package com.lonian.android.oauth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;
import oauth.signpost.exception.OAuthException;
import oauth.signpost.signature.HmacSha1MessageSigner;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

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
		return consumer;
	}
	
	public OAuthProvider getProvider() {
		return provider;
	}
	
	void restoreTokens(OAuthConsumer consumer) {
		String[] tokens = getOAuthUserTokens();
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

		return url;
	}
	
	public void verifyAccess(String verificationCode) {
        try {
			provider.retrieveAccessToken(consumer, verificationCode);
	        
	        setOAuthUserTokens(consumer.getToken(), consumer.getTokenSecret());
		} catch (OAuthException e) {
			e.printStackTrace();
		}
	}

	static String convertStreamToString(InputStream is) {
		/*
		 * To convert the InputStream to String we use the BufferedReader.readLine()
		 * method. We iterate until the BufferedReader return null which means
		 * there's no more data to read. Each line will appended to a StringBuilder
		 * and returned as String.
		 */
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return sb.toString();
	}
	
	public JSONObject makePost(String url, String body) throws IllegalStateException, IOException, OAuthException {
		return makePost(url, new StringEntity(body));
	}
	
	public JSONObject makePost(String url, HttpEntity body) throws IllegalStateException, IOException, OAuthException {
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(url);
		HttpResponse response;
		JSONObject result_object = null;

		httppost.setEntity(body);
		consumer.sign(httppost);
		Log.d(TAG, "POSTing to "+url);
		response = httpclient.execute(httppost);
		Log.d(TAG, "Status:[" + response.getStatusLine().toString() + "]");
		HttpEntity entity = response.getEntity();

		if (entity != null) {
			InputStream instream = entity.getContent();
			String result = convertStreamToString(instream);
			Log.d(TAG, "Result of converstion: [" + result + "]");

			instream.close();
			try {
				result_object = new JSONObject(result);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return result_object;
	}

	public JSONObject makeRequest(String url) throws IllegalStateException, IOException, OAuthException {
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet(url);
		HttpResponse response;
		JSONObject result_object = null;

		consumer.sign(httpget);
		Log.d(TAG, "GETting from "+url);
		response = httpclient.execute(httpget);
		Log.d(TAG, "Status:[" + response.getStatusLine().toString() + "]");
		HttpEntity entity = response.getEntity();

		if (entity != null) {
			InputStream instream = entity.getContent();
			String result = convertStreamToString(instream);
			Log.d(TAG, "Result of converstion: [" + result + "]");

			instream.close();
			try {
				result_object = new JSONObject(result);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return result_object;
	}
}