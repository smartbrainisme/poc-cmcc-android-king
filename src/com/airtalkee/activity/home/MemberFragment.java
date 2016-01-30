package com.airtalkee.activity.home;

import java.util.ArrayList;
import java.util.HashMap;
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
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.airtalkee.R;
import com.airtalkee.Util.Toast;
import com.airtalkee.Util.Util;
import com.airtalkee.activity.home.AdapterMember.CheckedCallBack;
import com.airtalkee.activity.home.widget.AlertDialog;
import com.airtalkee.activity.home.widget.AlertDialog.DialogListener;
import com.airtalkee.activity.home.widget.CallAlertDialog;
import com.airtalkee.activity.home.widget.CallAlertDialog.OnAlertDialogCancelListener;
import com.airtalkee.activity.home.widget.MemberAllView;
import com.airtalkee.activity.home.widget.MemberAllView.MemberCheckListener;
import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.AirtalkeeChannel;
import com.airtalkee.sdk.AirtalkeeContactPresence;
import com.airtalkee.sdk.AirtalkeeMessage;
import com.airtalkee.sdk.AirtalkeeSessionManager;
import com.airtalkee.sdk.OnContactPresenceListener;
import com.airtalkee.sdk.controller.SessionController;
import com.airtalkee.sdk.entity.AirChannel;
import com.airtalkee.sdk.entity.AirContact;
import com.airtalkee.sdk.entity.AirSession;
import com.airtalkee.sdk.util.Log;
import com.airtalkee.services.AirServices;
import com.airtalkee.widget.MListView;
import com.airtalkee.widget.MyRelativeLayout;

