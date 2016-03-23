package com.cmccpoc.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import com.cmccpoc.control.AirSessionControl;
import com.cmccpoc.services.AirServices;

public class ReceiverNoScreenOper extends BroadcastReceiver
{
	public static final String ACTION_VOICE_DOWN = "com.dfl.voice.down";
	public static final String ACTION_VOICE_LONG_PRESS = "com.dfl.voice.longpress";
	public static final String ACTION_VOICE_UP = "com.dfl.voice.up";
	public static final String ACTION_KNOB_ADD = "com.dfl.knob.add";
	public static final String ACTION_KNOB_SUB = "com.dfl.knob.sub";

	private static ReceiverNoScreenOper instance;
	private IntentFilter intentFilter = null;

	public static ReceiverNoScreenOper getInstance()
	{
		if (instance == null)
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
		if (intent.getAction().equals(ACTION_VOICE_LONG_PRESS))
		{

			if (intentFilter == null)
				intentFilter = new IntentFilter();
			intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
			context.registerReceiver(instance, intentFilter);
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
	}

	public void receiveUnReigster()
	{
		if (AirServices.getInstance() != null)
			AirServices.getInstance().unregisterReceiver(instance);
	}
}
