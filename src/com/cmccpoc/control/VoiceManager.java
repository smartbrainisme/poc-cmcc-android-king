package com.cmccpoc.control;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.widget.ImageView;
import com.airtalkee.sdk.util.IOoperate;
import com.airtalkee.sdk.util.Log;

@SuppressLint("InlinedApi")
@SuppressWarnings("unused")
public class VoiceManager
{

	private static final int STATE_CONNECTTING = 0;
	private static final int STATE_CONNECTED = 1;
	private static final int STATE_DISCONNECTTING = 2;
	private static final int STATE_DISCONNECTED = 3;


	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_TOAST = 5;
	public static final int MESSAGE_BLUETOOTH_CONNECT_TIMEOUT = 6;
	public static final int MESSAGE_BLUETOOTH_ENABLE_TIMEOUT = 7;
	public static final int MESSAGE_RECONNECT_BLUETOOTH = 8;
	public static final int MESSAGE_RECOERY_BLUETOOTH = 9000;

	public static final String DEVICE_NAME = "device_name";
	private static VoiceManager instance;
	private BluetoothAdapter btAdapter = null;
	private AudioManager am = null;
	private BluetoothHeadset btHeadset;
	private BroadcastReceiver receiverBtConnectState = null;
	private BroadcastReceiver receiverBtState = null;

	boolean isReStart = false;

	private IOoperate io;
	private boolean isCalling = false;;
	private Context context;
	private int mode = AudioManager.MODE_NORMAL;
	private int state = STATE_DISCONNECTED;

	private void setMode(int mode)
	{
		Log.d(VoiceManager.class, "voice: setMode mode=[" + mode + "]");
		this.mode = mode;
	}

	public int getMode()
	{
		return this.mode;
	}

	public interface OnBtScoChangeListener
	{
		public void onScoStateChange(int state);
	}

	public interface OnModeSetListener
	{
		public void onModeSet();
	}

	private VoiceManager()
	{
	}

	private VoiceManager(Context context, ImageView ivMode)
	{
		this.context = context;
		io = new IOoperate();
		btInit();
	}

	public static VoiceManager newInstance(Context context)
	{
		newInstance(context, null);
		return instance;
	}

	public static VoiceManager newInstance(Context context, ImageView ivMode)
	{
		if (instance == null)
		{
			instance = new VoiceManager(context, ivMode);
		}
		return instance;
	}

	public void setModeContext(ImageView ivMode, Context context)
	{
		this.context = context;
	}

	public static VoiceManager getInstance()
	{
		return instance;
	}

	public void release()
	{
		if (context != null)
		{
			try
			{
				if (receiverBtState != null)
					context.unregisterReceiver(receiverBtState);
				if (receiverBtConnectState != null)
					context.unregisterReceiver(receiverBtConnectState);
				if (btAdapter != null)
					btAdapter.closeProfileProxy(BluetoothProfile.HEADSET, btHeadset);
			}
			catch (Exception e)
			{

			}
		}
	}

	private void btInit()
	{
		Log.d(VoiceManager.class, "voice:  btInit");

		setMode(io.getInt("mode", AudioManager.MODE_NORMAL));
		btAdapter = BluetoothAdapter.getDefaultAdapter();
		if (btAdapter != null)
		{
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			{
				BluetoothProfile.ServiceListener mProfileListener = new BluetoothProfile.ServiceListener()
				{
					public void onServiceConnected(int profile, BluetoothProfile proxy)
					{
						Log.d(VoiceManager.class, "voice:  Headset onServiceConnected ");
						if (profile == BluetoothProfile.HEADSET)
						{
							btHeadset = (BluetoothHeadset) proxy;
						}
					}

					public void onServiceDisconnected(int profile)
					{
						Log.d(VoiceManager.class, "voice:  Headset onServiceDisconnected ");
						if (profile == BluetoothProfile.HEADSET)
						{
							btHeadset = null;
						}
					}
				};
				btAdapter.getProfileProxy(context, mProfileListener, BluetoothProfile.HEADSET);
			}

			am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		}
	}

	private void btDestory()
	{
		Log.d(VoiceManager.class, "voice:  btDestorying state =[" + state + "]");
		if (state != STATE_DISCONNECTED)
		{
			this.isReStart = false;
			this.isCalling = false;
			this.state = STATE_DISCONNECTED;
		}
		Log.d(VoiceManager.class, "voice:  btDestoryed state =[" + state + "]");
	}

	public void doChangeVoiceCall()
	{
		Log.d(VoiceManager.class, "voice:  doChangeVoiceCall");
		changeMode(AudioManager.MODE_IN_CALL);
	}

	public void doChangeSpeaker()
	{
		Log.d(VoiceManager.class, "voice:  doChangeSpeaker");
		changeMode(AudioManager.MODE_NORMAL);
	}

	private void changeMode(int mode)
	{
		Log.d(VoiceManager.class, "voice: changeMode mode=[" + mode + "]");
		setMode(mode);
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		// TODO Auto-generated method stub
	}


}
