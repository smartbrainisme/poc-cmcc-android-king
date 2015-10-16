package com.airtalkee.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airtalkee.R;
import com.airtalkee.Util.ThemeUtil;
import com.airtalkee.config.Config;
import com.airtalkee.listener.OnMmiLocationListener;
import com.airtalkee.location.AirLocation;
import com.airtalkee.location.AirLocationImp;
import com.airtalkee.sdk.entity.AirFunctionSetting;

public class MenuGpsActivity extends ActivityBase implements OnClickListener, OnMmiLocationListener
{

	private ImageView gps_type;
	private TextView gps_x, gps_y, gps_h, gps_o, gps_s, gps_t;
	private TextView gps_state, gps_frequence;
	int[] mFrequenceValue = { AirLocation.AIR_LOCATION_FRE_NAVIGATE, AirLocation.AIR_LOCATION_FRE_MINUTE_1, AirLocation.AIR_LOCATION_FRE_MINUTE_5,
		AirLocation.AIR_LOCATION_FRE_MINUTE_15, AirLocation.AIR_LOCATION_FRE_MINUTE_30, AirLocation.AIR_LOCATION_FRE_MINUTE_60 };
	String[] mFrequence = null;
	String[] mState = null;
	int mStateSelected = 0;
	int mFrequenceSelected = 0;

	@Override
	protected void onCreate(Bundle bundle)
	{
		// TODO Auto-generated method stub
		super.onCreate(bundle);
		setRequestedOrientation(Config.screenOrientation);
		setContentView(R.layout.activity_tool_gps);

		mStateSelected = AirLocation.getInstance(this).getSettingState() ? 0 : 1;
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
		// TODO Auto-generated method stub
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
		ivLeft.setImageResource(ThemeUtil.getResourceId(R.attr.theme_ic_topbar_back, this) );
		btnLeft.setOnClickListener(this);

		RelativeLayout ivRightLay = (RelativeLayout) findViewById(R.id.talk_menu_right_button);
		ImageView ivRight = (ImageView) findViewById(R.id.bottom_right_icon);
		ivRight.setVisibility(View.GONE);
		ivRightLay.setVisibility(View.INVISIBLE);

		mState = getResources().getStringArray(R.array.gps_state);
		mFrequence = getResources().getStringArray(R.array.gps_frequence);
		gps_type = (ImageView) findViewById(R.id.gps_type);
		gps_x = (TextView) findViewById(R.id.gps_x);
		gps_y = (TextView) findViewById(R.id.gps_y);
		gps_h = (TextView) findViewById(R.id.gps_h);
		gps_s = (TextView) findViewById(R.id.gps_s);
		gps_o = (TextView) findViewById(R.id.gps_o);
		gps_t = (TextView) findViewById(R.id.gps_t);
		gps_state = (TextView) findViewById(R.id.gps_state);
		gps_state.setText(mState[mStateSelected]);
		gps_frequence = (TextView) findViewById(R.id.gps_frequence);
		gps_frequence.setText(mFrequence[mFrequenceSelected]);
		if (Config.funcCenterLocation == AirFunctionSetting.SETTING_ENABLE)
		{
			findViewById(R.id.talk_gps_state_item).setOnClickListener(this);
			findViewById(R.id.talk_gps_frequence_item).setOnClickListener(this);
		}
		else
		{
			gps_state.setTextColor(0x60ffffff);
			gps_frequence.setTextColor(0x60ffffff);
			findViewById(R.id.gps_state_selector).setVisibility(View.GONE);
			findViewById(R.id.gps_frequence_selector).setVisibility(View.GONE);
		}

		findViewById(R.id.gps_state_selector_setting).setOnClickListener(this);
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
		if (type == AirLocationImp.LOCATION_TYPE_GPS)
			gps_type.setImageResource(R.drawable.loc_gps);
		else if (type == AirLocationImp.LOCATION_TYPE_CELL_BAIDU)
			gps_type.setImageResource(R.drawable.loc_cell);
		else
			gps_type.setImageResource(R.drawable.transparent);
		gps_x.setText(longitude + "");
		gps_y.setText(latitude + "");
		gps_h.setText(altitude + "");
		gps_s.setText(speed == 0 ? "" : speed + "");
		gps_o.setText("");
		gps_t.setText(time + "");
		gps_state.setText(mState[mStateSelected]);
		gps_frequence.setText(mFrequence[mFrequenceSelected]);
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
				gps_state.setText(mState[mStateSelected]);
				gps_frequence.setText(mFrequence[mFrequenceSelected]);
				AirLocation.getInstance(MenuGpsActivity.this).loopRun(MenuGpsActivity.this, AirLocation.AIR_LOCATION_FRE_NAVIGATE, true);
			}
		});

		builder.setNegativeButton(getString(R.string.talk_no), new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int whichButton)
			{
				dialog.cancel();
			}
		});
		builder.show();
	}

	@Override
	public void onClick(View v)
	{
		// TODO Auto-generated method stub
		final Context context = this;
		switch (v.getId())
		{
			case R.id.menu_left_button:
			case R.id.bottom_left_icon:
				finish();
				break;
			case R.id.talk_gps_state_item:
			{
				new AlertDialog.Builder(this).setTitle(R.string.talk_gps_state).setItems(R.array.gps_state, new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						if (mStateSelected != which)
						{
							if (which == 0 && mFrequenceSelected == 0)
							{
								confirmGpsHighPrecision();
							}
							else
							{
								mStateSelected = which;
								gps_state.setText(mState[mStateSelected]);
								if (mStateSelected == 0)
								{
									AirLocation.getInstance(context).loopRun(MenuGpsActivity.this, mFrequenceValue[mFrequenceSelected], true);
								}
								else
								{
									AirLocation.getInstance(context).loopTerminate();
								}
							}
						}
					}
				}).show();
				break;
			}
			case R.id.talk_gps_frequence_item:
			{
				new AlertDialog.Builder(this).setTitle(R.string.talk_gps_frequence).setItems(R.array.gps_frequence, new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						if (mFrequenceSelected != which)
						{
							if (which == 0 && mStateSelected == 0)
							{
								confirmGpsHighPrecision();
							}
							else
							{
								mFrequenceSelected = which;
								gps_frequence.setText(mFrequence[mFrequenceSelected]);
								if (mStateSelected == 0)
								{
									AirLocation.getInstance(context).loopRun(MenuGpsActivity.this, mFrequenceValue[mFrequenceSelected], true);
								}
							}
						}
					}
				}).show();
				break;
			}
			case R.id.gps_state_selector_setting:
			{
				AirLocation.getInstance(this).GpsActive();
				break;
			}
		}
	}

	@Override
	public void onLocationChanged(boolean isOk, int id, int type, double latitude, double longitude, double altitude, float speed, String time)
	{
		// TODO Auto-generated method stub
		if (isOk && id == AirLocation.AIR_LOCATION_ID_LOOP)
		{
			refreshGpsData(type, latitude, longitude, altitude, speed, time);
		}
	}

}
