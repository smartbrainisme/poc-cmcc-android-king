package com.airtalkee.activity.home;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.airtalkee.R;
import com.airtalkee.Util.Util;
import com.airtalkee.activity.home.widget.CallAlertDialog;
import com.airtalkee.activity.home.widget.MemberAllView;
import com.airtalkee.activity.home.widget.MemberAllView.MemberCheckListener;
import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.AirtalkeeMessage;
import com.airtalkee.sdk.controller.SessionController;
import com.airtalkee.sdk.entity.AirContact;
import com.airtalkee.sdk.entity.AirSession;
import com.airtalkee.services.AirServices;

public class SessionNewActivity extends Activity implements OnClickListener,
		MemberCheckListener {

	private LinearLayout containner;
	private MemberAllView memAllView;
	private ViewGroup bottom;
	private List<AirContact> tempCallMembers = null;
	private CallAlertDialog alertDialog;
	private int DIALOG_CALL = 111;

	@Override
	protected void onCreate(Bundle bundle) {
		// TODO Auto-generated method stub
		super.onCreate(bundle);
		setContentView(R.layout.activity_session_new);
		bottom = (ViewGroup) findViewById(R.id.layout_bottom);
		memAllView = new MemberAllView(this, this);
		findViewById(R.id.btn_close).setOnClickListener(this);
		containner = (LinearLayout) findViewById(R.id.containner);
		containner.addView(memAllView);
		findViewById(R.id.bar_left).setOnClickListener(this);
		findViewById(R.id.bar_mid).setOnClickListener(this);
		findViewById(R.id.bar_right).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v.getId() == R.id.btn_close) {
			this.finish();
		} else if (v.getId() == R.id.bar_left) {
			callSelectMember(true);
		} else if (v.getId() == R.id.bar_mid) {
			AirtalkeeMessage.getInstance().MessageRecordPlayStop();
			callSelectMember(false);
			callSelectClean();
		} else if (v.getId() == R.id.bar_right) {
			callSelectClean();
		}
	}
	
	public void callSelectClean() {
		memAllView.resetCheckBox();
		refreshBottomView(false);
	}

	public void callSelectMember(boolean isCall) {
		if (tempCallMembers == null)
			tempCallMembers = new ArrayList<AirContact>();
		else
			tempCallMembers.clear();

		for (AirContact c : memAllView.getSelectedMember()) {
			if (!TextUtils.equals(c.getIpocId(), AirtalkeeAccount.getInstance()
					.getUserId())) {
				tempCallMembers.add(c);
			}
		}

		if (tempCallMembers.size() > 0) {
			if (AirtalkeeAccount.getInstance().isEngineRunning()) {

				AirSession s = SessionController.SessionMatch(tempCallMembers);
				if (isCall) {
					alertDialog = new CallAlertDialog(this, "正在呼叫"
							+ s.getDisplayName(), "请稍后...", s.getSessionCode(),
							DIALOG_CALL);
					alertDialog.show();
				} else {
					Intent it = new Intent(this, SessionDialogActivity.class);
					it.putExtra("sessionCode", s.getSessionCode());
					it.putExtra("type", AirServices.TEMP_SESSION_TYPE_MESSAGE);
					startActivity(it);
				}

			} else {
				Util.Toast(this, getString(R.string.talk_network_warning));
			}
		} else {
			Util.Toast(this, getString(R.string.talk_tip_session_call));
		}

	}

	@Override
	public void onMemberChecked(boolean isChecked) {
		// TODO Auto-generated method stub
		refreshBottomView(isChecked);
	}

	private void refreshBottomView(boolean isChecked) {
		for (int i = 0; i < bottom.getChildCount(); i++) {
			View child = bottom.getChildAt(i);
			child.setEnabled(isChecked);
		}
	}
}
