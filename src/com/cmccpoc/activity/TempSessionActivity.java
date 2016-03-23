package com.cmccpoc.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.AirtalkeeMessage;
import com.airtalkee.sdk.AirtalkeeSessionManager;
import com.airtalkee.sdk.entity.AirSession;
import com.airtalkee.sdk.util.Log;
import com.airtalkee.sdk.util.Utils;
import com.cmccpoc.R;
import com.cmccpoc.Util.ThemeUtil;
import com.cmccpoc.Util.Util;
import com.cmccpoc.config.Config;
import com.cmccpoc.control.AirSessionControl;
import com.cmccpoc.listener.OnMmiSessionBoxRefreshListener;
import com.cmccpoc.services.AirServices;

public class TempSessionActivity extends ActivityBase implements
		OnClickListener, OnMmiSessionBoxRefreshListener
{

	private static TempSessionActivity mInstance;
	private View parentView = null;
	private SessionBox sessionBox;
	private AirSession session;

	private ImageView ivHuang;
	private View btnHangUp;
	private TextView tvTitle;
	private long currentTimeInMillis = 0;

	public boolean isShowing = false;

	private int actionType = AirServices.TEMP_SESSION_TYPE_MESSAGE;
	private boolean actionVideo = false;

	public static TempSessionActivity getInstance()
	{
		return mInstance;
	}

	public void setSession(AirSession s)
	{
		this.session = s;
		sessionBox.setSession(s);
	}

	public AirSession getSession()
	{
		return session;
	}

	@Override
	protected void onCreate(Bundle bundle)
	{
		super.onCreate(bundle);
		setRequestedOrientation(Config.screenOrientation);
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		mInstance = this;


		tvTitle = (TextView) findViewById(R.id.tv_main_title);
		ivHuang = (ImageView) findViewById(R.id.bottom_left_icon);
		ImageView ivRight = (ImageView) findViewById(R.id.bottom_right_icon);
		ivRight.setImageResource(ThemeUtil.getResourceId(R.attr.theme_nav_contact_selected, this));
		findViewById(R.id.talk_menu_right_button).setOnClickListener(this);
		btnHangUp = findViewById(R.id.menu_left_button);
		btnHangUp.setOnClickListener(this);

		bundle = getIntent().getExtras();
		if (bundle != null)
		{
			actionType = bundle.getInt("type");
			actionVideo = bundle.getBoolean("video");
			String sessionCode = bundle.getString("sessionCode");
			session = AirtalkeeSessionManager.getInstance().getSessionByCode(sessionCode);
			if (session != null)
			{
				sessionBox = new SessionBox(this, parentView, this, AirSession.TYPE_DIALOG);
				sessionBox.setSession(session);
				if (actionType == AirServices.TEMP_SESSION_TYPE_OUTGOING)
				{
					if (session.getSpecialNumber() == 0)
					{
						AirSessionControl.getInstance().SessionMakeCall(session);
					}
					else
					{
						AirSessionControl.getInstance().SessionMakeSpecialCall(session);
					}
					AirtalkeeMessage.getInstance().MessageSystemGenerate(session, getString(R.string.talk_call_state_outgoing_call), false);
				}
				else if (actionType == AirServices.TEMP_SESSION_TYPE_MESSAGE || session.getMessageUnreadCount() > 0)
				{
					sessionBox.tabJumpToPage(SessionBox.PAGE_MSG);
				}
			}
			else
			{
				finish();
			}
		}
		else
		{
			finish();
		}
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		if (session != null)
		{
			tvTitle.setText(session.getDisplayName());
			AirtalkeeSessionManager.getInstance().SessionLock(session, true);
		}
		sessionBox.setSession(session);
		isShowing = true;
	}

	@Override
	protected void onPause()
	{
		// TODO Auto-generated method stub
		super.onPause();
		if (session != null)
			AirtalkeeSessionManager.getInstance().SessionLock(session, false);
		isShowing = false;
	}

	@Override
	public void finish()
	{
		super.finish();
		finishCall();
		AirtalkeeMessage.getInstance().MessageListMoreClean(session);
		if (AirtalkeeAccount.getInstance().isAccountRunning())
		{
			Intent it = new Intent(this, MainActivity.class);
			startActivity(it);
		}
		mInstance = null;
		isShowing = false;
	}

	private void finishCall()
	{
		Log.e(TempSessionActivity.class, "handleSession.SessionBye(session)");
		if (session != null && session.getSessionState() != AirSession.SESSION_STATE_IDLE)
		{
			AirSessionControl.getInstance().SessionEndCall(session);
		}
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
	public void onAttachedToWindow()
	{
		this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD);
		super.onAttachedToWindow();
	}

	@Override
	public void onClick(View v)
	{
		// TODO Auto-generated method stub
		switch (v.getId())
		{
			case R.id.menu_left_button:
			{
				switch (actionType)
				{
					case AirServices.TEMP_SESSION_TYPE_INCOMING:
					case AirServices.TEMP_SESSION_TYPE_OUTGOING:
					case AirServices.TEMP_SESSION_TYPE_RESUME:
						if (sessionBox.tabIndex() == SessionBox.PAGE_PTT)
						{
							finish();
						}
						else
						{
							if (session != null && session.getSessionState() != AirSession.SESSION_STATE_IDLE)
							{
								finishCall();
							}
							else
							{
								finish();
							}
						}
						break;
					case AirServices.TEMP_SESSION_TYPE_MESSAGE:
						if (session != null && session.getSessionState() != AirSession.SESSION_STATE_IDLE)
						{
							finishCall();
						}
						else
						{
							finish();
						}
						break;
				}
				break;
			}
			case R.id.talk_menu_right_button:
			{
				if (session != null)
				{
					if (session.getSpecialNumber() != 0)
					{
						Util.Toast(this, getString(R.string.talk_special_forbid_invite));
						break;
					}
					Intent intent = new Intent();
					intent.setClass(this, TempSessionSelectedActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					intent.putExtra("sessionCode", session.getSessionCode());
					startActivity(intent);
					// vAdd.setVisibility(vAdd.getVisibility() == View.VISIBLE ?
					// View.GONE : View.VISIBLE);
				}
				break;
			}
			default:
				break;
		}
	}

	@Override
	@Deprecated
	protected Dialog onCreateDialog(final int id)
	{
		// TODO Auto-generated method stub
		switch (id)
		{
			case R.id.talk_dialog_message_txt_send_fail:
			case R.id.talk_dialog_message_txt:
			{
				final ListAdapter items = mSimpleAdapter(this, sessionBox.sessionBoxMessage.menuArray, R.layout.account_switch_listitem, R.id.AccountNameView);
				return new AlertDialog.Builder(this).setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int whichButton)
					{
						removeDialog(id);
						if (items instanceof SimpleAdapter)
						{
							sessionBox.sessionBoxMessage.onListItemLongClick(id, whichButton);
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

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		sessionBox.sessionBoxMessage.onActivityResult(requestCode, resultCode, data);
		sessionBox.sessionBoxTalk.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event)
	{
		// TODO Auto-generated method stub
		if (Config.pttVolumeKeySupport)
		{
			if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP || event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN)
			{
				if (AirtalkeeAccount.getInstance().isEngineRunning())
				{
					if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0)
					{
						AirtalkeeSessionManager.getInstance().TalkRequest(session);
					}
					else if (event.getAction() == KeyEvent.ACTION_UP)
					{
						AirtalkeeSessionManager.getInstance().TalkRelease(session);
					}
				}
				return true;
			}
		}

		if (event.getKeyCode() == Config.pttButtonKeycode)
		{
			if (AirtalkeeAccount.getInstance().isEngineRunning())
			{
				if (session.getSessionState() == AirSession.SESSION_STATE_DIALOG)
				{
					if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0)
					{
						AirtalkeeSessionManager.getInstance().TalkRequest(session);
					}
					else if (event.getAction() == KeyEvent.ACTION_UP)
					{
						AirtalkeeSessionManager.getInstance().TalkRelease(session);
					}
				}
				else if (session.getSessionState() == AirSession.SESSION_STATE_IDLE)
				{
					if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0)
					{
						AirSessionControl.getInstance().SessionMakeCall(session);
					}
				}
			}
			return true;
		}
		else if (event.getKeyCode() == KeyEvent.KEYCODE_BACK)
		{
			Log.e(TempSessionActivity.class, Utils.getCurrentTimeInMillis() + "");
			if (sessionBox.onKeyEvent(event))
			{
				return true;
			}
			if (session.getSessionState() != AirSession.SESSION_STATE_IDLE)
			{
				if (Utils.getCurrentTimeInMillis() - currentTimeInMillis > 5000)
				{
					currentTimeInMillis = Utils.getCurrentTimeInMillis();
					Toast.makeText(this, R.string.talk_session_back, Toast.LENGTH_SHORT).show();
					return true;
				}
			}
		}
		/*
		 * else if (event.getKeyCode() == KeyEvent.KEYCODE_HOME) { if
		 * (event.getAction() == KeyEvent.ACTION_UP) { if
		 * (session.getSessionState() != AirSession.SESSION_STATE_IDLE) { if
		 * (android.os.Build.VERSION.RELEASE.startsWith("2.")) {
		 * Log.e(TempSessionActivity.class, Utils.getCurrentTimeInMillis() +
		 * ""); if (Utils.getCurrentTimeInMillis() - currentTimeInMillis > 5000)
		 * { currentTimeInMillis = Utils.getCurrentTimeInMillis();
		 * Toast.makeText(this, R.string.talk_session_back, 1).show(); return
		 * true; } else { finish(); } } else { finish(); } } else { finish(); }
		 * } }
		 */
		return super.dispatchKeyEvent(event);
	}

	@Override
	public void onMmiSessionRefresh(AirSession session)
	{
		// TODO Auto-generated method stub
		if (session != null)
		{
			tvTitle.setText(session.getDisplayName());
			switch (session.getSessionState())
			{
				case AirSession.SESSION_STATE_IDLE:
				{
					ivHuang.setImageResource(ThemeUtil.getResourceId(R.attr.theme_ic_topbar_back, this));
					break;
				}
				case AirSession.SESSION_STATE_DIALOG:
				{
					ivHuang.setImageResource(R.drawable.incoming_reject_icon);
					break;
				}
				case AirSession.SESSION_STATE_CALLING:
				{
					ivHuang.setImageResource(R.drawable.incoming_reject_icon);
					break;
				}
			}
		}
	}

	@Override
	public void onMmiSessionEstablished(AirSession session)
	{
		// TODO Auto-generated method stub
		if (isShowing && actionVideo && sessionBox.sessionBoxTalk != null)
		{
			Intent intent = new Intent();
			intent.setClass(this, VideoSessionActivity.class);
			intent.putExtra("sessionCode", session.getSessionCode());
			intent.putExtra("video", true);
			startActivity(intent);
		}
	}

	@Override
	public void onMmiSessionReleased(AirSession session)
	{
		// TODO Auto-generated method stub
		if (this.session == session && (actionType == AirServices.TEMP_SESSION_TYPE_OUTGOING || actionType == AirServices.TEMP_SESSION_TYPE_INCOMING) && sessionBox.tabIndex() == SessionBox.PAGE_PTT)
		{
			this.finish();
		}
	}

}
