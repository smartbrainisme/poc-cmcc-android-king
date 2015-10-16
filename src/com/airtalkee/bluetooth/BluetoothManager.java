package com.airtalkee.bluetooth;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.Toast;
import com.airtalkee.R;
import com.airtalkee.Util.ThemeUtil;
import com.airtalkee.Util.Util;
import com.airtalkee.bluetooth.BluetoothSupportDevices.KeyHolder;
import com.airtalkee.control.AirSessionControl;
import com.airtalkee.sdk.AirtalkeeSessionManager;
import com.airtalkee.sdk.util.IOoperate;
import com.airtalkee.sdk.util.Log;

@SuppressLint("InlinedApi")
public class BluetoothManager
{
	
	private static final int STATE_CONNECTTING =0;
	private static final int STATE_CONNECTED =1;
	private static final int STATE_DISCONNECTTING =2;
	private static final int STATE_DISCONNECTED =3;
	
	private static final int TRY_TIMES = 3;
	private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
	
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_TOAST = 5;
	public static final int MESSAGE_BLUETOOTH_CONNECT_TIMEOUT = 6;
	public static final int MESSAGE_BLUETOOTH_ENABLE_TIMEOUT = 7;
	public static final int MESSAGE_RECONNECT_BLUETOOTH = 8;
	public static final int MESSAGE_RECOERY_BLUETOOTH = 9000;
	
	public static final String DEVICE_NAME = "device_name";
	private static BluetoothManager instance;
	private BluetoothAdapter btAdapter = null;
	private BluetoothChatService btSPPService = null;
	private BluetoothDevice currentDevice = null;
	private AudioManager am = null;
	private BluetoothHeadset btHeadset;
	private BroadcastReceiver receiverBtConnectState = null;
	private BroadcastReceiver receiverBtState = null;
	private ScoStateReceiver receiverScoState = null;
	
	private int resetTimes = TRY_TIMES;
	boolean isReStart = false;

