package com.airtalkee.adapter;

import java.util.List;
import com.airtalkee.sdk.entity.AirChannel;
import com.airtalkee.sdk.entity.AirSession;

public class GroupBean
{
	public static final int TYPE_CHANNEL = 0;
	public static final int TYPE_SESSION = 1;
	public String displayName;
	public int type = 0;
	public List<AirSession> sessionList;
	public List<AirChannel> channelList;
}
