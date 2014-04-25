package com.inf8405.wardriver.wifi;

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
		
		// R�cup�re le service de wifi
		mWifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		
		// Mettre le wifi � ON s'il est �teint
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
	
	// D�senregistre un observateur
	public void removeListener(WifiListener l)
	{
		listeners.remove(l);
	}
	
	// Fonction qui d�marre le scanner de wifi (ou met � jour la valeur de l'intervale)
	// Balaye uniquement 1 fois si l'interval recu est de 0
	public void start(Context context, int intervalMS)
	{
		if (mRunning)
		{
			// D�j� d�marr�, on update l'intervale et c'est tout
			mIntervalMS = intervalMS;
		}
		else
		{
			// Enregistre le receiver et on envoi une demande de scan
			context.registerReceiver(this, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
			mIntervalMS = intervalMS;
			if (mIntervalMS == 0)
			{
				// Une seule demande
				mWifiMgr.startScan();
			}
			else
			{
				// D�marre un scan p�riodique
				mRunning = true;
				scanAndPost();
			}
		}
	}
	
	// Fonction qui arr�te le scanner de wifi
	public void stop(Context context)
	{
		// D�senregistre le receiver
		mRunning = false;
		context.unregisterReceiver(this);
	}
	
	// Retourne si le scanner est actif
	public boolean isRunning()
	{
		return mRunning;
	}
	
	// D�marre un scan et se rapp�le apr�s chaque interval tant que 'mRunning' est vrai
	private void scanAndPost()
	{
		if (mRunning)
		{
			// D�marre un scan imm�diatement
			mWifiMgr.startScan();
			
			// On relance un autre scan apr�s l'interval
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
	
	
	// Envoi � tous les observateurs les informations sur le nouveau wifi trouv�
	private void notifyListeners(WifiInfo info)
	{
		// On notifie les observateurs
		for (WifiListener l : listeners)
		{
			l.onWifiFound(info);
		}
	}


	// Fonction appel�e avec les r�sultats lorsqu'on scan se termine
	@Override
	public void onReceive(Context context, Intent intent)
	{
		// On r�cup�re les donn�es
		List<ScanResult> wifiList = mWifiMgr.getScanResults();
		
		// On cr�e des objets WifiInfo et notifie
		for (ScanResult r : wifiList)
		{
			WifiInfo info = new WifiInfo(r);
			notifyListeners(info);
		}
		
		if (mIntervalMS == 0)
		{
			// Simple balayage, on ne r�p�te pas, on stoppe
			mRunning = false;
			context.unregisterReceiver(this);
		}
	}
}
