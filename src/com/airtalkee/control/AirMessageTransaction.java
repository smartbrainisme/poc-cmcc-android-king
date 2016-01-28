package com.airtalkee.control;

import java.util.List;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import com.airtalkee.R;
import com.airtalkee.Util.Sound;
import com.airtalkee.Util.Toast;
import com.airtalkee.Util.Util;
import com.airtalkee.activity.AccountActivity;
import com.airtalkee.activity.MenuNoticeActivity;
import com.airtalkee.activity.home.HomeActivity;
import com.airtalkee.activity.home.SessionDialogActivity;
import com.airtalkee.activity.home.widget.AlertDialog;
import com.airtalkee.activity.home.widget.AlertDialog.DialogListener;
import com.airtalkee.activity.home.widget.CallAlertDialog;
import com.airtalkee.activity.home.widget.CallAlertDialog.OnAlertDialogCancelListener;
import com.airtalkee.activity.home.widget.StatusBarTitle;
import com.airtalkee.config.Config;
import com.airtalkee.listener.OnMmiMessageListener;
import com.airtalkee.listener.OnMmiNoticeListener;
import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.AirtalkeeChannel;
import com.airtalkee.sdk.AirtalkeeMessage;
import com.airtalkee.sdk.AirtalkeeSessionManager;
import com.airtalkee.sdk.OnMessageListener;
import com.airtalkee.sdk.OnMessagePttListener;
import com.airtalkee.sdk.OnSystemBroadcastListener;
import com.airtalkee.sdk.OnSystemFenceWarningListener;
import com.airtalkee.sdk.controller.AirTaskController;
import com.airtalkee.sdk.controller.SessionController;
import com.airtalkee.sdk.entity.AirChannel;
import com.airtalkee.sdk.entity.AirFunctionSetting;
import com.airtalkee.sdk.entity.AirMessage;
import com.airtalkee.sdk.entity.AirSession;
import com.airtalkee.sdk.listener.AirTaskPushListener;
import com.airtalkee.sdk.util.Log;
import com.airtalkee.sdk.util.Utils;
import com.airtalkee.services.AirServices;

