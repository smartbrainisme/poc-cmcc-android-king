package com.airtalkee.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.airtalkee.R;
import com.airtalkee.Util.ThemeUtil;
import com.airtalkee.Util.Util;
import com.airtalkee.adapter.AdapterUserList;
import com.airtalkee.adapter.AdapterUserListSearch;
import com.airtalkee.adapter.AdapterUserPath;
import com.airtalkee.config.Config;
import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.AirtalkeeChannel;
import com.airtalkee.sdk.AirtalkeeContactPresence;
import com.airtalkee.sdk.AirtalkeeUserInfo;
import com.airtalkee.sdk.OnChannelPersonalListener;
import com.airtalkee.sdk.OnContactPresenceListener;
import com.airtalkee.sdk.OnUserInfoListener;
import com.airtalkee.sdk.controller.SessionController;
import com.airtalkee.sdk.entity.AirChannel;
import com.airtalkee.sdk.entity.AirContact;
import com.airtalkee.sdk.entity.AirContactGroup;
import com.airtalkee.sdk.entity.AirContactTiny;
import com.airtalkee.sdk.entity.AirSession;
import com.airtalkee.sdk.util.Utils;
import com.airtalkee.services.AirServices;
import com.airtalkee.widget.MListView;
import com.airtalkee.widget.PagerHorizontal;
import com.airtalkee.widget.PagerHorizontal.OnScrollListener;

public class UserAllActivity extends ActivityBase implements OnClickListener, OnUserInfoListener, OnItemClickListener, OnTouchListener, OnScrollListener, OnChannelPersonalListener, OnContactPresenceListener
{
	
	private TextView userAllPath;
	private ImageView userAllPathPoint;
	private ListView userAllPathList;
	private MListView userList, userListSearch;
	private ProgressBar userProgressBar;
	private EditText userSearchKey;
	private PagerHorizontal pager;
	public static final int PAGE_LIST = 0;
	public static final int PAGE_SEARCH = 1;

	private LinearLayout selectLayout, selectPanel,layoutBottom1,layoutBottom2;
	private HorizontalScrollView selectPanelSrcoll;
	private TextView selectPanelCount;

	private AdapterUserPath adapterPathList;
	private AdapterUserList adapterUserList;
	private AdapterUserListSearch adapterUserListSearch;
	private boolean isShowingPathList = false;
	private List<AirContactGroup> pathList = new ArrayList<AirContactGroup>();

	private HashMap<String, AirContact> selectedContacts = new HashMap<String, AirContact>();
	private List<AirContact> selectedContactsIndexing = new ArrayList<AirContact>();
	
	private int gOrgId = -1;
	
	private int type =0;
	private String name ="";
	public static final int TYPE_CALL = 0;
	public static final int TYPE_ADD = 1;
	public static final int TYPE_CREATE_ADD = 2;
	
	@Override
	protected void onCreate(Bundle bundle)
	{
		// TODO Auto-generated method stub
		super.onCreate(bundle);
		setRequestedOrientation(Config.screenOrientation);
		setContentView(R.layout.activity_user_all);
		type = getIntent().getIntExtra("type", 0);
		name = getIntent().getStringExtra("name");
		doInitView();

		AirtalkeeUserInfo.getInstance().setOnUserInfoListener(this);
		refreshUserList(0);
	}

	@Override
	protected void onStart()
	{
		// TODO Auto-generated method stub
		super.onStart();

	}

