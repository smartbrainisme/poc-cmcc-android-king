package com.cmccpoc.application;

import android.app.Application;
import android.content.res.Configuration;
import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.util.IOoperate;
import com.airtalkee.sdk.util.Log;
import com.cmccpoc.R;
import com.cmccpoc.Util.XmlModelReader;
import com.cmccpoc.config.Config;
import com.cmccpoc.services.PttKeyServices;

/**
 * 主程序类 处理第一次程序开启时需要启动或需要初始化的功能
 * @author Yao
 */
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
	
	/**
	 * 设置是否是第一次运行程序
	 * @param b
	 * @return
	 */
	public static boolean  setFirstLaunch(boolean b)
	{
		new IOoperate().putBoolean("firstLaunch", b);
		firstLaunch = b;
		return firstLaunch;
	}
	
	/**
	 * 返回是否是第一次运行程序
	 * @return
	 */
	public static boolean isFisrtLaunch()
	{
		return firstLaunch;
	}
	
	
}
