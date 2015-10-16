package com.airtalkee.activity;

import java.util.List;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airtalkee.R;
import com.airtalkee.Util.ThemeUtil;
import com.airtalkee.Util.Util;
import com.airtalkee.adapter.AdapterAttendace;
import com.airtalkee.config.Config;
import com.airtalkee.listener.OnMmiLocationListener;
import com.airtalkee.location.AirLocation;
import com.airtalkee.location.AirLocationImp;
import com.airtalkee.sdk.AirtalkeeReport;
import com.airtalkee.sdk.controller.AirAttendanceController;
import com.airtalkee.sdk.controller.AirAttendanceController.AirAttendanceListener;
import com.airtalkee.sdk.entity.AirAttendance;
import com.airtalkee.widget.MListView;

public class MenuAttendanceActivity extends ActivityBase implements OnClickListener, AirAttendanceListener, OnMmiLocationListener
{

	private MListView mList;
	private AdapterAttendace mListAdapter;
	private String mAttendanceRuleId = "";
	private int mAttendanceType = AirAttendance.TYPE_NONE;

	@Override
	protected void onCreate(Bundle bundle)
	{
		// TODO Auto-generated method stub
		super.onCreate(bundle);
		setRequestedOrientation(Config.screenOrientation);
		setContentView(R.layout.activity_tool_attendance);
		doInitView();
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onStart()
	{
		// TODO Auto-generated method stub
		super.onStart();
		showDialog(R.id.talk_dialog_waiting);
		AirAttendanceController.getInstance().setAirAttendanceListener(this);
		AirAttendanceController.getInstance().doAttendanceState();
	}

	private void doInitView()
	{
		TextView ivTitle = (TextView) findViewById(R.id.tv_main_title);
		ivTitle.setText(R.string.talk_tools_attend);
		View btnLeft = findViewById(R.id.menu_left_button);
		ImageView ivLeft = (ImageView) findViewById(R.id.bottom_left_icon);
		ivLeft.setImageResource(ThemeUtil.getResourceId(R.attr.theme_ic_topbar_back, this) );
		btnLeft.setOnClickListener(this);

		RelativeLayout ivRightLay = (RelativeLayout) findViewById(R.id.talk_menu_right_button);
		ImageView ivRight = (ImageView) findViewById(R.id.bottom_right_icon);
		ivRight.setVisibility(View.GONE);
		ivRightLay.setVisibility(View.INVISIBLE);

		mList = (MListView) findViewById(R.id.talk_attendance_list);
		mListAdapter = new AdapterAttendace(this);
		mList.setAdapter(mListAdapter);
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
		// TODO Auto-generated method stub
		switch (v.getId())
		{
			case R.id.menu_left_button:
			case R.id.bottom_left_icon:
				finish();
				break;
		}
	}
	
	@SuppressWarnings("deprecation")
	protected Dialog onCreateDialog(int id)
	{
		if (id == R.id.talk_dialog_waiting)
		{
			ProgressDialog dialog = new ProgressDialog(this);
			dialog.setMessage(getString(R.string.talk_tip_waiting));
			return dialog;
		}
		return super.onCreateDialog(id);
	}


	@SuppressWarnings("deprecation")
	public void doAttendanceOperation(String ruleId, int type)
	{
		mAttendanceRuleId = ruleId;
		mAttendanceType = type;
		//Util.Toast(this, getString(R.string.talk_attend_tip_opr_locating));
		showDialog(R.id.talk_dialog_waiting);
		AirLocation.getInstance(this).onceGet(this, 30);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onAttendanceOperation(boolean isOk, List<AirAttendance> attendances)
	{
		// TODO Auto-generated method stub
		if (isOk)
		{
			mListAdapter.notifyData(attendances);
			mListAdapter.notifyDataSetChanged();
			Util.Toast(this, getString(R.string.talk_attend_tip_ok_operation));
		}
		else
		{
			Util.Toast(this, getString(R.string.talk_attend_tip_failed_operation));
		}
		mAttendanceRuleId = "";
		mAttendanceType = AirAttendance.TYPE_NONE;
		removeDialog(R.id.talk_dialog_waiting);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onAttendanceState(boolean isOk, List<AirAttendance> attendances)
	{
		// TODO Auto-generated method stub
		removeDialog(R.id.talk_dialog_waiting);
		if (isOk)
		{
			mListAdapter.notifyData(attendances);
			mListAdapter.notifyDataSetChanged();
		}
		else
		{
			Util.Toast(this, getString(R.string.talk_attend_tip_failed_state));
		}
	}

	@Override
	public void onLocationChanged(boolean isOk, int id, int type, double latitude, double longitude, double altitude, float speed, String time)
	{
		// TODO Auto-generated method stub
		if (id == AirLocation.AIR_LOCATION_ID_ONCE)
		{
			if (mAttendanceType != AirAttendance.TYPE_NONE && !TextUtils.isEmpty(mAttendanceRuleId))
			{
				if (isOk)
				{
					int t = AirtalkeeReport.LOCATION_TYPE_GPS;
					if (type == AirLocationImp.LOCATION_TYPE_GPS)
						t = AirtalkeeReport.LOCATION_TYPE_GPS;
					else if (type == AirLocationImp.LOCATION_TYPE_CELL_BAIDU)
						t = AirtalkeeReport.LOCATION_TYPE_CELL;
					if (mAttendanceType == AirAttendance.TYPE_SHOULD_ON)
						AirAttendanceController.getInstance().doAttendanceOperation(mAttendanceRuleId, AirAttendance.TYPE_SHOULD_ON, t, latitude+"", longitude+"");
					else if (mAttendanceType == AirAttendance.TYPE_SHOULD_OFF)
						AirAttendanceController.getInstance().doAttendanceOperation(mAttendanceRuleId, AirAttendance.TYPE_SHOULD_OFF, t, latitude+"", longitude+"");
					Util.Toast(this, getString(R.string.talk_attend_tip_opr_doing));
				}
				else
				{
					Util.Toast(this, getString(R.string.talk_attend_tip_failed_location));
					mAttendanceRuleId = "";
					mAttendanceType = AirAttendance.TYPE_NONE;
				}
			}
		}
	}


}
