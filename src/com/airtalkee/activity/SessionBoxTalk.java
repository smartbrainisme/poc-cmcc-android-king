package com.airtalkee.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.airtalkee.R;
import com.airtalkee.Util.AirMmiTimer;
import com.airtalkee.Util.AirMmiTimerListener;
import com.airtalkee.Util.ThemeUtil;
import com.airtalkee.Util.Util;
import com.airtalkee.adapter.AdapterQueue;
import com.airtalkee.adapter.AdapterTools;
import com.airtalkee.bluetooth.BluetoothManager;
import com.airtalkee.config.Config;
import com.airtalkee.control.AirAccountManager;
import com.airtalkee.control.AirSessionControl;
import com.airtalkee.listener.OnMmiSessionBoxRefreshListener;
import com.airtalkee.listener.OnMmiSessionListener;
import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.AirtalkeeChannel;
import com.airtalkee.sdk.AirtalkeeMessage;
import com.airtalkee.sdk.AirtalkeeSessionManager;
import com.airtalkee.sdk.AirtalkeeVideo;
import com.airtalkee.sdk.OnMediaAudioVisualizerListener;
import com.airtalkee.sdk.OnMediaListener;
import com.airtalkee.sdk.entity.AirChannel;
import com.airtalkee.sdk.entity.AirContact;
import com.airtalkee.sdk.entity.AirSession;
import com.airtalkee.sdk.util.Log;
import com.airtalkee.widget.AudioVisualizerView;
import com.airtalkee.widget.VideoSufaceView;
import com.airtalkee.widget.VideoSufaceView.OnVideoStateChangeListener;

