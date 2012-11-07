package com.nathan.rubygems.browser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.nathanpc.misc.Fields;
import com.nathanpc.restful.RESTClient;

import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;

public class MyGemsFragment extends Fragment {
	private ProgressBar progress;
	private ListView listView;
	
	public Fields fields;
	private List<HashMap<String, String>> gemsList;

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	View view = inflater.inflate(R.layout.my_gems, null);
    	
    	try {
			fields = new Fields(this.getActivity());
		} catch (Exception e) {
			e.printStackTrace();
		}
    	
    	gemsList = new ArrayList<HashMap<String, String>>();
    	progress = (ProgressBar)view.findViewById(R.id.loading_my_gems);
    	listView = (ListView)view.findViewById(R.id.lst_my_gems);
        listView.setTextFilterEnabled(true);
        
        new getGemsListTask().execute();
        
		return view;
    }
	
	private void populateList() {
		String[] from = { "name", "downloads" };
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
	    	Log.i("My Gems", "Started GET");
	    	
	    	String[] keys = { "Authorization" };
	    	String[] values = { fields.api_key };
	    	String raw = RESTClient.rawGET(Fields.server_location + "/gems.json", keys, values);
	    	
	    	JSONArray json;
			try {
				json = new JSONArray(raw);
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
			listView.setVisibility(View.GONE);
		}

		protected void onPostExecute(JSONArray json) {
			Log.i("My Gems", "Parsing GET JSON");

	    	try {
	 			for (int i = 0; i < json.length(); ++i) {
	 				JSONObject gem = json.getJSONObject(i);

	 				String name = gem.getString("name");
	 				String downloads = gem.getString("downloads");
	 				String version_downloads = gem.getString("version_downloads");
	 			    
	 				HashMap<String, String> tmp_map = new HashMap<String,String>();
	 				tmp_map.put("name", name);
	 				tmp_map.put("downloads", downloads + " Downloads (" + version_downloads + " for the current version)");
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
