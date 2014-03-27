package com.inf8405.wardriver;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

public class WifiScanner extends BroadcastReceiver
{	
	private WifiManager mWifiMgr;
	
	private HashMap<String, ScanResult> mWifiList = new HashMap<String, ScanResult>();
	
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
	private void listAllWifis(Context context)
	{
		// Gros dialog temporaire pour tester
		String message = "";
		for (String key : mWifiList.keySet())
		{
			ScanResult r = mWifiList.get(key);
			boolean secured = (r.capabilities.contains("WPA") || r.capabilities.contains("WEP"));
			
	    	message +=   ("SSID: " + r.SSID +
	    			   	  "\nBSSID: " + r.BSSID +
	    			   	  "\nSecured: " + (secured ? "Yes" : "No") +
	    			   	  "\n" + r.capabilities +
	    			   	  "\nFreq: " + (float)(r.frequency / 1000.0) + " GHz" +
	    			   	  "\nLevel: " + r.level + " dBm" +
	    			   	  "\nEstimated distance: " + estimateRouterDistance(r.level, r.frequency) + "m\n\n");
		}
		
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
	    builder.setTitle("Wi-Fi list")
	           .setCancelable(false)
	    	   .setMessage(message)
	    	   .setPositiveButton("OK", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
	    final Dialog d = builder.create();
	    d.show();
	    
	    TextView textView = (TextView) d.findViewById(android.R.id.message);
	    textView.setTextSize(12);
	}
	
	
	// Fonction appel�e lorsqu'un nouveau wifi est d�couvert
	// ou que la distance est plus courte
	private void newWifiFound(ScanResult r)
	{
		// On notifie les observateurs
		for (WifiListener l : listeners)
		{
			l.onNewWifiFound(r);
		}
	}


	// Fonction appel�e avec les r�sultats lorsqu'on scan se termine
	@Override
	public void onReceive(Context context, Intent intent) {
		List<ScanResult> wifiList = mWifiMgr.getScanResults();
		for (ScanResult r : wifiList)
		{
			// SSID + " " + BSSID va �tre la cl� unique repr�sentant un wifi
			String key = r.SSID + " " + r.BSSID;
			
			if (mWifiList.get(key) == null)
			{
				// Nouveau wifi inconnu!
				newWifiFound(r);
			}
			
			// On ajoute / met � jour la liste
			// TODO: v�rifier si la distance est plus petite qu'avant -> updater
			mWifiList.put(key, r);
		}
		
		// FIXME: temporaire, enlever
		Log.i("WifiScanner", "Scan result received!");
		
		if (mIntervalMS == 0)
		{
			// On ne r�p�te pas, on stoppe
			mRunning = false;
			context.unregisterReceiver(this);
			listAllWifis(context); // FIXME: Temporaire pour tester
		}
	}
	
	
	// Fonction qui estime la distance d'un routeur de maison en fonction de la
	// force du signal (dBm) et sa fr�quence (MHz). Retourne la distance approx en m�tres.
	public int estimateRouterDistance(double signalStrengthInDBm, double freqInMHz)
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
