package com.airtalkee.activity;

import java.util.LinkedHashMap;
import java.util.List;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import com.airtalkee.R;
import com.airtalkee.Util.Setting;
import com.airtalkee.Util.Util;
import com.airtalkee.adapter.AdapterMenuLeft;
import com.airtalkee.adapter.GroupBean;
import com.airtalkee.config.Config;
import com.airtalkee.control.AirAccountManager;
import com.airtalkee.control.AirMessageTransaction;
import com.airtalkee.control.AirSessionControl;
import com.airtalkee.listener.OnMmiChannelListener;
import com.airtalkee.listener.OnMmiNoticeListener;
import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.AirtalkeeChannel;
import com.airtalkee.sdk.AirtalkeeMessage;
import com.airtalkee.sdk.OnChannelPersonalListener;
import com.airtalkee.sdk.controller.AccountController;
import com.airtalkee.sdk.controller.MessageController;
import com.airtalkee.sdk.entity.AirChannel;
import com.airtalkee.sdk.entity.AirContact;
import com.airtalkee.sdk.entity.AirContactTiny;
import com.airtalkee.sdk.entity.AirSession;
import com.airtalkee.services.AirServices;
import com.airtalkee.widget.ViewControllerManager;
import com.airtalkee.widget.ViewControllerSlideView;

public class MainPageView implements OnMmiChannelListener, OnMmiNoticeListener, OnChannelPersonalListener, OnChildClickListener, OnItemLongClickListener, OnClickListener
{
	private MainActivity contextMain;
	private View convertView;
	private ImageView ivNotice, ivUserIsb;
	private TextView ivUserName, ivUserId;
	public ExpandableListView lvGroup;
	public AdapterMenuLeft adapter;

	public MainPageView(MainActivity context, ViewControllerSlideView sliderView, ViewControllerManager navigation)
	{
		this.contextMain = context;

		convertView = View.inflate(contextMain, R.layout.include_main_page, null);
		lvGroup = (ExpandableListView) findViewWithId(R.id.exp_listview_group);
		sliderView.getLeftMenu().addView(convertView);
		
		ivNotice = (ImageView) findViewWithId(R.id.talk_tv_notice);
		findViewWithId(R.id.talk_tv_session).setOnClickListener(this);
		findViewWithId(R.id.talk_tv_tools).setOnClickListener(this);
		if (Config.funcUserAll)
		{
			findViewWithId(R.id.talk_tv_user_all).setOnClickListener(this);
		}
		else
		{
			findViewWithId(R.id.talk_tv_user_all).setVisibility(View.GONE);
		}
		if (Config.funcBroadcast)
		{
			ivNotice.setOnClickListener(this);
		}
		else
		{
			ivNotice.setVisibility(View.GONE);
		}

		ivUserName = (TextView) findViewWithId(R.id.talk_tv_user_name);
		ivUserId = (TextView) findViewWithId(R.id.talk_tv_user_id);
		ivUserIsb = (ImageView) findViewWithId(R.id.talk_user_isb);

		adapter = new AdapterMenuLeft(this.contextMain);
		lvGroup.setAdapter(adapter);
		lvGroup.setOnChildClickListener(this);
		lvGroup.setOnItemLongClickListener(this);
		lvGroup.expandGroup(0);
		lvGroup.expandGroup(1);
	}

	public void setListener()
	{
		AirtalkeeChannel.getInstance().setOnChannelPersonalListener(this);
		AirAccountManager.getInstance().setChannelListener(this);
		AirMessageTransaction.getInstance().setOnNoticeListener(this);
	}

	private View findViewWithId(int id)
	{
		if (convertView != null)
		{
			return convertView.findViewById(id);
		}
		return null;
	}

	public void refreshList()
	{
		adapter.notifyDataSetChanged();
	}

	public void refreshNotice()
	{
		if (Config.funcBroadcast && AirtalkeeAccount.getInstance().SystemBroadcastNumberGet() > 0)
		{
			ivNotice.setImageResource(R.drawable.selector_btn_tool_infosys_new);
		}
		else
		{
			ivNotice.setImageResource(R.drawable.selector_btn_tool_infosys);
		}
	}
	
	public void refreshSpeaker(AirSession session)
	{
		if (contextMain.viewControllerSlideView.isShowMenuLeft())
		{
			adapter.notifyDataSetChanged();
		}
	}
	
	public void refreshUser()
	{
		ivUserName.setText(AirtalkeeAccount.getInstance().getUserName());
		ivUserId.setText(AirtalkeeAccount.getInstance().getUserId());
		ivUserIsb.setVisibility(Setting.getPttIsb() ? View.VISIBLE : View.GONE);
	}

	public int countGroupNewMsg()
	{
		int count = 0;
		GroupBean group = (GroupBean) adapter.getGroup(AdapterMenuLeft.GROUP_POSITION);
		List<AirChannel> channels = group.channelList;
		for (int i = 0; i < channels.size(); i++)
		{
			AirChannel c = (AirChannel) channels.get(i);
			if (c != null)
			{
				if (c.getMsgUnReadCount() > 0)
					count++;
			}
		}
		return count;
	}

	public int countSessionNewMsg()
	{
		return MessageController.checkUnReadMessage();
	}

