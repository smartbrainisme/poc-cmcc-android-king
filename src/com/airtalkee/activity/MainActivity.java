package com.airtalkee.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import com.airtalkee.R;
import com.airtalkee.Util.Util;
import com.airtalkee.bluetooth.BluetoothManager;
import com.airtalkee.config.Config;
import com.airtalkee.control.AirAccountManager;
import com.airtalkee.control.AirSessionControl;
import com.airtalkee.listener.OnMmiAccountListener;
import com.airtalkee.receiver.ReceiverMediaButton;
import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.entity.AirSession;
import com.airtalkee.sdk.util.Log;
import com.airtalkee.services.AirServices;
import com.airtalkee.widget.ViewControllerManager;
import com.airtalkee.widget.ViewControllerSlideView;
import com.airtalkee.widget.ViewControllerSlideView.ViewControllerSlideScrollListener;

public class MainActivity extends ActivityBase implements OnMmiAccountListener, OnClickListener, ViewControllerSlideScrollListener
{
	public ViewControllerSlideView viewControllerSlideView;
	private static MainActivity mInstance;

	public MainSessionView viewMiddle = null;
	public MainPageView viewLeft = null;

	public boolean isShowing = false;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		mInstance = this;
	}

	@Override
	protected void onResume()
	{
		// TODO Auto-generated method stub
		super.onResume();
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN, WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
		Log.e(MainActivity.class, "MainActivity - onResume");

		viewLeft.setListener();
		viewMiddle.refreshNetState();

		viewLeft.refreshNotice();
		viewLeft.refreshUser();

		Util.closeNotification(Util.NOTIFI_ID_MESSAGE);
		Util.closeNotification(Util.NOTIFI_ID_VOICE_RECORD);

		if (AirServices.getInstance() != null && TempSessionActivity.getInstance() != null && AirSessionControl.getInstance().getCurrentSession() != null
			&& AirSessionControl.getInstance().getCurrentSession().getType() == AirSession.TYPE_DIALOG)
		{
			AirServices.getInstance().switchToSessionTemp(AirSessionControl.getInstance().getCurrentSession().getSessionCode(), AirServices.TEMP_SESSION_TYPE_RESUME, this);
		}

		isShowing = true;
	}

	@Override
	protected void onPause()
	{
		// TODO Auto-generated method stub
		super.onPause();
		Log.e(MainActivity.class, "MainActivity - onPause");
		isShowing = false;
		viewMiddle.onPause();
	}

	public boolean isMenuShowing()
	{
		return viewControllerSlideView.isShowMenu();
	}

	@Override
	public void finish()
	{
		// TODO Auto-generated method stub
		super.finish();
		isShowing = false;
		AirAccountManager.getInstance().setAccountListener(null);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		// TODO Auto-generated method stub
		return super.onTouchEvent(event);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void onAttachedToWindow()
	{
//		this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD);
		super.onAttachedToWindow();
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event)
	{
		// TODO Auto-generated method stub

		return super.dispatchKeyEvent(event);
	}

	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.talk_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onViewControllerSlideScrollFinished(boolean isMenuShowing)
	{
		// TODO Auto-generated method stub
		viewMiddle.sessionBox.setMenuShowing(isMenuShowing);
	}


	public boolean onOptionsItemSelected(MenuItem item)
	{
		super.onOptionsItemSelected(item);
		switch (item.getItemId())
		{
		}
		return true;
	}

	@SuppressWarnings("deprecation")
	@Override
	protected Dialog onCreateDialog(final int id)
	{
		// TODO Auto-generated method stub
		return super.onCreateDialog(id);
	}

	public SimpleAdapter mSimpleAdapter(Context contexts, String[] array, int layout, int id)
	{
		if (array == null)
			return null;
		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		data.clear();
		for (int i = 0; i < array.length; i++)
		{
			Map<String, Object> listItem = new HashMap<String, Object>();
			listItem.put("accountName", array[i]);
			data.add(listItem);
		}
		return new SimpleAdapter(this, data, layout, new String[] { "accountName" }, new int[] { id });
	}

	@Override
	public void onClick(View v)
	{
		// TODO Auto-generated method stub

	}
	
	public AlertDialog.Builder BuildExitDialog(Context context)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(getString(R.string.talk_exit_tip));
		final CheckBox cb = new CheckBox(context);
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
				AirtalkeeAccount.getInstance().Logout();
				finish();
			}
		});

		builder.setNegativeButton(getString(R.string.talk_session_call_cancel), new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int whichButton)
			{
				dialog.cancel();
			}
		});
		return builder;
	}

	// ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// //////////////////////-----------------------------------------------------------------////////////////////////
	// //////////////////////------------------------ EVENT�¼�
	// --------------------------////////////////////////
	// //////////////////////-----------------------------------------------------------------////////////////////////
	// ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@SuppressWarnings("deprecation")
	@Override
	public void onMmiHeartbeatException(int result)
	{
		// TODO Auto-generated method stub
		TextView tv = (TextView) viewMiddle.layoutNetWorkTip;
		if (result == AirtalkeeAccount.ACCOUNT_RESULT_ERR_SINGLE)
		{
			tv.setText(R.string.talk_account_other);
			showDialog(R.id.talk_dialog_login_single);
		}
		else
			tv.setText(R.string.talk_network_warning);
		viewMiddle.refreshNetState();
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onMmiHeartbeatLogin(int result)
	{
		// TODO Auto-generated method stub
		removeDialog(R.id.talk_dialog_login_waiting);
		if (result != 0)
		{
			if (result == AirtalkeeAccount.ACCOUNT_RESULT_ERR_SINGLE)
			{
				showDialog(R.id.talk_dialog_login_single);
			}
			else
			{
				// showDialog(R.id.talk_dialog_login_fail);
			}
		}

		if (AirtalkeeAccount.getInstance().isEngineRunning())
		{
			if (viewMiddle != null)
			{
				viewMiddle.layoutNetWorkTip.setVisibility(View.GONE);
			}
		}
	}

	@Override
	public void onMmiHeartbeatLogout()
	{
		// TODO Auto-generated method stub

	}

}
