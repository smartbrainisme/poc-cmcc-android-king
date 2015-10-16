package com.airtalkee.control;

import java.util.LinkedHashMap;
import java.util.List;

import android.os.Build;

import com.airtalkee.R;
import com.airtalkee.config.Config;
import com.airtalkee.listener.OnMmiAccountListener;
import com.airtalkee.listener.OnMmiChannelListener;
import com.airtalkee.location.AirLocation;
import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.AirtalkeeChannel;
import com.airtalkee.sdk.AirtalkeeMessage;
import com.airtalkee.sdk.AirtalkeeSessionManager;
import com.airtalkee.sdk.AirtalkeeUserInfo;
import com.airtalkee.sdk.AirtalkeeVideo;
import com.airtalkee.sdk.OnAccountListener;
import com.airtalkee.sdk.OnAccountSettingListener;
import com.airtalkee.sdk.OnChannelListener;
import com.airtalkee.sdk.OnVideoListener;
import com.airtalkee.sdk.entity.AirChannel;
import com.airtalkee.sdk.entity.AirContact;
import com.airtalkee.sdk.entity.AirFunctionSetting;
import com.airtalkee.sdk.entity.AirSession;
import com.airtalkee.sdk.util.Log;
import com.airtalkee.sdk.util.Utils;
import com.airtalkee.services.AirServices;

public class AirAccountManager implements OnAccountListener, OnChannelListener, OnAccountSettingListener, OnVideoListener
{
	public static final String KEY_ID = "USER_ID";
	public static final String KEY_PWD = "USER_PWD";
	public static final String KEY_HB = "USER_HB";
	
	public static String VIDEO_IP = "";
	public static int VIDEO_PORT = 0;

	private static AirAccountManager mInstance;
	private OnMmiAccountListener accountListener = null;
	private OnMmiChannelListener channelListener = null;

	public static AirAccountManager getInstance()
	{
		if (mInstance == null)
		{
			mInstance = new AirAccountManager();
			AirtalkeeAccount.getInstance().setOnAccountListener(mInstance);
			AirtalkeeAccount.getInstance().setOnAccountSettingListener(mInstance);
			AirtalkeeChannel.getInstance().setOnChannelListListener(mInstance);
			AirtalkeeVideo.getInstance().setOnVideoListener(mInstance);
		}
		return mInstance;
	}

	public void setAccountListener(OnMmiAccountListener listener)
	{
		this.accountListener = listener;
	}

	public void setChannelListener(OnMmiChannelListener listener)
	{
		this.channelListener = listener;
	}

	@Override
	public void onAccountFunctionSetting(AirFunctionSetting setting)
	{
		// TODO Auto-generated method stub
		if (Config.funcFORCE == false)
		{
			if (setting.centerCall != AirFunctionSetting.SETTING_IGNORE)
			{
				Config.funcCenterCall = setting.centerCall;
			}
			if (Config.funcCenterCall == AirFunctionSetting.SETTING_CALL_NUMBER)
			{
				if (!Utils.isEmpty(setting.centerCallNumber))
				{
					Config.funcCenterCallNumber = setting.centerCallNumber;
				}
				else
				{
					Config.funcCenterCall = AirFunctionSetting.SETTING_DISABLE;
				}
			}
			if (setting.centerReport != AirFunctionSetting.SETTING_IGNORE)
			{
				Config.funcCenterReport = (setting.centerReport == AirFunctionSetting.SETTING_ENABLE);
			}
			if (setting.centerLocation != AirFunctionSetting.SETTING_IGNORE)
			{
				Config.funcCenterLocation = setting.centerLocation;
				if (Config.funcCenterLocation != AirFunctionSetting.SETTING_DISABLE && Config.funcCenterLocation != AirFunctionSetting.SETTING_ENABLE
					&& Config.funcCenterLocation != AirFunctionSetting.SETTING_LOCATION_FORCE)
				{
					Config.funcCenterLocation = AirFunctionSetting.SETTING_DISABLE;
				}

				if (Config.funcCenterLocation == AirFunctionSetting.SETTING_LOCATION_FORCE)
				{
					AirLocation.getInstance(AirServices.getInstance()).setFrequenceDefault(setting.centerLocationFrequency, true);
				}
				else
				{
					AirLocation.getInstance(AirServices.getInstance()).setFrequenceDefault(setting.centerLocationFrequency, false);
				}
			}
			if (setting.manual != AirFunctionSetting.SETTING_IGNORE)
			{
				Config.funcManual = (setting.manual == AirFunctionSetting.SETTING_ENABLE);
			}
			if (setting.broadcast != AirFunctionSetting.SETTING_IGNORE)
			{
				Config.funcBroadcast = (setting.broadcast == AirFunctionSetting.SETTING_ENABLE);
			}
			if (setting.userRegistration != AirFunctionSetting.SETTING_IGNORE)
			{
				Config.funcUserRegistration = (setting.userRegistration == AirFunctionSetting.SETTING_ENABLE);
			}
			if (setting.userAll != AirFunctionSetting.SETTING_IGNORE)
			{
				Config.funcUserAll = (setting.userAll != AirFunctionSetting.SETTING_DISABLE);
			}
			if (setting.channelManage != AirFunctionSetting.SETTING_IGNORE)
			{
				Config.funcChannelManage = (setting.channelManage == AirFunctionSetting.SETTING_ENABLE);
			}
			if (setting.video != AirFunctionSetting.SETTING_IGNORE)
			{
				Config.funcVideo = (setting.video != AirFunctionSetting.SETTING_DISABLE ? true : false);
				if (Build.VERSION.SDK_INT < 16)		// v4.1
					Config.funcVideo = false;
			}
		}
	}

