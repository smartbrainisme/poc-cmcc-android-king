package com.cmccpoc.Util;

import java.util.HashMap;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import com.airtalkee.sdk.engine.AirPower;
import com.airtalkee.sdk.util.Log;
import com.cmccpoc.services.AirServices;

public class AirMmiTimer extends BroadcastReceiver
{

	public class TimerItem
	{
		public int id = 0;
		public PendingIntent intent = null;
		public AirMmiTimerListener listener = null;
		public Object userData = null;
		public boolean isAlarm = false;
		public boolean isWakeup = false;
		public boolean isLoop = false;
		public int timeout = 0;

		public TimerItem()
		{}
	}

	private final static String TIMER_WAKEUP_NAME = "MMI-TIMER-";

	private static AirMmiTimer mInstance = new AirMmiTimer();

	public static AirMmiTimer getInstance()
	{
		return mInstance;
	}

	public AirMmiTimer()
	{}

	private final static String AIR_TIMER_ID = "TIMER_ID";
	private final static String AIR_TIMER_LOOP = "TIMER_LOOP";
	private final static String AIR_TIMER_FLAG = "TIMER_FLAG";
	private final static String AIR_TIMER_TIME = "TIMER_TIME";

	private static HashMap<AirMmiTimerListener, TimerItem> timerPoolByListener = new HashMap<AirMmiTimerListener, TimerItem>();
	private static HashMap<String, TimerItem> timerPoolById = new HashMap<String, TimerItem>();
	private static int timerFlag = 0;
	private static int timerIndex = 0;

