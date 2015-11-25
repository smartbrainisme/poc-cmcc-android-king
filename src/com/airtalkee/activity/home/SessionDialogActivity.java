package com.airtalkee.activity.home;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.MotionEvent;
import android.view.View;
import com.airtalkee.R;
import com.airtalkee.activity.home.widget.MediaStatusBar;
import com.airtalkee.activity.home.widget.StatusBarTitle;
import com.airtalkee.config.Config;
import com.airtalkee.control.AirSessionControl;
import com.airtalkee.sdk.AirtalkeeSessionManager;
import com.airtalkee.sdk.entity.AirSession;
import com.airtalkee.services.AirServices;
import com.airtalkee.widget.PageIndicator;

public class SessionDialogActivity extends FragmentActivity implements
		OnPageChangeListener
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
	protected PageFragmentAdapter adapter;
	protected PageIndicator mPageIndicator;
	protected int pageIndex = PAGE_PTT;

	protected MediaStatusBar mediaStatusBar;
	protected int actionType;

	private boolean addFlag = false;

	@Override
	protected void onCreate(Bundle bundle)
	{
		// TODO Auto-generated method stub
		super.onCreate(bundle);
		bundle = getIntent().getExtras();
		if (null == bundle)
		{
			// finish();
			return;
		}

		String sessionCode = bundle.getString("sessionCode");
		actionType = bundle.getInt("type");
		switch (actionType)
		{
			case AirServices.TEMP_SESSION_TYPE_RESUME:
			case AirServices.TEMP_SESSION_TYPE_MESSAGE:
				pageIndex = PAGE_IM;
				addFlag = true;
				break;
			case AirServices.TEMP_SESSION_TYPE_OUTGOING:
				addFlag = true;
				break;
			default:
				addFlag = false;
				break;
		}
		AirSession session = AirtalkeeSessionManager.getInstance().getSessionByCode(sessionCode);
		if (null == session)
		{
			// finish();
			return;
		}

		setContentView(R.layout.activity_session_dialog);
		setRequestedOrientation(Config.screenOrientation);
		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		mediaStatusBar = (MediaStatusBar) findViewById(R.id.media_status_function_bar);

		mediaStatusBar.init((StatusBarTitle) findViewById(R.id.media_status_title_bar), session);

		final FragmentManager fm = getSupportFragmentManager();
		this.viewPager = (ViewPager) findViewById(R.id.home_activity_page_content);
		this.adapter = new PageFragmentAdapter(this, fm);
		this.viewPager.setAdapter(this.adapter);
		this.viewPager.setOnPageChangeListener(this);
		this.viewPager.setOffscreenPageLimit(TABS.length);
		this.mPageIndicator = (PageIndicator) findViewById(R.id.indicator);
		this.mPageIndicator.setViewPager(viewPager);
	}

	@Override
	protected void onResumeFragments()
	{
		// TODO Auto-generated method stub
		super.onResumeFragments();
		this.onPageSelected(pageIndex);
	}

	@Override
	public void onPageScrollStateChanged(int arg0)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onPageSelected(int page)
	{
		// TODO Auto-generated method stub

		if (mediaStatusBar != null)
			mediaStatusBar.onPageChanged(page);
		if (mPageIndicator != null)
			mPageIndicator.onPageChanged(page);

		for (int i = 0; i < TABS.length; i++)
		{
			if (null != adapter)
				if (i == page)
				{
					adapter.getItem(i).onResume();
				}
				else
				{
					adapter.getItem(i).onPause();
				}
		}
		pageIndex = page;
		viewPager.setCurrentItem(pageIndex);
	}

	@Override
	protected void onResume()
	{
		// TODO Auto-generated method stub
		super.onResume();
	}

	final class PageFragmentAdapter extends FragmentPagerAdapter
	{
		private final List<BaseFragment> fragments = new ArrayList<BaseFragment>();

		public PageFragmentAdapter(Context ctx, FragmentManager fm)
		{
			super(fm);
			if (!addFlag)
			{
				for (int i = 0; i < TABS.length; i++)
				{
					this.fragments.add(BaseFragment.newInstantiate(ctx, TABS[i].getName(), mediaStatusBar));
				}
			}
			else
			{
				for (int i = 0; i < TABS_ADD.length; i++)
				{
					this.fragments.add(BaseFragment.newInstantiate(ctx, TABS_ADD[i].getName(), mediaStatusBar));
				}
			}
		}

		@Override
		public BaseFragment getItem(int position)
		{
			return this.fragments.get(position);
		}

		@Override
		public int getCount()
		{
			return this.fragments.size();
		}
	}

	/*
	 * 点击输入框外的地方隐藏输入法 目前不需要
	 * 
	 * @Override public boolean dispatchTouchEvent(MotionEvent ev) { if
	 * (ev.getAction() == MotionEvent.ACTION_DOWN) { View v =
	 * mediaStatusBar.getBottomBarParent(); if (isShouldHideInput(v, ev)) {
	 * 
	 * InputMethodManager imm = (InputMethodManager)
	 * getSystemService(Context.INPUT_METHOD_SERVICE); if (imm != null) {
	 * imm.hideSoftInputFromWindow(v.getWindowToken(), 0); } } return
	 * super.dispatchTouchEvent(ev); }
	 * 
	 * if (getWindow().superDispatchTouchEvent(ev)) { return true; } return
	 * onTouchEvent(ev); }
	 */

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
		AirSession session = mediaStatusBar.getSession();
		if (session != null && session.getSessionState() != AirSession.SESSION_STATE_IDLE && session.getType() == AirSession.TYPE_DIALOG)
		{
			AirSessionControl.getInstance().SessionEndCall(session);
		}
	}

	public BaseFragment getIMFragment()
	{
		if (adapter != null)
		{
			return adapter.getItem(PAGE_IM);
		}
		return null;
	}

}
