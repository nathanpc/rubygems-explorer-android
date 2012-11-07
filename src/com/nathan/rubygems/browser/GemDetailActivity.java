package com.nathan.rubygems.browser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.nathanpc.misc.Fields;
import com.nathanpc.restful.RESTClient;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class GemDetailActivity extends Activity {
	private RelativeLayout cntRoot;
	private ProgressBar progress;
	private TextView lblName;
	private TextView lblVersion;
	private TextView lblDescription;
	private TextView lblDownloads;
	private TextView lblCurrentDownloads;
	private TextView lblAuthors;
	private Button btHomepage;
	private Button btCode;
	private Button btDocs;
	private Button btWiki;
	private Button btMail;
	private Button btBug;
	private LinearLayout cntRuntimeDeps;
	private LinearLayout cntDevDeps;
	
	public Fields fields;
	public static String name;
	public static JSONObject gem_json;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.gem_detail);
        
        setupUI();
        
        try {
			fields = new Fields(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
        
        name = getIntent().getExtras().getString("name");
        new getGemDetailsTask().execute();
	}
	
	private void setupUI() {
		cntRoot = (RelativeLayout)findViewById(R.id.gem_detail_root);
		progress = (ProgressBar)findViewById(R.id.loading_gem_details);
		
		lblName = (TextView)findViewById(R.id.gem_name);
		lblVersion = (TextView)findViewById(R.id.gem_version);
		lblDescription = (TextView)findViewById(R.id.gem_description);
		lblDownloads = (TextView)findViewById(R.id.gem_downloads);
		lblCurrentDownloads = (TextView)findViewById(R.id.gem_curr_downloads);
		lblAuthors = (TextView)findViewById(R.id.gem_authors);
		
		btHomepage = (Button)findViewById(R.id.bt_homepage);
		btCode = (Button)findViewById(R.id.bt_code);
		btDocs = (Button)findViewById(R.id.bt_docs);
		btWiki = (Button)findViewById(R.id.bt_wiki);
		btMail = (Button)findViewById(R.id.bt_mail);
		btBug = (Button)findViewById(R.id.bt_bug);
		
		cntRuntimeDeps = (LinearLayout)findViewById(R.id.gem_dependencies);
		cntDevDeps = (LinearLayout)findViewById(R.id.gem_dev_dependencies);
	}
	
	private void populateView() throws JSONException {
		lblName.setText(name);
		lblVersion.setText(gem_json.getString("version"));
		lblDescription.setText(gem_json.getString("info"));
		lblDownloads.setText(gem_json.getString("downloads"));
		lblCurrentDownloads.setText(gem_json.getString("version_downloads"));
		lblAuthors.setText(gem_json.getString("authors"));

		setupButton("homepage_uri", btHomepage);
		setupButton("source_code_uri", btCode);
		setupButton("documentation_uri", btDocs);
		setupButton("wiki_uri", btWiki);
		setupButton("mailing_list_uri", btMail);
		setupButton("bug_tracker_uri", btBug);

		populateList(cntRuntimeDeps, gem_json.getJSONObject("dependencies").getJSONArray("runtime"));
		populateList(cntDevDeps, gem_json.getJSONObject("dependencies").getJSONArray("development"));
	}
	
	public void setupButton(final String key, Button button) {
		try {
			final String value = gem_json.getString(key);
			
			if ((value.equals("null")) || (value.equals(""))) {
				button.setEnabled(false);
			} else {
				button.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View view) {
						Intent browser = new Intent(Intent.ACTION_VIEW, Uri.parse(value));
						startActivity(browser);
					}
		        });
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public void populateList(LinearLayout list, JSONArray deps) {
		for (int i = 0; i < deps.length(); i++) {
			try {
				String tmp_name = deps.getJSONObject(i).getString("name");
				String tmp_req = deps.getJSONObject(i).getString("requirements");
				
				TextView textView = new TextView(this);
			    textView.setText(Html.fromHtml("<b>" + tmp_name + "</b> " + tmp_req));
			    textView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
			    textView.setGravity(Gravity.CENTER);
			    
			    list.addView(textView);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	
	// The mess to do HTTP requests in Android:
    private class getGemDetailsTask extends AsyncTask<String, Void, JSONObject> {
		protected JSONObject doInBackground(String... arg) {
	    	Log.i(name + " Gem Detail", "Started GET");
	    	
	    	String[] keys = { "Authorization" };
	    	String[] values = { fields.api_key };
	    	String raw = RESTClient.rawGET(Fields.server_location + "/gems/" + name + ".json", keys, values);
	    	
	    	JSONObject json;
			try {
				json = new JSONObject(raw);
				return json;
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
	    }

	    @Override
		protected void onPreExecute() {
			super.onPreExecute();

			progress.setVisibility(View.VISIBLE);
			cntRoot.setVisibility(View.GONE);
		}

		protected void onPostExecute(JSONObject json) {
			Log.i(name + " Gem Detail", "Parsing GET JSON");

	    	gem_json = json;
	    	try {
				populateView();

				cntRoot.setVisibility(View.VISIBLE);
				progress.setVisibility(View.GONE);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
}
