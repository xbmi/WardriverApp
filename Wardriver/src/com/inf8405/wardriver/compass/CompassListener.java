package com.inf8405.wardriver.compass;

public interface CompassListener
{
	// Appelé lorsqu'il y a un changement d'azimuth avec la nouvelle direction
	public void onDeviceAzimuthChanged(float azimuth);
}
