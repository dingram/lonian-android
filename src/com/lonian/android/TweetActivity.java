package com.lonian.android;

import java.io.IOException;

import oauth.signpost.exception.OAuthException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

import com.lonian.android.oauth.OAuthClient;

public class TweetActivity extends Activity implements OnClickListener, TextWatcher, OnItemSelectedListener {
	static final String TAG = "com.lonian.android.TweetActivity";
	static final int MAX_TWEET_LENGTH = 140;
	private Button sendButton;
	private EditText tweetEntry;
	private Spinner twitterAccount;
	private TextView tweetLength;
	private boolean tweetLengthWasBold = false;
	private boolean tweetCouldSend = false;
	private int tweetLengthWasColor = 0xff000000;
	private String tweetUsername = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tweet);
		
		sendButton = (Button)findViewById(R.id.btn_send);
		tweetLength = (TextView)findViewById(R.id.tweet_length);
		tweetEntry = (EditText)findViewById(R.id.tweet);
		twitterAccount = (Spinner)findViewById(R.id.account_list);
		
		// TODO: make this persist
		JSONObject json_result;
		try {
			json_result = OAuthClient.getInstance().makeRequest("http://api.lonian.com/v1/twitter/accounts");
			if (json_result != null) {
				if (json_result.has("accounts")) {
					JSONArray accounts = json_result.getJSONArray("accounts");
					String[] items = new String[accounts.length()];
					for (int i=0; i<accounts.length(); ++i) {
						JSONObject account = accounts.getJSONObject(i);
						items[i] = account.getString("username");
					}
					ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, items);
					adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
					twitterAccount.setAdapter(adapter);
				} else {
					throw new IOException();
				}
			} else {
				throw new IOException();
			}
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Toast.makeText(this, "Could not fetch list of accounts", 3000).show();
			finish();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Toast.makeText(this, "Could not fetch list of accounts", 3000).show();
			finish();
		} catch (OAuthException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Toast.makeText(this, "Could not fetch list of accounts", 3000).show();
			finish();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Toast.makeText(this, "Could not fetch list of accounts", 3000).show();
			finish();
		}
		
		sendButton.setOnClickListener(this);
		twitterAccount.setOnItemSelectedListener(this);
		tweetEntry.addTextChangedListener(this);
		updateTweetLength();
	}

	public void onClick(View v) {
		if (v.getId() == R.id.btn_send) {
			if (tweetUsername != null) {
				JSONObject tweet = new JSONObject();
				try {
					tweet.put("account", tweetUsername);
					tweet.put("message", tweetEntry.getText());
					Toast.makeText(this, "Sending tweet as "+tweetUsername+"...", 3000).show();
					OAuthClient.getInstance().makePost("http://api.lonian.com/v1/twitter/tweet", tweet.toString());
				} catch (Exception e) {
					e.printStackTrace();
					Toast.makeText(this, "Tweet failed!", 3000).show();
				}
			} else {
				Toast.makeText(this, "Not sending tweet.", 3000).show();
			}
			finish();
		}
	}

	protected void updateTweetLength() {
		int tweetLen = tweetEntry.getText().length();
		int charsLeft = MAX_TWEET_LENGTH - tweetLen;

		boolean shouldBold = tweetLen > 120;
		boolean canSend = tweetLen > 0 && tweetLen <=140; 
		int textColor = 0xffcccccc;
		
		if (tweetLen > 130) {
			textColor = 0xffe70d12;
		} else if (tweetLen > 120) {
			//textColor = 0xff5c0002;
			textColor = 0xffcc3333;
		}
		
		tweetLength.setText(Integer.toString(charsLeft));
		if (tweetLengthWasBold != shouldBold) {
			tweetLength.setTypeface(tweetLength.getTypeface(), shouldBold ? Typeface.BOLD : Typeface.NORMAL);
			tweetLengthWasBold = shouldBold;
		}
		if (tweetLengthWasColor != textColor) {
			tweetLength.setTextColor(textColor);
			tweetLengthWasColor = textColor;
		}
		if (tweetCouldSend != canSend) {
			sendButton.setEnabled(canSend);
			tweetCouldSend = canSend;
		}
	}

	public void afterTextChanged(Editable arg0) {
	}

	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	}

	public void onTextChanged(CharSequence s, int start, int before, int count) {
		updateTweetLength();
	}

	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
		tweetUsername = parent.getItemAtPosition(pos).toString();
	}

	public void onNothingSelected(AdapterView<?> arg0) {
	}
	
}
