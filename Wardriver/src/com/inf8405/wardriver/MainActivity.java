package com.inf8405.wardriver;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
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
import com.inf8405.wardriver.db.DBSyncListener;
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

	// Attributs liés à l'interface usager
	private String[] mOptions;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private Button mBtnFilter;
    
    // Liste principale des WifiInfo
    private HashMap<String, WifiInfo> mWifiList = new HashMap<String, WifiInfo>();
    
    // Carte
    private WifiMap mMap;
    
    // Scanner wifi
    private WifiScanner mWifiScanner;
    private int mWifiScanIntervalMS = 3000;
    
    // Boussole
    private Compass mCompass;
    private boolean mCompassEnabled = false;
    private Button mBtnCompass;

    // GPS
    private GPS mGPS;
    private int mGPSScanIntervalMS = 3000;
    
    // Attributs liés au filtre
	private enum FilterState { OFF, UNSECURED, VULN, SECURED }
	private FilterState mFilterState = FilterState.OFF;
	
    
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);

		// On récupère et ajuste le "drawer" (paneau coulissant de gauche)
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
        
        // On construit le module pour la localisation
        mGPS = new GPS(this);
        mGPSScanIntervalMS = SettingsActivity.getScanInterval(this);
        
        // On zoom sur la position actuelle de l'usager
        Location location = mGPS.getLocationApprox();
        if (location != null)
        {
        	mMap.zoomOnLocation(location.getLatitude(), location.getLongitude());
        }
        
        // On charge la base de donnée locale et met à jour la carte
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
	    mDrawerToggle.syncState(); // on garde à jour le drawer
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
	    super.onConfigurationChanged(newConfig);
	    mDrawerToggle.onConfigurationChanged(newConfig); // on garde à jour le drawer
	}
	
	// Lorsque l'application est mise en pause (change d'application par exemple)
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
	
	// Lorsque notre application revient en premier plan
	@Override
	public void onResume()
	{
		super.onResume();
		
		// On redémarre la boussole si elle était préalablement activée
		if (mCompassEnabled)
		{
			mCompass.start();
		}
	}
	
	// Lorsque le bouton de retour est appuyé, on demande confirmation pour quitter
	@Override
	public void onBackPressed()
	{
		// Dialogue d'avertissement et de confirmation
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Quit Wardriver")
			   .setMessage("Do you really want to leave the app? The scanning will be stopped.")
			   .setCancelable(false)
			   .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// On arrête le scanneur de WiFi et GPS
						if (mWifiScanner.isRunning())
						{
							mWifiScanner.stop(MainActivity.this);
						}
						if (mGPS.isRunning())
						{
							mGPS.stop();
						}
						// On termine l'application
						finish();
					}
			   })
			   .setNegativeButton("No", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// On cancelle le dialogue
						dialog.cancel();
					}
			   });
	    builder.show();
	}
	
	// Pour controler l'ouverture et la fermeture du drawer
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
    	if (option.equals( getResources().getString(R.string.menu_record_start)) ) // START RECORDING
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
    	else if (option.equals( getResources().getString(R.string.menu_record_stop)) ) // STOP RECORDING
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
    	else if (option.equals( getResources().getString(R.string.menu_options)) ) // OPTIONS
    	{
    		// On ouvre l'activité des préférences
    		Intent intent = new Intent(this, SettingsActivity.class);
    		this.startActivityForResult(intent, RESULT_SETTINGS_ACTIVITY);
    	}
        else if(option.equals(getResources().getString(R.string.menu_testPush))) // SYNCRHONISATION AVEC SERVEUR
        {
        	// On synchronise avec un serveur
        	Integer taille = -1;
        	HashMap<String, WifiInfo> hm = null;
        	
        	hm = LocalDatabase.getInstance(this).getAllAccessPoints();
			taille = hm.size();
			System.out.println("Nombre d'entrees dans bd locale: " + taille.toString());
        	
        	ClientTCP client = new ClientTCP(mWifiList, this);
        	client.start(new DBSyncListener() {
				@Override
				public void onDBSynced() {
		        	// On met à jour la liste locale et la carte sur le main thread
					Handler mainHandler = new Handler(MainActivity.this.getMainLooper());
					Runnable myRunnable = new Runnable() {
						@Override
						public void run() {
							reloadAllFromDB();
						}
					};
					mainHandler.post(myRunnable);
				}
			});
        }
        else if (option.equals(getResources().getString(R.string.menu_info_gps))) // INFOS GPS
    	{
        	// On affiche diverses informations approximatives sur la position
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
        else if (option.equals(getResources().getString(R.string.menu_clear_map))) // INFOS GPS
    	{
        	mMap.reset();
        	mWifiList.clear();
        	LocalDatabase.getInstance(this).emptyTable();
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
				// Paramètres ont possiblement été changés, on met les attributs à jour
				
				// Offset de la boussole
				mCompass.setAzimuthOffset( SettingsActivity.getCompassOffset(this) );
				if (mCompassEnabled)
				{
					mCompass.notifyListeners();
				}
				Log.i("onActivityResult", "Compass offset: " + SettingsActivity.getCompassOffset(this));
				
				// Interval de scan du wifi
				mWifiScanIntervalMS = SettingsActivity.getScanInterval(this);
				if (mWifiScanner.isRunning())
				{
					mWifiScanner.start(this, mWifiScanIntervalMS);
				}
				Log.i("onActivityResult", "Wifi scan interval: " + mWifiScanIntervalMS);
				
				// Interval de mise à jour du GPS
				mGPSScanIntervalMS = SettingsActivity.getScanInterval(this);
				if (mGPS.isRunning())
				{
					mGPS.start(mGPSScanIntervalMS);
				}
				Log.i("onActivityResult", "GPS scan interval: " + mGPSScanIntervalMS);
				
				break;
		}
	}

    // Pour écouter les clics sur les autres éléments de l'interface
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
				// Applique un filtre sur les marqueurs affichés sur la carte
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
				
				// On construit une liste des marqueurs qui devraient être affichés (connus par le BSSID)
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
				
				// On applique le filtre
				mMap.applyFilter(bssidsToShow);
				break;
		}
	}

	// Fonction appelée par le WifiScanner lorsque des informations sur une borne WiFi on été trouvées
	@Override
	public void onWifiFound(WifiInfo newInfo)
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
				
				// Wifi inconnu, on ajoute
				mWifiList.put(newInfo.BSSID, newInfo);
				
				// Ajoute marqueur sur la carte
				mMap.setWifiMarker(newInfo);
				
				// Ajouter à la BD locale
				LocalDatabase.getInstance(this).insertAccessPoint(newInfo);
			}
		}
		else if (newInfo.distance < oldInfo.distance)
		{
			// Le Wifi est déjà connu mais la nouvelle distance est plus courte!
			
			// On va chercher la position actuelle
			Location location = mGPS.getLocationPrecise();
			
			// On ajoute seulement le wifi si on a une position valide
			if (location != null)
			{
				Log.i("Main", "Update wifi: " + newInfo.SSID + " [" + newInfo.BSSID + "]");
				
				newInfo.latitude = location.getLatitude();
				newInfo.longitude = location.getLongitude();
				newInfo.altitude = location.getAltitude();
				
				// Meilleure précision => on met à jour notre information
				mWifiList.put(newInfo.BSSID, newInfo);
				
				// Met à jour la position sur la carte
				mMap.setWifiMarker(newInfo);
				
				// Met à jour dans la BD locale
				LocalDatabase.getInstance(this).removeAccessPoint(newInfo);
				LocalDatabase.getInstance(this).insertAccessPoint(newInfo);
			}
		}
	}

	
	// Appelé lorsque l'utilisateur clique sur une bulle d'information d'un marqueur pour avoir plus d'informations
	@Override
	public void onMarkerInfoClick(String wifiBSSID)
	{
		// On trouve l'information associée
		WifiInfo w = mWifiList.get(wifiBSSID);

		// On affiche un dialogue avec toutes les informations
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
	
	// Méthode qui efface la liste de WiFi et la carte et recharge tout à partir de la base de donnée
	private void reloadAllFromDB()
	{
		mMap.reset();
        mWifiList = LocalDatabase.getInstance(MainActivity.this).getAllAccessPoints();
        for (String key : mWifiList.keySet())
        {
        	mMap.setWifiMarker(mWifiList.get(key));
        }
	}
}
