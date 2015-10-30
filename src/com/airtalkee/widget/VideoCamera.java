package com.airtalkee.widget;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.airtalkee.R;
import com.airtalkee.activity.MenuReportAsPicActivity;
import com.airtalkee.activity.MenuReportAsVidActivity;
import com.airtalkee.sdk.util.Utils;
import com.airtalkee.sdk.video.record.VideoQuality;
import com.airtalkee.sdk.video.record.VideoSession;
import com.airtalkee.sdk.video.record.VideoSession.Callback;

public class VideoCamera extends Activity implements OnClickListener, Callback
{
	public static String EXTRA_VIDEO_PATH = "extra_video_path";
	private ImageView mButtonStart;
	private ImageView mButtonCamera;
	private ImageView mButtonFlash;
	private Chronometer chronometer;
	private ImageView tvClose;
	private SurfaceView mSurfaceView;
	private VideoSession session;
	private ImageView mButtonToAlbum;
	private ImageView mButtonToPhoto;
	private RelativeLayout rlTopbars;
	private RelativeLayout rlBottombars;
	private ImageView ivSure, ivClose;
	
	private String picPathTemp = "";
	private Uri picUriTemp = null;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.video_camera);
		session = VideoSession.newInstance(VideoQuality.QUALITY_480P);
		mButtonStart = (ImageView) findViewById(R.id.start);
		mButtonCamera = (ImageView) findViewById(R.id.camera);
		mButtonFlash = (ImageView) findViewById(R.id.flash);
		mSurfaceView = (SurfaceView) findViewById(R.id.surface);
		chronometer = (Chronometer) findViewById(R.id.chronometer1);
		tvClose = (ImageView) findViewById(R.id.close);
		ivClose = (ImageView) findViewById(R.id.bottom_close);
		ivSure = (ImageView) findViewById(R.id.sure);
		mButtonToAlbum = (ImageView) findViewById(R.id.to_album);
		mButtonToPhoto = (ImageView) findViewById(R.id.to_camera);
		rlTopbars = (RelativeLayout) findViewById(R.id.topbars);
		rlTopbars.getBackground().setAlpha(80);
		rlBottombars = (RelativeLayout) findViewById(R.id.bottombars);
		rlBottombars.getBackground().setAlpha(80);

		mButtonToAlbum.setOnClickListener(this);
		mButtonToPhoto.setOnClickListener(this);
		mButtonStart.setOnClickListener(this);
		mButtonCamera.setOnClickListener(this);
		mButtonFlash.setOnClickListener(this);
		tvClose.setOnClickListener(this);
		ivClose.setOnClickListener(this);
		ivSure.setOnClickListener(this);
		session.setCallback(this);
		session.setSurfaceView(mSurfaceView);
		session.startPreview();
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
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
			case R.id.bottom_close:
				setResult(RESULT_CANCELED);
				finish();
				break;
			case R.id.sure:
				String path = session.getVideoFilePath();
				Intent data = new Intent(this, MenuReportAsVidActivity.class);
				data.putExtra(EXTRA_VIDEO_PATH, path);
				setResult(RESULT_OK, data);
				startActivity(data);
				finish();
				break;
			case R.id.to_album:
			{
				finish();
				Intent itImage = new Intent(this, MenuReportAsVidActivity.class);
				itImage.putExtra("type", "video");
				startActivity(itImage);
				break;
			}
			case R.id.to_camera:
			{
				finish();
				Intent it = new Intent(this, MenuReportAsPicActivity.class);
				it.putExtra("type", "camera");
				startActivity(it);
				break;
			}
		}
	}

	public void refreshStartButton()
	{
		switch (session.getState())
		{
			case VideoSession.STATE_STARTED:
				mButtonStart.setImageResource(R.drawable.btn_report_video_stop);
				chronometer.start();
				chronometer.setBase(SystemClock.elapsedRealtime());
				mButtonCamera.setVisibility(View.GONE);
				mButtonFlash.setVisibility(View.GONE);
				// tvClose.setVisibility(View.GONE);
				ivSure.setVisibility(View.GONE);
				mButtonToAlbum.setVisibility(View.GONE);
				mButtonToPhoto.setVisibility(View.GONE);
				chronometer.setVisibility(View.VISIBLE);
				break;
			case VideoSession.STATE_STOPPED:
				mButtonStart.setImageResource(R.drawable.btn_report_video_stop);
				mButtonStart.setVisibility(View.INVISIBLE);
				chronometer.setVisibility(View.GONE);
				mButtonFlash.setVisibility(View.GONE);
				mButtonCamera.setVisibility(View.GONE);
				ivSure.setVisibility(View.VISIBLE);
				ivClose.setVisibility(View.VISIBLE);
				mButtonToAlbum.setVisibility(View.GONE);
				mButtonToPhoto.setVisibility(View.GONE);
				rlTopbars.setVisibility(View.GONE);
				chronometer.stop();
				chronometer.setBase(SystemClock.elapsedRealtime());
				break;
		}
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		session.release();
		session = null;
		// mSurfaceView.getHolder().removeCallback(this);
	}

	long currentMillis = 0;

	public void toggleStream()
	{

		if (Utils.getCurrentTimeInMillis() - currentMillis > 2000)
		{
			if (session.getState() == VideoSession.STATE_STOPPED)
				session.startRecord();
			else
				session.stopRecord();

			currentMillis = Utils.getCurrentTimeInMillis();
		}
		// else
		// Toast.makeText(this, "Frequent operation!",
		// Toast.LENGTH_SHORT).show();
	}

	private void refreshFlashState()
	{
		if (session.getFlashState())
			mButtonFlash.setImageResource(R.drawable.ic_flash_on_holo_light);
		else
			mButtonFlash.setImageResource(R.drawable.ic_flash_off_holo_light);
	}

	public void toggleFlash()
	{
		session.toggleFlash();
	}

	@Override
	public void onSessionError(int reason, int streamType, Exception e)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onPreviewStarted()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onCameraSwitched(int cameraId)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onSessionStarted()
	{
		// TODO Auto-generated method stub
		refreshStartButton();
	}

	@Override
	public void onSessionStopped()
	{
		// TODO Auto-generated method stub
		refreshStartButton();
	}

	@Override
	public void onFlashToggle()
	{
		// TODO Auto-generated method stub
		refreshFlashState();
	}

}
