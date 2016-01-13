package com.airtalkee.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.ListAdapter;
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
import com.airtalkee.sdk.AirtalkeeChannel;
import com.airtalkee.sdk.AirtalkeeSessionManager;
import com.airtalkee.sdk.entity.AirSession;
import com.airtalkee.sdk.util.Log;
import com.airtalkee.sdk.util.Utils;
import com.airtalkee.services.AirServices;
import com.airtalkee.widget.ViewControllerManager;
import com.airtalkee.widget.ViewControllerSlideView;
import com.airtalkee.widget.ViewControllerSlideView.ViewControllerSlideScrollListener;

public class MainActivity extends ActivityBase implements OnMmiAccountListener, OnClickListener, ViewControllerSlideScrollListener
{
	public ViewControllerSlideView viewControllerSlideView;
	private ViewControllerManager navigationController;
	private static MainActivity mInstance;

	public MainSessionView viewMiddle = null;
	public MainPageView viewLeft = null;

	public boolean isShowing = false;

	public static MainActivity getInstance()
	{
		return mInstance;
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		if (!Utils.isEmpty(Config.model) && Config.model.startsWith("OINOM"))
		{
			final Window win = getWindow();
			win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
			win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
		}
		setRequestedOrientation(Config.screenOrientation);
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		mInstance = this;
		viewMiddle = new MainSessionView(this);
		viewControllerSlideView = new ViewControllerSlideView(this);
		viewControllerSlideView.setListener(this);
		navigationController = new ViewControllerManager(this);
		navigationController.setRootView(viewControllerSlideView.getMainView());
		navigationController.setRootController(viewMiddle);
		viewLeft = new MainPageView(this, viewControllerSlideView, navigationController);
		setContentView(viewControllerSlideView);

		AirAccountManager.getInstance().setAccountListener(this);

		isShowing = true;
	}