public class AirMessageTransaction implements OnMessageListener,
		OnMessagePttListener, OnSystemBroadcastListener,
		OnSystemFenceWarningListener, AirTaskPushListener,
		DialogListener
{
	private static final int DIALOG_2_SEND_MESSAGE = 101;
	private static final int DIALOG_CALL_CENTER = 100;
	private static final int DIALOG_CALL = 102;

	private static AirMessageTransaction mInstance = null;
	private OnMmiMessageListener msgListener = null;
	private OnMmiNoticeListener noticeListener = null;
	AlertDialog dialog;
	private AlertDialog alertDialog;

	private AirMessageTransaction()
	{
		AirtalkeeMessage.getInstance().setOnMessageListener(this);
		AirtalkeeMessage.getInstance().setOnMessagePttListener(this);
		AirtalkeeAccount.getInstance().setOnSystemBroadcastListener(this);
		AirtalkeeAccount.getInstance().setOnSystemFenceWarningListener(this);
		AirTaskController.getInstance().setAirTalkPushListener(this);
	}

	public static AirMessageTransaction getInstance()
	{
		if (mInstance == null)
		{
			mInstance = new AirMessageTransaction();
		}
		return mInstance;
	}

	public void setOnMessageListener(OnMmiMessageListener l)
	{
		this.msgListener = l;
	}

	public void setOnNoticeListener(OnMmiNoticeListener l)
	{
		this.noticeListener = l;
	}

	@Override
	public void onMessageIncomingRecv(List<AirMessage> messageList)
	{
		// TODO Auto-generated method stub
		if (msgListener != null)
			msgListener.onMessageIncomingRecv(messageList);
	}

	@Override
	public void onMessageIncomingRecv(boolean isCustom, AirMessage message)
	{
		// TODO Auto-generated method stub
		Log.i(AirMessageTransaction.class, "AirMessageTransaction onMessageIncomingRecv");
		Context ct = AirServices.getInstance();
		String from = "";
		String typeText = "";
		String msg = "";
		if (message != null)
		{
			from = message.getInameFrom();

			switch (message.getType())
			{
				case AirMessage.TYPE_PICTURE:
					typeText = Config.app_name + ct.getString(R.string.talk_msg_pic);
					msg = typeText;
					break;
				case AirMessage.TYPE_TEXT:
					typeText = Config.app_name + ct.getString(R.string.talk_msg_text);
					msg = message.getBody();
					break;
				case AirMessage.TYPE_RECORD:
					AirtalkeeMessage.getInstance().MessageRecordPlayDownload(message);
					typeText = Config.app_name + ct.getString(R.string.talk_msg_rec);
					msg = typeText;
					break;
				case AirMessage.TYPE_SYSTEM:
					typeText = Config.app_name + ct.getString(R.string.talk_session_msg);
					msg = typeText;
					break;
				case AirMessage.TYPE_CHANNEL_ALERT:
					typeText = ct.getString(R.string.talk_incoming_channel_alert_message);
					msg = typeText;
					break;
			}
		}

		boolean isHandled = false;
		if (msgListener != null)
			isHandled = msgListener.onMessageIncomingRecv(isCustom, message);
		if (!isHandled && message.getSessionCode() != null)
		{
			Intent intent = new Intent();
			if (AirSession.sessionType(message.getSessionCode()) == AirSession.TYPE_CHANNEL)
			{
				intent.setClass(ct, AccountActivity.class);
			}
			else
			{
				intent.setClass(ct, SessionDialogActivity.class);
				intent.putExtra("sessionCode", message.getSessionCode());
				intent.putExtra("type", AirServices.TEMP_SESSION_TYPE_RESUME);
			}
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			Util.showNotification(Util.NOTIFI_ID_MESSAGE, AirServices.getInstance(), intent, from, typeText, msg, null);
			Sound.playSound(Sound.PLAYER_NEWINFO, false, ct);

//			if (MainActivity.getInstance() != null && MainActivity.getInstance().viewMiddle != null)
//			{
//				MainActivity.getInstance().viewMiddle.refreshNewMsg();
//			}
//
//			if (SessionAndChannelView.getInstance() != null)
//			{
//				SessionAndChannelView.getInstance().refreshChannelAndDialog();
//				SessionAndChannelView.getInstance().resume();
//			}
//			if (HomeActivity.getInstance() != null)
//			{
//				HomeActivity.getInstance().checkNewIM(false);
//			}
		}
	}

	@Override
	public void onMessageOutgoingSent(boolean isCustom, AirMessage message, boolean isSent)
	{
		// TODO Auto-generated method stub
		if (msgListener != null)
			msgListener.onMessageOutgoingSent(isCustom, message, isSent);
	}

	@Override
	public void onMessageUpdated(AirMessage message)
	{
		// TODO Auto-generated method stub
		if (msgListener != null)
			msgListener.onMessageUpdated(message);
	}

	@Override
	public void onMessageRecordPlayLoaded(boolean isOk, String msgCode, String resId, byte[] resBytes)
	{
		// TODO Auto-generated method stub
		if (msgListener != null)
			msgListener.onMessageRecordPlayLoaded(isOk, msgCode, resId);
	}

	@Override
	public void onMessageRecordPlayLoading(String msgCode, String resId)
	{
		// TODO Auto-generated method stub
		if (msgListener != null)
			msgListener.onMessageRecordPlayLoading(msgCode, resId);
	}

	@Override
	public void onMessageRecordPlayStart(String msgCode, String resId)
	{
		// TODO Auto-generated method stub
		if (msgListener != null)
			msgListener.onMessageRecordPlayStart(msgCode, resId);
	}

	@Override
	public void onMessageRecordPlayStop(String msgCode, String resId)
	{
		// TODO Auto-generated method stub
		if (msgListener != null)
			msgListener.onMessageRecordPlayStop(msgCode, resId);
	}

	@Override
	public void onMessageRecordStart()
	{
		// TODO Auto-generated method stub
		if (msgListener != null)
			msgListener.onMessageRecordStart();
	}

	@Override
	public void onMessageRecordStop(int seconds, String msgCode)
	{
		// TODO Auto-generated method stub
		if (msgListener != null)
			msgListener.onMessageRecordStop(seconds, msgCode);
	}

	@Override
	public void onMessageRecordTransfered(String msgCode, String resId)
	{
		// TODO Auto-generated method stub
		if (msgListener != null)
			msgListener.onMessageRecordTransfered(msgCode, resId);
	}

	@Override
	public void onMessagePttRecord(AirSession session, AirMessage message, String msgCode, String resId)
	{
		// TODO Auto-generated method stub
		if (session != null && message != null && AirSession.sessionType(message.getSessionCode()) == AirSession.TYPE_CHANNEL && !TextUtils.equals(message.getIpocidFrom(), AirtalkeeAccount.getInstance().getUserId()) && AirServices.getInstance() != null && !Util.isScreenOn(AirServices.getInstance()) && HomeActivity.getInstance() != null)
		{
			AirChannel channel = AirtalkeeChannel.getInstance().ChannelGetByCode(message.getSessionCode());
			if (channel != null)
			{
				String from = channel.getDisplayName();
				String typeText = AirServices.getInstance().getString(R.string.talk_msg_ptt);
				String msg = typeText + " (" + message.getInameFrom() + ")";
				message.setState(AirMessage.STATE_NEW);

				Intent intent = new Intent();
				intent.setClass(AirServices.getInstance(), AccountActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				Util.showNotification(Util.NOTIFI_ID_VOICE_RECORD, AirServices.getInstance(), intent, from, typeText, msg, null);

				session.setMessageUnreadCount(session.getMessageUnreadCount() + 1);
			}
		}

		if (msgListener != null)
			msgListener.onMessageRecordPtt(session, message, msgCode, resId);
	}

	/**********************************
	 * 
	 * �㲥
	 * 
	 **********************************/

	@Override
	public void onSystemBroadcastNumber(int number)
	{
		// TODO Auto-generated method stub
		if (noticeListener != null)
		{
			noticeListener.onMmiNoticeNew(AirtalkeeAccount.getInstance().SystemBroadcastNumberGet());
		}
	}

	@Override
	public void onSystemBroadcastPush(String title, String url)
	{
		// TODO Auto-generated method stub
		Context ct = HomeActivity.getInstance();
		if (ct != null)
		{
			Intent intent = new Intent();
			intent.setClass(ct, MenuNoticeActivity.class);
			intent.putExtra("url", url);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			Util.showNotification(Util.NOTIFI_ID_NOTICE, ct, intent, title, "[" + ct.getString(R.string.talk_tools_notice) + "] " + title, title, null);
			Sound.playSound(Sound.PLAYER_NEWINFO, false, ct);
			// 弹出窗口
			try
			{
				dialog = new AlertDialog(ct, ct.getString(R.string.talk_tools_notice), title, ct.getString(R.string.talk_tools_know), ct.getString(R.string.talk_session_call), this, DIALOG_CALL);
				dialog.show();
				StatusBarTitle.getInstance().checkBrodcast();
			}
			catch (Exception e)
			{
				String str = e.getMessage();
				e.printStackTrace();
			}
		}
		if (noticeListener != null)
		{
			noticeListener.onMmiNoticeNew(AirtalkeeAccount.getInstance().SystemBroadcastNumberGet());
		}
	}

	/**********************************
	 * 
	 * 告警 电子围栏
	 * 
	 **********************************/

	@Override
	public void onSystemFenceWarningPush(String[] fenceName)
	{
		// TODO Auto-generated method stub
		Context ct = AirServices.getInstance();
		if (ct != null)
		{
			Intent intent = new Intent();
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			Util.showNotification(Util.NOTIFI_ID_FENCE_WARNING, ct, intent, ct.getString(R.string.talk_fence_warning_title), "[" + ct.getString(R.string.talk_fence_warning_title) + "] " + ct.getString(R.string.talk_fence_warning_tip), ct.getString(R.string.talk_fence_warning_tip), null);
			Toast.makeText1(ct, R.drawable.ic_error, ct.getString(R.string.talk_fence_warning_title) + "\r\n" + ct.getString(R.string.talk_fence_warning_tip), Toast.LENGTH_LONG).show();
			Sound.playSound(Sound.PLAYER_PTI, false, ct);
		}
	}

	@Override
	public void onClickOk(int id, Object obj)
	{
		switch (id)
		{
			case DIALOG_CALL:
				callStationCenter();
				break;
			case DIALOG_2_SEND_MESSAGE:
				if (obj != null)
				{
					String sessionCode = obj.toString();
					Context context = AirServices.getInstance();
					if (null != context)
					{
						Intent it = new Intent(context, SessionDialogActivity.class);
						it.putExtra("sessionCode", sessionCode);
						it.putExtra("type", AirServices.TEMP_SESSION_TYPE_MESSAGE);
						context.startActivity(it);
					}
				}
				break;
		}
	}

	@Override
	public void onClickOk(int id, boolean isChecked)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void onClickCancel(int id)
	{
		// TODO Auto-generated method stub
	}

	private void callStationCenter()
	{
		final Context context = HomeActivity.getInstance();
		if (Config.funcCenterCall == AirFunctionSetting.SETTING_ENABLE)
		{
			if (AirtalkeeAccount.getInstance().isAccountRunning())
			{
				if (AirtalkeeAccount.getInstance().isEngineRunning())
				{
					final AirSession s = SessionController.SessionMatchSpecial(AirtalkeeSessionManager.SPECIAL_NUMBER_DISPATCHER, context.getString(R.string.talk_tools_call_center));
					if (s != null)
					{
						alertDialog = new CallAlertDialog(context, "正在呼叫" + s.getDisplayName(), "请稍后...", s.getSessionCode(), DIALOG_CALL_CENTER, new OnAlertDialogCancelListener()
						{
							@Override
							public void onDialogCancel(int reason)
							{
								// TODO Auto-generated method stub
								switch (reason)
								{
									case AirSession.SESSION_RELEASE_REASON_NOTREACH:
										dialog = new AlertDialog(context, null, context.getString(R.string.talk_call_offline_tip), context.getString(R.string.talk_session_call_cancel), context.getString(R.string.talk_call_leave_msg), AirMessageTransaction.this, DIALOG_2_SEND_MESSAGE, s.getSessionCode());
										dialog.show();
										break;
									default:
										break;
								}
							}
						});
						alertDialog.show();
					}
				}
				else
				{
					Util.Toast(context, context.getString(R.string.talk_network_warning));
				}
			}
		}
		else if (Config.funcCenterCall == AirFunctionSetting.SETTING_CALL_NUMBER && !Utils.isEmpty(Config.funcCenterCallNumber))
		{
			Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + Config.funcCenterCallNumber));
			context.startActivity(intent);
		}
	}

	@Override
	public void onTaskDispatch(String taskId, String taskName)
	{
		// TODO Auto-generated method stub
		
	}

}
