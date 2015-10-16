package com.airtalkee.location;

public interface OnMapListener
{
	public void OnMapLocation(boolean isOk, int id, int type, boolean isFinal, double latitude, double longitude, double altitude, float speed, String time);
}
