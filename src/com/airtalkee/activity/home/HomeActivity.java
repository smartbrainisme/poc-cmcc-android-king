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
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;

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

public class HomeActivity extends SessionDialogActivity implements
		PanelSlideListener, ViewChangeListener
{

	private SlidingUpPanelLayout mLayout;
	private ImageView slidingBack;
	private SessionAndChannelView channelView;
	private LinearLayout contaner;
	private static HomeActivity mInstance;

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
		
		super.mediaStatusBar = (MediaStatusBar) findViewById(R.id.media_status_function_bar);
		mediaStatusBar.init((StatusBarTitle) findViewById(R.id.media_status_title_bar), AirSessionControl.getInstance().getCurrentChannelSession());
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
		Log.i("HOME_ACTIVITY", "onPanelSlide, offset " + slideOffset);
	}

	@Override
	public void onPanelExpanded(View panel)
	{
		Log.i("HOME_ACTIVITY", "onPanelExpanded");
		contaner.setBackgroundColor(0xff222222);
		slidingBack.setVisibility(View.VISIBLE);
		channelView.resume();
	}

	@Override
	public void onPanelCollapsed(View panel)
	{
		Log.i("HOME_ACTIVITY", "onPanelCollapsed");
		contaner.setBackgroundColor(0x00000000);
		slidingBack.setVisibility(View.GONE);
		this.onPageSelected(pageIndex);
		if (mediaStatusBar != null)
			mediaStatusBar.setSession(AirSessionControl.getInstance().getCurrentChannelSession());
	}

	@Override
	protected void onResume()
	{
		// TODO Auto-generated method stub
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
	protected Dialog onCreateDialog(final int id) {
		// TODO Auto-generated method stub
		switch(id)
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
}
