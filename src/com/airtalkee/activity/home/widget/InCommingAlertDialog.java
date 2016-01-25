package com.airtalkee.activity.home.widget;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.WindowManager;
import com.airtalkee.R;
import com.airtalkee.Util.Sound;
import com.airtalkee.activity.TempSessionActivity;
import com.airtalkee.activity.home.HomeActivity;
import com.airtalkee.activity.home.SessionDialogActivity;
import com.airtalkee.activity.home.widget.AlertDialog.DialogListener;
import com.airtalkee.control.AirSessionControl;
import com.airtalkee.sdk.AirtalkeeMessage;
import com.airtalkee.sdk.AirtalkeeSessionManager;
import com.airtalkee.sdk.entity.AirContact;
import com.airtalkee.sdk.entity.AirSession;
import com.airtalkee.sdk.util.Log;
import com.airtalkee.services.AirServices;

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
		if (HomeActivity.getInstance() == null)
		{
			Intent home = new Intent(AirServices.getInstance(), HomeActivity.class);
			AirServices.getInstance().startActivity(home);
		}
		Sound.stopSound(Sound.PLAYER_INCOMING_RING);
		AirtalkeeSessionManager.getInstance().SessionIncomingAccept(temAirSession);
		AirtalkeeMessage.getInstance().MessageSystemGenerate(temAirSession, getContext().getString(R.string.talk_call_state_incoming_call), false);
//		if (TempSessionActivity.getInstance() != null && TempSessionActivity.getInstance().getSession() != null && !TextUtils.equals(TempSessionActivity.getInstance().getSession().getSessionCode(), temAirSession.getSessionCode()))
//		{
//			TempSessionActivity.getInstance().setSession(temAirSession);
//		}
		switchToSessionDialog(temAirSession);
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

	private void switchToSessionDialog(AirSession session)
	{
		AirSessionControl.getInstance().setOnMmiSessionListener(null);
		if (session != null)
		{
			AirtalkeeSessionManager.getInstance().getSessionByCode(session.getSessionCode());
			if(HomeActivity.getInstance() != null)
			{
				HomeActivity.getInstance().onViewChanged(session.getSessionCode());
				HomeActivity.getInstance().panelCollapsed();
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
