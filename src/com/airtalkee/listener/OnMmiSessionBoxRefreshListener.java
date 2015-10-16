package com.airtalkee.listener;

import com.airtalkee.sdk.entity.AirSession;

public interface OnMmiSessionBoxRefreshListener
{
	public void onMmiSessionRefresh(AirSession session);

	public void onMmiSessionEstablished(AirSession session);

	public void onMmiSessionReleased(AirSession session);
}
