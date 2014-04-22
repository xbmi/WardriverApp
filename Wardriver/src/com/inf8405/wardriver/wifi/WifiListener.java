package com.inf8405.wardriver.wifi;

import com.inf8405.wardriver.wifi.WifiInfo;

public interface WifiListener
{
	// Pour s'enregistrer après du WifiScanner et se faire appeler
	// lorsqu'un nouveau wifi est trouvé (ou meilleure précision)
	public void onNewWifiFound(WifiInfo r);
}
