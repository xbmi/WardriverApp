package com.inf8405.wardriver.map;

public interface WifiMapClickListener
{
	// Appel� lorsque l'utilisateur clique sur un bulle d'information
	// d'un marqueur pour avoir plus de d�tails
	public void onMarkerInfoClick(String wifiBSSID);
}
