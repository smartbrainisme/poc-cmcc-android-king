package com.airtalkee.activity.home;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.MotionEvent;
import android.view.View;
import com.airtalkee.config.Config;
import com.airtalkee.widget.PageIndicator;

public class BaseActivity extends FragmentActivity
{

	public static final int PAGE_MEMBER = 0;
	public static final int PAGE_PTT = 1;
	public static final int PAGE_IM = 2;

	protected static final Class<?>[] TABS = {
	/* 0 */MemberFragment.class,
	/* 1 */PTTFragment.class,
	/* 2 */IMFragment.class, };

	protected static final Class<?>[] TABS_ADD = {
	/* 0 */MemberTempFragment.class,
	/* 1 */PTTFragment.class,
	/* 2 */IMFragment.class, };

	protected ViewPager viewPager;
	protected PageIndicator mPageIndicator;
	
	protected int pageIndex = PAGE_PTT;
	protected int actionType;
	

	@Override
	protected void onCreate(Bundle bundle)
	{
		// TODO Auto-generated method stub
		super.onCreate(bundle);
		setRequestedOrientation(Config.screenOrientation);
	}

	public boolean isShouldHideInput(View v, MotionEvent event)
	{
		if (v != null)
		{
			int[] leftTop = { 0, 0 };
			v.getLocationInWindow(leftTop);
			int left = leftTop[0];
			int top = leftTop[1];
			int bottom = top + v.getHeight();
			int right = left + v.getWidth();
			if (event.getX() > left && event.getX() < right && event.getY() > top && event.getY() < bottom)
			{
				return false;
			}
			else
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public void finish()
	{
		// TODO Auto-generated method stub
		super.finish();
	}

	

	// 点击输入框外的地方隐藏输入法 目前不需要
	/*
	@Override
	protect boolean dispatchTouchEvent(MotionEvent ev)
	{
		if (ev.getAction() == MotionEvent.ACTION_DOWN)
		{
			View v = mediaStatusBar.getBottomBarParent();
			if (isShouldHideInput(v, ev))
			{

				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				if (imm != null)
				{
					imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
				}
			}
			return super.dispatchTouchEvent(ev);
		}

		if (getWindow().superDispatchTouchEvent(ev))
		{
			return true;
		}
		return onTouchEvent(ev);
	}*/

}
