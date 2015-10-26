package com.airtalkee.activity.home;

<<<<<<< HEAD
=======
import java.util.ArrayList;
import java.util.List;
import android.content.Context;
>>>>>>> 953be99d96ed385cb38d51dabe14e951b1681af7
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.airtalkee.R;
import com.airtalkee.Util.DensityUtil;
import com.airtalkee.activity.home.widget.MediaStatusBar;
import com.airtalkee.activity.home.widget.SessionAndChannelView;
import com.airtalkee.activity.home.widget.SessionAndChannelView.ViewChangeListener;
import com.airtalkee.activity.home.widget.StatusBarTitle;
import com.airtalkee.config.Config;
import com.airtalkee.control.AirSessionControl;
import com.airtalkee.widget.PageIndicator;
import com.airtalkee.widget.SlidingUpPanelLayout;
import com.airtalkee.widget.SlidingUpPanelLayout.PanelSlideListener;
import com.airtalkee.widget.SlidingUpPanelLayout.PanelState;

<<<<<<< HEAD
public class HomeActivity extends SessionDialogActivity implements PanelSlideListener,ViewChangeListener
{
	
=======
public class HomeActivity extends FragmentActivity implements
		OnPageChangeListener, PanelSlideListener, ViewChangeListener
{

	private String TAG = "HOME_ACTIVITY";

	public static final int PAGE_MEMBER = 0;
	public static final int PAGE_PTT = 1;
	public static final int PAGE_IM = 2;

	private static final Class<?>[] TABS = {
	/* 0 */MemberFragment.class,
	/* 1 */PTTFragment.class,
	/* 2 */IMFragment.class, };

	private ViewPager viewPager;
	private PageFragmentAdapter adapter;
	private PageIndicator mPageIndicator;
	private int pageIndex = PAGE_PTT;

	private MediaStatusBar mediaStatusBar;
>>>>>>> 953be99d96ed385cb38d51dabe14e951b1681af7
	private SlidingUpPanelLayout mLayout;
	private ImageView slidingBack;
	private SessionAndChannelView channelView;
	private LinearLayout contaner;

	@Override
	protected void onCreate(Bundle bundle)
	{
		// TODO Auto-generated method stub
		super.onCreate(bundle);
		setContentView(R.layout.activity_home);

		setRequestedOrientation(Config.screenOrientation);
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
<<<<<<< HEAD
		
		super.mediaStatusBar = (MediaStatusBar) findViewById(R.id.media_status_function_bar);
		mediaStatusBar.init((StatusBarTitle) findViewById(R.id.media_status_title_bar),
				AirSessionControl.getInstance().getCurrentChannelSession());
		
=======

		mediaStatusBar = (MediaStatusBar) findViewById(R.id.media_status_function_bar);
		mediaStatusBar.init((StatusBarTitle) findViewById(R.id.media_status_title_bar));

>>>>>>> 953be99d96ed385cb38d51dabe14e951b1681af7
		final FragmentManager fm = getSupportFragmentManager();
		this.viewPager = (ViewPager) findViewById(R.id.home_activity_page_content);  
		this.adapter = new PageFragmentAdapter(this, fm);
		this.viewPager.setAdapter(this.adapter);
		this.viewPager.setOnPageChangeListener(this);
		this.viewPager.setOffscreenPageLimit(TABS.length);
		this.mPageIndicator = (PageIndicator) findViewById(R.id.indicator);
		this.mPageIndicator.setViewPager(viewPager);

		DensityUtil.initScreen(this);
		int height = DensityUtil.getHeight(this) - DensityUtil.getStatusHeight(this) - 150;
		mLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
		mLayout.setParalaxOffset(height);
		mLayout.setPanelSlideListener(this);
		contaner = (LinearLayout) findViewById(R.id.sliding_layout_contaner);

		channelView = new SessionAndChannelView(this, this);
		contaner.addView(channelView);
		slidingBack = (ImageView) channelView.findViewById(R.id.sliding_back);
	}

	@Override
	public void onPanelSlide(View panel, float slideOffset)
	{
		Log.i(TAG, "onPanelSlide, offset " + slideOffset);
	}

	@Override
	public void onPanelExpanded(View panel)
	{
		Log.i(TAG, "onPanelExpanded");
		contaner.setBackgroundColor(0xff222222);
		slidingBack.setVisibility(View.VISIBLE);
		channelView.resume();
	}

	@Override
	public void onPanelCollapsed(View panel)
	{
		Log.i(TAG, "onPanelCollapsed");
		contaner.setBackgroundColor(0x00000000);
		slidingBack.setVisibility(View.GONE);
		this.onPageSelected(pageIndex);
	}

	@Override
	public void onPanelAnchored(View panel)
	{
		Log.i(TAG, "onPanelAnchored");
	}

	@Override
	public void onPanelHidden(View panel)
	{
		Log.i(TAG, "onPanelHidden");
	}
<<<<<<< HEAD
	
	 @Override
     public void onPanelSlide(View panel, float slideOffset) {
         Log.i("HOME_ACTIVITY", "onPanelSlide, offset " + slideOffset);
     }

     @Override
     public void onPanelExpanded(View panel) {
         Log.i("HOME_ACTIVITY", "onPanelExpanded");
         contaner.setBackgroundColor(0xff222222);
         slidingBack.setVisibility(View.VISIBLE);
         channelView.resume();
     }

     @Override
     public void onPanelCollapsed(View panel) {
         Log.i("HOME_ACTIVITY", "onPanelCollapsed");
         contaner.setBackgroundColor(0x00000000);
         slidingBack.setVisibility(View.GONE);
         this.onPageSelected(pageIndex);
     }

     @Override
     public void onPanelAnchored(View panel) {
         Log.i("HOME_ACTIVITY", "onPanelAnchored");
     }

     @Override
     public void onPanelHidden(View panel) {
         Log.i("HOME_ACTIVITY", "onPanelHidden");
     }
	
=======

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
			for (int i = 0; i < TABS.length; i++)
			{
				this.fragments.add(BaseFragment.newInstantiate(ctx, TABS[i].getName(), mediaStatusBar));
			}
		}

		@Override
		public Fragment getItem(int position)
		{
			return this.fragments.get(position);
		}

		@Override
		public int getCount()
		{
			return this.fragments.size();
		}
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev)
	{
		// TODO Auto-generated method stub
		if (ev.getAction() == MotionEvent.ACTION_DOWN)
		{
			// View v = getCurrentFocus();
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

>>>>>>> 953be99d96ed385cb38d51dabe14e951b1681af7
	@Override
	public void onViewChanged(String sessionCode)
	{
		// TODO Auto-generated method stub
		if (mLayout != null)
		{
			mLayout.setPanelState(PanelState.COLLAPSED);
		}
	}
}
