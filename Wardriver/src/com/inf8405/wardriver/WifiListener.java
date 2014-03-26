package com.inf8405.wardriver;

import android.net.wifi.ScanResult;

public interface WifiListener
{
	// Pour s'enregistrer apr�s du WifiScanner et se faire appeler
	// lorsqu'un nouveau wifi est trouv� (ou meilleure pr�cision)
	public void onNewWifiFound(ScanResult r);
}
