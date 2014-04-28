package com.inf8405.wardriver.db;

import java.util.HashMap;

import com.inf8405.wardriver.wifi.WifiInfo;

import android.R.bool;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class LocalDatabase extends SQLiteOpenHelper {
	
	// Attributs pour reprensenter la base de donnees
	private static final int VERSION_BDD = 1;
	private static final String NOM_BDD = "wardriver.db";
	private static final String TABLE_ACCESS_POINTS = "access_points";
	private static final String COL_ID = "id";
	private static final int NUM_COL_ID = 0;
	private static final String COL_SSID = "SSID";
	private static final int NUM_COL_SSID = 1;
	private static final String COL_BSSID = "BSSID";
	private static final int NUM_COL_BSSID = 2;
	private static final String COL_SECURED = "secured";
	private static final int NUM_COL_SECURED = 3;
	private static final String COL_CAPABILITIES = "capabilities";
	private static final int NUM_COL_CAPABILITIES = 4;
	private static final String COL_FREQUENCY = "frequency";
	private static final int NUM_COL_FREQUENCY = 5;
	private static final String COL_LEVEL = "level";
	private static final int NUM_COL_LEVEL = 6;
	private static final String COL_DISTANCE = "distance";
	private static final int NUM_COL_DISTANCE = 7;
	private static final String COL_LONGITUDE = "longitude";
	private static final int NUM_COL_LONGITUDE = 8;
	private static final String COL_LATITUDE = "latitude";
	private static final int NUM_COL_LATITUDE = 9;
	private static final String COL_ALTITUDE = "altitude";
	private static final int NUM_COL_ALTITUDE = 10;
	
	// Bases de donnees en lecture et en ecriture
	private SQLiteDatabase db_write;
	private SQLiteDatabase db_read;
	
	private static LocalDatabase instance = null;
	
	// Chaine de creation de la base de donnees
	private static final String CREATE_BDD = "CREATE TABLE access_points " +
			"(id INTEGER PRIMARY KEY, SSID TEXT, BSSID TEXT, secured NUMERIC, " +
			"capabilities TEXT, frequency NUMERIC, level NUMERIC, distance NUMERIC, " +
			"longitude NUMERIC, latitude NUMERIC, altitude NUMERIC);"; // , androidId TEXT
 
	// Obtention de l'instance de la base de donnees
	public static LocalDatabase getInstance(Context context) {
		if(instance == null) {
			instance = new LocalDatabase(context);
		}
		return instance;
	}
	
	// Creation de la base de donnees
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_BDD);
	}
 
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE " + TABLE_ACCESS_POINTS + ";");
		onCreate(db);
	}
	
	// Constructeur
	private LocalDatabase(Context context) {
		super(context, NOM_BDD, null, VERSION_BDD);
		db_read = getReadableDatabase();
		db_write = getWritableDatabase();
	}
	
	// Verification des acces points en fonctin du BSSID
	public boolean checkAccessPoint(WifiInfo wf) {
	    String query = "SELECT * FROM " + TABLE_ACCESS_POINTS + " WHERE " + COL_BSSID + "=" + wf.BSSID;
	    Cursor c = db_read.rawQuery(query, null);
	    return (c.getCount() <= 0) ? false : true;
	}
	
	// Suppression des points d'acces
	public boolean removeAccessPoint(WifiInfo wf) {
	    return db_write.delete(TABLE_ACCESS_POINTS, COL_BSSID + " = ?", new String[]{ wf.BSSID }) > 0;
	}
	
	// Insertion d'un point d'acces
	public long insertAccessPoint(WifiInfo wf) {
		ContentValues values = new ContentValues();
		
		values.put(COL_SSID, wf.SSID);
		values.put(COL_BSSID, wf.BSSID);
		values.put(COL_SECURED, wf.secured);
		values.put(COL_CAPABILITIES, wf.capabilities);
		values.put(COL_FREQUENCY, wf.frequency);
		values.put(COL_LEVEL, wf.level);
		values.put(COL_DISTANCE, wf.distance);
		values.put(COL_LONGITUDE, wf.longitude);
		values.put(COL_LATITUDE, wf.latitude);
		values.put(COL_ALTITUDE, wf.altitude);
		return db_write.insert(TABLE_ACCESS_POINTS, null, values);
	}
	
	// Vider la table
	public void emptyTable() {
		db_write.execSQL("DELETE FROM '" + TABLE_ACCESS_POINTS + "'");
	}
	
	// Obtention de tous les points d'acces
	public HashMap<String, WifiInfo> getAllAccessPoints() {
		String query = "SELECT * FROM " + TABLE_ACCESS_POINTS;
	    Cursor c = db_read.rawQuery(query, null);
	    HashMap<String, WifiInfo> wifiList = new HashMap<String, WifiInfo>();
	    if(c.moveToFirst()) {
	    	do {
	    		WifiInfo wf = new WifiInfo();
	    		wf.SSID = c.getString(NUM_COL_SSID);
	    		wf.BSSID = c.getString(NUM_COL_BSSID);
	    		wf.secured = c.getInt(NUM_COL_SECURED) == 1 ? true : false;
	    		wf.capabilities = c.getString(NUM_COL_CAPABILITIES);
	    		wf.frequency = c.getFloat(NUM_COL_FREQUENCY);
	    		wf.level = c.getInt(NUM_COL_LEVEL);
	    		wf.distance = c.getInt(NUM_COL_DISTANCE);
	    		wf.longitude = c.getDouble(NUM_COL_LONGITUDE);
	    		wf.latitude = c.getDouble(NUM_COL_LATITUDE);
	    		wf.altitude = c.getDouble(NUM_COL_ALTITUDE);
	    		
	    		wifiList.put(wf.BSSID, wf);
	    	} while(c.moveToNext());
	    }
	    return wifiList;
	}
}
