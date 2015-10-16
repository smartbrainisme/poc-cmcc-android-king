package com.airtalkee.widget;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Chronometer;
import android.widget.Chronometer.OnChronometerTickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airtalkee.R;
import com.airtalkee.sdk.util.Log;

public class MacRecordingView extends RelativeLayout
{
	public static final int START_RECORD = 0;
	public static final int START_TIME = 1;
	public static final int STOP_TIME = 2;
	public static final int RECORD_CANCEL = 3;
	public static final int RECORD_OK = 4;
	// private static double view_high = 0;
	private String time_length = "01:00";
	public static int temp = 900;
	private Chronometer chronometer;
	private ImageView mac_image, screen_image, record_cancel;
	private TextView text_view;
	private ProgressBar pro2;
	private View porLay = null;
	private Context mInstance;

	public MacRecordingView(Context context)
	{
		// TODO Auto-generated constructor stub
		super(context);
		mInstance = context;
	}

	public MacRecordingView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		mInstance = context;
	}

	public Handler handler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what)
			{
				case START_RECORD:
					if (msg.obj != null)
					{
						reflashView((Integer) msg.obj);
					}
					break;
				case START_TIME:
					startTime();
					break;
				case STOP_TIME:
					if (msg.obj != null)
						stopTime((Boolean) msg.obj);
					break;
				case RECORD_CANCEL:
					recordCancel();
					break;
				case RECORD_OK:
					recording();
					break;
			}
		}
	};

	public void countSize(byte[] bufSend)
	{
		int v = 0;
		for (int i = 0; i < bufSend.length; i++)
		{
			v += Math.abs(Math.pow(bufSend[i], 3));
		}
		int value = (int) (Math.abs((int) (v / 100000)));
		registerMessage(START_RECORD, value);
	}

	public void registerMessage(int eventId, Object obj)
	{
		Log.e(MacRecordingView.class, "registerMessage eventId =" + eventId);
		Message message = handler.obtainMessage();
		message.arg1 = eventId;
		message.what = eventId;
		message.obj = obj;
		handler.sendMessage(message);
	}

	public void setText(String text)
	{
		if (text_view != null)
			text_view.setText(text);
	}

	private void startTime()
	{
		if (getVisibility() != View.VISIBLE)
			setVisibility(View.VISIBLE);
		recording();
		if (chronometer != null)
		{
			chronometer.setBase(SystemClock.elapsedRealtime());
			chronometer.start();
		}
		if (chronometer != null && chronometer.getOnChronometerTickListener() == null)
		{
			chronometer.setOnChronometerTickListener(new OnChronometerTickListener()
			{
				@Override
				public void onChronometerTick(Chronometer chronter)
				{
					// TODO Auto-generated method stub
					if (time_length.equals(chronter.getText()))
					{
						registerMessage(STOP_TIME, false);
					}
				}
			});
		}
	}

	private void stopTime(boolean recorderCancel)
	{
		setVisibility(View.INVISIBLE);
		if (chronometer != null)
		{
			chronometer.stop();
			chronometer.setBase(SystemClock.elapsedRealtime());
		}
	}

	public void initChild()
	{
		chronometer = (Chronometer) findViewById(R.id.chronometer);
		mac_image = (ImageView) findViewById(R.id.mac);
		screen_image = (ImageView) findViewById(R.id.image);
		record_cancel = (ImageView) findViewById(R.id.cancel);
		text_view = (TextView) findViewById(R.id.text);
		pro2 = (ProgressBar) findViewById(R.id.progress_small);
		porLay = findViewById(R.id.pro_lay);
	}

	private void recordCancel()
	{
		if (mac_image.getVisibility() != View.INVISIBLE)
			mac_image.setVisibility(View.INVISIBLE);
		if (screen_image.getVisibility() != View.INVISIBLE)
			screen_image.setVisibility(View.INVISIBLE);
		if (record_cancel.getVisibility() != View.VISIBLE)
			record_cancel.setVisibility(View.VISIBLE);
		if (porLay.getVisibility() != INVISIBLE)
			porLay.setVisibility(INVISIBLE);
		if (!text_view.getText().equals(mInstance.getString(R.string.talk_rec_release)))
			text_view.setText(mInstance.getString(R.string.talk_rec_release));
	}

	private void recording()
	{
		if (mac_image.getVisibility() != View.VISIBLE)
			mac_image.setVisibility(View.VISIBLE);
		if (screen_image.getVisibility() != View.VISIBLE)
			screen_image.setVisibility(View.VISIBLE);
		if (record_cancel.getVisibility() != View.INVISIBLE)
			record_cancel.setVisibility(View.INVISIBLE);
		if (porLay.getVisibility() != VISIBLE)
			porLay.setVisibility(VISIBLE);

		if (!text_view.getText().equals(mInstance.getString(R.string.talk_rec_cancel)))
			text_view.setText(mInstance.getString(R.string.talk_rec_cancel));
	}

	private void reflashView(int high)
	{
		if (high > temp)
		{
			pro2.setVisibility(View.GONE);
		}
		else
		{
			pro2.setVisibility(View.VISIBLE);
		}

		/*
		 * high = resetHeight(high);
		 * android.view.ViewGroup.LayoutParams localLayoutParams = screen_image
		 * .getLayoutParams();
		 * localLayoutParams.height = (int) (view_high - high);
		 * localLayoutParams.width = mac_image.getWidth();
		 * screen_image.setLayoutParams(localLayoutParams);
		 * screen_image.setBackgroundColor(R.color.profile_avater_bg);
		 */
	}

	/*
	 * private int resetHeight(int high){
	 * view_high = mac_image.getHeight();
	 * if (high < temp) {
	 * high = 0;
	 * } else if (high >= temp && high < temp + 10) {
	 * high = (int) ((view_high / 20) * 2);
	 * } else if (high >= temp + 10 && high < temp + 20) {
	 * high = (int) ((view_high / 20) * 3);
	 * } else if (high >= temp + 20 && high < temp + 50) {
	 * high = (int) ((view_high / 20) * 4);
	 * } else if (high >= temp + 50 && high < temp + 80) {
	 * high = (int) ((view_high / 20) * 5);
	 * } else if (high >= temp + 80 && high < temp + 100) {
	 * high = (int) ((view_high / 20) * 6);
	 * } else if (high >= temp + 100 && high < temp + 150) {
	 * high = (int) ((view_high / 20) * 7);
	 * } else if (high >= temp + 150 && high < temp + 180) {
	 * high = (int) ((view_high / 20) * 8);
	 * } else if (high >= temp + 180 && high < temp + 210) {
	 * high = (int) ((view_high / 20) * 8.5);
	 * } else if (high >= temp + 210 && high < temp + 300) {
	 * high = (int) ((view_high / 20) * 9);
	 * } else if (high >= temp + 300 && high < temp + 350) {
	 * high = (int) ((view_high / 20) * 10);
	 * } else if (high >= temp + 350 && high < temp + 400) {
	 * high = (int) ((view_high / 20) * 11);
	 * } else if (high >= temp + 400 && high < temp + 500) {
	 * high = (int) ((view_high / 20) * 12);
	 * } else if (high >= temp + 500 && high < temp + 600) {
	 * high = (int) ((view_high / 20) * 13);
	 * } else if (high >= temp + 600 && high < temp + 800) {
	 * high = (int) ((view_high / 20) * 14);
	 * } else if (high >= temp + 800 && high < temp + 1100) {
	 * high = (int) ((view_high / 20) * 15);
	 * } else if (high >= temp + 1100 && high < temp + 1500) {
	 * high = (int) ((view_high / 20) * 16);
	 * } else if (high >= temp + 1500 && high < temp + 2500) {
	 * high = (int) ((view_high / 20) * 17);
	 * } else {
	 * high = (int) ((view_high / 20) * 18);
	 * }
	 * return high;
	 * }
	 */
}
