package com.airtalkee.receiver;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import com.airtalkee.Util.AirMmiTimer;
import com.airtalkee.Util.AirMmiTimerListener;
import com.airtalkee.control.VoiceManager;
import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.util.Log;
import com.airtalkee.services.AirServices;

public class ReceiverPhoneState extends BroadcastReceiver implements AirMmiTimerListener
{

	public static boolean isCalling = false;

	private static final int CALL_RETRY_TIMER = 10 * 1000;

	@Override
	public void onReceive(Context context, Intent intent)
	{
		// TODO Auto-generated method stub

		if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL) || intent.getAction().equals("android.intent.action.PHONE_STATE"))
		{
			isCalling = isPhoneCalling(context);
			Log.i(ReceiverPhoneState.class, "CALLSTATE>>>" + isCalling);
			if (isCalling)
			{
				callStart();
			}
			else
			{
				callStop();
			}
		}
	}

	public static boolean isPhoneCalling(Context context)
	{
		try
		{
			TelephonyManager tm = (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);
			switch (tm.getCallState())
			{
				case TelephonyManager.CALL_STATE_RINGING:
					Log.i(ReceiverPhoneState.class, "TELE>>>>>>>>>CALL_STATE_RINGING");
					isCalling = true;
					break;
				case TelephonyManager.CALL_STATE_OFFHOOK:
					Log.i(ReceiverPhoneState.class, "TELE>>>>>>>>>CALL_STATE_OFFHOOK");
					isCalling = true;
					break;

				case TelephonyManager.CALL_STATE_IDLE:
					Log.i(ReceiverPhoneState.class, "TELE>>>>>>>>>CALL_STATE_IDLE");
					isCalling = false;
					break;
				default:
					Log.i(ReceiverPhoneState.class, "TELE>>>>>>>>>CALL_STATE_DEFAULT");
					isCalling = false;
					break;
			}
		}
		catch (Exception e)
		{
			// TODO: handle exception
		}
		return isCalling;
	}

	private void callStart()
	{
		AirtalkeeAccount.getInstance().CallStart();
		AirMmiTimer.getInstance().TimerRegister(AirServices.getInstance(), this, true, false, CALL_RETRY_TIMER, true, null);
	}

	private void callStop()
	{
		AirMmiTimer.getInstance().TimerUnregister(AirServices.getInstance(), this);
		AirtalkeeAccount.getInstance().CallStop();
	}

	@Override
	public void onMmiTimer(Context context, Object userData)
	{
		// TODO Auto-generated method stub
		Log.i(ReceiverPhoneState.class, "TELE>>>>>>>>>CALL_STATE onMmiTimer");
		boolean isCalling = isPhoneCalling(AirServices.getInstance());
		if (!isCalling)
		{
			Log.i(ReceiverPhoneState.class, "TELE>>>>>>>>>CALL_STATE onMmiTimer DO!!!");
			callStop();
		}
	}
}
