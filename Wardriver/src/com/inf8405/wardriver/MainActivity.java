package com.inf8405.wardriver;

import com.google.android.gms.maps.model.LatLng;
import com.inf8405.wardriver.WifiMap.MarkerType;

import java.io.*;
import java.net.*;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

public class MainActivity extends ActionBarActivity implements OnClickListener
{
	private static final int RESULT_SETTINGS_ACTIVITY = 1;
	
	private String[] mOptions;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    
    private WifiMap mMap;
    
    private WifiScanner mWifiScanner;
    private int mWifiScanIntervalMS = 0;
    
    private Compass mCompass;
    private boolean mCompassEnabled = false;
    private Button mBtnCompass;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);

		// On récupère et ajuste le drawer
		mOptions = getResources().getStringArray(R.array.menu_options);
		
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
        mWifiScanIntervalMS = SettingsActivity.getWifiScanInterval(this);
        
        // On contruit le compass
        mCompass = new Compass(this);
        mCompass.setAzimuthOffset( SettingsActivity.getCompassOffset(this) );
        mBtnCompass = (Button) findViewById(R.id.btnCompass);
        mBtnCompass.setOnClickListener(this);
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
		if (mCompassEnabled)
		{
			mCompass.stop();
		}
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		if (mCompassEnabled)
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
    	String option = mOptions[pos];
    	if (option.equals( getResources().getString(R.string.menu_record_start)) )
    	{
    		mOptions[pos] = getResources().getString(R.string.menu_record_stop);
    		mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list, mOptions));
    	}
    	else if (option.equals( getResources().getString(R.string.menu_record_stop)) )
    	{
    		mOptions[pos] = getResources().getString(R.string.menu_record_start);
    		mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list, mOptions));
    	}
    	else if (option.equals( getResources().getString(R.string.menu_options)) )
    	{
    		Intent intent = new Intent(this, SettingsActivity.class);
    		this.startActivityForResult(intent, RESULT_SETTINGS_ACTIVITY);
    	}
    	else if (option.equals( getResources().getString(R.string.menu_testWifi)) ) // Temporaire!
    	{
    		if (mWifiScanner.isRunning())
    		{
    			mWifiScanner.stop(this);
    		}
    		else
    		{
    			mWifiScanner.start(this, 0); //FIXME: 0 temporaire, mettre mWifiScanIntervalMS
    		}
    	}
        else if (option.equals( getResources().getString(R.string.menu_testPins)) ) // Temporaire!
        {
        	mMap.addWifiMarker(new LatLng(45.583, -73.806), "Test secure", MarkerType.SECURED);
        	mMap.addWifiMarker(new LatLng(45.584, -73.807), "Test unsecured", MarkerType.UNSECURED);
        	mMap.addWifiMarker(new LatLng(45.585, -73.808), "Test vulnerable", MarkerType.VULNERABLE);
        }
        else if(option.equals(getResources().getString(R.string.menu_testPush))) {
        	ClientTCP client = new ClientTCP();
        	client.connect();
        }
        
        mDrawerLayout.closeDrawer(mDrawerList);
    }
    
    private class DrawerItemClickListener implements ListView.OnItemClickListener
    {
        @SuppressWarnings("rawtypes")
		@Override
        public void onItemClick(AdapterView parent, View view, int position, long id)
        {
        	itemSelected(position);
        }
    }
    
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode)
		{
			case RESULT_SETTINGS_ACTIVITY:
				// Paramètres ont possiblement été changés, on met à jour
				
				// Offset de la boussole
				mCompass.setAzimuthOffset( SettingsActivity.getCompassOffset(this) );
				if (mCompassEnabled)
				{
					mCompass.applyMapRotation();
				}
				Log.i("onActivityResult", "Compass offset: " + SettingsActivity.getCompassOffset(this));
				
				// Interval de scan wifi
				mWifiScanIntervalMS = SettingsActivity.getWifiScanInterval(this);
				if (mWifiScanner.isRunning())
				{
					mWifiScanner.start(this, mWifiScanIntervalMS);
				}
				Log.i("onActivityResult", "Wifi scan interval: " + mWifiScanIntervalMS);
				
				break;
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
			case R.id.btnCompass:
				if (mCompassEnabled)
				{
					mCompassEnabled = false;
					mBtnCompass.setBackgroundDrawable(getResources().getDrawable(R.drawable.compass_off));
	        		mCompass.stop();
	        		mMap.resetOrientation();
				}
				else
				{
					mCompassEnabled = true;
					mBtnCompass.setBackgroundDrawable(getResources().getDrawable(R.drawable.compass_on));
	            	mCompass.registerMap(mMap);
	            	mCompass.start();
				}
				break;
		}
	}
}
