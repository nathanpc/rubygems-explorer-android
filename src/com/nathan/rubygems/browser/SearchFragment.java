package com.nathan.rubygems.browser;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.nathanpc.misc.Fields;
import com.nathanpc.restful.RESTClient;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;

public class SearchFragment extends Fragment {
	private ProgressBar progress;
	private ListView listView;
	private EditText searchEdit;
	
	private List<HashMap<String, String>> gemsList;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	View view = inflater.inflate(R.layout.search, null);
    	
    	gemsList = new ArrayList<HashMap<String, String>>();
    	
    	progress = (ProgressBar)view.findViewById(R.id.loading_search);
    	searchEdit = (EditText)view.findViewById(R.id.search_gem);
    	listView = (ListView)view.findViewById(R.id.search_gem_list);
        listView.setTextFilterEnabled(true);
        
        setupSearchEdit();
        
        if (!gemsList.isEmpty()) {
        	populateList();
        }
        
		return view;
    }
	
	private void setupSearchEdit() {
    	searchEdit.setImeActionLabel("Search", KeyEvent.KEYCODE_ENTER);
    	searchEdit.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
					new getGemsListTask().execute(searchEdit.getText().toString());
					
					InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(searchEdit.getWindowToken(), 0);
					
					return true;
				}

				return false;
    		}
    	});
	}
	
	private void populateList() {
		String[] from = { "name", "description" };
		int[] to = { R.id.item_title, R.id.item_subtitle };

		SimpleAdapter adapter = new SimpleAdapter(this.getActivity(), gemsList, R.layout.list_item, from, to);
		listView.setAdapter(adapter);
        
        listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Log.i("List Item Click", gemsList.get(position).get("name"));
				showDetail(gemsList.get(position).get("name"));
			}
        });
	}
	
	public void showDetail(String gem_name) {
		Intent intent = new Intent(getActivity().getBaseContext(), GemDetailActivity.class);
		intent.putExtra("name", gem_name);
		
    	startActivity(intent);
	}
	
	// The mess to do HTTP requests in Android:
    private class getGemsListTask extends AsyncTask<String, Void, JSONArray> {
		protected JSONArray doInBackground(String... arg) {
			try {
				Log.i("Search Gems", "Started GET");
		    	String raw = RESTClient.rawGET(Fields.server_location + "/search.json?query=" + URLEncoder.encode(arg[0],"UTF-8"));
		    	
		    	JSONArray json = new JSONArray(raw);
				return json;
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}

			return null;
	    }

	    @Override
		protected void onPreExecute() {
			super.onPreExecute();
			progress.setVisibility(View.VISIBLE);
			listView.setVisibility(View.GONE);
		}

		protected void onPostExecute(JSONArray json) {
			Log.i("Search Gems", "Parsing GET JSON");

	    	try {
	    		gemsList.clear();
	    		
	 			for (int i = 0; i < json.length(); ++i) {
	 				JSONObject gem = json.getJSONObject(i);

	 				String name = gem.getString("name");
	 				String description = gem.getString("info");
	 			    
	 				HashMap<String, String> tmp_map = new HashMap<String,String>();
	 				tmp_map.put("name", name);
	 				tmp_map.put("description", description);
	 	            gemsList.add(tmp_map);
	 			}
	 		} catch (JSONException e) {
	 			Log.e("JSON Parse", e.getMessage());
	 		}
	 		
	 		populateList();
	 		
	 		listView.setVisibility(View.VISIBLE);
			progress.setVisibility(View.GONE);
	     }
	}
}