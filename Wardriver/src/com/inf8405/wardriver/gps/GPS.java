package com.inf8405.wardriver.gps;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public class GPS implements LocationListener
{
	private LocationManager mLocationManager; 
	private Location mLocationPrecise;
	private int mUpdateIntervalMS = 3000;
	private boolean mRunning = false;
	
	public GPS(Context context)
	{
		mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		mLocationPrecise = null;
		mRunning = false;
	}
	
	public void start(int updateIntervalMS)
	{
		mRunning = true;
		
		mLocationManager.removeUpdates(this);
		
		mUpdateIntervalMS = updateIntervalMS;
		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, mUpdateIntervalMS, 0, this);
		mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, mUpdateIntervalMS, 0, this);
		Log.i("GPS", "Request location updates every " + mUpdateIntervalMS + "ms");
	}
	
	public void stop()
	{
		mRunning = false;
		mLocationManager.removeUpdates(this);
		mLocationPrecise = null;
	}
	
	public boolean isRunning()
	{
		return mRunning;
	}
	
	public Location getLocationPrecise()
	{
		// Vérifie si la location est trop vieille
		if (mLocationPrecise != null && System.currentTimeMillis() - mLocationPrecise.getTime() > 3 * mUpdateIntervalMS)
		{
			Log.i("GPS", "Location too old :(");
			mLocationPrecise = null;
			return mLocationPrecise;
		}
		else
		{
			return mLocationPrecise;
		}
	}
	
	public Location getLocationApprox()
	{
		String bestProvider = mLocationManager.getBestProvider(new Criteria(), false);
		return mLocationManager.getLastKnownLocation( bestProvider );
	}

	@Override
	public void onLocationChanged(Location location)
	{
		// Si on a deja une position non-expirée
		if (mLocationPrecise != null && System.currentTimeMillis() - mLocationPrecise.getTime() < 3 * mUpdateIntervalMS)
		{	
			// Vérifie si plus précis
			if (mLocationPrecise.getAccuracy() > 0 && location.getAccuracy() > 0 && location.getAccuracy() < mLocationPrecise.getAccuracy())
			{
				//plus précis
				mLocationPrecise = location;
				Log.i("GPS", "Better accuracy :)");
			}
			Log.i("GPS", "Already have better location");
		}
		else
		{
			// Null ou expiré, on met à jour
			mLocationPrecise = location;
			Log.i("GPS", "Location changed!");
		}
	}

	@Override
	public void onProviderDisabled(String provider)
	{
		// Vide
	}

	@Override
	public void onProviderEnabled(String provider)
	{
		// Vide
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras)
	{
		// Vide
	}
}
