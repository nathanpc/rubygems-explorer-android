package com.nathan.rubygems.browser;

import com.nathanpc.misc.Fields;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends FragmentActivity {
	SectionsPagerAdapter mSectionsPagerAdapter;
    ViewPager mViewPager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager)findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    		case R.id.logout_menu:
    			Intent addIntent = new Intent(this, LoginActivity.class);
    			
    			SharedPreferences settings = getSharedPreferences(Fields.PREFS_NAME, 0);
    			settings.edit().remove("api_key").commit();
    			
    	    	startActivity(addIntent);
    	    	MainActivity.this.finish();
    			break;
    	}

    	return true;
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
        	Log.i("Current Fragment", String.valueOf(i));
        	
        	switch (i) {
        		case 0:
        			return new DashboardFragment();
        		case 1:
        			return new MyGemsFragment();
        		case 2:
        			return new SearchFragment();
        	}
        	
        	return null;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0: return getString(R.string.dashboard).toUpperCase();
                case 1: return getString(R.string.my_gems).toUpperCase();
                case 2: return getString(R.string.search).toUpperCase();
            }
            return null;
        }
    }
}