	@Override
	public void onClick(View v)
	{
		// TODO Auto-generated method stub
		switch (v.getId())
		{
			case R.id.talk_tv_user_all:
			{
				Intent it = new Intent(contextMain, UserAllActivity.class);
				contextMain.startActivity(it);
				break;
			}
			case R.id.talk_tv_notice:
			{
				Intent it = new Intent(contextMain, MenuNoticeActivity.class);
				it.putExtra("url", AccountController.getDmWebNoticeUrl());
				contextMain.startActivity(it);
				break;
			}
			case R.id.talk_tv_tools:
			{
				Intent it = new Intent(contextMain, MoreActivity.class);
				contextMain.startActivity(it);
				break;
			}
			case R.id.talk_tv_session:
			{
				contextMain.viewControllerSlideView.resetShow();
				contextMain.viewMiddle.refreshSession();
				contextMain.viewMiddle.refreshSessionMember();
				break;
			}
			default:
				break;
		}
	}

	@Override
	public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id)
	{
		// TODO Auto-generated method stub
		Object group = adapter.getChild(groupPosition, childPosition);
		if (group == null)
			return false;
		if(AirSessionControl.getInstance().getCurrentSession() != null && AirSessionControl.getInstance().getCurrentSession().getType() == AirSession.TYPE_DIALOG)
		{
			AirtalkeeMessage.getInstance().MessageCleanBySession(AirSessionControl.getInstance().getCurrentSession());
		}
		if (group instanceof AirSession)
		{
			try
			{
				AirSession session = (AirSession) group;
				if (session != null)
				{
					AirtalkeeMessage.getInstance().MessageRecordPlayStop();
					AirServices.getInstance().switchToSessionTemp(session.getSessionCode(), AirServices.TEMP_SESSION_TYPE_MESSAGE, contextMain);
				}
			}
			catch (Exception e)
			{
				//
			}
		}
		else if (group instanceof AirChannel)
		{
			if (AirtalkeeAccount.getInstance().isEngineRunning())
			{
				AirChannel channel = (AirChannel) group;
				AirSessionControl.getInstance().SessionChannelIn(channel.getId());
				if (contextMain != null)
				{
					contextMain.viewControllerSlideView.resetShow();
					contextMain.viewMiddle.refreshSession();
					contextMain.viewMiddle.refreshSessionMember();
				}
			}
			else
			{
				Util.Toast(contextMain, contextMain.getString(R.string.talk_network_warning));
			}
		}
		return false;
	}
	
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
	{
		// TODO Auto-generated method stub
		int size = AirtalkeeChannel.getInstance().getChannels().size();
		if (position >= 1 && position <= size)
		{
			AirChannel channel = AirtalkeeChannel.getInstance().getChannels().get(position - 1);
			if (AirtalkeeAccount.getInstance().isEngineRunning())
			{
				AirSessionControl.getInstance().SessionChannelIn(channel.getId());
				if (contextMain != null)
				{
					contextMain.viewMiddle.refreshSession();
					contextMain.viewMiddle.refreshSessionMember();
				}
				refreshList();
			}
			else
			{
				Util.Toast(contextMain, contextMain.getString(R.string.talk_network_warning));
			}
		}
		return true;
	}

	@Override
	public void onMmiNoticeNew(int number)
	{
		// TODO Auto-generated method stub
		refreshNotice();
	}

	@Override
	public void onChannelMemberListGet(String channelId, final List<AirContact> members)
	{
		// TODO Auto-generated method stub
		if (members != null && members.size() > 0)
		{
			contextMain.viewMiddle.refreshSessionMember();
		}
	}

	@Override
	public void onChannelOnlineCount(LinkedHashMap<String, Integer> online)
	{
	// TODO Auto-generated method stub

	}

	@Override
	public void onChannelPersonalCreateNotify(AirChannel ch)
	{
	// TODO Auto-generated method stub

	}

	@Override
	public void onChannelPersonalDeleteNotify(AirChannel ch)
	{
	// TODO Auto-generated method stub

	}

	@Override
	public void onChannelPersonalCreate(int result, AirChannel ch, List<AirContactTiny> failedMembers)
	{
	// TODO Auto-generated method stub
	}

	@Override
	public void onChannelPersonalDelete(boolean isOk, AirChannel ch)
	{
		// TODO Auto-generated method stub
		/*
		contextMain.removeDialog(R.id.talk_dialog_waiting);
		String name = ch != null ? ch.getDisplayName() : "";
		if (isOk)
		{
			adapter.notifyDataSetChanged();
		}
		else
			Util.Toast(contextMain, String.format(contextMain.getString(R.string.talk_channel_delete_fail), name));
		*/
	}

	@Override
	public void onChannelPersonalMemberAdd(int result, AirChannel ch, List<AirContactTiny> failedMembers)
	{
	// TODO Auto-generated method stub

	}

	@SuppressWarnings("deprecation")
	@Override
	public void onChannelPersonalMemberDel(int result, AirChannel ch, List<AirContactTiny> failedMembers)
	{
		// TODO Auto-generated method stub
		contextMain.removeDialog(R.id.talk_dialog_waiting);
		if (result == 0)
		{
			contextMain.viewMiddle.sessionBox.sessionBoxMember.refreshMembers();
			contextMain.viewMiddle.sessionBox.sessionBoxMember.refreshMemberOnline();
		}
		else
		{
			Util.Toast(contextMain, contextMain.getString(R.string.talk_member_del_fail));
		}
	}

	@Override
	public void onChannelPersonalRename(int result, AirChannel ch)
	{
	// TODO Auto-generated method stub
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onChannelListGet(boolean isOk, List<AirChannel> channels)
	{
		// TODO Auto-generated method stub
		contextMain.removeDialog(R.id.talk_dialog_group_get_wait);
		if (isOk)
		{
			adapter.notifyDataSetChanged();
		}
		else
		{
			Util.Toast(contextMain, contextMain.getString(R.string.talk_channel_list_getfail));
		}
	}

}
