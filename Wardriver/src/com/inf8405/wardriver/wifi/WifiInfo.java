package com.inf8405.wardriver.wifi;

import android.net.wifi.ScanResult;

public class WifiInfo
{
	public String SSID = "";
	public String BSSID = "";
	public boolean secured = false;
	public String capabilities = "";
	public float frequency = 0f;
	public int level = 0;
	public int distance = 0;
	public double latitude = 0;
	public double longitude = 0;
	public double altitude = 0;
	public int userId = 0;
	
	public WifiInfo() {
	}
	
	public WifiInfo(ScanResult r)
	{
		SSID = r.SSID;
		BSSID = r.BSSID;
		secured = r.capabilities.contains("WPA") || r.capabilities.contains("WEP");
		capabilities = r.capabilities;
		frequency = r.frequency;
		level = r.level;
		distance = estimateRouterDistance(r.level, r.frequency);
	}
	
	// Fonction qui estime la distance d'un routeur de maison en fonction de la
	// force du signal (dBm) et sa fr�quence (MHz). Retourne la distance approx en m�tres.
	public static int estimateRouterDistance(double signalStrengthInDBm, double freqInMHz)
	{
		// Formule originale pour "Free-space path loss in decibels"
	    //double exp = (27.55 - (20 * Math.log10(freqInMHz)) - signalStrengthInDBm) / 20.0;
	    //return Math.pow(10.0, exp);
	    
		// Formule modifi�e par nous pour prendre compte que les routeur sont g�n�ralement
		// dans des maisons avec plusieurs murs qui r�duit rapidement la distance
	    double exp = (80.0 - (20 * Math.log10(freqInMHz)) - signalStrengthInDBm) / 20.0;
	    return (int)Math.pow(2.0, exp);
	}
}
