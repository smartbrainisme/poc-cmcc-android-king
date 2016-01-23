package com.airtalkee.receiver;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import com.airtalkee.activity.AccountActivity;
import com.airtalkee.activity.MainActivity;
import com.airtalkee.activity.TempSessionActivity;
import com.airtalkee.config.Config;
import com.airtalkee.control.AirSessionControl;
import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.AirtalkeeChannel;
import com.airtalkee.sdk.AirtalkeeSessionManager;
import com.airtalkee.sdk.engine.AirPower;
import com.airtalkee.sdk.entity.AirChannel;
import com.airtalkee.sdk.entity.AirSession;
import com.airtalkee.sdk.util.Log;
import com.airtalkee.sdk.util.Utils;
import com.airtalkee.services.AirServices;

public class ReceiverMediaButton extends BroadcastReceiver
{
	private static final String HEADSET_PLUG = "HEADSET_PLUG";
	
	private static int ACTION_EARPHONE_DOWN = 0;
	private static int ACTION_EARPHONE_UP = 1;

	public static boolean isPttPressed = false;
	public static boolean isChannelToogle = false;

	@Override
	public void onReceive(Context context, Intent intent)
	{
		String intentAction = intent.getAction();
		android.util.Log.d("m","ReceiverMediaButton");
		Log.d(ReceiverMediaButton.class, "ReceiverMediaButton " + intentAction + " MODEL=" + Config.model + " pttAction=" + Config.pttButtonAction);
		if (Intent.ACTION_HEADSET_PLUG.equals(intentAction))
		{
			if (intent.hasExtra("state"))
			{
				if (intent.getIntExtra("state", 0) == 0)
				{
					HeadsetRemoved();
				}
				else if (intent.getIntExtra("state", 0) == 1)
				{
					HeadsetPlugin(context);
				}
			}
		}
		else if (Intent.ACTION_MEDIA_BUTTON.equals(intentAction))
		{
			KeyEvent keyEvent = (KeyEvent) intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
			int keyCode = keyEvent.getKeyCode();
			int keyAction = keyEvent.getAction();
			Log.d(ReceiverMediaButton.class, "ReceiverMediaButton  case1 keyCode=" + keyCode + " keyAction=" + keyAction);
			boolean isBtKeyCode = Config.pttBtKeycode == keyCode || keyCode == 88;
			if(isBtKeyCode)
			{
				if(keyCode == 87 && keyAction ==0 )
				{
					if(!isPttPressed)
					{
						AirtalkeeSessionManager.getInstance().TalkRequest(AirSessionControl.getInstance().getCurrentSession(), AirSessionControl.getInstance().getCurrentSessionGrap());
						isPttPressed = true;
					}
				}
				else if(keyCode == 88 && keyAction == 1)
				{
					isPttPressed = false;
					AirtalkeeSessionManager.getInstance().TalkRelease(AirSessionControl.getInstance().getCurrentSession());
				}
				abortBroadcast();
				return;
			}
			if (Config.pttEarphoneKeycode == keyCode)
			{
				if (keyAction == ACTION_EARPHONE_DOWN || (Config.pttEarphoneLongPress ? keyAction == ACTION_EARPHONE_UP : false))
				{
					if (AirSessionControl.getInstance().getCurrentSession() != null && !ReceiverPhoneState.isPhoneCalling(context))
					{
						try
						{
							int code = Config.pttEarphoneLongPress ? keyCode : 2;
							if (code != 1)
							{
								if (code == 2)
								{
									AirtalkeeSessionManager.getInstance().TalkButtonClick(AirSessionControl.getInstance().getCurrentSession(), AirSessionControl.getInstance().getCurrentSessionGrap());
								}
								else
								{
									switch (keyAction)
									{
										case KeyEvent.ACTION_DOWN:
											if(!isPttPressed)
											{
												AirtalkeeSessionManager.getInstance().TalkRequest(AirSessionControl.getInstance().getCurrentSession(), AirSessionControl.getInstance().getCurrentSessionGrap());
												isPttPressed = true;
											}
											break;
										case KeyEvent.ACTION_UP:
											isPttPressed = false;
											AirtalkeeSessionManager.getInstance().TalkRelease(AirSessionControl.getInstance().getCurrentSession());
											break;
									}
								}
							}
							else
							{
								AirtalkeeSessionManager.getInstance().TalkButtonClick(AirSessionControl.getInstance().getCurrentSession(), AirSessionControl.getInstance().getCurrentSessionGrap());
							}
							Log.d(ReceiverMediaButton.class, "ReceiverMediaButton case1 handled!!");
							abortBroadcast();
						}
						catch (Exception e)
						{
							// TODO: handle exception
							Log.d(ReceiverMediaButton.class, "ReceiverMediaButton case1 error=" + e.toString());
						}
					}
				}
			}
		}
		else if (TextUtils.equals(Config.pttButtonAction, intentAction))
		{
			Log.d(ReceiverMediaButton.class, "ReceiverMediaButton case2 pttAction=" + intentAction + " pttActionUpDownCode=" + Config.pttButtonActionUpDownCode);
			boolean enable = true;
			if (!AirtalkeeAccount.getInstance().isAccountRunning() && !Config.pttButtonPressWakeupWhenStandby)
			{
				Log.d(ReceiverMediaButton.class, "ReceiverMediaButton case2 IGNORE!!!");
				enable = false;
			}

			Log.d(ReceiverMediaButton.class, "ReceiverMediaButton case2 enable=" + enable);
			if (enable)
			{
				boolean isAppShow = true;
				boolean isUpDownSupport = false;
				String udKey = "";
				int udKeyUp = 0;
				int udKeyDown = 0;
				if (!Utils.isEmpty(Config.pttButtonActionUpDownCode))
				{
					String[] keys = Config.pttButtonActionUpDownCode.split("-");
					if (keys != null && keys.length == 3 && !Utils.isEmpty(keys[0]) && !Utils.isEmpty(keys[1]) && !Utils.isEmpty(keys[2]))
					{
						udKey = keys[0];
						udKeyDown = Integer.parseInt(keys[1]);
						udKeyUp = Integer.parseInt(keys[2]);
						isUpDownSupport = true;
					}
				}

				if (isUpDownSupport)
				{
					if (AirtalkeeAccount.getInstance().isEngineRunning())
					{
						AirSession session = AirSessionControl.getInstance().getCurrentSession();
						if (session != null && session.getSessionState() == AirSession.SESSION_STATE_DIALOG)
						{
							if (intent.getIntExtra(udKey, 0) == udKeyDown)
							{
								Log.d(MainActivity.class, "ReceiverMediaButton case2 PTT-DOWN");
								boolean toGrap = false;
								if (session.getType() == AirSession.TYPE_CHANNEL)
								{
									AirChannel ch = AirtalkeeChannel.getInstance().ChannelGetByCode(session.getSessionCode());
									if (ch != null)
									{
										toGrap = ch.isRoleAppling();
									}
								}
								AirtalkeeSessionManager.getInstance().TalkRequest(session, toGrap);
							}
							else if (intent.getIntExtra(udKey, 0) == udKeyUp)
							{
								Log.d(MainActivity.class, "ReceiverMediaButton case2 PTT-UP");
								AirtalkeeSessionManager.getInstance().TalkRelease(session);
							}
							isAppShow = false;
						}
					}
				}

				if (isAppShow)
				{
					if (AirServices.getInstance() != null)
					{
						AirServices.getInstance().lightScreen();
						AirServices.getInstance().unlockScreen();
					}
					else
						return;

					if (AirSessionControl.getInstance().getCurrentSession() != null)
					{
						boolean isSwitch = false;
						if (TempSessionActivity.getInstance() != null && TempSessionActivity.getInstance().isShowing)
							isSwitch = false;
						Log.d(ReceiverMediaButton.class, "ReceiverMediaButton case2 isSwitch=" + isSwitch);
						if (isSwitch)
						{
							Intent it = new Intent(context, MainActivity.class);
							it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							context.startActivity(it);
						}
					}
					else
					{
						launch(context);
					}
				}
			}
			return;
		}
		else if (!Utils.isEmpty(Config.pttButtonActionUp) && !Utils.isEmpty(Config.pttButtonActionDown))
		{
			Log.d(ReceiverMediaButton.class, "ReceiverMediaButton case3  pttAction=" + intentAction);
			if (AirSessionControl.getInstance().getCurrentSession() != null)
			{
				if (TextUtils.equals(Config.pttButtonActionUp, intentAction))
				{
					
					if(!isChannelToogle)
						AirtalkeeSessionManager.getInstance().TalkRelease(AirSessionControl.getInstance().getCurrentSession());
					isPttPressed = false;
				}
				else if (TextUtils.equals(Config.pttButtonActionDown, intentAction))
				{
					if (!isPttPressed)
					{
						if (AirServices.getInstance() != null)
						{
							if (AirSessionControl.getInstance().getCurrentSession() != null)
							{
								isChannelToogle = AirSessionControl.getInstance().channelToggle();
								if(!isChannelToogle)
									AirtalkeeSessionManager.getInstance().TalkRequest(AirSessionControl.getInstance().getCurrentSession(), AirSessionControl.getInstance().getCurrentSessionGrap());
							}
							else
							{
								launch(context);
							}
						}
						isPttPressed = true;
					}
				}
			}
			else if(Config.funcPTTLaunch)
			{
				Log.d(ReceiverMediaButton.class, "ReceiverMediaButton case3  currentSession == null");
				launch(context);
			}
		}
	}
	
