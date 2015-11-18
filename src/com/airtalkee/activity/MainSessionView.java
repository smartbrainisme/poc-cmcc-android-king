package com.airtalkee.activity;

import android.content.Context;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import com.airtalkee.R;
import com.airtalkee.config.Config;
import com.airtalkee.control.AirMessageTransaction;
import com.airtalkee.control.AirSessionControl;
import com.airtalkee.listener.OnMmiSessionBoxRefreshListener;
import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.AirtalkeeUserInfo;
import com.airtalkee.sdk.entity.AirSession;
import com.airtalkee.widget.ViewController;

public class MainSessionView extends ViewController implements OnClickListener, OnMmiSessionBoxRefreshListener
{
	public MainSessionView(Context context)
	{
		super(context);
	}

	public SessionBox sessionBox;
	private MainActivity contextMain;

	private TextView tvNew;
	private TextView tvSessionName;
	public View layoutNetWorkTip;

	@Override
	public void onCreate()
	{
		View parentView = setContentView(R.layout.activity_talk);
		contextMain = (MainActivity) mContext;
		sessionBox = new SessionBox(contextMain, parentView, this, AirSession.TYPE_CHANNEL);
		doInitView();
	}

	public void onResume()
	{
		sessionBox.listenerEnable();
	}

	public void onPause()
	{
		sessionBox.listenerDisable();
		AirMessageTransaction.getInstance().setOnNoticeListener(null);
	}

	private void doInitView()
	{
		tvNew = (TextView) findViewById(R.id.tv_new);
		tvSessionName = (TextView) findViewById(R.id.tv_main_title);
		tvSessionName.setText(R.string.talk_group_no_connect);
		layoutNetWorkTip = findViewById(R.id.network_tip);
		
		ImageView ivLeft = (ImageView) findViewById(R.id.bottom_left_icon);
		ivLeft.setImageResource(R.drawable.ic_topbar_home);
		ImageView ivRight = (ImageView) findViewById(R.id.bottom_right_icon);
		ivRight.setImageResource(R.drawable.ic_topbar_lock_open);
		
		findViewById(R.id.menu_left_button).setOnClickListener(this);
	}

	@Override
	public void onClick(View v)
	{
		// TODO Auto-generated method stub
		switch (v.getId())
		{
			case R.id.menu_left_button:
			{
				contextMain.viewLeft.refreshList();
				contextMain.viewControllerSlideView.transLeftShow();
				break;
			}
			default:
				break;
		}
	}

	public void refreshSession()
	{
		refreshName();
		sessionBox.setSession(AirSessionControl.getInstance().getCurrentChannelSession());
	}

	public void refreshSessionMember()
	{
		sessionBox.sessionBoxMember.refreshMembers();
		sessionBox.sessionBoxMember.refreshMemberOnline();
	}

	public void refreshName()
	{
		if (Config.marketCode == Config.MARKET_BPER || Config.marketCode == Config.MARKET_BPER_GERMANY || Config.marketCode == Config.MARKET_BPER_SINGAPORE || Config.marketCode == Config.MARKET_BPER_MTT)
		{
			tvSessionName.setText(AirtalkeeUserInfo.getInstance().getUserInfo().getDisplayName());
		}
		else
		{
			if (AirSessionControl.getInstance().getCurrentChannelSession() != null)
			{
				tvSessionName.setText(AirSessionControl.getInstance().getCurrentChannelSession().getDisplayName());
			}
			else
			{
				tvSessionName.setText(R.string.talk_group_no_connect);
			}
		}
	}

	public void refreshNetState()
	{
		if (AirtalkeeAccount.getInstance().isEngineRunning())
		{
			layoutNetWorkTip.setVisibility(View.GONE);
		}
		else
		{
			layoutNetWorkTip.setVisibility(View.VISIBLE);
		}
	}

	public boolean refreshNewMsg()
	{
		boolean hasNew = false;
		int count = contextMain.viewLeft.countGroupNewMsg() + contextMain.viewLeft.countSessionNewMsg();
		hasNew = (count > 0);
		if (hasNew)
		{
			if (AirSessionControl.getInstance().getCurrentChannelSession() != null
				&& AirSessionControl.getInstance().getCurrentChannelSession().getChannel() != null
				&& count == AirSessionControl.getInstance().getCurrentChannelSession().getChannel().getMsgUnReadCount())
				tvNew.setVisibility(View.GONE);
			else
				tvNew.setVisibility(View.VISIBLE);
		}
		else
		{
			tvNew.setVisibility(View.GONE);
		}
		contextMain.viewLeft.refreshList();
		return hasNew;
	}

	public boolean onKeyEvent(KeyEvent event)
	{
		boolean isHandled = false;
		isHandled = sessionBox.onKeyEvent(event);
		return isHandled;
	}

	@Override
	public void onMmiSessionRefresh(AirSession session)
	{
		// TODO Auto-generated method stub
		refreshNetState();
	}

	@Override
	public void onMmiSessionEstablished(AirSession session)
	{
		// TODO Auto-generated method stub
		refreshNetState();
		contextMain.viewLeft.refreshList();
	}

	@Override
	public void onMmiSessionReleased(AirSession session)
	{
		// TODO Auto-generated method stub
		contextMain.viewLeft.refreshList();
	}

}