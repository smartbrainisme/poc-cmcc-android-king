package com.airtalkee.activity;

import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;

import com.airtalkee.R;
import com.airtalkee.Util.Util;
import com.airtalkee.sdk.controller.AccountController;
import com.airtalkee.sdk.controller.AccountInfoController;
import com.airtalkee.sdk.engine.StructUserMark;
import com.airtalkee.sdk.entity.AirContact;
import com.airtalkee.sdk.entity.AirContactGroup;
import com.airtalkee.sdk.entity.AirFunctionSetting;
import com.airtalkee.sdk.listener.UserAccountListener;
import com.airtalkee.sdk.listener.UserInfoListener;
import com.airtalkee.services.AirServices;

public class UserChangePasswordActivity extends ActivityBase implements OnClickListener, OnCheckedChangeListener, UserInfoListener, UserAccountListener
{

	private EditText old_password;
	private EditText new_password;
	private Button change_password;
	private CheckBox show_password;
	private static UserChangePasswordActivity mInstance = null;

	protected static UserChangePasswordActivity getInstance()
	{
		return mInstance;
	}

	@Override
	protected void onCreate(Bundle bundle)
	{
		// TODO Auto-generated method stub
		super.onCreate(bundle);
		setContentView(R.layout.activity_user_change_password);
		doInitFindView();
		doInit(bundle);
	}

	public void doInit(Bundle bundle)
	{
		// TODO Auto-generated method stub

		mInstance = this;
		//AccountController.setUserAccountListener(this);
		AccountInfoController.setUserInfoListener(this);
		// ���û�û�����ù�����IMIS�����û�������
		//old_password.setText(AccountController.getUserPassword());
		//old_password.setEnabled(false);
	}

	public void doInitFindView()
	{
		// TODO Auto-generated method stub
		old_password = (EditText) findViewById(R.id.old_password);
		new_password = (EditText) findViewById(R.id.new_password);
		change_password = (Button) findViewById(R.id.change_password);
		show_password = (CheckBox) findViewById(R.id.show_password);
		change_password.setOnClickListener(this);
		show_password.setOnCheckedChangeListener(this);
	}

	@SuppressWarnings("deprecation")
	protected Dialog onCreateDialog(int id)
	{
		if (id == R.id.talk_dialog_login_fail)
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(getString(R.string.pwd_change_failed));
			builder.setNegativeButton(getString(R.string.talk_ok), new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int whichButton)
				{
					dialog.cancel();
					finish();
				}
			});
			return builder.create();
		}
		else if (id == R.id.talk_dialog_waiting)
		{

			ProgressDialog dialog = new ProgressDialog(this);
			dialog.setMessage(getString(R.string.requesting));
			return dialog;
		}
		return super.onCreateDialog(id);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onClick(View v)
	{
		// TODO Auto-generated method stub

		switch (v.getId())
		{
			case R.id.change_password:
			{
				String password = old_password.getText().toString();
				String password_new = new_password.getText().toString();
				if (password_new.equals(""))
				{
					Util.Toast(mInstance, getString(R.string.userlogin_pwd_isnull));
				}
				else
				{

					AirContact userinfo = AccountController.getUserInfo();
					if (userinfo != null)
					{
						String ipocid = userinfo.getIpocId();
						if (password_new.length() < 6 || password.length() < 6)
						{
							Util.Toast(mInstance, getString(R.string.talk_pwd_error));
							return;
						}
						else if (password_new.length() > 15 || password.length() > 15)
						{
							Util.Toast(mInstance, getString(R.string.pwd_outof_lenth));
							return;
						}
						else if (!userinfo.getPwd().equals(password))
						{
							Util.Toast(mInstance, getString(R.string.pwd_no_same));
							return;
						}
						else if (password.equals(password_new))
						{
							Util.Toast(mInstance, getString(R.string.pwd_same_of_old));
							return;
						}
						AccountInfoController.userSetPassword(ipocid, password, password_new);
						showDialog(R.id.talk_dialog_waiting);
					}
				}

			}
				break;
		}

	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
	{
		// TODO Auto-generated method stub

		switch (buttonView.getId())
		{
			case R.id.show_password:
			{
				if (isChecked)
				{
					old_password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
					new_password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
					old_password.setSelection(old_password.getText().toString().length());
					new_password.setSelection(new_password.getText().toString().length());
				}
				else
				{
					old_password.setTransformationMethod(PasswordTransformationMethod.getInstance());
					new_password.setTransformationMethod(PasswordTransformationMethod.getInstance());
					old_password.setSelection(old_password.getText().toString().length());
					new_password.setSelection(new_password.getText().toString().length());
				}

			}
				break;
		}

	}

	@Override
	public void UserAccountMatch(boolean isOk, AirContact user)
	{
	// TODO Auto-generated method stub

	}

	@Override
	public void onUserInfoGenerateTempCodeByPhoneNumber(boolean isOk)
	{
	// TODO Auto-generated method stub

	}

	@Override
	public void onUserInfoGetEvent(AirContact user)
	{
	// TODO Auto-generated method stub

	}

	@Override
	public void onUserInfoGetbackAccountByPhoneNumber(int result, String[] ipocids)
	{
	// TODO Auto-generated method stub
	}

	@Override
	public void onUserInfoRegisterByPhoneNumber(int result, String ipocid)
	{
	// TODO Auto-generated method stub

	}

	@SuppressWarnings("deprecation")
	@Override
	public void onUserInfoSetPassword(boolean isOk, String password)
	{
		// TODO Auto-generated method stub
		removeDialog(R.id.talk_dialog_waiting);
		if (isOk)
		{
			if (new_password.getText() != null)
			{
				AirServices.iOperator.putString("USER_PWD", new_password.getText().toString());
			}
			Util.Toast(mInstance, getString(R.string.pwd_chage_success));
			finish();
		}
		else
		{
			showDialog(R.id.talk_dialog_login_fail);
		}

	}

	@Override
	public void onUserInfoUpdateEvent(boolean isOk, AirContact user)
	{
	// TODO Auto-generated method stub

	}

	@Override
	public void UserLoginEvent(int result, AirContact user)
	{
	// TODO Auto-generated method stub

	}

	@Override
	public void UserLogoutEvent(boolean success)
	{
	// TODO Auto-generated method stub

	}

	@Override
	public void UserHeartbeatEvent(int result)
	{
	// TODO Auto-generated method stub

	}

	@Override
	public void UserRegisterEvent(boolean isOk, AirContact user)
	{
	// TODO Auto-generated method stub

	}

	@Override
	public void UserUnregisterEvent()
	{
	// TODO Auto-generated method stub

	}

	@Override
	public void onUserInfoUpdatePhoneNum(boolean isOk)
	{
	// TODO Auto-generated method stub

	}

	@Override
	public void onUserIdGetByPhoneNum(int arg0, StructUserMark arg1)
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
	public void UserFunctionSetting(AirFunctionSetting setting)
	{
	// TODO Auto-generated method stub

	}

}
