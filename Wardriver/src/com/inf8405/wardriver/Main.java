package com.inf8405.wardriver;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class Main extends ActionBarActivity {

	private String[] mOptions;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    
    private WifiScanner mWifiScanner;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);

		// On récupère et ajuste le drawer
		mOptions = new String[]{"Start recording", "Settings", "Test Martin"};
		
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        
        mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list, mOptions));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        
        // On récupère et ajuste la carte google
        GoogleMap map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        map.setMyLocationEnabled(true);
        
        // On contruit le wifi scanner
        mWifiScanner = new WifiScanner(this);
	}
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
	    super.onPostCreate(savedInstanceState);
	    mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);
	    mDrawerToggle.onConfigurationChanged(newConfig);
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerLayout.isDrawerOpen(mDrawerList))
		{
			mDrawerLayout.closeDrawer(mDrawerList);
		}
		else
		{
			mDrawerLayout.openDrawer(Gravity.START);
		}
        return super.onOptionsItemSelected(item);
    }
	
    private void itemSelected(int pos) {
        if (mOptions[pos].equals("Test Martin"))
        {
        	mWifiScanner.scanNow(this);
        }
        
        mDrawerLayout.closeDrawer(mDrawerList);
    }
    
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
        	itemSelected(position);
        }
    }
}
