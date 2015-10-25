package com.airtalkee.activity.home.widget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.airtalkee.R;
import com.airtalkee.Util.Util;
import com.airtalkee.activity.MainActivity;
import com.airtalkee.activity.SessionBoxTalk;
import com.airtalkee.activity.home.BaseFragment;
import com.airtalkee.activity.home.widget.StatusBarBottom.OnBarItemClickListener;
import com.airtalkee.control.AirMessageTransaction;
import com.airtalkee.control.AirSessionControl;
import com.airtalkee.listener.OnMmiSessionListener;
import com.airtalkee.sdk.AirtalkeeContactPresence;
import com.airtalkee.sdk.AirtalkeeMediaVisualizer;
import com.airtalkee.sdk.AirtalkeeMessage;
import com.airtalkee.sdk.AirtalkeeSessionManager;
import com.airtalkee.sdk.OnMediaListener;
import com.airtalkee.sdk.entity.AirContact;
import com.airtalkee.sdk.entity.AirSession;
import com.airtalkee.sdk.util.Log;

public class MediaStatusBar extends LinearLayout implements OnBarItemClickListener, OnMmiSessionListener, OnMediaListener
{
	private AirSession session;
	private int currentPage = 0;
	private LinearLayout barGroup;
	private StatusBarTitle barTitle;
	private StatusTalkBtn talkBtn;
	private int[] memRes = new int[] { R.drawable.selector_fun_call, R.drawable.selector_fun_msg, R.drawable.selector_fun_cancel };
	private int[] pttRes = new int[] { R.drawable.ic_fun_report, R.drawable.ic_fun_video, R.drawable.ic_fun_call_center };
	private int[] IMRes = new int[] { R.drawable.ic_fun_voice, R.drawable.ic_fun_pic, R.drawable.ic_fun_input };

	private int[][] barArray = new int[][] { memRes, pttRes, IMRes };

	@SuppressLint("UseSparseArrays")
	private Map<Integer, StatusBarBottom> bars = new HashMap<Integer, StatusBarBottom>();
	
	protected    SharedPreferences sessionSp;

	public MediaStatusBar(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		LayoutInflater.from(this.getContext()).inflate(R.layout.include_home_function, this);
		this.sessionSp = context.getSharedPreferences(BaseFragment.SESSION_EVENT_KEY, 0);
	}

	public void init( StatusBarTitle title,AirSession s)
	{
		this.barTitle = title;
		setSession(s);
	}

	public void setSession(AirSession s)
	{
		listenerEnable();
		this.session = s;
		if (null != this.session)
		{
			barTitle.setSession(this.session);
			talkBtn.setSession(this.session);
		}
		sessionRefresh();
	}

	public AirSession getSession()
	{
		return this.session;
	}

	@Override
	protected void onFinishInflate()
	{
		// TODO Auto-generated method stub
		super.onFinishInflate();
		initFindView();
	}

	private void initFindView()
	{
		barGroup = (LinearLayout) findViewById(R.id.tools_bar);
		talkBtn = (StatusTalkBtn) findViewById(R.id.status_talk_btn);
		barInit();
	}

	public View getBottomBarParent()
	{
		return barGroup;
	}

	public void onPageChanged(int arg0)
	{
		// TODO Auto-generated method stub
		StatusBarBottom bar = bars.get(currentPage);
		bar.setVisibility(View.GONE);
		currentPage = arg0;
		bar = bars.get(currentPage);
		bar.setVisibility(View.VISIBLE);
	}

	private void barInit()
	{
		for (int i = 0; i < barArray.length; i++)
		{
			StatusBarBottom bar = new StatusBarBottom(barArray[i][0], barArray[i][1], barArray[i][2], i, getContext(), this);
			barGroup.addView(bar);
			bars.put(i, bar);
		}
	}
	
