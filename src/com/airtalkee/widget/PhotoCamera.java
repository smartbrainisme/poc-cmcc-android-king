package com.airtalkee.widget;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.media.ExifInterface;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.airtalkee.R;
import com.airtalkee.R.string;
import com.airtalkee.Util.Sound;
import com.airtalkee.activity.MenuReportAsPicActivity;
import com.airtalkee.activity.home.AlbumChooseActivity;
import com.airtalkee.activity.home.IMFragment;

public class PhotoCamera extends Activity implements OnClickListener, Callback
{
	// 拍照按钮
	private ImageView mButtonStart;
	// 闪光灯
	private ImageView mButtonFlash;
	private ImageView tvClose;
	private ImageView mButtonToAlbum;
	private ImageView mButtonToVideo;
	private RelativeLayout rlTopbars;
	private RelativeLayout rlBottombars;
	private ImageView ivSure, ivClose;
	private String picPathTemp = "";

	private Camera camera;
	private SurfaceHolder surfaceHolder;
	private SurfaceView mSurfaceView;
	
	private int type;
//	private Context mContext;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		// 设置长亮
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		// 没有标题
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
		setContentView(R.layout.photo_camera);
		mButtonStart = (ImageView) findViewById(R.id.start);
		mButtonFlash = (ImageView) findViewById(R.id.flash);
		mSurfaceView = (SurfaceView) findViewById(R.id.surface);
		surfaceHolder = mSurfaceView.getHolder();
		tvClose = (ImageView) findViewById(R.id.close);
		ivClose = (ImageView) findViewById(R.id.bottom_close);
		ivSure = (ImageView) findViewById(R.id.sure);
		mButtonToAlbum = (ImageView) findViewById(R.id.to_album);
		mButtonToVideo = (ImageView) findViewById(R.id.to_camera);
		rlTopbars = (RelativeLayout) findViewById(R.id.topbars);
		rlTopbars.getBackground().setAlpha(80);
		rlBottombars = (RelativeLayout) findViewById(R.id.bottombars);
		rlBottombars.getBackground().setAlpha(80);

		mButtonToAlbum.setOnClickListener(this);
		mButtonToVideo.setOnClickListener(this);
		mButtonStart.setOnClickListener(this);
		// mButtonFlash.setOnClickListener(this);
		tvClose.setOnClickListener(this);
		ivClose.setOnClickListener(this);
		ivSure.setOnClickListener(this);

		surfaceHolder.addCallback(this);

		savedInstanceState = getIntent().getExtras();
		if (savedInstanceState != null)
		{
			picPathTemp = savedInstanceState.getString(MediaStore.EXTRA_OUTPUT);
			type = savedInstanceState.getInt("type");
//			mContext = (Context) savedInstanceState.get("context");
		}
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
			case R.id.flash:
				break;
			case R.id.start:
				// toggleStream();
				camera.autoFocus(new AutoFocusCallback()
				{
					@Override
					public void onAutoFocus(boolean success, Camera camera)
					{
						if (success)
						{
							Parameters params = camera.getParameters();
							// 自动闪光
							params.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
							params.setPictureFormat(ImageFormat.JPEG);// 图片格式
							List<Size> sizes = params.getSupportedPreviewSizes();
							Size size = sizes.get(0);
							params.setPreviewSize(size.width, size.height);
							params.setJpegQuality(100);
							params.setPictureSize(size.width, size.height);
							camera.setParameters(params);// 将参数设置到我的camera
							camera.takePicture(null, null, jpeg);
						}
					}
				});
				break;
			case R.id.close:
			case R.id.bottom_close:
				setResult(RESULT_CANCELED);
				finish();
				break;
			case R.id.sure:
				// TODO
				switch (type)
				{
					case AlbumChooseActivity.TYPE_IM:
					{
						Intent data = new Intent();
						ArrayList<String> pathList = new ArrayList<String>();
						pathList.add(picPathTemp);
						data.putExtra("picPath", pathList);
						setResult(Activity.RESULT_OK, data);
						finish();
						break;
					}
					case AlbumChooseActivity.TYPE_REPORT:
					{
						Intent data = new Intent(this, MenuReportAsPicActivity.class);
						setResult(RESULT_OK, data);
						finish();
						break;
					}
				}
				break;
				
