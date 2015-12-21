package com.airtalkee.activity;

import java.util.LinkedHashMap;
import java.util.List;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.airtalkee.R;
import com.airtalkee.Util.ApnManager;
import com.airtalkee.Util.Toast;
import com.airtalkee.Util.Util;
import com.airtalkee.activity.home.HomeActivity;
import com.airtalkee.application.MainApplication;
import com.airtalkee.config.Config;
import com.airtalkee.control.AirAccountManager;
import com.airtalkee.listener.OnMmiAccountListener;
import com.airtalkee.listener.OnMmiChannelListener;
import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.AirtalkeeAccountMgr;
import com.airtalkee.sdk.AirtalkeeUserInfo;
import com.airtalkee.sdk.OnUserInfoListener;
import com.airtalkee.sdk.entity.AirChannel;
import com.airtalkee.sdk.entity.AirContact;
import com.airtalkee.sdk.entity.AirContactGroup;
import com.airtalkee.sdk.listener.AccountByImeiListener;
import com.airtalkee.sdk.util.Log;
import com.airtalkee.sdk.util.Utils;
import com.airtalkee.services.AirServices;

public class AccountActivity extends ActivityBase implements OnClickListener, OnMmiAccountListener, OnMmiChannelListener, OnUserInfoListener, OnTouchListener, AccountByImeiListener
{
	private EditText etIpocid;
	private EditText etPwd;
//	private TextView tvRegister;
	private View btnLogin;
	private LinearLayout layoutInput, layoutWaiting;
	private TextView tvInfo;

	private final int STATE_IDLE = 0;
	private final int STATE_LOGIN = 1;
	private final int STATE_LOADING = 2;

	private static AccountActivity instance = null;

