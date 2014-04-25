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
	
	// D�marre la localisation GPS avec un certain intervale de mise � jour
	public void start(int updateIntervalMS)
	{
		mRunning = true;		
		mLocationManager.removeUpdates(this);		
		mUpdateIntervalMS = updateIntervalMS;
		
		// On demande des mises � jour � la fois du GPS et network provider selon l'interval recu
		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, mUpdateIntervalMS, 0, this);
		mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, mUpdateIntervalMS, 0, this);
	}
	
	// On arr�te la localisation
	public void stop()
	{
		mRunning = false;
		mLocationManager.removeUpdates(this);
		mLocationPrecise = null;
	}
	
	// Retourne si la localisation est pr�sentement active
	public boolean isRunning()
	{
		return mRunning;
	}
	
	// Retourne la position pr�cise si elle est disponible et suffisament fraiche, sinon null
	public Location getLocationPrecise()
	{
		// V�rifie si la location est trop vieille (seuil de 3 * l'intervale de mise � jour)
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
	
	// Retourne la derni�re position connue qui peux �tre vieille ou approximative
	public Location getLocationApprox()
	{
		String bestProvider = mLocationManager.getBestProvider(new Criteria(), false);
		return mLocationManager.getLastKnownLocation( bestProvider );
	}

	// Appel� lors d'un update de position recu par le GPS ou le "network provider"
	@Override
	public void onLocationChanged(Location location)
	{
		// Si on a deja une position non-expir�e
		if (mLocationPrecise != null && System.currentTimeMillis() - mLocationPrecise.getTime() < 3 * mUpdateIntervalMS)
		{	
			// On v�rifie si plus pr�cis si la nouvelle est plus pr�cise
			if (mLocationPrecise.getAccuracy() > 0 && location.getAccuracy() > 0 && location.getAccuracy() < mLocationPrecise.getAccuracy())
			{
				// La nouvelle est plus pr�cise, on prend celle-ci
				mLocationPrecise = location;
				Log.i("GPS", "Better accuracy :)");
			}
			Log.i("GPS", "Already have better location");
		}
		else
		{
			// Notre position actuelle est null ou expir�e, on utilise la nouvelle
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
