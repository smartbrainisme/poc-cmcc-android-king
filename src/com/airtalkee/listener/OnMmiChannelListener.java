package com.airtalkee.listener;

import java.util.LinkedHashMap;
import java.util.List;

import com.airtalkee.sdk.entity.AirChannel;
import com.airtalkee.sdk.entity.AirContact;

public interface OnMmiChannelListener
{
	public void onChannelListGet(boolean isOk, final List<AirChannel> channels);

	public void onChannelMemberListGet(String channelId, final List<AirContact> members);

	public void onChannelOnlineCount(final LinkedHashMap<String, Integer> online);

	public void onChannelPersonalCreateNotify(AirChannel ch);

	public void onChannelPersonalDeleteNotify(AirChannel ch);

}
