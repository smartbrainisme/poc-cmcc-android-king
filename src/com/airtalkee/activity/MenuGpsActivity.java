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

	private Context context;

	@Override
	protected void onCreate(Bundle bundle)
	{
		super.onCreate(bundle);
		setRequestedOrientation(Config.screenOrientation);
		setContentView(R.layout.activity_tool_gps);
		context = this;
		gpsState = AirLocation.getInstance(context).getSettingState();
		mStateSelected = gpsState ? 0 : 1;
		int fre = AirLocation.getInstance(context).getSettingFrequence();
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
		// if (AirLocation.getInstance(this).GpsIsActive())
		// {
		// setGpsView(true);
		// }
		// else
		// {
		// setGpsView(false);
		// }
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
				{
					if (rb1Min.isChecked())
						set1MinView(true);
					break;
				}
				case R.id.rb_5_min:
				{
					if (rb5Min.isChecked())
						set5MinView(true);
					break;
				}
				case R.id.rb_15_min:
				{
					if (rb15Min.isChecked())
						set15MinView(true);
					break;
				}
				case R.id.rb_30_min:
				{
					if (rb30Min.isChecked())
						set30MinView(true);
					break;
				}
				case R.id.rb_60_min:
				{
					if (rb60Min.isChecked())
						set60MinView(true);
					break;
				}
				default:
					// setHighFreView(true);
					break;
			}
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
					break;
				}
				case AirLocation.AIR_LOCATION_FRE_MINUTE_1:
				{
					rb1Min.setChecked(true);
					break;
				}
				case AirLocation.AIR_LOCATION_FRE_MINUTE_5:
				{
					rb5Min.setChecked(true);
					break;
				}
				case AirLocation.AIR_LOCATION_FRE_MINUTE_15:
				{
					rb15Min.setChecked(true);
					break;
				}
				case AirLocation.AIR_LOCATION_FRE_MINUTE_30:
				{
					rb30Min.setChecked(true);
					break;
				}
				case AirLocation.AIR_LOCATION_FRE_MINUTE_60:
				{
					rb60Min.setChecked(true);
					break;
				}
				default:
					break;
			}
			AirLocation.getInstance(MenuGpsActivity.this).loopRun(MenuGpsActivity.this, value, true);
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

				AirLocation.getInstance(MenuGpsActivity.this).loopRun(MenuGpsActivity.this, AirLocation.AIR_LOCATION_FRE_NAVIGATE, true);
				setGpsView(true);
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
		final Context context = MenuGpsActivity.this;
		if (buttonView != null)
		{
			switch (buttonView.getId())
			{
				case R.id.talk_setting_gps:
				{
					if (cbGPS.isChecked())// 是否开启位置回传
					{
						setGpsView(true);
					}
					else
					{
						setGpsView(false);
						AirLocation.getInstance(context).loopTerminate();
					}
					break;
				}
				case R.id.gps_frequence_high:
				{
					if (cbGPSHigh.isChecked())// 是否开启高精度模式
					{
						if (cbGPS.isChecked())
						{
							setHighFreView(true);
							AirLocation.getInstance(MenuGpsActivity.this).loopRun(MenuGpsActivity.this, AirLocation.AIR_LOCATION_FRE_NAVIGATE, true);
						}
					}
					else
					{
						setHighFreView(false);
						switch (mFrequenceValue[mFrequenceSelected])
						{
							case AirLocation.AIR_LOCATION_FRE_MINUTE_1:
								rb1Min.setChecked(true);
								AirLocation.getInstance(MenuGpsActivity.this).loopRun(MenuGpsActivity.this, AirLocation.AIR_LOCATION_FRE_MINUTE_1, true);
								break;
							case AirLocation.AIR_LOCATION_FRE_MINUTE_5:
								rb5Min.setChecked(true);
								AirLocation.getInstance(MenuGpsActivity.this).loopRun(MenuGpsActivity.this, AirLocation.AIR_LOCATION_FRE_MINUTE_5, true);
								break;
							case AirLocation.AIR_LOCATION_FRE_MINUTE_15:
								rb15Min.setChecked(true);
								AirLocation.getInstance(MenuGpsActivity.this).loopRun(MenuGpsActivity.this, AirLocation.AIR_LOCATION_FRE_MINUTE_15, true);
								break;
							case AirLocation.AIR_LOCATION_FRE_MINUTE_30:
								rb30Min.setChecked(true);
								AirLocation.getInstance(MenuGpsActivity.this).loopRun(MenuGpsActivity.this, AirLocation.AIR_LOCATION_FRE_MINUTE_30, true);
								break;
							case AirLocation.AIR_LOCATION_FRE_MINUTE_60:
								rb60Min.setChecked(true);
								AirLocation.getInstance(MenuGpsActivity.this).loopRun(MenuGpsActivity.this, AirLocation.AIR_LOCATION_FRE_MINUTE_60, true);
								break;
							default:
								rb5Min.setChecked(true);
								AirLocation.getInstance(MenuGpsActivity.this).loopRun(MenuGpsActivity.this, AirLocation.AIR_LOCATION_FRE_MINUTE_5, true);
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
			cbGPSHigh.setEnabled(true);

			switch (mFrequenceSelected)
			{
				case 0:
					setHighFreView(true);
					cbGPSHigh.setChecked(true);
					AirLocation.getInstance(MenuGpsActivity.this).loopRun(MenuGpsActivity.this, AirLocation.AIR_LOCATION_FRE_NAVIGATE, true);
					break;
				case 1:
					rb1Min.setEnabled(true);
					rb5Min.setEnabled(true);
					rb15Min.setEnabled(true);
					rb30Min.setEnabled(true);
					rb60Min.setEnabled(true);
					rb1Min.setChecked(true);
					AirLocation.getInstance(MenuGpsActivity.this).loopRun(MenuGpsActivity.this, AirLocation.AIR_LOCATION_FRE_MINUTE_1, true);
					set1MinView(true);
					break;
				case 2:
					rb1Min.setEnabled(true);
					rb5Min.setEnabled(true);
					rb15Min.setEnabled(true);
					rb30Min.setEnabled(true);
					rb60Min.setEnabled(true);
					rb5Min.setChecked(true);
					AirLocation.getInstance(MenuGpsActivity.this).loopRun(MenuGpsActivity.this, AirLocation.AIR_LOCATION_FRE_MINUTE_5, true);
					set5MinView(true);
					break;
				case 3:
					rb1Min.setEnabled(true);
					rb5Min.setEnabled(true);
					rb15Min.setEnabled(true);
					rb30Min.setEnabled(true);
					rb60Min.setEnabled(true);
					rb15Min.setChecked(true);
					AirLocation.getInstance(MenuGpsActivity.this).loopRun(MenuGpsActivity.this, AirLocation.AIR_LOCATION_FRE_MINUTE_15, true);
					set15MinView(true);
					break;
				case 4:
					rb1Min.setEnabled(true);
					rb5Min.setEnabled(true);
					rb15Min.setEnabled(true);
					rb30Min.setEnabled(true);
					rb60Min.setEnabled(true);
					rb30Min.setChecked(true);
					AirLocation.getInstance(MenuGpsActivity.this).loopRun(MenuGpsActivity.this, AirLocation.AIR_LOCATION_FRE_MINUTE_30, true);
					set30MinView(true);
					break;
				case 5:
					rb1Min.setEnabled(true);
					rb5Min.setEnabled(true);
					rb15Min.setEnabled(true);
					rb30Min.setEnabled(true);
					rb60Min.setEnabled(true);
					rb60Min.setChecked(true);
					AirLocation.getInstance(MenuGpsActivity.this).loopRun(MenuGpsActivity.this, AirLocation.AIR_LOCATION_FRE_MINUTE_60, true);
					set60MinView(true);
					break;
				default:
					break;
			}
		}
		else
		{
			if (mFrequenceSelected == 0)
			{
				cbGPSHigh.setChecked(true);
			}
			gpsFrequenceText.setTextColor(getResources().getColor(R.color.text_gray));
			gpsFrequenceHigh.setTextColor(getResources().getColor(R.color.text_gray));

			tv1Min.setTextColor(getResources().getColor(R.color.text_gray));
			tv5Min.setTextColor(getResources().getColor(R.color.text_gray));
			tv15Min.setTextColor(getResources().getColor(R.color.text_gray));
			tv30Min.setTextColor(getResources().getColor(R.color.text_gray));
			tv60Min.setTextColor(getResources().getColor(R.color.text_gray));

			cbGPSHigh.setEnabled(false);
			rb1Min.setEnabled(false);
			rb5Min.setEnabled(false);
			rb15Min.setEnabled(false);
			rb30Min.setEnabled(false);
			rb60Min.setEnabled(false);
		}
	}

	private void setHighFreView(boolean b)
	{
		if (b) // 高精度
		{
			mStateSelected = 0;
			mFrequenceSelected = 0;
			tv1Min.setTextColor(getResources().getColor(R.color.black_gray));
			tv5Min.setTextColor(getResources().getColor(R.color.black_gray));
			tv15Min.setTextColor(getResources().getColor(R.color.black_gray));
			tv30Min.setTextColor(getResources().getColor(R.color.black_gray));
			tv60Min.setTextColor(getResources().getColor(R.color.black_gray));
			rb1Min.setEnabled(false);
			rb5Min.setEnabled(false);
			rb15Min.setEnabled(false);
			rb30Min.setEnabled(false);
			rb60Min.setEnabled(false);
			set1MinView(false);
			set5MinView(false);
			set15MinView(false);
			set30MinView(false);
			set60MinView(false);
			AirLocation.getInstance(MenuGpsActivity.this).loopRun(MenuGpsActivity.this, AirLocation.AIR_LOCATION_FRE_NAVIGATE, true);
		}
		else
		{
			tv1Min.setTextColor(getResources().getColor(R.color.black_gray));
			tv5Min.setTextColor(getResources().getColor(R.color.black_gray));
			tv15Min.setTextColor(getResources().getColor(R.color.black_gray));
			tv30Min.setTextColor(getResources().getColor(R.color.black_gray));
			tv60Min.setTextColor(getResources().getColor(R.color.black_gray));
			rb1Min.setEnabled(true);
			rb5Min.setEnabled(true);
			rb15Min.setEnabled(true);
			rb30Min.setEnabled(true);
			rb60Min.setEnabled(true);
		}
	}

	private void set1MinView(boolean b)
	{
		if (b)
		{
			mFrequenceSelected = 1;
			tv1Min.setTextColor(getResources().getColor(R.color.black));
			set5MinView(false);
			set15MinView(false);
			set30MinView(false);
			set60MinView(false);
			AirLocation.getInstance(MenuGpsActivity.this).loopRun(MenuGpsActivity.this, AirLocation.AIR_LOCATION_FRE_MINUTE_1, true);
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
			tv5Min.setTextColor(getResources().getColor(R.color.black));
			set1MinView(false);
			set15MinView(false);
			set30MinView(false);
			set60MinView(false);
			AirLocation.getInstance(MenuGpsActivity.this).loopRun(MenuGpsActivity.this, AirLocation.AIR_LOCATION_FRE_MINUTE_5, true);
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
			tv15Min.setTextColor(getResources().getColor(R.color.black));
			set5MinView(false);
			set1MinView(false);
			set30MinView(false);
			set60MinView(false);
			AirLocation.getInstance(MenuGpsActivity.this).loopRun(MenuGpsActivity.this, AirLocation.AIR_LOCATION_FRE_MINUTE_15, true);
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
			tv30Min.setTextColor(getResources().getColor(R.color.black));
			set5MinView(false);
			set15MinView(false);
			set1MinView(false);
			set60MinView(false);
			AirLocation.getInstance(MenuGpsActivity.this).loopRun(MenuGpsActivity.this, AirLocation.AIR_LOCATION_FRE_MINUTE_30, true);
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
			tv60Min.setTextColor(getResources().getColor(R.color.black));
			set5MinView(false);
			set15MinView(false);
			set30MinView(false);
			set1MinView(false);
			AirLocation.getInstance(MenuGpsActivity.this).loopRun(MenuGpsActivity.this, AirLocation.AIR_LOCATION_FRE_MINUTE_60, true);
		}
		else
		{
			tv60Min.setTextColor(getResources().getColor(R.color.black_gray));
		}
	}

}
