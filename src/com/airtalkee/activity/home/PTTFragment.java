package com.airtalkee.activity.home;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.airtalkee.R;
import com.airtalkee.Util.Util;
import com.airtalkee.activity.MenuReportActivity;
import com.airtalkee.config.Config;
import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.AirtalkeeMessage;
import com.airtalkee.sdk.AirtalkeeSessionManager;
import com.airtalkee.sdk.controller.SessionController;
import com.airtalkee.sdk.entity.AirFunctionSetting;
import com.airtalkee.sdk.entity.AirMessage;
import com.airtalkee.sdk.entity.AirSession;
import com.airtalkee.sdk.util.Utils;
import com.airtalkee.services.AirServices;

public class PTTFragment extends BaseFragment implements OnClickListener
{

	private LinearLayout recPlayback;
	private ImageView recPlaybackIcon;
	private TextView recPlaybackUser;
	private TextView recPlaybackSeconds;
	private TextView recPlaybackTime;
	private TextView recPlaybackNone;
	private ImageView recPlaybackNew;

	private AirSession session = null;
	private AirMessage currentMessage;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

	}

	@Override
	public void onResume()
	{
		// TODO Auto-generated method stub
		super.onResume();
		setSession(getSession());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		v = inflater.inflate(getLayout(), container, false);

		findViewById(R.id.talk_playback).setOnClickListener(this);
		recPlayback = (LinearLayout) findViewById(R.id.talk_playback_panel);
		recPlaybackIcon = (ImageView) findViewById(R.id.talk_playback_icon);
		recPlaybackUser = (TextView) findViewById(R.id.talk_playback_user);
		recPlaybackSeconds = (TextView) findViewById(R.id.talk_playback_seconds);
		recPlaybackTime = (TextView) findViewById(R.id.talk_playback_time);
		recPlaybackNone = (TextView) findViewById(R.id.talk_playback_none);
		recPlaybackNew = (ImageView) findViewById(R.id.talk_playback_user_unread);

		refreshPlayback();
		return v;
	}

	@Override
	public int getLayout()
	{
		// TODO Auto-generated method stub
		return R.layout.frag_ptt_layout;
	}

	@Override
	public void dispatchBarClickEvent(int page, int id)
	{
		// TODO Auto-generated method stub
		if (page == HomeActivity.PAGE_PTT)
		{
			// TODO Auto-generated method stub
			switch (id)
			{
				case R.id.bar_left:
					Intent it = new Intent(getActivity(), MenuReportActivity.class);
					it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(it);
					break;
				case R.id.bar_mid:

					break;
				case R.id.bar_right:
					callStationCenter();
					break;
			}

		}
	}

	public void setSession(AirSession s)
	{
		this.session = s;

	}

	private void callStationCenter()
	{
		if (Config.funcCenterCall == AirFunctionSetting.SETTING_ENABLE)
		{
			if (AirtalkeeAccount.getInstance().isAccountRunning())
			{
				if (AirtalkeeAccount.getInstance().isEngineRunning())
				{
					AirSession session = SessionController.SessionMatchSpecial(AirtalkeeSessionManager.SPECIAL_NUMBER_DISPATCHER, getString(R.string.talk_tools_call_center));
					AirServices.getInstance().switchToSessionTemp(session.getSessionCode(), AirServices.TEMP_SESSION_TYPE_OUTGOING, getActivity());
				}
				else
				{
					Util.Toast(getActivity(), getString(R.string.talk_network_warning));
				}
			}
		}
		else if (Config.funcCenterCall == AirFunctionSetting.SETTING_CALL_NUMBER && !Utils.isEmpty(Config.funcCenterCallNumber))
		{
			Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + Config.funcCenterCallNumber));
			getActivity().startActivity(intent);
		}
	}

	@Override
	public void onClick(View v)
	{
		// TODO Auto-generated method stub
		if (v.getId() == R.id.talk_playback)
		{
			if (session != null && session.getMessagePlayback() != null)
			{
				currentMessage = session.getMessagePlayback();
				if (currentMessage.isRecordPlaying())
				{
					AirtalkeeMessage.getInstance().MessageRecordPlayStop();
				}
				else
				{
					AirtalkeeMessage.getInstance().MessageRecordPlayStart(currentMessage);
					if (currentMessage.getState() == AirMessage.STATE_NEW)
					{
						session.setMessageUnreadCount(session.getMessageUnreadCount() - 1);
//						refreshMessageNewCount(false);
					}
				}
			}
		}
	}

	public void refreshPlayback()
	{
		if (session != null && session.getMessagePlayback() != null)
		{
			AirMessage msg = session.getMessagePlayback();
			if (msg.isRecordPlaying())
			{
				recPlaybackIcon.setImageResource(R.drawable.msg_audio_stop);
			}
			else
			{
				recPlaybackIcon.setImageResource(R.drawable.msg_audio_play);
			}
			if (TextUtils.equals(msg.getIpocidFrom(), AirtalkeeAccount.getInstance().getUserId()))
				recPlaybackUser.setText(getString(R.string.talk_me));
			else
				recPlaybackUser.setText(msg.getInameFrom());
			recPlaybackSeconds.setText(msg.getImageLength() + "''");
			recPlaybackTime.setText(msg.getTime());
			recPlayback.setVisibility(View.VISIBLE);
			recPlaybackNone.setVisibility(View.GONE);
			if (msg.getState() == AirMessage.STATE_NEW)
			{
				recPlaybackNew.setVisibility(View.VISIBLE);
			}
			else
			{
				recPlaybackNew.setVisibility(View.GONE);
			}
		}
		else
		{
			recPlaybackIcon.setImageResource(R.drawable.msg_audio_play);
			recPlaybackUser.setText("");
			recPlaybackSeconds.setText("");
			recPlaybackTime.setText("");
			recPlayback.setVisibility(View.GONE);
			recPlaybackNone.setVisibility(View.VISIBLE);
			recPlaybackNew.setVisibility(View.GONE);
		}
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
	{
		// TODO Auto-generated method stub
		if (key.equals(SESSION_EVENT_KEY))
		{
			if (null != session && session.getMessagePlayback() != null)
			{
				refreshPlayback();
			}
		}

	}

}
