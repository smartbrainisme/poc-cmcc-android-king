package com.cmccpoc.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.airtalkee.sdk.util.Log;
import com.cmccpoc.services.AirServices;

public class ReceiverScreenOff extends BroadcastReceiver
{

	@Override
	public void onReceive(Context context, Intent intent)
	{
		// TODO Auto-generated method stub
		Log.e(ReceiverScreenOff.class, "ReceiverScreenOff");
		String action = intent.getAction();
		if (action.equals("android.intent.action.SCREEN_OFF"))
		{
			if (AirServices.getInstance() != null)
			{
				AirServices.getInstance().lockScreen();
			}
		}
	}

}
