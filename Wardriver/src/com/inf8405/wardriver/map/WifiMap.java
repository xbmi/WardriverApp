package com.inf8405.wardriver.map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import android.app.FragmentManager;
import android.graphics.Color;

import com.google.android.gms.maps.CameraUpdate;
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
import com.inf8405.wardriver.R;
import com.inf8405.wardriver.compass.CompassListener;
import com.inf8405.wardriver.wifi.WifiInfo;

public class WifiMap implements CompassListener, OnMarkerClickListener, OnInfoWindowClickListener
{
	private GoogleMap mMap;
	
	private LinkedList<WifiMapClickListener> wifiClickListeners = new LinkedList<WifiMapClickListener>();
	
	private HashMap<String, Marker> mMarkers = new HashMap<String, Marker>();
	private HashMap<String, CircleOptions> mCircleOptions = new HashMap<String, CircleOptions>();
	
	private HashMap<Marker, String> mMarkersBSSID = new HashMap<Marker, String>();
	
	private Circle shownCircle;
	
	private final int UNSECURED_COLOR = Color.argb(90, 240, 110, 110);
	private final int VULN_COLOR = Color.argb(90, 240, 175, 105);
	private final int SECURED_COLOR = Color.argb(90, 110, 240, 110);

	
	// Carte google sur laquelle on indique les points d'accès wifi
	public WifiMap(FragmentManager fragMgr)
	{
        mMap = ((MapFragment) fragMgr.findFragmentById(R.id.map)).getMap();
        mMap.setOnMarkerClickListener(this);
        mMap.setOnInfoWindowClickListener(this);
        mMap.setMyLocationEnabled(true);
        mMarkers.clear();
        mCircleOptions.clear();
        mMarkersBSSID.clear();
        wifiClickListeners.clear();
	}
	
	// Ajoute un listener
	public void addWifiClickListener(WifiMapClickListener l)
	{
		wifiClickListeners.add(l);
	}
	
	// Désenregistre un listener
	public void removeWifiClickListener(WifiMapClickListener l)
	{
		wifiClickListeners.remove(l);
	}
	
	// Reset l'orientation de la carte vers le Nord
	public void resetOrientation()
	{
		// On remet la caméra droite (vers le nord) avec une animation
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
		mMap.moveCamera(CameraUpdateFactory.newCameraPosition(camPos)); // sans animation
	}
	
	// Ajoute un marqueur sur la carte pour un points d'accès wifi
	public void setWifiMarker(WifiInfo w)
	{
		// Vérifie si un markeur existe déjà pour ce BSSID
		Marker m = mMarkers.get(w.BSSID);
		CircleOptions cOptions = mCircleOptions.get(w.BSSID);
		if (m != null && cOptions != null)
		{
			// Existe déjà! On le met à jour (position et rayon)
			m.setPosition(new LatLng(w.latitude, w.longitude));
			cOptions.center(new LatLng(w.latitude, w.longitude));
			cOptions.radius(w.distance);
		}
		else
		{
			// Aucun marqueur existant, on en ajoute un nouveau
			String title = w.SSID; // titre est le SSID
			if (title.isEmpty())
			{
				title = "No SSID (hidden)"; // Certains wifi ne propagent aucun SSID
			}
	    	MarkerOptions marker = new MarkerOptions()
	    		.position(new LatLng(w.latitude, w.longitude))
	    		.title(title)
	    		.snippet("Click for more info");
	    	
			// Marqueur vert pour wifi sécurisé
			// Marqueur rouge pour wifi non-sécurisé
	    	// Marqueur orange pour wifi vulnérable ou autre
	    	int circleFillColor = Color.TRANSPARENT;
	    	if (w.capabilities.contains("WEP"))
	    	{
				marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
				circleFillColor = VULN_COLOR;
	    	}
	    	else if (w.capabilities.contains("WPA"))
	    	{
				marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
				circleFillColor = SECURED_COLOR;
	    	}
	    	else
	    	{
				marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
				circleFillColor = UNSECURED_COLOR;
	    	}
	    	
	    	// Et un cercle pour l'imprécision (distance selon puissance)
	    	cOptions = new CircleOptions()
				 .center(new LatLng(w.latitude, w.longitude))
				 .radius(w.distance) // en mètres
				 .strokeColor(Color.TRANSPARENT)
				 .fillColor(circleFillColor);
	
	    	m = mMap.addMarker(marker); // On ajoute à la carte
	    	mMarkers.put(w.BSSID, m);
	    	mCircleOptions.put(w.BSSID, cOptions); // On va seulement afficher le cercle quand le wifi est sélectionné
	    	mMarkersBSSID.put(m, w.BSSID);
		}
	}
	
	// Enlève toutes les données de la carte
	public void reset()
	{
		mMap.clear();
        mMarkers.clear();
        mCircleOptions.clear();
        mMarkersBSSID.clear();
        wifiClickListeners.clear();
        shownCircle = null;
	}

	// Appelé lors d'un changement d'orientation si l'objet actuel est enregistré auprès du "Compass"
	@Override
	public void onDeviceAzimuthChanged(float azimuth)
	{
		rotateTo(azimuth);
	}

	// Appelé lorsque l'utilisateur clique sur la bulle d'informations d'un marqueur
	@Override
	public void onInfoWindowClick(Marker m)
	{
		// On appèle les listeners
		for (WifiMapClickListener l : wifiClickListeners)
		{
			l.onMarkerInfoClick( mMarkersBSSID.get(m) );
		}
	}

	// Appelé lorsque l'utilisateur clique sur un marqueur pour le sélectionner
	@Override
	public boolean onMarkerClick(Marker m)
	{
		// On clear le dernier cercle affiché
		if (shownCircle != null)
		{
			shownCircle.remove();
		}
		
		// On affiche le cercle (rayon selon puissance) du wifi
		String BSSID = mMarkersBSSID.get(m);
		shownCircle = mMap.addCircle( mCircleOptions.get(BSSID) );
		return false;
	}
	
	// Déplace la caméra avec une animation vers une position recue
	public void zoomOnLocation(double latitude, double longitute)
	{
        LatLng coordinate = new LatLng(latitude, longitute);
        CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(coordinate, 15);
        mMap.animateCamera(yourLocation);
	}
	
	// Applique un filtre sur les marqueurs à afficher sur la carte
	public void applyFilter(ArrayList<String> bssidsToShow)
	{
		for (String bssid : mMarkers.keySet())
		{
			// Si le marqueur (identifié par le BSSID) est contenu dans la liste, on le met visible sinon non
			mMarkers.get(bssid).setVisible(bssidsToShow.contains(bssid));
		}
	}
	
	// Enlève tout filtre et affiche tous les marqueurs connus
	public void resetFilter()
	{
		for (String bssid : mMarkers.keySet())
		{
			mMarkers.get(bssid).setVisible(true);
		}
	}
}