	@Override
	public void onAccountMatch(int result, String userId)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onHeartbeat(int result)
	{
		// TODO Auto-generated method stub
		if (result == AirtalkeeAccount.ACCOUNT_RESULT_ERR_SINGLE)
		{
			AirServices.iOperator.putString(KEY_PWD, "");
		}
		if (accountListener != null)
		{
			accountListener.onMmiHeartbeatException(result);
		}
	}

	@Override
	public void onLogin(int result)
	{
		// TODO Auto-generated method stub
		Log.e(AirAccountManager.class, "MMI ======================== onLogin =======================");
		if (accountListener != null)
		{
			accountListener.onMmiHeartbeatLogin(result);
		}

		if (result == AirtalkeeAccount.ACCOUNT_RESULT_OK)
		{
			AirSessionMediaSound.toggleLedStatus(1 ,0);
			AirtalkeeVideo.getInstance().VideoAddr();
			
			AirServices.iOperator.putString(KEY_ID, AirtalkeeAccount.getInstance().getUserId());
			AirServices.iOperator.putString(KEY_PWD, AirtalkeeAccount.getInstance().getUserKey());
			if (!AirtalkeeChannel.getInstance().isChannelListLoaded())
			{
				if (Config.marketCode == Config.MARKET_FIRE_BJ)
				{
					AirtalkeeChannel.getInstance().ChannelListGet(AirtalkeeChannel.CHANNEL_TYPE_ALL);
				}
				else
				{
					AirtalkeeChannel.getInstance().ChannelListGet(AirtalkeeChannel.CHANNEL_TYPE_SECRET);
				}
			}
			AirtalkeeUserInfo.getInstance().onUserInfoGetEvent(AirtalkeeUserInfo.getInstance().getUserInfo());
			AirLocation.getInstance(AirServices.getInstance()).loopCheck();
		}
	}

	@Override
	public void onLogout()
	{
		// TODO Auto-generated method stub
		
		if (accountListener != null)
		{
			accountListener.onMmiHeartbeatLogout();
		}
		AirServices.getInstance().stopSelf();
		Log.e(AirAccountManager.class, "MMI ======================== onLogout =======================");
	}

	// ---------------------------
	// Channel Event
	// ---------------------------

	@Override
	public void onChannelListGet(boolean isOk, List<AirChannel> channels)
	{
		// TODO Auto-generated method stub
		Log.i(AirAccountManager.class, "MMI onChannelListGet isOk=" + isOk);
		if (isOk)
		{
			for (int i = 0; i < channels.size(); i++)
			{
				AirtalkeeChannel.getInstance().ChannelMemberGet(channels.get(i).getId());
			}
		}
		
		AirSessionControl.getInstance().channelAttachLoad();
		AirSessionControl.getInstance().SessionChannelAttach(AirServices.iOperator.getBoolean(AirAccountManager.KEY_HB, false));
		AirServices.iOperator.putBoolean(AirAccountManager.KEY_HB, true);
		if (channelListener != null)
		{
			channelListener.onChannelListGet(isOk, channels);
		}
	}

	@Override
	public void onChannelMemberListGet(String channelId, List<AirContact> members)
	{
		// TODO Auto-generated method stub
		if (channelListener != null)
		{
			channelListener.onChannelMemberListGet(channelId, members);
		}
	}

	@Override
	public void onChannelOnlineCount(LinkedHashMap<String, Integer> online)
	{
		// TODO Auto-generated method stub
		if (channelListener != null)
		{
			channelListener.onChannelOnlineCount(online);
		}
	}

	@Override
	public void onChannelPersonalCreateNotify(AirChannel ch)
	{
		// TODO Auto-generated method stub
		if (ch != null)
		{
			AirtalkeeChannel.getInstance().ChannelMemberGet(ch.getId());
			AirSession session = AirtalkeeSessionManager.getInstance().SessionMatch(ch.getId());
			if (session != null)
			{
				String tip = String.format(AirServices.getInstance().getString(R.string.talk_channel_tip_create), ch.getCreatorName());
				AirtalkeeMessage.getInstance().MessageSystemGenerate(session, tip, true);
			}

			if (channelListener != null)
			{
				channelListener.onChannelPersonalCreateNotify(ch);
			}
		}
	}

	@Override
	public void onChannelPersonalDeleteNotify(AirChannel ch)
	{
		// TODO Auto-generated method stub
		if (ch != null)
		{
			//AirSessionControl.getInstance().SessionChannelOut(ch.getId());
			AirSession session = AirtalkeeSessionManager.getInstance().SessionMatch(ch.getCreator());
			if (session != null)
			{
				session.setVisible(true);

				String tip = String.format(AirServices.getInstance().getString(R.string.talk_channel_tip_delete), ch.getDisplayName());
				AirtalkeeMessage.getInstance().MessageSystemGenerate(session, tip, true);
			}
			if (channelListener != null)
			{
				channelListener.onChannelPersonalDeleteNotify(ch);
			}
		}
	}

	@Override
	public void onVideoAddr(boolean isOk, String serverIp, int serverPort)
	{
		// TODO Auto-generated method stub
		Log.i(AirAccountManager.class, "VIDEO ADDR: isOk="+isOk + " serverIp="+serverIp + " serverPort="+serverPort);
		VIDEO_IP = serverIp;
		VIDEO_PORT = serverPort;
	}

}
