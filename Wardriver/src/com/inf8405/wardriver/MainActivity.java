package com.inf8405.wardriver;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Location;
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
import android.widget.TextView;

import com.inf8405.wardriver.compass.Compass;
import com.inf8405.wardriver.db.ClientTCP;
import com.inf8405.wardriver.db.LocalDatabase;
import com.inf8405.wardriver.gps.GPS;
import com.inf8405.wardriver.map.WifiMap;
import com.inf8405.wardriver.map.WifiMapClickListener;
import com.inf8405.wardriver.wifi.WifiInfo;
import com.inf8405.wardriver.wifi.WifiListener;
import com.inf8405.wardriver.wifi.WifiScanner;

public class MainActivity extends ActionBarActivity implements OnClickListener, WifiListener, WifiMapClickListener
{
	private static final int RESULT_SETTINGS_ACTIVITY = 1;
	
	private String[] mOptions;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    
    private HashMap<String, WifiInfo> mWifiList = new HashMap<String, WifiInfo>();
    
    private WifiMap mMap;
    
    private WifiScanner mWifiScanner;
    private int mWifiScanIntervalMS = 3000;
    
    private Compass mCompass;
    private boolean mCompassEnabled = false;
    private Button mBtnCompass;

    private GPS mGPS;
    private int mGPSScanIntervalMS = 3000;
    
	private enum FilterState { OFF, UNSECURED, VULN, SECURED }
	private FilterState mFilterState = FilterState.OFF;
	private Button mBtnFilter;
    
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
        mMap.addWifiClickListener(this);
        mBtnFilter = (Button) findViewById(R.id.btnFilter);
        mBtnFilter.setOnClickListener(this);
        
        // On contruit le scanner de points d'accès wifi
        mWifiScanner = new WifiScanner(this);
        mWifiScanner.addListener(this);
        mWifiScanIntervalMS = SettingsActivity.getScanInterval(this);
        
        // On contruit la boussole
        mCompass = new Compass(this);
        mCompass.setAzimuthOffset( SettingsActivity.getCompassOffset(this) );
        mBtnCompass = (Button) findViewById(R.id.btnCompass);
        mBtnCompass.setOnClickListener(this);
        
        //On construit le listener pour la position
        mGPS = new GPS(this);
        mGPSScanIntervalMS = SettingsActivity.getScanInterval(this);
        
        //On zoom sur la position actuelle
        Location location = mGPS.getLocationApprox();
        mMap.zoomOnLocation(location.getLatitude(), location.getLongitude());
        
        // Load la base de donnée locale et met à jour la carte
        mWifiList = LocalDatabase.getInstance(this).getAllAccessPoints();
        for (String key : mWifiList.keySet())
        {
        	mMap.setWifiMarker(mWifiList.get(key));
        }
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
		
