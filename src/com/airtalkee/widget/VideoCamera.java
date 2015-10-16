package com.airtalkee.widget;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;

import com.airtalkee.R;
import com.airtalkee.sdk.util.Utils;
import com.airtalkee.sdk.video.record.VideoQuality;
import com.airtalkee.sdk.video.record.VideoSession;
import com.airtalkee.sdk.video.record.VideoSession.Callback;

public class VideoCamera extends Activity implements 
OnClickListener,Callback {
	public static String EXTRA_VIDEO_PATH ="extra_video_path";
	private ImageView mButtonStart;
	private ImageView mButtonCamera;
	private ImageView mButtonFlash;
	private Chronometer chronometer;
	private TextView tvClose;
	private TextView tvSure;
	private SurfaceView mSurfaceView;
	private VideoSession session;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState); 
		
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.video_camera);
		session = VideoSession.newInstance(VideoQuality.QUALITY_480P);
		mButtonStart = (ImageView) findViewById(R.id.start);
		mButtonCamera = (ImageView) findViewById(R.id.camera);
		mButtonFlash =(ImageView)findViewById(R.id.flash);
		mSurfaceView = (SurfaceView) findViewById(R.id.surface);
		chronometer = (Chronometer)findViewById(R.id.chronometer1);
		tvClose = (TextView)findViewById(R.id.close);
		tvSure = (TextView)findViewById(R.id.sure);
		
		mButtonStart.setOnClickListener(this);
		mButtonCamera.setOnClickListener(this);
		mButtonFlash.setOnClickListener(this);
		tvClose.setOnClickListener(this);
		tvSure.setOnClickListener(this);
		session.setCallback(this);
		session.setSurfaceView(mSurfaceView);
		session.startPreview();
	}


	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.flash:
			toggleFlash();
			break;
		case R.id.start:
			toggleStream();
			break;
		case R.id.camera:
			session.switchCamera();
			break;
		case R.id.close:
			setResult(RESULT_CANCELED);
			finish();
			break;
		case R.id.sure:
			String path = session.getVideoFilePath();
			Intent data = new Intent();
			data.putExtra(EXTRA_VIDEO_PATH, path);
			setResult(RESULT_OK, data);
			finish();
			break;
		}
	}
	
	public void refreshStartButton()
	{
		switch(session.getState())
		{
			case VideoSession.STATE_STARTED:
				mButtonStart.setImageResource(R.drawable.ic_switch_video_active1);
				chronometer.start();
				chronometer.setBase(SystemClock.elapsedRealtime());
				mButtonCamera.setVisibility(View.GONE);
				mButtonFlash.setVisibility(View.GONE);
				tvClose.setVisibility(View.GONE);
				tvSure.setVisibility(View.GONE);
				chronometer.setVisibility(View.VISIBLE);
				break;
			case VideoSession.STATE_STOPPED:
				mButtonStart.setImageResource(R.drawable.ic_switch_video_normal);
				chronometer.setVisibility(View.GONE);
				mButtonFlash.setVisibility(View.VISIBLE);
				mButtonCamera.setVisibility(View.VISIBLE);
				tvSure.setVisibility(View.VISIBLE);
				tvClose.setVisibility(View.VISIBLE);
				chronometer.stop();
				chronometer.setBase(SystemClock.elapsedRealtime());
				break;
		}
	}

	@Override
	public void onDestroy(){
		super.onDestroy();
		session.release();
		session = null;
//		mSurfaceView.getHolder().removeCallback(this);
	}
	
	long currentMillis =0;

	public void toggleStream() {
		
		if(Utils.getCurrentTimeInMillis() - currentMillis >2000)
		{
			if(session.getState() == VideoSession.STATE_STOPPED)
				session.startRecord();
			else
				session.stopRecord();
			
			currentMillis = Utils.getCurrentTimeInMillis();
		}
		//else
			//Toast.makeText(this, "Frequent operation!", Toast.LENGTH_SHORT).show();
	}
	
	private void refreshFlashState()
	{
		if(session.getFlashState())
			mButtonFlash.setImageResource(R.drawable.ic_flash_on_holo_light);
		else
			mButtonFlash.setImageResource(R.drawable.ic_flash_off_holo_light);
	}
	
	public void toggleFlash()
	{
		session.toggleFlash();
	}


	@Override
	public void onSessionError(int reason, int streamType, Exception e) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onPreviewStarted() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onCameraSwitched(int cameraId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSessionStarted() {
		// TODO Auto-generated method stub
		refreshStartButton();
	}


	@Override
	public void onSessionStopped() {
		// TODO Auto-generated method stub
		refreshStartButton();
	}


	@Override
	public void onFlashToggle() {
		// TODO Auto-generated method stub
		refreshFlashState();
	}

}
