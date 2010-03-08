package com.lonian.android.api;

import java.io.IOException;

import oauth.signpost.exception.OAuthException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TwitterAPI extends APIClient {
	static public JSONArray getAccounts() {
		JSONArray result = null;
		try {
			JSONObject response = requestToJSONObject(doGet(ENDPOINT_BASE+"twitter/accounts"));
			if (response.has("accounts")) {
				result = response.getJSONArray("accounts");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (OAuthException e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	static public JSONObject tweet(String account, String message) {
		JSONObject tweet = new JSONObject();
		JSONObject result = null;
		
		try {
			tweet.put("account", account);
			tweet.put("message", message);
			result = requestToJSONObject(doPost(ENDPOINT_BASE+"twitter/tweet", tweet.toString()));
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (OAuthException e) {
			e.printStackTrace();
		}
		return result;
	}
}