public class SessionBoxTalk extends View implements OnClickListener, OnItemClickListener, OnCheckedChangeListener, OnTouchListener, OnMediaAudioVisualizerListener,
	OnMediaListener, OnMmiSessionListener, AirMmiTimerListener
{

	private final int TIMEOUT_LONG_CLICK = 200;
	private Activity contextMain = null;
	private SessionBox sessionBox = null;
	private OnMmiSessionBoxRefreshListener sessionBoxRefreshListener = null;
	private AirSession session = null;
	private AirChannel channel = null;
	private ListView lvQueue;
	public static final int mVisualizerSpectrumNum = 18;
	private AudioVisualizerView mVisualizerView;
	private ImageView btnTalk, btnTalkVideo, btnTalkCall;
	private TextView tvSpeaker;
	private ImageView tvSpeakerIcon;
	private TextView tvSpeakerTime;
	private TextView tvQueueFirst;
	private ImageView ivSpeaker;
	private View laoutQueue;
	private View layoutSpeakerStatus;
	private LinearLayout layoutHidePanelLayout;
	private AdapterQueue adapterQueue;
	private ImageView toolManager;
	private ImageView toolPlayMode;
	private PopupWindow pwPlayMode = null;
	private GridView gvTools;
	private AdapterTools adapterTools;
	private View parentView;
	private ImageView btnLock;
	private VideoSufaceView videoSuface;
	private ImageView videoActionOpen, videoActionClose;
	private ImageButton videoSettings;
	private RadioGroup videoSettingRadio;
	private FrameLayout videoSettingsLayout;
	private RelativeLayout videoLayoutNormal, videoLayoutRun;
	private LinearLayout mSessionSpeakPanel;
	private TextView mSessionSpeakGroup, mSessionSpeakUser;
	private int screenWidth, screenHeight;

	private long mSpeakingTimeStamp = 0;
	private AirMmiTimerListener mSpeakingTimer = new AirMmiTimerListener()
	{

		@Override
		public void onMmiTimer(Context context, Object userData)
		{
			// TODO Auto-generated method stub
			Log.e(SessionBoxTalk.class, "Speaking Timer!!");
			if (tvSpeakerTime.getVisibility() == View.VISIBLE && mSpeakingTimeStamp > 0)
			{
				int time = (int) (System.currentTimeMillis() - mSpeakingTimeStamp);
				if (time > 0)
				{
					int tminite = time / 1000 / 60;
					int tsecond = time / 1000 % 60;
					String sminite = tminite < 10 ? "0" + tminite : "" + tminite;
					String ssecond = tsecond < 10 ? "0" + tsecond : "" + tsecond;
					tvSpeakerTime.setText(sminite + ":" + ssecond);
				}
			}
		}
	};

	// public View vConnectTip;

	@SuppressWarnings("deprecation")
	public SessionBoxTalk(Context context, View parentView, SessionBox box, OnMmiSessionBoxRefreshListener listener)
	{
		super(context);
		// TODO Auto-generated constructor stub
		contextMain = (Activity) context;
		sessionBox = box;
		sessionBoxRefreshListener = listener;

		screenWidth = contextMain.getWindowManager().getDefaultDisplay().getWidth();
		screenHeight = contextMain.getWindowManager().getDefaultDisplay().getHeight();

		loadView(parentView);
	}

	private void loadView(View parentView)
	{
		this.parentView = parentView;
		btnTalk = (ImageView) parentView.findViewById(R.id.talk_btn_session);
		btnTalk.setImageResource(R.drawable.btn_talk_none);
		btnTalk.setOnTouchListener(this);
		btnTalkVideo = (ImageView) parentView.findViewById(R.id.talk_btn_session_on_video);
		btnTalkVideo.setImageResource(R.drawable.btn_talk_video_none);
		btnTalkVideo.setOnTouchListener(this);
		btnTalkCall = (ImageView) parentView.findViewById(R.id.talk_btn_session_call_icon);
		btnTalkCall.setVisibility(View.GONE);
		btnLock = (ImageView) parentView.findViewById(R.id.bottom_right_icon);
		
		layoutHidePanelLayout = (LinearLayout) parentView.findViewById(R.id.talk_layout_hide_panel);
		mVisualizerView = (AudioVisualizerView) parentView.findViewById(R.id.talk_audio_visualizer);
		mVisualizerView.setSpectrumNum(mVisualizerSpectrumNum);

		if (Config.pttButtonVisibility == View.VISIBLE)
		{
			btnTalk.setVisibility(View.VISIBLE);
			layoutHidePanelLayout.setVisibility(View.GONE);
		}
		else
		{
			btnTalk.setVisibility(View.GONE);
			layoutHidePanelLayout.setVisibility(View.VISIBLE);
			if (sessionBox.getSessionType() == AirSession.TYPE_CHANNEL)
			{
				parentView.findViewById(R.id.talk_btn_call_center_ptt).setOnClickListener(this);
			}
			else
			{
				parentView.findViewById(R.id.talk_btn_call_center_ptt).setVisibility(View.GONE);
			}
		}

		tvSpeaker = (TextView) parentView.findViewById(R.id.talk_tv_session_speaker);
		tvSpeaker.setText(R.string.talk_channel_idle);
		tvSpeakerIcon = (ImageView) parentView.findViewById(R.id.talk_tv_session_speaker_icon);
		tvSpeakerTime = (TextView) parentView.findViewById(R.id.talk_tv_session_speaker_time);
		ivSpeaker = (ImageView) parentView.findViewById(R.id.talk_iv_session_speaker);
		layoutSpeakerStatus = parentView.findViewById(R.id.talk_layout_meida_status1);
		layoutSpeakerStatus.setOnTouchListener(this);
		laoutQueue = parentView.findViewById(R.id.talk_layout_media_queue);
		laoutQueue.setOnClickListener(this);
		tvQueueFirst = (TextView) parentView.findViewById(R.id.talk_tv_session_queue);

		adapterQueue = new AdapterQueue(contextMain);
		lvQueue = (ListView) parentView.findViewById(R.id.talk_lv_session_queue);
		lvQueue.setAdapter(adapterQueue);

		parentView.findViewById(R.id.talk_tool_setting).setOnClickListener(this);
		toolManager = (ImageView) parentView.findViewById(R.id.talk_tool_manager);
		if (sessionBox.getSessionType() == AirSession.TYPE_CHANNEL)
		{
			toolManager.setVisibility(View.VISIBLE);
			toolManager.setOnClickListener(this);
		}
		else
		{
			toolManager.setVisibility(View.INVISIBLE);
		}
		toolPlayMode = (ImageView) parentView.findViewById(R.id.talk_tool_play_mode);
		toolPlayMode.setOnClickListener(this);

		videoSuface = (VideoSufaceView) parentView.findViewById(R.id.video_suface);
		videoSuface.setVisibility(View.GONE);
		videoSettings = (ImageButton) parentView.findViewById(R.id.talk_btn_settings);
		videoSettings.setOnClickListener(this);
		videoSettings.setVisibility(View.GONE);
		videoActionOpen = (ImageView) parentView.findViewById(R.id.talk_btn_video_open);
		videoActionOpen.setOnClickListener(this);
		videoActionOpen.setVisibility(View.GONE);
		videoActionClose = (ImageView) parentView.findViewById(R.id.talk_btn_video_close);
		videoActionClose.setOnClickListener(this);
		videoSettingRadio = (RadioGroup) parentView.findViewById(R.id.radio);
		videoSettingRadio.setOnCheckedChangeListener(this);
		videoSettingsLayout = (FrameLayout) parentView.findViewById(R.id.video_layout);
		videoLayoutNormal = (RelativeLayout) parentView.findViewById(R.id.talk_layout_normal);
		videoLayoutRun = (RelativeLayout) parentView.findViewById(R.id.talk_layout_video);
		
		mSessionSpeakPanel = (LinearLayout) parentView.findViewById(R.id.main_speak_panel);
		mSessionSpeakGroup = (TextView) parentView.findViewById(R.id.main_speak_group);
		mSessionSpeakUser = (TextView) parentView.findViewById(R.id.main_speak_user);
		mSessionSpeakPanel.setVisibility(View.GONE);
	}

	public void setSession(AirSession s)
	{
		session = s;
		if (s != null)
		{
			if (sessionBox.getSessionType() == AirSession.TYPE_CHANNEL)
			{
				channel = AirtalkeeChannel.getInstance().ChannelGetByCode(s.getSessionCode());
				parentView.findViewById(R.id.talk_menu_right_button).setOnClickListener(this);
			}
			else
			{
				channel = null;
			}
		}
		else
		{
			channel = null;
		}
		refreshSession();
		refreshRole(false);
		otherSpeakerClean();
	}
	
	public void videoStart()
	{
		if (AirAccountManager.VIDEO_PORT == 0)
		{
			Util.Toast(contextMain, contextMain.getString(R.string.talk_video_addr_error));
			AirtalkeeVideo.getInstance().VideoAddr();
		}
		else
		{
			videoLayoutNormal.setVisibility(View.GONE);
			videoLayoutRun.setVisibility(View.VISIBLE);
			videoSuface.setVisibility(View.VISIBLE);
			videoSuface.start(new OnVideoStateChangeListener()
			{
				@Override
				public void onVideoStateChange(boolean isClose)
				{
					// TODO Auto-generated method stub
					if (isClose)
					{
						videoSettings.setVisibility(View.GONE);
						contextMain.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
					}
					else
					{
						contextMain.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
						videoSettings.setVisibility(View.VISIBLE);
					}
				}
			}, videoSettingRadio, parentView, session, AirAccountManager.VIDEO_IP, AirAccountManager.VIDEO_PORT, screenWidth, screenHeight);
		}
	}
	
	public void videoFinish()
	{
		videoLayoutNormal.setVisibility(View.VISIBLE);
		videoLayoutRun.setVisibility(View.GONE);
		videoSuface.setVisibility(View.GONE);
		videoSuface.finish();
	}

	public boolean refreshRole(boolean toChange)
	{
		boolean isAppling = false;
		if (session != null && sessionBox.getSessionType() == AirSession.TYPE_CHANNEL)
		{
			AirChannel channel = AirtalkeeChannel.getInstance().ChannelGetByCode(session.getSessionCode());
			if (channel != null)
			{
				if (toChange)
				{
					channel.setRoleAppling(!channel.isRoleAppling());
				}
				if (channel.isRoleAppling())
				{
					toolManager.setImageResource(R.drawable.media_manager_select);
				}
				else
				{
					toolManager.setImageResource(ThemeUtil.getResourceId(R.attr.theme_media_manager, contextMain));
				}
				isAppling = channel.isRoleAppling();
			}
		}
		return isAppling;
	}

	public void refreshPttButton()
	{
		if (session == null)
			return;
		switch (session.getMediaButtonState())
		{
			case AirSession.MEDIA_BUTTON_STATE_IDLE:
				if (session.getSessionState() == AirSession.SESSION_STATE_DIALOG)
				{
					int resid = ThemeUtil.getResourceId(R.attr.theme_talk_idle, contextMain);
					btnTalk.setImageResource(resid);
					btnTalkVideo.setImageResource(R.drawable.btn_talk_video_idle);
					if (Config.pttButtonVisibility == View.VISIBLE)
						btnTalkCall.setVisibility(View.GONE);
				}
				else
				{
					int resid = ThemeUtil.getResourceId(R.attr.theme_talk_none, contextMain);
					btnTalk.setImageResource(resid);
					btnTalkVideo.setImageResource(R.drawable.btn_talk_video_none);
					if (session.getType() == AirSession.TYPE_DIALOG && Config.pttButtonVisibility == View.VISIBLE)
					{
						btnTalkCall.setVisibility(View.VISIBLE);
						btnTalkCall.setImageResource(ThemeUtil.getResourceId(R.attr.theme_talk_call_idle, contextMain));
					}
				}
				refreshMediaState();
				break;
			case AirSession.MEDIA_BUTTON_STATE_CONNECTING:
				int resid = ThemeUtil.getResourceId(R.attr.theme_talk_idle, contextMain);
				btnTalk.setImageResource(resid);
				btnTalkVideo.setImageResource(R.drawable.btn_talk_video_idle);
				tvSpeaker.setText(R.string.talk_session_building);
				tvSpeakerIcon.setVisibility(View.GONE);
				tvSpeakerTime.setVisibility(View.GONE);
				if (session.getType() == AirSession.TYPE_DIALOG && Config.pttButtonVisibility == View.VISIBLE)
				{
					btnTalkCall.setVisibility(View.VISIBLE);
					btnTalkCall.setImageResource(ThemeUtil.getResourceId(R.attr.theme_talk_call_ing, contextMain));
				}
				break;
			case AirSession.MEDIA_BUTTON_STATE_TALKING:
				if (Config.marketCode == Config.MARKET_CHINA_TELECOM || Config.marketCode == Config.MARKET_CHINA_35)
					btnTalk.setImageResource(R.drawable.btn_talk_speak_red);
				else
					btnTalk.setImageResource(ThemeUtil.getResourceId(R.attr.theme_talk_speak, contextMain));
				btnTalkVideo.setImageResource(R.drawable.btn_talk_video_speak);
				break;
			case AirSession.MEDIA_BUTTON_STATE_QUEUE:
				btnTalk.setImageResource(ThemeUtil.getResourceId(R.attr.theme_talk_press, contextMain));
				btnTalkVideo.setImageResource(R.drawable.btn_talk_video_press);
				break;
			case AirSession.MEDIA_BUTTON_STATE_REQUESTING:
				btnTalk.setImageResource(ThemeUtil.getResourceId(R.attr.theme_talk_press, contextMain));
				btnTalkVideo.setImageResource(R.drawable.btn_talk_video_press);
				tvSpeaker.setText(R.string.talk_click_applying);
				tvSpeakerIcon.setVisibility(View.GONE);
				tvSpeakerTime.setVisibility(View.GONE);
				ivSpeaker.setImageResource(ThemeUtil.getResourceId(R.attr.theme_media_talk_wait, contextMain));
				break;
			case AirSession.MEDIA_BUTTON_STATE_RELEASING:
				btnTalk.setImageResource(ThemeUtil.getResourceId(R.attr.theme_talk_idle, contextMain));
				btnTalkVideo.setImageResource(R.drawable.btn_talk_video_idle);
				refreshMediaState();
				break;
		}
	}

	public void refreshMediaState()
	{
		if (session == null)
			return;
		Log.i(MainSessionView.class, "refreshMediaState name=" + session.getDisplayName() + " state=" + session.getSessionState());
		try
		{
			switch (session.getSessionState())
			{
				case AirSession.SESSION_STATE_CALLING:
					tvSpeaker.setText(R.string.talk_session_building);
					ivSpeaker.setImageResource(ThemeUtil.getResourceId(R.attr.theme_media_idle, contextMain));
					tvSpeakerIcon.setVisibility(View.GONE);
					tvSpeakerTime.setVisibility(View.GONE);
					break;
				case AirSession.SESSION_STATE_DIALOG:
					switch (session.getMediaState())
					{
						case AirSession.MEDIA_STATE_IDLE:
						{
							tvSpeaker.setText(R.string.talk_session_speak_idle);
							ivSpeaker.setImageResource(ThemeUtil.getResourceId(R.attr.theme_media_idle, contextMain));
							tvSpeakerIcon.setVisibility(View.GONE);
							tvSpeakerTime.setVisibility(View.GONE);
							break;
						}
						case AirSession.MEDIA_STATE_TALK:
						{
							ivSpeaker.setImageResource(ThemeUtil.getResourceId(R.attr.theme_media_talk, contextMain));
							// tvSpeaker.setText(contextMain.getString(R.string.talk_speak_me));
							tvSpeaker.setText("");
							tvSpeakerIcon.setVisibility(View.VISIBLE);
							tvSpeakerTime.setVisibility(View.VISIBLE);
							break;
						}
						case AirSession.MEDIA_STATE_LISTEN:
						{
							AirContact contact = session.getSpeaker();
							ivSpeaker.setImageResource(ThemeUtil.getResourceId(R.attr.theme_media_listen, contextMain));
							if (contact != null)
							{
								tvSpeaker.setText(contact.getDisplayName() + "  " + contextMain.getString(R.string.talk_speaking));
								tvSpeakerIcon.setVisibility(View.GONE);
								tvSpeakerTime.setVisibility(View.GONE);
							}
							break;
						}
					}
					break;
				case AirSession.SESSION_STATE_IDLE:
					if (session.getType() == AirSession.TYPE_CHANNEL)
					{
						tvSpeaker.setText(R.string.talk_channel_idle);
					}
					else
					{
						tvSpeaker.setText(R.string.talk_session_speak_idle);
					}
					ivSpeaker.setImageResource(ThemeUtil.getResourceId(R.attr.theme_media_idle, contextMain));
					tvSpeakerIcon.setVisibility(View.GONE);
					tvSpeakerTime.setVisibility(View.GONE);
					break;
			}
		}
		catch (Exception e)
		{
			// TODO: handle exception
		}
	}

	public void refreshQueue()
	{
		if (session != null)
		{
			ArrayList<AirContact> queue = session.usersQueues();
			if (queue != null && queue.size() > 0)
			{
				laoutQueue.setVisibility(View.VISIBLE);
				AirContact c = queue.get(0);
				if (c != null)
				{
					tvQueueFirst.setText(c.getDisplayName() + "(" + queue.size() + ")");
				}
			}
			else
			{
				laoutQueue.setVisibility(View.GONE);
				lvQueue.setVisibility(View.GONE);
			}
			adapterQueue.notfiyQueue(queue);
		}
	}

	public void refreshSession()
	{
		if (BluetoothManager.getInstance() != null)
			BluetoothManager.getInstance().setModeContext(toolPlayMode, contextMain);
		if (session != null)
		{
			switch (session.getSessionState())
			{
				case AirSession.SESSION_STATE_IDLE:
				{
					if (session.getType() == AirSession.TYPE_CHANNEL)
					{
						// vConnectTip.setVisibility(View.VISIBLE);
						btnLock.setVisibility(View.GONE);
					}
					adapterQueue.notfiyQueue(null);
					// layoutSpeakerStatus.setVisibility(View.INVISIBLE);
					// ivAudioWaveLayout.setVisibility(View.INVISIBLE);
					laoutQueue.setVisibility(View.GONE);
					lvQueue.setVisibility(View.GONE);
					break;
				}
				case AirSession.SESSION_STATE_DIALOG:
				{
					if (session.getType() == AirSession.TYPE_CHANNEL)
					{
						// vConnectTip.setVisibility(View.GONE);
						btnLock.setVisibility(View.VISIBLE);
						if (session.isVoiceLocked())
							btnLock.setImageResource(R.drawable.ic_topbar_lock_close);
						else
							btnLock.setImageResource(R.drawable.ic_topbar_lock_open);
					}
					// layoutSpeakerStatus.setVisibility(View.VISIBLE);
					// contextMain.removeDialog(R.id.talk_dialog_connect_waiting);
					break;
				}
				case AirSession.SESSION_STATE_CALLING:
				{
					if (session.getType() == AirSession.TYPE_CHANNEL)
					{
						// vConnectTip.setVisibility(View.GONE);
						btnLock.setVisibility(View.GONE);
					}
					// layoutSpeakerStatus.setVisibility(View.INVISIBLE);
					// contextMain.showDialog(R.id.talk_dialog_connect_waiting);
					// ivAudioWaveLayout.setVisibility(View.INVISIBLE);
					break;
				}
			}
			refreshMediaState();
			refreshPttButton();

			if (Config.funcVideo && (session.getSpecialNumber() == AirtalkeeSessionManager.SPECIAL_NUMBER_DISPATCHER || session.getType() == AirSession.TYPE_CHANNEL) && session.getSessionState() == AirSession.SESSION_STATE_DIALOG)
				videoActionOpen.setVisibility(View.VISIBLE);
			else
				videoActionOpen.setVisibility(View.GONE);

			if (sessionBoxRefreshListener != null)
			{
				sessionBoxRefreshListener.onMmiSessionRefresh(session);
			}
		}
		else
			btnLock.setVisibility(View.GONE);
	}
	
	private AirSession mOtherSession = null;
	
	private void otherSpeakerOn(AirSession session)
	{
		if (session != null && session.getSpeaker() != null)
		{
			/*
			if (session.isVoicePlaying())
				mSessionSpeakIcon.setVisibility(View.VISIBLE);
			else
				mSessionSpeakIcon.setVisibility(View.GONE);
			*/
			mSessionSpeakGroup.setText(session.getDisplayName());
			mSessionSpeakUser.setText(session.getSpeaker().getDisplayName());
			mSessionSpeakPanel.setVisibility(View.VISIBLE);
			mOtherSession = session;
		}
		else
			mSessionSpeakPanel.setVisibility(View.GONE);
	}
	
	private void otherSpeakerOff(AirSession session)
	{
		if (session == mOtherSession)
		{
			mSessionSpeakPanel.setVisibility(View.GONE);
			mOtherSession = null;
		}
	}
	
	private void otherSpeakerClean()
	{
		mSessionSpeakPanel.setVisibility(View.GONE);
		mOtherSession = null;
	}

	public boolean onKeyEvent(KeyEvent event)
	{
		boolean isHandled = false;

		return isHandled;
	}

	@Override
	public void onClick(View v)
	{
		// TODO Auto-generated method stub
		if (sessionBox.isMenuShowing())
		{
			sessionBox.resetMenu();
			return;
		}

		switch (v.getId())
		{
			case R.id.talk_menu_right_button:
			{
				if (session != null && session.getType() == AirSession.TYPE_CHANNEL && session.getSessionState() == AirSession.SESSION_STATE_DIALOG)
				{
					if (session.isVoiceLocked())
					{
						AirtalkeeSessionManager.getInstance().SessionLock(session, false);
						btnLock.setImageResource(R.drawable.ic_topbar_lock_open);
					}
					else
					{
						AirtalkeeSessionManager.getInstance().SessionLock(session, true);
						btnLock.setImageResource(R.drawable.ic_topbar_lock_close);
					}
				}
				break;
			}
			case R.id.talk_btn_call_center_ptt:
			{
				sessionBox.sessionBoxMember.callStationCenter();
				break;
			}
			case R.id.talk_layout_media_queue:
			{
				lvQueue.setVisibility(lvQueue.getVisibility() != View.VISIBLE ? View.VISIBLE : View.GONE);
				break;
			}
			case R.id.talk_layout_session_null_retry:
			{
				/*
				 * ZUOLIN
				 * if (session != null)
				 * {
				 * contextMain.leftView.adapter.resetSelectedItem(session.
				 * getSessionCode());
				 * contextMain.leftView.adapter.notifyDataSetChanged();
				 * }
				 * if (AirAccountManager.channelCurrentIndex == -1)
				 * {
				 * contextMain.viewControllerSlideView.transLeftShow();
				 * }
				 * else
				 * {
				 * contextMain.leftView.doConnect();
				 * }
				 */
				break;
			}
			case R.id.talk_layout_session_null_select:
			{
				MainActivity mainActivity = (MainActivity) contextMain;
				mainActivity.viewControllerSlideView.transLeftShow();
				break;
			}
			case R.id.talk_tool_setting:
			{
				Intent it = new Intent(contextMain, MenuSettingPttActivity.class);
				contextMain.startActivity(it);
				break;
			}
			case R.id.talk_tool_manager:
			{
				boolean isAppling = refreshRole(true);
				if (isAppling)
				{
					Util.Toast(contextMain, contextMain.getString(R.string.talk_channel_manager_grab_enable));
				}
				else
				{
					Util.Toast(contextMain, contextMain.getString(R.string.talk_channel_manager_grab_disable));
				}
				break;
			}
			case R.id.talk_tool_play_mode:
			{
				showToolsPw();
				break;
			}
			case R.id.talk_btn_video_open:
			{
				videoStart();
				break;
			}
			case R.id.talk_btn_video_close:
			{
				videoFinish();
				break;
			}
			case R.id.talk_btn_settings:
			{
				if (videoSettingsLayout.getVisibility() == View.VISIBLE)
				{
					videoSettingsLayout.setVisibility(View.GONE);
				}
				else
				{
					videoSettingsLayout.setVisibility(View.VISIBLE);
				}
				break;
			}
			default:
				break;
		}
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId)
	{
		// TODO Auto-generated method stub
		videoSettingsLayout.setVisibility(View.GONE);
		videoSuface.selectQuality(videoSettingRadio);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
	{
		// TODO Auto-generated method stub
		if (arg0.getId() == R.id.tools_gridview)
		{
			if (pwPlayMode != null)
			{
				pwPlayMode.dismiss();
			}
			switch (arg2)
			{
				case AdapterTools.MENU_SPEAKER:
					BluetoothManager.getInstance().doChangeSpeaker();
					break;
				case AdapterTools.MENU_RECEIVER:
					BluetoothManager.getInstance().doChangeVoiceCall();
					break;
				case AdapterTools.MENU_BLUETOOTH:
				{
					BluetoothManager.getInstance().doChangeBluetooth();
					break;
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	private void showToolsPw()
	{
		if (pwPlayMode == null)
		{
			LayoutInflater mLayoutInflater = (LayoutInflater) contextMain.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View actionView = mLayoutInflater.inflate(R.layout.layout_popup_window_tools, null);
			View v = actionView.findViewById(R.id.layout_pup_content);
			v.setBackgroundResource(ThemeUtil.getResourceId(R.attr.theme_playvideo_tools_background, contextMain));
			pwPlayMode = new PopupWindow(actionView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			gvTools = (GridView) actionView.findViewById(R.id.tools_gridview);
			gvTools.setOnItemClickListener(this);
			adapterTools = new AdapterTools(contextMain, R.array.play_mode_icon_array);
			gvTools.setAdapter(adapterTools);
		}
		RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		p.height = (int) (contextMain.getResources().getDimension(R.dimen.pop_menu_tools_height));
		p.width = (int) ((contextMain.getResources().getDimension(R.dimen.pop_menu_tools_width) + 5) * adapterTools.getCount());
		gvTools.setLayoutParams(p);
		pwPlayMode.setAnimationStyle(R.style.menudialog);
		pwPlayMode.setOutsideTouchable(true);
		pwPlayMode.setFocusable(true);
		pwPlayMode.setBackgroundDrawable(new BitmapDrawable());

		View v = parentView.findViewById(R.id.layout_bottom);
		pwPlayMode.showAsDropDown(parentView.findViewById(R.id.layout_tool), 0, -v.getHeight() * 2);
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		BluetoothManager.getInstance().onActivityResult(requestCode, resultCode, data);
	}

	private boolean isTalkLongClick = false;

	@Override
	public boolean onTouch(View v, MotionEvent event)
	{
		if (sessionBox.isMenuShowing())
		{
			sessionBox.resetMenu();
			return false;
		}
		else
		{
			try
			{
				if (v.getId() == R.id.talk_btn_session || v.getId() == R.id.talk_btn_session_on_video)
				{
					boolean isAction = false;
					if (v.getId() == R.id.talk_btn_session)
					{
						if (event.getAction() == MotionEvent.ACTION_DOWN)
						{
							double d = Math.sqrt((btnTalk.getWidth() / 2 - event.getX()) * (btnTalk.getWidth() / 2 - event.getX()) + (btnTalk.getHeight() / 2 - event.getY())
								* (btnTalk.getHeight() / 2 - event.getY()));
							if (btnTalk.getWidth() / 2 >= d)
							{
								isAction = true;
							}
						}
						else
							isAction = true;
					}
					else
						isAction = true;

					if (isAction && session != null)
					{
						if (session.getSessionState() == AirSession.SESSION_STATE_DIALOG)
						{
							if (Config.pttClickSupport)
							{
								if (event.getAction() == MotionEvent.ACTION_DOWN)
								{
									isTalkLongClick = false;
									v.setPressed(true);
									Log.i(MainSessionView.class, "TalkButton Start timeout!");
									AirMmiTimer.getInstance().TimerRegister(contextMain, this, false, true, TIMEOUT_LONG_CLICK, false, null);
								}
								else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL)
								{
									AirMmiTimer.getInstance().TimerUnregister(contextMain, this);
									if (session.getSessionState() == AirSession.SESSION_STATE_DIALOG)
									{
										if (isTalkLongClick)
										{
											Log.i(MainSessionView.class, "TalkButton onLongClick released!");
											AirtalkeeSessionManager.getInstance().TalkRelease(session);
											isTalkLongClick = false;
											v.setPressed(false);
										}
										else
										{
											Log.i(MainSessionView.class, "TalkButton onClick!");
											AirtalkeeSessionManager.getInstance().TalkButtonClick(session, channel != null ? channel.isRoleAppling() : false);
										}
									}
									isTalkLongClick = false;
								}
							}
							else
							{
								if (event.getAction() == MotionEvent.ACTION_DOWN)
								{
									Log.i(MainSessionView.class, "TalkButton onLongClick TalkRequest!");
									AirtalkeeSessionManager.getInstance().TalkRequest(session, channel != null ? channel.isRoleAppling() : false);
								}
								else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL)
								{
									Log.i(MainSessionView.class, "TalkButton onLongClick TalkRelease!");
									AirtalkeeSessionManager.getInstance().TalkRelease(session);
								}
							}
						}
						else if (session.getSessionState() == AirSession.SESSION_STATE_CALLING)
						{
							if (event.getAction() == MotionEvent.ACTION_DOWN)
							{
								if (session.getType() == AirSession.TYPE_DIALOG)
								{
									AirSessionControl.getInstance().SessionEndCall(session);
								}
							}
						}
						else if (session.getSessionState() == AirSession.SESSION_STATE_IDLE)
						{
							if (event.getAction() == MotionEvent.ACTION_DOWN)
							{
								if (session.getType() == AirSession.TYPE_DIALOG)
								{
									AirSessionControl.getInstance().SessionMakeCall(session);
								}
								else if (session.getType() == AirSession.TYPE_CHANNEL)
								{
									if (AirtalkeeAccount.getInstance().isEngineRunning())
									{
										AirSessionControl.getInstance().SessionChannelIn(session.getSessionCode());
									}
									else
									{
										Util.Toast(contextMain, contextMain.getString(R.string.talk_network_warning));
									}
								}
							}
						}
						return true;
					}
				}
				return true;
			}
			catch (Exception e)
			{
				// TODO: handle exception
				return false;
			}
		}
	}

	@Override
	public void onMmiTimer(Context context, Object userData)
	{
		// TODO Auto-generated method stub
		try
		{
			isTalkLongClick = true;
			Log.i(MainSessionView.class, "TalkButton Stop timeout!");
			AirtalkeeSessionManager.getInstance().TalkRequest(session, channel != null ? channel.isRoleAppling() : false);
		}
		catch (Exception e)
		{
			// TODO: handle exception
		}
	}

	@Override
	public void onMediaQueue(AirSession session, ArrayList<AirContact> queue)
	{
		// TODO Auto-generated method stub
		if (this.session != null && TextUtils.equals(this.session.getSessionCode(), session.getSessionCode()))
		{
			refreshQueue();
			refreshMediaState();
		}
	}

	@Override
	public void onMediaQueueIn(AirSession session)
	{
		// TODO Auto-generated method stub
		if (this.session != null && TextUtils.equals(this.session.getSessionCode(), session.getSessionCode()))
		{
			Log.i(SessionBoxTalk.class, "onMediaStateListen");
			refreshPttButton();
			if (contextMain != null)
				Util.Toast(contextMain, contextMain.getString(R.string.talk_channel_tip_media_queue_in));
		}
	}

	@Override
	public void onMediaQueueOut(AirSession session)
	{
		// TODO Auto-generated method stub
		if (this.session != null && TextUtils.equals(this.session.getSessionCode(), session.getSessionCode()))
		{
			Log.i(SessionBoxTalk.class, "onMediaStateListen");
			refreshPttButton();
			if (contextMain != null)
				Util.Toast(contextMain, contextMain.getString(R.string.talk_channel_tip_media_queue_out));
		}
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
				refreshMediaState();
			}
			else
			{
				otherSpeakerOn(session);
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
				refreshMediaState();
			}
			else
			{
				otherSpeakerOff(session);
			}
		}
	}
	
	@Override
	public void onMediaStateListenVoice(AirSession session)
	{
		// TODO Auto-generated method stub
		/*
		if (session != null && !TextUtils.equals(this.session.getSessionCode(), session.getSessionCode()))
			otherSpeakerOn(session);
		*/
	}

	@Override
	public void onMediaStateTalkPreparing(AirSession session)
	{
		// TODO Auto-generated method stub
		if (this.session != null && TextUtils.equals(this.session.getSessionCode(), session.getSessionCode()))
		{
			Log.i(SessionBoxTalk.class, "onMediaStateListen");
			refreshPttButton();
		}
	}

	@Override
	public void onMediaStateTalk(AirSession session)
	{
		// TODO Auto-generated method stub
		if (this.session != null && TextUtils.equals(this.session.getSessionCode(), session.getSessionCode()))
		{
			Log.i(SessionBoxTalk.class, "onMediaStateTalk");
			refreshMediaState();
			refreshPttButton();

			tvSpeakerTime.setText("00:00");
			mSpeakingTimeStamp = System.currentTimeMillis();
			AirMmiTimer.getInstance().TimerRegister(contextMain, mSpeakingTimer, false, false, 1000, true, null);
			if (MainActivity.getInstance() != null && MainActivity.getInstance().viewControllerSlideView.isShowMenuLeft())
			{
				MainActivity.getInstance().viewLeft.refreshList();
			}
		}
	}

	@Override
	public void onMediaStateTalkEnd(AirSession session, int reason)
	{
		// TODO Auto-generated method stub
		if (this.session != null && TextUtils.equals(this.session.getSessionCode(), session.getSessionCode()))
		{
			Log.i(SessionBoxTalk.class, "onMediaStateTalkEnd");
			switch (reason)
			{
				case AirtalkeeSessionManager.TALK_FINISH_REASON_EXCEPTION:
					Util.Toast(contextMain, contextMain.getString(R.string.talk_channel_tip_media_exception));
					break;
				case AirtalkeeSessionManager.TALK_FINISH_REASON_LISTEN_ONLY:
					Util.Toast(contextMain, contextMain.getString(R.string.talk_channel_tip_media_listen_only));
					break;
				case AirtalkeeSessionManager.TALK_FINISH_REASON_SPEAKING_FULL:
					Util.Toast(contextMain, contextMain.getString(R.string.talk_channel_tip_media_speak_full));
					break;
				case AirtalkeeSessionManager.TALK_FINISH_REASON_TIMEOUT:
					Util.Toast(contextMain, contextMain.getString(R.string.talk_channel_tip_media_timeout));
					break;
				case AirtalkeeSessionManager.TALK_FINISH_REASON_TIMEUP:
					Util.Toast(contextMain, contextMain.getString(R.string.talk_channel_tip_media_timeup));
					break;
				case AirtalkeeSessionManager.TALK_FINISH_REASON_GRABED:
					Util.Toast(contextMain, contextMain.getString(R.string.talk_channel_tip_media_interruptted));
					break;
				default:
					break;
			}
			refreshMediaState();
			refreshPttButton();

			AirMmiTimer.getInstance().TimerUnregister(contextMain, mSpeakingTimer);
			if (MainActivity.getInstance() != null && MainActivity.getInstance().viewControllerSlideView.isShowMenuLeft())
			{
				MainActivity.getInstance().viewLeft.refreshList();
			}
		}
	}

	@Override
	public void onSessionOutgoingRinging(AirSession session)
	{
		// TODO Auto-generated method stub
		if (this.session != null && TextUtils.equals(this.session.getSessionCode(), session.getSessionCode()))
		{
			//
		}
	}

	@Override
	public void onSessionPresence(AirSession session, final List<AirContact> membersAll, final List<AirContact> membersPresence)
	{
		// TODO Auto-generated method stub
		if (this.session != null && TextUtils.equals(this.session.getSessionCode(), session.getSessionCode()))
		{
			Log.i(MainActivity.class, "UI--onSessionPresence");
			sessionBox.sessionBoxMember.refreshMembers(session, membersAll);
			sessionBox.sessionBoxMember.refreshMemberOnline(membersPresence);
		}
	}

	@Override
	public void onSessionReleased(AirSession session, int reason)
	{
		// TODO Auto-generated method stub
		if (this.session != null && TextUtils.equals(this.session.getSessionCode(), session.getSessionCode()))
		{
			Log.i(MainActivity.class, "UI--onSessionReleased");
			refreshSession();
			refreshPttButton();
			AirtalkeeMessage.getInstance().MessageListMoreClean(session);
			sessionBox.sessionBoxMessage.refreshMessages();
			sessionBox.sessionBoxMember.refreshMembers();
			sessionBox.sessionBoxMember.refreshMemberOnline();
			if (sessionBox.getSessionType() == AirSession.TYPE_DIALOG)
			{
				String prompt = "";
				switch (reason)
				{
					case AirSession.SESSION_RELEASE_REASON_GENERAL:
						prompt = "";
						break;
					case AirSession.SESSION_RELEASE_REASON_ERROR:
						prompt = contextMain.getString(R.string.talk_calling_fail);
						break;
					case AirSession.SESSION_RELEASE_REASON_NOTREACH:
						prompt = contextMain.getString(R.string.talk_calling_offline);
						break;
					case AirSession.SESSION_RELEASE_REASON_BUSY:
						prompt = contextMain.getString(R.string.talk_line_busy);
						break;
					case AirSession.SESSION_RELEASE_REASON_FORBIDDEN:
						prompt = contextMain.getString(R.string.talk_calling_server_deny);
						break;
					case AirSession.SESSION_RELEASE_REASON_FREQUENTLY:
						prompt = contextMain.getString(R.string.talk_calling_frequently);
						break;
					case AirSession.SESSION_RELEASE_REASON_REJECTED:
						prompt = contextMain.getString(R.string.talk_line_reject);
						break;
					case AirSession.SESSION_RELEASE_REASON_NOANSWER:
						prompt = contextMain.getString(R.string.talk_calling_noanswer);
						break;
					default:
						prompt = contextMain.getString(R.string.talk_had_idel);
						break;
				}
				if (!prompt.equals(""))
				{
					Util.Toast(contextMain, prompt);
				}
			}

			videoFinish();
		}
		if (sessionBoxRefreshListener != null)
		{
			sessionBoxRefreshListener.onMmiSessionReleased(session);
		}
	}

	@Override
	public void onSessionEstablishing(AirSession session)
	{
		// TODO Auto-generated method stub
		if (this.session != null && TextUtils.equals(this.session.getSessionCode(), session.getSessionCode()))
		{
			refreshSession();
			refreshPttButton();
			sessionBox.sessionBoxMessage.refreshMessages();
		}
	}

	@Override
	public void onSessionEstablished(AirSession session, boolean isOk)
	{
		// TODO Auto-generated method stub
		if (this.session != null && TextUtils.equals(this.session.getSessionCode(), session.getSessionCode()))
		{
			Log.i(MainActivity.class, "UI--onSessionEstablished");
			refreshSession();
			refreshPttButton();
			sessionBox.sessionBoxMember.refreshMembers();
			sessionBox.sessionBoxMember.refreshMemberOnline();
		}
		if (sessionBoxRefreshListener != null)
		{
			sessionBoxRefreshListener.onMmiSessionEstablished(session);
		}
	}

	@Override
	public void onSessionMemberUpdate(AirSession session, List<AirContact> members, boolean isOk)
	{
		// TODO Auto-generated method stub
		if (this.session != null && TextUtils.equals(this.session.getSessionCode(), session.getSessionCode()))
		{
			sessionBox.sessionBoxMember.refreshMembers();
			sessionBox.sessionBoxMember.refreshMemberOnline();
		}
	}

	@Override
	public void onMediaAudioVisualizerChanged(byte[] values, int spectrumNum)
	{
		// TODO Auto-generated method stub
		mVisualizerView.updateVisualizer(values);
	}


}
