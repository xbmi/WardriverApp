package com.inf8405.wardriver.map;

public interface WifiMapClickListener
{
	// Appelé lorsque l'utilisateur clique sur un bulle d'information
	// d'un marqueur pour avoir plus de détails
	public void onMarkerInfoClick(String wifiBSSID);
}
