package com.cmccpoc.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.airtalkee.sdk.util.Log;
import com.cmccpoc.activity.AccountActivity;
import com.cmccpoc.config.Config;

/**
 * 广播接收器：启动poc服务
 * @author Yao
 */
public class ReceiverBoot extends BroadcastReceiver
{

	@Override
	public void onReceive(Context context, Intent intent)
	{
		// TODO Auto-generated method stub
		Log.d(ReceiverBoot.class, "ReceiverBoot onReceive");
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED))
		{
			if (Config.funcBootLaunch)
			{
				Log.d(ReceiverBoot.class, "AirServices start Activity");
				Intent it = new Intent(context, AccountActivity.class);
				it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(it);
			}
		}
	}
}
