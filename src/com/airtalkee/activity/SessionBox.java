package com.airtalkee.activity;

import android.app.Activity;
import android.content.Context;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import com.airtalkee.R;
import com.airtalkee.Util.Util;
import com.airtalkee.control.AirMessageTransaction;
import com.airtalkee.control.AirSessionControl;
import com.airtalkee.listener.OnMmiSessionBoxRefreshListener;
import com.airtalkee.sdk.AirtalkeeChannel;
import com.airtalkee.sdk.AirtalkeeContactPresence;
import com.airtalkee.sdk.AirtalkeeMediaVisualizer;
import com.airtalkee.sdk.AirtalkeeMessage;
import com.airtalkee.sdk.AirtalkeeSessionManager;
import com.airtalkee.sdk.entity.AirChannel;
import com.airtalkee.sdk.entity.AirSession;
import com.airtalkee.sdk.util.Log;
import com.airtalkee.services.AirServices;
import com.airtalkee.widget.PagerHorizontal;
import com.airtalkee.widget.PagerHorizontal.OnScrollListener;

public class SessionBox extends View implements OnClickListener, OnScrollListener
{

	private Activity contextMain;
	private AirSession session;
	private int sessionType = 0;

	public SessionBoxTalk sessionBoxTalk = null;
	public SessionBoxMessage sessionBoxMessage = null;
	public SessionBoxMember sessionBoxMember = null; 
	public static final int PAGE_MEMBER = 0;
	public static final int PAGE_PTT = 1;
	public static final int PAGE_MSG = 2;

	private boolean isMenuShowing = false;

	private PagerHorizontal pager;

	public SessionBox(Context context, View parentView, OnMmiSessionBoxRefreshListener listener, int session_type)
	{
		super(context, null);
		contextMain = (Activity) context;
		loadView(parentView);
		sessionType = session_type;
		sessionBoxTalk = new SessionBoxTalk(context, parentView, this, listener);
		sessionBoxMessage = new SessionBoxMessage(context, parentView, this);
		sessionBoxMember = new SessionBoxMember(context, parentView, this);
	}

	private void loadView(View parentView)
	{
		pager = (PagerHorizontal) parentView.findViewById(R.id.pager);
		pager.setCurrentPage(PAGE_PTT);
		pager.addOnScrollListener(this);
	}

	public void setSession(AirSession s)
	{
		Log.i(MainSessionView.class, "setSession");
		session = s;
	}

	public int getSessionType()
	{
		return sessionType;
	}

	public void setMenuShowing(boolean isMenuShowing)
	{
		this.isMenuShowing = isMenuShowing;
		pager.setPageSrcollAllow(!isMenuShowing);
	}

	public boolean isMenuShowing()
	{
		boolean showing = false;
		if (sessionType == AirSession.TYPE_CHANNEL)
		{
			showing = isMenuShowing;
		}
		return showing;
	}

	@Override
	public void onClick(View v)
	{
		// TODO Auto-generated method stub
		if (isMenuShowing())
		{
			return;
		}

		switch (v.getId())
		{
			case R.id.talk_label_member:
				tabSnapeToPage(PAGE_MEMBER);
				break;
			case R.id.talk_label_msg:
				tabSnapeToPage(PAGE_MSG);
				break;
			case R.id.talk_label_ptt_left:
				tabSnapeToPage(PAGE_PTT);
				break;
			case R.id.talk_label_ptt_right:
				tabSnapeToPage(PAGE_PTT);
				break;
			default:
				break;
		}
	}

	public boolean onKeyEvent(KeyEvent event)
	{
		boolean isHandled = false;
		if (sessionBoxTalk.onKeyEvent(event) || sessionBoxMessage.onKeyEvent(event) || sessionBoxMember.onKeyEvent(event))
		{
			isHandled = true;
		}
		return isHandled;
	}

	public void tabResetPage()
	{
		if (pager != null)
			pager.snapToPage(PAGE_PTT);
	}

	public int tabIndex()
	{
		return pager.getmCurrentPage();
	}

	public void tabSnapeToPage(int page)
	{
		pager.snapToPage(page);
	}

	public void tabJumpToPage(int page)
	{
		pager.setCurrentPage(page);
	}

	// ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// //////////////////////-----------------------------------------------------------------////////////////////////
	// //////////////////////------------------------ EVENT�¼�
	// --------------------------////////////////////////
	// //////////////////////-----------------------------------------------------------------////////////////////////
	// ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public void onScroll(int scrollX)
	{
	// TODO Auto-generated method stub
	}

	@Override
	public void onViewScrollFinished(int currentPage)
	{
		// TODO Auto-generated method stub
		if (currentPage == PAGE_MSG)
		{
			if (session != null)
			{
				if (session.getType() == AirSession.TYPE_DIALOG)
				{
					if (session.getMessageUnreadCount() > 0)
					{
						AirServices.getInstance().dbProxy().SessionDbCleanUnread(session.getSessionCode());
					}
					session.setMessageUnreadCount(0);
				}
				else if (session.getType() == AirSession.TYPE_CHANNEL)
				{
					AirChannel c = AirtalkeeChannel.getInstance().ChannelGetByCode(session.getSessionCode());
					if (c != null)
					{
						c.msgUnReadCountClean();
					}
				}
				sessionBoxMessage.refreshMessageNewCount(false);
			}
		}
		else
		{
			Util.hideSoftInput(contextMain);
		}

		if (currentPage == PAGE_MEMBER)
		{
			sessionBoxMember.refreshManageButtons(true);
		}
		else
		{
			sessionBoxMember.refreshManageButtons(false);
		}
	}

}