	public void setBarEnable(int pageIndex,boolean enabled)
	{
		if(bars != null && bars.size()>0)
		{
			StatusBarBottom bar = bars.get(pageIndex);
			if(null != bar)
			{
				ViewGroup grop = (ViewGroup)bar.getChildAt(0);
				for(int i=0;i<grop.getChildCount();i++)
				{
					View child = grop.getChildAt(i);
					child.setEnabled(enabled);
				}
			}
		}
	}

	public void setMediaStatusBarVisibility(int visibility)
	{
		this.setVisibility(visibility);
	}

	@Override
	public void onBarItemClick(int itemId, int page)
	{
		// TODO Auto-generated method stub
//		Toast.makeText(getContext(), itemId + "--" + page, 0).show();
	}

	public void listenerEnable()
	{
		AirtalkeeSessionManager.getInstance().setOnMediaListener(this);
		AirSessionControl.getInstance().setOnMmiSessionListener(this);
	}

	public void listenerDisable()
	{
		AirtalkeeSessionManager.getInstance().setOnMediaListener(null);
		AirtalkeeMediaVisualizer.getInstance().setOnMediaAudioVisualizerListener(null);
		AirSessionControl.getInstance().setOnMmiSessionListener(null);
		AirtalkeeContactPresence.getInstance().setContactPresenceListener(null);
		AirMessageTransaction.getInstance().setOnMessageListener(null);
		AirtalkeeMessage.getInstance().setOnMessageListListener(null);
	}

	public void sessionRefresh()
	{
		if (barTitle != null && talkBtn != null)
		{
			barTitle.refreshMediaStatus();
			talkBtn.refreshPttButton();
		}
	}

	@Override
	public void onSessionOutgoingRinging(AirSession session)
	{
		// TODO Auto-generated method stub
		sessionRefresh();
	}

	@Override
	public void onSessionEstablishing(AirSession session)
	{
		// TODO Auto-generated method stub
		sessionRefresh();
	}

	@Override
	public void onSessionEstablished(AirSession session, boolean isOk)
	{
		// TODO Auto-generated method stub
		sessionRefresh();
	}

	@Override
	public void onSessionReleased(AirSession session, int reason)
	{
		// TODO Auto-generated method stub
		sessionRefresh();
	}

	@Override
	public void onSessionPresence(AirSession session, List<AirContact> membersAll, List<AirContact> membersPresence)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onSessionMemberUpdate(AirSession session, List<AirContact> members, boolean isOk)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onMediaStateTalkPreparing(AirSession session)
	{
		// TODO Auto-generated method stub
		if (this.session != null && TextUtils.equals(this.session.getSessionCode(), session.getSessionCode()))
		{
			Log.i(SessionBoxTalk.class, "onMediaStateListen");
			talkBtn.refreshPttButton();
		}
	}

	@Override
	public void onMediaStateTalk(AirSession session)
	{
		// TODO Auto-generated method stub
//		if (this.session != null && TextUtils.equals(this.session.getSessionCode(), session.getSessionCode()))
//		{
		Log.i(SessionBoxTalk.class, "onMediaStateTalk");
		barTitle.refreshMediaStatus();
		talkBtn.refreshPttButton();

//			tvSpeakerTime.setText("00:00");
//			mSpeakingTimeStamp = System.currentTimeMillis();
//			AirMmiTimer.getInstance().TimerRegister(contextMain, mSpeakingTimer, false, false, 1000, true, null);
//			if (MainActivity.getInstance() != null && MainActivity.getInstance().viewControllerSlideView.isShowMenuLeft())
//			{
//				MainActivity.getInstance().viewLeft.refreshList();
//			}
//		}
	}