	private IOoperate io;
	private boolean isCalling = false;;
	private Context context;
	private ImageView ivMode;
	private String address ;
	private int mode = AudioManager.MODE_NORMAL;
	private int state = STATE_DISCONNECTED;
	private int currentMode = mode;
	private void setMode(int mode)
	{
		Log.d(BluetoothManager.class, "bluetooth: setMode mode=["+mode+"]");
		this.mode = mode;
		this.currentMode = this.mode;
	}private void setMode1(int mode)
	{
		Log.d(BluetoothManager.class, "bluetooth: setMode mode=["+mode+"]");
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

	private BluetoothManager()
	{}

	private BluetoothManager(Context context, ImageView ivMode)
	{
		this.context = context;
		io = new IOoperate();
		this.ivMode = ivMode;
		btInit();
	}
	

	public static BluetoothManager newInstance(Context context)
	{
		newInstance(context, null);
		return instance;
	}

	public static BluetoothManager newInstance(Context context, ImageView ivMode)
	{
		if (instance == null)
		{
			instance = new BluetoothManager(context, ivMode);
		}
		return instance;
	}

	public void setModeContext(ImageView ivMode, Context context)
	{
		this.ivMode = ivMode;
		this.context = context;
		if(am != null)
			refreshSpeakerIcon(am.getMode());
	}

	public static BluetoothManager getInstance()
	{
		return instance;
	}
	
	public void release()
	{
		if(context != null)
		{
			try
			{
				if(receiverBtState != null)
					context.unregisterReceiver(receiverBtState);
				if(receiverScoState != null)
					context.unregisterReceiver(receiverScoState);
				if(receiverBtConnectState != null)
					context.unregisterReceiver(receiverBtConnectState);
				if(btAdapter != null)
				btAdapter.closeProfileProxy(BluetoothProfile.HEADSET, btHeadset);
			}
			catch(Exception e)
			{
				
			}
		}
	}
	
	private void btInit()
	{
		Log.d(BluetoothManager.class, "bluetooth:  btInit"); 
		
		address = io.getString("address");
		setMode1( io.getInt("mode", AudioManager.MODE_NORMAL));
		btAdapter = BluetoothAdapter.getDefaultAdapter();
		if(btAdapter != null)
		{
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			{
				BluetoothProfile.ServiceListener mProfileListener = new BluetoothProfile.ServiceListener() 
				{
					public void onServiceConnected(int profile, BluetoothProfile proxy) 
					{
						Log.d(BluetoothManager.class, "bluetooth:  Headset onServiceConnected ");
					    if (profile == BluetoothProfile.HEADSET) 
					    {
					    	btHeadset = (BluetoothHeadset) proxy;
					    }
					}
					public void onServiceDisconnected(int profile) 
					{
						Log.d(BluetoothManager.class, "bluetooth:  Headset onServiceDisconnected ");
					    if (profile == BluetoothProfile.HEADSET) 
					    {
					    	btHeadset = null;
					    }
					}
				};
				btAdapter.getProfileProxy(context, mProfileListener, BluetoothProfile.HEADSET);
			}
			
			am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
			btSPPService = new BluetoothChatService(context, mHandler);
			BluetoothSupportDevices.getInstance().init(context);
			if(receiverBtState == null)
				receiverBtState = new BtStateReceiver();
			if(receiverScoState == null)
				receiverScoState = new ScoStateReceiver();
			if(receiverBtConnectState == null)
				receiverBtConnectState = new BtConnectStateReceiver();
			
			if(!receiverBtState.isOrderedBroadcast())
				context.registerReceiver(receiverBtState, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
			   
			if(!receiverScoState.isOrderedBroadcast())
				context.registerReceiver(receiverScoState, new IntentFilter(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED));
			
			if(!receiverBtConnectState.isOrderedBroadcast())
				context.registerReceiver(receiverBtConnectState, new IntentFilter(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED));
		}
	}
	
	private void btDestory()
	{
		Log.d(BluetoothManager.class, "bluetooth:  btDestorying state =["+state+"]");   
		if(state != STATE_DISCONNECTED)
		{
			this.isReStart = false;
			this.isCalling = false;
			this.state = STATE_DISCONNECTED;
			if(btSPPService != null)
				btSPPService.stop();
		}
		Log.d(BluetoothManager.class, "bluetooth:  btDestoryed state =["+state+"]");   
	}
	
	public void btStop()
	{
		Log.d(BluetoothManager.class, "bluetooth:  btStop state =["+state+"]"); 
		if(state != STATE_DISCONNECTED)
		{
			state = STATE_DISCONNECTTING;
			am.setBluetoothScoOn(false);
			am.stopBluetoothSco();
		}
	}
	
	private void btReset()
	{
		Log.d(BluetoothManager.class, "bluetooth:  btReset state =["+state+"]");
		if(this.state != STATE_CONNECTED)
		{
			setMode( AudioManager.MODE_NORMAL);
			handleSco();
		}
	}

	private void btDeviceConnect(String address)
	{
		// Get the BluetoothDevice object
		currentDevice = btAdapter.getRemoteDevice(address);
		this.address = address;
		if(currentDevice != null)
		{
			 KeyHolder holder = BluetoothSupportDevices.getInstance().getSupportDevice(currentDevice);
			int connectState = 0;
			if(btAdapter != null )
				connectState = btAdapter.getProfileConnectionState(BluetoothProfile.HEADSET);
			
			if(holder != null && holder.onlySpp)
			{
				Log.d(BluetoothManager.class, "bluetooth:  connect onlyspp device");
				sppConnect(currentDevice);
				return;
			}
			
			Log.d(BluetoothManager.class, "bluetooth:  btDeviceConnect address=["+address+"],connectState=["+ connectState+"]");
			if(connectState != BluetoothHeadset.STATE_CONNECTED)
			{
				btStateUpdate(true);
			}
			else
			{
				if (BluetoothSupportDevices.getInstance().isContainsDevice(currentDevice))
				{
					sppConnect(currentDevice);
				}
				else
				{
					Toast.makeText(context, R.string.bluetooth_connect_tip, Toast.LENGTH_LONG).show();
					Log.d(BluetoothManager.class, "bluetooth:  no support the device spp!");
					setMode( AudioManager.MODE_IN_COMMUNICATION);
					handleSco();
				}
			}
		}
		else
		{
			Log.d(BluetoothManager.class, "bluetooth:  btDeviceConnect currentDevice= null");
		}
		
	}
	
	private void sppConnect(BluetoothDevice device)
	{
		if (btSPPService != null && device != null)
		{
			if (btSPPService.getState() == BluetoothChatService.STATE_NONE)
			{
				Log.d(BluetoothManager.class, "bluetooth:  SPPService  to start");
				btSPPService.start();
			}
			Log.d(BluetoothManager.class, "bluetooth:  SPPService to connect");
			// Attempt to connect to the device and waiting to callback  handleSPP
			btSPPService.connect(device);
		}
	}
	
	private void btDeviceConnect()
	{
		Log.d(BluetoothManager.class, "bluetooth:  to connect currentDevice");
		if (currentDevice != null)
		{
			btDeviceConnect(currentDevice.getAddress());
		}
	}
	
	private boolean btEnable()
	{
		Log.d(BluetoothManager.class, "bluetooth:  btEnable mode=["+mode+"]");
		boolean isEnable = btAdapter.isEnabled();
		if (!isEnable)
		{
			mHandler.sendEmptyMessageDelayed(MESSAGE_BLUETOOTH_ENABLE_TIMEOUT, 2000);
			btAdapter.enable();
		}
		else
		{
			mHandler.removeMessages(MESSAGE_BLUETOOTH_ENABLE_TIMEOUT);
			
			if(currentMode != AudioManager.MODE_IN_COMMUNICATION && (BluetoothDeviceListActivity.instance == null || (BluetoothDeviceListActivity.instance != null && BluetoothDeviceListActivity.instance.isFinishing())))
			{
				Intent serverIntent = new Intent(context, BluetoothDeviceListActivity.class);
				((Activity) context).startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
			}
		}
		refreshSpeakerIcon();
		return isEnable;
	}

	public void doChangeBluetooth()
	{
		if (!am.isBluetoothScoAvailableOffCall())
		{
			Util.Toast(context, context.getString(R.string.bluetooth_no_support_record));
			return;
		}
		Log.d(BluetoothManager.class, "bluetooth:  doChangeBluetooth");
		btEnable();
	}

	public void doChangeVoiceCall()
	{
		Log.d(BluetoothManager.class, "bluetooth:  doChangeVoiceCall");
		changeMode(AudioManager.MODE_IN_CALL);
	}

	public void doChangeSpeaker()
	{
		Log.d(BluetoothManager.class, "bluetooth:  doChangeSpeaker");
		changeMode(AudioManager.MODE_NORMAL);    
	}

	private void changeMode(int mode)
	{
		Log.d(BluetoothManager.class, "bluetooth: changeMode mode=["+mode+"]");
		setMode( mode);
		handleSco();
	}
	
	public void callStart()
	{
		Log.d(BluetoothManager.class, "bluetooth: callStart isCalling =["+isCalling+"]");
		if (!isCalling)
		{
			btStop();
			btDestory();
			isCalling = true;
		}
	}
	
	public void callStop()
	{
		Log.d(BluetoothManager.class, "bluetooth: callStop  isCalling=["+isCalling+"]");
		
		if (isCalling)
		{
			isCalling = false;
			btStateRecoveryDelayed(3*1000);
		}
	}

	private void btReStart()
	{
		Log.d(BluetoothManager.class, "bluetooth: btReStart =["+isCalling+"]");
		if (!isReStart)
		{
			state = STATE_DISCONNECTTING;
			isReStart = true;
			btAdapter.disable();
		}
	}
	
	private void btStateSave(int mode)
	{
		io.putInt("mode", mode);
		if (currentDevice != null)
		{
			io.putString("address", currentDevice.getAddress());
			Log.d(BluetoothManager.class, "bluetooth:  btStateSave mode =["+mode+"], address =["+currentDevice.getAddress()+"]");
		}
	}

	private void btStateRecovery()
	{
		if(AirSessionControl.getInstance().getCurrentSession() == null)
		{
			Log.d(BluetoothManager.class, "bluetooth:  btStateRecovery break");
			return;
		}
		Log.d(BluetoothManager.class, "bluetooth:  btStateRecovery mode =["+mode+"], address =["+address+"],state=["+state+"]");
		if (mode == AudioManager.MODE_IN_COMMUNICATION && state == STATE_DISCONNECTED)
		{
			btDeviceConnect(address);
		}
	}

	public void btStateRecoveryDelayed(int delayTime)
	{
		Log.d(BluetoothManager.class, "bluetooth:  btStateRecoveryDelayed delayTime=["+delayTime+"]");
		mHandler.sendEmptyMessageDelayed(MESSAGE_RECOERY_BLUETOOTH, delayTime);
	}	
	
	public void btStateRecoveryDelayed()
	{
		btStateRecoveryDelayed(10*1000);
	}	

	private void refreshSpeakerIcon()
	{
		refreshSpeakerIcon(mode);
	}
	
	private void refreshSpeakerIcon(int mode)
	{
		if (ivMode == null)
			return;
		Log.d(BluetoothManager.class, "bluetooth:  refreshSpeakerIcon mode=["+mode+"]");
		boolean isEnable = true;
		switch(mode)	
		{
			case AudioManager.MODE_IN_CALL:
				ivMode.setImageResource(ThemeUtil.getResourceId(R.attr.theme_audio_phone, context));
				break;
			case AudioManager.MODE_IN_COMMUNICATION:
				ivMode.setImageResource(ThemeUtil.getResourceId(R.attr.theme_audio_bluetooth, context));
				break;
			default:
				ivMode.setImageResource(ThemeUtil.getResourceId(R.attr.theme_audio_speaker, context));
				break;
		}
		switch(state)
		{
			case STATE_CONNECTTING:
				isEnable = false;
				break;
			default :
				isEnable = true;
				break;
		}
		ivMode.setClickable(isEnable);
		ivMode.setEnabled(isEnable);
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		// TODO Auto-generated method stub
		Log.d(BluetoothManager.class, "bluetooth:  onActivityResult");
		switch (requestCode)
		{
			case REQUEST_CONNECT_DEVICE_SECURE:
				// When DeviceListActivity returns with a device to connect
				if (resultCode == Activity.RESULT_OK)
				{
					Log.d(BluetoothManager.class, "bluetooth:  onActivityResult btDeviceConnect");
					String address = data.getExtras().getString(BluetoothDeviceListActivity.EXTRA_DEVICE_ADDRESS);
					setMode(AudioManager.MODE_IN_COMMUNICATION);
					btDeviceConnect(address);
				}
				else if (resultCode == Activity.RESULT_CANCELED)
				{
					Log.d(BluetoothManager.class, "bluetooth:  onActivityResult btReset");
					btReset();
				}
				break;
		}
	}
	
	private void handleSPP(int state)
	{
		Log.d(BluetoothManager.class, "bluetooth: handleSPP state=" + state);
		switch (state)
		{
			case BluetoothChatService.STATE_CONNECTED:
			{
				Log.d(BluetoothManager.class, "bluetooth: handleSPP state= [STATE_CONNECTED] ");
				if(state != STATE_CONNECTED)
				{
					setMode(AudioManager.MODE_IN_COMMUNICATION);
					
					KeyHolder holder = BluetoothSupportDevices.getInstance().getSupportDevice(currentDevice);
					if (holder != null && holder.onlySpp)
					{
						refreshSpeakerIcon(currentMode);
						Util.Toast(context, context.getString(R.string.bluetooth_connected));
						return;
					}
					handleSco();
					isReStart = false;  
					resetTimes = TRY_TIMES;
					Util.Toast(context, context.getString(R.string.bluetooth_connectting));
				}
				break;
			}
			case BluetoothChatService.STATE_CONNECTING:
			{
				Log.d(BluetoothManager.class, "bluetooth: handleSPP state= [STATE_CONNECTING] ");
				Util.Toast(context, context.getString(R.string.bluetooth_connectting));
				break;
			}
			case BluetoothChatService.STATE_CONNECT_FAILED:
			{
				Log.d(BluetoothManager.class, "bluetooth: handleSPP state= [STATE_CONNECT_FAILED] ");
				btReStart();
				break;
			}
			case BluetoothChatService.STATE_DISCONNECT:
			{
				Log.d(BluetoothManager.class, "bluetooth:  handleSPP state= [STATE_DISCONNECT] ");
				if(BluetoothManager.this.state != STATE_DISCONNECTED && !isCalling)
				{
					btStop();
				}  
				break;
			}
		}
	}

	@SuppressWarnings("deprecation")
	private void handleSco()
	{
		Log.d(BluetoothManager.class, "bluetooth: handleSco mode =["+mode+"],state =["+state+"]");
		if(am == null) return;
		
		switch(mode)
		{
			case AudioManager.MODE_IN_COMMUNICATION:
			{
				state = STATE_CONNECTTING;
				if(!am.isBluetoothScoOn()) 
				{
					am.setBluetoothScoOn(true);
					am.startBluetoothSco();
				}
				break;
			}
			default:
			{
				state = STATE_DISCONNECTTING;
				if(mode == AudioManager.MODE_IN_CALL)
				{
					 am.setSpeakerphoneOn(false);//关闭扬声器
					 am.setRouting(AudioManager.MODE_NORMAL, AudioManager.ROUTE_EARPIECE, AudioManager.ROUTE_ALL);
			         am.setMode(AudioManager.MODE_IN_CALL);
				}
				am.setMode(mode);
				am.setBluetoothScoOn(false);
				am.stopBluetoothSco();
				btDestory();
				refreshSpeakerIcon();
				break;
			}
		}
		btStateSave(mode);
		//Wait ScoStateReceiver onReceive
	}
	
	class ScoStateReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			// TODO Auto-generated method stub
			Log.d(BluetoothManager.class, "bluetooth: ScoStateReceiver onReceive");
			currentMode = mode;
			
			boolean isClose = false;
			if((btAdapter != null && (btAdapter.getState() ==  BluetoothAdapter.STATE_OFF ||btAdapter.getState() ==  BluetoothAdapter.STATE_TURNING_OFF )) 
				|| (btHeadset != null && btHeadset.getConnectionState(currentDevice) != BluetoothHeadset.STATE_CONNECTED) || BluetoothManager.this.state == STATE_DISCONNECTTING)
			{
				isClose = true;
			}
			
			int state = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, -1);
			Log.d(BluetoothManager.class, "bluetooth: ScoStateReceiver state=[" + state+"] ,mode =["+BluetoothManager.this.mode+"],isClose =["+isClose+"] isCalling =["+isCalling+"] isScoOn=["+am.isBluetoothScoOn()+"]");
			switch(state)
			{
				case AudioManager.SCO_AUDIO_STATE_CONNECTED:
				{
					if(BluetoothManager.this.mode == AudioManager.MODE_IN_COMMUNICATION)
					{
						BluetoothManager.this.state = STATE_CONNECTED;
						mHandler.removeMessages(MESSAGE_BLUETOOTH_CONNECT_TIMEOUT);
						Util.Toast(context, context.getString(R.string.bluetooth_connected));
					}
					break;
				}
				case AudioManager.SCO_AUDIO_STATE_DISCONNECTED:
				{
					if(isClose || isCalling)
					{
						BluetoothManager.this.state = STATE_DISCONNECTTING;
						currentMode =  AudioManager.MODE_NORMAL;
						break;
					}
					else
						return;
				}
				default:
					return;
			}
			
			if(BluetoothManager.this.state == STATE_DISCONNECTTING)
			{
				btDestory();
			}
			am.setMode(currentMode);
			refreshSpeakerIcon(currentMode);
		}
	}
	
	class BtConnectStateReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			// TODO Auto-generated method stub
			int state = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, -1);
			Log.d(BluetoothManager.class, "bluetooth:  BtConnectStateReceiver onReceive state =["+state+"],btState=["+BluetoothManager.this.state+"]");
			switch(state)
			{
				case BluetoothAdapter.STATE_CONNECTED:         
					btStateRecoveryDelayed(5000);
					break;
				case BluetoothAdapter.STATE_DISCONNECTED:
					if(BluetoothManager.this.state != STATE_DISCONNECTED)
					{
						btStop();
					}
					break;
			}
		}
	}
	
	class BtStateReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			// TODO Auto-generated method stub
			Log.d(BluetoothManager.class, "bluetooth:  BtStateReceiver state =["+btAdapter.getState()+"],isReStart=["+isReStart+"]");
			switch (btAdapter.getState())
			{
				case BluetoothAdapter.STATE_ON:
				{
					if(isReStart)
					{
						if(resetTimes>0)
						{
							mHandler.sendEmptyMessageDelayed(MESSAGE_RECONNECT_BLUETOOTH, 3500);
							resetTimes --;
						}
						else
						{
							resetTimes = TRY_TIMES;
							isReStart = false;
							btReset();
						}
					}
					else
					{
						
					}
					break;
				}
				case BluetoothAdapter.STATE_OFF:
				{
					if(isReStart)
					{
						btAdapter.enable();
					}
					break;
				}
			}
		}
	}
	
	
	private void handlePttBtn(String key)
	{
		KeyHolder holder = BluetoothSupportDevices.getInstance().getSupportDevice(currentDevice);
		if (holder != null)
		{
			if (holder.keyP.equals(key))
			{
				AirtalkeeSessionManager.getInstance().TalkButtonClick(AirSessionControl.getInstance().getCurrentSession(), AirSessionControl.getInstance().getCurrentSessionGrap());
			}
			else if (holder.keyR.equals(key))
			{
				AirtalkeeSessionManager.getInstance().TalkRelease(AirSessionControl.getInstance().getCurrentSession());
			}
		}
	}
	
	public void ackSpeakState(boolean isTalk)
	{
		try
		{
			Log.d(BluetoothManager.class, "bluetooth:  ackSpeakState isTalk=["+isTalk+"]");
			if(currentDevice == null) return;
			KeyHolder holder = BluetoothSupportDevices.getInstance().getSupportDevice(currentDevice);
			if(holder != null && btSPPService != null)
			{
				if(!TextUtils.isEmpty(holder.ackSpeakTalk))
				{
					
					if(isTalk)
					{
						btSPPService.write(holder.ackSpeakTalk.getBytes());
						Thread.sleep(20);
					}
					else
					{
						btSPPService.write(holder.ackSpeakRelease.getBytes());
						Thread.sleep(20);
					}
				}
			}
		}
		catch(Exception e)
		{
			Log.e(BluetoothManager.class, "ackSpeakState error ="+e.toString());
		}
	}
	
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void  btStateUpdate(boolean connect)
	{
		Log.d(BluetoothManager.class, "bluetooth:  btStateUpdate connect = ["+connect+"]");
		try
		{
			Class cls = Class.forName(btHeadset.getClass().getName());
            Method meth = null;
            if(connect)
            {
            	meth = cls.getMethod("connect", new Class[]{BluetoothDevice.class});
            }
            else
            {
            	meth = cls.getMethod("disconnect", new Class[]{BluetoothDevice.class});
            }
            Object[] args1 = { currentDevice };
			meth.invoke(btHeadset, args1);
		}
		catch (ClassNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (NoSuchMethodException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IllegalAccessException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IllegalArgumentException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (InvocationTargetException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private final Handler mHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			Log.d(BluetoothManager.class, "bluetooth:   handleMessage what=[" + msg.what + "] arg1=[" + msg.arg1 + "]");
			try
			{
				switch (msg.what)
				{
					case MESSAGE_STATE_CHANGE:
					{
						// TODO Auto-generated method stub
						handleSPP(msg.arg1);
						break;
					}
					case MESSAGE_READ:
					{
						byte[] readBuf = (byte[]) msg.obj;
						String readMessage = null;
						KeyHolder holder = BluetoothSupportDevices.getInstance().getSupportDevice(currentDevice);
						if(holder != null)
						{
							Log.d(BluetoothManager.class, "bluetooth:   MESSAGE_READ msg ={"+readMessage+"}");
							if (holder.isEncrypt)
							{
								readMessage = ack(readBuf);
							}
							else
							{
								readMessage = new String(readBuf, 0, msg.arg1);
							}
							handlePttBtn(readMessage);
						}
						break;
					}
					case MESSAGE_BLUETOOTH_CONNECT_TIMEOUT:
					{
						Log.d(BluetoothManager.class, "bluetooth:   MESSAGE_BLUETOOTH_CONNECT_TIMEOUT");
						btReset();
						Util.Toast(context, context.getString(R.string.bluetooth_connect_error));
						break;
					}
					case MESSAGE_BLUETOOTH_ENABLE_TIMEOUT:
						btEnable();
						break;
					case MESSAGE_RECONNECT_BLUETOOTH:
						btDeviceConnect();
						break;
					case MESSAGE_RECOERY_BLUETOOTH:
						btStateRecovery();
						break;
				}
			}
			catch (Exception e)
			{
				// TODO: handle exception
			}
		}
	};

	///////////////////////////////////////////////////////  Decryption  /////////////////////////////////////////////////

	private static final int CMD_TIMEOUT_EVENT = 200;
	private static int CMD_TIMEOUT = 100;// mm
	private static final int STATE_WAIT_SIGN = 0;
	private static final int STATE_CMD = 1;
	private int cmd_state = STATE_WAIT_SIGN;
	byte C_CmdLen = 16;
	byte c_key_dw = 0;
	byte c_key_up = 1;
	byte c_key_error = 2;
	byte Cmd[] = new byte[16];
	byte Ack[] = new byte[16];
	public void registerCmdTimeOut()
	{
		cmd_state = STATE_CMD;
		mHandler.sendEmptyMessageDelayed(CMD_TIMEOUT_EVENT, CMD_TIMEOUT);
	}

	public void removeCmdTimeOut()
	{
		cmd_state = STATE_WAIT_SIGN;
		mHandler.removeMessages(CMD_TIMEOUT_EVENT);
	}

	
	public void cmd()
	{
		registerCmdTimeOut();
		for (int i = 0; i < Cmd.length; i++)
		{
			Cmd[i] = (byte) (Math.random() * 127);
		}
		btSPPService.write(Cmd);
	}

	
	public String ack(byte[] buff)
	{
		int ret = c_key_error;
		switch (cmd_state)
		{
			case STATE_WAIT_SIGN:
			{
				cmd();
				break;
			}
			case STATE_CMD:
			{
				removeCmdTimeOut();
				Ack = buff;
				ret = AppDecryption();
				break;
			}
		}
		return ret + "";
	}

	byte AppDecryption()
	{
		byte i, t, key;
		key = 0x55; // up
		for (i = 0; i < C_CmdLen; i++)
		{
			t = Ack[i];
			t -= key;
			t ^= 0xff;
			t &= 0xff;
			key++;
			if (t != Cmd[i])
				break;
		}
		if (i == C_CmdLen)
			return c_key_up; // key up, 
		key = (byte) 0xaa; // dw
		for (i = 0; i < C_CmdLen; i++)
		{
			t = Ack[i];
			t -= key;
			t ^= 0xff;
			t &= 0xff;
			key++;
			if (t != Cmd[i])
				break;
		}
		if (i == C_CmdLen)
			return c_key_dw; // key dw, 
		return c_key_error;
	}


}