public class MemberFragment extends BaseFragment implements OnClickListener,
		OnItemClickListener, CheckedCallBack, MemberCheckListener,
		OnContactPresenceListener, TextWatcher
{
	private static final int DIALOG_CALL = 99;
	private TextView tabMemberSession, tabMemberAll;
	private ImageView ivSerachIcon;
	private List<AirContact> tempCallMembers = null;
	Map<String, AirContact> tempCallMembersCache = new TreeMap<String, AirContact>();
	private LinkedHashMap<Integer, TextView> ids = new LinkedHashMap<Integer, TextView>();
	private MListView lvMember;
	private AdapterMember adapterMember;
	private CallAlertDialog alertDialog;
	private LinearLayout memAllContainer, addMemberPanel, searchPannelChannel, searchPannelAll;
	private EditText searchEditChannel, searchEditAll;
	private ImageView ivSearch;
	private Button btnSearch;
	private int currentSelectPage = R.id.tab_member_session;
	private MemberAllView memberAllView;
	private boolean memberSessionChecked = false;
	private boolean memberAllChecked = false;
	List<AirContact> memberSearchResult = new ArrayList<AirContact>();
	AlertDialog dialog;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		// registerViewReSize();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		v = inflater.inflate(getLayout(), container, false);

		addMemberPanel = (LinearLayout) findViewById(R.id.add_member_panel);
		addMemberPanel.setOnClickListener(this);

		btnSearch = (Button) findViewById(R.id.btn_search);
		btnSearch.setOnClickListener(this);
		
		lvMember = (MListView) findViewById(R.id.talk_lv_member);
		lvMember.setAdapter(adapterMember = new AdapterMember(getActivity(), null, null, true, true, this));
		lvMember.setOnItemClickListener(this);
		tabMemberSession = (TextView) findViewById(R.id.tab_member_session);
		tabMemberAll = (TextView) findViewById(R.id.tab_member_all);

		ivSerachIcon = (ImageView) findViewById(R.id.iv_search_icon);
		ivSerachIcon.setOnClickListener(this);

		memAllContainer = (LinearLayout) findViewById(R.id.mem_container);
		memberAllView = new MemberAllView(getActivity(), this);
		memAllContainer.addView(memberAllView);

		searchPannelChannel = (LinearLayout) findViewById(R.id.serach_pannel);
		searchPannelAll = (LinearLayout) memberAllView.findViewById(R.id.serach_pannel);

		searchEditChannel = (EditText) findViewById(R.id.et_search);
		searchEditAll = (EditText) memberAllView.findViewById(R.id.et_search);
		
		searchEditChannel.addTextChangedListener(this);
		ivSearch = (ImageView) findViewById(R.id.iv_search);
		ids.put(R.id.tab_member_session, tabMemberSession);
		ids.put(R.id.tab_member_all, tabMemberAll);

		// refreshTab(R.id.tab_member_session);

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
		if (getSession() != null)
		{
			refreshTab(R.id.tab_member_session);
			AirtalkeeContactPresence.getInstance().setContactPresenceListener(this);
			if (getSession().getType() == AirSession.TYPE_CHANNEL)
			{
				addMemberPanel.setVisibility(View.GONE);
			}
			else
			{
				addMemberPanel.setVisibility(View.VISIBLE);
			}
			
		}
	}

	@Override
	public void onPause()
	{
		// TODO Auto-generated method stub
		super.onPause();
		AirtalkeeContactPresence.getInstance().setContactPresenceListener(null);
		AirtalkeeContactPresence.getInstance().ContactPresenceUnsubscribe();
	}

	@Override
	public int getLayout()
	{
		// TODO Auto-generated method stub
		return R.layout.frag_member_layout;
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
		searchPannelAll.setVisibility(View.GONE);
		searchPannelChannel.setVisibility(View.GONE);
		ivSerachIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_search_white));
		Util.hideSoftInput(getActivity());
		Iterator<Integer> iter = ids.keySet().iterator();
		while (iter.hasNext())
		{
			Integer i = iter.next();
			TextView v = ids.get(i);
			v.setOnClickListener(this);
			v.setSelected(i == id);
		}
		try
		{
			if (id == R.id.tab_member_session) // 频道成员
			{
				lvMember.setVisibility(View.VISIBLE);
				memAllContainer.setVisibility(View.GONE);
				if(memberSearchResult.size() > 0)
				{
					setSession(getSession());
				}
				tabMemberSession.setEnabled(false);
				tabMemberAll.setEnabled(true);
			}
			else // 全部成员
			{
				if (memberAllView == null || memberAllView.memberAll.size() < 1)
				{
					memberAllView = new MemberAllView(getActivity(), this);
					memberAllView.adapterMember.notifyDataSetChanged();
					memAllContainer.addView(memberAllView);
				}
				memberAllView.adapterMember.notifyMember(memberAllView.memberAll);
				lvMember.setVisibility(View.GONE);
				memAllContainer.setVisibility(View.VISIBLE);
				tabMemberSession.setEnabled(true);
				tabMemberAll.setEnabled(false);
			}
		}
		catch (Exception e)
		{
			// TODO: handle exception
		}
	}

	private void refreshSearch(int id)
	{
		Iterator<Integer> iter = ids.keySet().iterator();
		while (iter.hasNext())
		{
			Integer i = iter.next();
			TextView v = ids.get(i);
			v.setOnClickListener(this);
			v.setSelected(i == id);
		}
		try
		{
			if (id == R.id.tab_member_session) // 频道成员
			{
				if (searchPannelChannel.getVisibility() == View.GONE)
				{
					ivSerachIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_search_orange));
					searchPannelChannel.setVisibility(View.VISIBLE);
					searchEditChannel.requestFocus();
					Util.showSoftInput(getActivity());
				}
				else
				{
					ivSerachIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_search_white));
					searchPannelChannel.setVisibility(View.GONE);
					searchEditChannel.clearFocus();
					Util.hideSoftInput(getActivity());
				}
			}
			else
			// 全部成员
			{
				if (searchPannelAll.getVisibility() == View.GONE)
				{
					ivSerachIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_search_orange));
					searchPannelAll.setVisibility(View.VISIBLE);
					searchEditAll.requestFocus();
					Util.showSoftInput(getActivity());
				}
				else
				{
					ivSerachIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_search_white));
					searchPannelAll.setVisibility(View.GONE);
					searchEditAll.clearFocus();
					Util.hideSoftInput(getActivity());
				}
			}
		}
		catch (Exception e)
		{
			// TODO: handle exception
		}
	}

	@Override
	public void onClick(View v)
	{
		// TODO Auto-generated method stub
		switch (v.getId())
		{
			case R.id.tab_member_session:
			case R.id.tab_member_all:
			{
				currentSelectPage = v.getId();
				refreshTab(currentSelectPage);
				break;
			}
			case R.id.add_member_panel:
			{
				Intent it = new Intent(getActivity(), SessionAddActivity.class);
				it.putExtra("sessionCode", getSession().getSessionCode());
				it.putExtra("type", AirServices.TEMP_SESSION_TYPE_MESSAGE);
				getActivity().startActivity(it);
				break;
			}
			case R.id.iv_search_icon:
			{
				refreshSearch(currentSelectPage);
				break;
			}
			case R.id.btn_search:
			{
				searchByKey();
				break;
			}
		}
	}

	private void searchByKey()
	{
		String key = searchEditChannel.getText().toString();
		memberSearchResult.clear();
		setSession(getSession());
		for (int i = 0; i < adapterMember.getCount(); i++)
		{
			AirContact contact = (AirContact) adapterMember.getItem(i);
			if (contact.getDisplayName().equalsIgnoreCase(key) || contact.getIpocId().equals(key) || contact.getDisplayName().contains(key) || contact.getIpocId().contains(key))
			{
				memberSearchResult.add(contact);
			}
		}
		refreshMembers(getSession(), memberSearchResult);
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
		try
		{
			adapterMember.notifyMember(session, members);
			adapterMember.notifyDataSetChanged();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

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
								Log.i(MemberFragment.class, "MemberFragment reason = "+reason);
								switch (reason)
								{
									case AirSession.SESSION_RELEASE_REASON_NOTREACH:
										dialog = new AlertDialog(getActivity(), null, getString(R.string.talk_call_offline_tip), getString(R.string.talk_session_call_cancel), getString(R.string.talk_call_leave_msg), listener, reason);
										dialog.show();
										break;
									case AirSession.SESSION_RELEASE_REASON_REJECTED:
										Toast.makeText1(AirServices.getInstance(), "对方已拒接", Toast.LENGTH_SHORT).show();
										break;
									case AirSession.SESSION_RELEASE_REASON_BUSY:
										Toast.makeText1(AirServices.getInstance(), "对方正在通话中，无法建立呼叫", Toast.LENGTH_SHORT).show();
										break;
								}
							}
						});
						alertDialog.show();
					}
					else
					{
						AirtalkeeSessionManager.getInstance().getSessionByCode(s.getSessionCode());
						HomeActivity.getInstance().pageIndex = BaseActivity.PAGE_IM;
						HomeActivity.getInstance().onViewChanged(s.getSessionCode());
						HomeActivity.getInstance().panelCollapsed();
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
					if (currentSelectPage != HomeActivity.PAGE_IM)
					{
						mediaStatusBar.setMediaStatusBarVisibility(isShow ? View.GONE : View.VISIBLE);
					}
				}
			}
		}, filter);
	}

	@Override
	public void onContactPresence(boolean isSubscribed, HashMap<String, Integer> presenceMap)
	{
		if (getSession() != null && getSession().getType() == AirSession.TYPE_DIALOG)
		{
			getSession().MembersSort();
		}
		adapterMember.notifyDataSetChanged();
		memberAllView.adapterMember.notifyDataSetChanged();
	}

	@Override
	public void onContactPresence(boolean isSubscribed, String uid, int state)
	{
		if (getSession() != null && getSession().getType() == AirSession.TYPE_DIALOG)
		{
			getSession().MembersSort();
		}
		adapterMember.notifyDataSetChanged();
		memberAllView.adapterMember.notifyDataSetChanged();
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after)
	{
		
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count)
	{
		btnSearch.setEnabled(!TextUtils.isEmpty(searchEditChannel.getText()));
		if (TextUtils.isEmpty(searchEditChannel.getText()))
		{
			setSession(getSession());
			ivSearch.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_member_search));
			ivSearch.setOnClickListener(null);
		}
		else
		{
			searchByKey();
			ivSearch.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_close_cicle));
			ivSearch.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					searchEditChannel.setText("");
				}
			});
		}
	}

	@Override
	public void afterTextChanged(Editable s)
	{
		
	}
}
