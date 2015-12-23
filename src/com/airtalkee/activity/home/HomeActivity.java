package com.airtalkee.activity.home;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import com.airtalkee.R;
import com.airtalkee.Util.DensityUtil;
import com.airtalkee.activity.home.widget.MediaStatusBar;
import com.airtalkee.activity.home.widget.SessionAndChannelView;
import com.airtalkee.activity.home.widget.SessionAndChannelView.ViewChangeListener;
import com.airtalkee.activity.home.widget.StatusBarTitle;
import com.airtalkee.config.Config;
import com.airtalkee.control.AirSessionControl;
import com.airtalkee.sdk.AirtalkeeChannel;
import com.airtalkee.sdk.entity.AirChannel;
import com.airtalkee.sdk.entity.AirSession;
import com.airtalkee.widget.PageIndicator;
import com.airtalkee.widget.SlidingUpPanelLayout;
import com.airtalkee.widget.SlidingUpPanelLayout.PanelSlideListener;
import com.airtalkee.widget.SlidingUpPanelLayout.PanelState;

public class HomeActivity extends BaseActivity implements PanelSlideListener, OnPageChangeListener, ViewChangeListener
{
	private AirSession session;
	private PageFragmentAdapter adapter;
	private SlidingUpPanelLayout mLayout;
	private ImageView slidingBack;
	private SessionAndChannelView channelView;
	private LinearLayout contaner;
	private ImageView ivIMNew, ivIMPoint;
	private MediaStatusBar mediaStatusBar;
	private TextView networkTip;
	private static HomeActivity mInstance;
	protected final FragmentManager fm = getSupportFragmentManager();

	public static HomeActivity getInstance()
	{
		return mInstance;
	}

	@Override
	protected void onCreate(Bundle bundle)
	{
		// TODO Auto-generated method stub
		super.onCreate(bundle);
		mInstance = this;
		setContentView(R.layout.activity_home);

		setRequestedOrientation(Config.screenOrientation);
		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		mediaStatusBar = (MediaStatusBar) findViewById(R.id.media_status_function_bar);
		mediaStatusBar.init((StatusBarTitle) findViewById(R.id.media_status_title_bar), AirSessionControl.getInstance().getCurrentChannelSession());
		ivIMNew = (ImageView) findViewById(R.id.iv_im_new);
		this.ivIMPoint = (ImageView) findViewById(R.id.iv_im_point);
		this.viewPager = (ViewPager) findViewById(R.id.home_activity_page_content);
		this.adapter = new PageFragmentAdapter(this, fm);
		this.viewPager.setAdapter(this.adapter);
		this.viewPager.setOnPageChangeListener(this);
		this.viewPager.setOffscreenPageLimit(TABS.length);
		this.mPageIndicator = (PageIndicator) findViewById(R.id.indicator);
		this.mPageIndicator.setViewPager(viewPager);
		this.networkTip = (TextView) findViewById(R.id.network_tip);

		DensityUtil.initScreen(this);
		int height = DensityUtil.getHeight(this) - DensityUtil.getStatusHeight(this) - 150;
		mLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
		mLayout.setParalaxOffset(height);
		mLayout.setPanelSlideListener(this);
		contaner = (LinearLayout) findViewById(R.id.sliding_layout_contaner);

		channelView = new SessionAndChannelView(this, this);
		contaner.addView(channelView);
		slidingBack = (ImageView) channelView.findViewById(R.id.sliding_back);
		session = AirSessionControl.getInstance().getCurrentChannelSession();
		if (null != session)
		{
			checkNewIM(false);
		}
	}

	@Override
	public void finish()
	{
		// TODO Auto-generated method stub
		super.finish();

	}

	// 滑动
	@Override
	public void onPanelSlide(View panel, float slideOffset)
	{
		Log.i("HOME_ACTIVITY", "onPanelSlide, offset " + slideOffset);
	}

	// 展开
	@Override
	public void onPanelExpanded(View panel)
	{
		Log.i("HOME_ACTIVITY", "onPanelExpanded");
		contaner.setBackgroundColor(0xff222222);
		slidingBack.setVisibility(View.VISIBLE);
		channelView.resume();
	}

	@Override
	protected void onPause()
	{
		// TODO Auto-generated method stub
		super.onPause();

	}

	// 收起
	@Override
	public void onPanelCollapsed(View panel)
	{
		panelCollapsed();
	}