		// On arrête la boussole si activée
		if (mCompassEnabled)
		{
			mCompass.stop();
		}
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		
		// On redémarre la boussole si activée
		if (mCompassEnabled)
		{
			mCompass.start();
		}
	}
	
	@Override
	public void onBackPressed()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Quit Wardriver")
			   .setMessage("Do you really want to leave the app? The scanning will be stopped.")
			   .setCancelable(false)
			   .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (mWifiScanner.isRunning())
						{
							mWifiScanner.stop(MainActivity.this);
							mGPS.stop();
						}
						finish();
					}
			   })
			   .setNegativeButton("No", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
			   });
	    builder.show();
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
	
	// Fonction appelée lorsqu'un item du menu est sélectionné
    private void itemSelected(int pos)
    {
    	// On vérifie quelle option a été choisie
    	String option = mOptions[pos];
    	if (option.equals( getResources().getString(R.string.menu_record_start)) )
    	{
    		// On démarre le GPS
    		if (!mGPS.isRunning())
    		{
    			mGPS.start(mGPSScanIntervalMS);
    			Log.i("Main", "GPS scanner started, interval: " + mGPSScanIntervalMS);
    		}
    		
    		// On démarre le scanneur
    		if (!mWifiScanner.isRunning())
    		{
    			mWifiScanner.start(this, mWifiScanIntervalMS);
    			Log.i("Main", "Wifi scanner started, interval: " + mWifiScanIntervalMS);
    		}
    		
    		// On change le menu pour "stop"
    		mOptions[pos] = getResources().getString(R.string.menu_record_stop);
    		mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list, mOptions));
    	}
    	else if (option.equals( getResources().getString(R.string.menu_record_stop)) )
    	{
    		// On arrête le GPS
    		if (mGPS.isRunning())
    		{
    			mGPS.stop();
    			Log.i("Main", "GPS scanner stopped");
    		}
    		
    		// On arrête le scanner
    		if (mWifiScanner.isRunning())
    		{
    			mWifiScanner.stop(this);
    			Log.i("Main", "Wifi scanner stopped");
    		}
    		
    		// On change le menu pour "start"
    		mOptions[pos] = getResources().getString(R.string.menu_record_start);
    		mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list, mOptions));
    	}
    	else if (option.equals( getResources().getString(R.string.menu_options)) )
    	{
    		Intent intent = new Intent(this, SettingsActivity.class);
    		this.startActivityForResult(intent, RESULT_SETTINGS_ACTIVITY);
    	}
        else if(option.equals(getResources().getString(R.string.menu_testPush)))
        {
        	ClientTCP client = new ClientTCP(mWifiList, this);
        	client.start();
        }
        else if (option.equals(getResources().getString(R.string.info_gps)))
    	{
        	Location location = mGPS.getLocationApprox();
        	
    		AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("GPS Infos");
            if (location != null)
            {
            	builder.setMessage("Latitude: " + location.getLatitude() + "\nLongitude: " + location.getLongitude() + "\nAltitude: " + location.getAltitude() + "\nSpeed: "+location.getSpeed() );
            }
            else
            {
            	builder.setMessage("No good position available at the moment, please try again shortly.");
            }
            builder.setPositiveButton("OK", null);
            builder.setCancelable(false);
        	AlertDialog alert = builder.create();
    	    alert.show();
    	}
    	// Finalement on ferme le menu
        mDrawerLayout.closeDrawer(mDrawerList);
    }
    
    // Classe enregistrée auprès du menu pour détecter les sélections
    private class DrawerItemClickListener implements ListView.OnItemClickListener
    {
        @SuppressWarnings("rawtypes")
		@Override
        public void onItemClick(AdapterView parent, View view, int position, long id)
        {
        	// On redirige vers une autre méthode
        	itemSelected(position);
        }
    }
    
    // Fonction de rappel appelée lorsqu'un activité auquel on attendait un résultat a terminé
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode)
		{
			case RESULT_SETTINGS_ACTIVITY:
				// Paramètres ont possiblement été changés, on met les paramètres à jour
				
				// Offset de la boussole
				mCompass.setAzimuthOffset( SettingsActivity.getCompassOffset(this) );
				if (mCompassEnabled)
				{
					mCompass.notifyOrientation();
				}
				Log.i("onActivityResult", "Compass offset: " + SettingsActivity.getCompassOffset(this));
				
				// Interval de scan du wifi
				mWifiScanIntervalMS = SettingsActivity.getScanInterval(this);
				if (mWifiScanner.isRunning())
				{
					mWifiScanner.start(this, mWifiScanIntervalMS);
				}
				Log.i("onActivityResult", "Wifi scan interval: " + mWifiScanIntervalMS);
				
				// Interval GPS
				mGPSScanIntervalMS = SettingsActivity.getScanInterval(this);
				if (mGPS.isRunning())
				{
					mGPS.start(mGPSScanIntervalMS);
				}
				Log.i("onActivityResult", "GPS scan interval: " + mGPSScanIntervalMS);
				
				break;
		}
	}

    // Pour écouter les clicks sur les autes éléments de l'interface
	@SuppressWarnings("deprecation")
	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
			case R.id.btnCompass:
				// Active ou désactive la rotation automatique de la carte par le capteur d'orientation
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
	            	mCompass.addListener(mMap);
	            	mCompass.start();
				}
				break;
			case R.id.btnFilter:
				switch (mFilterState)
				{
					case OFF:
						mFilterState = FilterState.UNSECURED;
						mBtnFilter.setBackgroundDrawable(getResources().getDrawable(R.drawable.filter_unsecured));
						break;
					case UNSECURED:
						mFilterState = FilterState.VULN;
						mBtnFilter.setBackgroundDrawable(getResources().getDrawable(R.drawable.filter_vuln));
						break;
					case VULN:
						mFilterState = FilterState.SECURED;
						mBtnFilter.setBackgroundDrawable(getResources().getDrawable(R.drawable.filter_secured));
						break;	
					case SECURED:
						mFilterState = FilterState.OFF;
						mBtnFilter.setBackgroundDrawable(getResources().getDrawable(R.drawable.filter_off));
						mMap.resetFilter();
						return;
				}
				
				ArrayList<String> bssidsToShow = new ArrayList<String>();
				for (String bssid : mWifiList.keySet())
				{
					WifiInfo i = mWifiList.get(bssid);
			    	if (mFilterState == FilterState.VULN && i.capabilities.contains("WEP"))
			    	{
			    		bssidsToShow.add(bssid);
			    	}
			    	else if (mFilterState == FilterState.SECURED && i.capabilities.contains("WPA"))
			    	{
			    		bssidsToShow.add(bssid);
			    	}
			    	else if (mFilterState == FilterState.UNSECURED && !i.capabilities.contains("WPA") && !i.capabilities.contains("WEP"))
			    	{
			    		bssidsToShow.add(bssid);
			    	}
				}
				
				mMap.applyFilter(bssidsToShow);
				break;
		}
	}

	@Override
	public void onNewWifiFound(WifiInfo newInfo)
	{
		// On vérifie si existe déjà dans la liste ou non
		WifiInfo oldInfo = mWifiList.get(newInfo.BSSID);
		if (oldInfo == null)
		{
			// Va chercher la position actuelle
			Location location = mGPS.getLocationPrecise();
			
			// On ajoute seulement le wifi si on a une position valide
			if (location != null)
			{
				Log.i("Main", "New wifi: " + newInfo.SSID + " [" + newInfo.BSSID + "]");
				
				newInfo.latitude = location.getLatitude();
				newInfo.longitude = location.getLongitude();
				newInfo.altitude = location.getAltitude();
				
				// Inconu, on ajoute
				mWifiList.put(newInfo.BSSID, newInfo);
				
				// Ajoute sur la map
				mMap.setWifiMarker(newInfo);
				
				// ajouter à la BD locale
				LocalDatabase.getInstance(this).insertAccessPoint(newInfo);
			}
		}
		else if (newInfo.distance < oldInfo.distance)
		{
			// Va chercher la position actuelle
			Location location = mGPS.getLocationPrecise();
			
			// On ajoute seulement le wifi si on a une position valide
			if (location != null)
			{
				Log.i("Main", "Update wifi: " + newInfo.SSID + " [" + newInfo.BSSID + "]");
				
				newInfo.latitude = location.getLatitude();
				newInfo.longitude = location.getLongitude();
				newInfo.altitude = location.getAltitude();
				
				// Existe, mais la distance est plus courte donc meilleure précision => on update
				mWifiList.put(newInfo.BSSID, newInfo);
				
				// Update position sur la map
				mMap.setWifiMarker(newInfo);
				
				// met à jour dans la BD locale
				LocalDatabase.getInstance(this).removeAccessPoint(newInfo);
				LocalDatabase.getInstance(this).insertAccessPoint(newInfo);
			}
		}
	}

	@Override
	public void onMarkerClick(String wifiBSSID)
	{
		WifiInfo w = mWifiList.get(wifiBSSID);

		String info = "SSID: " + w.SSID +
					  "\nBSSID: " + w.BSSID +
					  "\nSecured: "	+ (w.secured ? "Yes" : "No") +
					  "\n" + w.capabilities +
					  "\nFreq: " + (float) (w.frequency / 1000.0) + " GHz" +
					  "\nLevel where captured: " + w.level + " dBm" +
					  "\nLatitude: " + w.latitude +
					  "\nLongitude: " + w.longitude +
					  "\nAltitude: " + (int)w.altitude + "m";

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Access point informations")
			   .setMessage(info)
			   .setCancelable(false).setPositiveButton("OK", null);
		final AlertDialog d = builder.create();
		d.show();

		TextView textView = (TextView) d.findViewById(android.R.id.message);
		textView.setTextSize(14);
	}
}
