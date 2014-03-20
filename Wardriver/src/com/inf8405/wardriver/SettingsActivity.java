package com.inf8405.wardriver;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.MenuItem;

public class SettingsActivity extends PreferenceActivity
{
	
    @SuppressWarnings("deprecation")
	@Override
    public void onCreate(Bundle savedInstanceState) {        
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        addPreferencesFromResource(R.xml.preferences);        
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem)
    {       
        onBackPressed();
        return true;
    }
	
	public static int getCompassOffset(Context c)
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
		return Integer.parseInt( prefs.getString( c.getResources().getString(R.string.pref_key_compass_offset), "0" ) );
	}
	
	public static int getWifiScanInterval(Context c)
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
		return Integer.parseInt( prefs.getString( c.getResources().getString(R.string.pref_key_wifi_scanrate), "3000" ) );
	}
}
