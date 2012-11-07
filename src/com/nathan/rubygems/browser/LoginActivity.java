package com.nathan.rubygems.browser;

import org.json.JSONException;
import org.json.JSONObject;

import com.nathanpc.misc.Fields;
import com.nathanpc.restful.RESTClient;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

public class LoginActivity extends Activity {
	public SharedPreferences settings;
	public boolean firstShow = true;
	
	private EditText username;
	private EditText password;
	private Button bt_login;
	private ProgressBar progress;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        
        settings = getSharedPreferences(Fields.PREFS_NAME, 0);
		String api_key = settings.getString("api_key", null);
		
		if (api_key != null) {
        	showMain();
		} else {
			setupUI();
			setupEdits();
		}
    }
	
	@Override
	protected void onResume() {
		super.onResume();
		
		if (!firstShow) {
			finish();
		}
	}

	private void setupUI() {
		username = (EditText)findViewById(R.id.txt_username);
		password = (EditText)findViewById(R.id.txt_password);
		progress = (ProgressBar)findViewById(R.id.loading_login);

		bt_login = (Button)findViewById(R.id.bt_login);
		bt_login.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				login(username.getText().toString(), password.getText().toString());
			}
		});
	}
	
	private void setupEdits() {
    	username.setImeActionLabel("Next", KeyEvent.KEYCODE_ENTER);
    	username.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
					username.clearFocus();
					password.setNextFocusDownId(password.getId());
					
					return true;
				}

				return false;
    		}
    	});
    	
    	password.setImeActionLabel("Login", KeyEvent.KEYCODE_ENTER);
    	password.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
					login(username.getText().toString(), password.getText().toString());
					
					InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(password.getWindowToken(), 0);
					
					return true;
				}

				return false;
    		}
    	});
	}
	
	private class loginTask extends AsyncTask<String, Void, String> {
	    protected String doInBackground(String... params) {
	    	String username = params[0];
	    	String password = params[1];
			
			JSONObject json;
			try {
				String response = RESTClient.rawGET(Fields.server_location + "/api_key.json", username, password);
				Log.i("Response", response);
				
				json = new JSONObject(response);
				return json.getString("rubygems_api_key");
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
	    }

	    @Override
		protected void onPreExecute() {
			super.onPreExecute();
			
			bt_login.setVisibility(View.GONE);
			progress.setVisibility(View.VISIBLE);
		}

	    protected void onPostExecute(String api_key) {
	    	progress.setVisibility(View.GONE);
	    	bt_login.setVisibility(View.VISIBLE);
			
	    	if (api_key != null) {
	    		SharedPreferences.Editor editor = settings.edit();
	    		editor.putString("api_key", api_key);
	    		editor.commit();
	    		
	    		showMain();
	    	} else {
	    		Toast.makeText(getApplicationContext(), "Incorrect username or password", Toast.LENGTH_SHORT).show();
	    	}
	     }
	 }
	
	public void login(String username, String password) {
		new loginTask().execute(username, password);
	}
	
	public void showMain() {
		firstShow = false;
		
		Intent intent = new Intent(this, MainActivity.class);
		
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		intent.addFlags(Intent.FLAG_ACTIVITY_TASK_ON_HOME);
		
    	startActivity(intent);
	}
}