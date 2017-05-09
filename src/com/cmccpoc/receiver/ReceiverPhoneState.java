package com.cmccpoc.receiver;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.util.Log;
import com.cmccpoc.Util.AirMmiTimer;
import com.cmccpoc.Util.AirMmiTimerListener;
import com.cmccpoc.services.AirServices;

/**
 * 接收手机拨打、接听电话行为的广播
 * @author Yao
 */
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

	/**
	 * 是否正在通话中
	 */
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

	/**
	 * 开始通话时
	 */
	private void callStart()
	{
		AirtalkeeAccount.getInstance().CallStart();
		AirMmiTimer.getInstance().TimerRegister(AirServices.getInstance(), this, true, false, CALL_RETRY_TIMER, true, null);
	}

	/**
	 * 通话结束时
	 */
	private void callStop()
	{
		AirMmiTimer.getInstance().TimerUnregister(AirServices.getInstance(), this);
		AirtalkeeAccount.getInstance().CallStop();
	}

	/**
	 * 计时器检测通话状态
	 */
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
