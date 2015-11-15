package com.airtalkee.widget;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.usb.UsbDevice;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import com.airtalkee.R;
import com.airtalkee.Util.Util;
import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.AirtalkeeReport;
import com.airtalkee.sdk.controller.AirTaskController;
import com.airtalkee.sdk.controller.AirTaskController.AirTaskTakePictureListener;
import com.airtalkee.sdk.entity.AirSession;
import com.airtalkee.sdk.util.IOoperate;
import com.airtalkee.sdk.video.Session;
import com.airtalkee.sdk.video.SessionBuilder;
import com.airtalkee.sdk.video.codec.VideoQuality;
import com.airtalkee.sdk.video.gl.SurfaceView;
import com.airtalkee.sdk.video.rtsp.RtspClient;
import com.airtalkee.sdk.video.hw.external.CameraUsbManager;
import com.luktong.multistream.sdk.ui.PreviewSurfaceView;
import com.luktong.multistream.sdk.usb.DeviceFilter;
import com.luktong.multistream.sdk.usb.USBMonitor;
import com.luktong.multistream.sdk.usb.USBMonitor.OnDeviceConnectListener;
import com.luktong.multistream.sdk.usb.USBMonitor.UsbControlBlock;

public class VideoSufaceView extends FrameLayout implements OnClickListener,
		RtspClient.Callback, Session.Callback, SurfaceHolder.Callback,
		SensorEventListener, AirTaskTakePictureListener,
		OnDeviceConnectListener
{
	private ImageButton mButtonStart;
	private ImageButton mButtonFlash;
	private ImageButton mButtonCamera;
	private ImageButton mButtonSettings;
	private ImageButton mButtonMic;
	private View parentView;
	private SurfaceView mSurfaceView;
	private PreviewSurfaceView mUSBSurfaceView = null;
	private TextView mTextBitrate;
	private ProgressBar mProgressBar;
	private Session mSession;
	private String mSessionUid = "";
	private RtspClient mClient;
	private String uri;
	private OnVideoStateChangeListener l;
	private int mVideoWidth = 0;
	private int mVideoHeight = 0;
	private int mFramerate = 0;
	private SensorManager sm = null;
	private Sensor mAccelerometer = null;
	Activity activity;
	private CameraUsbManager cameraUsbBinder = null;
	private USBMonitor mUSBMonitor;
	boolean pendingUSBConnect0 = false;
	UsbDevice usbConCB0 = null;
	@SuppressWarnings("unused")
	private boolean allowCheckedChangeEvent = true;
	private int cameraType = Session.CAMERA_EXTERNAL_TYPE_NONE;
	private boolean isCameraUsbReady = false;

	public VideoSufaceView(Context context)
	{
		super(context);
		// TODO Auto-generated constructor stub
		onCreate();
	}

	public VideoSufaceView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		onCreate();
	}

	public VideoSufaceView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		// TODO Auto-generated constructor stub
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
		AirTaskController.getInstance().setAirTaskTakePictureListener(this);
		mButtonStart = (ImageButton) findViewById(R.id.start);
		mButtonFlash = (ImageButton) findViewById(R.id.flash);
		mButtonCamera = (ImageButton) findViewById(R.id.camera);
		mButtonSettings = (ImageButton) findViewById(R.id.settings);
		mButtonMic = (ImageButton) findViewById(R.id.mic);
		mSurfaceView = (SurfaceView) findViewById(R.id.surface);
		// mUSBSurfaceView = (PreviewSurfaceView) findViewById(R.id.svPreview3);
		mTextBitrate = (TextView) findViewById(R.id.bitrate);
		mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
		mButtonStart.setOnClickListener(this);
		mButtonFlash.setOnClickListener(this);
		mButtonCamera.setOnClickListener(this);
		mButtonSettings.setOnClickListener(this);
		mButtonMic.setOnClickListener(this);
		mButtonMic.setTag(false);
		mButtonMic.setEnabled(false);
		mButtonFlash.setTag("off");

		mUSBMonitor = new USBMonitor(this.getContext(), this);
		mUSBMonitor.register();
		cameraUsbBinder = CameraUsbManager.getInstance();
		cameraUsbBinder.startManager();

	}

	public boolean isCameraUsbReady()
	{
		return isCameraUsbReady;
	}

	public void start(OnVideoStateChangeListener listener, int cameraType, RadioGroup rg, View parent, AirSession session, String serverIp, int serverPort, int screenWidth, int screenHeight)
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
		this.cameraType = cameraType;
		mSession = SessionBuilder.getInstance().setContext(this.getContext().getApplicationContext()).setVideoEncoder(SessionBuilder.VIDEO_H264).setSurfaceView(mSurfaceView).setPreviewOrientation(0).setCallback(this).setExternalCameraType(cameraType).build();

		// Configures the RTSP client
		mClient = new RtspClient();
		mClient.setSession(mSession);
		mClient.setSessionCode(session != null ? session.getSessionCode() : "");
		mClient.setCallback(this);
		selectQuality(rg);
		toggleStream();

		switch (cameraType)
		{
			case Session.CAMERA_EXTERNAL_TYPE_NONE:
//				mUSBSurfaceView.setVisibility(View.GONE);
				mSurfaceView.getHolder().addCallback(this);
				mSurfaceView.setVisibility(View.VISIBLE);
				break;
			case Session.CAMERA_EXTERNAL_TYPE_USB:
				mUSBSurfaceView.setVisibility(View.VISIBLE);
				mSurfaceView.setVisibility(View.GONE);
				startUsbPreview();
				break;
			case Session.CAMERA_EXTERNAL_TYPE_WIFI:
				break;
		}
	}

	public void finish()
	{
		if (mClient != null && mClient.isStreaming())
		{
			if (sm != null)
				sm.unregisterListener(this);
			mProgressBar.setVisibility(View.GONE);
			this.setVisibility(View.GONE);
			mClient.stopStream();
			mSession.release();
			switch (cameraType)
			{
				case Session.CAMERA_EXTERNAL_TYPE_NONE:
					mSurfaceView.getHolder().removeCallback(this);
					break;
				case Session.CAMERA_EXTERNAL_TYPE_USB:
					stopUsbPreview();
					break;
				case Session.CAMERA_EXTERNAL_TYPE_WIFI:
					break;
			}
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
		// case R.id.start:
		// toggleStream();
		// break;
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
		}
	}

	public void selectQuality(RadioGroup radiGroup)
	{
		int id = radiGroup.getCheckedRadioButtonId();
		RadioButton button = (RadioButton) parentView.findViewById(id);
		if (button == null)
			return;

		String text = button.getText().toString();
		Pattern pattern = Pattern.compile("(\\d+)x(\\d+)\\D+(\\d+)\\D+(\\d+)");
		Matcher matcher = pattern.matcher(text);

		matcher.find();
		mVideoWidth = Integer.parseInt(matcher.group(1));
		mVideoHeight = Integer.parseInt(matcher.group(2));
		mFramerate = Integer.parseInt(matcher.group(3));
		int bitrate = Integer.parseInt(matcher.group(4)) * 1000;

		mSession.setVideoQuality(new VideoQuality(mVideoWidth, mVideoHeight, mFramerate, bitrate));
		Util.Toast(this.getContext(), button.getText().toString());

		Log.d("m", "Selected resolution: " + mVideoWidth + "x" + mVideoHeight);
	}

	private void enableUI()
	{
		mButtonStart.setEnabled(true);
		mButtonCamera.setEnabled(true);
	}

	// Connects/disconnects to the RTSP server and starts/stops the stream
	public void toggleStream()
	{
		finish();

		mProgressBar.setVisibility(View.VISIBLE);
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
			this.setVisibility(View.VISIBLE);
		}
	}

	private void logError(final String msg)
	{
		/*
		 * final String error = (msg == null) ? "Error unknown" : msg; //
		 * Displays a popup to report the eror to the user AlertDialog.Builder
		 * builder = new AlertDialog.Builder(this.getContext());
		 * builder.setMessage(error).setPositiveButton("OK", new
		 * DialogInterface.OnClickListener() { public void
		 * onClick(DialogInterface dialog, int id) {} }); AlertDialog dialog =
		 * builder.create(); dialog.show();
		 */
	}

	@Override
	public void onBitrateUpdate(long bitrate)
	{
		String text = mVideoWidth + " x " + mVideoHeight + "\n";
		text += mFramerate + " fps\n";
		text += bitrate / 1000 + " kbps";
		mTextBitrate.setText(text);
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
		mProgressBar.setVisibility(View.GONE);
		this.setVisibility(View.VISIBLE);
		if (this.l != null)
		{
			l.onVideoStateChange(false);
		}
		if (sm != null)
			sm.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
		mAutoFocus = true;
		mSession.autoFocus(callback);
		// mButtonMic.setTag(true);
		// mButtonMic.setEnabled(true);
		// mButtonMic.setImageResource(R.drawable.ic_microphone_on);
	}

	@Override
	public void onSessionStopped()
	{
		enableUI();
		if (sm != null)
			sm.unregisterListener(this);
		AirTaskController.getInstance().setAirTaskTakePictureListener(null);
		mButtonStart.setImageResource(R.drawable.ic_switch_video);
		mProgressBar.setVisibility(View.GONE);
		this.setVisibility(View.GONE);
		if (this.l != null)
		{
			l.onVideoStateChange(true);
		}
		// mButtonMic.setEnabled(false);
		// mButtonMic.setTag(false);
		// mButtonMic.setImageResource(R.drawable.ic_microphone_off);
	}

	@Override
	public void onSessionError(int reason, int streamType, Exception e)
	{
		mProgressBar.setVisibility(View.GONE);
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
		mSession.startPreview();
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

	private PictureCallback jpegCallback = new PictureCallback()
	{
		public void onPictureTaken(byte[] _data, Camera _camera)
		{
			try
			{
				mSession.toggleFlash(false);
				/* 取得相仞Bitmap对象 */
				if (_data != null)
				{
					new IOoperate().fileSave(IOoperate.VIDEO_PATH, Util.getCurrentTime() + ".jpg", _data, false);
					AirtalkeeReport.getInstance().ReportCaputreImage(mSessionUid, AirtalkeeReport.RESOURCE_TYPE_PICTURE, "", _data, "");
				}

			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	};

	public void takePicture(String uid, boolean flash)
	{
		if (mSession != null)
		{
			mSession.takePicture(jpegCallback);
			mSessionUid = uid;
			if (flash)
				mSession.toggleFlash(true);
		}
	}

	@Override
	public void onTaskTakePicture(String uid, boolean toFlashLamp)
	{
		// TODO Auto-generated method stub
		takePicture(uid, toFlashLamp);
	}

	/****************************************************************
	 * 
	 * USB camera
	 * 
	 ****************************************************************/

	private void startUsbPreview()
	{
		if (cameraUsbBinder != null)
		{
			final List<DeviceFilter> filter = DeviceFilter.getDeviceFilters(this.getContext(), R.xml.device_filter);
			List<UsbDevice> devices = mUSBMonitor.getDeviceList(filter.get(0));

			if (devices != null && devices.size() > 0)
			{

				allowCheckedChangeEvent = false;
				allowCheckedChangeEvent = true;
				UsbDevice device = devices.get(0);
				pendingUSBConnect0 = true;
				mUSBMonitor.requestPermission(device);

			}
			else
			{
				allowCheckedChangeEvent = false;
				allowCheckedChangeEvent = true;
			}
		}
		else
		{
			allowCheckedChangeEvent = false;
			allowCheckedChangeEvent = true;
		}
	}

	private void stopUsbPreview()
	{
		if (cameraUsbBinder == null || !cameraUsbBinder.stopPreview())
		{
			allowCheckedChangeEvent = false;
			allowCheckedChangeEvent = true;
		}
	}

	private void connectUSBCamera(UsbControlBlock ctrlBlock, UsbDevice device)
	{

		pendingUSBConnect0 = false;
		if (!cameraUsbBinder.startPreview(mVideoWidth, mVideoHeight, mFramerate, mFramerate, ctrlBlock))
		{
			allowCheckedChangeEvent = false;
			allowCheckedChangeEvent = true;
			usbConCB0 = null;

		}
		else
		{
			cameraUsbBinder.setPreviewSurface(null);
			cameraUsbBinder.setPreviewSurface(mUSBSurfaceView.getHolder().getSurface());
			allowCheckedChangeEvent = false;

			allowCheckedChangeEvent = true;
			startUsbPreview();
			usbConCB0 = device;
		}
	}

	private void disconnectUSBCamera()
	{
		if (cameraUsbBinder != null)
		{

			if (cameraUsbBinder.isPreview())
			{
				cameraUsbBinder.stopPreview();
			}

			usbConCB0 = null;
		}
	}

	@Override
	public void onAttach(UsbDevice arg0)
	{
		// TODO Auto-generated method stub
		// Toast.makeText(this.getContext(), "USB_DEVICE_ATTACHED",
		// Toast.LENGTH_SHORT).show();
		isCameraUsbReady = true;
	}

	@Override
	public void onDettach(UsbDevice device)
	{
		// TODO Auto-generated method stub
		// Toast.makeText(this.getContext(), "USB_DEVICE_DETACHED",
		// Toast.LENGTH_SHORT).show();
		isCameraUsbReady = false;
		if (device != null)
		{
			activity.runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{

					disconnectUSBCamera();

				}
			});
		}
	}

	@Override
	public void onCancel(UsbDevice arg0)
	{
		// TODO Auto-generated method stub
		isCameraUsbReady = false;
		activity.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				allowCheckedChangeEvent = false;
				// UsbCameraActivity.this.tbtnStartStopPreview.setChecked(false);
				// UsbCameraActivity.this.tbtnStartStopPreview.setEnabled(true);
				stopUsbPreview();
				allowCheckedChangeEvent = true;

			}
		});
	}

	@Override
	public void onConnect(final UsbDevice device, final UsbControlBlock ctrlBlock, boolean arg2)
	{
		// TODO Auto-generated method stub
		isCameraUsbReady = true;
		if (pendingUSBConnect0)
		{
			activity.runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					connectUSBCamera(ctrlBlock, device);
				}
			});
		}
	}

	@Override
	public void onDisconnect(UsbDevice device, UsbControlBlock arg1)
	{
		// TODO Auto-generated method stub
		isCameraUsbReady = false;
		if (device != null)
		{
			activity.runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{

					disconnectUSBCamera();
				}
			});
		}
	}
}
