package com.airtalkee.activity;


import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;

import com.airtalkee.R;
import com.airtalkee.Util.ThemeUtil;
import com.airtalkee.Util.Util;
import com.airtalkee.control.AirAccountManager;
import com.airtalkee.control.AirSessionControl;
import com.airtalkee.listener.OnMmiSessionListener;
import com.airtalkee.sdk.AirtalkeeMediaVideoControl;
import com.airtalkee.sdk.AirtalkeeSessionManager;
import com.airtalkee.sdk.OnMediaListener;
import com.airtalkee.sdk.entity.AirContact;
import com.airtalkee.sdk.entity.AirSession;
import com.airtalkee.sdk.util.Log;
import com.airtalkee.sdk.video.Session;
import com.airtalkee.widget.VideoSufaceView;
import com.airtalkee.widget.VideoSufaceView.OnVideoStateChangeListener;


public class VideoSessionActivity extends Activity implements OnClickListener, OnCheckedChangeListener, OnTouchListener,
	OnMmiSessionListener, OnMediaListener
{

	private static VideoSessionActivity mInstance;
	private AirSession session;
	private boolean videoShow = false;
	private TextView tvTitle;
	private ImageView ivLeft, ivRight;
	
	private VideoSufaceView videoSufaceRecord;
	private SurfaceView mSurfacePlayer;
	
	private ImageView videoSettings;
	private RadioGroup videoSettingRadio;
	private FrameLayout videoSettingsLayout;
	private RelativeLayout videoPanel;
	private TextView videoStatusText;
	private ImageView btnTalkVideo;
	private Animation animVideoFull, animVideoSmall;
	private boolean isVideoRecording = false;
	private boolean isVideoRecordSmall = false;
	private boolean isVideoPlaying = true;
	
	public static VideoSessionActivity getInstance()
	{
		return mInstance;
	}

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle); 
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_session_video);
		mInstance = this;

		bundle = getIntent().getExtras();
		if (bundle != null)
		{
			String sessionCode = bundle.getString("sessionCode");
			session = AirtalkeeSessionManager.getInstance().getSessionByCode(sessionCode);
			videoShow = bundle.getBoolean("video", false);
		}
		loadView();
		
		if (videoShow)
		{
			videoRecordStart();
		}
	}
	
	private void loadView()
	{
		tvTitle = (TextView) findViewById(R.id.tv_main_title);
		
		View btnLeft = findViewById(R.id.menu_left_button);
		ivLeft = (ImageView) findViewById(R.id.bottom_left_icon);
		ivLeft.setImageResource(ThemeUtil.getResourceId(R.attr.theme_ic_topbar_back, this) );
		btnLeft.setOnClickListener(this);

		RelativeLayout ivRightLay = (RelativeLayout) findViewById(R.id.talk_menu_right_button);
		ivRight = (ImageView) findViewById(R.id.bottom_right_icon);
		ivRight.setImageResource(R.drawable.ic_topbar_video_open);
		ivRightLay.setOnClickListener(this);
		
		mSurfacePlayer = (SurfaceView)findViewById(R.id.talk_video_surface_player);
		mSurfacePlayer.getHolder().addCallback(null);
		
		videoSufaceRecord = (VideoSufaceView) findViewById(R.id.talk_video_surface_recorder);
		videoSettings = (ImageView) findViewById(R.id.talk_btn_settings);
		videoSettings.setOnClickListener(this);
		videoSettings.setVisibility(View.INVISIBLE);
		videoSettingRadio = (RadioGroup) findViewById(R.id.radio);
		videoSettingRadio.setOnCheckedChangeListener(this);
		videoSettingsLayout = (FrameLayout) findViewById(R.id.video_layout);
		videoPanel = (RelativeLayout) findViewById(R.id.talk_video_panel);
		videoPanel.setVisibility(View.GONE);
		videoStatusText = (TextView) findViewById(R.id.talk_video_status_panel);
		videoStatusText.setVisibility(View.GONE);
		btnTalkVideo = (ImageView) findViewById(R.id.talk_btn_session_on_video);
		btnTalkVideo.setOnTouchListener(this);
		animVideoFull = AnimationUtils.loadAnimation(this, R.anim.video_full);
		animVideoSmall = AnimationUtils.loadAnimation(this, R.anim.video_small);
		findViewById(R.id.talk_btn_choose).setOnClickListener(this);
		findViewById(R.id.talk_btn_choose).setVisibility(View.INVISIBLE);
	}

	@Override
	protected void onPause()
	{
		// TODO Auto-generated method stub
		super.onPause();
		videoRecordFinish();
	}

	@Override
	protected void onResume()
	{
		// TODO Auto-generated method stub
		super.onResume();
		if (session != null)
		{
			tvTitle.setText(session.getDisplayName());
			AirtalkeeSessionManager.getInstance().setOnMediaListener(this);
			AirSessionControl.getInstance().setOnMmiSessionListener(this);
			refreshPttState();
		}
	}

	@Override
	public void finish()
	{
		// TODO Auto-generated method stub
		videoRecordFinish();
		AirtalkeeSessionManager.getInstance().setOnMediaListener(null);
		AirSessionControl.getInstance().setOnMmiSessionListener(null);
		super.finish();
	}
	
	public void videoRecordStart()
	{
		if (AirAccountManager.VIDEO_PORT == 0)
		{
			Util.Toast(this, getString(R.string.talk_video_addr_error));
			AirtalkeeMediaVideoControl.getInstance().VideoAddr();
		}
		else
		{
			int cameraType = Session.CAMERA_EXTERNAL_TYPE_NONE;
			if (videoSufaceRecord.isCameraUsbReady())
			{
				//cameraType = Session.CAMERA_EXTERNAL_TYPE_USB;
				cameraType = Session.CAMERA_EXTERNAL_TYPE_NONE;
			}
			isVideoRecording = true;
			videoSufaceRecord.start(new OnVideoStateChangeListener()
			{
				@Override
				public void onVideoStateChange(boolean isClose)
				{
					// TODO Auto-generated method stub
					if (isClose)
					{
						getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
					}
					else
					{
						getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
					}
				}
			}, cameraType, videoSettingRadio, videoPanel, session, AirAccountManager.VIDEO_IP, AirAccountManager.VIDEO_PORT, 0, 0);
			ivRight.setImageResource(R.drawable.ic_topbar_video_close);
			refreshVideoRecorderStart();
		}
	}
	
	public void videoRecordFinish()
	{
		isVideoRecording = false;
		videoSufaceRecord.finish();
		ivRight.setImageResource(R.drawable.ic_topbar_video_open);
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		refreshVideoRecorderStop();
	}
	
	public void refreshPttState()
	{
		if (session == null)
			return;
		switch (session.getMediaButtonState())
		{
			case AirSession.MEDIA_BUTTON_STATE_IDLE:
			case AirSession.MEDIA_BUTTON_STATE_RELEASING:
				btnTalkVideo.setBackgroundResource(R.drawable.btn_talk_video_idle);
				break;
			case AirSession.MEDIA_BUTTON_STATE_TALKING:
				btnTalkVideo.setBackgroundResource(R.drawable.btn_talk_video_speak);
				break;
			case AirSession.MEDIA_BUTTON_STATE_CONNECTING:
			case AirSession.MEDIA_BUTTON_STATE_REQUESTING:
			case AirSession.MEDIA_BUTTON_STATE_QUEUE:
				btnTalkVideo.setBackgroundResource(R.drawable.btn_talk_video_press);
				break;
		}
		
		if (session.getMediaState() == AirSession.MEDIA_STATE_LISTEN && session.getSpeaker() != null)
		{
			videoStatusText.setText(session.getSpeaker().getDisplayName());
			videoStatusText.setTextColor(Color.WHITE);
			videoStatusText.setVisibility(View.VISIBLE);
		}
		else if (session.getMediaState() == AirSession.MEDIA_STATE_TALK)
		{
			videoStatusText.setText(getString(R.string.talk_me));
			videoStatusText.setTextColor(Color.YELLOW);
			videoStatusText.setVisibility(View.VISIBLE);
		}
		else
		{
			videoStatusText.setVisibility(View.GONE);
		}
	}
	
	private void refreshVideoRecorderStart()
	{
		if (videoSufaceRecord.getVisibility() == View.GONE)
		{
			if (isVideoPlaying)
			{
				videoSufaceRecord.startAnimation(animVideoSmall);
				isVideoRecordSmall = true;
			}
			else
			{
				videoSufaceRecord.startAnimation(animVideoFull);
				isVideoRecordSmall = false;
			}
			videoSufaceRecord.setVisibility(View.VISIBLE);
		}
	}
	
	private void refreshVideoRecorderStop()
	{
		if (videoSufaceRecord.getVisibility() == View.VISIBLE)
		{
			videoSufaceRecord.setVisibility(View.GONE);
		}
	}
	
	private void refreshVideoPlayerStart()
	{
		if (videoSufaceRecord.getVisibility() == View.VISIBLE)
		{
			if (!isVideoRecordSmall)
			{
				videoSufaceRecord.startAnimation(animVideoSmall);
				isVideoRecordSmall = true;
			}
		}
	}
	
	private void refreshVideoPlayerStop()
	{
		if (videoSufaceRecord.getVisibility() == View.VISIBLE)
		{
			if (isVideoRecordSmall)
			{
				videoSufaceRecord.startAnimation(animVideoFull);
				isVideoRecordSmall = false;
			}
		}
	}
	
	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
			case R.id.menu_left_button:
			case R.id.bottom_left_icon:
			{
				finish();
				break;
			}
			case R.id.talk_menu_right_button:
			case R.id.bottom_right_icon:
			{
				if (!isVideoRecording)
				{
					AlertDialog.Builder builder = new AlertDialog.Builder(this);
					builder.setTitle(getString(R.string.talk_channel_create_tip));
					builder.setMessage(getString(R.string.talk_video_tio_to_open));
					builder.setPositiveButton(getString(R.string.talk_ok), new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialog, int which)
						{
							videoRecordStart();
						}
					});
					builder.setNegativeButton(this.getString(R.string.talk_no), null);
					builder.show();
				}
				else
				{
					AlertDialog.Builder builder = new AlertDialog.Builder(this);
					builder.setTitle(getString(R.string.talk_channel_create_tip));
					builder.setMessage(getString(R.string.talk_video_tio_to_close));
					builder.setPositiveButton(getString(R.string.talk_ok), new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialog, int which)
						{
							if (videoShow)
								finish();
							else
								videoRecordFinish();
						}
					});
					builder.setNegativeButton(this.getString(R.string.talk_no), null);
					builder.show();
				}
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
			case R.id.talk_btn_choose:
			{
				isVideoPlaying = !isVideoPlaying;
				if (isVideoPlaying)
					refreshVideoPlayerStart();
				else
					refreshVideoPlayerStop();
				break;
			}
		}
	}
	
	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId)
	{
		// TODO Auto-generated method stub
		videoSettingsLayout.setVisibility(View.GONE);
		videoSufaceRecord.selectQuality(videoSettingRadio);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event)
	{
		// TODO Auto-generated method stub
		if (session != null)
		{
			if (v.getId() == R.id.talk_btn_session_on_video)
			{
				if (session.getSessionState() == AirSession.SESSION_STATE_DIALOG)
				{
					if (event.getAction() == MotionEvent.ACTION_DOWN)
					{
						Log.i(VideoSessionActivity.class, "TalkButton onLongClick TalkRequest!");
						AirtalkeeSessionManager.getInstance().TalkRequest(session, false);
					}
					else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL)
					{
						Log.i(VideoSessionActivity.class, "TalkButton onLongClick TalkRelease!");
						AirtalkeeSessionManager.getInstance().TalkRelease(session);
					}
					refreshPttState();
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public void onSessionOutgoingRinging(AirSession session)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSessionEstablishing(AirSession session)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSessionEstablished(AirSession session, boolean isOk)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSessionReleased(AirSession session, int reason)
	{
		// TODO Auto-generated method stub
		finish();
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
		refreshPttState();
	}

	@Override
	public void onMediaStateTalk(AirSession session)
	{
		// TODO Auto-generated method stub
		refreshPttState();
	}

	@Override
	public void onMediaStateTalkEnd(AirSession session, int reason)
	{
		// TODO Auto-generated method stub
		refreshPttState();
	}

	@Override
	public void onMediaStateListen(AirSession session, AirContact speaker)
	{
		// TODO Auto-generated method stub
		refreshPttState();
	}

	@Override
	public void onMediaStateListenEnd(AirSession session)
	{
		// TODO Auto-generated method stub
		refreshPttState();
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
		refreshPttState();
	}

	@Override
	public void onMediaQueueIn(AirSession session)
	{
		// TODO Auto-generated method stub
		refreshPttState();
	}

	@Override
	public void onMediaQueueOut(AirSession session)
	{
		// TODO Auto-generated method stub
		refreshPttState();
	}


}
