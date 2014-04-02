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
	
	// Fonction qui d�marre le scanner de wifi (ou update l'interval)
	// Scan uniquement 1 fois si l'interval recu est de 0
	public void start(Context context, int intervalMS)
	{
		if (mRunning)
		{
			// D�j� d�marr�, on update l'interval et c'est tout
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
	
	// FIXME: temporaire, pour tester
//	private void listAllWifis(Context context)
//	{
//		// Gros dialog temporaire pour tester
//		String message = "";
//		for (String key : mWifiList.keySet())
//		{
//			WifiInfo r = mWifiList.get(key);
//			
//	    	message +=   ("SSID: " + r.SSID +
//	    			   	  "\nBSSID: " + r.BSSID +
//	    			   	  "\nSecured: " + (r.secured ? "Yes" : "No") +
//	    			   	  "\n" + r.capabilities +
//	    			   	  "\nFreq: " + (float)(r.frequency / 1000.0) + " GHz" +
//	    			   	  "\nLevel: " + r.level + " dBm" +
//	    			   	  "\nEstimated distance: " + r.distance + "m\n\n");
//		}
//		
//		AlertDialog.Builder builder = new AlertDialog.Builder(context);
//	    builder.setTitle("Wi-Fi list")
//	           .setCancelable(false)
//	    	   .setMessage(message)
//	    	   .setPositiveButton("OK", new OnClickListener() {
//					@Override
//					public void onClick(DialogInterface dialog, int which) {
//						dialog.dismiss();
//					}
//				});
//	    final Dialog d = builder.create();
//	    d.show();
//	    
//	    TextView textView = (TextView) d.findViewById(android.R.id.message);
//	    textView.setTextSize(12);
//	}
	
	
	// Fonction appel�e lorsqu'un nouveau wifi est d�couvert
	// ou que la distance est plus courte
	private void newWifiFound(WifiInfo info)
	{
		// On notifie les observateurs
		for (WifiListener l : listeners)
		{
			l.onNewWifiFound(info);
		}
	}


	// Fonction appel�e avec les r�sultats lorsqu'on scan se termine
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
			// On ne r�p�te pas, on stoppe
			mRunning = false;
			context.unregisterReceiver(this);
		}
	}
}
