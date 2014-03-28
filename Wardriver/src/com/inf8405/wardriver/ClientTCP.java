package com.inf8405.wardriver;

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

public class ClientTCP {
	private String serverIpAddr = "192.168.1.115";
	private int serverPort = 9000;
	private Socket socket;
	private HashMap<String, WifiInfo> wifiMap;
	
	
	public ClientTCP(HashMap<String, WifiInfo> wifiMap) {
		this.wifiMap = wifiMap;
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
					wifiNetwork.put("userId", "1"); // c'est quoi? ca pas rapport ak le wifi j'imagine?
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
