package com.airtalkee.activity;

import java.util.List;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.airtalkee.R;
import com.airtalkee.Util.ThemeUtil;
import com.airtalkee.Util.Util;
import com.airtalkee.config.Config;
import com.airtalkee.sdk.AirtalkeeUserInfo;
import com.airtalkee.sdk.OnUserInfoListener;
import com.airtalkee.sdk.entity.AirContact;
import com.airtalkee.sdk.entity.AirContactGroup;
import com.airtalkee.sdk.entity.AirFunctionSetting;
import com.airtalkee.sdk.util.Utils;
import com.airtalkee.services.AirServices;

public class MenuSettingActivity extends ActivityBase implements OnClickListener, OnUserInfoListener
{
	public final static String AIR_SETTING_ANSWER = "AIR_SETTING_ANSWER";

	public static final int SETTING_DISPLAYNAME_LEN = 11;

	private CheckBox mCheckBoxAnswer;

	@Override
	protected void onCreate(Bundle bundle)
	{
		// TODO Auto-generated method stub
		super.onCreate(bundle);
		setRequestedOrientation(Config.screenOrientation);
		setContentView(R.layout.activity_setting);
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
		ivTitle.setText(R.string.talk_tools_setting);
		View btnLeft = findViewById(R.id.menu_left_button);
		ImageView ivLeft = (ImageView) findViewById(R.id.bottom_left_icon);
		ivLeft.setImageResource(ThemeUtil.getResourceId(R.attr.theme_ic_topbar_back, this) );
		btnLeft.setOnClickListener(this);

		RelativeLayout ivRightLay = (RelativeLayout) findViewById(R.id.talk_menu_right_button);
		ImageView ivRight = (ImageView) findViewById(R.id.bottom_right_icon);
		ivRight.setVisibility(View.GONE);
		ivRightLay.setVisibility(View.INVISIBLE);

		if (Config.funcCenterLocation == AirFunctionSetting.SETTING_DISABLE)
		{
			findViewById(R.id.talk_lv_tool_gps).setVisibility(View.GONE);
			findViewById(R.id.talk_lv_tool_gps_divider).setVisibility(View.GONE);
		}
		else
		{
			if (Config.funcCenterLocationMenuShow)
			{
				findViewById(R.id.talk_lv_tool_gps).setVisibility(View.GONE);
				findViewById(R.id.talk_lv_tool_gps_divider).setVisibility(View.GONE);
			}
			else
			{
				findViewById(R.id.talk_lv_tool_gps).setOnClickListener(this);
				findViewById(R.id.talk_lv_tool_gps).setVisibility(View.VISIBLE);
				findViewById(R.id.talk_lv_tool_gps_divider).setVisibility(View.VISIBLE);
			}
		}

		findViewById(R.id.talk_setting_voice).setOnClickListener(this);
		findViewById(R.id.talk_setting_rename).setOnClickListener(this);
		findViewById(R.id.talk_setting_pwd).setOnClickListener(this);

		findViewById(R.id.talk_setting_answer).setOnClickListener(this);
		mCheckBoxAnswer = (CheckBox) findViewById(R.id.talk_setting_answer_check);
		mCheckBoxAnswer.setChecked(AirServices.iOperator.getBoolean(AIR_SETTING_ANSWER, false));
		AirtalkeeUserInfo.getInstance().setOnUserInfoListener(this);
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
			case R.id.talk_setting_rename:
			{
				final EditText input = new EditText(this);
				input.setSingleLine();
				input.setHint(AirtalkeeUserInfo.getInstance().getUserInfo().getDisplayName());
				new AlertDialog.Builder(this).setTitle(getString(R.string.talk_user_info_update_name)).setView(input).setPositiveButton(getString(R.string.talk_ok),
					new DialogInterface.OnClickListener()
					{

						public void onClick(DialogInterface dialog, int which)
						{
							String value = input.getText().toString();
							if (!Utils.isEmpty(value))
							{
								if (value.length() > SETTING_DISPLAYNAME_LEN)
								{
									Util.Toast(MenuSettingActivity.this, getString(R.string.talk_user_info_update_name_error));
								}
								else
								{
									AirtalkeeUserInfo.getInstance().UserInfoUpdate(value.trim());
									Util.Toast(MenuSettingActivity.this, getString(R.string.talk_user_info_update_name_doing));
								}
							}
						}

					}).setNegativeButton(getString(R.string.talk_no), null).show();
				break;
			}
			case R.id.talk_setting_pwd:
			{
				Intent it = new Intent(this, UserChangePasswordActivity.class);
				startActivity(it);
				break;
			}
			case R.id.talk_setting_answer:
			{
				mCheckBoxAnswer.setChecked(!mCheckBoxAnswer.isChecked());
				AirServices.iOperator.putBoolean(AIR_SETTING_ANSWER, mCheckBoxAnswer.isChecked());
				break;
			}
		}
	}

	@Override
	public void onUserIdGetByPhoneNum(int result, AirContact contact)
	{
	// TODO Auto-generated method stub

	}

	@Override
	public void onUserInfoGet(AirContact user)
	{
	// TODO Auto-generated method stub

	}

	@Override
	public void onUserInfoUpdate(boolean isOk, AirContact user)
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
