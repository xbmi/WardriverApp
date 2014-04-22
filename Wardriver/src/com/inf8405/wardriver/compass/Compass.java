package com.inf8405.wardriver.compass;

import java.util.LinkedList;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class Compass implements SensorEventListener
{
	private boolean mRunning = false;
	private float mAzimuth = 0f;
	private int mOffset = 0;
	private SensorManager mSensorMgr = null;
	
	private LinkedList<CompassListener> listeners = new LinkedList<CompassListener>();
	
	// Classe qui offre plusieurs services avec le censeur magn�tique (d'orientation)
	public Compass(Context context)
	{
		// R�cup�re le service de censeur
		mSensorMgr = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		
		// Vide la liste d'observateurs
		listeners.clear();
	}
	
	// Enregistre un observateur
	public void addListener(CompassListener l)
	{
		listeners.add(l);
	}
	
	// D�senregistre un observateur
	public void removeListener(CompassListener l)
	{
		listeners.remove(l);
	}
	
	// On d�marre l'�coute du censeur
	@SuppressWarnings("deprecation")
	public void start()
	{
		// On s'enregistre aupr�s du censeur d'orientation
		mSensorMgr.registerListener(this, mSensorMgr.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_UI);
		mRunning = true;
		notifyOrientation();
	}
	
	// On arr�te l'�coute du censeur
	public void stop()
	{
		// On se d�senregistre
		mSensorMgr.unregisterListener(this);
		mRunning = false;
	}
	
	// Retourne si on �coute actuellement le censeur
	public boolean isRunning()
	{
		return mRunning;
	}
	
	// Retourne l'azimuth vers quel pointe le p�riph�rique
	public float getAzimuth()
	{
		return mAzimuth + mOffset;
	}
	
	// Modifie un offset pour l'orientation magn�tique
	public void setAzimuthOffset(int offset)
	{
		mOffset = offset;
	}

	// Appel� lors d'un changement de pr�cision
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy)
	{
		// On ne s'en occupe pas
	}

	// Appel� lors d'un changement de direction
	@Override
	public void onSensorChanged(SensorEvent event)
	{
		// On v�rifie si l'azimuth a chang� suffisamment (values[0])
		if (Math.abs(mAzimuth - event.values[0]) > 0.3)
		{
			// Si oui on la r�cup�re et rotate la carte
			mAzimuth = event.values[0]; // Note: 0=Nord, 90=Est, 180=Sud, 270=Ouest
			notifyOrientation();
		}
	}
	
	// Averti les observateurs de l'orientation a chang�
	public void notifyOrientation()
	{
		for (CompassListener l : listeners)
		{
			l.onOrientationChanged(mAzimuth + mOffset);
		}
	}
}
