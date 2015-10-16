package com.airtalkee.activity;

import java.util.List;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airtalkee.R;
import com.airtalkee.Util.AirMmiTimer;
import com.airtalkee.Util.AirMmiTimerListener;
import com.airtalkee.Util.ThemeUtil;
import com.airtalkee.Util.Util;
import com.airtalkee.config.Config;
import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.AirtalkeeUserInfo;
import com.airtalkee.sdk.OnUserInfoListener;
import com.airtalkee.sdk.entity.AirContact;
import com.airtalkee.sdk.entity.AirContactGroup;
import com.airtalkee.sdk.entity.AirFunctionSetting;
import com.airtalkee.sdk.entity.AirStatisticsNetworkByte;
import com.airtalkee.sdk.util.IOoperate;
import com.airtalkee.sdk.util.Utils;

public class MoreActivity extends ActivityBase implements OnClickListener, AirMmiTimerListener, OnUserInfoListener
{

	public TextView tvUserName;
	private TextView tvVersion;

	private LinearLayout statLayout;
	private TextView statLayoutTime;
	private TextView statLayoutBytes;

	private final String STAT_RECV = "STAT_RECV";
	private final String STAT_SENT = "STAT_SENT";
	private final String STAT_TIME = "STAT_TIME";

	private int gStatRecv = 0;
	private int gStatSent = 0;
	private long gStatTime = 0;
	private IOoperate iOperate = null;

