package com.airtalkee.widget;

import android.content.Context;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.widget.FrameLayout;
import com.airtalkee.sdk.video.gl.SurfaceView;

public class PhotoSufaceView extends FrameLayout implements
		SurfaceHolder.Callback
{
	private Camera camera;
	private SurfaceHolder surfaceHolder;

	public PhotoSufaceView(Context context)
	{
		super(context);
	}

	public PhotoSufaceView(Context context, Camera camera, SurfaceView surfaceView)
	{
		super(context);
		this.camera = camera;
		surfaceHolder = surfaceView.getHolder();
		surfaceHolder.addCallback(this);
	}

	public void onCreate()
	{
	}

	public void initView()
	{

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder)
	{
		try
		{
			camera.setPreviewDisplay(holder);
			camera.startPreview();
		}
		catch (Exception e)
		{}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
	{
		if (surfaceHolder.getSurface() == null)
			return;
		// Camera.Parameters params = camera.getParameters();
		// params.setPictureFormat(ImageFormat.JPEG);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder)
	{
		if (camera != null)
		{
			camera.release();
			camera = null;
		}
	}

}
