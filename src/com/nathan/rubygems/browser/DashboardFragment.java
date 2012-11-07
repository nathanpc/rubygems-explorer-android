package com.nathan.rubygems.browser;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.nathanpc.misc.Fields;
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

public class DashboardFragment extends Fragment {
    private ProgressBar progress;
	private ListView listView;
	
	public Fields fields;
	private List<HashMap<String, String>> gemsList;

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	View view = inflater.inflate(R.layout.dashboard, null);
    	
    	try {
			fields = new Fields(this.getActivity());
		} catch (Exception e) {
			e.printStackTrace();
		}
    	
    	gemsList = new ArrayList<HashMap<String, String>>();
    	progress = (ProgressBar)view.findViewById(R.id.loading_dashboard);
    	listView = (ListView)view.findViewById(R.id.dashboard_list);
        listView.setTextFilterEnabled(true);
        
        new getGemsListTask().execute();
        
		return view;
    }
	
	private void populateList() {
		String[] from = { "name", "updated" };
		int[] to = { R.id.item_title, R.id.item_subtitle };

		SimpleAdapter adapter = new SimpleAdapter(this.getActivity(), gemsList, R.layout.list_item, from, to);
		listView.setAdapter(adapter);
        
        listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Log.i("List Item Click", gemsList.get(position).get("name"));
				
				String name = gemsList.get(position).get("name");
				showDetail(name.substring(0, name.indexOf(" ")));
			}
        });
	}
	
	public void showDetail(String gem_name) {
		Intent intent = new Intent(getActivity().getBaseContext(), GemDetailActivity.class);
		intent.putExtra("name", gem_name);
		
    	startActivity(intent);
	}
	
	// The mess to do HTTP requests in Android:
    private class getGemsListTask extends AsyncTask<String, Void, Document> {
		protected Document doInBackground(String... arg) {
	    	Log.i("Dashboard", "Started GET");

			try {
				URL xml = new URL(Fields.server_domain + "/dashboard.atom?api_key=" + fields.api_key);
				URLConnection urlConnection = xml.openConnection();
				
				DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				Document document = documentBuilder.parse(urlConnection.getInputStream());
				
				return document;
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			} catch (SAXException e) {
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

		protected void onPostExecute(Document document) {
			Log.i("Dashboard", "Parsing GET Atom");
			
			NodeList entries = document.getElementsByTagName("entry");
			for (int i = 0; i < entries.getLength(); i++) {
				Element entry = (Element)entries.item(i);

				String name = getTagValue("title", entry);
 				String date = getTagValue("updated", entry);
 				
 				SimpleDateFormat sdf  = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
 				try {
 					Calendar calendar = Calendar.getInstance();
 					long current_date = calendar.getTimeInMillis();
					
 					Date update = sdf.parse(date.replace("T", " ").replace("Z", ""));
					long update_date = update.getTime();
					
					long diff_date = TimeUnit.MILLISECONDS.toDays(current_date - update_date);
					
					if (diff_date < 31) {
						date = diff_date + " days ago";
					} else if (diff_date < 365) {
						date = (diff_date / 31) + " months ago";
					} else if (diff_date > 365) {
						date = (diff_date / 365) + " years ago";
					}
				} catch (ParseException e) {
					e.printStackTrace();
				}
 				
 				HashMap<String, String> tmp_map = new HashMap<String,String>();
 				tmp_map.put("name", name);
 				tmp_map.put("updated", date);
 	            gemsList.add(tmp_map);
			}
	 		
	 		populateList();
	 		
	 		listView.setVisibility(View.VISIBLE);
			progress.setVisibility(View.GONE);
	     }
		
		private String getTagValue(String tag, Element element) {
			NodeList list = element.getElementsByTagName(tag).item(0).getChildNodes();
			Node node = (Node)list.item(0);

			return node.getNodeValue();
		}
	}
}