package com.lonian.android.api;

import java.io.IOException;

import oauth.signpost.exception.OAuthException;

import org.json.JSONException;
import org.json.JSONObject;

public class UpdateAPI extends APIClient {
	protected static long mLastUpdateId = 0;
	
	static public long fetchLastUpdateId() throws JSONException, IOException, OAuthException {
		JSONObject result = requestToJSONObject(doGet(ENDPOINT_BASE+"last_known_seen"));
		
		if (result.has("id")) {
			mLastUpdateId = result.getLong("id");
		}
		return mLastUpdateId;
	}
	
	static public JSONObject getUpdates() {
		JSONObject result = null;
		try {
			if (mLastUpdateId == 0) {
				fetchLastUpdateId();
			}
			result = requestToJSONObject(doGet(ENDPOINT_BASE+"updates?since="+mLastUpdateId));
			if (result.has("meta")) {
				JSONObject meta = result.getJSONObject("meta");
				if (meta.has("user")) {
					JSONObject user = meta.getJSONObject("user");
					if (user.has("last_seen_update_id")) {
						mLastUpdateId = user.getLong("last_seen_update_id");
					}
				}
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
	

}
