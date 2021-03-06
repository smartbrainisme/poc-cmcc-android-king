package com.cmccpoc.activity.home;

import java.lang.reflect.Field;
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
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.AirtalkeeChannel;
import com.airtalkee.sdk.AirtalkeeMessage;
import com.airtalkee.sdk.AirtalkeeSessionManager;
import com.airtalkee.sdk.entity.AirChannel;
import com.airtalkee.sdk.entity.AirSession;
import com.cmccpoc.R;
import com.cmccpoc.Util.DensityUtil;
import com.cmccpoc.activity.home.widget.MediaStatusBar;
import com.cmccpoc.activity.home.widget.SessionAndChannelView;
import com.cmccpoc.activity.home.widget.StatusBarTitle;
import com.cmccpoc.activity.home.widget.SessionAndChannelView.ViewChangeListener;
import com.cmccpoc.config.Config;
import com.cmccpoc.control.AirSessionControl;
import com.cmccpoc.services.AirServices;
import com.cmccpoc.widget.PageIndicator;
import com.cmccpoc.widget.SlidingUpPanelLayout;
import com.cmccpoc.widget.SlidingUpPanelLayout.PanelSlideListener;
import com.cmccpoc.widget.SlidingUpPanelLayout.PanelState;

/**
 * 主界面 登录之后首先呈现出来的Activity，中间为PTT，左侧为频道成员，右侧为IM消息，顶部下来为频道与会话列表
 * @author Yao
 */
public class HomeActivity extends BaseActivity implements PanelSlideListener, OnPageChangeListener, ViewChangeListener
{
	private AirSession session;
	private PageFragmentAdapter adapter;
	public SlidingUpPanelLayout mLayout;
	private ImageView slidingBack;
	private SessionAndChannelView channelView;
	private LinearLayout contaner;
	private ImageView ivIMNew, ivIMPoint;
	private MediaStatusBar mediaStatusBar;
	private TextView networkTip;
	private boolean isChannel = true;

	private static HomeActivity mInstance;

	/**
	 * 获取HomeActivity实例
	 * @return
	 */
	public static HomeActivity getInstance()
	{
		return mInstance;
	}

	/**
	 * 获取当前会话
	 * @return 会话Entity
	 */
	public AirSession getSession()
	{
		return session;
	}

	/**
	 * 设置session会话
	 * @param session 会话Entity
	 */
	public void setSession(AirSession session)
	{
		this.session = session;
	}

	@Override
	protected void onCreate(Bundle bundle)
	{
		com.airtalkee.sdk.util.Log.i(HomeActivity.class, "HomeActivity onCreate");
		// TODO Auto-generated method stub
		super.onCreate(bundle);
		toMarquee();
		session = AirSessionControl.getInstance().getCurrentSession();
		mInstance = this;

		setContentView(R.layout.activity_home);
		setRequestedOrientation(Config.screenOrientation);
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		mediaStatusBar = (MediaStatusBar) findViewById(R.id.media_status_function_bar);
		mediaStatusBar.init((StatusBarTitle) findViewById(R.id.media_status_title_bar), session);
		ivIMNew = (ImageView) findViewById(R.id.iv_im_new);
		this.ivIMPoint = (ImageView) findViewById(R.id.iv_im_point);
		this.viewPager = (ViewPager) findViewById(R.id.home_activity_page_content);
		this.adapter = new PageFragmentAdapter(this, fm);
		this.viewPager.setAdapter(this.adapter);
		this.viewPager.setOnPageChangeListener(this);
		this.viewPager.setOffscreenPageLimit(3);
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
		if (null != session)
		{
			checkNewIM(false);
		}
	}

