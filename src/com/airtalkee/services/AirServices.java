package com.airtalkee.services;

import java.io.File;
import android.R.integer;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.text.TextUtils;
import android.view.WindowManager;
import com.airtalkee.R;
import com.airtalkee.Util.AirMmiTimer;
import com.airtalkee.Util.Language;
import com.airtalkee.Util.Setting;
import com.airtalkee.Util.Sound;
import com.airtalkee.Util.SoundPlayer;
import com.airtalkee.Util.Util;
import com.airtalkee.activity.DialogVersionUpdate;
import com.airtalkee.activity.MainActivity;
import com.airtalkee.activity.SessionBoxTalk;
import com.airtalkee.activity.TempSessionActivity;
import com.airtalkee.activity.home.HomeActivity;
import com.airtalkee.activity.home.SessionDialogActivity;
import com.airtalkee.activity.home.widget.InCommingAlertDialog;
import com.airtalkee.application.MainApplication;
import com.airtalkee.bluetooth.BluetoothManager;
import com.airtalkee.config.Config;
import com.airtalkee.control.AirAccountManager;
import com.airtalkee.control.AirMessageTransaction;
import com.airtalkee.control.AirReportManager;
import com.airtalkee.control.AirSessionControl;
import com.airtalkee.control.AirSessionMediaSound;
import com.airtalkee.dao.DBHelp;
import com.airtalkee.location.AirLocation;
import com.airtalkee.receiver.ReceiverConnectionChange;
import com.airtalkee.receiver.ReceiverPhoneState;
import com.airtalkee.receiver.ReceiverScreenOff;
import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.AirtalkeeChannel;
import com.airtalkee.sdk.AirtalkeeContact;
import com.airtalkee.sdk.AirtalkeeMediaVisualizer;
import com.airtalkee.sdk.AirtalkeeMessage;
import com.airtalkee.sdk.AirtalkeeSessionManager;
import com.airtalkee.sdk.AirtalkeeUserInfo;
import com.airtalkee.sdk.AirtalkeeUserRegister;
import com.airtalkee.sdk.AirtalkeeVersionUpdate;
import com.airtalkee.sdk.OnChannelAlertListener;
import com.airtalkee.sdk.OnSessionIncomingListener;
import com.airtalkee.sdk.OnVersionUpdateListener;
import com.airtalkee.sdk.controller.AccountController;
import com.airtalkee.sdk.engine.AirEngine;
import com.airtalkee.sdk.entity.AirChannel;
import com.airtalkee.sdk.entity.AirContact;
import com.airtalkee.sdk.entity.AirSession;
import com.airtalkee.sdk.entity.DBProxy;
import com.airtalkee.sdk.util.IOoperate;
import com.airtalkee.sdk.util.Log;
import com.airtalkee.tts.TTSManager;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.umeng.analytics.MobclickAgent;

