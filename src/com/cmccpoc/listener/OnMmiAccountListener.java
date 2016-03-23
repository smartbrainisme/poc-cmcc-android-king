package com.cmccpoc.listener;

public interface OnMmiAccountListener
{
    public void onMmiHeartbeatLogin(int result);

	public void onMmiHeartbeatLogout();

	public void onMmiHeartbeatException(int result);
}
