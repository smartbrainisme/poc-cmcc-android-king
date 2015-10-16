package com.airtalkee.listener;

public interface OnMmiAccountListener
{
    public void onMmiHeartbeatLogin(int result);

	public void onMmiHeartbeatLogout();

	public void onMmiHeartbeatException(int result);
}