	@Override
	protected void onCreate(Bundle bundle)
	{
		// TODO Auto-generated method stub
		super.onCreate(bundle);
		setRequestedOrientation(Config.screenOrientation);
		setContentView(R.layout.activity_tool);
		iOperate = new IOoperate();
		gStatRecv = iOperate.getInt(STAT_RECV, 0);
		gStatSent = iOperate.getInt(STAT_SENT, 0);
		gStatTime = iOperate.getLong(STAT_TIME);
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
		ivTitle.setText(R.string.talk_tools);
		View btnLeft = findViewById(R.id.menu_left_button);
		ImageView ivLeft = (ImageView) findViewById(R.id.bottom_left_icon);
		ivLeft.setImageResource(ThemeUtil.getResourceId(R.attr.theme_ic_topbar_back, this));
		btnLeft.setOnClickListener(this);

		RelativeLayout ivRightLay = (RelativeLayout) findViewById(R.id.talk_menu_right_button);
		ImageView ivRight = (ImageView) findViewById(R.id.bottom_right_icon);
		ivRight.setVisibility(View.GONE);
		ivRightLay.setVisibility(View.INVISIBLE);

		// GPS item
		if (Config.funcCenterLocation == AirFunctionSetting.SETTING_DISABLE)
		{
			findViewById(R.id.talk_lv_tool_gps).setVisibility(View.GONE);
			findViewById(R.id.talk_lv_tool_gps_divider).setVisibility(View.GONE);
		}
		else
		{
			if (Config.funcCenterLocationMenuShow)
			{
//				findViewById(R.id.talk_lv_tool_gps).setOnClickListener(this);
//				findViewById(R.id.talk_lv_tool_gps).setVisibility(View.VISIBLE);
//				findViewById(R.id.talk_lv_tool_gps_divider).setVisibility(View.VISIBLE);

				// 2015年10月15日16:33:27 zuocy
				findViewById(R.id.talk_lv_tool_gps).setVisibility(View.GONE);
				findViewById(R.id.talk_lv_tool_gps_divider).setVisibility(View.GONE);
			}
			else
			{
//				findViewById(R.id.talk_lv_tool_gps).setVisibility(View.GONE);
//				findViewById(R.id.talk_lv_tool_gps_divider).setVisibility(View.GONE);
				findViewById(R.id.talk_lv_tool_gps).setOnClickListener(this);
				findViewById(R.id.talk_lv_tool_gps).setVisibility(View.VISIBLE);
				findViewById(R.id.talk_lv_tool_gps_divider).setVisibility(View.VISIBLE);
			}
		}
		findViewById(R.id.talk_setting_voice).setOnClickListener(this);
		findViewById(R.id.talk_lv_tool_infosys).setOnClickListener(this);
		// Report item
//		if (Config.funcCenterReport)
//		{
//			findViewById(R.id.talk_lv_tool_report).setOnClickListener(this);
//			findViewById(R.id.talk_lv_tool_report).setVisibility(View.VISIBLE);
//			findViewById(R.id.talk_lv_tool_report_divider).setVisibility(View.VISIBLE);
//		}
//		else
//		{
//			findViewById(R.id.talk_lv_tool_report).setVisibility(View.GONE);
//			findViewById(R.id.talk_lv_tool_report_divider).setVisibility(View.GONE);
//		}

		// Call center item
		if (Config.funcCenterCall == AirFunctionSetting.SETTING_DISABLE)
		{
			findViewById(R.id.talk_lv_tool_call).setVisibility(View.GONE);
			findViewById(R.id.talk_lv_tool_call_divider).setVisibility(View.GONE);
		}
		else
		{
			if (Config.funcCenterCallMenuShow)
			{
				findViewById(R.id.talk_lv_tool_call).setOnClickListener(this);
				findViewById(R.id.talk_lv_tool_call).setVisibility(View.VISIBLE);
				findViewById(R.id.talk_lv_tool_call_divider).setVisibility(View.VISIBLE);
			}
			else
			{
				findViewById(R.id.talk_lv_tool_call).setVisibility(View.GONE);
				findViewById(R.id.talk_lv_tool_call_divider).setVisibility(View.GONE);
			}
		}

		// Manual item
		if (Config.funcManual)
		{
			findViewById(R.id.talk_lv_tool_manual).setOnClickListener(this);
			findViewById(R.id.talk_lv_tool_manual).setVisibility(View.VISIBLE);
			findViewById(R.id.talk_lv_tool_manual_divider).setVisibility(View.VISIBLE);
		}
		else
		{
			findViewById(R.id.talk_lv_tool_manual).setVisibility(View.GONE);
			findViewById(R.id.talk_lv_tool_manual_divider).setVisibility(View.GONE);
		}

		// Defect report
		if (Config.funcfeedback)
		{
			findViewById(R.id.talk_lv_tool_defect).setOnClickListener(this);
			findViewById(R.id.talk_lv_tool_defect).setVisibility(View.VISIBLE);
			findViewById(R.id.talk_lv_tool_defect_divider).setVisibility(View.VISIBLE);
		}
		else
		{
			findViewById(R.id.talk_lv_tool_defect).setVisibility(View.GONE);
			findViewById(R.id.talk_lv_tool_defect_divider).setVisibility(View.GONE);
		}

		// Sysinfo item
		if (!Utils.isEmpty(Config.serverUrlInfosys))
		{
			findViewById(R.id.talk_lv_tool_infosys).setOnClickListener(this);
			findViewById(R.id.talk_lv_tool_infosys).setVisibility(View.VISIBLE);
			findViewById(R.id.talk_lv_tool_infosys_divider).setVisibility(View.VISIBLE);
		}
		else
		{
			findViewById(R.id.talk_lv_tool_infosys).setVisibility(View.GONE);
			findViewById(R.id.talk_lv_tool_infosys_divider).setVisibility(View.GONE);
		}

		// Statistic item
		statLayout = (LinearLayout) findViewById(R.id.talk_tv_statistic);
		statLayoutTime = (TextView) findViewById(R.id.talk_tv_statistic_time);
		statLayoutBytes = (TextView) findViewById(R.id.talk_tv_statistic_bytes);

		if (Config.funcStatisticNetwork)
		{
			statLayout.setOnClickListener(this);
		}
		else
		{
			statLayout.setVisibility(View.GONE);
		}

		if (Config.funcThemeChange)
		{
			findViewById(R.id.talk_change_theme).setOnClickListener(this);
		}
		else
		{
			findViewById(R.id.talk_change_theme).setVisibility(View.GONE);
			findViewById(R.id.talk_change_theme_divider).setVisibility(View.GONE);
		}

		//TaskDspatch
		if (Config.funcTaskDispatch)
		{
			findViewById(R.id.talk_lv_tools_task).setVisibility(View.VISIBLE);
			findViewById(R.id.talk_lv_tool_task_divider).setVisibility(View.VISIBLE);
			findViewById(R.id.talk_lv_tools_task).setOnClickListener(this);
		}

		//Attendance
		if (Config.funcAttendance)
		{
			findViewById(R.id.talk_lv_tools_attend).setVisibility(View.VISIBLE);
			findViewById(R.id.talk_lv_tool_attend_divider).setVisibility(View.VISIBLE);
			findViewById(R.id.talk_lv_tools_attend).setOnClickListener(this);
		}

		// Others
		findViewById(R.id.talk_lv_tool_exit).setOnClickListener(this);
		findViewById(R.id.talk_lv_tool_update).setOnClickListener(this);
		findViewById(R.id.talk_tv_user_name_panel).setOnClickListener(this);

		tvUserName = (TextView) findViewById(R.id.talk_tv_user_name);
		tvUserName.setText(AirtalkeeAccount.getInstance().getUserName());
		tvVersion = (TextView) findViewById(R.id.talk_tv_version);
		tvVersion.setText(getString(R.string.talk_version) + Config.VERSION_CODE);
		if (Config.funcShowCustomLogo)
		{
			TextView logoText = (TextView) findViewById(R.id.talk_copyright_text);
			logoText.setVisibility(View.VISIBLE);
			logoText.setText(Config.funcShowCustomLogoStringId2);
		}

		if (Config.marketCode == Config.MARKET_UNI_STRONG)
		{
			findViewById(R.id.talk_lv_tool_exit).setVisibility(View.GONE);
			findViewById(R.id.talk_exit_divider).setVisibility(View.GONE);
		}
	}

