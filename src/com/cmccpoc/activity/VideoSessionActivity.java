package com.cmccpoc.activity;

import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.airtalkee.sdk.AirtalkeeMediaVideoControl;
import com.airtalkee.sdk.AirtalkeeSessionManager;
import com.airtalkee.sdk.OnMediaListener;
import com.airtalkee.sdk.entity.AirContact;
import com.airtalkee.sdk.entity.AirSession;
import com.airtalkee.sdk.util.Log;
import com.airtalkee.sdk.video.Session;
import com.cmccpoc.R;
import com.cmccpoc.Util.Toast;
import com.cmccpoc.Util.Util;
import com.cmccpoc.activity.home.widget.AlertDialog;
import com.cmccpoc.activity.home.widget.AlertDialog.DialogListener;
import com.cmccpoc.control.AirAccountManager;
import com.cmccpoc.control.AirSessionControl;
import com.cmccpoc.listener.OnMmiSessionListener;
import com.cmccpoc.widget.VideoSufaceView;
import com.cmccpoc.widget.VideoSufaceView.OnVideoStateChangeListener;

//import android.app.AlertDialog;

public class VideoSessionActivity extends Activity implements OnClickListener,
		OnTouchListener, OnMmiSessionListener, OnMediaListener, DialogListener
{

	private static VideoSessionActivity mInstance;
	private AirSession session;
	private boolean videoShow = false;

	private VideoSufaceView videoSufaceRecord;
	private SurfaceView mSurfacePlayer;

	// private FrameLayout videoSettingsLayout;
	private RelativeLayout videoPanel;
	private TextView videoStatusText;

	private ImageView btnTalkVideo, icVideoStatus;
	private Animation animVideoFull, animVideoSmall;
	private boolean isVideoRecording = false;
	private boolean isVideoPlaying = true;

	private ImageView ivVideoBtn;
	private TextView tvVideoText;

	public static VideoSessionActivity getInstance()
	{
		return mInstance;
	}

	public boolean getRecordingState()
	{
		return getInstance().isVideoRecording;
	}

	@Override
	protected void onCreate(Bundle bundle)
	{
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
		videoRecordStart();
	}

	private void loadView()
	{
		mSurfacePlayer = (SurfaceView) findViewById(R.id.talk_video_surface_player);
		mSurfacePlayer.getHolder().addCallback(null);

		videoSufaceRecord = (VideoSufaceView) findViewById(R.id.talk_video_surface_recorder);

		// videoSettingsLayout = (FrameLayout) findViewById(R.id.video_layout);
		videoPanel = (RelativeLayout) findViewById(R.id.talk_video_panel);
		videoPanel.setVisibility(View.GONE);
		videoStatusText = (TextView) findViewById(R.id.talk_video_status_panel);
		btnTalkVideo = (ImageView) findViewById(R.id.talk_btn_session_on_video);
		btnTalkVideo.setOnTouchListener(this);
		animVideoFull = AnimationUtils.loadAnimation(this, R.anim.video_full);
		animVideoSmall = AnimationUtils.loadAnimation(this, R.anim.video_small);
		ivVideoBtn = (ImageView) findViewById(R.id.video_record);
		ivVideoBtn.setOnClickListener(this);
		tvVideoText = (TextView) findViewById(R.id.tv_video_status_tip);
		icVideoStatus = (ImageView) findViewById(R.id.talk_video_status_iv);
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
			// tvTitle.setText(session.getDisplayName());
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
			videoSufaceRecord.start(new OnVideoStateChangeListener()
			{
				@Override
				public void onVideoStateChange(boolean isClose)
				{
					if (isClose)
					{
						getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
					}
					else
					{
						getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
					}
				}
			}, cameraType, videoPanel, session, AirAccountManager.VIDEO_IP, AirAccountManager.VIDEO_PORT, 0, 0, false);
			refreshVideoRecorderStart();
		}
	}

	public void videoRecordFinish()
	{
		isVideoRecording = false;
		videoSufaceRecord.finish(true);
		// ivRight.setImageResource(R.drawable.ic_topbar_video_open);
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
				btnTalkVideo.setBackgroundResource(R.drawable.video_talk_normal);
				break;
			case AirSession.MEDIA_BUTTON_STATE_TALKING:
				btnTalkVideo.setBackgroundResource(R.drawable.video_talk_press);
				break;
			case AirSession.MEDIA_BUTTON_STATE_CONNECTING:
			case AirSession.MEDIA_BUTTON_STATE_REQUESTING:
			case AirSession.MEDIA_BUTTON_STATE_QUEUE:
				btnTalkVideo.setBackgroundResource(R.drawable.video_talk_press);
				break;
		}

		if (session.getMediaState() == AirSession.MEDIA_STATE_LISTEN && session.getSpeaker() != null)
		{
			videoStatusText.setText(getString(R.string.talk_video_listen));
			icVideoStatus.setBackgroundResource(R.drawable.media_listen);
			// videoStatusText.setVisibility(View.VISIBLE);
		}
		else if (session.getMediaState() == AirSession.MEDIA_STATE_TALK)
		{
			videoStatusText.setText(getString(R.string.talk_video_me));
			icVideoStatus.setBackgroundResource(R.drawable.media_talk);
			// videoStatusText.setVisibility(View.VISIBLE);
		}
		else
		{
			videoStatusText.setText(getString(R.string.talk_video_idle));
			icVideoStatus.setBackgroundResource(R.drawable.media_talk);
		}
	}

	private void refreshVideoRecorderStart()
	{
		if (videoSufaceRecord.getVisibility() == View.GONE)
		{
			if (isVideoPlaying)
			{
				videoSufaceRecord.startAnimation(animVideoSmall);
			}
			else
			{
				videoSufaceRecord.startAnimation(animVideoFull);
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

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
			case R.id.video_record:
			{
				if (videoShow)
				{
					if (!isVideoRecording)
					{
						String netType = Util.getCurrentNetType();
						if (netType.equals("2g"))
						{
							AlertDialog dialog = new AlertDialog(this, getString(R.string.talk_tools_session_video_confirm), null, this, 0);
							dialog.show();
						}
						else if (netType.equals("null"))
						{
							Toast.makeText1(this, "当前网络不佳，请稍后再试！", Toast.LENGTH_LONG).show();
						}
						else
						{
							isVideoRecording = true;
							videoSufaceRecord.toggleStream();
							ivVideoBtn.setImageResource(R.drawable.ic_session_video_stop);
							tvVideoText.setText(R.string.talk_session_video_stop);
						}
					}
					else
					{
						isVideoRecording = false;
						videoRecordFinish();
						ivVideoBtn.setImageResource(R.drawable.ic_session_video_start);
						tvVideoText.setText(R.string.talk_session_video_start);
						videoRecordStart();
					}
				}
				break;
			}
		}
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

	@Override
	public void onClickOk(int id, Object obj)
	{
		isVideoRecording = true;
		videoSufaceRecord.toggleStream();
		ivVideoBtn.setImageResource(R.drawable.ic_session_video_stop);
		tvVideoText.setText(R.string.talk_session_video_stop);
	}

	@Override
	public void onClickOk(int id, boolean isChecked)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onClickCancel(int id)
	{
		// TODO Auto-generated method stub

	}
}
