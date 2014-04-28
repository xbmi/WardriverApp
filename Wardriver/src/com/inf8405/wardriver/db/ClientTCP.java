package com.inf8405.wardriver.db;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.inf8405.wardriver.wifi.WifiInfo;

import android.content.Context;
import android.util.Log;

public class ClientTCP {
	
	// Attributs de la classe du client
	private Socket socket;
	
	// Serveur et port du serveur distant 
	private String serverIpAddr = "soju.no-ip.biz";
	private int serverPort = 9000;
	
	// Liste des WiFis
	private HashMap<String, WifiInfo> wifiMap;
	private Context context;
	
	// Constructeur par parametre
	public ClientTCP(HashMap<String, WifiInfo> wifiMap, Context context) {
		this.wifiMap = wifiMap;
		this.context = context;
	}
	
	// Debut du fil d'execution reseau pour la base de donees
	public void start(DBSyncListener l) {
		new Thread(new ClientThread(l)).start();
	}
	
	// Classe qui represente le fil d'execution qui echange avec le serveur
	class ClientThread implements Runnable {
		DBSyncListener listener;
		
		public ClientThread(DBSyncListener l) {
			super();
			listener = l;
		}
		
		@Override
		public void run() {
			try {
				// Creation du socket de communication avec le serveur
				socket = new Socket(serverIpAddr, serverPort);
				
				// Modules d'envoi et de reception
				PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				
				JSONObject wifiListJSON = new JSONObject();
				int i = 0;
				
				// Iteration sur les reseaux WiFis et construction d'un objet JSON avec ces informations
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
					
					// Ajout des informations du reseau a la liste des reseaux 
					wifiListJSON.put("wifi_" + Integer.toString(i), wifiNetwork);
					i += 1;
				}
				
				String jsonStr = wifiListJSON.toString();
				Integer messageSize = jsonStr.length();
				
				// Envoi du message avec l'entete qui est la longueur du message
				out.print(String.format("%016d", Integer.parseInt(messageSize.toString())) + jsonStr + "\n");
				out.flush();
				
				String recept = "";
				boolean validJson;
				
				// Reception de la reponse du serveur.
				recept = in.readLine();
				
				// Traitement d'erreur si la requete n'a pu etre executee
				if(recept.equals("error")) {
					validJson = false;
				} else {
					validJson = true;
				}
				 
				// Fermeture des connexions
				in.close();
				out.close();
				socket.close();
				
				// La requete s'est bien executee
				if(validJson) {
					HashMap<String, WifiInfo> hm = null;
					
					// Instantiation de la base de donnees
					LocalDatabase db = LocalDatabase.getInstance(context);
					
					// Obtention des points d'acces pour les logs
					hm = db.getAllAccessPoints();
					Log.i("Sync", "Taille DB avant la reception: " + hm.size());
					
					// Vider la table
					db.emptyTable();
					
					// Obtention des points d'acces pour les logs
					hm = db.getAllAccessPoints();
					Log.i("Sync", "Taille DB après nettoyage: " + hm.size());
					
					// Mettre la reponse dans un objet JSON pour les iterations
					JSONArray arr = new JSONArray(recept);
					Log.i("Sync", "Nombre d'entrees recues: " + arr.length() + " Ajout a la DB..");
					
					WifiInfo wf = new WifiInfo();
					
					// Pour chaque objet recu, insertion dans la base de donnees nouvellement vide
					for (int j = 0; j < arr.length(); j++) {
						JSONArray entry = arr.getJSONArray(j);
						wf.reset();
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
			    		db.insertAccessPoint(wf);
					}
					
					// Obtention des points d'acces pour les logs
					hm = db.getAllAccessPoints();
					Log.i("Sync", "Fin d'ajout a la DB. Taille DB après sync: " + hm.size());
					
					// S'assure que le listener est synchronise
					listener.onDBSynced();
				}
			
			// Gestion standard d'erreurs
			} catch (UnknownHostException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
}
