package com.airtalkee.activity;

import android.content.Intent;
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
import com.airtalkee.sdk.util.Utils;

public class UserRegisterPrepareActivity extends ActivityBase implements OnClickListener, OnCheckedChangeListener
{
	private EditText etPhone;
	private EditText etName;
	private EditText etPwd;
	private Button register;
	private CheckBox cbShowPwd;
	private String phoneNum = "";
	private String displayName = "";
	private String passWord = "";

	private static UserRegisterPrepareActivity mInstance = null;

	protected static UserRegisterPrepareActivity getInstance()
	{
		return mInstance;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register_ns);
		mInstance = this;
		etPhone = (EditText) findViewById(R.id.et_phone);
		etName = (EditText) findViewById(R.id.et_name);
		etPwd = (EditText) findViewById(R.id.et_password);
		cbShowPwd = (CheckBox) findViewById(R.id.show_password);
		register = (Button) findViewById(R.id.next);
		register.setOnClickListener(this);
		cbShowPwd.setOnCheckedChangeListener(this);
	}

	@Override
	public void finish()
	{
		// TODO Auto-generated method stub
		super.finish();
		mInstance = null;
	}

	@Override
	protected void onDestroy()
	{
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	public void onClick(View v)
	{
		// TODO Auto-generated method stub
		switch (v.getId())
		{
			case R.id.next:
			{
				phoneNum = etPhone.getText().toString();
				passWord = etPwd.getText().toString();
				displayName = etName.getText().toString();
				passWord = etPwd.getText().toString();
				if (!Utils.isEmpty(displayName) || !Utils.isEmpty(phoneNum) || !Utils.isEmpty(passWord))
				{
					if (phoneNum.length() < 11)
					{
						Util.Toast(mInstance, getString(R.string.talk_phonenum_error));
						return;
					}
					if (passWord.length() > 15)
					{
						Util.Toast(mInstance, getString(R.string.talk_pwd_outof_lenth));
						return;
					}
					else if (passWord.length() < 6)
					{
						Util.Toast(mInstance, getString(R.string.talk_pwd_error));
						return;
					}

					Intent it = new Intent(this, UserRegisterCompleteActivity.class);
					it.putExtra("displayName", Util.filterDisplayName(displayName));
					it.putExtra("phoneNum", phoneNum);
					it.putExtra("passWord", passWord);
					startActivity(it);

				}
				else
				{
					Util.Toast(mInstance, getString(R.string.talk_input_isnotnull));
				}
			}
				break;
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
	{
		// TODO Auto-generated method stub
		if (isChecked)
		{
			etPwd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
			etPwd.setSelection(etPwd.getText().toString().length());
		}
		else
		{
			etPwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
			etPwd.setSelection(etPwd.getText().toString().length());
		}
	}

}