	public void TimerRegister(Context context, AirMmiTimerListener listener, boolean isAlarm, boolean isWakeup, int timeout, boolean loop, Object userData)
	{
		if (listener != null)
		{
			TimerUnregister(context, listener);
			
			try
			{
				if (isAlarm)
				{
					if (timerFlag == 0)
					{
						timerFlag = (int) (System.currentTimeMillis() % 1000000);
						timerFlag += 1000000;
					}
					timerIndex++;
	
					AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
					Intent intent = new Intent(context, AirMmiTimer.class);
					intent.putExtra(AIR_TIMER_ID, timerIndex);
					intent.putExtra(AIR_TIMER_FLAG, timerFlag);
					intent.putExtra(AIR_TIMER_TIME, timeout);
					intent.putExtra(AIR_TIMER_LOOP, loop ? 1 : 0);
					PendingIntent pi = PendingIntent.getBroadcast(context, timerFlag + timerIndex, intent, loop ? PendingIntent.FLAG_UPDATE_CURRENT : PendingIntent.FLAG_ONE_SHOT);
					TimerItem item = new TimerItem();
					item.id = timerIndex;
					item.intent = pi;
					item.listener = listener;
					item.userData = userData;
					item.timeout = timeout;
					item.isLoop = loop;
					item.isAlarm = isAlarm;
					item.isWakeup = false; // If it's alarm, do not need to do
					// wakeup!!
					timerPoolByListener.put(listener, item);
					timerPoolById.put(timerIndex + "", item);
					if (loop)
					{
						am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + timeout, timeout, pi);
					}
					else
					{
						am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + timeout, pi);
					}
				}
				else
				{
					timerIndex++;
	
					TimerItem item = new TimerItem();
					item.id = timerIndex;
					item.intent = null;
					item.listener = listener;
					item.userData = userData;
					item.timeout = timeout;
					item.isLoop = loop;
					item.isAlarm = isAlarm;
					item.isWakeup = isWakeup;
					timerPoolByListener.put(listener, item);
					timerPoolById.put(timerIndex + "", item);
	
					if (isWakeup)
					{
						AirPower.PowerManagerWakeup(TIMER_WAKEUP_NAME + timerIndex);
					}
	
					Message msg = new Message();
					msg.what = timerIndex;
					msg.obj = item;
					timeOutHandler.sendMessageDelayed(msg, timeout);
				}
				Log.i(AirMmiTimer.class, "[MMI-TIMER][" + listener.toString() + "] TimerRegister id=" + timerIndex + " isAlarm=" + isAlarm + " flag=" + timerFlag + " timeout="
					+ timeout + " loop=" + loop);
			}
			catch (Exception e)
			{
			}
		}
	}

	public Object TimerUnregister(Context context, AirMmiTimerListener listener)
	{
		Object userObject = null;
		try
		{
			if (listener != null)
			{
				TimerItem item = timerPoolByListener.get(listener);
				if (item != null)
				{
					int id = item.id;
					boolean isWakeup = false;
					userObject = item.userData;
					if (item.isAlarm)
					{
						AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
						if (item.intent != null)
						{
							am.cancel(item.intent);
						}
					}
					else
					{
						isWakeup = item.isWakeup;
						timeOutHandler.removeMessages(item.id);
					}
					Log.i(AirMmiTimer.class, "[MMI-TIMER][" + listener.toString() + "] TimerUnregister id=" + id);
					timerPoolByListener.remove(listener);
					timerPoolById.remove(id + "");

					if (isWakeup)
					{
						AirPower.PowerManagerSleep(TIMER_WAKEUP_NAME + id);
					}
				}
			}
		}
		catch(Exception e)
		{
		}
		return userObject;
	}

	public Handler timeOutHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			super.handleMessage(msg);
			try
			{
				TimerItem item = (TimerItem) msg.obj;
				if (item != null)
				{
					if (timerPoolById.get(item.id + "") != null)
					{
						Log.i(AirMmiTimer.class, "[MMI-TIMER][" + item.listener.toString() + "] Timeout! id=" + item.id);
						item.listener.onMmiTimer(AirServices.getInstance(), item.userData);
						if (item.isLoop)
						{
							Message m = new Message();
							m.what = timerIndex;
							m.obj = item;
							timeOutHandler.sendMessageDelayed(m, item.timeout);
						}
						else
						{
							if (item.isWakeup)
							{
								AirPower.PowerManagerSleep(TIMER_WAKEUP_NAME + item.id);
							}
							timerPoolByListener.remove(item.listener);
							timerPoolById.remove(item.id + "");
						}
					}
				}
			}
			catch(Exception e)
			{
				
			}
		}
	};

	@Override
	public void onReceive(Context context, Intent intent)
	{
		// TODO Auto-generated method stub
		try
		{
			int id = intent.getExtras().getInt(AIR_TIMER_ID, 0);
			int loop = intent.getExtras().getInt(AIR_TIMER_LOOP, 0);
			int timeout = intent.getExtras().getInt(AIR_TIMER_TIME, 0);
			int flag = intent.getExtras().getInt(AIR_TIMER_FLAG);

			TimerItem item = timerPoolById.get(id + "");
			if (item != null && item.listener != null)
			{
				if (flag != timerFlag)
				{
					Log.i(AirMmiTimer.class, "[MMI-TIMER][" + item.listener.toString() + "][ERR] onReceive flag=" + flag + " timeout=" + timeout + " loop=" + (loop == 1));
					AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
					PendingIntent pi = PendingIntent.getBroadcast(context, flag + id, intent, PendingIntent.FLAG_CANCEL_CURRENT);
					am.cancel(pi);
				}
				else
				{
					Log.i(AirMmiTimer.class, "[MMI-TIMER][" + item.listener.toString() + "] onReceive timeout=" + timeout + " loop=" + (loop == 1));
					item.listener.onMmiTimer(context, item.userData);
					if (timerPoolById.get(id + "") != null)
					{
						if (loop == 1 && timeout > 0)
						{
							Log.i(AirMmiTimer.class, "[MMI-TIMER][" + item.listener.toString() + "] TimerLoopAgain flag=" + timerFlag + " timeout=" + timeout);
						}
						else
						{
							timerPoolByListener.remove(item.listener);
							timerPoolById.remove(item.id + "");
						}
					}
				}
			}
		}
		catch(Exception e)
		{
		}
		abortBroadcast();
	}

}