			case R.id.to_album:
			{
				finish();
				camera.release();
				Intent itImage = new Intent(this, MenuReportAsPicActivity.class);
				itImage.putExtra("type", "image");
				startActivity(itImage);
				break;
			}
			case R.id.to_camera:
			{
				finish();
				camera.release();
				Intent it = new Intent(this, VideoCamera.class);
				it.putExtra("type", "camera");
				startActivity(it);
				break;
			}
		}
	}

	public void refreshStartButton(int state)
	{
		switch (state)
		{
			case 0:
				mButtonStart.setImageResource(R.drawable.btn_report_video_stop);
				mButtonFlash.setVisibility(View.GONE);
				// tvClose.setVisibility(View.GONE);
				ivSure.setVisibility(View.GONE);
				mButtonToAlbum.setVisibility(View.GONE);
				mButtonToVideo.setVisibility(View.GONE);
				break;
			case 1:
				mButtonStart.setImageResource(R.drawable.btn_report_video_stop);
				mButtonStart.setVisibility(View.INVISIBLE);
				mButtonFlash.setVisibility(View.GONE);
				ivSure.setVisibility(View.VISIBLE);
				ivClose.setVisibility(View.VISIBLE);
				mButtonToAlbum.setVisibility(View.GONE);
				mButtonToVideo.setVisibility(View.GONE);
				rlTopbars.setVisibility(View.GONE);
				break;
		}
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		// mSurfaceView.getHolder().removeCallback(this);
		camera.release();
		camera = null;
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder)
	{
		if (camera == null)
			camera = Camera.open();
		try
		{
			camera.setPreviewDisplay(holder);
			camera.setDisplayOrientation(getPreviewDegree(PhotoCamera.this));// 设置相机方向
			camera.startPreview();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder)
	{
		// TODO Auto-generated method stub

	}

	PictureCallback jpeg = new PictureCallback()
	{
		@Override
		public void onPictureTaken(byte[] data, Camera camera)
		{
			// 此处调用系统声音
			Sound.playSound(Sound.PLAYER_TAKE_PHOTO, PhotoCamera.this);
			Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
			int degree = getPictureDegree(picPathTemp);
			bitmap = rotaingImageView(degree, bitmap);
			File file = new File(picPathTemp);
			BufferedOutputStream bos;
			try
			{
				bos = new BufferedOutputStream(new FileOutputStream(file));
				bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
				bos.write(data); // 写入sd卡中
				bos.close(); // 关闭输出流
				camera.stopPreview();
				bitmap.recycle();
				refreshStartButton(1);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

		}
	};

	public static int getPreviewDegree(Activity activity)
	{
		// 获得手机的方向
		int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
		int degree = 0;
		// 根据手机的方向计算相机预览画面应该选择的角度
		switch (rotation)
		{
			case Surface.ROTATION_0:
				degree = 90;
				break;
			case Surface.ROTATION_90:
				degree = 0;
				break;
			case Surface.ROTATION_180:
				degree = 270;
				break;
			case Surface.ROTATION_270:
				degree = 180;
				break;
		}
		return degree;
	}

	public int getPictureDegree(String path)
	{
		int degree = 0;
		try
		{
			ExifInterface exifInterface = new ExifInterface(path);
			int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
			switch (orientation)
			{
				case ExifInterface.ORIENTATION_UNDEFINED:
					degree = 90;
					break;
				case ExifInterface.ORIENTATION_ROTATE_90:
					degree = 0;
					break;
				case ExifInterface.ORIENTATION_ROTATE_180:
					degree = 270;
					break;
				case ExifInterface.ORIENTATION_ROTATE_270:
					degree = 180;
					break;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return degree;
	}

	public Bitmap rotaingImageView(int angle, Bitmap bitmap)
	{
		// 旋转图片 动作
		Matrix matrix = new Matrix();
		matrix.postRotate(angle);
		// 创建新的图片
		return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
	}

}
