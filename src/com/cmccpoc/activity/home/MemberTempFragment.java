package com.cmccpoc.activity.home;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.AirtalkeeChannel;
import com.airtalkee.sdk.AirtalkeeMessage;
import com.airtalkee.sdk.controller.SessionController;
import com.airtalkee.sdk.entity.AirChannel;
import com.airtalkee.sdk.entity.AirContact;
import com.airtalkee.sdk.entity.AirSession;
import com.cmccpoc.R;
import com.cmccpoc.Util.Util;
import com.cmccpoc.activity.home.adapter.AdapterMember;
import com.cmccpoc.activity.home.adapter.AdapterMember.CheckedCallBack;
import com.cmccpoc.activity.home.widget.AlertDialog;
import com.cmccpoc.activity.home.widget.CallAlertDialog;
import com.cmccpoc.activity.home.widget.MemberAllView;
import com.cmccpoc.activity.home.widget.AlertDialog.DialogListener;
import com.cmccpoc.activity.home.widget.CallAlertDialog.OnAlertDialogCancelListener;
import com.cmccpoc.activity.home.widget.MemberAllView.MemberCheckListener;
import com.cmccpoc.services.AirServices;
import com.cmccpoc.widget.MListView;
import com.cmccpoc.widget.MyRelativeLayout;