	/**
	 * 解决 部分机型的textView设置android:ellipsize="marquee"后 仍会显示省略号
	 */
	private void toMarquee()
	{
		ViewConfiguration configuration = ViewConfiguration.get(this);
		Class claz = configuration.getClass();
		try
		{
			Field field = claz.getDeclaredField("mFadingMarqueeEnabled");
			field.setAccessible(true);
			field.set(configuration, true);
		}
		catch (NoSuchFieldException e)
		{
			e.printStackTrace();
		}
		catch (IllegalArgumentException e)
		{
			e.printStackTrace();
		}
		catch (IllegalAccessException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void finish()
	{
		super.finish();
		if (!isChannel)
		{
			/*
			 * if (session != null && session.getSessionState() !=
			 * AirSession.SESSION_STATE_IDLE && session.getType() ==
			 * AirSession.TYPE_DIALOG) {
			 * AirSessionControl.getInstance().SessionEndCall(session); }
			 */
			AirtalkeeMessage.getInstance().MessageListMoreClean(session);
		}
		if (channelView != null)
		{
			channelView.unRegisterReceiver();
		}
	}

	/**
	 * 滑动
	 */
	@Override
	public void onPanelSlide(View panel, float slideOffset)
	{
		Log.i("HOME_ACTIVITY", "onPanelSlide, offset " + slideOffset);
		if (channelView.getVisibility() == View.GONE)
			channelView.setVisibility(View.VISIBLE);
	}

	/**
	 * 展开
	 */
	@Override
	public void onPanelExpanded(View panel)
	{
		Log.i("HOME_ACTIVITY", "onPanelExpanded");
		contaner.setBackgroundColor(0xff222222);
		slidingBack.setVisibility(View.VISIBLE);
		channelView.resume();
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm.isActive())
			imm.hideSoftInputFromWindow(mediaStatusBar.getBottomBarParent().getWindowToken(), 0);
		IMFragment.getInstance().textPannel.setVisibility(View.GONE);
	}

	@Override
	protected void onPause()
	{
		// TODO Auto-generated method stub
		super.onPause();
	}

	/**
	 * 收起
	 */
	@Override
	public void onPanelCollapsed(View panel)
	{
		panelCollapsed();
	}

	/**
	 * 收起
	 */
	public void panelCollapsed()
	{
		Log.i("HOME_ACTIVITY", "onPanelCollapsed");
		contaner.setBackgroundColor(0x00000000);
		slidingBack.setVisibility(View.GONE);
		if (AirSessionControl.getInstance().getCurrentChannelSession() != null)
		{
			if (mediaStatusBar != null)
				mediaStatusBar.setSession(session);
			// 解决刷新频道成员
			MemberFragment memberFragment = (MemberFragment) adapter.getItem(0);
			if (isChannel)
			{
				memberFragment.refreshMembers(session, session.getChannel().MembersGet());
			}
			else
			{
				memberFragment.refreshMembers(session, session.getMemberAll());
			}
			PTTFragment.getInstance().refreshPlayback(session);
			this.onPageSelected(pageIndex);
			adapter.notifyDataSetChanged();
			// 检测是否有新im消息
			checkNewIM(false);
			mediaStatusBar.setMediaStatusBarVisibility(View.VISIBLE);
		}
		else
		{
			session = null;
			mediaStatusBar.setSession(null);
			PTTFragment.getInstance().refreshPlayback(null);
			this.onPageSelected(pageIndex);
		}
	}

	@Override
	protected void onResumeFragments()
	{
		// TODO Auto-generated method stub
		super.onResumeFragments();
		this.onPageSelected(pageIndex);
	}

	/**
	 * 页面被选择时
	 */
	@Override
	public void onPageSelected(int page)
	{
		if (mediaStatusBar != null)
			mediaStatusBar.onPageChanged(page);
		if (mPageIndicator != null)
			mPageIndicator.onPageChanged(page);
		for (int i = 0; i < 3; i++)
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
			getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
			channelView.setVisibility(View.GONE);
			if (StatusBarTitle.getInstance() != null)
				StatusBarTitle.getInstance().refreshNewMsg();
			try
			{
				IMFragment.getInstance().setTextPannelVisiblity(View.GONE);
			}
			catch (Exception e)
			{}
		}
		else
		{
			getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
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
	public void onResume()
	{
		super.onResume();
		com.airtalkee.sdk.util.Log.i(HomeActivity.class, "HomeActivity onResume");
		Bundle bundle = getIntent().getExtras();
		if (bundle != null)
		{
			String sessionTag = bundle.getString("tag");
			if ("onMessageIncomingRecv".equals(sessionTag))
			{
				String sessionCode = bundle.getString("sessionCode");
				int sessionType = bundle.getInt("type", -1);
				if (sessionCode != null)
				{
					session = AirtalkeeSessionManager.getInstance().getSessionByCode(sessionCode);
				}
				else
				{
					session = AirSessionControl.getInstance().getCurrentSession();
				}
				if (sessionType == AirServices.TEMP_SESSION_TYPE_MESSAGE)
				{
					pageIndex = PAGE_IM;
					onPageSelected(pageIndex);
				}
			}
		}
		if (mediaStatusBar != null)
		{
			mediaStatusBar.setSession(session);
		}
		StatusBarTitle.getInstance().checkBrodcast();
		if (session != null)
		{
			if (session.getType() == AirSession.TYPE_CHANNEL)
			{
				AirChannel channel = session.getChannel();
				channel.setRoleAppling(true);
			}
		}
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
		isChannel = sessionCode.startsWith("C");
		if (isChannel)
		{
			session = AirSessionControl.getInstance().getCurrentChannelSession();
		}
		else
		{
			session = AirtalkeeSessionManager.getInstance().getSessionByCode(sessionCode);
		}
		// this.adapter = new PageFragmentAdapter(this, fm);
		// this.viewPager.setAdapter(this.adapter);
		// this.mPageIndicator.setViewPager(viewPager);
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

	/**
	 * 获取IM消息Fragment实例
	 * @return IMFragment实例
	 */
	private BaseFragment getIMFragment()
	{
		if (adapter != null)
		{
			return adapter.getItem(PAGE_IM);
		}
		return null;
	}

	/**
	 * 自定义适配器
	 * @param context 上下文
	 * @param array 数据
	 * @param layout layout
	 * @param id id
	 * @return
	 */
	public SimpleAdapter mSimpleAdapter(Context context, String[] array, int layout, int id)
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

	/**
	 * 内部类：页面片段适配器 
	 */
	class PageFragmentAdapter extends FragmentPagerAdapter
	{
		private List<BaseFragment> fragments = new ArrayList<BaseFragment>();

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

	/**
	 * 检测是否有新im消息
	 * @param toClean 是否清除所有未读消息
	 */
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

	@Override
	public boolean dispatchKeyEvent(KeyEvent event)
	{
		if (event.getKeyCode() == Config.pttButtonKeycode || (Config.pttVolumeKeySupport && (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP || event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN)))
		{
			if (AirtalkeeAccount.getInstance().isEngineRunning() && AirSessionControl.getInstance().getCurrentChannelSession() != null && AirSessionControl.getInstance().getCurrentChannelSession().getChannel() != null)
			{
				if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0)
				{
					AirtalkeeSessionManager.getInstance().TalkRequest(AirSessionControl.getInstance().getCurrentChannelSession(), AirSessionControl.getInstance().getCurrentChannelSession().getChannel().isRoleAppling());
				}
				else if (event.getAction() == KeyEvent.ACTION_UP)
				{
					AirtalkeeSessionManager.getInstance().TalkRelease(AirSessionControl.getInstance().getCurrentChannelSession());
				}
			}
			return true;
		}

		return super.dispatchKeyEvent(event);
	}

}
