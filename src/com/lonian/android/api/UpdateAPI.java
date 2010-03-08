package com.lonian.android.api;

import java.io.IOException;

import oauth.signpost.exception.OAuthException;

import org.json.JSONException;
import org.json.JSONObject;

public class UpdateAPI extends APIClient {
	static public JSONObject getUpdates() {
		JSONObject result = null;
		try {
			result = requestToJSONObject(doGet(ENDPOINT_BASE+"updates"));
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (OAuthException e) {
			e.printStackTrace();
		}
		
		return result;
	}
	

}
