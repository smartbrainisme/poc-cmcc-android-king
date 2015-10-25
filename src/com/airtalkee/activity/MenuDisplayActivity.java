package com.airtalkee.activity;

import java.util.List;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.airtalkee.R;
import com.airtalkee.Util.AirMmiTimer;
import com.airtalkee.Util.ThemeUtil;
import com.airtalkee.Util.Util;
import com.airtalkee.config.Config;
import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.AirtalkeeUserInfo;
import com.airtalkee.sdk.OnUserInfoListener;
import com.airtalkee.sdk.entity.AirContact;
import com.airtalkee.sdk.entity.AirContactGroup;
import com.airtalkee.sdk.util.Utils;

public class MenuDisplayActivity extends ActivityBase implements
		OnClickListener, OnUserInfoListener
{
	public EditText tvUserName;

	@Override
	protected void onCreate(Bundle bundle)
	{
		// TODO Auto-generated method stub
		super.onCreate(bundle);
		setRequestedOrientation(Config.screenOrientation);
		setContentView(R.layout.activity_tool_display);
		doInitView();
	}

	@Override
	protected void onStart()
	{
		// TODO Auto-generated method stub
		super.onStart();
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
	}

	private void doInitView()
	{
		TextView ivTitle = (TextView) findViewById(R.id.tv_main_title);
		ivTitle.setText(R.string.talk_user_username_edit);
		View btnLeft = findViewById(R.id.menu_left_button);
		ImageView ivLeft = (ImageView) findViewById(R.id.bottom_left_icon);
		ivLeft.setImageResource(ThemeUtil.getResourceId(R.attr.theme_ic_topbar_back, this));
		btnLeft.setOnClickListener(this);

		RelativeLayout ivRightLay = (RelativeLayout) findViewById(R.id.talk_menu_right_button);
		ImageView ivRight = (ImageView) findViewById(R.id.bottom_right_icon);
		ivRight.setVisibility(View.GONE);
		ivRightLay.setVisibility(View.INVISIBLE);

		tvUserName = (EditText) findViewById(R.id.talk_tv_user_name);
		tvUserName.setText(AirtalkeeAccount.getInstance().getUserName());

		findViewById(R.id.talk_lv_tool_save).setOnClickListener(this);
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
			case R.id.talk_lv_tool_save:
			{
				String value = tvUserName.getText().toString();
				tvUserName.setSingleLine();
				tvUserName.setHint(AirtalkeeUserInfo.getInstance().getUserInfo().getDisplayName());
				if (!Utils.isEmpty(value))
				{
					if (value.length() > MenuSettingActivity.SETTING_DISPLAYNAME_LEN)
					{
						Util.Toast(this, getString(R.string.talk_user_info_update_name_error));
					}
					else
					{
						try
						{
							// Util.Toast(this,
							// getString(R.string.talk_user_info_update_name_doing));
							AirtalkeeUserInfo.getInstance().UserInfoUpdate(value.trim());
							Util.Toast(this, getString(R.string.talk_channel_editname_success), R.drawable.ic_success);
							finish();
						}
						catch (Exception e)
						{
							Util.Toast(this, getString(R.string.talk_channel_editname_fail), R.drawable.ic_error);
						}
					}
				}
				break;
			}
			default:
				break;
		}
	}

	@Override
	public void onUserInfoGet(AirContact user)
	{
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

	}

	@Override
	public void onUserOrganizationTree(boolean isOk, AirContactGroup org)
	{

	}

	@Override
	public void onUserOrganizationTreeSearch(boolean isOk, List<AirContact> contacts)
	{

	}
}
