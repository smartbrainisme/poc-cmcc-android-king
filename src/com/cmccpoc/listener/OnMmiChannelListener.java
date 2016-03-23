package com.cmccpoc.listener;

import java.util.LinkedHashMap;
import java.util.List;
import com.airtalkee.sdk.entity.AirChannel;
import com.airtalkee.sdk.entity.AirContact;

public interface OnMmiChannelListener
{
	/**
	 * 获取频道列表时触发
	 * @param isOk  是否获取完成
	 * @param channels  频道列表
	 */
	public void onChannelListGet(boolean isOk, final List<AirChannel> channels);

	/**
	 * 获取频道成员时触发
	 * @param channelId  频道Id
	 * @param members  频道成员列表
	 */
	public void onChannelMemberListGet(String channelId, final List<AirContact> members);

	/**
	 * 
	 * @param online
	 */
	public void onChannelOnlineCount(final LinkedHashMap<String, Integer> online);

	/**
	 * 
	 * @param ch
	 */
	public void onChannelPersonalCreateNotify(AirChannel ch);

	/**
	 * 
	 * @param ch
	 */
	public void onChannelPersonalDeleteNotify(AirChannel ch);

}