	private void launch(Context context)
	{
		Intent it = new Intent(context,AccountActivity.class);
		it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(it);  
		
		//Intent intent = new Intent(AirServices.SERVICE_PATH);
		//context.startService(intent);	
	}
	
	public static void HeadsetPlugin(Context context)
	{
		if (Config.pttEarphonePlug)
		{    
			Log.d(ReceiverMediaButton.class, "ReceiverMediaButton HeadsetPlugin");
			AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
			ComponentName comp = new ComponentName(context.getPackageName(), ReceiverMediaButton.class.getName());
			try
			{
				mAudioManager.registerMediaButtonEventReceiver(comp);
			}
			catch (NoSuchMethodError e)
			{
				
			}
			mAudioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC,  AudioManager.AUDIOFOCUS_GAIN);
			AirPower.PowerManagerWakeup(HEADSET_PLUG);
		}
	}
	
	@SuppressWarnings("deprecation")
	public static void HeadsetPluginCheck(Context context)
	{
		if (Config.pttEarphonePlug)
		{
			AudioManager localAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);  
			if (localAudioManager.isWiredHeadsetOn())
				AirPower.PowerManagerWakeup(HEADSET_PLUG);
			else
				AirPower.PowerManagerSleep(HEADSET_PLUG);
		}
	}

	public static void HeadsetRemoved()
	{
		if (Config.pttEarphonePlug)
		{
			Log.d(ReceiverMediaButton.class, "ReceiverMediaButton HeadsetRemoved");
			AirPower.PowerManagerSleep(HEADSET_PLUG);
		}
	}
}