	private void doInitView()
	{
		TextView ivTitle = (TextView) findViewById(R.id.tv_main_title);
		ivTitle.setText(R.string.talk_user_title);
		View btnLeft = findViewById(R.id.menu_left_button);
		ImageView ivLeft = (ImageView) findViewById(R.id.bottom_left_icon);
		ivLeft.setImageResource(ThemeUtil.getResourceId(R.attr.theme_ic_topbar_back, this) );
		btnLeft.setOnClickListener(this);

		RelativeLayout ivRightLay = (RelativeLayout) findViewById(R.id.talk_menu_right_button);
		ImageView ivRight = (ImageView) findViewById(R.id.bottom_right_icon);
		ivRight.setImageResource(ThemeUtil.getResourceId(R.attr.theme_ic_refresh, this) );
		ivRightLay.setOnClickListener(this);

		findViewById(R.id.user_all_path_selector).setOnClickListener(this);
		findViewById(R.id.user_all_search).setOnClickListener(this);
		findViewById(R.id.user_all_search_back).setOnClickListener(this);
		findViewById(R.id.user_all_search_do).setOnClickListener(this);
		userProgressBar = (ProgressBar) findViewById(R.id.user_all_progress);
		userAllPath = (TextView) findViewById(R.id.user_all_path);
		userAllPathPoint = (ImageView) findViewById(R.id.user_all_path_point);
		userAllPathList = (ListView) findViewById(R.id.user_all_path_list);
		userAllPathList.setOnItemClickListener(this);
		userList = (MListView) findViewById(R.id.user_all_list);
		userList.setOnItemClickListener(this);
		userList.setOnTouchListener(this);
		userListSearch = (MListView) findViewById(R.id.user_all_list_search);
		userListSearch.setOnItemClickListener(this);
		userListSearch.setOnTouchListener(this);
		userSearchKey = (EditText) findViewById(R.id.user_all_search_key);

		adapterPathList = new AdapterUserPath(this);
		userAllPathList.setAdapter(adapterPathList);

		adapterUserList = new AdapterUserList(this, selectedContacts);
		userList.setAdapter(adapterUserList);
		adapterUserListSearch = new AdapterUserListSearch(this, selectedContacts);
		userListSearch.setAdapter(adapterUserListSearch);

		selectLayout = (LinearLayout) findViewById(R.id.talk_user_member_opr);
		selectLayout.setVisibility(View.GONE);
		selectPanel = (LinearLayout) findViewById(R.id.user_member_opr_panel);
		layoutBottom1 = (LinearLayout) findViewById(R.id.talk_layout_bottom);
		layoutBottom2 = (LinearLayout) findViewById(R.id.talk_layout_session_m_bottom);
		selectPanelSrcoll = (HorizontalScrollView) findViewById(R.id.user_member_opr_panel_scroll);
		selectPanelCount = (TextView) findViewById(R.id.user_member_opr_panel_cnt);
		findViewById(R.id.talk_btn_session_call).setOnClickListener(this);
		findViewById(R.id.talk_btn_session_msg).setOnClickListener(this);
		findViewById(R.id.talk_btn_session_cancel).setOnClickListener(this);

		pager = (PagerHorizontal) findViewById(R.id.talk_user_pager);
		pager.setCurrentPage(PAGE_LIST);
		pager.addOnScrollListener(this);
		
		if(type == TYPE_CALL)
		{
			layoutBottom1.setVisibility(View.GONE);
			layoutBottom2.setVisibility(View.VISIBLE);
		}
		else
		{
			
			ivTitle.setText(R.string.talk_channel_title_add);
			layoutBottom1.setVisibility(View.VISIBLE);
			layoutBottom2.setVisibility(View.GONE);
			findViewById(R.id.talk_btn_ok).setOnClickListener(this);
			findViewById(R.id.talk_btn_cancel).setOnClickListener(this);
			AirtalkeeChannel.getInstance().setOnChannelPersonalListener(this);
		}
	}

	@Override
	public void finish()
	{
		// TODO Auto-generated method stub
		super.finish();
	}
	
	@Override
	protected void onPause()
	{
		// TODO Auto-generated method stub
		super.onPause();
		AirtalkeeContactPresence.getInstance().setContactPresenceListener(null);
		AirtalkeeContactPresence.getInstance().ContactPresenceUnsubscribe();
	}

