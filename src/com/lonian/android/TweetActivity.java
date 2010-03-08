package com.lonian.android;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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

import com.lonian.android.api.TwitterAPI;

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
		JSONArray accounts = TwitterAPI.getAccounts();
		if (accounts != null && accounts.length() > 0) {
			String[] items = new String[accounts.length()];
			try {
				for (int i=0; i<accounts.length(); ++i) {
					JSONObject account = accounts.getJSONObject(i);
					items[i] = account.getString("username");
				}
			} catch (JSONException e) {
				Toast.makeText(this, "Could not fetch list of accounts", 3000).show();
				finish();
			}
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, items);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			twitterAccount.setAdapter(adapter);
		} else {
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
				Toast.makeText(this, "Sending tweet as "+tweetUsername+"...", 3000).show();
				JSONObject result = TwitterAPI.tweet(tweetUsername, tweetEntry.getText().toString());
				if (result == null) {
					Toast.makeText(this, "Tweeting failed!", 3000).show();
				}
			} else {
			    AlertDialog alertDialog = new AlertDialog.Builder(this).create();
			    alertDialog.setTitle("Tweet error");
			    alertDialog.setMessage("You must select a valid Twitter account");
			    alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
			      public void onClick(DialogInterface dialog, int which) {
			        return;
			      } }); 
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
