package com.inf8405.wardriver;

import com.google.android.gms.maps.model.LatLng;
import com.inf8405.wardriver.WifiMap.MarkerType;

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

public class Main extends ActionBarActivity
{	
	private String[] mOptions;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    
    private WifiMap mMap;
    
    private WifiScanner mWifiScanner;
    
    private Compass mCompass;
    private boolean compassEnabled = false;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);

		// On récupère et ajuste le drawer
		mOptions = new String[]{"Start recording", "Settings", "Test Wi-Fi", "Test compass", "Test pins"};
		
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        
        mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list, mOptions));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        
        // On construit la carte google pour les wifi
        mMap = new WifiMap(getFragmentManager());
        
        // On contruit le wifi scanner
        mWifiScanner = new WifiScanner(this);
        
        // On contruit le compass
        mCompass = new Compass(this);
	}
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState)
	{
	    super.onPostCreate(savedInstanceState);
	    mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
	    super.onConfigurationChanged(newConfig);
	    mDrawerToggle.onConfigurationChanged(newConfig);
	}
	
	@Override
	public void onPause()
	{
		super.onPause();
		if (compassEnabled)
		{
			mCompass.stop();
		}
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		if (compassEnabled)
		{
			mCompass.start();
		}
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item)
	{
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
	
    private void itemSelected(int pos)
    {
    	// Temporaire!
        if (mOptions[pos].equals("Test Wi-Fi"))
        {
        	mWifiScanner.scanNow(this);
        }
        else if (mOptions[pos].equals("Test compass"))
        {
        	if (compassEnabled)
        	{
        		mCompass.stop();
        		compassEnabled = false;
        		mMap.resetOrientation();
        	}
        	else
        	{
            	mCompass.registerMap(mMap);
            	mCompass.start();
            	compassEnabled = true;
        	}
        }
        else if (mOptions[pos].equals("Test pins"))
        {
        	mMap.addWifiMarker(new LatLng(45.583, -73.806), "Test secure", MarkerType.SECURED);
        	mMap.addWifiMarker(new LatLng(45.584, -73.807), "Test unsecured", MarkerType.UNSECURED);
        	mMap.addWifiMarker(new LatLng(45.585, -73.808), "Test vulnerable", MarkerType.VULNERABLE);
        }
        
        mDrawerLayout.closeDrawer(mDrawerList);
    }
    
    private class DrawerItemClickListener implements ListView.OnItemClickListener
    {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id)
        {
        	itemSelected(position);
        }
    }
}