	public void panelCollapsed()
	{
		Log.i("HOME_ACTIVITY", "onPanelCollapsed");
		contaner.setBackgroundColor(0x00000000);
		slidingBack.setVisibility(View.GONE);
		this.onPageSelected(pageIndex);
		if (mediaStatusBar != null)
			mediaStatusBar.setSession(AirSessionControl.getInstance().getCurrentChannelSession());
		// 解决刷新频道成员
		session = mediaStatusBar.getSession();
		MemberFragment memberFragment = (MemberFragment) adapter.getItem(0);
		memberFragment.refreshMembers(session, session.getChannel().MembersGet());
		// 检测是否有新im消息
		checkNewIM(false);
	}

	@Override
	protected void onResumeFragments()
	{
		// TODO Auto-generated method stub
		super.onResumeFragments();
		this.onPageSelected(pageIndex);
	}

	@Override
	public void onPageSelected(int page)
	{
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
		if (page == PAGE_IM)
		{
			checkNewIM(true);
			SessionAndChannelView.getInstance().refreshChannelAndDialog();
		}
		else {
		}
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm.isActive())
			imm.hideSoftInputFromWindow(mediaStatusBar.getBottomBarParent().getWindowToken(), 0);
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
	protected void onResume()
	{
		super.onResume();
		if (mediaStatusBar != null)
			mediaStatusBar.setSession(AirSessionControl.getInstance().getCurrentChannelSession());
	}

	@Override
	public void onPanelAnchored(View panel)
	{
		Log.i("HOME_ACTIVITY", "onPanelAnchored");
	}

	@Override
	public void onPanelHidden(View panel)
	{
		Log.i("HOME_ACTIVITY", "onPanelHidden");
	}

	@Override
	public void onViewChanged(String sessionCode)
	{
		// TODO Auto-generated method stub
		if (mLayout != null)
		{
			mLayout.setPanelState(PanelState.COLLAPSED);
		}
	}

	@Override
	@Deprecated
	protected Dialog onCreateDialog(final int id)
	{
		// TODO Auto-generated method stub
		switch (id)
		{
			case R.id.talk_dialog_message_txt_send_fail:
			case R.id.talk_dialog_message_txt:
			{
				final ListAdapter items = mSimpleAdapter(this, IMFragment.menuArray, R.layout.account_switch_listitem, R.id.AccountNameView);
				return new AlertDialog.Builder(this).setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int whichButton)
					{
						removeDialog(id);
						if (items instanceof SimpleAdapter)
						{
							getIMFragment().onListItemLongClick(id, whichButton);
						}
					}
				}).setOnCancelListener(new OnCancelListener()
				{
					@Override
					public void onCancel(DialogInterface dialog)
					{
						// TODO Auto-generated method stub
						removeDialog(id);
					}
				}).create();
			}
		}
		return super.onCreateDialog(id);
	}

	private BaseFragment getIMFragment()
	{
		if (adapter != null)
		{
			return adapter.getItem(PAGE_IM);
		}
		return null;
	}

	public SimpleAdapter mSimpleAdapter(Context contexts, String[] array, int layout, int id)
	{
		if (array == null)
			return null;
		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		data.clear();
		for (int i = 0; i < array.length; i++)
		{
			Map<String, Object> listItem = new HashMap<String, Object>();
			listItem.put("accountName", array[i]);
			data.add(listItem);
		}
		return new SimpleAdapter(this, data, layout, new String[] { "accountName" }, new int[] { id });
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

	public void checkNewIM(boolean toClean)
	{
		int count = 0;
		try
		{
			if (session != null)
			{
				int type = session.getType();
				if (type == AirSession.TYPE_CHANNEL)
				{
					AirChannel channel = AirtalkeeChannel.getInstance().ChannelGetByCode(session.getSessionCode());
					if (channel != null)
					{
						if (toClean)
						{
							channel.msgUnReadCountClean();
						}
						count = channel.getMsgUnReadCount();
					}
				}
				else if (type == AirSession.TYPE_DIALOG)
				{
					if (toClean)
					{
						session.setMessageUnreadCount(0);
					}
					count = session.getMessageUnreadCount();
				}
			}
			if (count > 0)
			{
				ivIMNew.setVisibility(View.VISIBLE);
				ivIMPoint.setVisibility(View.VISIBLE);
			}
			else
			{
				ivIMNew.setVisibility(View.GONE);
				ivIMPoint.setVisibility(View.GONE);
			}
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// 点击输入框外的地方隐藏输入法 目前不需要
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev)
	{
		if (ev.getAction() == MotionEvent.ACTION_DOWN)
		{
			View v = mediaStatusBar.getBottomBarParent();
			if (isShouldHideInput(v, ev))
			{
				InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
				if (imm != null && !imm.isActive())
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
}
