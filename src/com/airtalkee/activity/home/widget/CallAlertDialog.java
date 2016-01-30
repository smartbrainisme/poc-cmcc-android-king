package com.airtalkee.activity.home.widget;

import java.util.List;
import android.R.integer;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import com.airtalkee.R;
import com.airtalkee.Util.Toast;
import com.airtalkee.activity.TempSessionActivity;
import com.airtalkee.activity.home.HomeActivity;
import com.airtalkee.activity.home.SessionDialogActivity;
import com.airtalkee.activity.home.SessionNewActivity;
import com.airtalkee.activity.home.widget.AlertDialog.DialogListener;
import com.airtalkee.control.AirSessionControl;
import com.airtalkee.listener.OnMmiSessionListener;
import com.airtalkee.sdk.AirtalkeeMessage;
import com.airtalkee.sdk.AirtalkeeSessionManager;
import com.airtalkee.sdk.entity.AirContact;
import com.airtalkee.sdk.entity.AirSession;
import com.airtalkee.sdk.util.Log;
import com.airtalkee.services.AirServices;

public class CallAlertDialog extends AlertDialog implements android.view.View.OnClickListener, DialogListener, OnMmiSessionListener
{
	private String sessionCode;
	private AirSession session;
	public interface OnAlertDialogCancelListener
	{
		public void onDialogCancel(int reason);
	}
	OnAlertDialogCancelListener listener;
	
	public CallAlertDialog(Context context, String title, String content, String sessionCode, int id,OnAlertDialogCancelListener l)
	{
		this(context, title, content, sessionCode, id);
		this.listener = l;
	}

	
	public CallAlertDialog(Context context, String title, String content, String sessionCode, int id)
	{
		super(context, title, content, "", "", null, id);
		this.sessionCode = sessionCode;
		setListener(this);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{

		setContentView(R.layout.dialog_call_receiver_layout);
		initView();
		fillView();

		session = AirtalkeeSessionManager.getInstance().getSessionByCode(sessionCode);
		AirSessionControl.getInstance().setOnMmiSessionListener(this);
		if (null != session)
		{
			if (session.getSpecialNumber() == 0)
			{
				AirSessionControl.getInstance().SessionMakeCall(session);
			}
			else
			{
				AirSessionControl.getInstance().SessionMakeSpecialCall(session);
			}
			AirtalkeeMessage.getInstance().MessageSystemGenerate(session, getContext().getString(R.string.talk_call_state_outgoing_call), false);
		}

	}

	protected void fillView()
	{

		if (TextUtils.isEmpty(title))
		{
			tvTitle.setVisibility(View.GONE);
		}

		if (TextUtils.isEmpty(content))
		{
			tvContent.setVisibility(View.GONE);
		}

		s.setVisibility(View.GONE);

		tvTitle.setText(title);
		tvContent.setText(content);
	}

	private void finishCall()
	{
		Log.e(TempSessionActivity.class, "handleSession.SessionBye(session)");
		if (session != null && session.getSessionState() != AirSession.SESSION_STATE_IDLE)
		{
			AirSessionControl.getInstance().SessionEndCall(session);
		}
	}

	@Override
	public void onClickOk(int id,Object object)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onClickCancel(int id)
	{
		// TODO Auto-generated method stub
		finishCall();
	}

	@Override
	public void onSessionOutgoingRinging(AirSession session)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onSessionEstablishing(AirSession session)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onSessionEstablished(AirSession session, boolean isOk)
	{
		// TODO Auto-generated method stub
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
	public void onSessionReleased(AirSession session, int reason)
	{
		// TODO Auto-generated method stub
		AirSessionControl.getInstance().setOnMmiSessionListener(null);
		this.cancel();
		this.session = null;
		if(listener != null)
		{
			listener.onDialogCancel(reason);
		}
	}

	@Override
	public void onSessionPresence(AirSession session, List<AirContact> membersAll, List<AirContact> membersPresence)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onSessionMemberUpdate(AirSession session, List<AirContact> members, boolean isOk)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onClickOk(int id, boolean isChecked)
	{
		// TODO Auto-generated method stub

	}

}