	@Override
	protected void onResume()
	{
		// TODO Auto-generated method stub
		super.onResume();
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN, WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
		Log.e(MainActivity.class, "MainActivity - onResume");
		ReceiverMediaButton.HeadsetPluginCheck(this);

		viewLeft.setListener();
		viewMiddle.refreshNetState();

		viewLeft.refreshList();
		viewLeft.refreshNotice();
		viewLeft.refreshUser();
		viewMiddle.onResume();
		viewMiddle.refreshSession();
		viewMiddle.refreshSessionMember();
		viewMiddle.refreshNewMsg();

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
		viewMiddle.sessionBox.sessionBoxMessage.onActivityResult(requestCode, resultCode, data);
		viewMiddle.sessionBox.sessionBoxTalk.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		// TODO Auto-generated method stub
		/*
		if (viewControllerSlideView.isShowMenu())
		{
			if (viewControllerSlideView.isShowMenuLeft())
			{
				viewControllerSlideView.transLeftShow();
			}
			else if (viewControllerSlideView.isShowMenuRight())
			{
				viewControllerSlideView.transRightShow();
			}
			return true;
		}
		else
		{
			return super.onTouchEvent(event);
		}
		*/
		return super.onTouchEvent(event);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		// TODO Auto-generated method stub
		if (navigationController.onKeyDown(keyCode, event))
			return true;
		else
		{
			return super.onKeyDown(keyCode, event);
		}
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
		
		if (event.getKeyCode() == Config.pttButtonKeycode
			|| (Config.pttVolumeKeySupport && (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP || event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN)))
		{
			if (AirtalkeeAccount.getInstance().isEngineRunning() && AirSessionControl.getInstance().getCurrentChannelSession() != null
				&& AirSessionControl.getInstance().getCurrentChannelSession().getChannel() != null)
			{
				if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0)
				{
					Log.i(MainActivity.class, "dispatchKeyEvent PTT-DOWN");
					AirtalkeeSessionManager.getInstance().TalkRequest(AirSessionControl.getInstance().getCurrentChannelSession(), AirSessionControl.getInstance().getCurrentChannelSession().getChannel().isRoleAppling());
				}
				else if (event.getAction() == KeyEvent.ACTION_UP)
				{
					Log.i(MainActivity.class, "dispatchKeyEvent PTT-UP");
					AirtalkeeSessionManager.getInstance().TalkRelease(AirSessionControl.getInstance().getCurrentChannelSession());
				}
			}
			return true;
		}
		else
		{
			switch (event.getKeyCode())
			{
				case KeyEvent.KEYCODE_HOME:
				{
					if (event.getAction() == KeyEvent.ACTION_UP && !Config.screenAlwaysPtt)
						finish();
					break;
				}
				case KeyEvent.KEYCODE_DPAD_LEFT:
				{
					if (event.getAction() == KeyEvent.ACTION_DOWN)
						viewControllerSlideView.transLeftShow();
					return true;
				}
				case KeyEvent.KEYCODE_DPAD_RIGHT:
				{
					if (event.getAction() == KeyEvent.ACTION_DOWN)
						viewControllerSlideView.transRightShow();
					return true;
				}
				case KeyEvent.KEYCODE_CALL:
				{
					if (event.getAction() == KeyEvent.ACTION_DOWN)
					{
						viewMiddle.sessionBox.sessionBoxMember.callSelectMember(true);
						viewMiddle.sessionBox.sessionBoxMember.callSelectClean();
					}
					return true;
				}
				case KeyEvent.KEYCODE_BACK:
				{
					if (Config.screenAlwaysPtt)
						return true;
					if (viewMiddle.onKeyEvent(event))
					{
						return true;
					}
					if (event.getAction() == KeyEvent.ACTION_DOWN)
					{
						if (!viewControllerSlideView.isShowMenu() && viewMiddle.sessionBox.tabIndex() == SessionBox.PAGE_PTT)
						{
							viewLeft.refreshList();
							viewControllerSlideView.transLeftShow();
							return true;
						}
						if (!Util.isSoftKeybordOpen(this))
						{
							if (viewMiddle.sessionBox.tabIndex() == SessionBox.PAGE_MEMBER && viewMiddle.sessionBox.sessionBoxMember.adapterMember.getSelectedMemberList() != null
								&& viewMiddle.sessionBox.sessionBoxMember.adapterMember.getSelectedMemberList().size() > 0)
							{
								viewMiddle.sessionBox.sessionBoxMember.callSelectClean();
								return true;
							}
							if (!viewControllerSlideView.isShowMenu() && viewMiddle.sessionBox.tabIndex() != SessionBox.PAGE_PTT)
							{
								viewMiddle.sessionBox.tabResetPage();
								return true;
							}
						}
					}
					break;
				}
				case KeyEvent.KEYCODE_CAMERA:
				{
					android.util.Log.e("m", "action =" + event.getAction());
					if (event.getAction() == KeyEvent.ACTION_DOWN)
					{
						return true;
					}
					break;
				}
			}
		}

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

	public boolean onMenuOpened(int featureId, Menu menu)
	{
		try
		{
			if (AirSessionControl.getInstance().getCurrentChannelSession() != null && AirSessionControl.getInstance().getCurrentChannelSession().getType() == AirSession.TYPE_CHANNEL
				&& AirSessionControl.getInstance().getCurrentChannelSession().getSessionState() != AirSession.SESSION_STATE_IDLE)
			{
				menu.findItem(R.id.talk_menu_exit_channel).setEnabled(true);
			}
			else
			{
				menu.findItem(R.id.talk_menu_exit_channel).setEnabled(false);
			}
			
			if(Config.marketCode == Config.MARKET_UNI_STRONG)
			{
				menu.findItem(R.id.talk_menu_exit).setVisible(false);
			}
		}
		catch (Exception e)
		{
			// TODO: handle exception
		}
		return super.onMenuOpened(featureId, menu);
	}

	@SuppressWarnings("deprecation")
	public boolean onOptionsItemSelected(MenuItem item)
	{
		super.onOptionsItemSelected(item);
		switch (item.getItemId())
		{
			case R.id.talk_menu_exit_channel:
			{
				String channelId = null;
				if (AirSessionControl.getInstance().getCurrentChannelSession() != null)
					channelId = AirSessionControl.getInstance().getCurrentChannelSession().getSessionCode();
				if (channelId != null)
					AirSessionControl.getInstance().SessionChannelOut(channelId);
				break;
			}
			case R.id.talk_menu_exit:
			{
				showDialog(R.id.talk_dialog_exit);
				break;
			}
		}
		return true;
	}

