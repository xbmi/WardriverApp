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
	
	// Classe qui offre plusieurs services avec le censeur magnétique (d'orientation)
	public Compass(Context context)
	{
		// Récupère le service de censeur
		mSensorMgr = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		
		// Vide la liste d'observateurs
		listeners.clear();
	}
	
	// Enregistre un observateur
	public void addListener(CompassListener l)
	{
		listeners.add(l);
	}
	
	// Désenregistre un observateur
	public void removeListener(CompassListener l)
	{
		listeners.remove(l);
	}
	
	// On démarre l'écoute du censeur
	@SuppressWarnings("deprecation")
	public void start()
	{
		// On s'enregistre auprès du censeur d'orientation
		mSensorMgr.registerListener(this, mSensorMgr.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_UI);
		mRunning = true;
		notifyOrientation();
	}
	
	// On arrête l'écoute du censeur
	public void stop()
	{
		// On se désenregistre
		mSensorMgr.unregisterListener(this);
		mRunning = false;
	}
	
	// Retourne si on écoute actuellement le censeur
	public boolean isRunning()
	{
		return mRunning;
	}
	
	// Retourne l'azimuth vers quel pointe le périphérique
	public float getAzimuth()
	{
		return mAzimuth + mOffset;
	}
	
	// Modifie un offset pour l'orientation magnétique
	public void setAzimuthOffset(int offset)
	{
		mOffset = offset;
	}

	// Appelé lors d'un changement de précision
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy)
	{
		// On ne s'en occupe pas
	}

	// Appelé lors d'un changement de direction
	@Override
	public void onSensorChanged(SensorEvent event)
	{
		// On vérifie si l'azimuth a changé suffisamment (values[0])
		if (Math.abs(mAzimuth - event.values[0]) > 0.3)
		{
			// Si oui on la récupère et rotate la carte
			mAzimuth = event.values[0]; // Note: 0=Nord, 90=Est, 180=Sud, 270=Ouest
			notifyOrientation();
		}
	}
	
	// Averti les observateurs de l'orientation a changé
	public void notifyOrientation()
	{
		for (CompassListener l : listeners)
		{
			l.onOrientationChanged(mAzimuth + mOffset);
		}
	}
}
