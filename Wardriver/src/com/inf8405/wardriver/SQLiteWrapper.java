package com.inf8405.wardriver;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

public class SQLiteWrapper extends SQLiteOpenHelper {
	private static final String TABLE_ACCESS_POINTS = "access_points";
 
	private static final String CREATE_BDD = "CREATE TABLE access_points " +
			"(id INTEGER PRIMARY KEY, SSID TEXT, BSSID TEXT, secured NUMERIC, " +
			"capabilities TEXT, frequency NUMERIC, level NUMERIC, distance NUMERIC, " +
			"longitude NUMERIC, latitude NUMERIC, altitude NUMERIC);"; // , androidId TEXT
 
	public SQLiteWrapper(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
	}
 
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_BDD);
	}
 
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE " + TABLE_ACCESS_POINTS + ";");
		onCreate(db);
	}
}
