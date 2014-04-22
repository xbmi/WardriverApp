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
        
        // On contruit la page � l'aide du fichier XML de pr�f�rences (voir res/xml/preferences.xml)
        // Deprecated: on n'utilise pas les fragments pour avoir une meilleure r�tro-compatibilit�
        addPreferencesFromResource(R.xml.preferences);
    }
    
    // Lorsqu'on clique sur l'icone de l'application en haut � gauche, m�me comportement qui si on appuie sur "retour"
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem)
    {       
        onBackPressed();
        return true;
    }
	
    // Retourne la valeur sauvegard�e ou sinon celle par d�faut de l'offset de la boussole
	public static int getCompassOffset(Context c)
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
		return Integer.parseInt( prefs.getString( c.getResources().getString(R.string.pref_key_compass_offset), "0" ) );
	}
	
	// Retourne la valeur sauvegard�e ou sinon celle par d�faut de l'interval de scan
	public static int getScanInterval(Context c)
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
		return Integer.parseInt( prefs.getString( c.getResources().getString(R.string.pref_key_scanrate), "3000" ) );
	}
}
