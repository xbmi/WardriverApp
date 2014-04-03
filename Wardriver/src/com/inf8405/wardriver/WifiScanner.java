package com.inf8405.wardriver;

import java.util.LinkedList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Handler;

public class WifiScanner extends BroadcastReceiver
{	
	private WifiManager mWifiMgr;
	
	private LinkedList<WifiListener> listeners = new LinkedList<WifiListener>();
	
	private boolean mRunning = false;
	private volatile int mIntervalMS = 0;
	
	
	public WifiScanner(Context context)
	{
		listeners.clear();
		mRunning = false;
		
		// Récupère le service de wifi
		mWifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		
		// Mettre le wifi à ON s'il est éteint
		if (!mWifiMgr.isWifiEnabled())
        {
			mWifiMgr.setWifiEnabled(true);
        }
	}
	
	// Enregistre un observateur
	public void addListener(WifiListener l)
	{
		listeners.add(l);
	}
	
	// Désenregistre un observateur
	public void removeListener(WifiListener l)
	{
		listeners.remove(l);
	}
	
	// Fonction qui démarre le scanner de wifi (ou update l'interval)
	// Scan uniquement 1 fois si l'interval recu est de 0
	public void start(Context context, int intervalMS)
	{
		if (mRunning)
		{
			// Déjà démarré, on update l'interval et c'est tout
			mIntervalMS = intervalMS;
		}
		else
		{
			// Enregistre le receiver et on envoi une demande de scan
			context.registerReceiver(this, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
			mIntervalMS = intervalMS;
			if (mIntervalMS == 0)
			{
				mWifiMgr.startScan();
			}
			else
			{
				mRunning = true;
				scanAndPost();
			}
		}
	}
	
	// Fonction qui arrête le scanner de wifi
	public void stop(Context context)
	{
		// Désenregistre le receiver
		mRunning = false;
		context.unregisterReceiver(this);
	}
	
	// Retourne si le scanner est actif
	public boolean isRunning()
	{
		return mRunning;
	}
	
	// Démarre un scan et se rappèle après chaque interval tant que 'mRunning' est vrai
	private void scanAndPost()
	{
		if (mRunning)
		{
			// Démarre un scan immédiatement
			mWifiMgr.startScan();
			
			// On relance un autre scan après l'interval
			final Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
				@Override
				public void run()
				{
					scanAndPost();
				}
			}, mIntervalMS);
		}
	}
	
	
	// Fonction appelée lorsqu'un nouveau wifi est découvert
	// ou que la distance est plus courte
	private void newWifiFound(WifiInfo info)
	{
		// On notifie les observateurs
		for (WifiListener l : listeners)
		{
			l.onNewWifiFound(info);
		}
	}


	// Fonction appelée avec les résultats lorsqu'on scan se termine
	@Override
	public void onReceive(Context context, Intent intent) {
		List<ScanResult> wifiList = mWifiMgr.getScanResults();
		for (ScanResult r : wifiList)
		{
			WifiInfo info = new WifiInfo(r);
			newWifiFound(info);
		}
		
		if (mIntervalMS == 0)
		{
			// On ne répète pas, on stoppe
			mRunning = false;
			context.unregisterReceiver(this);
		}
	}
}
