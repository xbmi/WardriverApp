package com.inf8405.wardriver;

public interface WifiListener
{
	// Pour s'enregistrer apr�s du WifiScanner et se faire appeler
	// lorsqu'un nouveau wifi est trouv� (ou meilleure pr�cision)
	public void onNewWifiFound(WifiInfo r);
}