	public static AccountActivity getInstance()
	{
		return instance;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Log.i(AccountActivity.class, "AccountActivity onCreate");
		setRequestedOrientation(Config.screenOrientation);
		if (!Utils.isEmpty(Config.model) && Config.model.startsWith("OINOM"))
		{
			final Window win = getWindow();
			win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
			win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
		}
		instance = this;
		setContentView(R.layout.activity_account);
		doInitFindView();
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onStart()
	{
		// TODO Auto-generated method stub
		super.onStart();
		Log.i(AccountActivity.class, "AccountActivity onStart");
		if(MainApplication.isFisrtLaunch()  && !Config.funcBootLaunch)
		{
			showDialog(R.id.talk_dialog_ascess_network);
			Log.i(AccountActivity.class, "MainApplication.isFisrtLaunch()  && !Config.funcLaunch");
		}
		else
		{
			Log.i(AccountActivity.class, "else");
			new Task().execute();
		}
	}

	private void accountCheck()
	{
		if (!AirtalkeeAccount.getInstance().isAccountRunning())
		{
			Log.i(AccountActivity.class, "AccountActivity accountCheck");
			AirAccountManager.getInstance().setAccountListener(this);
			AirAccountManager.getInstance().setChannelListener(this);
			AirtalkeeUserInfo.getInstance().setOnUserInfoListener(this);
			String userId = AirServices.iOperator.getString(AirAccountManager.KEY_ID);
			String userPwd = AirServices.iOperator.getString(AirAccountManager.KEY_PWD);
			boolean toLogin = false;
			if (!Utils.isEmpty(userId) && !Utils.isEmpty(userPwd))
			{
				toLogin = true;
			}
			else if(Config.funcTTS)
			{
				Util.Toast(this, getString(R.string.talk_account_checking));
				AirtalkeeAccountMgr.getInstance().AccountMgrGetIDByIMEI(this, this);
			}

			if (toLogin)
			{
				etIpocid.setText(userId);
				etPwd.setText(userPwd);
				AirtalkeeAccount.getInstance().Login(userId, userPwd);
				accountStateShow(STATE_LOGIN);
			}
			else
			{
				AirServices.iOperator.clean();
			}
		}
		else
		{
			Intent it = new Intent(this, HomeActivity.class);
			startActivity(it);
			finish();
		}
	}

	private void accountStateShow(int state)
	{
		switch (state)
		{
			case STATE_IDLE:
				layoutInput.setVisibility(View.VISIBLE);
				layoutWaiting.setVisibility(View.GONE);
				break;
			case STATE_LOGIN:
				layoutInput.setVisibility(View.GONE);
				layoutWaiting.setVisibility(View.VISIBLE);
				tvInfo.setText(getString(R.string.talk_logining));
				break;
			case STATE_LOADING:
				layoutInput.setVisibility(View.GONE);
				layoutWaiting.setVisibility(View.VISIBLE);
				tvInfo.setText(getString(R.string.talk_login_loading));
				break;
			default:
				break;
		}
	}

	private class Task extends AsyncTask<Void, Void, String[]>
	{
		int times=0;
		protected String[] doInBackground(Void... params)
		{
		
			while (!AirServices.appRunning)
			{
				if(times == 0)
				{
					Log.i(AccountActivity.class, "Task execute start services");
					Intent intent = new Intent(AirServices.SERVICE_PATH);
					startService(intent);
				}
				try
				{
					Log.i(AccountActivity.class, "Task looping ... times=["+times+"]");
					Thread.sleep(100);
				}
				catch (InterruptedException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				times++;
				if(times > 30)
				{
					Log.i(AccountActivity.class, "Task reset state");
					times = 0;
				}
			}
			return null;
		}

		protected void onPostExecute(String[] result)
		{
			Log.i(AccountActivity.class, "Task post execute");
			accountCheck();
			super.onPostExecute(result);
		}
	}

	public void doInitFindView()
	{
		// TODO Auto-generated method stub
		etIpocid = (EditText) findViewById(R.id.talk_et_ipocid);
		etPwd = (EditText) findViewById(R.id.talk_et_ipocpwd);
		btnLogin = findViewById(R.id.talk_btn_login);
//		tvRegister = (TextView) findViewById(R.id.talk_tv_to_register);

		layoutInput = (LinearLayout) findViewById(R.id.talk_account_input);
		layoutWaiting = (LinearLayout) findViewById(R.id.talk_account_waiting);
		tvInfo = (TextView) findViewById(R.id.talk_account_info);

		etIpocid.setOnTouchListener(this);
		if (!Config.funcUserIdAndPwdIsNumber)
		{
			etIpocid.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
			etPwd.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
		}
		etPwd.setOnTouchListener(this);
		if (Config.funcUserRegistration)
		{
//			tvRegister.setText(R.string.talk_register);
//			tvRegister.setOnClickListener(this);
		}
		btnLogin.setOnClickListener(this);

		TextView logo = (TextView) findViewById(R.id.logo);
		logo.setText(Config.app_name);

		if (Config.funcShowCustomLogo)
		{
			TextView logoText = (TextView) findViewById(R.id.talk_copyright_text);
			ImageView logoImage = (ImageView) findViewById(R.id.talk_copyright_logo);
			if(Config.funcShowCustomLogoStringId1 != 0)
				logoText.setText(getString(Config.funcShowCustomLogoStringId1));
			if(Config.funcShowCustomLogoIconId != 0)
				logoImage.setImageResource(Config.funcShowCustomLogoIconId);
		}
	}

	@Override
	public void finish()
	{
		// TODO Auto-generated method stub
		super.finish();
		Log.e(AccountActivity.class, "finish");
		AirAccountManager.getInstance().setAccountListener(null);
	}

	@SuppressWarnings("deprecation")
	protected Dialog onCreateDialog(int id)
	{
		if (id == R.id.talk_dialog_login_waiting)
		{
			ProgressDialog dialog = new ProgressDialog(this);
			dialog.setMessage(getString(R.string.talk_logining));
			dialog.setOnCancelListener(new OnCancelListener()
			{

				@Override
				public void onCancel(DialogInterface dialog)
				{
					// TODO Auto-generated method stub
					AirServices.iOperator.putBoolean(AirAccountManager.KEY_HB, false);
					AirtalkeeAccount.getInstance().Logout();
				}
			});
			return dialog;
		}
		else if (id == R.id.talk_dialog_network_error)
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(getString(R.string.talk_network_error_hint));
			builder.setPositiveButton(getString(R.string.talk_set_network), new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int whichButton)
				{
					dialog.cancel();
					ApnManager.startActivitySetting(AccountActivity.this);
				}
			});

			builder.setNegativeButton(getString(R.string.talk_exit), new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int whichButton)
				{
					dialog.cancel();
					finish();
				}
			});
			return builder.create();
		}
		else if(id == R.id.talk_dialog_ascess_network)
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(getString(R.string.talk_protocol));
			builder.setCancelable(false);
			builder.setPositiveButton(R.string.talk_ok, new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int whichButton)
				{
					dialog.cancel();
					new Task().execute();
				}
			});

			builder.setNegativeButton(R.string.talk_no, new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int whichButton)
				{
					dialog.cancel();
					finish();
				}
			});
			return builder.create();
		}
		return super.onCreateDialog(id);
	}

	@Override
	public void onClick(View v)
	{
		// TODO Auto-generated method stub
		switch (v.getId())
		{
			case R.id.talk_btn_login:
			{
				Util.hideSoftInput(instance);
				// boolean network = ApnManager.activeNetworkFeature(this);
				// if (network)
				{
					String ipocId = etIpocid.getText().toString();
					String pwd = etPwd.getText().toString();
					Log.i(AccountActivity.class, ipocId + pwd);
					if (!ipocId.equals("") && !pwd.equals(""))
					{
						if (pwd.length() < 6)
						{
							Util.Toast(this, getString(R.string.talk_pwd_error),R.drawable.ic_error);
							return;
						}

						if (Config.funcUserPhone)
							AirtalkeeAccount.getInstance().Login(ipocId, pwd);
						else
						{
							if (Util.IsUserName(ipocId))
							{
								AirtalkeeUserInfo.getInstance().UserInfoGetIdByUserId(ipocId, pwd);
							}
							else
							{
								AirtalkeeAccount.getInstance().Login(ipocId, pwd);
							}
						}

						accountStateShow(STATE_LOGIN);
					}
					else
					{
						Util.Toast(this, getString(R.string.talk_account_isnotnull),R.drawable.ic_error);
					}
				}
				// else
				// {
				// showDialog(R.id.talk_dialog_network_error);
				// }
				break;
			}
//			case R.id.talk_tv_to_register:
//			{
//				Intent it = new Intent(this, UserRegisterPrepareActivity.class);
//				startActivity(it);
//				break;
//			}
			default:
				break;
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		// TODO Auto-generated method stub
		if (event.getAction() == MotionEvent.ACTION_DOWN)
			Util.hideSoftInput(instance);
		return super.onTouchEvent(event);
	}

	@Override
	public void onUserIdGetByPhoneNum(int result, AirContact contact)
	{
		// TODO Auto-generated method stub
		if (result == 0 && contact != null)
		{
			AirtalkeeAccount.getInstance().Login(contact.getIpocId(), contact.getPwd());
		}
		else
		{
			accountStateShow(STATE_IDLE);
			Util.Toast(this, getString(R.string.talk_login_login_failed_user_or_password),R.drawable.ic_error);
		}
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

	@Override
	public boolean onTouch(View v, MotionEvent event)
	{
		// TODO Auto-generated method stub

		switch (v.getId())
		{
			case R.id.talk_et_ipocid:
			case R.id.talk_et_ipocpwd:
			{
				v.setFocusableInTouchMode(true);
			}
		}

		return false;
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event)
	{
		// TODO Auto-generated method stub
		if (event.getAction() == KeyEvent.ACTION_DOWN && (event.getKeyCode() == KeyEvent.KEYCODE_HOME || event.getKeyCode() == KeyEvent.KEYCODE_BACK))
		{
			System.exit(0);
		}
		return super.dispatchKeyEvent(event);
	}

	@Override
	public void onMmiHeartbeatException(int result)
	{
		// TODO Auto-generated method stub
		if (result == AirtalkeeAccount.ACCOUNT_RESULT_ERR_SINGLE)
		{
			// tv.setText(R.string.talk_account_other);
			// showDialog(R.id.talk_dialog_login_single);
			Toast.makeText1(this, getString(R.string.talk_account_other), Toast.LENGTH_LONG).show();
		}
	}
	
	@Override
	public void onUserIdGetByImei(int state, String uid, String pwd)
	{
		// TODO Auto-generated method stub
		if(state == 0)
		{
			etIpocid.setText(uid);
			etPwd.setText(pwd);
			Util.Toast(this, getString(R.string.talk_logining));
			AirtalkeeAccount.getInstance().Login(uid,pwd);
			accountStateShow(STATE_LOGIN);
		}
		else if(state == 1)
		{
			Util.Toast(this, getString(R.string.talk_account_bind_error),R.drawable.ic_error);
			Util.Toast(this, getString(R.string.talk_account_get_error),R.drawable.ic_error);
		}
		else
		{
			Util.Toast(this, getString(R.string.talk_account_get_error),R.drawable.ic_error);
		}
	}

	@Override
	public void onMmiHeartbeatLogin(int result)
	{
		// TODO Auto-generated method stub
		//	removeDialog(R.id.talk_dialog_login_waiting);
		if (result == AirtalkeeAccount.ACCOUNT_RESULT_OK)
		{
			accountStateShow(STATE_LOADING);
			//AirServices.iOperator.putBoolean(AirAccountManager.KEY_HB, true);
		}
		else
		{
			accountStateShow(STATE_IDLE);
			if (result == AirtalkeeAccount.ACCOUNT_RESULT_ERR_LICENSE)
			{
				String msg = "";
				msg += getString(R.string.talk_authorization_info1) + Config.model + "\r\n";
				msg += getString(R.string.talk_authorization_info2) + Util.getImei(this) + "\r\n\r\n";
				msg += getString(R.string.talk_authorization_info3);
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(getString(R.string.talk_authorization_title));
				builder.setMessage(msg);
				builder.setCancelable(false);
				builder.setPositiveButton(getString(R.string.talk_ok), new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int whichButton)
					{
						dialog.cancel();
					}
				});
				builder.show();
			}
			else
			{
				Util.Toast(this, Util.loginInfo(result, this),R.drawable.ic_error);
			}
		}
	}

	@Override
	public void onMmiHeartbeatLogout()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onChannelListGet(boolean isOk, List<AirChannel> channels)
	{
		// TODO Auto-generated method stub
		Log.i(AccountActivity.class, "AccountActivity onChannelListGet");
		if (isOk)
		{
			Log.i(AccountActivity.class, "AccountActivity onChannelListGet OK!");
			Intent it = new Intent(this, HomeActivity.class);
			startActivity(it);
			if (AirServices.getInstance() != null)
			{
				AirServices.getInstance().versionCheck();
			}
			finish();
		}
		else
		{
			Log.i(AccountActivity.class, "AccountActivity onChannelListGet Fail!");
			accountStateShow(STATE_IDLE);
			Util.Toast(this, getString(R.string.talk_channel_list_getfail),R.drawable.ic_error);
		}
	}

	@Override
	public void onChannelMemberListGet(String channelId, List<AirContact> members)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onChannelOnlineCount(LinkedHashMap<String, Integer> online)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onChannelPersonalCreateNotify(AirChannel ch)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onChannelPersonalDeleteNotify(AirChannel ch)
	{
		// TODO Auto-generated method stub

	}

}
