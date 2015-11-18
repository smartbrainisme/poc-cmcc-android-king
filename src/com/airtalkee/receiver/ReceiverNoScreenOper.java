package com.airtalkee.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import com.airtalkee.config.Config;
import com.airtalkee.control.AirSessionControl;
import com.airtalkee.services.AirServices;
import com.airtalkee.tts.TTSManager;

public class ReceiverNoScreenOper extends BroadcastReceiver
{
	public static final String ACTION_VOICE_DOWN = "com.dfl.voice.down";
	public static final String ACTION_VOICE_LONG_PRESS = "com.dfl.voice.longpress";	
	public static final String ACTION_VOICE_UP = "com.dfl.voice.up";	
	public static final String ACTION_KNOB_ADD = "com.dfl.knob.add";
	public static final String ACTION_KNOB_SUB = "com.dfl.knob.sub";
	
	private static ReceiverNoScreenOper instance;
	private IntentFilter intentFilter = null;
	private boolean battery = false;
	public static ReceiverNoScreenOper getInstance()
	{
		if(instance == null)
		{
			instance = new ReceiverNoScreenOper();
		}
		return instance;
	}
	@Override
	public void onReceive(Context context, Intent intent)
	{
		// TODO Auto-generated method stub
		Log.e("m", "com.airtalkee.receiver onReceive");
		if (intent.getAction().equals(ACTION_VOICE_DOWN))
		{
			if(AirSessionControl.getInstance() != null && AirSessionControl.getInstance().getCurrentChannelSession() != null)
			{
				TTSManager.getInstance().synth(AirSessionControl.getInstance().getCurrentChannelSession().getDisplayName());
			}
			else
			{
				TTSManager.getInstance().synth("当前没有连接频道");
			}
		}
		else if (intent.getAction().equals(ACTION_VOICE_LONG_PRESS))
		{
			
			if(intentFilter == null)
				intentFilter = new IntentFilter();
			intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
			context.registerReceiver(instance,  intentFilter);
			battery = true;
		}
		else if (intent.getAction().equals(ACTION_VOICE_UP))
		{
			
		}
		else if (intent.getAction().equals(ACTION_KNOB_ADD))
		{
			AirSessionControl.getInstance().channelSelect(true);
		}
		else if (intent.getAction().equals(ACTION_KNOB_SUB))
		{
			AirSessionControl.getInstance().channelSelect(false);
		}
		else if (intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED))
		{
			if(battery)
			{
			 int level = intent.getIntExtra("level", 0);
		     int scale = intent.getIntExtra("scale", 100);
		     TTSManager.getInstance().synth("当前剩余电量"+ (level * 100 / scale) + "%");
			}
		}
	}
	
	public  void receiveReigster()
	{
		if(Config.funcTTS && AirServices.getInstance() != null)
		{
			if(intentFilter == null)
				intentFilter = new IntentFilter();
			
			intentFilter.addAction(ACTION_VOICE_DOWN);
			intentFilter.addAction(ACTION_VOICE_LONG_PRESS);
			intentFilter.addAction(ACTION_VOICE_UP);
			intentFilter.addAction(ACTION_KNOB_ADD);
			intentFilter.addAction(ACTION_KNOB_SUB);
			AirServices.getInstance().registerReceiver(instance, intentFilter);
		}
	}
	
	public  void receiveUnReigster()
	{
		if(AirServices.getInstance() != null)
			AirServices.getInstance().unregisterReceiver(instance);
	}
}
