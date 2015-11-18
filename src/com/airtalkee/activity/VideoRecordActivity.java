package com.airtalkee.activity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.hardware.Camera.CameraInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import com.airtalkee.R;
import com.airtalkee.Util.Util;
import com.airtalkee.sdk.video.Session;
import com.airtalkee.sdk.video.SessionBuilder;
import com.airtalkee.sdk.video.audio.AudioQuality;
import com.airtalkee.sdk.video.codec.VideoQuality;
import com.airtalkee.sdk.video.gl.SurfaceView;
import com.airtalkee.sdk.video.rtsp.RtspClient;

public class VideoRecordActivity extends Activity implements OnClickListener,
		RtspClient.Callback, Session.Callback, SurfaceHolder.Callback,
		OnCheckedChangeListener
{

	public final static String TAG = "MainActivity";

	private Button mButtonSave;
	private Button mButtonVideo;
	private ImageButton mButtonStart;
	private ImageButton mButtonFlash;
	private ImageButton mButtonCamera;
	private ImageButton mButtonSettings;
	private ImageButton mButtonMic;
	private RadioGroup mRadioGroup;
	private FrameLayout mLayoutVideoSettings;
	private FrameLayout mLayoutServerSettings;
	private SurfaceView mSurfaceView;
	private TextView mTextBitrate;
	private EditText mEditTextURI;
	private EditText mEditTextPassword;
	private EditText mEditTextUsername;
	private ProgressBar mProgressBar;
	private Session mSession;
	private RtspClient mClient;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.video_main);

		mButtonVideo = (Button) findViewById(R.id.video);
		mButtonSave = (Button) findViewById(R.id.save);
		mButtonStart = (ImageButton) findViewById(R.id.start);
		mButtonFlash = (ImageButton) findViewById(R.id.flash);
		mButtonCamera = (ImageButton) findViewById(R.id.camera);
		mButtonSettings = (ImageButton) findViewById(R.id.settings);
		mButtonMic = (ImageButton) findViewById(R.id.mic);
		mSurfaceView = (SurfaceView) findViewById(R.id.surface);
		mEditTextURI = (EditText) findViewById(R.id.uri);
		mEditTextUsername = (EditText) findViewById(R.id.username);
		mEditTextPassword = (EditText) findViewById(R.id.password);
		mTextBitrate = (TextView) findViewById(R.id.bitrate);
		mLayoutVideoSettings = (FrameLayout) findViewById(R.id.video_layout);
		mLayoutServerSettings = (FrameLayout) findViewById(R.id.server_layout);
		mRadioGroup = (RadioGroup) findViewById(R.id.radio);
		mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);

		mRadioGroup.setOnCheckedChangeListener(this);
		mRadioGroup.setOnClickListener(this);

		mButtonStart.setOnClickListener(this);
		mButtonSave.setOnClickListener(this);
		mButtonFlash.setOnClickListener(this);
		mButtonCamera.setOnClickListener(this);
		mButtonVideo.setOnClickListener(this);
		mButtonSettings.setOnClickListener(this);
		mButtonMic.setOnClickListener(this);
		mButtonMic.setTag(false);
		mButtonMic.setEnabled(false);
		mButtonFlash.setTag("off");

		SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(VideoRecordActivity.this);
		// if (mPrefs.getString("uri", null) != null)
		mLayoutServerSettings.setVisibility(View.GONE);
		mEditTextPassword.setText(mPrefs.getString("password", "123456"));

		// mEditTextURI.setText(mPrefs.getString("uri",
		// "rtsp://121.40.177.113:1935/live/test.stream"));//移动项目
		// mEditTextUsername.setText(mPrefs.getString("username",
		// "haolian"));//移动项目
		// mEditTextURI.setText(mPrefs.getString("uri",
		// "rtsp://106.37.190.22:1935/live/test.stream"));//合纵思壮

		/*
		 * String uid = AirtalkeeAccount.getInstance().getUserId(); AirSession
		 * session = null; if(session != null) { String sid =
		 * session.getSessionCode(); if(session.getType() ==
		 * AirSession.TYPE_CHANNEL) { sid =
		 * session.getSessionCode().substring(1,
		 * session.getSessionCode().length()); }
		 * 
		 * String uri
		 * ="rtsp://106.37.190.22:554/"+uid+"/"+session.getType()+"/"+sid+"/0";
		 * mEditTextURI.setText(uri);//合纵思壮 } else { Util.Toast(this,
		 * "当前会话未连接"); finish(); }
		 */

		mEditTextUsername.setText(mPrefs.getString("username", "weptt"));// 合纵思壮

		// Configures the SessionBuilder
		mSession = SessionBuilder.getInstance().setContext(getApplicationContext()).setAudioEncoder(SessionBuilder.AUDIO_AAC).setAudioQuality(new AudioQuality(8000, 16000)).setVideoEncoder(SessionBuilder.VIDEO_H264).setSurfaceView(mSurfaceView)

		.setPreviewOrientation(0).setCallback(this).build();

		// Configures the RTSP client
		mClient = new RtspClient();
		mClient.setSession(mSession);
		mClient.setCallback(this);

		// Use this to force streaming with the MediaRecorder API
		// mSession.getVideoTrack().setStreamingMethod(MediaStream.MODE_MEDIARECORDER_API);

		// Use this to stream over TCP, EXPERIMENTAL!
		// mClient.setTransportMode(RtspClient.TRANSPORT_TCP);

		// Use this if you want the aspect ratio of the surface view to
		// respect the aspect ratio of the camera preview
		// mSurfaceView.setAspectRatioMode(SurfaceView.ASPECT_RATIO_PREVIEW);

		mSurfaceView.getHolder().addCallback(this);

		selectQuality();

	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId)
	{
		mLayoutVideoSettings.setVisibility(View.GONE);
		// mLayoutServerSettings.setVisibility(View.VISIBLE);
		selectQuality();
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
			case R.id.start:
				mLayoutServerSettings.setVisibility(View.GONE);
				toggleStream();
				break;
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
			case R.id.settings:

				if (mLayoutVideoSettings.getVisibility() != View.GONE)
				{
					mLayoutVideoSettings.setVisibility(View.GONE);
				}
				else
				{
					mRadioGroup.clearCheck();
					mLayoutVideoSettings.setVisibility(View.VISIBLE);
				}

				break;
			case R.id.video:

				break;
			case R.id.save:
				mLayoutServerSettings.setVisibility(View.GONE);
				break;
			case R.id.mic:
				Boolean b = (Boolean) v.getTag();
				if (b)
				{
					mButtonMic.setImageResource(R.drawable.ic_microphone_off);
					// mSession.stopAudioTrack();
				}
				else
				{
					// mSession.startAudioTrack();
					mButtonMic.setImageResource(R.drawable.ic_microphone_on);
				}
				mButtonMic.setTag(!b);
				break;
		}
	}

	@Override
	public void onBackPressed()
	{
		// TODO Auto-generated method stub
		super.onBackPressed();
		// System.exit(0);
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();

		mClient.release();
		mSession.release();
		mSurfaceView.getHolder().removeCallback(this);

	}

	private void selectQuality()
	{
		int id = mRadioGroup.getCheckedRadioButtonId();
		RadioButton button = (RadioButton) findViewById(id);
		if (button == null)
			return;

		String text = button.getText().toString();
		Pattern pattern = Pattern.compile("(\\d+)x(\\d+)\\D+(\\d+)\\D+(\\d+)");
		Matcher matcher = pattern.matcher(text);

		matcher.find();
		int width = Integer.parseInt(matcher.group(1));
		int height = Integer.parseInt(matcher.group(2));
		int framerate = Integer.parseInt(matcher.group(3));
		int bitrate = Integer.parseInt(matcher.group(4)) * 1000;

		mSession.setVideoQuality(new VideoQuality(width, height, framerate, bitrate));
		Util.Toast(this, ((RadioButton) findViewById(id)).getText().toString());
		Log.d(TAG, "Selected resolution: " + width + "x" + height);
	}

	private void enableUI()
	{
		mButtonStart.setEnabled(true);
		mButtonCamera.setEnabled(true);
	}

	// Connects/disconnects to the RTSP server and starts/stops the stream
	public void toggleStream()
	{
		mProgressBar.setVisibility(View.VISIBLE);
		if (!mClient.isStreaming())
		{
			String ip, port, path;

			// We save the content user inputs in Shared Preferences
			SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(VideoRecordActivity.this);
			Editor editor = mPrefs.edit();
			editor.putString("uri", mEditTextURI.getText().toString());
			editor.putString("password", mEditTextPassword.getText().toString());
			editor.putString("username", mEditTextUsername.getText().toString());
			editor.commit();

			// We parse the URI written in the Editext
			Pattern uri = Pattern.compile("rtsp://(.+):(\\d*)/(.+)");
			Matcher m = uri.matcher(mEditTextURI.getText());
			m.find();
			ip = m.group(1);
			port = m.group(2);
			path = m.group(3);

			mClient.setCredentials(mEditTextUsername.getText().toString(), mEditTextPassword.getText().toString());
			mClient.setServerAddress(ip, Integer.parseInt(port));
			mClient.setStreamPath("/" + path);
			mClient.startStream();

		}
		else
		{
			// Stops the stream and disconnects from the RTSP server
			mClient.stopStream();
		}
	}

	private void logError(final String msg)
	{
		// Displays a popup to report the eror to the user
		AlertDialog.Builder builder = new AlertDialog.Builder(VideoRecordActivity.this);
		builder.setMessage(msg).setPositiveButton("OK", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int id)
			{
			}
		});
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	@Override
	public void onBitrateUpdate(long bitrate)
	{
		mTextBitrate.setText("" + bitrate / 1000 + " kbps");
	}

	@Override
	public void onPreviewStarted()
	{
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
		mButtonStart.setImageResource(R.drawable.ic_switch_video_active);
		mButtonMic.setTag(true);
		mButtonMic.setEnabled(true);
		mButtonMic.setImageResource(R.drawable.ic_microphone_on);
		mProgressBar.setVisibility(View.GONE);
	}

	@Override
	public void onSessionStopped()
	{
		enableUI();
		mButtonStart.setImageResource(R.drawable.ic_switch_video);
		mProgressBar.setVisibility(View.GONE);
		mButtonMic.setEnabled(false);
		mButtonMic.setTag(false);
		mButtonMic.setImageResource(R.drawable.ic_microphone_off);
	}

	@Override
	public void onSessionError(int reason, int streamType, Exception e)
	{
		mProgressBar.setVisibility(View.GONE);
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
				logError("The following settings are not supported on this phone: " + quality.toString() + " " + "(" + e.getMessage() + ")");
				e.printStackTrace();
				return;
			case Session.ERROR_OTHER:
				break;
		}

		if (e != null)
		{
			logError(e.getMessage());
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
				logError(e.getMessage());
				e.printStackTrace();
				break;
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
	{

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder)
	{
		try
		{
			mSession.startPreview();
		}
		catch (Exception e)
		{
			Toast.makeText(this, "Cammera Connect error!", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder)
	{
		mClient.stopStream();
	}
}