	@Override
	public void finish()
	{
		// TODO Auto-generated method stub
		super.finish();
	}

	@Override
	protected void onPause()
	{
		// TODO Auto-generated method stub
		super.onPause();
		AirtalkeeUserInfo.getInstance().setOnUserInfoListener(null);
		if (statLayout.getVisibility() == View.VISIBLE)
		{
			AirMmiTimer.getInstance().TimerUnregister(this, this);
		}
	}

	@Override
	protected void onResume()
	{
		// TODO Auto-generated method stub
		super.onResume();
		AirtalkeeUserInfo.getInstance().setOnUserInfoListener(this);
		if (statLayout.getVisibility() == View.VISIBLE)
		{
			AirMmiTimer.getInstance().TimerRegister(this, this, false, false, 1000, true, null);
		}
	}

	@Override
	public void onClick(View v)
	{
		// TODO Auto-generated method stub
		switch (v.getId())
		{
			case R.id.menu_left_button:
			case R.id.bottom_left_icon:
			{
				finish();
				break;
			}
			case R.id.talk_lv_tool_gps:
			{
				Intent it = new Intent(this, MenuGpsActivity.class);
				startActivity(it);
				break;
			}
			case R.id.talk_setting_voice:
			{
				Intent it = new Intent(this, MenuSettingPttActivity.class);
				startActivity(it);
				break;
			}
			case R.id.talk_lv_tool_report:
			{
				Intent it = new Intent(this, MenuReportActivity.class);
				it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(it);
				break;
			}
			case R.id.talk_lv_tool_exit:
			{
				if (MainActivity.getInstance() != null)
				{
					Builder builder = MainActivity.getInstance().BuildExitDialog(this);
					builder.show();
				}
				break;
			}
			case R.id.talk_change_theme:
			{
				ThemeUtil.changeTheme(this);
				break;
			}
			case R.id.talk_lv_tool_call:
			{
				if (MainActivity.getInstance() != null)
					MainActivity.getInstance().viewMiddle.sessionBox.sessionBoxMember.callStationCenter();
				break;
			}
			case R.id.talk_lv_tool_update:
			{
				Intent it = new Intent(this, MenuSettingActivity.class);
				startActivity(it);
				break;
			}
			case R.id.talk_lv_tool_manual:
			{
				Intent it = new Intent(this, MenuManualActivity.class);
				startActivity(it);
				break;
			}
			case R.id.talk_lv_tool_defect:
			{
				Intent it = new Intent(this, MenuDefectReportActivity.class);
				startActivity(it);
				break;
			}
			case R.id.talk_lv_tool_infosys:
			{
				Intent LaunchIntent = getPackageManager().getLaunchIntentForPackage("com.wf.info");
				if (LaunchIntent != null)
				{
					startActivity(LaunchIntent);
				}
				else
				{
					Util.Toast(this, getString(R.string.talk_tools_infosys_null));
					Uri uri = Uri.parse(Config.serverUrlInfosys);
					Intent web = new Intent(Intent.ACTION_VIEW, uri);
					startActivity(web);
				}
				break;
			}
			case R.id.talk_tv_user_name_panel:
			{
				final Context context = this;
				final EditText input = new EditText(this);
				input.setSingleLine();
				input.setHint(AirtalkeeUserInfo.getInstance().getUserInfo().getDisplayName());
				new AlertDialog.Builder(this).setTitle(getString(R.string.talk_user_info_update_name)).setView(input)
					.setPositiveButton(getString(R.string.talk_ok), new DialogInterface.OnClickListener()
					{

						public void onClick(DialogInterface dialog, int which)
						{
							String value = input.getText().toString();
							if (!Utils.isEmpty(value))
							{
								if (value.length() > MenuSettingActivity.SETTING_DISPLAYNAME_LEN)
								{
									Util.Toast(context, getString(R.string.talk_user_info_update_name_error));
								}
								else
								{
									AirtalkeeUserInfo.getInstance().UserInfoUpdate(value.trim());
									Util.Toast(context, getString(R.string.talk_user_info_update_name_doing));
								}
							}
						}

					}).setNegativeButton(getString(R.string.talk_no), null).show();

				break;
			}
			case R.id.talk_tv_statistic:
			{
				AirtalkeeAccount.getInstance().statisticsNetworkByteClean();

				gStatRecv = 0;
				gStatSent = 0;
				gStatTime = 0;
				try
				{
					iOperate.putInt(STAT_RECV, 0);
					iOperate.putInt(STAT_SENT, 0);
					iOperate.putLong(STAT_TIME, 0);
				}
				catch (Exception e)
				{
					// TODO: handle exception
				}

				statLayoutTime.setText(getString(R.string.talk_statistic_time) + "00:00:00");
				statLayoutBytes.setText(getString(R.string.talk_statistic_bytes) + "0.0K");
				Util.Toast(this, getString(R.string.talk_statistic_tip));
				break;
			}
			case R.id.talk_lv_tools_task:
			{
				Intent it = new Intent(this, MenuTaskActivity.class);
				startActivity(it);
				break;
			}
			case R.id.talk_lv_tools_attend:
			{
				Intent it = new Intent(this, MenuAttendanceActivity.class);
				startActivity(it);
				break;
			}
			default:
				break;
		}
	}

