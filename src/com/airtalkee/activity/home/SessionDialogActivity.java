package com.airtalkee.activity.home;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import com.airtalkee.R;
import com.airtalkee.activity.home.widget.AlertDialog;
import com.airtalkee.activity.home.widget.MediaStatusBar;
import com.airtalkee.activity.home.widget.SessionAndChannelView;
import com.airtalkee.activity.home.widget.StatusBarTitle;
import com.airtalkee.config.Config;
import com.airtalkee.control.AirSessionControl;
import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.AirtalkeeChannel;
import com.airtalkee.sdk.AirtalkeeMessage;
import com.airtalkee.sdk.AirtalkeeSessionManager;
import com.airtalkee.sdk.entity.AirChannel;
import com.airtalkee.sdk.entity.AirSession;
import com.airtalkee.services.AirServices;
import com.airtalkee.widget.PageIndicator;

public class SessionDialogActivity extends BaseActivity implements OnPageChangeListener
{
	private AirSession session;
	private PageFragmentAdapter adapter;
	private ImageView ivIMNew, ivIMPoint;
	private MediaStatusBar mediaStatusBar;
	
	private boolean addFlag = false;
	
	private static SessionDialogActivity mInstance;
	public static SessionDialogActivity getInstance()
	{
		return mInstance;
	}

	@Override
	protected void onCreate(Bundle bundle)
	{
		// TODO Auto-generated method stub
		super.onCreate(bundle);
		mInstance = this;
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
			case AirServices.TEMP_SESSION_TYPE_INCOMING:
				addFlag = true;
				break;
			default:
				addFlag = false;
				break;
		}
		session = AirtalkeeSessionManager.getInstance().getSessionByCode(sessionCode);
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
		this.ivIMPoint = (ImageView) findViewById(R.id.iv_im_point);
		this.ivIMNew = (ImageView) findViewById(R.id.iv_im_new);
		this.viewPager = (ViewPager) findViewById(R.id.home_activity_page_content);
		this.adapter = new PageFragmentAdapter(this, fm);
		this.viewPager.setAdapter(this.adapter);
		this.viewPager.setOnPageChangeListener(this);
		this.viewPager.setOffscreenPageLimit(TABS.length);
		this.mPageIndicator = (PageIndicator) findViewById(R.id.indicator);
		this.mPageIndicator.setViewPager(viewPager);
		HomeActivity.getInstance().finish();
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
			getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
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

	@Override
	public void finish()
	{
		// TODO Auto-generated method stub
		super.finish();
		// AirSession session = mediaStatusBar.getSession();
		if (session != null && session.getSessionState() != AirSession.SESSION_STATE_IDLE && session.getType() == AirSession.TYPE_DIALOG)
		{
			AirSessionControl.getInstance().SessionEndCall(session);
		}
		AirtalkeeMessage.getInstance().MessageListMoreClean(session);
		Intent homeIntent = new Intent(this, HomeActivity.class);
		startActivity(homeIntent);
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
}
