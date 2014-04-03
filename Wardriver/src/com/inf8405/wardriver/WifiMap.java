package com.inf8405.wardriver;

import java.util.HashMap;

import android.app.FragmentManager;
import android.graphics.Color;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class WifiMap implements CompassListener
{
	private GoogleMap mMap;
	
	private HashMap<String, Marker> mMarkers = new HashMap<String, Marker>();
	private HashMap<String, Circle> mCircles = new HashMap<String, Circle>();
	
	// Carte google sur laquelle on indique les points d'accès wifi
	public WifiMap(FragmentManager fragMgr)
	{
        mMap = ((MapFragment) fragMgr.findFragmentById(R.id.map)).getMap();
        mMap.setMyLocationEnabled(true);
        mMarkers.clear();
        mCircles.clear();
	}
	
	// Reset l'orientation de la carte vers le Nord
	public void resetOrientation()
	{
		// On remet la caméra droite (vers le nord)
		CameraPosition camPos = new CameraPosition.Builder(mMap.getCameraPosition())
			.bearing(0)
			.build();
		mMap.stopAnimation();
		mMap.animateCamera(CameraUpdateFactory.newCameraPosition(camPos));
	}
	
	// Tourne la carte vers un certain azimuth
	public void rotateTo(float azimuth)
	{
		CameraPosition camPos = CameraPosition.builder(mMap.getCameraPosition())
			.bearing(azimuth)
			.build();
		mMap.stopAnimation();
		mMap.animateCamera(CameraUpdateFactory.newCameraPosition(camPos));
	}
	
	// Ajoute un marqueur sur la carte pour un points d'accès wifi
	public void setWifiMarker(WifiInfo w)
	{
		// Vérifie si existe déjà
		Marker m = mMarkers.get(w.BSSID);
		Circle c = mCircles.get(w.BSSID);
		if (m != null && c != null)
		{
			// Existe déjà! On l'update
			m.setPosition(new LatLng(w.latitude, w.longitude));
			c.setCenter(new LatLng(w.latitude, w.longitude));
			c.setRadius(w.distance);
		}
		else
		{
			// N'existe pas, on ajoute un nouveau
	    	MarkerOptions marker = new MarkerOptions()
	    		.position(new LatLng(w.latitude, w.longitude))
	    		.title(w.SSID)
	    		.snippet("Click for more info");
	    	
	    	// TODO: checker comment faire pour afficher une page quand on click pour plus d'infos
	    	
			// Marqueur vert pour wifi sécurisé
			// Marqueur rouge pour wifi non-sécurisé
	    	// Marqueur orange pour wifi vulnérable ou autre
	    	int circleFillColor = Color.TRANSPARENT;
	    	if (w.capabilities.contains("WEP"))
	    	{
				marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
				circleFillColor = Color.argb(90, 240, 175, 105);
	    	}
	    	else if (w.capabilities.contains("WPA"))
	    	{
				marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
				circleFillColor = Color.argb(90, 110, 240, 110);
	    	}
	    	else
	    	{
				marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
				circleFillColor = Color.argb(90, 240, 110, 110);
	    	}
	    	
	    	// Et un cercle pour l'imprécision (distance selon puissance)
	    	CircleOptions circle = new CircleOptions()
				 .center(new LatLng(w.latitude, w.longitude))
				 .radius(w.distance) // en mètres
				 .strokeColor(Color.TRANSPARENT)
				 .fillColor(circleFillColor);
	
	    	m = mMap.addMarker(marker);
	    	c = mMap.addCircle(circle);
	    	mMarkers.put(w.BSSID, m);
	    	mCircles.put(w.BSSID, c);
		}
	}
	
	// Enlève tous les merqueurs de la carte
	public void clear()
	{
		mMap.clear();
	}

	// Appelé lors d'un changement d'orientation si l'objet actuel est enregistré
	@Override
	public void onOrientationChanged(float azimuth)
	{
		rotateTo(azimuth);
	}
}
