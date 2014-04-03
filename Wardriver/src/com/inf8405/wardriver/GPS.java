package com.inf8405.wardriver;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;

public class GPS
{
	private LocationManager mLocationManager; 
	
	public GPS(Context context)
	{
		mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
	}
	
	public Location getLocation()
	{
		Criteria criteria = new Criteria();
		String bestProvider = mLocationManager.getBestProvider(criteria, false);
		return mLocationManager.getLastKnownLocation(bestProvider);
	}
}
