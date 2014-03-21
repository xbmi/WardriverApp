package com.inf8405.wardriver;

import android.app.FragmentManager;
import android.graphics.Color;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
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
		// On remet la cam�ra droite (vers le nord)
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
		// Un marqueur
    	MarkerOptions marker = new MarkerOptions()
    		.position(pos)
    		.title(title)
    		.snippet("Click for more info");
    	
		// Marqueur vert pour wifi s�curis�
		// Marqueur rouge pour wifi non-s�curis�
    	// Marqueur orange pour wifi vuln�rable ou autre
    	int circleFillColor = Color.TRANSPARENT;
    	switch (type)
    	{
			case SECURED:
				marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
				circleFillColor = Color.argb(105, 110, 240, 110);
				break;
			case UNSECURED:
				marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
				circleFillColor = Color.argb(105, 240, 110, 110);
				break;
			case VULNERABLE:
			default:
				marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
				circleFillColor = Color.argb(105, 240, 175, 105);
				break;
    	}
    	
    	// Et un cercle pour l'inpr�cision
    	CircleOptions circle = new CircleOptions()
			 .center(pos)
			 .radius(35) // en m�tres
			 .strokeColor(Color.TRANSPARENT)
			 .fillColor(circleFillColor);

    	mMap.addMarker(marker);
    	mMap.addCircle(circle);
	}
}
