package com.airtalkee.activity;

import java.util.List;
import android.R.integer;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import com.airtalkee.R;
import com.airtalkee.Util.ThemeUtil;
import com.airtalkee.Util.Util;
import com.airtalkee.config.Config;
import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.AirtalkeeUserInfo;
import com.airtalkee.sdk.OnUserInfoListener;
import com.airtalkee.sdk.controller.AccountController;
import com.airtalkee.sdk.entity.AirContact;
import com.airtalkee.sdk.entity.AirContactGroup;
import com.airtalkee.sdk.entity.AirFunctionSetting;

public class MoreActivity extends ActivityBase implements OnClickListener,
		OnUserInfoListener, OnSeekBarChangeListener
{

	public TextView tvUserName;
	public TextView tvUserIpocid;
	private TextView tvVersion;

	private SeekBar mVoiceVolumeSeekBar;
	private CheckBox mVoiceMode;

	private ImageView ivUnread;

	private static MoreActivity mInstance = null;

	public static MoreActivity getInstance()
	{
		return mInstance;
	}

	@Override
	protected void onCreate(Bundle bundle)
	{
		super.onCreate(bundle);
		setRequestedOrientation(Config.screenOrientation);
		setContentView(R.layout.activity_tool);
		doInitView();
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		tvUserName.setText(AirtalkeeAccount.getInstance().getUserName());
	}

	private void doInitView()
	{
		mInstance = this;
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

		mVoiceVolumeSeekBar = (SeekBar) findViewById(R.id.SoundSettingBarView);
		AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		int max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		mVoiceVolumeSeekBar.setMax(max);
		mVoiceVolumeSeekBar.setProgress(Util.getStreamVolume(this));
		mVoiceVolumeSeekBar.setOnSeekBarChangeListener(this);

		// findViewById(R.id.talk_setting_voice_mode).setOnClickListener(this);//
		// 收听模式
		mVoiceMode = (CheckBox) findViewById(R.id.talk_setting_voice_mode);
		switch (Util.getMode(this))
		{
			case 0: // 扬声器
				mVoiceMode.setChecked(false);
				break;
			case 2:// 听筒
				mVoiceMode.setChecked(true);
				break;
			default:
				Util.setMode(this);
				mVoiceMode.setChecked(false); // 置为扬声器
				break;
		}

		mVoiceMode.setOnClickListener(this);

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
				// findViewById(R.id.talk_lv_tool_gps).setOnClickListener(this);
				// findViewById(R.id.talk_lv_tool_gps).setVisibility(View.VISIBLE);
				// findViewById(R.id.talk_lv_tool_gps_divider).setVisibility(View.VISIBLE);

				findViewById(R.id.talk_lv_tool_gps).setVisibility(View.GONE);
				findViewById(R.id.talk_lv_tool_gps_divider).setVisibility(View.GONE);
			}
			else
			{
				// findViewById(R.id.talk_lv_tool_gps).setVisibility(View.GONE);
				// findViewById(R.id.talk_lv_tool_gps_divider).setVisibility(View.GONE);
				findViewById(R.id.talk_lv_tool_gps).setOnClickListener(this);
				findViewById(R.id.talk_lv_tool_gps).setVisibility(View.VISIBLE);
				findViewById(R.id.talk_lv_tool_gps_divider).setVisibility(View.VISIBLE);
			}
		}
		findViewById(R.id.talk_setting_voice).setOnClickListener(this);
		findViewById(R.id.talk_lv_tool_upload_record).setOnClickListener(this);// 上报记录
		findViewById(R.id.talk_lv_tool_help).setOnClickListener(this);// 使用和帮助
		findViewById(R.id.talk_tv_notice).setOnClickListener(this);// 广播
		findViewById(R.id.talk_lv_tool_about).setOnClickListener(this);// 关于
		// Report item
		// if (Config.funcCenterReport)
		// {
		// findViewById(R.id.talk_lv_tool_report).setOnClickListener(this);
		// findViewById(R.id.talk_lv_tool_report).setVisibility(View.VISIBLE);
		// findViewById(R.id.talk_lv_tool_report_divider).setVisibility(View.VISIBLE);
		// }
		// else
		// {
		// findViewById(R.id.talk_lv_tool_report).setVisibility(View.GONE);
		// findViewById(R.id.talk_lv_tool_report_divider).setVisibility(View.GONE);
		// }

		// Call center item
		// if (Config.funcCenterCall == AirFunctionSetting.SETTING_DISABLE) {
		// findViewById(R.id.talk_lv_tool_call).setVisibility(View.GONE);
		// findViewById(R.id.talk_lv_tool_call_divider).setVisibility(
		// View.GONE);
		// } else {
		// if (Config.funcCenterCallMenuShow) {
		// findViewById(R.id.talk_lv_tool_call).setOnClickListener(this);
		// findViewById(R.id.talk_lv_tool_call)
		// .setVisibility(View.VISIBLE);
		// findViewById(R.id.talk_lv_tool_call_divider).setVisibility(
		// View.VISIBLE);
		// } else {
		// findViewById(R.id.talk_lv_tool_call).setVisibility(View.GONE);
		// findViewById(R.id.talk_lv_tool_call_divider).setVisibility(
		// View.GONE);
		// }
		// }

		// Manual item
		/*
		 * if (Config.funcManual) {
		 * findViewById(R.id.talk_lv_tool_manual).setOnClickListener(this);
		 * findViewById(R.id.talk_lv_tool_manual).setVisibility(View.VISIBLE);
		 * findViewById
		 * (R.id.talk_lv_tool_manual_divider).setVisibility(View.VISIBLE); }
		 * else {
		 * findViewById(R.id.talk_lv_tool_manual).setVisibility(View.GONE);
		 * findViewById
		 * (R.id.talk_lv_tool_manual_divider).setVisibility(View.GONE); }
		 * 
		 * // Defect report if (Config.funcfeedback) {
		 * findViewById(R.id.talk_lv_tool_defect).setOnClickListener(this);
		 * findViewById(R.id.talk_lv_tool_defect).setVisibility(View.VISIBLE);
		 * findViewById
		 * (R.id.talk_lv_tool_defect_divider).setVisibility(View.VISIBLE); }
		 * else {
		 * findViewById(R.id.talk_lv_tool_defect).setVisibility(View.GONE);
		 * findViewById
		 * (R.id.talk_lv_tool_defect_divider).setVisibility(View.GONE); } //
		 * Sysinfo item if (!Utils.isEmpty(Config.serverUrlInfosys)) {
		 * findViewById(R.id.talk_lv_tool_infosys).setOnClickListener(this);
		 * findViewById(R.id.talk_lv_tool_infosys).setVisibility(View.VISIBLE);
		 * findViewById
		 * (R.id.talk_lv_tool_infosys_divider).setVisibility(View.VISIBLE); }
		 * else {
		 * findViewById(R.id.talk_lv_tool_infosys).setVisibility(View.GONE);
		 * findViewById
		 * (R.id.talk_lv_tool_infosys_divider).setVisibility(View.GONE); }
		 */

		if (Config.funcThemeChange)
		{
			findViewById(R.id.talk_change_theme).setOnClickListener(this);
		}
		else
		{
			findViewById(R.id.talk_change_theme).setVisibility(View.GONE);
			findViewById(R.id.talk_change_theme_divider).setVisibility(View.GONE);
		}

		// TaskDspatch
		if (Config.funcTaskDispatch)
		{
			findViewById(R.id.talk_lv_tools_task).setVisibility(View.VISIBLE);
			findViewById(R.id.talk_lv_tool_task_divider).setVisibility(View.VISIBLE);
			findViewById(R.id.talk_lv_tools_task).setOnClickListener(this);
		}

		// Attendance
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
		tvUserIpocid = (TextView) findViewById(R.id.talk_tv_user_ipocid);
		tvUserIpocid.setText(AirtalkeeAccount.getInstance().getUserId());
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

		ivUnread = (ImageView) findViewById(R.id.iv_Unread);
		checkBrodcast();
	}
	
	public void checkBrodcast()
	{
		if (Config.funcBroadcast && AirtalkeeAccount.getInstance().SystemBroadcastNumberGet() > 0)
		{
			ivUnread.setVisibility(View.VISIBLE);
		}
		else
		{
			ivUnread.setVisibility(View.GONE);
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
	}

	@Override
	protected void onResume()
	{
		// TODO Auto-generated method stub
		super.onResume();
		AirtalkeeUserInfo.getInstance().setOnUserInfoListener(this);
		checkBrodcast();
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
			case R.id.talk_setting_voice_mode:
			{
				Util.setMode(this);
				break;
			}
			case R.id.talk_setting_voice:
			{
				Intent it = new Intent(this, MenuSettingPttActivity.class);
				startActivity(it);
				break;
			}
			case R.id.talk_lv_tool_upload_record:
			{
				Intent it = new Intent(this, MenuReportActivity.class);
				it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(it);
				break;
			}
			case R.id.talk_lv_tool_help:
			{
				Intent it = new Intent(this, MenuHelpActivity.class);
				startActivity(it);
				break;
			}
			case R.id.talk_tv_notice:
			{
				Intent it = new Intent(this, MenuNoticeActivity.class);
				it.putExtra("url", AccountController.getDmWebNoticeUrl());
				startActivity(it);
				break;
			}
			case R.id.talk_lv_tool_about:
			{
				Intent it = new Intent(this, MenuAboutActivity.class);
				startActivity(it);
				break;
			}
			/*
			 * case R.id.talk_lv_tool_exit: { if (MainActivity.getInstance() !=
			 * null) { Builder builder =
			 * MainActivity.getInstance().BuildExitDialog(this); builder.show();
			 * } break; } case R.id.talk_change_theme: {
			 * ThemeUtil.changeTheme(this); break; } case
			 * R.id.talk_lv_tool_update: { Intent it = new Intent(this,
			 * MenuSettingActivity.class); startActivity(it); break; } case
			 * R.id.talk_lv_tool_manual: { Intent it = new Intent(this,
			 * MenuManualActivity.class); startActivity(it); break; } case
			 * R.id.talk_lv_tool_defect: { Intent it = new Intent(this,
			 * MenuDefectReportActivity.class); startActivity(it); break; } case
			 * R.id.talk_lv_tool_infosys: { Intent LaunchIntent =
			 * getPackageManager().getLaunchIntentForPackage("com.wf.info"); if
			 * (LaunchIntent != null) { startActivity(LaunchIntent); } else {
			 * Util.Toast(this, getString(R.string.talk_tools_infosys_null));
			 * Uri uri = Uri.parse(Config.serverUrlInfosys); Intent web = new
			 * Intent(Intent.ACTION_VIEW, uri); startActivity(web); } break; }
			 */
			case R.id.talk_tv_user_name_panel:
			{
				/*
				 * final Context context = this; final EditText input = new
				 * EditText(this); input.setSingleLine();
				 * input.setHint(AirtalkeeUserInfo
				 * .getInstance().getUserInfo().getDisplayName()); new
				 * AlertDialog.Builder(this).setTitle(getString(R.string.
				 * talk_user_info_update_name
				 * )).setView(input).setPositiveButton(
				 * getString(R.string.talk_ok), new
				 * DialogInterface.OnClickListener() {
				 * 
				 * public void onClick(DialogInterface dialog, int which) {
				 * String value = input.getText().toString(); if
				 * (!Utils.isEmpty(value)) { if (value.length() >
				 * MenuSettingActivity.SETTING_DISPLAYNAME_LEN) {
				 * Util.Toast(context,
				 * getString(R.string.talk_user_info_update_name_error)); } else
				 * {
				 * AirtalkeeUserInfo.getInstance().UserInfoUpdate(value.trim());
				 * Util.Toast(context,
				 * getString(R.string.talk_user_info_update_name_doing)); } } }
				 * 
				 * }).setNegativeButton(getString(R.string.talk_no),
				 * null).show();
				 */
				Intent it = new Intent(this, MenuAccountActivity.class);
				startActivity(it);
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
	public void onUserInfoGet(AirContact user)
	{
		// TODO Auto-generated method stub
		if (user != null)
		{
			tvUserName.setText(user.getDisplayName());
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

	@Override
	public boolean dispatchKeyEvent(KeyEvent event)
	{
		boolean handled = false;
		switch (event.getKeyCode())
		{
			case KeyEvent.KEYCODE_VOLUME_UP:
				Util.setStreamVolumeUp(this);
				mVoiceVolumeSeekBar.setProgress(Util.getStreamVolume(this));
				handled = true;
				break;
			case KeyEvent.KEYCODE_VOLUME_DOWN:
				Util.setStreamVolumeDown(this);
				mVoiceVolumeSeekBar.setProgress(Util.getStreamVolume(this));
				handled = true;
				break;
		// case KeyEvent.KEYCODE_CAMERA:
		// if (event.getAction() == KeyEvent.ACTION_UP)
		// {
		// Util.setMode(this);
		// }
		// handled = true;
		// break;
		}
		return handled ? handled : super.dispatchKeyEvent(event);
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
	{
		// TODO Auto-generated method stub
		Util.setStreamVolume(this, progress);
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar)
	{
		// TODO Auto-generated method stub
	}

}
