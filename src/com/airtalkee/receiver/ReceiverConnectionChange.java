package com.airtalkee.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.airtalkee.Util.AirMmiTimer;
import com.airtalkee.Util.AirMmiTimerListener;
import com.airtalkee.activity.MainActivity;
import com.airtalkee.control.AirAccountManager;
import com.airtalkee.control.AirSessionMediaSound;
import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.util.Log;
import com.airtalkee.services.AirServices;

public class ReceiverConnectionChange extends BroadcastReceiver implements AirMmiTimerListener
{
	public static final String ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
	public static NetworkInfo activeNetInfo;
	private ConnectivityManager connectivityManager = null;
	private int ConnectionType = -1;
	private AirtalkeeAccount handleAccount = AirtalkeeAccount.getInstance();

	private static final int NETWORK_RETRY_TIMER = 10 * 1000;

	public void onReceive(Context context, Intent intent)
	{
		android.util.Log.e("m", "ReceiverConnectionChange  onReceive!!  Action = " + intent.getAction());
		if (intent.getAction().equals(ACTION))
		{
			if (connectivityManager == null)
				connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			activeNetInfo = connectivityManager.getActiveNetworkInfo();
			if (activeNetInfo != null)
			{
				if (activeNetInfo.getState() != NetworkInfo.State.CONNECTED)
				{
					Log.i(ReceiverConnectionChange.class, "ConnectionChangeReceiver  DISCONNECTED");
					networkClose();
				}
				else
				{
					Log.i(ReceiverConnectionChange.class, "ConnectionChangeReceiver  CONNECTED");
					if (ConnectionType != -1 && ConnectionType != activeNetInfo.getType())
					{
						Log.i(ReceiverConnectionChange.class, "ConnectionType changed!!!!");
						networkClose();
					}

					networkOpen();
					ConnectionType = activeNetInfo.getType();
				}
			}
			else
			{
				Log.i(ReceiverConnectionChange.class, "ConnectionChangeReceiver  activeNetInfo == null !!");
				networkClose();
			}
		}
	}

	private void networkOpen()
	{
		AirMmiTimer.getInstance().TimerUnregister(AirServices.getInstance(), this);
		String userId = AirServices.iOperator.getString(AirAccountManager.KEY_ID, "");
		String userPwd = AirServices.iOperator.getString(AirAccountManager.KEY_PWD, "");
		Log.e(ReceiverConnectionChange.class, String.format("userId =[%s],userPwd =[%s]", userId, userPwd));
		handleAccount.setUserIdAndPwd(userId, userPwd);
		handleAccount.NetworkOpen();
	
	}

	private void networkClose()
	{
		handleAccount.NetworkClose();
		AirMmiTimer.getInstance().TimerRegister(AirServices.getInstance(), this, true, false, NETWORK_RETRY_TIMER, true, null);
		try
		{
//			if (MainActivity.getInstance() != null)
//			{
//				MainActivity.getInstance().viewMiddle.sessionBox.sessionBoxTalk.videoFinish();
//			}
			AirSessionMediaSound.destoryState();
		}
		catch (Exception e)
		{
			// TODO: handle exception
		}
	}

	@Override
	public void onMmiTimer(Context context, Object userData)
	{
		// TODO Auto-generated method stub
		Log.i(ReceiverConnectionChange.class, "ConnectionChangeReceiver  onMmiTimer");
		if (connectivityManager == null)
			connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		activeNetInfo = connectivityManager.getActiveNetworkInfo();
		if (activeNetInfo != null && activeNetInfo.getState() == NetworkInfo.State.CONNECTED)
		{
			Log.i(ReceiverConnectionChange.class, "ConnectionChangeReceiver  onMmiTimer DO!!!");
			networkOpen();
		}
	}

}