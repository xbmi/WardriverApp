package com.inf8405.wardriver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.net.wifi.ScanResult;

public class ClientTCP {
	private String serverIpAddr = "192.168.1.115";
	private int serverPort = 9000;
	private Socket socket;
	private HashMap<String, ScanResult> wifiMap;
	
	
	public ClientTCP(HashMap<String, ScanResult> wifiMap) {
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
				// TODO: Hashmap des wifis dans WifiScanner qui contient deja ces infos la (exemple: pas de nouveau calcul de frequence a faire
				JSONObject wifiListJSON = new JSONObject();
				int i = 0;
				for (String key : wifiMap.keySet())
				{
					ScanResult r = wifiMap.get(key);
					JSONObject wifiNetwork = new JSONObject();
					wifiNetwork.put("SSID", r.SSID);
					wifiNetwork.put("BSSID", r.BSSID);
					wifiNetwork.put("secured", (r.capabilities.contains("WPA") || r.capabilities.contains("WEP")));
					wifiNetwork.put("capabilities", r.capabilities);
					wifiNetwork.put("frequency", Float.toString((float)(r.frequency / 1000.0)));
					wifiNetwork.put("level", r.level);
					wifiNetwork.put("distance", "999");
					wifiNetwork.put("latitude", "-10");
					wifiNetwork.put("longitude", "-10");
					wifiNetwork.put("altitude", "-10");
					wifiNetwork.put("userId", "1");
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
