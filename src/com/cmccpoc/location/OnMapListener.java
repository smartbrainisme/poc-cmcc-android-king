package com.cmccpoc.location;

public interface OnMapListener
{
	public void OnMapLocation(boolean isOk, int id, int type, boolean isFinal, double latitude, double longitude, double altitude, float direction, float speed, String time, String address);
}
