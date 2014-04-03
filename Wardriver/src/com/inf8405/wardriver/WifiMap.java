package com.inf8405.wardriver;

import java.util.HashMap;
import java.util.LinkedList;

import android.app.FragmentManager;
import android.graphics.Color;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class WifiMap implements CompassListener, OnMarkerClickListener, OnInfoWindowClickListener
{
	private GoogleMap mMap;
	
	private LinkedList<WifiMapClickListener> wifiClickListeners = new LinkedList<WifiMapClickListener>();
	
	private HashMap<String, Marker> mMarkers = new HashMap<String, Marker>();
	private HashMap<String, CircleOptions> mCircleOptions = new HashMap<String, CircleOptions>();
	
	private HashMap<Marker, String> mMarkersBSSID = new HashMap<Marker, String>();
	
	private Circle shownCircle;
	
	// Carte google sur laquelle on indique les points d'acc�s wifi
	public WifiMap(FragmentManager fragMgr)
	{
        mMap = ((MapFragment) fragMgr.findFragmentById(R.id.map)).getMap();
        mMap.setOnMarkerClickListener(this);
        mMap.setOnInfoWindowClickListener(this);
        mMap.setMyLocationEnabled(true);
        mMarkers.clear();
        mCircleOptions.clear();
	}
	
	public void addWifiClickListener(WifiMapClickListener l)
	{
		wifiClickListeners.add(l);
	}
	
	public void removeWifiClickListener(WifiMapClickListener l)
	{
		wifiClickListeners.remove(l);
	}
	
	// Reset l'orientation de la carte vers le Nord
	public void resetOrientation()
	{
		// On remet la cam�ra droite (vers le nord)
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
	
	// Ajoute un marqueur sur la carte pour un points d'acc�s wifi
	public void setWifiMarker(WifiInfo w)
	{
		// V�rifie si existe d�j�
		Marker m = mMarkers.get(w.BSSID);
		CircleOptions cOptions = mCircleOptions.get(w.BSSID);
		if (m != null /*&& c != null*/)
		{
			// Existe d�j�! On l'update
			m.setPosition(new LatLng(w.latitude, w.longitude));
			cOptions.center(new LatLng(w.latitude, w.longitude));
			cOptions.radius(w.distance);
		}
		else
		{
			// N'existe pas, on ajoute un nouveau
	    	MarkerOptions marker = new MarkerOptions()
	    		.position(new LatLng(w.latitude, w.longitude))
	    		.title(w.SSID)
	    		.snippet("Click for more info");
	    	
			// Marqueur vert pour wifi s�curis�
			// Marqueur rouge pour wifi non-s�curis�
	    	// Marqueur orange pour wifi vuln�rable ou autre
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
	    	
	    	// Et un cercle pour l'impr�cision (distance selon puissance)
	    	cOptions = new CircleOptions()
				 .center(new LatLng(w.latitude, w.longitude))
				 .radius(w.distance) // en m�tres
				 .strokeColor(Color.TRANSPARENT)
				 .fillColor(circleFillColor);
	
	    	m = mMap.addMarker(marker);
	    	mMarkers.put(w.BSSID, m);
	    	mCircleOptions.put(w.BSSID, cOptions); // On va seulement afficher le cercle quand le wifi est s�lectionn�, sinon lag
	    	mMarkersBSSID.put(m, w.BSSID);
		}
	}
	
	// Enl�ve tous les merqueurs de la carte
	public void clear()
	{
		mMap.clear();
	}

	// Appel� lors d'un changement d'orientation si l'objet actuel est enregistr�
	@Override
	public void onOrientationChanged(float azimuth)
	{
		rotateTo(azimuth);
	}

	@Override
	public void onInfoWindowClick(Marker m)
	{
		for (WifiMapClickListener l : wifiClickListeners)
		{
			l.onMarkerClick( mMarkersBSSID.get(m) );
		}
	}

	@Override
	public boolean onMarkerClick(Marker m)
	{
		// On clear le dernier cercle affich�
		if (shownCircle != null)
		{
			shownCircle.remove();
		}
		
		// On affiche le cercle du wifi
		String BSSID = mMarkersBSSID.get(m);
		shownCircle = mMap.addCircle( mCircleOptions.get(BSSID) );
		return false;
	}
}
