package com.inf8405.wardriver.db;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.inf8405.wardriver.wifi.WifiInfo;

import android.content.Context;
import android.provider.Settings.Secure;
import android.util.Log;

public class ClientTCP {
	private String serverIpAddr = "soju.no-ip.biz";
	private int serverPort = 9000;
	private Socket socket;
	private HashMap<String, WifiInfo> wifiMap;
	private Context context;
	
	public ClientTCP(HashMap<String, WifiInfo> wifiMap, Context context) {
		this.wifiMap = wifiMap;
		this.context = context;
	}
	
	public void start(DBSyncListener l) {
		new Thread(new ClientThread(l)).start();
	}
	
	class ClientThread implements Runnable {
		DBSyncListener listener;
		
		public ClientThread(DBSyncListener l)
		{
			super();
			listener = l;
		}
		
		@Override
		public void run() {
			try {
				socket = new Socket(serverIpAddr, serverPort);
				
				PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				
				JSONObject wifiListJSON = new JSONObject();
				int i = 0;
				for (String key : wifiMap.keySet())
				{
					WifiInfo r = wifiMap.get(key);
					JSONObject wifiNetwork = new JSONObject();
					wifiNetwork.put("SSID", r.SSID);
					wifiNetwork.put("BSSID", r.BSSID);
					wifiNetwork.put("secured", r.secured);
					wifiNetwork.put("capabilities", r.capabilities);
					wifiNetwork.put("frequency", r.frequency);
					wifiNetwork.put("level", r.level);
					wifiNetwork.put("distance", r.distance);
					wifiNetwork.put("latitude", r.latitude);
					wifiNetwork.put("longitude", r.longitude);
					wifiNetwork.put("altitude", r.altitude);
					wifiListJSON.put("wifi_" + Integer.toString(i), wifiNetwork);
					i += 1;
				}
				String jsonStr = wifiListJSON.toString();
				//System.out.println("Envoi de : " + jsonStr);
				Integer messageSize = jsonStr.length();
				
				out.print(String.format("%016d", Integer.parseInt(messageSize.toString())) + jsonStr);
				out.flush();
				
				String recept = "";
				boolean validJson;
				
				recept = in.readLine();
				System.out.println("Reception: " + recept);
				if(recept == "error") {
					validJson = false;
				} else {
					validJson = true;
				}
				 
				/*if(in.ready()) {
					recept = in.readLine();
					System.out.println("Reception: " + recept);
				} else {
					System.out.println("Probleme de communication!");
					validJson = false;
				}*/
				in.close();
				out.close();
				socket.close();
				
				if(validJson) {
					Integer taille = -1;
					HashMap<String, WifiInfo> hm = null;
					
					hm = LocalDatabase.getInstance(context).getAllAccessPoints();
					taille = hm.size();
					
					LocalDatabase.getInstance(context).emptyTable();
					
					hm = LocalDatabase.getInstance(context).getAllAccessPoints();
					taille = hm.size();
					
					JSONArray arr = new JSONArray(recept);
					System.out.println("Nombre d'entrees recues: " + arr.length());
					for (int j = 0; j < arr.length(); j++) {
						JSONArray entry = arr.getJSONArray(j);
						WifiInfo wf = new WifiInfo();
			    		wf.SSID = (String) entry.get(1);
			    		wf.BSSID = (String) entry.get(2);
			    		wf.secured = (boolean) ((Integer) entry.get(3) == 1 ? true : false);
			    		wf.capabilities = (String) entry.get(4);
			    		wf.frequency = Float.parseFloat(entry.get(5).toString());
			    		wf.level = (Integer) entry.get(6);
			    		wf.distance = (Integer) entry.get(7);
			    		wf.longitude = Double.parseDouble(entry.get(8).toString());
			    		wf.latitude = Double.parseDouble(entry.get(9).toString());
			    		wf.altitude = Double.parseDouble(entry.get(10).toString());
			    		LocalDatabase.getInstance(context).insertAccessPoint(wf);
					}
					
					hm = LocalDatabase.getInstance(context).getAllAccessPoints();
					taille = hm.size();
					
					listener.onDBSynced();
				}
			} catch (UnknownHostException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}