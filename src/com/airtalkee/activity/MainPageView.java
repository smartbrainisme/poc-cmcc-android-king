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
import com.airtalkee.Util.Util;
import com.airtalkee.config.Config;
import com.airtalkee.control.AirAccountManager;
import com.airtalkee.control.AirMessageTransaction;
import com.airtalkee.listener.OnMmiChannelListener;
import com.airtalkee.listener.OnMmiNoticeListener;
import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.AirtalkeeChannel;
import com.airtalkee.sdk.OnChannelPersonalListener;
import com.airtalkee.sdk.controller.MessageController;
import com.airtalkee.sdk.entity.AirChannel;
import com.airtalkee.sdk.entity.AirContact;
import com.airtalkee.sdk.entity.AirContactTiny;
import com.airtalkee.widget.ViewControllerManager;
import com.airtalkee.widget.ViewControllerSlideView;

public class MainPageView implements OnMmiChannelListener, OnMmiNoticeListener, OnChannelPersonalListener, OnChildClickListener, OnItemLongClickListener, OnClickListener
{
	private MainActivity contextMain;
	private View convertView;
	private ImageView ivNotice;
	private TextView ivUserName;
	public ExpandableListView lvGroup;

	public MainPageView(MainActivity context, ViewControllerSlideView sliderView, ViewControllerManager navigation)
	{
		this.contextMain = context;

		lvGroup = (ExpandableListView) findViewWithId(R.id.exp_listview_group);
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
	
	
	public void refreshUser()
	{
		ivUserName.setText(AirtalkeeAccount.getInstance().getUserName());
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
			default:
				break;
		}
	}

	@Override
	public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id)
	{
		// TODO Auto-generated method stub
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
		}
		else
		{
			Util.Toast(contextMain, contextMain.getString(R.string.talk_channel_list_getfail));
		}
	}

}