	@SuppressWarnings("deprecation")
	@Override
	protected Dialog onCreateDialog(final int id)
	{
		// TODO Auto-generated method stub
		switch (id)
		{
			case R.id.talk_dialog_login_single:
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage(getString(R.string.talk_account_other));
				builder.setNegativeButton(getString(R.string.talk_exit), new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int whichButton)
					{
						System.exit(0);
					}
				});
				builder.setOnCancelListener(new OnCancelListener()
				{
					@Override
					public void onCancel(DialogInterface dialog)
					{
						// TODO Auto-generated method stub
						System.exit(0);
					}
				});
				return builder.create();
			}
			case R.id.talk_dialog_login_fail:
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage(getString(R.string.talk_login_fail));
				builder.setPositiveButton(getString(R.string.talk_login_retry), new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int whichButton)
					{
						dialog.cancel();
						AirtalkeeAccount.getInstance().NetworkOpen();
					}
				});

				builder.setNegativeButton(getString(R.string.talk_exit), new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int whichButton)
					{
						dialog.cancel();
						AirServices.iOperator.putBoolean(AirAccountManager.KEY_HB, false);
						AirtalkeeAccount.getInstance().Logout();
						finish();
					}
				});
				return builder.create();
			}
			case R.id.talk_dialog_connect_waiting:
			{
				ProgressDialog dialog = new ProgressDialog(this);
				dialog.setMessage(getString(R.string.talk_channel_building));
				return dialog;
			}
			case R.id.talk_dialog_group_get_wait:
			{
				ProgressDialog dialog = new ProgressDialog(this);
				dialog.setMessage(getString(R.string.talk_channel_get_wait));
				return dialog;
			}
			case R.id.talk_dialog_waiting:
			{
				ProgressDialog dialog = new ProgressDialog(this);
				dialog.setMessage(getString(R.string.talk_tip_waiting));
				return dialog;
			}
			case R.id.talk_dialog_member_delete:
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage(getString(R.string.talk_channel_member_delete));
				builder.setPositiveButton(getString(R.string.talk_ok), new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int whichButton)
					{
						viewMiddle.sessionBox.sessionBoxMember.deleteSelectMember();
					}
				});

				builder.setNegativeButton(getString(R.string.talk_no), new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int whichButton)
					{
						dialog.cancel();
					}
				});
				return builder.create();
			}

			case R.id.talk_dialog_exit:
			{
				AlertDialog.Builder builder = BuildExitDialog(this);
				return builder.create();
			}

			case R.id.talk_dialog_channel_delete:
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(getString(R.string.talk_delete_tip));
				builder.setPositiveButton(getString(R.string.talk_ok), new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int whichButton)
					{
						AirtalkeeChannel.getInstance().PersonalChannelDelete(viewLeft.adapter.deleteRoomId);
						showDialog(R.id.talk_dialog_waiting);
					}
				});
				builder.setNegativeButton(getString(R.string.talk_no), new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int whichButton)
					{
						dialog.cancel();
					}
				});
				return builder.create();
			}
			case R.id.talk_dialog_message_txt_send_fail:
			case R.id.talk_dialog_message_txt:
			{
				final ListAdapter items = mSimpleAdapter(this, viewMiddle.sessionBox.sessionBoxMessage.menuArray, R.layout.account_switch_listitem, R.id.AccountNameView);
				return new AlertDialog.Builder(this).setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int whichButton)
					{
						removeDialog(id);
						if (items instanceof SimpleAdapter)
						{
							viewMiddle.sessionBox.sessionBoxMessage.onListItemLongClick(id, whichButton);
						}
					}
				}).setOnCancelListener(new OnCancelListener()
				{
					@Override
					public void onCancel(DialogInterface dialog)
					{
						// TODO Auto-generated method stub
						removeDialog(id);
					}
				}).create();
			}
		}
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
				BluetoothManager.getInstance().btStop();
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
