package com.lonian.android.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import oauth.signpost.exception.OAuthException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import com.lonian.android.oauth.OAuthClient;

import android.util.Log;

abstract class APIClient {
	static final String ENDPOINT_BASE = "http://api.lonian.com/v1/";
	static final String TAG = "com.lonian.android.APIClient";
	static HttpClient httpClient = new DefaultHttpClient();
	
	protected APIClient() {
	}
	
	protected static String convertStreamToString(InputStream stream) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
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
				stream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return sb.toString();
	}
	
	protected static JSONObject requestToJSONObject(HttpResponse response) throws JSONException, IOException {
		return requestToJSONObject(requestToString(response));
	}

	protected static JSONObject requestToJSONObject(String response) throws JSONException {
		JSONObject result_object = null;

		if (response != null) {
			result_object = new JSONObject(response);
		}

		return result_object;
	}

	protected static String requestToString(HttpResponse response) throws IOException {
		HttpEntity entity = response.getEntity();
		String result = null;

		if (entity != null) {
			InputStream instream = entity.getContent();
			result = convertStreamToString(instream);
			Log.d(TAG, "Result of converstion: [" + result + "]");

			instream.close();
		}

		return result;
	}

	protected static HttpResponse doGet(String url) throws IOException, OAuthException {
		HttpGet httpGet = new HttpGet(url);
		HttpResponse response;

		OAuthClient.getInstance().sign(httpGet);
		Log.d(TAG, "POSTing to "+url);
		response = httpClient.execute(httpGet);
		Log.d(TAG, "Status:[" + response.getStatusLine().toString() + "]");
		return response;
	}

	protected static HttpResponse doPost(String url, String requestBody) throws IOException, OAuthException {
		return doPost(url, new StringEntity(requestBody));
	}
	
	protected static HttpResponse doPost(String url, HttpEntity requestBody) throws IOException, OAuthException {
		HttpPost httpPost = new HttpPost(url);
		HttpResponse response;

		httpPost.setEntity(requestBody);
		OAuthClient.getInstance().sign(httpPost);
		Log.d(TAG, "POSTing to "+url);
		response = httpClient.execute(httpPost);
		Log.d(TAG, "Status:[" + response.getStatusLine().toString() + "]");
		return response;
	}

	protected static HttpResponse doPut(String url, String requestBody) throws IOException, OAuthException {
		return doPut(url, new StringEntity(requestBody));
	}
	
	protected static HttpResponse doPut(String url, HttpEntity requestBody) throws IOException, OAuthException {
		HttpPut httpPut = new HttpPut(url);
		HttpResponse response;

		httpPut.setEntity(requestBody);
		OAuthClient.getInstance().sign(httpPut);
		Log.d(TAG, "PUTting to "+url);
		response = httpClient.execute(httpPut);
		Log.d(TAG, "Status:[" + response.getStatusLine().toString() + "]");
		return response;
	}

	protected static HttpResponse doDelete(String url) throws IOException, OAuthException {	
		HttpDelete httpDelete = new HttpDelete(url);
		HttpResponse response;

		OAuthClient.getInstance().sign(httpDelete);
		Log.d(TAG, "POSTing to "+url);
		response = httpClient.execute(httpDelete);
		Log.d(TAG, "Status:[" + response.getStatusLine().toString() + "]");
		return response;
	}
}