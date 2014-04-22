package com.inf8405.wardriver.db;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import com.inf8405.wardriver.wifi.WifiInfo;

import android.content.Context;
import android.provider.Settings.Secure;

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
	
	public void start() {
		new Thread(new ClientThread()).start();
	}
	
	class ClientThread implements Runnable {
		@Override
		public void run() {
			try {
				socket = new Socket(serverIpAddr, serverPort);
				
				OutputStream out = socket.getOutputStream();       
				PrintWriter output = new PrintWriter(out);
				
				InputStreamReader input = new InputStreamReader(socket.getInputStream());
				BufferedReader in = new BufferedReader(input);
				
				// Hardcoder
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
					//wifiNetwork.put("androidId", Secure.getString(context.getContentResolver(), Secure.ANDROID_ID));
					wifiListJSON.put("wifi_" + Integer.toString(i), wifiNetwork);
					i += 1;
				}
				String jsonStr = wifiListJSON.toString();
				output.print(jsonStr);
				output.flush();
				
				String recept = "";
				if(input.ready()) {
					recept = in.readLine();
				}
				
				out.flush();
				out.close();
				socket.close();
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