	@Override
	protected void onResume()
	{
		// TODO Auto-generated method stub
		super.onResume();
		if (gOrgId >= 0)
			AirtalkeeContactPresence.getInstance().ContactPresenceSubscribe(gOrgId, false);
		AirtalkeeContactPresence.getInstance().setContactPresenceListener(this);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event)
	{
		// TODO Auto-generated method stub
		if (isShowingPathList)
		{
			refreshPathList(false);
			return true;
		}
		return false;
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event)
	{
		// TODO Auto-generated method stub
		if (event.getAction() == KeyEvent.ACTION_DOWN)
		{
			switch (event.getKeyCode())
			{
				case KeyEvent.KEYCODE_BACK:
				{
					if (pager.getCurrentPage() == PAGE_SEARCH)
					{
						pager.snapToPage(PAGE_LIST);
						return true;
					}
					else
					{
						AirContactGroup group = pathList.get(pathList.size() - 1);
						if (group != null && group.getParentGroup() != null)
						{
							userProgressBar.setVisibility(View.GONE);
							refreshUserList(group.getParentGroup().getGroupId());
							return true;
						}
						else if (selectedContacts.size() > 0)
						{
							selectPanelClean();
							return true;
						}
					}
					break;
				}
				default:
					break;
			}
		}
		return super.dispatchKeyEvent(event);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onClick(View v)
	{
		// TODO Auto-generated method stub
		switch (v.getId())
		{
			case R.id.menu_left_button:
			case R.id.bottom_left_icon:
			{
				if (pager.getCurrentPage() == PAGE_SEARCH)
				{
					pager.snapToPage(PAGE_LIST);
				}
				else
				{
					AirContactGroup group = pathList.get(pathList.size() - 1);
					if (group != null && group.getParentGroup() != null)
					{
						userProgressBar.setVisibility(View.GONE);
						refreshUserList(group.getParentGroup().getGroupId());
					}
					else
					{
						finish();
					}
				}
				break;
			}
			case R.id.talk_menu_right_button:
			case R.id.bottom_right_icon:
				AirContactGroup group = pathList.get(pathList.size() - 1);
				if (group != null)
				{
					userProgressBar.setVisibility(View.VISIBLE);
					AirtalkeeUserInfo.getInstance().UserInfoGetOrganizationTree(group.getGroupId());
				}
				break;
			case R.id.user_all_path_selector:
				refreshPathList();
				break;
			case R.id.talk_btn_session_call:
				doSelectionCall();
				selectPanelClean();
				finish();
				break;
			case R.id.talk_btn_ok:
				 List<AirContactTiny> tinys = getContactTinys();
				// selectPanelClean();
				 if(type == TYPE_ADD)
				 {
					 	AirChannel channel = new AirChannel();
						channel.setId(name);
						AirtalkeeChannel.getInstance().PersonalChannelMemberAdd(channel, tinys);
						showDialog(R.id.talk_dialog_waiting);
				 }
				 else if(type == TYPE_CREATE_ADD)
				 {
						AirtalkeeChannel.getInstance().PersonalChannelCreate(name, tinys);
						showDialog(R.id.talk_dialog_waiting);
				 }
				 
				break;
			case R.id.talk_btn_session_msg:
				doSelectionMessage();
				selectPanelClean();
				finish();
				break;
			case R.id.talk_btn_cancel:
			case R.id.talk_btn_session_cancel:
				selectPanelClean();
				break;
			case R.id.user_all_search:
				refreshPathList(false);
				pager.snapToPage(PAGE_SEARCH);
				break;
			case R.id.user_all_search_back:
				pager.snapToPage(PAGE_LIST);
				break;
			case R.id.user_all_search_do:
				String text = userSearchKey.getText().toString();
				if (text != null)
					text = text.trim();
				if (Utils.isEmpty(text))
				{
					Util.Toast(this, getString(R.string.talk_user_search_key_empty));
				}
				else
				{
					Util.hideSoftInput(this);
					userProgressBar.setVisibility(View.VISIBLE);
					AirtalkeeUserInfo.getInstance().UserInfoGetOrganizationTreeSearch(userSearchKey.getText().toString());
				}
				break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	{
		// TODO Auto-generated method stub
		switch (parent.getId())
		{
			case R.id.user_all_path_list:
			{
				AirContactGroup group = pathList.get(position);
				if (group != null)
				{
					refreshUserList(group.getGroupId());
					refreshPathList(false);
				}
				break;
			}
			case R.id.user_all_list:
			{
				Object item = adapterUserList.getItem(position - 1);
				if (item != null)
				{
					if (item instanceof AirContactGroup)
					{
						AirContactGroup g = (AirContactGroup) item;
						if (g.getChildCount() > 0)
						{
							refreshUserList(g.getGroupId());
						}
					}
					else if (item instanceof AirContact)
					{
						AirContact c = (AirContact) item;
						if (c != null && !TextUtils.equals(c.getIpocId(), AirtalkeeAccount.getInstance().getUserId()))
						{
							selectPanelUpdate(c);
						}
					}
				}
				break;
			}
			case R.id.user_all_list_search:
			{
				Util.hideSoftInput(this);
				Object item = adapterUserListSearch.getItem(position - 1);
				if (item != null)
				{
					AirContact c = (AirContact) item;
					if (c != null && !TextUtils.equals(c.getIpocId(), AirtalkeeAccount.getInstance().getUserId()))
					{
						selectPanelUpdate(c);
					}
				}
				break;
			}
		}
	}
	
	private List<AirContactTiny> getContactTinys()
	{
		 List<AirContactTiny> tinys = new  ArrayList<AirContactTiny>();
		 
		 for(int i=0;i<selectedContactsIndexing.size();i++)
		 {
				AirContact contact = selectedContactsIndexing.get(i);
			 	AirContactTiny tiny = new AirContactTiny();
				tiny.setIpocId( contact.getIpocId());
				tiny.setiPhoneNumber(contact.getiPhoneNumber());
				tiny.setDisplayName( contact.getDisplayName());
				tiny.setPhotoId(contact.getPhotoId());
				tiny.setSex(contact.getSex());
				tiny.setType(contact.getType());
				tinys.add(tiny);
		 }
		 
		 return tinys;
	}
	
	private void selectPanelUpdate(AirContact contact)
	{
		if (contact != null)
		{
			AirContact select = selectedContacts.get(contact.getIpocId());
			if (select == null)
			{
				if (selectedContacts.size() < SessionController.SESSION_MEMBER_MAX)
				{
					selectedContacts.put(contact.getIpocId(), contact);
					selectedContactsIndexing.add(contact);

					final AirContact con = contact;
					View v = View.inflate(this, R.layout.listitem_member_check, null);
					TextView text = (TextView) v.findViewById(R.id.talk_tv_group_member);
					text.setText(contact.getDisplayName());
					text.setOnClickListener(new OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							// TODO Auto-generated method stub
							AirContact contact = selectedContacts.get(con.getIpocId());
							selectPanelUpdate(contact);
						}
					});
					selectPanel.addView(v);
					selectPanel.computeScroll();
					selectPanelSrcoll.smoothScrollTo(selectPanel.getMeasuredWidth(), 0);
				}
				else
				{
					Util.Toast(this, getString(R.string.talk_tip_session_members_max));
				}
			}
			else
			{
				int idx = -1;
				selectedContacts.remove(contact.getIpocId());
				for (int i = 0; i < selectedContactsIndexing.size(); i++)
				{
					if (TextUtils.equals(contact.getIpocId(), selectedContactsIndexing.get(i).getIpocId()))
					{
						idx = i;
						break;
					}
				}
				if (idx != -1)
				{
					selectedContactsIndexing.remove(idx);
					selectPanel.removeViewAt(idx);
				}
			}

			if (selectedContacts.size() > 0)
			{
				selectLayout.setVisibility(View.VISIBLE);
				selectPanelCount.setText(selectedContacts.size() + "");
			}
			else
			{
				selectLayout.setVisibility(View.GONE);
			}
			adapterUserList.notifyDataSetChanged();
			adapterUserListSearch.notifyDataSetChanged();
		}
	}

	private void selectPanelClean()
	{
		selectedContacts.clear();
		selectedContactsIndexing.clear();
		selectPanel.removeAllViews();
		adapterUserList.notifyDataSetChanged();
		selectLayout.setVisibility(View.GONE);
	}

	private void refreshPathList()
	{
		if (isShowingPathList)
		{
			userAllPathPoint.setImageResource(R.drawable.point_item);
			userAllPathList.setVisibility(View.GONE);
			isShowingPathList = false;
		}
		else
		{
			adapterPathList.notfiyPathList(pathList);
			userAllPathPoint.setImageResource(R.drawable.point_item_up);
			userAllPathList.setVisibility(View.VISIBLE);
			isShowingPathList = true;
		}
	}

	private void refreshPathList(boolean toShow)
	{
		if (!toShow)
		{
			userAllPathPoint.setImageResource(R.drawable.point_item);
			userAllPathList.setVisibility(View.GONE);
			isShowingPathList = false;
		}
		else
		{
			adapterPathList.notfiyPathList(pathList);
			userAllPathPoint.setImageResource(R.drawable.point_item_up);
			userAllPathList.setVisibility(View.VISIBLE);
			isShowingPathList = true;
		}
	}

	private void refreshPath(AirContactGroup group)
	{
		if (group != null)
		{
			boolean isAdd = true;
			int position = 0;
			for (int i = 0; i < pathList.size(); i++)
			{
				if (group == pathList.get(i))
				{
					isAdd = false;
					position = i;
					break;
				}
			}

			if (group.getGroupId() == 0)
			{
				group.setGroupName(getString(R.string.talk_user_title));
			}

			if (isAdd)
			{
				pathList.add(group);
			}
			else
			{
				while (pathList.size() - 1 > position)
				{
					pathList.remove(pathList.size() - 1);
				}
			}

			if (pathList.size() - 1 <= 0)
			{
				userAllPath.setText(" " + getString(R.string.talk_user_title));
			}
			else
			{
				String dot = "";
				if (position == 0)
					position = pathList.size() - 1;
				for (int i = 0; i < position; i++)
				{
					dot += "~";
				}
				userAllPath.setText(" " + dot + " " + pathList.get(pathList.size() - 1).getGroupName());
			}
		}
	}

	private void refreshUserList(int orgId)
	{
		AirContactGroup group = AirtalkeeUserInfo.getInstance().getUserOrganization(orgId);
		if (group != null)
		{
			refreshPath(group);
			if (group.isLoaded())
			{
				adapterUserList.notifyGroup(group);
				AirtalkeeContactPresence.getInstance().ContactPresenceSubscribe(group.getGroupId(), false);
				gOrgId = group.getGroupId();
			}
			else
			{
				adapterUserList.notifyGroup(null);
				userProgressBar.setVisibility(View.VISIBLE);
				AirtalkeeUserInfo.getInstance().UserInfoGetOrganizationTree(orgId);
			}
		}
	}

	private void doSelectionCall()
	{
		AirSession s = SessionController.SessionMatch(selectedContactsIndexing);
		AirServices.getInstance().switchToSessionTemp(s.getSessionCode(), AirServices.TEMP_SESSION_TYPE_OUTGOING, this);
	}

	private void doSelectionMessage()
	{
		AirSession s = SessionController.SessionMatch(selectedContactsIndexing);
		AirServices.getInstance().switchToSessionTemp(s.getSessionCode(), AirServices.TEMP_SESSION_TYPE_MESSAGE, this);
	}

	@Override
	public void onUserOrganizationTree(boolean isOk, AirContactGroup org)
	{
		// TODO Auto-generated method stub
		userProgressBar.setVisibility(View.GONE);
		if (isOk && org != null)
		{
			AirContactGroup group = pathList.get(pathList.size() - 1);
			if (group != null && group.getGroupId() == org.getGroupId())
			{
				adapterUserList.notifyGroup(org);
				AirtalkeeContactPresence.getInstance().ContactPresenceSubscribe(org.getGroupId(), false);
				gOrgId = org.getGroupId();
			}
		}
	}

	@Override
	public void onUserOrganizationTreeSearch(boolean isOk, List<AirContact> contacts)
	{
		// TODO Auto-generated method stub
		userProgressBar.setVisibility(View.GONE);
		if (isOk & contacts != null && contacts.size() > 0)
		{
			adapterUserListSearch.notifyContacts(contacts);
		}
		else
		{
			Util.Toast(this, getString(R.string.talk_user_search_result_empty));
		}
	}

	@Override
	public void onUserIdGetByPhoneNum(int result, AirContact contact)
	{
	// TODO Auto-generated method stub

	}

	@Override
	public void onUserInfoGet(AirContact user)
	{
	// TODO Auto-generated method stub

	}

	@Override
	public void onUserInfoUpdate(boolean isOk, AirContact user)
	{
	// TODO Auto-generated method stub

	}

	@Override
	public void onScroll(int scrollX)
	{
	// TODO Auto-generated method stub

	}

	@Override
	public void onViewScrollFinished(int currentPage)
	{
		// TODO Auto-generated method stub
		if (currentPage == PAGE_LIST)
		{
			Util.hideSoftInput(this);
		}
	}
	
	@SuppressWarnings("deprecation")
	protected Dialog onCreateDialog(int id)
	{
		if (id == R.id.talk_dialog_waiting)
		{
			ProgressDialog dialog = new ProgressDialog(this);
			dialog.setMessage(getString(R.string.talk_tip_waiting));
			dialog.setCancelable(false);
			return dialog;
		}
		return super.onCreateDialog(id);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onChannelPersonalMemberAdd(int result, AirChannel ch, List<AirContactTiny> failedMembers)
	{
		// TODO Auto-generated method stub
		removeDialog(R.id.talk_dialog_waiting);
		if (result == 0)
		{
			Util.Toast(this, getString(R.string.talk_channel_add_success));
			finish();
		}
		else
		{
			Util.Toast(this, getString(R.string.talk_channel_add_fail));
			finish();
		}
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void onChannelPersonalCreate(int result, AirChannel ch, List<AirContactTiny> failedMembers)
	{
		// TODO Auto-generated method stub
		removeDialog(R.id.talk_dialog_waiting);
		if (result == 0)
		{
			Util.Toast(this, getString(R.string.talk_channel_create_success));
			finish();
			if(ChannelManageActivity.getInstance() != null)
			{
				ChannelManageActivity.getInstance().finish();
			}
		}
		else if (result == 446)
		{
			Util.Toast(this, getString(R.string.talk_channel_create_full));
			finish();
		}
		else
		{
			finish();
			Util.Toast(this, getString(R.string.talk_channel_create_fail));
		}
	}

	@Override
	public void onChannelPersonalDelete(boolean arg0, AirChannel arg1)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onChannelPersonalMemberDel(int arg0, AirChannel arg1, List<AirContactTiny> arg2)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onChannelPersonalRename(int arg0, AirChannel arg1)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onContactPresence(boolean isSubscribed, HashMap<String, Integer> presenceMap)
	{
		// TODO Auto-generated method stub
		adapterUserList.notifyDataSetChanged();
		adapterUserListSearch.notifyDataSetChanged();
	}

	@Override
	public void onContactPresence(boolean isSubscribed, String uid, int state)
	{
		// TODO Auto-generated method stub
		adapterUserList.notifyDataSetChanged();
		adapterUserListSearch.notifyDataSetChanged();
	}

}
