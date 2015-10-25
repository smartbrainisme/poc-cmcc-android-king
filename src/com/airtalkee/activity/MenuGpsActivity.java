package com.airtalkee.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.airtalkee.R;
import com.airtalkee.Util.ThemeUtil;
import com.airtalkee.config.Config;
import com.airtalkee.listener.OnMmiLocationListener;
import com.airtalkee.location.AirLocation;
import com.airtalkee.sdk.entity.AirFunctionSetting;
import com.umeng.common.net.r;

public class MenuGpsActivity extends ActivityBase implements OnClickListener,
		OnMmiLocationListener, OnCheckedChangeListener
{
	private TextView gps_t;
	int[] mFrequenceValue = { AirLocation.AIR_LOCATION_FRE_NAVIGATE, AirLocation.AIR_LOCATION_FRE_MINUTE_1, AirLocation.AIR_LOCATION_FRE_MINUTE_5, AirLocation.AIR_LOCATION_FRE_MINUTE_15, AirLocation.AIR_LOCATION_FRE_MINUTE_30, AirLocation.AIR_LOCATION_FRE_MINUTE_60 };
	String[] mFrequence = null;
	String[] mState = null;
	int mStateSelected = 0;
	int mFrequenceSelected = 0;// index

	private CheckBox cbGPS;
	private CheckBox cbGPSHigh;
	boolean gpsState;
	private TextView gpsFrequenceText; // 回传频率
	private TextView gpsFrequenceHigh; // 高精度回传
	private RadioGroup rgGpsFrequence;
	private RadioButton rb1Min, rb5Min, rb15Min, rb30Min, rb60Min;
	private TextView tv1Min, tv5Min, tv15Min, tv30Min, tv60Min;

	@Override
	protected void onCreate(Bundle bundle)
	{
		super.onCreate(bundle);
		setRequestedOrientation(Config.screenOrientation);
		setContentView(R.layout.activity_tool_gps);
		gpsState = AirLocation.getInstance(this).getSettingState();
		mStateSelected = gpsState ? 0 : 1;
		int fre = AirLocation.getInstance(this).getSettingFrequence();
		for (int i = 0; i < mFrequenceValue.length; i++)
		{
			if (fre == mFrequenceValue[i])
			{
				mFrequenceSelected = i;
			}
		}
		doInitView();

		AirLocation.getInstance(this).setListener(this, AirLocation.AIR_LOCATION_ID_LOOP);
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		ImageView icon = (ImageView) findViewById(R.id.gps_state_selector_gps);
		if (AirLocation.getInstance(this).GpsIsActive())
		{
			icon.setImageResource(R.drawable.gps_state_enable);
		}
		else
		{
			icon.setImageResource(R.drawable.gps_state_disable);
		}
	}

	private void doInitView()
	{
		TextView ivTitle = (TextView) findViewById(R.id.tv_main_title);
		ivTitle.setText(R.string.talk_tools_location);
		View btnLeft = findViewById(R.id.menu_left_button);
		ImageView ivLeft = (ImageView) findViewById(R.id.bottom_left_icon);
		ivLeft.setImageResource(ThemeUtil.getResourceId(R.attr.theme_ic_topbar_back, this));
		btnLeft.setOnClickListener(this);

		RelativeLayout ivRightLay = (RelativeLayout) findViewById(R.id.talk_menu_right_button);
		ImageView ivRight = (ImageView) findViewById(R.id.bottom_right_icon);
		ivRight.setVisibility(View.GONE);
		ivRightLay.setVisibility(View.INVISIBLE);

		mState = getResources().getStringArray(R.array.gps_state);
		gps_t = (TextView) findViewById(R.id.gps_t);

		gpsFrequenceText = (TextView) findViewById(R.id.gps_frequence_text);
		gpsFrequenceHigh = (TextView) findViewById(R.id.gps_frequence_high_text);

		cbGPS = (CheckBox) findViewById(R.id.talk_setting_gps);
		cbGPS.setChecked(gpsState);
		cbGPS.setOnCheckedChangeListener(this);

		cbGPSHigh = (CheckBox) findViewById(R.id.gps_frequence_high);
		cbGPSHigh.setOnCheckedChangeListener(this);

		rgGpsFrequence = (RadioGroup) findViewById(R.id.rg_gps_frequence);
		rgGpsFrequence.setOnCheckedChangeListener(listener);
		rb1Min = (RadioButton) findViewById(R.id.rb_1_min);
		rb5Min = (RadioButton) findViewById(R.id.rb_5_min);
		rb15Min = (RadioButton) findViewById(R.id.rb_15_min);
		rb30Min = (RadioButton) findViewById(R.id.rb_30_min);
		rb60Min = (RadioButton) findViewById(R.id.rb_60_min);
		rb1Min.setBackground(getResources().getDrawable(R.drawable.selector_radio_check));
		rb5Min.setBackground(getResources().getDrawable(R.drawable.selector_radio_check));
		rb15Min.setBackground(getResources().getDrawable(R.drawable.selector_radio_check));
		rb30Min.setBackground(getResources().getDrawable(R.drawable.selector_radio_check));
		rb60Min.setBackground(getResources().getDrawable(R.drawable.selector_radio_check));
		tv1Min = (TextView) findViewById(R.id.tv_1_min);
		tv5Min = (TextView) findViewById(R.id.tv_5_min);
		tv15Min = (TextView) findViewById(R.id.tv_15_min);
		tv30Min = (TextView) findViewById(R.id.tv_30_min);
		tv60Min = (TextView) findViewById(R.id.tv_60_min);
		initRadioGroup(gpsState, mFrequenceValue[mFrequenceSelected]);
	}

	private RadioGroup.OnCheckedChangeListener listener = new RadioGroup.OnCheckedChangeListener()
	{
		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId)
		{
			int rid = group.getCheckedRadioButtonId();
			switch (rid)
			{
				case R.id.rb_1_min:
					set1MinView(true);
					break;
				case R.id.rb_5_min:
					set5MinView(true);
					break;
				case R.id.rb_15_min:
					set15MinView(true);
					break;
				case R.id.rb_30_min:
					set30MinView(true);
					break;
				case R.id.rb_60_min:
					set60MinView(true);
					break;
				default:
					break;
			}
			// initRadioGroup(1);
		}
	};

	private void initRadioGroup(boolean state, int value)
	{
		if (state)
		{
			switch (value)
			{
				case AirLocation.AIR_LOCATION_FRE_NAVIGATE:
				{
					cbGPSHigh.setChecked(true);
					setHighFreView(true);
					break;
				}
				case AirLocation.AIR_LOCATION_FRE_MINUTE_1:
				{
					set1MinView(true);
					break;
				}
				case AirLocation.AIR_LOCATION_FRE_MINUTE_5:
				{
					set5MinView(true);
					break;
				}
				case AirLocation.AIR_LOCATION_FRE_MINUTE_15:
				{
					set15MinView(true);
					break;
				}
				case AirLocation.AIR_LOCATION_FRE_MINUTE_30:
				{
					set30MinView(true);
					break;
				}
				case AirLocation.AIR_LOCATION_FRE_MINUTE_60:
				{
					set60MinView(true);
					break;
				}
				default:
					break;
			}
		}
		else
		{
			setGpsView(false);
		}
	}

	@Override
	public void finish()
	{
		// TODO Auto-generated method stub
		super.finish();
		AirLocation.getInstance(this).setListener(null, AirLocation.AIR_LOCATION_ID_LOOP);
	}

	private void refreshGpsData(int type, double latitude, double longitude, double altitude, float speed, String time)
	{
		gps_t.setText(time + "");
	}

	private void confirmGpsHighPrecision()
	{
		final boolean isGpsActived = AirLocation.getInstance(this).GpsIsActive();

		String btn_ok = isGpsActived ? getString(R.string.talk_ok) : getString(R.string.talk_gps_setting);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(getString(R.string.talk_gps_high_tip));
		builder.setPositiveButton(btn_ok, new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int whichButton)
			{
				dialog.cancel();
				if (!isGpsActived)
				{
					AirLocation.getInstance(MenuGpsActivity.this).GpsActive();
				}
				mStateSelected = 0;
				mFrequenceSelected = 0;
				AirLocation.getInstance(MenuGpsActivity.this).loopRun(MenuGpsActivity.this, AirLocation.AIR_LOCATION_FRE_NAVIGATE, true);
				setHighFreView(true);
			}
		});

		builder.setNegativeButton(getString(R.string.talk_no), new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int whichButton)
			{
				dialog.cancel();
				setHighFreView(false);
			}
		});
		builder.show();
	}

	@Override
	public void onClick(View v)
	{
		// TODO Auto-generated method stub
		// final Context context = this;
		switch (v.getId())
		{
			case R.id.menu_left_button:
			case R.id.bottom_left_icon:
				finish();
				break;
		// case R.id.gps_state_selector_setting:
		// {
		// AirLocation.getInstance(this).GpsActive();
		// break;
		// }
		}
	}

	@Override
	public void onLocationChanged(boolean isOk, int id, int type, double latitude, double longitude, double altitude, float speed, String time)
	{
		if (isOk && id == AirLocation.AIR_LOCATION_ID_LOOP)
		{
			refreshGpsData(type, latitude, longitude, altitude, speed, time);
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
	{
		final Context context = this;
		if (buttonView != null)
		{
			switch (buttonView.getId())
			{
				case R.id.talk_setting_gps:
				{
					if (cbGPS.isChecked())// 是否开启位置回传
					{
						// if (cbGPSHigh.isChecked())// 是否开启高精度模式
						// {
						// confirmGpsHighPrecision();
						// }
						AirLocation.getInstance(context).loopRun(MenuGpsActivity.this, mFrequenceValue[mFrequenceSelected], true);
						setGpsView(true);
					}
					else
					{
						if (cbGPSHigh.isChecked())
						{
							cbGPSHigh.setBackground(getResources().getDrawable(R.drawable.btn_setting_open_gray));
						}
						else
						{
							cbGPSHigh.setBackground(getResources().getDrawable(R.drawable.btn_setting_close));
						}
						setGpsView(false);
						AirLocation.getInstance(context).loopTerminate();
					}
					break;
				}
				case R.id.gps_frequence_high:
				{
					if (cbGPSHigh.isChecked())// 是否开启高精度模式
					{
						final boolean isGpsActived = AirLocation.getInstance(this).GpsIsActive();
						confirmGpsHighPrecision();
						// confirmGpsHighPrecision();
//						if (!isGpsActived)
//						{
//							AirLocation.getInstance(MenuGpsActivity.this).GpsActive();
//						}
//						mStateSelected = 0;
//						mFrequenceSelected = 0;
//						AirLocation.getInstance(MenuGpsActivity.this).loopRun(MenuGpsActivity.this, AirLocation.AIR_LOCATION_FRE_NAVIGATE, true);
//						setHighFreView(true);
					}
					else
					{
//						AirLocation.getInstance(MenuGpsActivity.this).loopRun(MenuGpsActivity.this, mFrequenceValue[mFrequenceSelected], true);
						setHighFreView(false);
						switch (mFrequenceValue[mFrequenceSelected])
						{
							case AirLocation.AIR_LOCATION_FRE_MINUTE_1:
								set1MinView(true);
								break;
							case AirLocation.AIR_LOCATION_FRE_MINUTE_5:
								set5MinView(true);
								break;
							case AirLocation.AIR_LOCATION_FRE_MINUTE_15:
								set15MinView(true);
								break;
							case AirLocation.AIR_LOCATION_FRE_MINUTE_30:
								set30MinView(true);
								break;
							case AirLocation.AIR_LOCATION_FRE_MINUTE_60:
								set60MinView(true);
								break;
							default:
								break;
						}
					}
					break;
				}
				default:
					break;
			}
		}
	}

	private void setGpsView(boolean b)
	{
		if (b)
		{
			gpsFrequenceText.setTextColor(getResources().getColor(R.color.black));
			gpsFrequenceHigh.setTextColor(getResources().getColor(R.color.black_gray));
			cbGPSHigh.setClickable(true);
			// tv1Min.setTextColor(getResources().getColor(R.color.black_gray));
			// tv5Min.setTextColor(getResources().getColor(R.color.black_gray));
			// tv15Min.setTextColor(getResources().getColor(R.color.black_gray));
			// tv30Min.setTextColor(getResources().getColor(R.color.black_gray));
			// tv60Min.setTextColor(getResources().getColor(R.color.black_gray));
			rb1Min.setBackground(getResources().getDrawable(R.drawable.selector_radio_check));
			rb5Min.setBackground(getResources().getDrawable(R.drawable.selector_radio_check));
			rb15Min.setBackground(getResources().getDrawable(R.drawable.selector_radio_check));
			rb30Min.setBackground(getResources().getDrawable(R.drawable.selector_radio_check));
			rb60Min.setBackground(getResources().getDrawable(R.drawable.selector_radio_check));
			rb1Min.setClickable(true);
			rb5Min.setClickable(true);
			rb15Min.setClickable(true);
			rb30Min.setClickable(true);
			rb60Min.setClickable(true);
		}
		else
		{
			gpsFrequenceText.setTextColor(getResources().getColor(R.color.text_gray));
			gpsFrequenceHigh.setTextColor(getResources().getColor(R.color.text_gray));
			cbGPSHigh.setClickable(false);
			tv1Min.setTextColor(getResources().getColor(R.color.text_gray));
			tv5Min.setTextColor(getResources().getColor(R.color.text_gray));
			tv15Min.setTextColor(getResources().getColor(R.color.text_gray));
			tv30Min.setTextColor(getResources().getColor(R.color.text_gray));
			tv60Min.setTextColor(getResources().getColor(R.color.text_gray));
			rb1Min.setBackground(getResources().getDrawable(R.drawable.radio_normal));
			rb5Min.setBackground(getResources().getDrawable(R.drawable.radio_normal));
			rb15Min.setBackground(getResources().getDrawable(R.drawable.radio_normal));
			rb30Min.setBackground(getResources().getDrawable(R.drawable.radio_normal));
			rb60Min.setBackground(getResources().getDrawable(R.drawable.radio_normal));
			rb1Min.setChecked(false);
			rb5Min.setChecked(false);
			rb15Min.setChecked(false);
			rb30Min.setChecked(false);
			rb60Min.setChecked(false);
			rb1Min.setClickable(false);
			rb5Min.setClickable(false);
			rb15Min.setClickable(false);
			rb30Min.setClickable(false);
			rb60Min.setClickable(false);
		}
	}

	private void setHighFreView(boolean b)
	{
		if (b) // 高精度
		{
			tv1Min.setTextColor(getResources().getColor(R.color.black_gray));
			tv5Min.setTextColor(getResources().getColor(R.color.black_gray));
			tv15Min.setTextColor(getResources().getColor(R.color.black_gray));
			tv30Min.setTextColor(getResources().getColor(R.color.black_gray));
			tv60Min.setTextColor(getResources().getColor(R.color.black_gray));
			rb1Min.setBackground(getResources().getDrawable(R.drawable.radio_normal));
			rb5Min.setBackground(getResources().getDrawable(R.drawable.radio_normal));
			rb15Min.setBackground(getResources().getDrawable(R.drawable.radio_normal));
			rb30Min.setBackground(getResources().getDrawable(R.drawable.radio_normal));
			rb60Min.setBackground(getResources().getDrawable(R.drawable.radio_normal));
			rb1Min.setClickable(false);
			rb5Min.setClickable(false);
			rb15Min.setClickable(false);
			rb30Min.setClickable(false);
			rb60Min.setClickable(false);
			rb1Min.setChecked(false);
			rb5Min.setChecked(false);
			rb15Min.setChecked(false);
			rb30Min.setChecked(false);
			rb60Min.setChecked(false);
			set1MinView(false);
			set5MinView(false);
			set15MinView(false);
			set30MinView(false);
			set60MinView(false);
			cbGPSHigh.setBackground(getResources().getDrawable(R.drawable.btn_setting_open));
//			cbGPSHigh.setBackground(getResources().getDrawable(R.drawable.selector_btn_check_new));
		}
		else
		{
			tv5Min.setTextColor(getResources().getColor(R.color.black_gray));
			tv15Min.setTextColor(getResources().getColor(R.color.black_gray));
			tv30Min.setTextColor(getResources().getColor(R.color.black_gray));
			tv60Min.setTextColor(getResources().getColor(R.color.black_gray));
			rb1Min.setBackground(getResources().getDrawable(R.drawable.selector_radio_check));
			rb5Min.setBackground(getResources().getDrawable(R.drawable.selector_radio_check));
			rb15Min.setBackground(getResources().getDrawable(R.drawable.selector_radio_check));
			rb30Min.setBackground(getResources().getDrawable(R.drawable.selector_radio_check));
			rb60Min.setBackground(getResources().getDrawable(R.drawable.selector_radio_check));
			rb1Min.setClickable(true);
			rb5Min.setClickable(true);
			rb15Min.setClickable(true);
			rb30Min.setClickable(true);
			rb60Min.setClickable(true);
			cbGPSHigh.setChecked(false);
			cbGPSHigh.setBackground(getResources().getDrawable(R.drawable.btn_setting_close));
//			cbGPSHigh.setBackground(getResources().getDrawable(R.drawable.selector_btn_check_new));
		}
	}

	private void set1MinView(boolean b)
	{
		if (b)
		{
			mFrequenceSelected = 1;
			AirLocation.getInstance(MenuGpsActivity.this).loopRun(MenuGpsActivity.this, AirLocation.AIR_LOCATION_FRE_MINUTE_1, true);
			rb1Min.setChecked(true);
			tv1Min.setTextColor(getResources().getColor(R.color.black));
			set5MinView(false);
			set15MinView(false);
			set30MinView(false);
			set60MinView(false);
		}
		else
		{
			tv1Min.setTextColor(getResources().getColor(R.color.black_gray));
		}
	}

	private void set5MinView(boolean b)
	{
		if (b)
		{
			mFrequenceSelected = 2;
			AirLocation.getInstance(MenuGpsActivity.this).loopRun(MenuGpsActivity.this, AirLocation.AIR_LOCATION_FRE_MINUTE_5, true);
			rb5Min.setChecked(true);
			tv5Min.setTextColor(getResources().getColor(R.color.black));
			set1MinView(false);
			set15MinView(false);
			set30MinView(false);
			set60MinView(false);
		}
		else
		{
			tv5Min.setTextColor(getResources().getColor(R.color.black_gray));
		}
	}

	private void set15MinView(boolean b)
	{
		if (b)
		{
			mFrequenceSelected = 3;
			AirLocation.getInstance(MenuGpsActivity.this).loopRun(MenuGpsActivity.this, AirLocation.AIR_LOCATION_FRE_MINUTE_15, true);
			rb15Min.setChecked(true);
			tv15Min.setTextColor(getResources().getColor(R.color.black));
			set5MinView(false);
			set1MinView(false);
			set30MinView(false);
			set60MinView(false);
		}
		else
		{
			tv15Min.setTextColor(getResources().getColor(R.color.black_gray));
		}
	}

	private void set30MinView(boolean b)
	{
		if (b)
		{
			mFrequenceSelected = 4;
			AirLocation.getInstance(MenuGpsActivity.this).loopRun(MenuGpsActivity.this, AirLocation.AIR_LOCATION_FRE_MINUTE_30, true);
			rb30Min.setChecked(true);
			tv30Min.setTextColor(getResources().getColor(R.color.black));
			set5MinView(false);
			set15MinView(false);
			set1MinView(false);
			set60MinView(false);
		}
		else
		{
			tv30Min.setTextColor(getResources().getColor(R.color.black_gray));
		}
	}

	private void set60MinView(boolean b)
	{
		if (b)
		{
			mFrequenceSelected = 5;
			AirLocation.getInstance(MenuGpsActivity.this).loopRun(MenuGpsActivity.this, AirLocation.AIR_LOCATION_FRE_MINUTE_60, true);
			rb60Min.setChecked(true);
			tv60Min.setTextColor(getResources().getColor(R.color.black));
			set5MinView(false);
			set15MinView(false);
			set30MinView(false);
			set1MinView(false);
		}
		else
		{
			tv60Min.setTextColor(getResources().getColor(R.color.black_gray));
		}
	}

}
