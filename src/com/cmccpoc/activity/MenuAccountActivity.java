package com.cmccpoc.activity;

import java.util.List;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.OnUserInfoListener;
import com.airtalkee.sdk.entity.AirContact;
import com.airtalkee.sdk.entity.AirContactGroup;
import com.cmccpoc.R;
import com.cmccpoc.Util.ThemeUtil;
import com.cmccpoc.Util.Util;
import com.cmccpoc.activity.home.HomeActivity;
import com.cmccpoc.activity.home.widget.AlertDialog;
import com.cmccpoc.activity.home.widget.AlertDialog.DialogListener;
import com.cmccpoc.config.Config;
import com.cmccpoc.control.AirAccountManager;
import com.cmccpoc.services.AirServices;

/**
 * 更多：账户管理Activity
 * 主要功能包括：修改名称，修改密码，退出登录。
 * @author Yao
 */
public class MenuAccountActivity extends ActivityBase implements OnClickListener, OnUserInfoListener, DialogListener
{
	public TextView tvUserName;
	public TextView tvUserIpocid;

	AlertDialog dialog;

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

	/**
	 * 初始化绑定控件Id
	 */
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
				dialog = new AlertDialog(this, getString(R.string.talk_exit_tip), getString(R.string.talk_auto_login), getString(R.string.talk_exit), true, this, 0);
				dialog.show();
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
		}
	}

	@Override
	public void onUserInfoUpdate(boolean isOk, AirContact user)
	{
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

	@Override
	public void onClickOk(int id,Object obj)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void onClickOk(int id, boolean isChecked)
	{
		if (!isChecked)
		{
			AirServices.iOperator.putString(AirAccountManager.KEY_PWD, "");
		}
		AirServices.iOperator.putBoolean(AirAccountManager.KEY_HB, false);
		AirtalkeeAccount.getInstance().Logout();
		finish();
		if (MoreActivity.getInstance() != null)
		{
			MoreActivity.getInstance().finish();
		}
		if (HomeActivity.getInstance() != null)
		{
			HomeActivity.getInstance().finish();
		}
	}

	@Override
	public void onClickCancel(int id)
	{
		
	}
}
