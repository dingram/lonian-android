package com.lonian.android;

import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;

import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {
	static final int MENU_QUIT     = 1;
	static final int MENU_SETTINGS = 2;
	static final String TAG = "com.lonian.android.MainActivity";

	static final String OPT_OAUTH_R_TOKEN  = "oauth.request_token";
	static final String OPT_OAUTH_R_SECRET = "oauth.request_secret";
	static final String OPT_OAUTH_U_TOKEN  = "oauth.user_token";
	static final String OPT_OAUTH_U_SECRET = "oauth.user_secret";

	String[] getOAuthRequestTokens() {
		String token  = getPreferences(MODE_PRIVATE).getString(OPT_OAUTH_R_TOKEN,  null);
		String secret = getPreferences(MODE_PRIVATE).getString(OPT_OAUTH_R_SECRET, null);
		return new String[] { token, secret };
	}

	void setOAuthRequestTokens(String token, String secret) {
		getPreferences(MODE_PRIVATE).edit()
		.putString(OPT_OAUTH_R_TOKEN,  token )
		.putString(OPT_OAUTH_R_SECRET, secret)
		.commit();
	}

	String[] getOAuthUserTokens() {
		String token  = getPreferences(MODE_PRIVATE).getString(OPT_OAUTH_U_TOKEN,  null);
		String secret = getPreferences(MODE_PRIVATE).getString(OPT_OAUTH_U_SECRET, null);
		return new String[] { token, secret };
	}

	void setOAuthUserTokens(String token, String secret) {
		getPreferences(MODE_PRIVATE).edit()
			.putString(OPT_OAUTH_U_TOKEN,  token )
			.putString(OPT_OAUTH_U_SECRET, secret)
			.commit();
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		Button button = (Button)findViewById(R.id.btn_updates);
		// Register the onClick listener with the implementation above
		button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				/*
                Vibrator vibrator =
                	(Vibrator)getSystemService(Context.VIBRATOR_SERVICE);

                vibrator.vibrate(50);
				 */

				String result = queryRESTurl("http://api.lonian.com/v1/updates");

				if (result != null) {
					try{
						JSONObject json_result = new JSONObject(result);
						JSONArray  json_errors = json_result.getJSONArray("errors");
						if (json_errors.length() > 0) {
							StringBuilder errors = new StringBuilder("Error: ");
							for (int i=0; i<json_errors.length(); ++i) {
								errors.append(json_errors.getString(i));
							}
							Toast.makeText(getBaseContext(), errors, 3000).show();
						} else {
							Toast.makeText(getBaseContext(), "There were updates", 3000).show();
						}
					}
					catch (JSONException e) {
						Log.e("JSON", "There was an error parsing the JSON", e);
					}
				}
			}
		});

		final Activity _this = this;

		button = (Button)findViewById(R.id.btn_auth);
		button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				OAuthClient.sendToAuthorize(_this);
			}
		});

		button = (Button)findViewById(R.id.btn_notify);
		button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
				int icon = R.drawable.notification_alert;
				CharSequence tickerText = "13 new updates available!";
				long when = System.currentTimeMillis();

				Notification notification = new Notification(icon, tickerText, when);
				Context context = getApplicationContext();
				CharSequence contentTitle = "Lonian";
				CharSequence contentText = "13 new updates since 09:52";
				Intent notificationIntent = new Intent(_this, MainActivity.class);
				PendingIntent contentIntent = PendingIntent.getActivity(_this, 0, notificationIntent, 0);

				notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
				final int HELLO_ID = 1;

				mNotificationManager.notify(HELLO_ID, notification);
			}
		});
	}

	@Override
	public void onResume() {
		// extract the OAUTH access token if it exists
		Uri uri = this.getIntent().getData();
		((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancelAll();
		if(uri != null) {
			String access_token = uri.getQueryParameter("oauth_token");
			Log.d(TAG, "Access token: "+access_token);
			Toast.makeText(this, "Got token: "+access_token, Toast.LENGTH_LONG);
			try {
				OAuthClient.setVerifier(null);
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
		super.onResume();
	}

	public String queryRESTurl(String url) {
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet(url);
		HttpResponse response;

		try {
			OAuthClient.sign(httpget);
			response = httpclient.execute(httpget);
			Log.i(TAG, "Status:[" + response.getStatusLine().toString() + "]");
			HttpEntity entity = response.getEntity();

			if (entity != null) {

				InputStream instream = entity.getContent();
				String result = RestClient.convertStreamToString(instream);
				Log.i(TAG, "Result of converstion: [" + result + "]");

				instream.close();
				return result;
			}
		} catch (ClientProtocolException e) {
			Toast.makeText(getBaseContext(), "Could not access server", 3000).show();
			Log.e("REST", "There was a protocol based error", e);
		} catch (UnknownHostException e) {
			Toast.makeText(this, "Could not access server", 3000).show();
			Log.e("REST", "Unknown host exception", e);
		} catch (IOException e) {
			Toast.makeText(getBaseContext(), "Could not access server", 3000).show();
			Log.e("REST", "There was an IO Stream related error", e);
		} catch (OAuthMessageSignerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OAuthExpectationFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OAuthCommunicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_SETTINGS, 0, "Settings").setIcon(android.R.drawable.ic_menu_preferences);
		menu.add(0, MENU_QUIT, 0, "Quit").setIcon(android.R.drawable.ic_menu_close_clear_cancel);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_SETTINGS:
			startActivity(new Intent(this, PrefsActivity.class));
			return true;
		case MENU_QUIT:
			finish();
			return true;
		}
		return false;
	}
}
