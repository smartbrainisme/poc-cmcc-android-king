package com.airtalkee.activity;

import android.app.Activity;
import android.os.Bundle;
import com.airtalkee.Util.ThemeUtil;

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
	}
	
	@Override
	protected void onResume()
	{
		// TODO Auto-generated method stub
		super.onResume();
	}

}
