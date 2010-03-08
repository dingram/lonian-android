package com.lonian.android;

import java.io.IOException;

import oauth.signpost.exception.OAuthException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.lonian.android.oauth.OAuthClient;

public class MainActivity extends Activity implements OnClickListener {
	static final String TAG = "com.lonian.android.MainActivity";
	static final int MENU_QUIT     = 1;
	static final int MENU_SETTINGS = 2;
	static final int MENU_TWEET    = 3;
	
	static final int AUTH_URL_ACTIVITY = 1;
	
	OAuthClient oauth;
	Button authButton;
	Button deauthButton;
	Button updateButton;
	Button notifyButton;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		authButton = (Button)findViewById(R.id.btn_auth);
		authButton.setOnClickListener(this);

		deauthButton = (Button)findViewById(R.id.btn_deauth);
		deauthButton.setOnClickListener(this);

		updateButton = (Button)findViewById(R.id.btn_updates);
		updateButton.setOnClickListener(this);

		notifyButton = (Button)findViewById(R.id.btn_notify);
		notifyButton.setOnClickListener(this);

		oauth = OAuthClient.getInstance(this);
		updateAuthorized();
	}

	@Override
	public void onResume() {
		// extract the OAUTH access token if it exists
		((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancelAll();
		verifyFromIntent(this.getIntent());
		super.onResume();
	}
	
	protected void updateAuthorized() {
		authButton.setEnabled(!oauth.isAuthorized());
		deauthButton.setEnabled(oauth.isAuthorized());
		updateButton.setEnabled(oauth.isAuthorized());
	}
	
	protected void verifyFromIntent(Intent i) {		
		Uri uri = i.getData();
		if(uri != null) {
			String access_token = uri.getQueryParameter("oauth_token");
			String verifier     = uri.getQueryParameter("oauth_verifier");
			Log.i(TAG, "Access token: "+access_token);
			if (verifier != null) {
				Log.i(TAG, "Verifier: "+verifier);
			}
			oauth.verifyAccess(verifier);
			updateAuthorized();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_TWEET,    0, "Tweet"   ).setIcon(android.R.drawable.ic_menu_edit);
		menu.add(0, MENU_SETTINGS, 0, "Settings").setIcon(android.R.drawable.ic_menu_preferences);
		menu.add(0, MENU_QUIT,     0, "Quit"    ).setIcon(android.R.drawable.ic_menu_close_clear_cancel);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_TWEET:
			startActivity(new Intent(this, TweetActivity.class));
			return true;
		case MENU_SETTINGS:
			startActivity(new Intent(this, PrefsActivity.class));
			return true;
		case MENU_QUIT:
			finish();
			return true;
		}
		return false;
	}
	
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == AUTH_URL_ACTIVITY) {
            if (resultCode == RESULT_OK) {
                verifyFromIntent(data);
            }
        }
    }

	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btn_deauth:
			if (oauth.isAuthorized()) {
				oauth.deauth();
			}
			updateAuthorized();
			break;
			
		case R.id.btn_auth:
			if (!oauth.isAuthorized()) {
				String url = oauth.getAuthorizeUrl();
				// start the web auth activity
				Log.d(TAG, "Sending to authorize");
				Intent i = new Intent(this, WebAuthActivity.class);
				Log.i(TAG, "Authorize URL: "+url);
				i.putExtra(WebAuthActivity.AUTH_URL, url);
				Log.d(TAG, "Starting activity");
				startActivityForResult(i, AUTH_URL_ACTIVITY);
			}
			break;
			
		case R.id.btn_notify:
			NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			int icon = R.drawable.notification_alert;
			CharSequence tickerText = "13 new updates available!";
			long when = System.currentTimeMillis();

			Notification notification = new Notification(icon, tickerText, when);
			Context context = getApplicationContext();
			CharSequence contentTitle = "Lonian";
			CharSequence contentText = "13 new updates since 09:52";
			Intent notificationIntent = new Intent(this, MainActivity.class);
			PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

			notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
			final int HELLO_ID = 1;

			mNotificationManager.notify(HELLO_ID, notification);
			break;
			
		case R.id.btn_updates:
			JSONObject json_result = null;
			try {
				json_result = oauth.makeRequest("http://api.lonian.com/v1/updates");
				JSONArray  json_errors = json_result == null ? null : json_result.getJSONArray("errors");
				if (json_result == null || json_errors == null) {
					Toast.makeText(this, "Could not parse server output", 3000).show();
				} else if (json_errors.length() > 0) {
					StringBuilder errors = new StringBuilder("Error: ");
					for (int i=0; i<json_errors.length(); ++i) {
						errors.append(json_errors.getString(i));
					}
					Toast.makeText(this, errors, 3000).show();
				} else {
					Toast.makeText(this, "There were some updates", 3000).show();
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (OAuthException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		}
	}
}
