package com.airtalkee.listener;

import java.util.List;
import com.airtalkee.sdk.entity.AirMessage;
import com.airtalkee.sdk.entity.AirSession;

public interface OnMmiMessageListener
{
	public boolean onMessageIncomingRecv(boolean isCustom, AirMessage message);

	public void onMessageIncomingRecv(List<AirMessage> messageList);

	public void onMessageOutgoingSent(boolean isCustom, AirMessage message, boolean isSent);

	public void onMessageUpdated(AirMessage message);

	public void onMessageRecordStart();

	public void onMessageRecordStop(int seconds, String msgCode);

	public void onMessageRecordTransfered(String msgCode, String resId);

	public void onMessageRecordPlayLoading(String msgCode, String resId);

	public void onMessageRecordPlayLoaded(boolean isOk, String msgCode, String resId);

	public void onMessageRecordPlayStart(String msgCode, String resId);

	public void onMessageRecordPlayStop(String msgCode, String resId);

	public void onMessageRecordPtt(AirSession session, AirMessage message, String msgCode, String resId);
}
