package com.lonian.android;

import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;

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
import android.content.Intent;
import android.os.Bundle;
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
                
                String result = queryRESTurl("http://lonian.com/since");   
                
                if (result != null) {
                	try{   
                		JSONObject json_result = new JSONObject(result);   
                		JSONObject json_meta = json_result.getJSONObject("meta");
                		JSONArray  json_errors = json_meta.getJSONArray("errors");
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
    }
    
    public String queryRESTurl(String url) {   
        HttpClient httpclient = new DefaultHttpClient();   
        HttpGet httpget = new HttpGet(url);   
        HttpResponse response;   
           
        try {   
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