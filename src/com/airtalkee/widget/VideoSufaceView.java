package com.airtalkee.widget;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.usb.UsbDevice;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import com.airtalkee.R;
import com.airtalkee.Util.Setting;
import com.airtalkee.Util.Util;
import com.airtalkee.activity.VideoSessionActivity;
import com.airtalkee.activity.home.IMFragment;
import com.airtalkee.activity.home.widget.AlertDialog.DialogListener;
import com.airtalkee.config.Config;
import com.airtalkee.control.AirSessionControl;
import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.AirtalkeeMessage;
import com.airtalkee.sdk.controller.AccountController;
import com.airtalkee.sdk.controller.MessageController;
import com.airtalkee.sdk.entity.AirMessage;
import com.airtalkee.sdk.entity.AirSession;
import com.airtalkee.sdk.listener.CallbackRtspClient;
import com.airtalkee.sdk.listener.CallbackVideoSession;
import com.airtalkee.sdk.video.Session;
import com.airtalkee.sdk.video.SessionBuilder;
import com.airtalkee.sdk.video.codec.VideoQuality;
import com.airtalkee.sdk.video.gl.SurfaceView;
import com.airtalkee.sdk.video.rtsp.RtspClient;

public class VideoSufaceView extends FrameLayout implements OnClickListener,
		CallbackRtspClient, CallbackVideoSession, SurfaceHolder.Callback,
		SensorEventListener, DialogListener, OnCheckedChangeListener
{
	private static final String VIDEO_QUALITY_LOW = "480 * 320";
	private static final String VIDEO_QUALITY_NORMAL = "640 * 480";
	private static final String VIDEO_QUALITY_BEST = "1280 * 720";

	private static String videoRateStr = Setting.getVideoRate();

	// private ImageButton mButtonStart;
	private ImageButton mButtonFlash;
	private ImageButton mButtonCamera;
	private ImageButton mButtonSettings;
	private ImageButton mButtonMic;
	private View parentView;
	private SurfaceView mSurfaceView;
	private ProgressBar mProgressBar;
	private Session mSession;
	public RtspClient mClient;
	private String uri;
	private OnVideoStateChangeListener l;
	private int mVideoWidth = 0;
	private int mVideoHeight = 0;
	private int mFramerate = 0;
	private SensorManager sm = null;
	private Sensor mAccelerometer = null;
	Activity activity;
	boolean pendingUSBConnect0 = false;
	UsbDevice usbConCB0 = null;
	@SuppressWarnings("unused")
	private boolean allowCheckedChangeEvent = true;
	private boolean isCameraUsbReady = false;

	private TextView tv_status;
	private Chronometer ch_time;
	private ImageView iv_back, iv_setting, iv_video_recording;

	private RadioGroup videoRateRadio, videoFpsRadio;
	private View popItemView, popRateView, popFpsView;
	private PopupWindow popItemWindow, popRateWindow, popFpsWindow;// 弹出窗口
	private AirMessage iMessage;
	private String videoTime = "00:00";
	private boolean settingMode = false;

	public VideoSufaceView(Context context)
	{
		super(context);
		onCreate();
	}

	public VideoSufaceView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		onCreate();
	}

	public VideoSufaceView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		onCreate();
	}

	public void onCreate()
	{
		if (isInEditMode())
		{
			return;
		}
		activity = (Activity) this.getContext();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
		{
			LayoutInflater.from(this.getContext()).inflate(R.layout.video_main, this);
			initView();
		}
	}

	public void initView()
	{
		sm = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
		mAccelerometer = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mButtonFlash = (ImageButton) findViewById(R.id.flash);
		mButtonCamera = (ImageButton) findViewById(R.id.camera);
		mButtonSettings = (ImageButton) findViewById(R.id.settings);
		mButtonMic = (ImageButton) findViewById(R.id.mic);
		mSurfaceView = (SurfaceView) findViewById(R.id.surface);
		mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
		mButtonFlash.setOnClickListener(this);
		mButtonCamera.setOnClickListener(this);
		mButtonSettings.setOnClickListener(this);
		mButtonMic.setOnClickListener(this);
		mButtonMic.setTag(false);
		mButtonMic.setEnabled(false);
		mButtonFlash.setTag("off");

		tv_status = (TextView) findViewById(R.id.tv_video_status);
		ch_time = (Chronometer) findViewById(R.id.ch_timer);
		iv_back = (ImageView) findViewById(R.id.iv_video_back);
		iv_back.setOnClickListener(this);
		iv_setting = (ImageView) findViewById(R.id.iv_video_setting);
		iv_setting.setOnClickListener(this);
		iv_video_recording = (ImageView) findViewById(R.id.iv_video_recording);
		initPopupItemWindow();
		initPopupRateWindow();
		initPopupFpsWindow();
	}

	public boolean isCameraUsbReady()
	{
		return isCameraUsbReady;
	}

	public void start(OnVideoStateChangeListener listener, int cameraType, View parent, AirSession session, String serverIp, int serverPort, int screenWidth, int screenHeight, boolean flag)
	{
		this.parentView = parent;
		this.l = listener;
		String uid = AirtalkeeAccount.getInstance().getUserId();
		if (session != null)
		{
			uri = "rtsp://" + serverIp + ":" + serverPort + "/stream-record/source/" + uid;
		}
		else
		{
			Util.Toast(this.getContext(), "当前会话未连接");
			setVisibility(View.GONE);
			return;
		}
		// Configures the SessionBuilder
		mSession = SessionBuilder.getInstance().setContext(this.getContext().getApplicationContext()).setVideoEncoder(SessionBuilder.VIDEO_H264).setSurfaceView(mSurfaceView).setPreviewOrientation(0).setCallback(this).setExternalCameraType(cameraType).build();
		selectQuality(videoRateRadio.getCheckedRadioButtonId(), flag);
		// Configures the RTSP client
		mClient = new RtspClient();
		mClient.setSession(mSession);
		mClient.setSessionCode(session != null ? session.getSessionCode() : "");
		mClient.setCallback(this);
		mSurfaceView.getHolder().addCallback(this);
		mSurfaceView.setVisibility(View.VISIBLE);
		this.setVisibility(View.VISIBLE);
	}

	public void finish(boolean flag)
	{
		if (mClient != null && mClient.isStreaming())
		{
			if (sm != null)
				sm.unregisterListener(this);
			mProgressBar.setVisibility(View.GONE);
			videoTime = ch_time.getText().toString();
			ch_time.stop();
			ch_time.setBase(SystemClock.elapsedRealtime());
			this.setVisibility(View.GONE);
			mClient.stopStream();
			if (flag)
			{
				mSession.release();
			}
			mSurfaceView.getHolder().removeCallback(this);
		}
	}

	public int getVideoWidth()
	{
		return mVideoWidth;
	}

	public int getVideoHeight()
	{
		return mVideoHeight;
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
			case R.id.flash:
				if (mButtonFlash.getTag().equals("on"))
				{
					mButtonFlash.setTag("off");
					mButtonFlash.setImageResource(R.drawable.ic_flash_on_holo_light);
				}
				else
				{
					mButtonFlash.setImageResource(R.drawable.ic_flash_off_holo_light);
					mButtonFlash.setTag("on");
				}
				mSession.toggleFlash();
				break;
			case R.id.camera:
				mSession.switchCamera();
				break;
			case R.id.iv_video_back:
			{
				/*
				if (null != VideoSessionActivity.getInstance())
				{
					if (VideoSessionActivity.getInstance().getRecordingState())
					{
						AlertDialog builder = new AlertDialog(getContext(), "", getContext().getString(R.string.talk_video_tio_to_close), getContext().getString(R.string.talk_no), getContext().getString(R.string.talk_ok), this, 0);
						builder.show();
					}
				}*/
				VideoSessionActivity.getInstance().finish();
				finish(true);
				break;
			}
			case R.id.iv_video_setting:
			{
				if (!settingMode)
				{
					if (Config.model.startsWith("SM"))// 三星note
					{
						popItemWindow.showAtLocation(popItemView, Gravity.RIGHT | Gravity.TOP, 5, 300);
					}
					else
					{
						popItemWindow.showAtLocation(popItemView, Gravity.RIGHT | Gravity.TOP, 5, 150);
					}
					iv_setting.setImageResource(R.drawable.ic_session_video_setting_orange);
					settingMode = true;
				}
				else
				{
					popItemWindow.dismiss();
					popFpsWindow.dismiss();
					popRateWindow.dismiss();
					iv_setting.setImageResource(R.drawable.ic_session_video_setting_normal);
					((Button) popItemView.findViewById(R.id.button_rate)).setTextColor(getResources().getColorStateList(R.color.white));
					((Button) popItemView.findViewById(R.id.button_fps)).setTextColor(getResources().getColorStateList(R.color.white));
					settingMode = false;
				}
				break;
			}
			case R.id.button_rate:
			{
				popRateWindow.showAtLocation(popRateView, Gravity.RIGHT | Gravity.TOP, 500, 300);
				popFpsWindow.dismiss();
				((Button) popItemView.findViewById(R.id.button_rate)).setTextColor(0X7fFF9400);
				((Button) popItemView.findViewById(R.id.button_fps)).setTextColor(getResources().getColorStateList(R.color.white));
				for (int index = 0; index < videoRateRadio.getChildCount(); index++)
				{
					if(videoRateRadio.getChildAt(index) instanceof RadioButton)
					{
						RadioButton rbRate = (RadioButton) videoRateRadio.getChildAt(index);
						if (rbRate.getId() == videoRateRadio.getCheckedRadioButtonId())
						{
							rbRate.setTextColor(0X7fFF9400);
							rbRate.setChecked(true);
						}
						else
						{
							rbRate.setTextColor(getResources().getColorStateList(R.color.white));
							rbRate.setChecked(false);
						}
					}
				}
				break;
			}
			case R.id.button_fps:
			{
				popFpsWindow.showAtLocation(popFpsView, Gravity.RIGHT | Gravity.TOP, 500, 300);
				((Button) popItemView.findViewById(R.id.button_rate)).setTextColor(getResources().getColorStateList(R.color.white));
				((Button) popItemView.findViewById(R.id.button_fps)).setTextColor(0X7fff9400);
				popRateWindow.dismiss();
				for (int i = 0; i < videoFpsRadio.getChildCount(); i++)
				{
					if(videoFpsRadio.getChildAt(i) instanceof RadioButton)
					{
						RadioButton rbFps = (RadioButton) videoFpsRadio.getChildAt(i);
						if (rbFps.getId() == videoFpsRadio.getCheckedRadioButtonId())
						{
							rbFps.setTextColor(0X7fFF9400);
							rbFps.setChecked(true);
						}
						else
						{
							rbFps.setTextColor(getResources().getColorStateList(R.color.white));
							rbFps.setChecked(false);
						}
					}
				}
				break;
			}
		}
	}

	private void initPopupItemWindow()
	{
		popItemView = LayoutInflater.from(getContext()).inflate(R.layout.layout_popup_window_video_item, null);
		popItemWindow = new PopupWindow(popItemView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		popItemWindow.setOutsideTouchable(true);
		popItemView.findViewById(R.id.button_rate).setOnClickListener(this);
		popItemView.findViewById(R.id.button_fps).setOnClickListener(this);
	}

	private void initPopupRateWindow()
	{
		popRateView = LayoutInflater.from(getContext()).inflate(R.layout.layout_popup_window_video_settings, null);
		popRateWindow = new PopupWindow(popRateView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		popRateWindow.setOutsideTouchable(true);
		videoRateRadio = (RadioGroup) popRateView.findViewById(R.id.radio_rate_panel);
		videoRateStr = Setting.getVideoRate();
		if (VIDEO_QUALITY_LOW.equals(videoRateStr))
		{
			videoRateStr = VIDEO_QUALITY_LOW;
			RadioButton rb = (RadioButton) videoRateRadio.findViewById(R.id.radio_low);
			rb.setChecked(true);
		}
		else if (VIDEO_QUALITY_NORMAL.equals(videoRateStr))
		{
			videoRateStr = VIDEO_QUALITY_NORMAL;
			RadioButton rb = (RadioButton) videoRateRadio.findViewById(R.id.radio_normal);
			rb.setChecked(true);
		}
		else if (VIDEO_QUALITY_BEST.equals(videoRateStr))
		{
			videoRateStr = VIDEO_QUALITY_BEST;
			RadioButton rb = (RadioButton) videoRateRadio.findViewById(R.id.radio_best);
			rb.setChecked(true);
		}
		videoRateRadio.setOnCheckedChangeListener(this);
	}

	private void initPopupFpsWindow()
	{
		popFpsView = LayoutInflater.from(getContext()).inflate(R.layout.layout_popup_window_video_fps, null);
		popFpsWindow = new PopupWindow(popFpsView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		popFpsWindow.setOutsideTouchable(true);
		videoFpsRadio = (RadioGroup) popFpsView.findViewById(R.id.radio_fps_panel);
		int currentFps = Setting.getVideoFrameRate();
		switch (currentFps)
		{
			case 10:
				((RadioButton) videoFpsRadio.findViewById(R.id.radio_fps_10)).setChecked(true);
				break;
			case 15:
				((RadioButton) videoFpsRadio.findViewById(R.id.radio_fps_15)).setChecked(true);
				break;
			case 20:
				((RadioButton) videoFpsRadio.findViewById(R.id.radio_fps_20)).setChecked(true);
				break;
			case 25:
				((RadioButton) videoFpsRadio.findViewById(R.id.radio_fps_25)).setChecked(true);
				break;
			case 30:
				((RadioButton) videoFpsRadio.findViewById(R.id.radio_fps_30)).setChecked(true);
				break;
			default:
				((RadioButton) videoFpsRadio.findViewById(R.id.radio_fps_20)).setChecked(true);
				break;
		}
		videoFpsRadio.setOnCheckedChangeListener(this);
	}

	public void selectQuality(int radioButtonId, boolean flag)
	{
		RadioButton button = (RadioButton) parentView.findViewById(radioButtonId);
		if (button == null)
			return;
		int bitrate = 0;
		mFramerate = 20;
		switch (radioButtonId)
		{
			case R.id.radio_low:
				mVideoWidth = 480;
				mVideoHeight = 320;
				bitrate = 300;
				break;
			case R.id.radio_normal:
				mVideoWidth = 640;
				mVideoHeight = 480;
				bitrate = 500;
				break;
			case R.id.radio_best:
				mVideoWidth = 1280;
				mVideoHeight = 720;
				bitrate = 800;
				break;
			default:
				mVideoWidth = 640;
				mVideoHeight = 480;
				bitrate = 500;
				break;
		}

		//mSession.setVideoQuality(new VideoQuality(mVideoWidth, mVideoHeight, mFramerate, bitrate * 1000));
		if (flag)
		{
			Util.Toast(this.getContext(), button.getText().toString() + "\n修改成功，再次进入视频回传后生效");
		}
		else
		{
			Util.Toast(this.getContext(), button.getText().toString());
		}
		Setting.setVideoResolutionWidth(mVideoWidth);
		Setting.setVideoResolutionHeight(mVideoHeight);
		popRateWindow.dismiss();
		button.setTextColor(0X7fFF9400);
	}

	private void enableUI()
	{
		mButtonCamera.setEnabled(true);
	}

	// Connects/disconnects to the RTSP server and starts/stops the stream
	public void toggleStream()
	{
		finish(false);
		mProgressBar.setVisibility(View.GONE);
		if (!mClient.isStreaming())
		{
			String ip, port, path;
			// We parse the URI written in the Editext
			Pattern uri = Pattern.compile("rtsp://(.+):(\\d*)/(.+)");
			Matcher m = uri.matcher(this.uri);
			m.find();
			ip = m.group(1);
			port = m.group(2);
			path = m.group(3);
			mClient.setCredentials("username", "password");
			mClient.setServerAddress(ip, Integer.parseInt(port));
			mClient.setStreamPath("/" + path);
			mClient.startStream();
		}
	}

	@Override
	public void onBitrateUpdate(long bitrate)
	{
	}

	@Override
	public void onPreviewStarted()
	{
		VideoQuality quality = mSession.getVideoTrack().getVideoQuality();
		if (mSession.getCamera() == CameraInfo.CAMERA_FACING_FRONT)
		{
			mButtonFlash.setEnabled(false);
			mButtonFlash.setTag("off");
			mButtonFlash.setImageResource(R.drawable.ic_flash_on_holo_light);
		}
		else
		{
			mButtonFlash.setEnabled(true);
		}
	}

	@Override
	public void onSessionConfigured()
	{

	}

	@Override
	public void onSessionStarted()
	{
		enableUI();
		// mButtonStart.setImageResource(R.drawable.ic_switch_video_active);
		mProgressBar.setVisibility(View.GONE);
		iv_back.setVisibility(View.INVISIBLE);
		iv_setting.setVisibility(View.INVISIBLE);
		this.setVisibility(View.VISIBLE);
		if (this.l != null)
		{
			l.onVideoStateChange(false);
		}
		if (sm != null)
			sm.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
		mAutoFocus = true;
		mSession.autoFocus(callback);
		ch_time.start();
		ch_time.setBase(SystemClock.elapsedRealtime());
		tv_status.setText(getContext().getString(R.string.talk_video_uploading));
		iv_video_recording.setVisibility(View.VISIBLE);
	}

	@Override
	public void onSessionStopped()
	{
		enableUI();
		if (sm != null)
			sm.unregisterListener(this);
		iv_back.setVisibility(View.VISIBLE);
		iv_setting.setVisibility(View.VISIBLE);
		mProgressBar.setVisibility(View.GONE);
		if (this.l != null)
		{
			l.onVideoStateChange(true);
		}
		iv_video_recording.setVisibility(View.GONE);
		AirSession session = AirSessionControl.getInstance().getCurrentChannelSession();
		AirtalkeeMessage.getInstance().MessageRemove(session.getSessionCode(), iMessage);
		if (session != null)
		{
			String msg = getContext().getString(R.string.talk_session_video_message_time) + videoTime;
			if (msg != null && !msg.trim().equals(""))
			{
				iMessage = MessageController.messageGenerate(session, "TEMP_VIDEO_SESSION", AirMessage.TYPE_SESSION_VIDEO, AccountController.getUserInfo(), msg);
				//AirtalkeeMessage.getInstance().MessageSessionVideoSend(session, msg, true);
				IMFragment.getInstance().refreshMessages();
			}
		}
		
	}

	@Override
	public void onSessionError(int reason, int streamType, Exception e)
	{
		mProgressBar.setVisibility(View.GONE);
		// tv_status.setText(getContext().getString(R.string.talk_video_connect_fail));
		if (sm != null)
			sm.unregisterListener(this);
		switch (reason)
		{
			case Session.ERROR_CAMERA_ALREADY_IN_USE:
				break;
			case Session.ERROR_CAMERA_HAS_NO_FLASH:
				mButtonFlash.setImageResource(R.drawable.ic_flash_on_holo_light);
				mButtonFlash.setTag("off");
				break;
			case Session.ERROR_INVALID_SURFACE:
				break;
			case Session.ERROR_STORAGE_NOT_READY:
				break;
			case Session.ERROR_CONFIGURATION_NOT_SUPPORTED:
				VideoQuality quality = mSession.getVideoTrack().getVideoQuality();
				e.printStackTrace();
				return;
			case Session.ERROR_OTHER:
				break;
		}

		if (e != null)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void onRtspUpdate(int message, Exception e)
	{
		switch (message)
		{
			case RtspClient.ERROR_CONNECTION_FAILED:
			case RtspClient.ERROR_WRONG_CREDENTIALS:
				mProgressBar.setVisibility(View.GONE);
				enableUI();
				e.printStackTrace();
				break;
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
	{
		mSession.startPreview();
		autoFoucs();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder)
	{
		mSession.startPreview();
		autoFoucs();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder)
	{
		mClient.stopStream();
	}

	public interface OnVideoStateChangeListener
	{
		public void onVideoStateChange(boolean isClose);

	}

	public void autoFoucs()
	{
		if (mSession != null)
			mSession.autoFocus(callback);
	}

	float mLastX, mLastY, mLastZ;
	boolean mInitialized = false, mAutoFocus = true;

	private AutoFocusCallback callback = new AutoFocusCallback()
	{
		@Override
		public void onAutoFocus(boolean success, Camera camera)
		{
			// TODO Auto-generated method stub
			mAutoFocus = true;
		}
	};

	@Override
	public void onSensorChanged(SensorEvent event)
	{
		// TODO Auto-generated method stub
		float x = event.values[0];
		float y = event.values[1];
		float z = event.values[2];
		if (!mInitialized)
		{
			mLastX = x;
			mLastY = y;
			mLastZ = z;
			mInitialized = true;
		}
		float deltaX = Math.abs(mLastX - x);
		float deltaY = Math.abs(mLastY - y);
		float deltaZ = Math.abs(mLastZ - z);
		com.airtalkee.sdk.util.Log.d(VideoSufaceView.class, "diffX=" + deltaX + ",diffY=" + deltaY + ",deltaZ=" + deltaZ);
		if (deltaX > 0.5 && mAutoFocus)
		{
			mAutoFocus = false;
			autoFoucs();
		}
		if (deltaY > 0.5 && mAutoFocus)
		{
			mAutoFocus = false;
			autoFoucs();
		}
		if (deltaZ > 0.5 && mAutoFocus)
		{
			mAutoFocus = false;
			autoFoucs();
		}

		mLastX = x;
		mLastY = y;
		mLastZ = z;
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onClickOk(int id, Object obj)
	{
		VideoSessionActivity.getInstance().finish();
	}

	@Override
	public void onClickCancel(int id)
	{

	}

	@Override
	public void onClickOk(int id, boolean isChecked)
	{
		// TODO Auto-generated method stub

	}

	private Handler mHandler = new Handler();

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId)
	{
		// popWindow.dismiss();
		switch (group.getId())
		{
			case R.id.radio_rate_panel:
			{
				selectQuality(videoRateRadio.getCheckedRadioButtonId(), true);
				switch (checkedId)
				{
					case R.id.radio_low:
						videoRateStr = VIDEO_QUALITY_LOW;
						break;
					case R.id.radio_normal:
						videoRateStr = VIDEO_QUALITY_NORMAL;
						break;
					case R.id.radio_best:
						videoRateStr = VIDEO_QUALITY_BEST;
						break;
				}
				break;
			}
			case R.id.radio_fps_panel:
			{
				initRgFps();
				popFpsWindow.dismiss();
				break;
			}
		}
	}
	
	private void initRgFps()
	{
		for (int i = 0; i < videoFpsRadio.getChildCount(); i++)
		{
			if(videoFpsRadio.getChildAt(i) instanceof RadioButton)
			{
				RadioButton rbFps = (RadioButton) videoFpsRadio.getChildAt(i);
				if (rbFps.getId() == videoFpsRadio.getCheckedRadioButtonId())
				{
					rbFps.setTextColor(0X7fFF9400);
					rbFps.setChecked(true);
					String rbName = rbFps.getText().toString();
					if(rbName.contains("10"))
					{
						Setting.setVideoFrameRate(10);
					}
					else if(rbName.contains("15"))
					{
						Setting.setVideoFrameRate(15);
					}
					else if(rbName.contains("20"))
					{
						Setting.setVideoFrameRate(20);
					}
					else if(rbName.contains("25"))
					{
						Setting.setVideoFrameRate(25);
					}
					else if(rbName.contains("30"))
					{
						Setting.setVideoFrameRate(30);
					}
					else
					{
						Setting.setVideoFrameRate(20);
					}
				}
				else
				{
					rbFps.setTextColor(getResources().getColorStateList(R.color.white));
					rbFps.setChecked(false);
				}
			}
		}
	}
}
