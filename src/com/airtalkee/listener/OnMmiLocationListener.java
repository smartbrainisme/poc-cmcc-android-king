package com.airtalkee.listener;

public interface OnMmiLocationListener
{
	public void onLocationChanged(boolean isOk, int id, int type, double latitude, double longitude, double altitude, float speed, String time);
	
	public void onLocationChanged(boolean isOk, int id, int type, double latitude, double longitude, double altitude, float speed, String time, String address);
}
