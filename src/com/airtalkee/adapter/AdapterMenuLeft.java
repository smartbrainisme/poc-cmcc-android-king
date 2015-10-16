package com.airtalkee.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.Spannable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.airtalkee.R;
import com.airtalkee.Util.ThemeUtil;
import com.airtalkee.Util.Util;
import com.airtalkee.activity.ChannelActivity;
import com.airtalkee.activity.TempSessionManageActivity;
import com.airtalkee.config.Config;
import com.airtalkee.control.AirSessionControl;
import com.airtalkee.sdk.AirtalkeeChannel;
import com.airtalkee.sdk.AirtalkeeUserInfo;
import com.airtalkee.sdk.controller.MessageController;
import com.airtalkee.sdk.controller.SessionController;
import com.airtalkee.sdk.entity.AirChannel;
import com.airtalkee.sdk.entity.AirSession;
import com.airtalkee.sdk.util.Log;
import com.airtalkee.sdk.util.Utils;

public class AdapterMenuLeft extends BaseExpandableListAdapter
{
	public static final int GROUP_POSITION = 0;
	public static final int SESSION_POSITION = 1;
	public String deleteRoomId = "";
	public String deleteRoomName = "";
	Context context = null;
	List<GroupBean> group = null;;
	private int channelUnReadCount = 0;
	private int sessionUnReadCount = 0;

	public AdapterMenuLeft(Context context)
	{
		this.context = context;
		loadData();
		channelUnReadCount = getChannelsMsgCount();
		sessionUnReadCount = MessageController.checkUnReadMessage();
	}

	private void loadData()
	{
		group = new ArrayList<GroupBean>();
		GroupBean sessionBean = new GroupBean();
		sessionBean.displayName = context.getString(R.string.talk_session_title);
		sessionBean.type = GroupBean.TYPE_SESSION;
		sessionBean.sessionList = SessionController.SessionListVisibleGet();

		GroupBean channelBean = new GroupBean();
		channelBean.displayName = context.getString(R.string.talk_group_title);
		channelBean.type = GroupBean.TYPE_CHANNEL;
		channelBean.channelList = AirtalkeeChannel.getInstance().getChannels();
		group.add(channelBean);
		group.add(sessionBean);
	}

	@Override
	public void notifyDataSetChanged()
	{
		// TODO Auto-generated method stub
		channelUnReadCount = getChannelsMsgCount();
		sessionUnReadCount = MessageController.checkUnReadMessage();

		super.notifyDataSetChanged();
	}

	@Override
	public Object getChild(int groupPosition, int childPosition)
	{
		// TODO Auto-generated method stub
		Object obj = null;
		GroupBean group = (GroupBean) getGroup(groupPosition);
		if (group.type == GroupBean.TYPE_CHANNEL)
		{
			if (group.channelList != null)
				obj = group.channelList.get(childPosition);
		}
		else if (group.type == GroupBean.TYPE_SESSION)
		{
			if (group.sessionList != null)
				obj = group.sessionList.get(childPosition);
		}

		return obj;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition)
	{
		// TODO Auto-generated method stub
		return childPosition;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent)
	{
		// TODO Auto-generated method stub
		GroupBean group = (GroupBean) getGroup(groupPosition);
		if (group.type == GroupBean.TYPE_CHANNEL)
		{
			convertView = getChannelView(convertView, groupPosition, childPosition);
		}
		else if (group.type == GroupBean.TYPE_SESSION)
		{
			convertView = getSessionView(convertView, groupPosition, childPosition);
		}

		return convertView;
	}

	public View getSessionView(View convertView, int groupPosition, int childPosition)
	{
		ViewHolderSession holder;
		try
		{
			AirSession session = (AirSession) getChild(groupPosition, childPosition);

			convertView = LayoutInflater.from(context).inflate(R.layout.listitem_sessionlist, null);
			holder = new ViewHolderSession();
			holder.nickName = (TextView) convertView.findViewById(R.id.nick_name);
			holder.currentMessage = (TextView) convertView.findViewById(R.id.currentMessage);
			holder.smsCount = (TextView) convertView.findViewById(R.id.sms_count);
			holder.mic = (ImageView) convertView.findViewById(R.id.mic);
			holder.memberCount = (TextView) convertView.findViewById(R.id.member_count);

			if (session != null)
			{
				Spannable name = null;
				if (context != null && !Utils.isEmpty(session.getDisplayName()))
					name = Util.buildPlainMessageSpannable(context, session.getDisplayName().replaceAll("\r", "").getBytes());
				holder.nickName.setText(name);

				String new_Message = (session.getMessageLast() != null) ? session.getMessageLast().getBody() : convertView.getResources().getString(R.string.main_default_message);
				String time = (session.getMessageLast() != null) ? session.getMessageLast().getTime() : "";
				if (!TextUtils.isEmpty(time))
					new_Message = "[" + time + "] " + new_Message;
				if (context != null)
				{
					Spannable spannable = Util.buildPlainMessageSpannable(context, new_Message.getBytes());
					holder.currentMessage.setText(spannable);
				}

				if (session.getMessageUnreadCount() > 0)
				{
					holder.smsCount.setVisibility(View.VISIBLE);
					holder.smsCount.setText(session.getMessageUnreadCount() + "");
				}
				else
				{
					holder.smsCount.setVisibility(View.GONE);
				}
				if (session.getSessionState() == AirSession.SESSION_STATE_DIALOG)
				{
					holder.mic.setVisibility(View.VISIBLE);
				}
				else
				{
					holder.mic.setVisibility(View.INVISIBLE);
				}
				int num = session.getMemberAll().size();
				holder.memberCount.setText(num > 1 ? "(" + num + ")" : "");
			}

		}
		catch (Exception e)
		{
			Log.e(AdapterMenuLeft.class, "Exception :" + e.toString());
		}
		return convertView;

	}

