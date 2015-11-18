package com.airtalkee.activity.home.widget;

import java.util.List;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import com.airtalkee.R;
import com.airtalkee.activity.TempSessionActivity;
import com.airtalkee.activity.home.SessionDialogActivity;
import com.airtalkee.activity.home.widget.AlertDialog.DialogListener;
import com.airtalkee.control.AirSessionControl;
import com.airtalkee.listener.OnMmiSessionListener;
import com.airtalkee.sdk.AirtalkeeMessage;
import com.airtalkee.sdk.AirtalkeeSessionManager;
import com.airtalkee.sdk.entity.AirContact;
import com.airtalkee.sdk.entity.AirSession;
import com.airtalkee.sdk.util.Log;
import com.airtalkee.services.AirServices;

public class CallAlertDialog extends AlertDialog implements
		android.view.View.OnClickListener ,DialogListener,OnMmiSessionListener{
	private String sessionCode;
	private AirSession session;
	
	public CallAlertDialog(Context context, String title, String content,
			String sessionCode,  int id) {
		super(context, title, content, "", "", null, id);
		this.sessionCode = sessionCode;
		setListener(this);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		setContentView(R.layout.dialog_call_receiver_layout);
		initView();
		fillView();
		
		session = AirtalkeeSessionManager.getInstance().getSessionByCode(sessionCode);
		AirSessionControl.getInstance().setOnMmiSessionListener(this);
		if(null != session)
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

	protected void fillView() {

		if (TextUtils.isEmpty(title)) {
			tvTitle.setVisibility(View.GONE);
		}

		if (TextUtils.isEmpty(content)) {
			tvContent.setVisibility(View.GONE);
		}

		s.setVisibility(View.GONE );

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
	public void onClickOk(int id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onClickCancel(int id) {
		// TODO Auto-generated method stub
		finishCall();
	}

	@Override
	public void onSessionOutgoingRinging(AirSession session) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSessionEstablishing(AirSession session) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSessionEstablished(AirSession session, boolean isOk) {
		// TODO Auto-generated method stub
		AirSessionControl.getInstance().setOnMmiSessionListener(null);
		Intent it = new Intent(getContext(), SessionDialogActivity.class);
		it.putExtra("sessionCode", session.getSessionCode());
		it.putExtra("type", AirServices.TEMP_SESSION_TYPE_OUTGOING);
		getContext().startActivity(it);
		this.cancel();
	}

	@Override
	public void onSessionReleased(AirSession session, int reason) {
		// TODO Auto-generated method stub
		AirSessionControl.getInstance().setOnMmiSessionListener(null);
		this.cancel();
		this.session = null;
	}

	@Override
	public void onSessionPresence(AirSession session,
			List<AirContact> membersAll, List<AirContact> membersPresence) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSessionMemberUpdate(AirSession session,
			List<AirContact> members, boolean isOk) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onClickOk(int id, boolean isChecked)
	{
		// TODO Auto-generated method stub
		
	}

}
