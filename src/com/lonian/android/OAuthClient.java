package com.lonian.android;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;
import oauth.signpost.signature.HmacSha1MessageSigner;

public class OAuthClient {
	protected static final String REQUEST_TOKEN_ENDPOINT_URL = "http://api.lonian.com/v1/oauth/request_token"; 
	protected static final String ACCESS_TOKEN_ENDPOINT_URL  = "http://api.lonian.com/v1/oauth/access_token"; 
	protected static final String AUTHORIZE_ENDPOINT_URL     = "http://lonian.com/oauth/authorize"; 
	protected static final String CONSUMER_KEY    = "300e1b1ef27001e9c6a5ba91a49aca6704b8e5ee1";
	protected static final String CONSUMER_SECRET = "3a8598219d8f4348fa3520a09dd0dbd1";
	protected static final String CALLBACK_URL    = "lonian://oauth-callback";
	protected static OAuthConsumer consumer = null;
	protected static OAuthProvider provider = null;
	private static final String TAG = "com.lonian.android.OAuthClient"; 
	
	protected static void initOAuthObjects() {
		if (consumer == null) {
			consumer = new CommonsHttpOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);
			consumer.setMessageSigner(new HmacSha1MessageSigner());
		}
		if (provider == null) {
			// create a new service provider object and configure it with
			// the URLs which provide request tokens, access tokens, and
			// the URL to which users are sent in order to grant permission
			// to your application to access protected resources
			provider = new CommonsHttpOAuthProvider(
					REQUEST_TOKEN_ENDPOINT_URL, ACCESS_TOKEN_ENDPOINT_URL,
					AUTHORIZE_ENDPOINT_URL);
		}
	}
	
	public static String getAuthorizeUrl() throws OAuthMessageSignerException, OAuthNotAuthorizedException, OAuthExpectationFailedException, OAuthCommunicationException {
		initOAuthObjects();
		
		// fetches a request token from the service provider and builds
		// a url based on AUTHORIZE_WEBSITE_URL and CALLBACK_URL to
		// which your app must now send the user
		return provider.retrieveRequestToken(consumer, CALLBACK_URL);
	}
	
	public static void setVerifier(String verificationCode) throws OAuthMessageSignerException, OAuthNotAuthorizedException, OAuthExpectationFailedException, OAuthCommunicationException {
		initOAuthObjects();
		
        provider.retrieveAccessToken(consumer, verificationCode);
        Log.d(TAG, "Token: " + consumer.getToken());
        Log.d(TAG, "Secret: " + consumer.getTokenSecret());
	}
	
	public static void sendToAuthorize(Activity parentActivity) {
		Log.d(TAG, "Sending to authorize");
		Log.d(TAG, "Creating Intent");
		Intent i = new Intent(parentActivity, WebAuthActivity.class);
		try {
			Log.d(TAG, "Authorize URL");
			i.putExtra(WebAuthActivity.AUTH_URL, getAuthorizeUrl());
			Log.d(TAG, "Starting activity");
			parentActivity.startActivity(i);
		} catch (OAuthMessageSignerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OAuthNotAuthorizedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OAuthExpectationFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OAuthCommunicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
