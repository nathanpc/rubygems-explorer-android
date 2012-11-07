package com.nathanpc.misc;

import android.app.Activity;
import android.content.SharedPreferences;

public class Fields {
	public static final String PREFS_NAME = "RubyGems";
	public static final String server_domain = "https://rubygems.org";
	public static final String server_location = server_domain + "/api/v1";
	
	public SharedPreferences settings;
	public final String api_key;
	
	public Fields(Activity activity) throws Exception {
		this.settings = activity.getSharedPreferences(PREFS_NAME, 0);
		this.api_key = settings.getString("api_key", null);
	}
}
