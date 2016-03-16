package com.airtalkee.activity;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.airtalkee.R;
import com.airtalkee.Util.Setting;
import com.airtalkee.Util.ThemeUtil;
import com.airtalkee.config.Config;

public class MenuSettingSessionVideoActivity extends ActivityBase implements
		OnClickListener, OnCheckedChangeListener
{
	private RadioGroup rgRateFrequence, rgFpsFrequence;
	private int[] fpsFrequence = { 10, 15, 20, 25, 30 };

	@Override
	protected void onCreate(Bundle bundle)
	{
		// TODO Auto-generated method stub
		super.onCreate(bundle);
		setRequestedOrientation(Config.screenOrientation);
		setContentView(R.layout.activity_setting_session_video);
		doInitView();
	}

	@Override
	protected void onStart()
	{
		// TODO Auto-generated method stub
		super.onStart();
	}

	private void doInitView()
	{
		TextView ivTitle = (TextView) findViewById(R.id.tv_main_title);
		ivTitle.setText(R.string.talk_tools_video);
		View btnLeft = findViewById(R.id.menu_left_button);
		ImageView ivLeft = (ImageView) findViewById(R.id.bottom_left_icon);
		ivLeft.setImageResource(ThemeUtil.getResourceId(R.attr.theme_ic_topbar_back, this));
		btnLeft.setOnClickListener(this);

		RelativeLayout ivRightLay = (RelativeLayout) findViewById(R.id.talk_menu_right_button);
		ImageView ivRight = (ImageView) findViewById(R.id.bottom_right_icon);
		ivRight.setVisibility(View.GONE);
		ivRightLay.setVisibility(View.INVISIBLE);
		rgRateFrequence = (RadioGroup) findViewById(R.id.rg_rate_frequence);
		rgFpsFrequence = (RadioGroup) findViewById(R.id.rg_fps_frequence);
		initRgRate();
		initRgFps();
	}

	private void initRgRate()
	{
		String currentRate = Setting.getVideoRate();
		if (currentRate.equals(Setting.VIDEO_RESOLUTION_RATE[2]))
		{
			((RadioButton) rgRateFrequence.findViewById(R.id.rb_rate_low)).setChecked(true);
		}
		else if (currentRate.equals(Setting.VIDEO_RESOLUTION_RATE[1]))
		{
			((RadioButton) rgRateFrequence.findViewById(R.id.rb_rate_normal)).setChecked(true);
		}
		else if (currentRate.equals(Setting.VIDEO_RESOLUTION_RATE[0]))
		{
			((RadioButton) rgRateFrequence.findViewById(R.id.rb_rate_high)).setChecked(true);
		}
		else
		{
			((RadioButton) rgRateFrequence.findViewById(R.id.rb_rate_normal)).setChecked(true);
		}
		((RadioButton) rgRateFrequence.findViewById(R.id.rb_rate_low)).setOnCheckedChangeListener(this);
		((RadioButton) rgRateFrequence.findViewById(R.id.rb_rate_normal)).setOnCheckedChangeListener(this);
		((RadioButton) rgRateFrequence.findViewById(R.id.rb_rate_high)).setOnCheckedChangeListener(this);
	}

	private void initRgFps()
	{
		int currentFps = Setting.getVideoFrameRate();
		switch (currentFps)
		{
			case 10:
				((RadioButton) rgFpsFrequence.findViewById(R.id.rb_fps_10)).setChecked(true);
				break;
			case 15:
				((RadioButton) rgFpsFrequence.findViewById(R.id.rb_fps_15)).setChecked(true);
				break;
			case 20:
				((RadioButton) rgFpsFrequence.findViewById(R.id.rb_fps_20)).setChecked(true);
				break;
			case 25:
				((RadioButton) rgFpsFrequence.findViewById(R.id.rb_fps_25)).setChecked(true);
				break;
			case 30:
				((RadioButton) rgFpsFrequence.findViewById(R.id.rb_fps_30)).setChecked(true);
				break;
			default:
				((RadioButton) rgFpsFrequence.findViewById(R.id.rb_fps_20)).setChecked(true);
				break;
		}
		((RadioButton) rgFpsFrequence.findViewById(R.id.rb_fps_10)).setOnCheckedChangeListener(this);
		((RadioButton) rgFpsFrequence.findViewById(R.id.rb_fps_15)).setOnCheckedChangeListener(this);
		((RadioButton) rgFpsFrequence.findViewById(R.id.rb_fps_20)).setOnCheckedChangeListener(this);
		((RadioButton) rgFpsFrequence.findViewById(R.id.rb_fps_25)).setOnCheckedChangeListener(this);
		((RadioButton) rgFpsFrequence.findViewById(R.id.rb_fps_30)).setOnCheckedChangeListener(this);
	}

	@Override
	public void finish()
	{
		// TODO Auto-generated method stub
		super.finish();
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
			case R.id.menu_left_button:
			case R.id.bottom_left_icon:
				finish();
				break;
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
	{
		if (buttonView != null)
		{
			switch (buttonView.getId())
			{
				case R.id.rb_rate_low:
				{
					if (isChecked)
					{
						Setting.setVideoResolutionWidth(480);
						Setting.setVideoResolutionHeight(320);
					}
					break;
				}
				case R.id.rb_rate_normal:
				{
					if (isChecked)
					{
						Setting.setVideoResolutionWidth(640);
						Setting.setVideoResolutionHeight(480);
					}
					break;
				}
				case R.id.rb_rate_high:
				{
					if (isChecked)
					{
						Setting.setVideoResolutionWidth(1280);
						Setting.setVideoResolutionHeight(720);
					}
					break;
				}
				case R.id.rb_fps_10:
				{
					if (isChecked)
					{
						Setting.setVideoFrameRate(10);
					}
					break;
				}
				case R.id.rb_fps_15:
				{
					if (isChecked)
					{
						Setting.setVideoFrameRate(15);
					}
					break;
				}
				case R.id.rb_fps_20:
				{
					if (isChecked)
					{
						Setting.setVideoFrameRate(20);
					}
					break;
				}
				case R.id.rb_fps_25:
				{
					if (isChecked)
					{
						Setting.setVideoFrameRate(25);
					}
					break;
				}
				case R.id.rb_fps_30:
				{
					if (isChecked)
					{
						Setting.setVideoFrameRate(30);
					}
					break;
				}
			}
		}
	}

}
