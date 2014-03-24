package com.inf8405.wardriver;

import android.net.wifi.ScanResult;

public interface WifiListener
{
	public void onNewWifiFound(ScanResult r);
}
