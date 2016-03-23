package com.cmccpoc.activity;

import android.content.Context;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.AirtalkeeUserInfo;
import com.airtalkee.sdk.entity.AirSession;
import com.cmccpoc.R;
import com.cmccpoc.config.Config;
import com.cmccpoc.control.AirMessageTransaction;
import com.cmccpoc.control.AirSessionControl;
import com.cmccpoc.listener.OnMmiSessionBoxRefreshListener;
import com.cmccpoc.widget.ViewController;

public class MainSessionView extends ViewController implements OnClickListener, OnMmiSessionBoxRefreshListener
{
	public MainSessionView(Context context)
	{
		super(context);
	}

	public SessionBox sessionBox;
	private MainActivity contextMain;

	public View layoutNetWorkTip;

	@Override
	public void onCreate()
	{
		doInitView();
	}


	public void onPause()
	{
		AirMessageTransaction.getInstance().setOnNoticeListener(null);
	}

	private void doInitView()
	{
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
				contextMain.viewControllerSlideView.transLeftShow();
				break;
			}
			default:
				break;
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
	}

	@Override
	public void onMmiSessionReleased(AirSession session)
	{
		// TODO Auto-generated method stub
	}

}