	@Override
	public void onMediaStateTalkEnd(AirSession session, int reason)
	{
		// TODO Auto-generated method stub
		Log.i(SessionBoxTalk.class, "onMediaStateTalkEnd");
		switch (reason)
		{
			case AirtalkeeSessionManager.TALK_FINISH_REASON_EXCEPTION:
				Util.Toast(getContext(), getContext().getString(R.string.talk_channel_tip_media_exception));
				break;
			case AirtalkeeSessionManager.TALK_FINISH_REASON_LISTEN_ONLY:
				Util.Toast(getContext(), getContext().getString(R.string.talk_channel_tip_media_listen_only));
				break;
			case AirtalkeeSessionManager.TALK_FINISH_REASON_SPEAKING_FULL:
				Util.Toast(getContext(), getContext().getString(R.string.talk_channel_tip_media_speak_full));
				break;
			case AirtalkeeSessionManager.TALK_FINISH_REASON_TIMEOUT:
				Util.Toast(getContext(), getContext().getString(R.string.talk_channel_tip_media_timeout));
				break;
			case AirtalkeeSessionManager.TALK_FINISH_REASON_TIMEUP:
				Util.Toast(getContext(), getContext().getString(R.string.talk_channel_tip_media_timeup));
				break;
			case AirtalkeeSessionManager.TALK_FINISH_REASON_GRABED:
				Util.Toast(getContext(), getContext().getString(R.string.talk_channel_tip_media_interruptted));
				break;
			default:
				break;
		}
		barTitle.refreshMediaStatus();
		talkBtn.refreshPttButton();

//		AirMmiTimer.getInstance().TimerUnregister(getContext(), mSpeakingTimer);
		if (MainActivity.getInstance() != null && MainActivity.getInstance().viewControllerSlideView.isShowMenuLeft())
		{
			MainActivity.getInstance().viewLeft.refreshList();
		}
		
		int val = sessionSp.getInt(BaseFragment.SESSION_EVENT_KEY, 1);
		sessionSp.edit().putInt(BaseFragment.SESSION_EVENT_KEY, val+1).commit();
	}

	@Override
	public void onMediaStateListen(AirSession session, AirContact speaker)
	{
		// TODO Auto-generated method stub
		if (this.session != null)
		{
			if (TextUtils.equals(this.session.getSessionCode(), session.getSessionCode()))
			{
				Log.i(SessionBoxTalk.class, "onMediaStateListen");
				barTitle.refreshMediaStatus();
			}
			else
			{
//				otherSpeakerOn(session);
			}
		}
	}

	@Override
	public void onMediaStateListenEnd(AirSession session)
	{
		// TODO Auto-generated method stub
		if (this.session != null)
		{
			if (TextUtils.equals(this.session.getSessionCode(), session.getSessionCode()))
			{
				Log.i(SessionBoxTalk.class, "onMediaStateListenEnd");
				barTitle.refreshMediaStatus();
			}
			else
			{

			}

		}
	}

	@Override
	public void onMediaStateListenVoice(AirSession session)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onMediaQueue(AirSession session, ArrayList<AirContact> queue)
	{
		// TODO Auto-generated method stub
		if (this.session != null && TextUtils.equals(this.session.getSessionCode(), session.getSessionCode()))
		{
			Log.i(SessionBoxTalk.class, "onMediaStateListen");
			talkBtn.refreshPttButton();
			Util.Toast(getContext(), getContext().getString(R.string.talk_channel_tip_media_queue_in));
		}
	}

	@Override
	public void onMediaQueueIn(AirSession session)
	{
		// TODO Auto-generated method stub
		if (this.session != null && TextUtils.equals(this.session.getSessionCode(), session.getSessionCode()))
		{
			Log.i(SessionBoxTalk.class, "onMediaStateListen");
			talkBtn.refreshPttButton();
			Util.Toast(getContext(), getContext().getString(R.string.talk_channel_tip_media_queue_in));
		}
	}

	@Override
	public void onMediaQueueOut(AirSession session)
	{
		// TODO Auto-generated method stub
		if (this.session != null && TextUtils.equals(this.session.getSessionCode(), session.getSessionCode()))
		{
			Log.i(SessionBoxTalk.class, "onMediaStateListen");
			talkBtn.refreshPttButton();
			Util.Toast(getContext(), getContext().getString(R.string.talk_channel_tip_media_queue_out));
		}
	}

}
