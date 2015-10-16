package com.airtalkee.activity;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.airtalkee.R;
import com.airtalkee.Util.Util;
import com.airtalkee.control.AirAccountManager;
import com.airtalkee.listener.OnMmiAccountListener;
import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.AirtalkeeUserRegister;
import com.airtalkee.sdk.OnUserRegisterListener;
import com.airtalkee.sdk.engine.AirEngine;
import com.airtalkee.sdk.util.Utils;

public class UserRegisterCompleteActivity extends Activity implements OnClickListener, OnMmiAccountListener, OnUserRegisterListener
{
	public static final int EVENT_WAIT_TIMER_REFRESH = 0;
	private AirtalkeeAccount handleAccount = null;
	private AirtalkeeUserRegister handleRegister = null;
	private Button btnNext;
	private Button btnWait;
	private EditText etTempCode;
	private String phoneNum = "";
	private String passwrod = "";
	private String userName = "";
	private int waitTime = 120;
	private static UserRegisterCompleteActivity mInstance = null;

	protected static UserRegisterCompleteActivity getInstance()
	{
		return mInstance;
	}

	@Override
	protected void onCreate(Bundle bundle)
	{
		super.onCreate(bundle);
		setContentView(R.layout.activity_register);
		mInstance = this;
		handleAccount = AirtalkeeAccount.getInstance();
		handleRegister = AirtalkeeUserRegister.getInstance();
		btnNext = (Button) findViewById(R.id.next);
		btnWait = (Button) findViewById(R.id.btn_waiting);
		etTempCode = (EditText) findViewById(R.id.temp_code);
		bundle = this.getIntent().getExtras();
		if (bundle != null)
		{
			phoneNum = bundle.getString("phoneNum");
			userName = bundle.getString("displayName");
			passwrod = bundle.getString("passWord");
			btnNext.setOnClickListener(this);
			btnWait.setOnClickListener(this);
			handleRegister.setOnUserRegisterListener(this);
			handleRegister.generteTempCode(phoneNum);
			AirAccountManager.getInstance().setAccountListener(this);
		}
	}

	@Override
	public void finish()
	{
		// TODO Auto-generated method stub
		super.finish();
		mInstance = null;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onClick(View v)
	{
		// TODO Auto-generated method stub

		switch (v.getId())
		{
			case R.id.next:
			{
				String tempcode = etTempCode.getText().toString().trim();
				if (!Utils.isEmpty(tempcode))
				{
					handleRegister.register(phoneNum, userName, tempcode, passwrod);
					showDialog(R.id.talk_dialog_waiting);
				}
				else
					Util.Toast(mInstance, getString(R.string.talk_please_input_validate));
			}
				break;
			case R.id.btn_waiting:
			{
				handleRegister.generteTempCode(phoneNum);
				break;
			}
		}

	}

	protected Dialog onCreateDialog(int id)
	{
		// TODO Auto-generated method stub
		if (mInstance == null)
			return null;
		switch (id)
		{

			case R.id.talk_dialog_waiting:
			{
				ProgressDialog dialog = new ProgressDialog(this);
				dialog.setMessage(getString(R.string.talk_tip_waiting));
				return dialog;
			}
			case R.id.talk_dialog_login_waiting:
			{
				ProgressDialog dialog = new ProgressDialog(this);
				dialog.setMessage(getString(R.string.talk_logining));
				return dialog;
			}
		}
		return null;
	}

	// ˢ�µȴ��ʱ��
	public void refreshBtnTimer()
	{
		if (waitTime > 0)
		{
			handler.sendEmptyMessageDelayed(EVENT_WAIT_TIMER_REFRESH, 1000);
			waitTime--;
			btnWait.setText("�ȴ�" + waitTime);
			btnWait.setEnabled(false);
		}
		else
		{
			btnWait.setEnabled(true);
			btnWait.setText("���»�ȡ");
		}
	}

	public Handler handler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			if (msg.what == EVENT_WAIT_TIMER_REFRESH)
			{
				removeMessages(msg.what);
				refreshBtnTimer();
			}
		}
	};

	@Override
	public void onUserGenerateTempCode(boolean isOK)
	{
		// TODO Auto-generated method stub
		if (!isOK)
		{
			Util.Toast(mInstance, getString(R.string.talk_tempcode_get_fail));
		}
		else
		{
			waitTime = 120;
			refreshBtnTimer();
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onUserRegister(int result, String ipocid)
	{
		// TODO Auto-generated method stub
		removeDialog(R.id.talk_dialog_waiting);
		if (result == AirEngine.EVENT_PARAM_SUCCEED)
		{
			handleAccount.Login(ipocid, passwrod);
			showDialog(R.id.talk_dialog_login_waiting);
		}
		else
		{
			if (result == -1)
			{
				Util.Toast(mInstance, mInstance.getString(R.string.talk_register_fail));
			}
			else
			{
				Util.Toast(mInstance, Util.loginInfo(result, mInstance));
			}
		}
	}

	@Override
	public void onMmiHeartbeatException(int result)
	{
	// TODO Auto-generated method stub

	}

	@SuppressWarnings("deprecation")
	@Override
	public void onMmiHeartbeatLogin(int result)
	{
		// TODO Auto-generated method stub
		removeDialog(R.id.talk_dialog_login_waiting);
		if (result == AirtalkeeAccount.ACCOUNT_RESULT_OK)
		{
			Intent it = new Intent(this, MainActivity.class);
			startActivity(it);
			if (UserRegisterCompleteActivity.getInstance() != null)
			{
				UserRegisterPrepareActivity.getInstance().finish();
			}
			if (AccountActivity.getInstance() != null)
			{
				AccountActivity.getInstance().finish();
			}
			finish();
		}
		else
		{
			// TODO option1
			Util.Toast(mInstance, Util.loginInfo(result, mInstance));
		}
	}

	@Override
	public void onMmiHeartbeatLogout()
	{
	// TODO Auto-generated method stub

	}

}