public class AirServices extends Service implements OnSessionIncomingListener,
		OnChannelAlertListener, OnVersionUpdateListener
{
	@SuppressWarnings("deprecation")
	private KeyguardManager.KeyguardLock mKeyguardLock;
	private KeyguardManager km;
	public static final String SERVICE_PATH = "com.airtalkee.services.AirServices";
	private Dialog incomingDialog;
	private final IBinder mBinder = new LocalBinder();
	private final ReceiverConnectionChange ccr = new ReceiverConnectionChange();
	BroadcastReceiver receiverScreen;
	private static AirServices context = null;
	public static boolean isScreenOn = false;

	public static boolean appRunning = false;
	public static DBProxy db_proxy = null;
	public static boolean VERSION_NEW = false;

	public static IOoperate iOperator = null;

	public static AirServices getInstance()
	{
		return context;
	}

	public IBinder onBind(Intent intent)
	{
		// TODO Auto-generated method stub
		return mBinder;
	}

	public class LocalBinder extends Binder
	{
		public AirServices getService()
		{
			return AirServices.this;
		}
	}

	public void onCreate()
	{
		super.onCreate();
		context = this;
		Log.i(AirServices.class, "AirServices onCreate");
		appRun();
		registerReceiver(ccr, new IntentFilter(ReceiverConnectionChange.ACTION));
		registerScreenReceiver();
		appRunning = true;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		// TODO Auto-generated method stub
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy()
	{
		// TODO Auto-generated method stub
		super.onDestroy();
		unregisterReceiver(ccr);
		if (BluetoothManager.getInstance() != null)
			BluetoothManager.getInstance().release();

		if (receiverScreen != null)
			unregisterReceiver(receiverScreen);
		context = null;
		Log.i(AirServices.class, "AirServices onDestroy");
		appRunning = false;
		System.exit(0);
	}

	private void appRun()
	{
		try
		{
			Log.e(AirServices.class, "AirServices  appRun!");
			MainApplication.setFirstLaunch(false);
			AirSessionMediaSound.toggleLight(false, 0xFFFF0000, this);
			IOoperate.setContext(this);
			iOperator = new IOoperate();
			AirMmiTimer.getInstance();
			Util.versionConfig(this);
			BluetoothManager.newInstance(this);
			MobclickAgent.updateOnlineConfig(this);
			initImageLoader();
			SoundPlayer.soundInit(this);
			Setting.getPttClickSupport();
			Setting.getPttVolumeSupport();

			TTSManager.getInstance().init(this);// getBaseContext().getFilesDir().getAbsolutePath().replace("files",
												// "lib")
			db_proxy = new DBHelp(this);
			db_proxy.DbActionRun();
			AirtalkeeAccount.getInstance();
			AirtalkeeMessage.getInstance();
			AirtalkeeSessionManager.getInstance();
			AirtalkeeChannel.getInstance();
			AirtalkeeUserInfo.getInstance();
			AirtalkeeContact.getInstance();
			AirtalkeeContact.getInstance();
			AirtalkeeUserRegister.getInstance();
			AirMessageTransaction.getInstance();
			AirSessionControl.getInstance();
			AirAccountManager.getInstance();
			AirReportManager.getInstance();
			AirLocation.getInstance(this).locationRun();

			// AirtalkeeMediaAudioControl.getInstance().setMediaAudioCodecAmrMode(AirtalkeeMediaAudioControl.AUDIO_CODEC_AMR_MODE_7);
			AirtalkeeMediaVisualizer.getInstance().setMediaAudioVisualizerValid(true, true);
			AirtalkeeMediaVisualizer.getInstance().setMediaAudioVisualizerSpectrumNumber(SessionBoxTalk.mVisualizerSpectrumNum);
			AirtalkeeAccount.getInstance().AirTalkeePowerManagerRun(this);
			// AirtalkeeAccount.getInstance().AirTalkeeTimerApiRun(this);
			AirtalkeeAccount.getInstance().AirTalkeeConfig(this, Config.serverAddress, 4001);
			AirtalkeeAccount.getInstance().AirTalkeeConfigMarketCode(Config.marketCode);
			if (Config.SUB_PLATFORM_VALID)
				AirtalkeeAccount.getInstance().AirTalkeeConfigSubServer(Config.SUB_PLATFORM_ADDRESS_DM, Config.SUB_PLATFORM_ADDRESS_WEB, Config.SUB_PLATFORM_ADDRESS_NOTICE);
			AirtalkeeAccount.getInstance().dbProxySet(db_proxy);
			AirtalkeeSessionManager.getInstance().setMediaEngineSetting(Setting.getPttHeartbeat(), Config.engineMediaSettingHbPackSize);
			AirtalkeeSessionManager.getInstance().setOnSessionIncomingListener(this);
			AirtalkeeSessionManager.getInstance().setSessionDialogSetAnswerMode(Setting.getPttAnswerMode() ? AirSession.INCOMING_MODE_AUTO : AirSession.INCOMING_MODE_MANUALLY);
			AirtalkeeSessionManager.getInstance().setSessionDialogSetIsbMode(Setting.getPttIsb());
			AirtalkeeSessionManager.getInstance().setOnMediaSoundListener(new AirSessionMediaSound(this));
			AirtalkeeChannel.getInstance().setOnChannelAlertListener(this);
			AccountController.setAccountInfoAutoLoad(true);
			AccountController.setAccountInfoOfflineMsgLoad(true);

			AirtalkeeSessionManager.getInstance().setAudioAmplifier(Setting.getVoiceAmplifier());
			if (!Environment.MEDIA_REMOVED.equals(Environment.getExternalStorageState()))
			{
				AirtalkeeSessionManager.getInstance().MediaRealtimeRecordEnable();
			}
			AirtalkeeMessage.getInstance().setMessageListNumberMax(10);

			String userId = iOperator.getString(AirAccountManager.KEY_ID, "");
			String userPwd = iOperator.getString(AirAccountManager.KEY_PWD, "");
			boolean userHb = iOperator.getBoolean(AirAccountManager.KEY_HB, false);
			AirtalkeeAccount.getInstance().loginAutoBoot(userId, userPwd, userHb);
			AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
			am.setMode(AudioManager.MODE_NORMAL);
			am.setSpeakerphoneOn(true);
			/*
			for (int i = 0; i < 70; i++)
			{
				Thread t = new Thread(new Runnable()
				{
					public void run()
					{
						double n = 0;
						while (true)
						{
							try
							{
								n = Math.random() * Math.random();
								n *= n ;
								Thread.sleep(1);
							}
							catch (InterruptedException e)
							{
								e.printStackTrace();
							}
						}
					}
				});
				t.start();
			}*/
		}
		catch (Exception e)
		{
			Log.e(AirServices.class, "AirServices run Exception!");
		}
	}

	public DBProxy dbProxy()
	{
		return db_proxy;
	}

	public void registerScreenReceiver()
	{
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		receiverScreen = new ReceiverScreenOff();
		registerReceiver(receiverScreen, filter);
	}

	@SuppressWarnings("deprecation")
	public void lightScreen()
	{
		PowerManager powerManager = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
		WakeLock wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "");
		wakeLock.acquire(20 * 1000);
	}

	@SuppressWarnings("deprecation")
	public void unlockScreen()
	{
		if (mKeyguardLock == null || km == null)
		{
			km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
			mKeyguardLock = km.newKeyguardLock("SimpleTimer");
		}
		if (!isScreenOn)
		{
			mKeyguardLock.disableKeyguard();
			isScreenOn = true;
		}
	}

	@SuppressWarnings("deprecation")
	public void lockScreen()
	{
		if (mKeyguardLock == null || km == null)
		{
			km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
			mKeyguardLock = km.newKeyguardLock("SimpleTimer");
		}
		if (isScreenOn)
		{
			isScreenOn = false;
			mKeyguardLock.reenableKeyguard();
		}
	}

	/***********************************
	 * 
	 * PTT
	 * 
	 ***********************************/

	private boolean isCalling = false;

	@Override
	public void onSessionIncomingAlertStart(AirSession session, AirContact caller, boolean isAccepted)
	{
		// TODO Auto-generated method stub
		Log.i(AirServices.class, "onSessionIncomingAlertStart");

		if (ReceiverPhoneState.isPhoneCalling(this) || isCalling || isAlerting)
		{
			Log.i(AirServices.class, "onSessionIncomingAlertStart - SessionIncomingBusy (isCalling=" + isCalling + ") (isAlerting=" + isAlerting + ")");
			AirtalkeeSessionManager.getInstance().SessionIncomingBusy(session);
			AirtalkeeMessage.getInstance().MessageSystemGenerate(session, session.getCaller(), getString(R.string.talk_call_state_missed_call), true);
			return;
		}

		if (TempSessionActivity.getInstance() != null && TempSessionActivity.getInstance().isShowing && TempSessionActivity.getInstance().getSession() != null && TextUtils.equals(TempSessionActivity.getInstance().getSession().getSessionCode(), session.getSessionCode()))
		{
			AirtalkeeSessionManager.getInstance().SessionIncomingAccept(session);
			return;
		}

		AirtalkeeMessage.getInstance().MessageRecordPlayStop();
		lightScreen();
		unlockScreen();
		if (session != null)
		{
			final AirSession temAirSession = session;
			try
			{
				if (MainActivity.getInstance() != null && MainActivity.getInstance().viewLeft != null)
				{
					MainActivity.getInstance().viewLeft.refreshList();
				}
			}
			catch (Exception e)
			{
				// TODO: handle exception
			}

			if (Setting.getPttIsb())
			{
				AirtalkeeSessionManager.getInstance().SessionIncomingBusy(temAirSession);
				return;
			}
			else if (Setting.getPttAnswerMode())
			{
				AirtalkeeSessionManager.getInstance().SessionIncomingAccept(temAirSession);
				AirtalkeeMessage.getInstance().MessageSystemGenerate(temAirSession, getString(R.string.talk_call_state_incoming_call), false);
				try
				{
					if (TempSessionActivity.getInstance() != null && TempSessionActivity.getInstance().getSession() != null && !TextUtils.equals(TempSessionActivity.getInstance().getSession().getSessionCode(), temAirSession.getSessionCode()))
					{
						TempSessionActivity.getInstance().setSession(temAirSession);
					}
					if (temAirSession != null)
					{
						AirtalkeeSessionManager.getInstance().getSessionByCode(temAirSession.getSessionCode());
						HomeActivity.getInstance().onViewChanged(session.getSessionCode());
						HomeActivity.getInstance().panelCollapsed();
					}
//					Intent it = new Intent(AirServices.getInstance(), SessionDialogActivity.class);
//					it.putExtra("sessionCode", session.getSessionCode());
//					it.putExtra("type", AirServices.TEMP_SESSION_TYPE_INCOMING);
//					it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//					AirServices.getInstance().startActivity(it);

					// switchToSessionTemp(temAirSession.getSessionCode(),
					// TEMP_SESSION_TYPE_INCOMING, AirServices.getInstance());
				}
				catch (Exception e)
				{
					// TODO: handle exception
				}
				return;
			}
			else
			{
				Sound.playSound(Sound.PLAYER_INCOMING_RING, true, context);

				incomingDialog = new InCommingAlertDialog(context, temAirSession, caller);
				incomingDialog.show();
			}
		}
		// if (caller != null)
		// {
		// builder.setMessage(caller.getDisplayName() +
		// getString(R.string.talk_incoming));
		// }
		// builder.setCancelable(false);
		// builder.setOnKeyListener(new OnKeyListener()
		// {
		//
		// @Override
		// public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent
		// event)
		// {
		// // TODO Auto-generated method stub
		// switch (keyCode)
		// {
		// case KeyEvent.KEYCODE_CALL:
		// {
		// dialog.cancel();
		// Sound.stopSound(Sound.PLAYER_INCOMING_RING);
		// isCalling = false;
		// AirtalkeeSessionManager.getInstance().SessionIncomingAccept(temAirSession);
		// AirtalkeeMessage.getInstance().MessageSystemGenerate(temAirSession,
		// getString(R.string.talk_call_state_incoming_call), false);
		// try
		// {
		// if (TempSessionActivity.getInstance() != null &&
		// TempSessionActivity.getInstance().getSession() != null
		// &&
		// !TextUtils.equals(TempSessionActivity.getInstance().getSession().getSessionCode(),
		// temAirSession.getSessionCode()))
		// {
		// TempSessionActivity.getInstance().setSession(temAirSession);
		// }
		//
		// Intent it = new Intent(AirServices.getInstance(),
		// SessionDialogActivity.class);
		// it.putExtra("sessionCode", temAirSession.getSessionCode());
		// it.putExtra("type", AirServices.TEMP_SESSION_TYPE_INCOMING);
		// it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);;
		// AirServices.getInstance().startActivity(it);
		//
		// // switchToSessionTemp(temAirSession.getSessionCode(),
		// TEMP_SESSION_TYPE_INCOMING, AirServices.getInstance());
		// }
		// catch (Exception e)
		// {
		// // TODO: handle exception
		// }
		//
		// return true;
		// }
		// case KeyEvent.KEYCODE_ENDCALL:
		// {
		// dialog.cancel();
		// Sound.stopSound(Sound.PLAYER_INCOMING_RING);
		// isCalling = false;
		// AirtalkeeSessionManager.getInstance().SessionIncomingReject(temAirSession);
		// AirtalkeeMessage.getInstance().MessageSystemGenerate(temAirSession,
		// temAirSession.getCaller(),
		// getString(R.string.talk_call_state_rejected_call),
		// true);
		// try
		// {
		// if (MainActivity.getInstance() != null &&
		// MainActivity.getInstance().viewLeft != null)
		// {
		// MainActivity.getInstance().viewLeft.refreshList();
		// }
		// }
		// catch (Exception e)
		// {
		// // TODO: handle exception
		// }
		// return true;
		// }
		// case KeyEvent.KEYCODE_MENU:
		// case KeyEvent.KEYCODE_BACK:
		// {
		// if (Sound.soundIsPlaying(Sound.PLAYER_INCOMING_RING))
		// {
		// Sound.stopSound(Sound.PLAYER_INCOMING_RING);
		// }
		// return true;
		// }
		// }
		// return false;
		// }
		//
		// });
		//
		// builder.setPositiveButton(getString(R.string.talk_incoming_accept),
		// new DialogInterface.OnClickListener()
		// {
		// public void onClick(DialogInterface dialog, int whichButton)
		// {
		// try
		// {
		// dialog.cancel();
		// Sound.stopSound(Sound.PLAYER_INCOMING_RING);
		// AirtalkeeSessionManager.getInstance().SessionIncomingAccept(temAirSession);
		// AirtalkeeMessage.getInstance().MessageSystemGenerate(temAirSession,
		// getString(R.string.talk_call_state_incoming_call), false);
		// if (TempSessionActivity.getInstance() != null &&
		// TempSessionActivity.getInstance().getSession() != null
		// &&
		// !TextUtils.equals(TempSessionActivity.getInstance().getSession().getSessionCode(),
		// temAirSession.getSessionCode()))
		// {
		// TempSessionActivity.getInstance().setSession(temAirSession);
		// }
		//
		// Intent it = new Intent(AirServices.getInstance(),
		// SessionDialogActivity.class);
		// it.putExtra("sessionCode", temAirSession.getSessionCode());
		// it.putExtra("type", AirServices.TEMP_SESSION_TYPE_INCOMING);
		// it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		// AirServices.getInstance().startActivity(it);
		// // switchToSessionTemp(temAirSession.getSessionCode(),
		// TEMP_SESSION_TYPE_INCOMING, AirServices.getInstance());
		// }
		// catch (Exception e)
		// {
		// // TODO: handle exception
		// }
		// isCalling = false;
		// }
		// });
		//
		// builder.setNegativeButton(getString(R.string.talk_incoming_reject),
		// new DialogInterface.OnClickListener()
		// {
		// public void onClick(DialogInterface dialog, int whichButton)
		// {
		// try
		// {
		// dialog.cancel();
		// Sound.stopSound(Sound.PLAYER_INCOMING_RING);
		// AirtalkeeSessionManager.getInstance().SessionIncomingReject(temAirSession);
		// AirtalkeeMessage.getInstance().MessageSystemGenerate(temAirSession,
		// temAirSession.getCaller(),
		// getString(R.string.talk_call_state_rejected_call), true);
		// if (MainActivity.getInstance() != null &&
		// MainActivity.getInstance().viewLeft != null)
		// {
		// MainActivity.getInstance().viewLeft.refreshList();
		// }
		// }
		// catch (Exception e)
		// {
		// // TODO: handle exception
		// }
		// isCalling = false;
		// }
		// });
		// incomingDialog = builder.create();
		// incomingDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		// incomingDialog.show();
		// }

		// isCalling = true;
		// }

	}

	@Override
	public void onSessionIncomingAlertStop(AirSession session)
	{
		// TODO Auto-generated method stub
		isCalling = false;
		Sound.stopSound(Sound.PLAYER_INCOMING_RING);
		if (session != null && !session.isCallHandled())
		{
			AirtalkeeMessage.getInstance().MessageSystemGenerate(session, session.getCaller(), getString(R.string.talk_call_state_missed_call), true);
			try
			{
				if (MainActivity.getInstance() != null && MainActivity.getInstance().viewLeft != null)
				{
					MainActivity.getInstance().viewLeft.refreshList();
				}
			}
			catch (Exception e)
			{
				// TODO: handle exception
			}
		}
		if (incomingDialog != null)
		{
			incomingDialog.cancel();
		}
	}

	public static final int TEMP_SESSION_TYPE_OUTGOING = 0;
	public static final int TEMP_SESSION_TYPE_INCOMING = 1;
	public static final int TEMP_SESSION_TYPE_MESSAGE = 2;
	public static final int TEMP_SESSION_TYPE_RESUME = 10;

	public void switchToSessionTemp(String code, int type, Context ct)
	{
		Intent intent = new Intent();
		intent.setClass(ct, TempSessionActivity.class);
		intent.putExtra("sessionCode", code);
		intent.putExtra("type", type);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		ct.startActivity(intent);
	}

	/***********************************
	 * 
	 * 
	 ***********************************/

	public void versionCheck()
	{
		String lang = Language.getLocalLanguage(this);
		AirtalkeeVersionUpdate.getInstance().versionCheck(this, AirtalkeeAccount.getInstance().getUserId(), Config.marketCode, lang, Config.VERSION_PLATFORM, Config.VERSION_TYPE, Config.model, Config.VERSION_CODE);
	}

	@Override
	public void UserVersionUpdate(int versionFlag, String versionInfo, final String url)
	{
		if (versionFlag == 0)
			return;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.talk_verion_title);
		builder.setMessage(versionInfo);
		builder.setPositiveButton(getString(R.string.talk_verion_upeate), new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int whichButton)
			{
				try
				{
					dialog.cancel();
					DialogVersionUpdate update = new DialogVersionUpdate(MainActivity.getInstance(), url);
					update.show();
				}
				catch (Exception e)
				{
					// TODO: handle exception
				}
			}
		});
		if (versionFlag == 2)
		{
			builder.setCancelable(false);
		}
		else
		{
			builder.setNegativeButton(getString(R.string.talk_verion_cancel), new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int whichButton)
				{
					try
					{
						dialog.cancel();
					}
					catch (Exception e)
					{
						// TODO: handle exception
					}
				}
			});
		}
		Dialog d = builder.create();
		d.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		d.show();
	}

	/***********************************
	 * 
	 * Ƶ��������ʾ
	 * 
	 ***********************************/

	private Dialog alertDialog;
	private boolean isAlerting = false;

	@Override
	public void onChannelAlertIncomingStart(AirChannel channel, AirContact caller, boolean isAcceptCall)
	{
		// TODO Auto-generated method stub
		if (isCalling || ReceiverPhoneState.isPhoneCalling(this))
		{
			AirtalkeeChannel.getInstance().ChannelAlertIncomingClose(false);
		}
		else
		{
			lightScreen();
			unlockScreen();
			if (isAcceptCall)
			{
				AirSessionControl.getInstance().SessionChannelIn(channel.getId());
				AirtalkeeChannel.getInstance().ChannelAlertIncomingClose(true);
				Util.Toast(this, getString(R.string.talk_incoming_channel_alert_owner_tip));
				if (MainActivity.getInstance() != null)
				{
					if (MainActivity.getInstance().viewLeft != null)
						MainActivity.getInstance().viewLeft.refreshList();
					if (MainActivity.getInstance().viewMiddle != null)
					{
						MainActivity.getInstance().viewMiddle.refreshSession();
						MainActivity.getInstance().viewMiddle.refreshSessionMember();
					}
				}
			}
			else
			{
				final String channelId = channel.getId();
				isAlerting = true;
				Sound.playSound(Sound.PLAYER_INCOMING_RING, true, context);
				AlertDialog.Builder builder = new AlertDialog.Builder(this);

				String tip = caller.getDisplayName() + String.format(getString(R.string.talk_incoming_channel_alert_tip), channel.getDisplayName());
				builder.setMessage(tip);
				builder.setCancelable(false);

				builder.setPositiveButton(getString(R.string.talk_incoming_channel_alert_accept), new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int whichButton)
					{
						try
						{
							dialog.cancel();
							Sound.stopSound(Sound.PLAYER_INCOMING_RING);
							AirSessionControl.getInstance().SessionChannelIn(channelId);
							AirtalkeeChannel.getInstance().ChannelAlertIncomingClose(true);
							if (MainActivity.getInstance() != null)
							{
								if (MainActivity.getInstance().viewMiddle != null)
								{
									MainActivity.getInstance().viewMiddle.refreshSession();
									MainActivity.getInstance().viewMiddle.refreshSessionMember();
								}
							}
						}
						catch (Exception e)
						{
							// TODO: handle exception
						}
						isAlerting = false;
					}
				});

				builder.setNegativeButton(getString(R.string.talk_incoming_channel_alert_reject), new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int whichButton)
					{
						try
						{
							dialog.cancel();
							Sound.stopSound(Sound.PLAYER_INCOMING_RING);
							AirtalkeeChannel.getInstance().ChannelAlertIncomingClose(false);
						}
						catch (Exception e)
						{
							// TODO: handle exception
						}
						isAlerting = false;
					}
				});
				alertDialog = builder.create();
				alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
				alertDialog.show();
			}
		}
	}

	@Override
	public void onChannelAlertIncomingStop()
	{
		// TODO Auto-generated method stub
		Sound.stopSound(Sound.PLAYER_INCOMING_RING);
		isAlerting = false;
		if (alertDialog != null)
		{
			alertDialog.cancel();
		}
	}

	@Override
	public void onChannelAlertSent(boolean isOk)
	{
		// TODO Auto-generated method stub
		if (isOk)
			Util.Toast(this, getString(R.string.talk_incoming_channel_alert_sent_tip_ok));
		else
			Util.Toast(this, getString(R.string.talk_incoming_channel_alert_sent_tip_err));
	}

	private void initImageLoader()
	{
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext()).threadPriority(Thread.NORM_PRIORITY - 2).denyCacheImageMultipleSizesInMemory().discCacheFileNameGenerator(new Md5FileNameGenerator()).tasksProcessingOrder(QueueProcessingType.LIFO).writeDebugLogs().build();
		// Initialize ImageLoader with configuration.
		ImageLoader.getInstance().init(config);
	}

	private boolean isSecretValid = false;

	private void secretConfig()
	{
		if (Config.funcEncryption)
		{
			if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()))
			{
				String path = Environment.getExternalStorageDirectory() + "/SANSECIO.CARD";
				File file = new File(path);
				if (file.exists())
				{
					isSecretValid = true;
				}
			}
			if (!isSecretValid)
				Util.Toast(this, getString(R.string.talk_secret_nocard));

			// set Encryption flag
			AirEngine.serviceSecretSettingType(AirEngine.SECRET_TYPE_SOFT);
			AirEngine.serviceSecretSettingValid(isSecretValid);
			AirEngine.serviceSecretSettingValidEncrypt(Setting.getPttEncrypt());
		}
	}

	public boolean secretValid()
	{
		return isSecretValid;
	}

	public static void sendBroadcast(String action)
	{
		if (action != null && action.length() > 0)
		{
			Intent intent = new Intent();
			intent.setAction(action);
			if (AirServices.getInstance() != null)
				AirServices.getInstance().sendBroadcast(intent);
			Log.i(AirServices.class, "broadcast  " + action + "  send!!!");
		}
	}

	public static void setVoxStatus(boolean status)
	{
		if (Config.funcVoxSetting)
		{
			AudioManager vox = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
			vox.setParameters(status ? "dmo_state=vox_switch_on" : "dmo_state=vox_switch_off"); // 设置VOX开关。
		}
	}
}
