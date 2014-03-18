package com.inf8405.wardriver;

import java.util.HashMap;
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
import android.widget.TextView;

public class WifiScanner
{	
	private WifiManager mWifiMgr;
	private WifiBroadcastReceiver mWifiReceiver;
	
	private HashMap<String, ScanResult> mWifiList = new HashMap<String, ScanResult>();
	
	public WifiScanner(Context context)
	{
		// Récupère le service de wifi
		mWifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		
		// Mettre le wifi à ON s'il est éteint
		if (!mWifiMgr.isWifiEnabled())
        {
			mWifiMgr.setWifiEnabled(true);
        }
		
		// On enregistre un recepteur
		mWifiReceiver = new WifiBroadcastReceiver();
		context.registerReceiver(mWifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
	}
	
	public void scanNow(Context context)
	{
		// On envoi une demande de scan
		mWifiMgr.startScan();
	}
	
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
	
	private void newWifiDetected(Context context, ScanResult r)
	{
		// On affiche un dialog pour le nouveau wifi
		boolean secured = (r.capabilities.contains("WPA") || r.capabilities.contains("WEP"));
		
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
	    builder.setTitle("New Wi-Fi detected!")
	           .setCancelable(false)
	    	   .setMessage("SSID: " + r.SSID +
	    			   	  "\nBSSID: " + r.BSSID +
	    			   	  "\nSecured: " + (secured ? "Yes" : "No") +
	    			   	  "\n" + r.capabilities +
	    			   	  "\nFreq: " + (float)(r.frequency / 1000.0) + " GHz" +
	    			   	  "\nLevel: " + r.level + " dBm" +
	    			   	  "\nEstimated distance: " + estimateRouterDistance(r.level, r.frequency) + "m")
	    	   .setPositiveButton("OK", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
	    final Dialog d = builder.create();
	    d.show();
	}

	private class WifiBroadcastReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context context, Intent intent) {
			List<ScanResult> wifiList = mWifiMgr.getScanResults();
			for (ScanResult r : wifiList)
			{
				// SSID + " " + BSSID va être la clé unique représentant un wifi
				String key = r.SSID + " " + r.BSSID;
				
				if (mWifiList.get(key) == null)
				{
					// Nouveau wifi inconnu!
					//newWifiDetected(context, r);
				}
				
				// On ajoute / met à jour la liste
				mWifiList.put(key, r);
			}
			
			listAllWifis(context);
		}
	}
	
	
	// Fonction qui estime la distance d'un routeur de maison en fonction de la
	// force du signal (dBm) et sa fréquence (MHz). Retourne la distance approx en mètres.
	public int estimateRouterDistance(double signalStrengthInDBm, double freqInMHz)
	{
		// Formule originale pour "Free-space path loss in decibels"
	    //double exp = (27.55 - (20 * Math.log10(freqInMHz)) - signalStrengthInDBm) / 20.0;
	    //return Math.pow(10.0, exp);
	    
		// Formule modifiée par nous pour prendre compte que les routeur sont généralement
		// dans des maisons avec plusieurs murs qui réduit rapidement la distance
	    double exp = (80.0 - (20 * Math.log10(freqInMHz)) - signalStrengthInDBm) / 20.0;
	    return (int)Math.pow(2.0, exp);
	}
}