	public View getChannelView(View convertView, int groupPosition, int childPosition)
	{
		ViewHolderChannel holder = null;
		try
		{
			convertView = LayoutInflater.from(context).inflate(R.layout.listitem_group, null);
			holder = new ViewHolderChannel();
			holder.layout = (LinearLayout) convertView.findViewById(R.id.talk_tv_group_item);
			holder.leftLine = convertView.findViewById(R.id.talk_v_group_selected);
			holder.icon = (ImageView) convertView.findViewById(R.id.talk_iv_group_icon);
			holder.grpId = (TextView) convertView.findViewById(R.id.talk_tv_group_id);
			holder.grpIcon = (ImageView) convertView.findViewById(R.id.talk_iv_group_creater);
			holder.grpLock = (ImageView) convertView.findViewById(R.id.talk_iv_group_lock);
			holder.name = (TextView) convertView.findViewById(R.id.talk_tv_group_name);
			holder.item = convertView.findViewById(R.id.talk_layout_group_item);
			holder.count = (TextView) convertView.findViewById(R.id.tv_group_msg_count);

			final AirChannel channel = (AirChannel) getChild(groupPosition, childPosition);
			if (channel != null)
			{
				if (channel.getSession() != null && channel.getSession().getSessionState() == AirSession.SESSION_STATE_DIALOG)
				{
					holder.leftLine.setBackgroundColor(Color.DKGRAY);
					holder.leftLine.setVisibility(View.VISIBLE);
					holder.icon.setVisibility(View.VISIBLE);
					holder.icon.setOnClickListener(new OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							// TODO Auto-generated method stub
							AirSessionControl.getInstance().SessionChannelOut(channel.getId());
						}
					});
					if (channel.getSession().isVoiceLocked())
						holder.grpLock.setVisibility(View.VISIBLE);
					else
						holder.grpLock.setVisibility(View.GONE);
					if (channel.getSession().getMediaState() == AirSession.MEDIA_STATE_TALK)
						holder.name.setText(R.string.talk_speak_me);
					else
						holder.name.setText(channel.getDisplayName());
				}
				else
				{
					holder.leftLine.setVisibility(View.INVISIBLE);
					holder.icon.setVisibility(View.GONE);
					holder.grpLock.setVisibility(View.GONE);
					holder.name.setText(channel.getDisplayName());
				}
				if (AirSessionControl.getInstance().getCurrentSession() != null && AirSessionControl.getInstance().getCurrentSession().getChannel() == channel)
				{
					holder.layout.setBackgroundResource(ThemeUtil.getResourceId(R.attr.theme_selector_group_item_select, context)/*R.drawable.selector_group_item_select_dark*/);
					holder.layout.setSelected(true);
					holder.leftLine.setBackgroundColor(Color.YELLOW);
					holder.leftLine.setVisibility(View.VISIBLE);
					holder.grpId.setVisibility(View.VISIBLE);
				}
				else
				{
					holder.layout.setBackgroundResource(R.drawable.selector_group_item_dark);
					holder.layout.setSelected(false);
					holder.grpId.setVisibility(View.GONE);
				}
				
				holder.grpId.setText(channel.getId());
				if (channel.getMsgUnReadCount() > 0)
				{
					holder.count.setVisibility(View.VISIBLE);
					holder.count.setText(channel.getMsgUnReadCount() + "");
				}
				else
				{
					holder.count.setVisibility(View.GONE);
					holder.count.setText("");
				}
				if (channel.getLevel() == AirChannel.LEVEL_ALL)
				{
					holder.grpIcon.setBackgroundResource(ThemeUtil.getResourceId(R.attr.theme_group_all,context));
				}
				else
				{	
					int resid = ThemeUtil.getResourceId(isChannelRoot(channel) ? R.attr.theme_group_me : R.attr.theme_group_others, context);
					holder.grpIcon.setBackgroundResource(resid);
				}
			}
		}
		catch (Exception e)
		{
			// TODO: handle exception
		}
		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition)
	{
		// TODO Auto-generated method stub
		GroupBean group = (GroupBean) getGroup(groupPosition);
		int count = 0;
		if (group.type == GroupBean.TYPE_CHANNEL)
		{
			if (group.channelList != null)
				count = group.channelList.size();
		}
		else if (group.type == GroupBean.TYPE_SESSION)
		{
			if (group.sessionList != null)
				count = group.sessionList.size();
		}
		return count;
	}

	@Override
	public Object getGroup(int groupPosition)
	{
		// TODO Auto-generated method stub
		return group.get(groupPosition);
	}

	@Override
	public int getGroupCount()
	{
		// TODO Auto-generated method stub
		return group.size();
	}

	@Override
	public long getGroupId(int groupPosition)
	{
		// TODO Auto-generated method stub
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
	{
		// TODO Auto-generated method stub
		ExpListViewGroupHolder holder = null;
		GroupBean group = (GroupBean) getGroup(groupPosition);
		if (group != null)
		{
			if (convertView == null)
			{
				convertView = LayoutInflater.from(context).inflate(R.layout.exp_listitem_group_left, null);
				holder = new ExpListViewGroupHolder();
				holder.image = (ImageView) convertView.findViewById(R.id.ImageView01);
				holder.title = (TextView) convertView.findViewById(R.id.content_001);
				holder.unRead = (TextView) convertView.findViewById(R.id.content_002);
				holder.setting = (ImageView) convertView.findViewById(R.id.content_003);
				convertView.setTag(holder);
			}
			else
			{
				holder = (ExpListViewGroupHolder) convertView.getTag();
			}
			
			holder.title.setText(group.displayName);

			holder.unRead.setVisibility(View.GONE);
			if (group.type == GroupBean.TYPE_CHANNEL)
			{
				if (channelUnReadCount > 0)
				{
					holder.unRead.setText(channelUnReadCount + "");
					holder.unRead.setVisibility(View.VISIBLE);
				}
			}
			else if (group.type == GroupBean.TYPE_SESSION)
			{
				if (sessionUnReadCount > 0)
				{
					holder.unRead.setText(sessionUnReadCount + "");
					holder.unRead.setVisibility(View.VISIBLE);
				}
			}
			
			if (isExpanded)
				holder.image.setBackgroundResource(R.drawable.col_down);
			else
				holder.image.setBackgroundResource(R.drawable.col_right);
			
			if (group.type == GroupBean.TYPE_CHANNEL)
			{
				if (Config.funcChannelManage)
				{
					holder.setting.setVisibility(View.VISIBLE);
					holder.setting.setOnClickListener(new OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							Intent intent = new Intent();
							intent.setClass(context, ChannelActivity.class);
							context.startActivity(intent);
						}
					});
				}
				else
				{
					holder.setting.setVisibility(View.GONE);
				}
			}
			else if (group.type == GroupBean.TYPE_SESSION)
			{
				holder.setting.setVisibility(View.VISIBLE);
				holder.setting.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						Intent intent = new Intent();
						intent.setClass(context, TempSessionManageActivity.class);
						context.startActivity(intent);
					}
				});
			}
		}
		return convertView;
	}

	private boolean isChannelRoot(AirChannel channel)
	{
		boolean isRoot = false;
		if (channel != null)
		{
			String myid = AirtalkeeUserInfo.getInstance().getUserInfo() != null ? AirtalkeeUserInfo.getInstance().getUserInfo().getIpocId() : null;
			if (myid != null && myid.equals(channel.getCreatorId()))
				isRoot = true;
		}
		return isRoot;
	}

	@Override
	public boolean hasStableIds()
	{
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition)
	{
		// TODO Auto-generated method stub
		return true;
	}

	class ViewHolderChannel
	{
		LinearLayout layout;
		ImageView icon;
		TextView grpId;
		ImageView grpIcon;
		ImageView grpLock;
		TextView name;
		TextView count;
		View item;
		View leftLine;
	}

	class ViewHolderSession
	{
		ImageView mic;
		TextView nickName;
		TextView currentMessage;
		TextView smsCount;
		TextView memberCount;
	}

	class ExpListViewGroupHolder
	{
		TextView title;
		TextView unRead;
		ImageView setting;
		ImageView image;
	}

	private int getChannelsMsgCount()
	{
		int count = 0;
		GroupBean group = (GroupBean) getGroup(GROUP_POSITION);
		List<AirChannel> listChannel = group.channelList;
		for (int i = 0; i < listChannel.size(); i++)
		{
			AirChannel c = (AirChannel) listChannel.get(i);
			if (c != null)
			{
				if (c.getMsgUnReadCount() > 0)
				{
					count += c.getMsgUnReadCount();
				}
			}
		}
		return count;
	}

}
