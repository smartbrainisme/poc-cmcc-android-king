package com.cmccpoc.activity.home.widget;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.WindowManager;
import com.airtalkee.sdk.AirtalkeeMessage;
import com.airtalkee.sdk.AirtalkeeSessionManager;
import com.airtalkee.sdk.entity.AirContact;
import com.airtalkee.sdk.entity.AirSession;
import com.airtalkee.sdk.util.Log;
import com.cmccpoc.R;
import com.cmccpoc.Util.Sound;
import com.cmccpoc.Util.Util;
import com.cmccpoc.activity.SessionNewActivity;
import com.cmccpoc.activity.home.HomeActivity;
import com.cmccpoc.activity.home.widget.AlertDialog.DialogListener;
import com.cmccpoc.control.AirSessionControl;
import com.cmccpoc.services.AirServices;

/**
 * 临时呼叫的接听和挂断弹窗（被叫）
 * @author Yao
 */
public class InCommingAlertDialog extends AlertDialog implements DialogListener
{
	private AirSession temAirSession;

	public InCommingAlertDialog(Context ct, AirSession s, AirContact caller)
	{
		super(ct);
		temAirSession = s;
		if (s != null)
		{
			title = caller.getDisplayName() + "   " + ct.getString(R.string.talk_incoming);
			content = ct.getString(R.string.talk_incoming_tip);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setCancelable(false);
		getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		setContentView(R.layout.dialog_call_receiver_layout);
		initView();
		fillView();
		setListener(this);
	}

	@Override
	public void onClickOk(int id,Object obj)
	{
		Log.i(InCommingAlertDialog.class, "InCommingAlertDialog click ok!");
		AirSession session = AirSessionControl.getInstance().getCurrentSession();
		if (session.getType() == AirSession.TYPE_DIALOG)
		{
			AirSessionControl.getInstance().SessionEndCall(session);
		}
		Sound.stopSound(Sound.PLAYER_INCOMING_RING);
		AirtalkeeSessionManager.getInstance().SessionIncomingAccept(temAirSession);
		AirtalkeeMessage.getInstance().MessageSystemGenerate(temAirSession, getContext().getString(R.string.talk_call_state_incoming_call), false);
		switchToSessionDialog(temAirSession);
		if (Util.isBackground(AirServices.getInstance()))
		{
			Intent home = new Intent(AirServices.getInstance(), HomeActivity.class);
			home.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			AirServices.getInstance().startActivity(home);
		}
		this.cancel();
	}

	@Override
	public void onClickCancel(int id)
	{
		try
		{
			Sound.stopSound(Sound.PLAYER_INCOMING_RING);
			AirtalkeeSessionManager.getInstance().SessionIncomingReject(temAirSession);
			AirtalkeeMessage.getInstance().MessageSystemGenerate(temAirSession, temAirSession.getCaller(), getContext().getString(R.string.talk_call_state_rejected_call), true);
			this.cancel();
		}
		catch (Exception e)
		{
			// TODO: handle exception
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		// TODO Auto-generated method stub

		switch (keyCode)
		{
			case KeyEvent.KEYCODE_CALL:
			{
				this.cancel();
				Sound.stopSound(Sound.PLAYER_INCOMING_RING);
				// isCalling = false;
				AirtalkeeSessionManager.getInstance().SessionIncomingAccept(temAirSession);
				AirtalkeeMessage.getInstance().MessageSystemGenerate(temAirSession, getContext().getString(R.string.talk_call_state_incoming_call), false);
				try
				{
					switchToSessionDialog(temAirSession);
				}
				catch (Exception e)
				{
					// TODO: handle exception
				}

				return true;
			}
			case KeyEvent.KEYCODE_ENDCALL:
			{
				cancel();
				Sound.stopSound(Sound.PLAYER_INCOMING_RING);
				AirtalkeeSessionManager.getInstance().SessionIncomingReject(temAirSession);
				AirtalkeeMessage.getInstance().MessageSystemGenerate(temAirSession, temAirSession.getCaller(), getContext().getString(R.string.talk_call_state_rejected_call), true);
				return true;
			}
			case KeyEvent.KEYCODE_MENU:
			case KeyEvent.KEYCODE_BACK:
			{
				if (Sound.soundIsPlaying(Sound.PLAYER_INCOMING_RING))
				{
					Sound.stopSound(Sound.PLAYER_INCOMING_RING);
				}
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * 从当前会话切换到临时会话
	 * @param session 会话Entity
	 */
	private void switchToSessionDialog(AirSession session)
	{
		AirSessionControl.getInstance().setOnMmiSessionListener(null);
		if (session != null)
		{
			AirtalkeeSessionManager.getInstance().getSessionByCode(session.getSessionCode());
			final HomeActivity mInstance = HomeActivity.getInstance();
			if(mInstance != null)
			{
				mInstance.onViewChanged(session.getSessionCode());
				mInstance.pageIndex = HomeActivity.PAGE_PTT;
				mInstance.panelCollapsed();
			}
			if (SessionNewActivity.getInstance() != null)
			{
				SessionNewActivity.getInstance().finish();
			}
		}
		this.cancel();
	}

	@Override
	public void onClickOk(int id, boolean isChecked)
	{
		// TODO Auto-generated method stub

	}
}