	@Override
	public void onMmiTimer(Context context, Object userData)
	{
		// TODO Auto-generated method stub
		AirStatisticsNetworkByte net = AirtalkeeAccount.getInstance().statisticsNetworkByte();

		int statRecvBytes = net.getRecvBytes() + gStatRecv;
		int statSentBytes = net.getSentBytes() + gStatSent;
		long statTime = net.getTimeTotal() + gStatTime;

		String timeString = String.format("%02d:%02d:%02d", statTime / 1000 / 60 / 60, statTime / 1000 / 60 % 60, statTime / 1000 % 60);
		statLayoutTime.setText(getString(R.string.talk_statistic_time) + timeString);

		String total = "";
		int bytesTotal = (statRecvBytes + statSentBytes) / 1024;
		if (bytesTotal > 1024) // M
		{
			total = "" + (bytesTotal / 1024) + "." + ((bytesTotal % 1024) / 100) + "M";
		}
		else
		// K
		{
			total = "" + bytesTotal + "." + (((statRecvBytes + statSentBytes) % 1024) / 100) + "K";
		}

		int bytesInterval = net.getRecvBytesInterval() + net.getSentBytesInterval();
		String bytesString = getString(R.string.talk_statistic_bytes) + total;
		bytesString += " (";
		if (net.getTimeInterval() / 1000 > 1)
		{
			bytesString += bytesInterval / (net.getTimeInterval() / 1000);
		}
		else
		{
			bytesString += bytesInterval;
		}
		bytesString += "B/S)";
		statLayoutBytes.setText(bytesString);

		try
		{
			iOperate.putInt(STAT_RECV, statRecvBytes);
			iOperate.putInt(STAT_SENT, statSentBytes);
			iOperate.putLong(STAT_TIME, statTime);
		}
		catch (Exception e)
		{
			// TODO: handle exception
		}
	}

	@Override
	public void onUserInfoGet(AirContact user)
	{
		// TODO Auto-generated method stub
		if (user != null)
		{
			tvUserName.setText(user.getDisplayName());
			if (MainActivity.getInstance() != null)
				MainActivity.getInstance().viewMiddle.refreshName();
		}
	}

	@Override
	public void onUserInfoUpdate(boolean isOk, AirContact user)
	{
		// TODO Auto-generated method stub
		if (isOk)
		{
			tvUserName.setText(user.getDisplayName());
			Util.Toast(this, getString(R.string.talk_user_info_update_name_ok));
			if (MainActivity.getInstance() != null)
			{
				MainActivity.getInstance().viewMiddle.refreshSessionMember();
				MainActivity.getInstance().viewMiddle.refreshName();
			}
		}
		else
		{
			Util.Toast(this, getString(R.string.talk_user_info_update_name_fail));
		}
	}

	@Override
	public void onUserIdGetByPhoneNum(int result, AirContact contact)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onUserOrganizationTree(boolean isOk, AirContactGroup org)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onUserOrganizationTreeSearch(boolean isOk, List<AirContact> contacts)
	{
		// TODO Auto-generated method stub

	}

}
