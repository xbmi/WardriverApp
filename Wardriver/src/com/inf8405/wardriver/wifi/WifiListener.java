package com.inf8405.wardriver.wifi;

import com.inf8405.wardriver.wifi.WifiInfo;

public interface WifiListener
{
	// Pour s'enregistrer apr�s du WifiScanner et se faire appeler
	// lorsqu'un nouveau wifi est d�tect�
	public void onWifiFound(WifiInfo r);
}
