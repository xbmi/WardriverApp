package com.inf8405.wardriver;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class LocalDatabase {
	private static final int VERSION_BDD = 1;
	private static final String NOM_BDD = "wardriver.db";
	private static final String TABLE_ACCESS_POINTS = "access_points";
	private SQLiteDatabase bdd;
	private SQLiteWrapper sqliteWrapper;
	
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
	//private static final String COL_ANDROIDID = "androidId";
	//private static final int NUM_COL_ANDROIDID = 11;

	
	public LocalDatabase(Context context){
		sqliteWrapper = new SQLiteWrapper(context, NOM_BDD, null, VERSION_BDD);
	}
	
	public void open(){
		bdd = sqliteWrapper.getWritableDatabase();
	}
	
	public void close(){
		bdd.close();
	}
	
	public SQLiteDatabase getBDD(){
		return bdd;
	}
	
	public boolean checkAccessPoint(WifiInfo wf) {
		SQLiteDatabase sqldb = getBDD();
	    String query = "SELECT * FROM " + TABLE_ACCESS_POINTS + " WHERE " + COL_BSSID + "=" + wf.BSSID;
	    Cursor c = sqldb.rawQuery(query, null);
	    return (c.getCount() <= 0) ? false : true;
	}
	
	public long insertAccessPoint(WifiInfo wf){
		ContentValues values = new ContentValues();
		
		values.put(COL_SSID, wf.SSID);
		values.put(COL_BSSID, wf.BSSID);
		values.put(COL_SECURED, wf.secured);
		values.put(COL_CAPABILITIES, wf.capabilities);
		values.put(COL_FREQUENCY, wf.frequency);
		values.put(COL_LEVEL, wf.level);
		values.put(COL_DISTANCE, wf.distance);
		values.put(COL_LONGITUDE, wf.latitude);
		values.put(COL_LATITUDE, wf.longitude);
		values.put(COL_ALTITUDE, wf.altitude);
		
		return bdd.insert(TABLE_ACCESS_POINTS, null, values);
	}
}
