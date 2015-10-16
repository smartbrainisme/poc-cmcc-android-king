package com.airtalkee.activity;

import android.app.Activity;
import android.os.Bundle;

import com.airtalkee.Util.ThemeUtil;
import com.airtalkee.application.MainApplication;
import com.airtalkee.config.Config;
import com.umeng.analytics.MobclickAgent;

public class ActivityBase extends Activity
{
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		ThemeUtil.setTheme(this);
	}
	
	@Override
	protected void onPause()
	{
		// TODO Auto-generated method stub
		super.onPause();
		if (Config.funcStatisticUmeng && !MainApplication.isFisrtLaunch())
			MobclickAgent.onPause(this);
	}
	
	@Override
	protected void onResume()
	{
		// TODO Auto-generated method stub
		super.onResume();
		if (Config.funcStatisticUmeng && !MainApplication.isFisrtLaunch())
			MobclickAgent.onResume(this);
	}

}
