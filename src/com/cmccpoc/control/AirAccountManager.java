package com.cmccpoc.control;

import java.util.LinkedHashMap;
import java.util.List;
import android.os.Build;
import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.AirtalkeeChannel;
import com.airtalkee.sdk.AirtalkeeMediaVideoControl;
import com.airtalkee.sdk.AirtalkeeMessage;
import com.airtalkee.sdk.AirtalkeeSessionManager;
import com.airtalkee.sdk.AirtalkeeUserInfo;
//import com.airtalkee.sdk.AirtalkeeVideo;
import com.airtalkee.sdk.OnAccountListener;
import com.airtalkee.sdk.OnAccountSettingListener;
import com.airtalkee.sdk.OnChannelListener;
import com.airtalkee.sdk.OnMediaVideoListener;
//import com.airtalkee.sdk.OnVideoListener;
import com.airtalkee.sdk.entity.AirChannel;
import com.airtalkee.sdk.entity.AirContact;
import com.airtalkee.sdk.entity.AirFunctionSetting;
import com.airtalkee.sdk.entity.AirSession;
import com.airtalkee.sdk.util.Log;
import com.airtalkee.sdk.util.Utils;
import com.cmccpoc.R;
import com.cmccpoc.config.Config;
import com.cmccpoc.listener.OnMmiAccountListener;
import com.cmccpoc.listener.OnMmiChannelListener;
import com.cmccpoc.location.AirLocation;
import com.cmccpoc.services.AirServices;

/**
 * 用户管理类
 * @author Yao
 */
public class AirAccountManager implements OnAccountListener, OnChannelListener, OnAccountSettingListener, OnMediaVideoListener
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
			AirtalkeeMediaVideoControl.getInstance().setOnVideoListener(mInstance);
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

	/**
	 * 用户账号功能配置
	 * @param setting 配置项Entity
	 */
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
				if (Config.funcCenterLocation != AirFunctionSetting.SETTING_DISABLE && Config.funcCenterLocation != AirFunctionSetting.SETTING_ENABLE && Config.funcCenterLocation != AirFunctionSetting.SETTING_LOCATION_FORCE)
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
			if (setting.userAll != AirFunctionSetting.SETTING_IGNORE)
			{
				Config.funcUserAll = (setting.userAll != AirFunctionSetting.SETTING_DISABLE);
			}
			if (setting.video != AirFunctionSetting.SETTING_IGNORE)
			{
				Config.funcVideo = (setting.video != AirFunctionSetting.SETTING_DISABLE ? true : false);
				if (Build.VERSION.SDK_INT < 16) // v4.1
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
		Log.e(AirAccountManager.class, "MMI ======================== onLogin =======================");
		if (accountListener != null)
		{
			accountListener.onMmiHeartbeatLogin(result);
		}

		if (result == AirtalkeeAccount.ACCOUNT_RESULT_OK)
		{
			AirtalkeeMediaVideoControl.getInstance().VideoAddr();

			AirServices.iOperator.putString(KEY_ID, AirtalkeeAccount.getInstance().getUserId());
			AirServices.iOperator.putString(KEY_PWD, AirtalkeeAccount.getInstance().getUserKey());
			if (!AirtalkeeChannel.getInstance().isChannelListLoaded())
			{
				AirtalkeeChannel.getInstance().ChannelListGet(AirtalkeeChannel.CHANNEL_TYPE_SECRET);
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
		//AirtalkeeSessionManager.getInstance().GroupBroadcastRun();
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
			// AirSessionControl.getInstance().SessionChannelOut(ch.getId());
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

	/**
	 * 视频地址赋值 
	 * @param isOk 状态
	 * @param serverIp IP地址
	 * @param serverPort 端口号
	 */
	@Override
	public void onVideoAddr(boolean isOk, String serverIp, int serverPort)
	{
		// TODO Auto-generated method stub
		Log.i(AirAccountManager.class, "VIDEO ADDR: isOk=" + isOk + " serverIp=" + serverIp + " serverPort=" + serverPort);
		VIDEO_IP = serverIp;
		VIDEO_PORT = serverPort;
	}

	/**
	 * 开始录制视频
	 * @param sessionId 会话Id
	 * @param result 结果状态
	 */
	@Override
	public void onVideoRecorderStart(int sessionId, int result)
	{
		// TODO Auto-generated method stub
	}

	/**
	 * 结束录制视频
	 * @param sessionId 会话Id
	 */
	@Override
	public void onVideoRecorderStop(int sessionId)
	{
		// TODO Auto-generated method stub
	}

}