public class MemberTempFragment extends BaseFragment implements
		OnClickListener, OnItemClickListener, CheckedCallBack,
		MemberCheckListener
{
	private static final int DIALOG_CALL = 99;
	private TextView tabMemberSession, tabMemberAll;
	private List<AirContact> tempCallMembers = null;
	Map<String, AirContact> tempCallMembersCache = new TreeMap<String, AirContact>();
	private LinkedHashMap<Integer, TextView> ids = new LinkedHashMap<Integer, TextView>();
	private MListView lvMember;
	private AdapterMember adapterMember;
	private CallAlertDialog alertDialog;
	private LinearLayout memAllContainer, addMemberPanel;
	private int currentSelectPage = R.id.tab_member_all;
	private MemberAllView memberAllView;
	private boolean memberSessionChecked = false;
	private boolean memberAllChecked = false;
	AlertDialog dialog;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		registerViewReSize();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		v = inflater.inflate(getLayout(), container, false);

		memAllContainer = (LinearLayout) findViewById(R.id.mem_container);
		memAllContainer.addView(memberAllView = new MemberAllView(getActivity(), this));

		addMemberPanel = (LinearLayout) findViewById(R.id.add_member_panel);
		addMemberPanel.setOnClickListener(this);
		lvMember = (MListView) findViewById(R.id.talk_lv_member);
		lvMember.setAdapter(adapterMember = new AdapterMember(getActivity(), null, null, true, true, this));
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
	public void onResume()
	{
		// TODO Auto-generated method stub
		super.onResume();
		if (mediaStatusBar != null)
			mediaStatusBar.setBarEnable(HomeActivity.PAGE_MEMBER, false);
		setSession(getSession());
	}

	@Override
	public int getLayout()
	{
		// TODO Auto-generated method stub
		return R.layout.frag_member_layout_temp;
	}

	@Override
	public void dispatchBarClickEvent(int page, int id)
	{
		// TODO Auto-generated method stub
		if (page == HomeActivity.PAGE_MEMBER)
		{
			switch (id)
			{
				case R.id.bar_left:
					callSelectMember(true);
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

	public void refreshTab(int id)
	{
		Iterator<Integer> iter = ids.keySet().iterator();
		while (iter.hasNext())
		{
			Integer i = iter.next();
			TextView v = ids.get(i);
			v.setOnClickListener(this);
			v.setSelected(i == id);
		}

		if (id == R.id.tab_member_session)
		{
			lvMember.setVisibility(View.VISIBLE);
			memAllContainer.setVisibility(View.GONE);
		}
		else
		{
			lvMember.setVisibility(View.GONE);
			memAllContainer.setVisibility(View.VISIBLE);
		}

	}

	@Override
	public void onClick(View v)
	{
		// TODO Auto-generated method stub
		switch (v.getId())
		{
			case R.id.tab_member_session:
				addMemberPanel.setVisibility(View.VISIBLE);
				currentSelectPage = v.getId();
				refreshTab(v.getId());
				break;
			case R.id.tab_member_all:
				addMemberPanel.setVisibility(View.GONE);
				currentSelectPage = v.getId();
				refreshTab(v.getId());
				break;
			case R.id.add_member_panel:
			{
				Intent it = new Intent(getActivity(), SessionAddActivity.class);
				it.putExtra("sessionCode", getSession().getSessionCode());
				it.putExtra("type", AirServices.TEMP_SESSION_TYPE_MESSAGE);
				getActivity().startActivity(it);
				break;
			}
		}
	}

	public void setSession(AirSession s)
	{
		if (s != null)
		{
			switch (s.getType())
			{
				case AirSession.TYPE_CHANNEL:
				{
					AirChannel c = AirtalkeeChannel.getInstance().ChannelGetByCode(s.getSessionCode());
					if (c != null)
					{
						c.MembersSort();
						refreshMembers(s, c.MembersGet());
					}
					break;
				}
				case AirSession.TYPE_DIALOG:
				{
					s.MembersSort();
					refreshMembers(s, s.getMemberAll());
					break;
				}
			}
			// refreshMemberOnline(s.SessionPresenceList());
		}
		else
		{
			refreshMembers(null, null);
			// sessionBoxMember.refreshMemberOnline(null);
		}
	}

	public void refreshMembers(AirSession session, List<AirContact> members)
	{
		// this.session = session;
		// sessionBox.sessionBoxTalk.refreshRole(false);
		adapterMember.notifyMember(session, members);
		adapterMember.notifyDataSetChanged();
		// refreshManageButtons(sessionBox.tabIndex() ==
		// SessionBox.PAGE_MEMBER);
	}

	public void refreshMembers()
	{
		adapterMember.notifyDataSetChanged();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	{
		switch (parent.getId())
		{
			case R.id.talk_lv_member:
			{
				CheckBox cb = (CheckBox) view.findViewById(R.id.talk_cb_group_member);
				AirContact c = (AirContact) adapterMember.getItem(position - 1);
				if (c != null)
				{
					if (!AirtalkeeAccount.getInstance().getUserId().equals(c.getIpocId()))
					{
						if (cb != null)
							cb.setChecked(!cb.isChecked());
					}
				}
				break;
			}
		}
	}

	public void callSelectMember(boolean isCall)
	{
		if ((adapterMember.getSelectedMemberList() != null && adapterMember.getSelectedMemberList().size() > 0) || memberAllView.getSelectedMemberSize() > 0)
		{
			if (tempCallMembers == null)
				tempCallMembers = new ArrayList<AirContact>();
			tempCallMembers.clear();
			tempCallMembersCache.clear();
			for (AirContact c : adapterMember.getSelectedMemberList())
			{
				if (!TextUtils.equals(c.getIpocId(), AirtalkeeAccount.getInstance().getUserId()))
				{
					if (!tempCallMembersCache.containsKey(c.getIpocId()))
					{
						tempCallMembersCache.put(c.getIpocId(), c);
						tempCallMembers.add(c);
					}
				}
			}

			for (AirContact c : memberAllView.getSelectedMember())
			{
				if (!TextUtils.equals(c.getIpocId(), AirtalkeeAccount.getInstance().getUserId()))
				{
					if (!tempCallMembersCache.containsKey(c.getIpocId()))
					{
						tempCallMembersCache.put(c.getIpocId(), c);
						tempCallMembers.add(c);
					}
				}
			}

			if (tempCallMembers.size() > 0)
			{
				if (AirtalkeeAccount.getInstance().isEngineRunning())
				{
					AirSession s = SessionController.SessionMatch(tempCallMembers);
					if (isCall)
					{
						alertDialog = new CallAlertDialog(getActivity(), "正在呼叫" + s.getDisplayName(), "请稍后...", s.getSessionCode(), DIALOG_CALL, new OnAlertDialogCancelListener()
						{
							@Override
							public void onDialogCancel(int reason)
							{
								// TODO Auto-generated method stub
								switch (reason)
								{
									case AirSession.SESSION_RELEASE_REASON_NOTREACH:
										dialog = new AlertDialog(getActivity(), null, getString(R.string.talk_call_offline_tip), getString(R.string.talk_session_call_cancel), getString(R.string.talk_call_leave_msg), listener, reason);
										dialog.show();
										break;
									default:
										break;
								}
							}
						});
						alertDialog.show();
					}
					else
					{
						Intent it = new Intent(getActivity(), SessionDialogActivity.class);
						it.putExtra("sessionCode", s.getSessionCode());
						it.putExtra("type", AirServices.TEMP_SESSION_TYPE_MESSAGE);
						getActivity().startActivity(it);
					}

				}
				else
				{
					Util.Toast(getActivity(), getString(R.string.talk_network_warning));
				}
			}
			else
			{
				Util.Toast(getActivity(), getString(R.string.talk_tip_session_call));
			}
		}
		else
		{
			Util.Toast(getActivity(), getString(R.string.talk_tip_session_call));
		}
	}

	public void callSelectClean()
	{
		memberAllChecked = false;
		memberSessionChecked = false;
		adapterMember.resetCheckBox();
		memberAllView.resetCheckBox();
		mediaStatusBar.setBarEnable(HomeActivity.PAGE_MEMBER, false);
	}

	@Override
	public void onChecked(boolean isChecked)
	{
		// TODO Auto-generated method stub
		memberSessionChecked = isChecked;
		mediaStatusBar.setBarEnable(HomeActivity.PAGE_MEMBER, memberSessionChecked || memberAllChecked);
	}

	@Override
	public void onMemberChecked(boolean isChecked)
	{
		// TODO Auto-generated method stub
		memberAllChecked = isChecked;
		mediaStatusBar.setBarEnable(HomeActivity.PAGE_MEMBER, memberSessionChecked || memberAllChecked);
	}

	@Override
	public void onListItemLongClick(int id, int selectedItem)
	{
		// TODO Auto-generated method stub

	}

	private DialogListener listener = new DialogListener()
	{
		@Override
		public void onClickOk(int id, boolean isChecked)
		{
			// TODO Auto-generated method stub

		}

		@Override
		public void onClickOk(int id, Object object)
		{
			AirtalkeeMessage.getInstance().MessageRecordPlayStop();
			callSelectMember(false);
			callSelectClean();
		}

		@Override
		public void onClickCancel(int id)
		{
			// TODO Auto-generated method stub

		}
	};

	private void registerViewReSize()
	{
		final IntentFilter filter = new IntentFilter();
		filter.addAction(MyRelativeLayout.ACTION_ON_VIEW_RESIZE);
		filter.addCategory(Intent.CATEGORY_DEFAULT);

		getActivity().registerReceiver(new BroadcastReceiver()
		{
			@Override
			public void onReceive(Context context, Intent intent)
			{
				// TODO Auto-generated method stub
				if (intent.getAction().equals(MyRelativeLayout.ACTION_ON_VIEW_RESIZE))
				{
					boolean isShow = intent.getBooleanExtra(MyRelativeLayout.EXTRA_IS_SOFTKEYBOARD_SHOWN, false);
					mediaStatusBar.setMediaStatusBarVisibility(isShow ? View.GONE : View.VISIBLE);
				}
			}
		}, filter);
	}
}
