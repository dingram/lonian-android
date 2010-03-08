package com.lonian.android;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebAuthActivity extends Activity {
	private static final String TAG = "com.lonian.android.WebAuthActivity";
	static final String AUTH_URL = "com.lonian.webauth.auth_url";
	private WebView webView;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().requestFeature(Window.FEATURE_PROGRESS);
		setContentView(R.layout.auth);
		webView = (WebView)findViewById(R.id.web_view);
		
		final Activity _this = this;
		
		webView.setWebChromeClient(new WebChromeClient() {
			public void onProgressChanged(WebView view, int progress) {
				// Activities and WebViews measure progress with different scales.
				// The progress meter will automatically disappear when we reach 100%
				_this.setProgress(progress * 1000);
			}
		});
		
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url)
			{
				if (url.startsWith("lonian://oauth-callback", 0)) {
					Intent intent = new Intent();
					intent.setData(Uri.parse(url));
					_this.setResult(RESULT_OK, intent);
					_this.finish();
					return true;
				}
				webView.loadUrl(url);
				return true;
			}
			
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				Log.d(TAG, "start loading page....");
			}
			
			@Override
			public void onPageFinished(WebView view, String url) {
				Log.d(TAG, "finish loading page....");
			}
		});
		
		if (getIntent() != null && getIntent().hasExtra(AUTH_URL)) {
			String authUrl = getIntent().getStringExtra(AUTH_URL);
			Log.d(TAG, "Opening URL: "+authUrl);
			openUrl(authUrl);
		}
	}
	
	private void openUrl(String url) {
		webView.getSettings().setJavaScriptEnabled(true);
		webView.loadUrl(url);
	}

}
