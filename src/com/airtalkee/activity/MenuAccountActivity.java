package com.airtalkee.activity;

import java.util.List;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.airtalkee.R;
import com.airtalkee.Util.ThemeUtil;
import com.airtalkee.Util.Util;
import com.airtalkee.activity.home.HomeActivity;
import com.airtalkee.bluetooth.BluetoothManager;
import com.airtalkee.config.Config;
import com.airtalkee.control.AirAccountManager;
import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.OnUserInfoListener;
import com.airtalkee.sdk.entity.AirContact;
import com.airtalkee.sdk.entity.AirContactGroup;
import com.airtalkee.services.AirServices;

public class MenuAccountActivity extends ActivityBase implements
		OnClickListener, OnUserInfoListener
{
	public TextView tvUserName;
	public TextView tvUserIpocid;

	@Override
	protected void onCreate(Bundle bundle)
	{
		// TODO Auto-generated method stub
		super.onCreate(bundle);
		setRequestedOrientation(Config.screenOrientation);
		setContentView(R.layout.activity_tool_account);
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
		ivTitle.setText(R.string.talk_user_account_manage);
		View btnLeft = findViewById(R.id.menu_left_button);
		ImageView ivLeft = (ImageView) findViewById(R.id.bottom_left_icon);
		ivLeft.setImageResource(ThemeUtil.getResourceId(R.attr.theme_ic_topbar_back, this));
		btnLeft.setOnClickListener(this);

		RelativeLayout ivRightLay = (RelativeLayout) findViewById(R.id.talk_menu_right_button);
		ImageView ivRight = (ImageView) findViewById(R.id.bottom_right_icon);
		ivRight.setVisibility(View.GONE);
		ivRightLay.setVisibility(View.INVISIBLE);

		tvUserName = (TextView) findViewById(R.id.talk_tv_user_name);
		tvUserName.setText(AirtalkeeAccount.getInstance().getUserName());

		tvUserIpocid = (TextView) findViewById(R.id.talk_tv_user_ipocid);
		tvUserIpocid.setText(AirtalkeeAccount.getInstance().getUserId());

		findViewById(R.id.talk_lv_displayname).setOnClickListener(this);
		findViewById(R.id.talk_lv_password).setOnClickListener(this);
		findViewById(R.id.talk_lv_tool_exit).setOnClickListener(this);
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
			case R.id.talk_lv_displayname:
			{
				Intent it = new Intent(MenuAccountActivity.this, MenuDisplayActivity.class);
				it.putExtra("oldUserName", tvUserName.getText().toString().trim());
				startActivityForResult(it, 1);
				// startActivity(it);
				// finish();
				break;
			}
			case R.id.talk_lv_password:
			{
				Intent it = new Intent(this, MenuPasswordActivity.class);
				startActivity(it);
				break;
			}
			case R.id.talk_lv_tool_exit:
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(getString(R.string.talk_exit_tip));
				final CheckBox cb = new CheckBox(this);
				cb.setText(getString(R.string.talk_auto_login));
				cb.setChecked(true);
				builder.setView(cb);
				builder.setPositiveButton(getString(R.string.talk_exit), new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int whichButton)
					{
						if (!cb.isChecked())
						{
							AirServices.iOperator.putString(AirAccountManager.KEY_PWD, "");
						}
						AirServices.iOperator.putBoolean(AirAccountManager.KEY_HB, false);
						BluetoothManager.getInstance().btStop();
						AirtalkeeAccount.getInstance().Logout();
						finish();
						android.os.Process.killProcess(android.os.Process.myPid());
					}
				});

				builder.setNegativeButton(getString(R.string.talk_session_call_cancel), new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int whichButton)
					{
						dialog.cancel();
					}
				});
				builder.show();
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

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (data != null)
		{
			if (requestCode == 1)
			{
				super.onActivityResult(requestCode, resultCode, data);
				/* 取得来自SecondActivity页面的数据，并显示到画面 */
				Bundle bundle = data.getExtras();
				/* 获取Bundle中的数据，注意类型和key */
				String name = bundle.getString("newUserName");
				tvUserName.setText(name);
			}
		}
	}
}
