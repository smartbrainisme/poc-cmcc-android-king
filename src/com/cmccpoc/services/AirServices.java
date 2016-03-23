package com.cmccpoc.services;

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
import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.AirtalkeeChannel;
import com.airtalkee.sdk.AirtalkeeContact;
import com.airtalkee.sdk.AirtalkeeMediaVisualizer;
import com.airtalkee.sdk.AirtalkeeMessage;
import com.airtalkee.sdk.AirtalkeeSessionManager;
import com.airtalkee.sdk.AirtalkeeUserInfo;
import com.airtalkee.sdk.AirtalkeeUserRegister;
import com.airtalkee.sdk.AirtalkeeVersionUpdate;
import com.airtalkee.sdk.OnSessionIncomingListener;
import com.airtalkee.sdk.OnVersionUpdateListener;
import com.airtalkee.sdk.controller.AccountController;
import com.airtalkee.sdk.entity.AirContact;
import com.airtalkee.sdk.entity.AirSession;
import com.airtalkee.sdk.entity.DBProxy;
import com.airtalkee.sdk.util.IOoperate;
import com.airtalkee.sdk.util.Log;
import com.cmccpoc.R;
import com.cmccpoc.Util.AirMmiTimer;
import com.cmccpoc.Util.Language;
import com.cmccpoc.Util.Setting;
import com.cmccpoc.Util.Sound;
import com.cmccpoc.Util.SoundPlayer;
import com.cmccpoc.Util.Util;
import com.cmccpoc.activity.DialogVersionUpdate;
import com.cmccpoc.activity.home.HomeActivity;
import com.cmccpoc.activity.home.widget.InCommingAlertDialog;
import com.cmccpoc.application.MainApplication;
import com.cmccpoc.config.Config;
import com.cmccpoc.control.AirAccountManager;
import com.cmccpoc.control.AirMessageTransaction;
import com.cmccpoc.control.AirReportManager;
import com.cmccpoc.control.AirSessionControl;
import com.cmccpoc.control.AirSessionMediaSound;
import com.cmccpoc.control.VoiceManager;
import com.cmccpoc.dao.DBHelp;
import com.cmccpoc.receiver.ReceiverConnectionChange;
import com.cmccpoc.receiver.ReceiverPhoneState;
import com.cmccpoc.receiver.ReceiverScreenOff;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.umeng.analytics.MobclickAgent;

public class AirServices extends Service implements OnSessionIncomingListener, OnVersionUpdateListener
{
	@SuppressWarnings("deprecation")
	private KeyguardManager.KeyguardLock mKeyguardLock;
	private KeyguardManager km;
	public static final String SERVICE_PATH = "com.cmccpoc.services.AirServices";
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
		if (VoiceManager.getInstance() != null)
			VoiceManager.getInstance().release();

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
			VoiceManager.newInstance(this);
			MobclickAgent.updateOnlineConfig(this);
			initImageLoader();
			SoundPlayer.soundInit(this);
			Setting.getPttClickSupport();
			Setting.getPttVolumeSupport();

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

			AirtalkeeMediaVisualizer.getInstance().setMediaAudioVisualizerValid(true, true);
			AirtalkeeMediaVisualizer.getInstance().setMediaAudioVisualizerSpectrumNumber(18);
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

		if (ReceiverPhoneState.isPhoneCalling(this) || isCalling)
		{
			Log.i(AirServices.class, "onSessionIncomingAlertStart - SessionIncomingBusy (isCalling=" + isCalling + ") ");
			AirtalkeeSessionManager.getInstance().SessionIncomingBusy(session);
			AirtalkeeMessage.getInstance().MessageSystemGenerate(session, session.getCaller(), getString(R.string.talk_call_state_missed_call), true);
			return;
		}

		AirtalkeeMessage.getInstance().MessageRecordPlayStop();
		lightScreen();
		unlockScreen();
		if (session != null)
		{
			final AirSession temAirSession = session;
//			try
//			{
//				if (MainActivity.getInstance() != null && MainActivity.getInstance().viewLeft != null)
//				{
//					MainActivity.getInstance().viewLeft.refreshList();
//				}
//			}
//			catch (Exception e)
//			{
//				// TODO: handle exception
//			}

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
					if (temAirSession != null)
					{
						AirtalkeeSessionManager.getInstance().getSessionByCode(temAirSession.getSessionCode());
						HomeActivity.getInstance().onViewChanged(session.getSessionCode());
						HomeActivity.getInstance().panelCollapsed();
					}
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
					DialogVersionUpdate update = new DialogVersionUpdate(HomeActivity.getInstance(), url);
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

	private void initImageLoader()
	{
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext()).threadPriority(Thread.NORM_PRIORITY - 2).denyCacheImageMultipleSizesInMemory().discCacheFileNameGenerator(new Md5FileNameGenerator()).tasksProcessingOrder(QueueProcessingType.LIFO).writeDebugLogs().build();
		// Initialize ImageLoader with configuration.
		ImageLoader.getInstance().init(config);
	}

	private boolean isSecretValid = false;

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
}
