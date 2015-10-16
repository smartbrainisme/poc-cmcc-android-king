package com.airtalkee.application;

import android.app.Application;
import android.content.res.Configuration;
import com.airtalkee.R;
import com.airtalkee.Util.XmlModelReader;
import com.airtalkee.config.Config;
import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.util.IOoperate;
import com.airtalkee.sdk.util.Log;
import com.airtalkee.services.PttKeyServices;

public class MainApplication extends Application
{
	private static boolean firstLaunch = true;
	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onCreate()
	{
		// TODO Auto-generated method stub
		super.onCreate();
		AirtalkeeAccount.getInstance().AirTalkeeConfigTrace(Config.TRACE_MODE);
		Log.d(MainApplication.class, "Weptt Application Start!");
		IOoperate.setContext(this.getApplicationContext());
		new XmlModelReader(this.getApplicationContext()).inflate(R.menu.ptt_config);
		Config.marketConfig(this.getApplicationContext());
		AppExcepiton.getInstance().init(this.getApplicationContext());
		PttKeyServices.startServices(this.getApplicationContext());
		firstLaunch = setFirstLaunch(new IOoperate().getBoolean("firstLaunch", true));
	}

	@Override
	public void onLowMemory()
	{
		// TODO Auto-generated method stub
		super.onLowMemory();
		Log.w(MainApplication.class, "-----------------------------------------------------------");
		Log.w(MainApplication.class, "   --------- Weptt Application --LowMemory----------");
		Log.w(MainApplication.class, "-----------------------------------------------------------");
	}

	@Override
	public void onTerminate()
	{
		// TODO Auto-generated method stub
		super.onTerminate();
		Log.e(MainApplication.class, "Weptt Application Stop!");
	}
	
	public static boolean  setFirstLaunch(boolean b)
	{
		new IOoperate().putBoolean("firstLaunch", b);
		firstLaunch = b;
		return firstLaunch;
	}
	
	public static boolean isFisrtLaunch()
	{
		return firstLaunch;
	}
	
	
}
