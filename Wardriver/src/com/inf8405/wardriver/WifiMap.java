package com.inf8405.wardriver;

import android.app.FragmentManager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class WifiMap
{
	public enum MarkerType { SECURED, VULNERABLE, UNSECURED };
	
	private GoogleMap mMap;
	
	public WifiMap(FragmentManager fragMgr)
	{
        mMap = ((MapFragment) fragMgr.findFragmentById(R.id.map)).getMap();
        mMap.setMyLocationEnabled(true);
	}
	
	public void resetOrientation()
	{
		// On remet la caméra droite (vers le nord)
		CameraPosition camPos = new CameraPosition.Builder(mMap.getCameraPosition())
			.bearing(0)
			.build();
		mMap.stopAnimation();
		mMap.animateCamera(CameraUpdateFactory.newCameraPosition(camPos));
	}
	
	public void rotateTo(float azimuth)
	{
		CameraPosition camPos = CameraPosition.builder(mMap.getCameraPosition())
			.bearing(azimuth)
			.build();
		mMap.stopAnimation();
		mMap.animateCamera(CameraUpdateFactory.newCameraPosition(camPos));
	}
	
	public void addWifiMarker(LatLng pos, String title, MarkerType type)
	{
    	MarkerOptions marker = new MarkerOptions()
    		.position(pos)
    		.title(title)
    		.snippet("Click for more info");
    	
		// Marqueur vert pour wifi sécurisé
		// Marqueur rouge pour wifi non-sécurisé
    	// Marqueur orange pour wifi vulnérable ou autre
    	switch (type)
    	{
			case SECURED:
				marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
				break;
			case UNSECURED:
				marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
				break;
			case VULNERABLE:
				marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
				break;
			default:
				marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
				break;
    	}

    	mMap.addMarker(marker);
	}
}
