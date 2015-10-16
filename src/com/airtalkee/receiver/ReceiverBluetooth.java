package com.airtalkee.receiver;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

public class ReceiverBluetooth extends BroadcastReceiver
{
	Context context;
	public static boolean isConnected = false;

	@Override
	public void onReceive(Context context, Intent intent)
	{
		// TODO Auto-generated method stub
		this.context = context;
		android.util.Log.e("m", "ReceiverBluetooth  action	" + intent.getAction());
		String action = intent.getAction();

		if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action) || BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action))
		{
			isConnected = false;
//			Util.setBlueToothMode(context, false);
		}
		else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action))
		{
			isConnected = true;
			mHandler.postDelayed(runnable, 10000);
		}
		abortBroadcast();
	}

	Handler mHandler = new Handler();

	Runnable runnable = new Runnable()
	{

		@Override
		public void run()
		{
			// TODO Auto-generated method stub
			if (isConnected)
			{
//				Util.setBlueToothMode(AirServices.getInstance(), true);
			}
		}

	};
}
