package com.inf8405.wardriver;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class Compass implements SensorEventListener
{
	private boolean mRunning = false;
	private float mAzimuth = 0f;
	private SensorManager mSensorMgr = null;
	private WifiMap mMapToRotate = null;
	
	public Compass(Context context)
	{
		// Récupère le service de censeur
		mSensorMgr = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
	}
	
	public void start()
	{
		// On s'enregistre auprès du censeur d'orientation
		mSensorMgr.registerListener(this, mSensorMgr.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_UI);
		mRunning = true;
	}
	
	public void stop()
	{
		// On se désenregistre
		mSensorMgr.unregisterListener(this);
		mRunning = false;
	}
	
	public boolean isRunning()
	{
		return mRunning;
	}
	
	public float getAzimuth()
	{
		return mAzimuth;
	}
	
	public void registerMap(WifiMap map)
	{
		mMapToRotate = map;
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy)
	{
		// On ne s'en occupe pas
	}

	@Override
	public void onSensorChanged(SensorEvent event)
	{
		// On vérifie si l'azimuth a changé (values[0])
		if (Math.abs(mAzimuth - event.values[0]) > 5)
		{
			// Si oui on la récupère
			mAzimuth = event.values[0]; // Note: 0=Nord, 90=Est, 180=Sud, 270=Ouest
			
			// Si on a une map, on la rotate
			if (mMapToRotate != null)
			{
				mMapToRotate.rotateTo(mAzimuth);
			}
		}
	}
}
