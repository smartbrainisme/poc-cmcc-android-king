package com.airtalkee.activity.home;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.TextView;

import com.airtalkee.R;
import com.airtalkee.Util.Util;
import com.airtalkee.activity.home.AdapterMember.CheckedCallBack;
import com.airtalkee.activity.home.widget.AlertDialog.DialogListener;
import com.airtalkee.activity.home.widget.CallAlertDialog;
import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.AirtalkeeChannel;
import com.airtalkee.sdk.AirtalkeeMessage;
import com.airtalkee.sdk.controller.SessionController;
import com.airtalkee.sdk.entity.AirChannel;
import com.airtalkee.sdk.entity.AirContact;
import com.airtalkee.sdk.entity.AirSession;
import com.airtalkee.services.AirServices;
import com.airtalkee.widget.MListView;

public class MemberFragment extends BaseFragment implements OnClickListener,
		OnItemClickListener, CheckedCallBack, DialogListener {
	private static final int DIALOG_CALL = 99;
	private TextView tabMemberSession, tabMemberAll;
	private List<AirContact> tempCallMembers = null;
	private LinkedHashMap<Integer, TextView> ids = new LinkedHashMap<Integer, TextView>();
	private MListView lvMember;
	private AdapterMember adapterMember;
	CallAlertDialog alertDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		v = inflater.inflate(getLayout(), container, false);

		lvMember = (MListView) findViewById(R.id.talk_lv_member);
		lvMember.setAdapter(adapterMember = new AdapterMember(getActivity(),
				null, null, true, true, this));
		lvMember.setOnItemClickListener(this);
		tabMemberSession = (TextView) findViewById(R.id.tab_member_session);
		tabMemberAll = (TextView) findViewById(R.id.tab_member_all);

		ids.put(R.id.tab_member_session, tabMemberSession);
		ids.put(R.id.tab_member_all, tabMemberAll);

		refreshTab(R.id.tab_member_session);

		if (mediaStatusBar != null)
			mediaStatusBar.setBarEnable(HomeActivity.PAGE_MEMBER, false);

		setSession(getSession());
		return v;
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

	}

	@Override
	public int getLayout() {
		// TODO Auto-generated method stub
		return R.layout.frag_member_layout;
	}

	@Override
	public void dispatchBarClickEvent(int page, int id) {
		// TODO Auto-generated method stub
		if (page == HomeActivity.PAGE_MEMBER) {
			switch (id) {
			case R.id.bar_left:

				alertDialog = new CallAlertDialog(getActivity(), "正在呼叫",
						"请稍后...",true, this, DIALOG_CALL);
				alertDialog.show();
				break;
			case R.id.bar_mid:
				AirtalkeeMessage.getInstance().MessageRecordPlayStop();
				callSelectMember(false);
				callSelectClean();
				break;
			case R.id.bar_right:
				callSelectClean();
				break;
			}
		}
	}

	public void refreshTab(int id) {
		Iterator<Integer> iter = ids.keySet().iterator();
		while (iter.hasNext()) {
			Integer i = iter.next();
			TextView v = ids.get(i);
			v.setOnClickListener(this);
			v.setSelected(i == id);
		}

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.tab_member_session:
		case R.id.tab_member_all:
			refreshTab(v.getId());
			break;
		}
	}

	public void setSession(AirSession s) {

		if (s != null) {
			switch (s.getType()) {
			case AirSession.TYPE_CHANNEL: {
				AirChannel c = AirtalkeeChannel.getInstance().ChannelGetByCode(
						s.getSessionCode());
				if (c != null) {
					c.MembersSort();
					refreshMembers(s, c.MembersGet());
				}
				break;
			}
			case AirSession.TYPE_DIALOG: {
				s.MembersSort();
				refreshMembers(s, s.getMemberAll());
				break;
			}
			}
			// refreshMemberOnline(s.SessionPresenceList());
		} else {
			refreshMembers(null, null);
			// sessionBoxMember.refreshMemberOnline(null);
		}
	}

	public void refreshMembers(AirSession session, List<AirContact> members) {
		// this.session = session;
		// sessionBox.sessionBoxTalk.refreshRole(false);
		adapterMember.notifyMember(session, members);
		adapterMember.notifyDataSetChanged();
		// refreshManageButtons(sessionBox.tabIndex() ==
		// SessionBox.PAGE_MEMBER);
	}

	public void refreshMembers() {
		adapterMember.notifyDataSetChanged();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		switch (parent.getId()) {
		case R.id.talk_lv_member: {
			CheckBox cb = (CheckBox) view
					.findViewById(R.id.talk_cb_group_member);
			AirContact c = (AirContact) adapterMember.getItem(position - 1);
			if (c != null) {
				if (!AirtalkeeAccount.getInstance().getUserId()
						.equals(c.getIpocId())) {
					if (cb != null)
						cb.setChecked(!cb.isChecked());
				}
			}
			break;
		}
		}
	}

	public void callSelectMember(boolean isCall) {
		if (adapterMember.getSelectedMemberList() != null
				&& adapterMember.getSelectedMemberList().size() > 0) {
			if (tempCallMembers == null)
				tempCallMembers = new ArrayList<AirContact>();
			tempCallMembers.clear();
			for (AirContact c : adapterMember.getSelectedMemberList()) {
				if (!TextUtils.equals(c.getIpocId(), AirtalkeeAccount
						.getInstance().getUserId())) {
					tempCallMembers.add(c);
				}
			}

			if (tempCallMembers.size() > 0) {
				if (AirtalkeeAccount.getInstance().isEngineRunning()) {
					AirSession s = SessionController
							.SessionMatch(tempCallMembers);
					AirServices.getInstance().switchToSessionTemp(
							s.getSessionCode(),
							isCall ? AirServices.TEMP_SESSION_TYPE_OUTGOING
									: AirServices.TEMP_SESSION_TYPE_MESSAGE,
							getActivity());
				} else {
					Util.Toast(getActivity(),
							getString(R.string.talk_network_warning));
				}
			} else {
				Util.Toast(getActivity(),
						getString(R.string.talk_tip_session_call));
			}
		} else {
			Util.Toast(getActivity(), getString(R.string.talk_tip_session_call));
		}
	}

	public void callSelectClean() {
		adapterMember.resetCheckBox();
		mediaStatusBar.setBarEnable(HomeActivity.PAGE_MEMBER, false);
		// layoutBottom.setVisibility(View.GONE);
		// layoutBtns.setVisibility(View.VISIBLE);
	}

	@Override
	public void onChecked(boolean isChecked) {
		// TODO Auto-generated method stub
		mediaStatusBar.setBarEnable(HomeActivity.PAGE_MEMBER, isChecked);
	}

	@Override
	public void onClickOk(int id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onClickCancel(int id) {
		// TODO Auto-generated method stub
		if (id == DIALOG_CALL) {
			
			
			Intent it = new Intent(getActivity(),SessionDialogActivity.class);
			startActivity(it);
//			AirtalkeeMessage.getInstance().MessageRecordPlayStop();
//			callSelectMember(true);
//			callSelectClean();
		}
	}

}
