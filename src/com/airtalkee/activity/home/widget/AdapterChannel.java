package com.airtalkee.activity.home.widget;

import java.util.ArrayList;
import java.util.List;
import android.R.integer;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.airtalkee.R;
import com.airtalkee.R.string;
import com.airtalkee.adapter.GroupBean;
import com.airtalkee.control.AirSessionControl;
import com.airtalkee.sdk.AirtalkeeChannel;
import com.airtalkee.sdk.AirtalkeeContactPresence;
import com.airtalkee.sdk.entity.AirChannel;
import com.airtalkee.sdk.entity.AirContact;
import com.airtalkee.sdk.entity.AirSession;

public class AdapterChannel extends BaseAdapter
{
	public static final int GROUP_POSITION = 0;
	int itemHeight;
	Context mContext;

	public AdapterChannel(Context mContext, ArrayList<AirChannel> data)
	{
		this.mContext = mContext;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		HodlerView hodler = null;
		if (convertView == null)
		{
			convertView = LayoutInflater.from(mContext).inflate(R.layout.channel_listitem, null);
			hodler = new HodlerView(convertView);
			convertView.setTag(hodler);
		}
		else
			hodler = (HodlerView) convertView.getTag();

		hodler.fill((AirChannel) getItem(position));
		return convertView;
	}

	class HodlerView
	{
		public TextView tvName;
		public TextView tvCount;
		public LinearLayout baseView;
		public ImageView ivListener;
		public ImageView ivVoiceLocked;
		public TextView tvUnread;

		public HodlerView(View convertView)
		{
			tvName = (TextView) convertView.findViewById(R.id.tv_name);
			tvCount = (TextView) convertView.findViewById(R.id.tv_count);
			baseView = (LinearLayout) convertView.findViewById(R.id.baseview);
			ivListener = (ImageView) convertView.findViewById(R.id.iv_listen);
			ivVoiceLocked = (ImageView) convertView.findViewById(R.id.iv_lock);
			tvUnread = (TextView) convertView.findViewById(R.id.tv_unread_count);
		}

		public void fill(AirChannel item)
		{
			AirSession sessionCurrent = AirSessionControl.getInstance().getCurrentChannelSession();
			if (item != null)
			{
				tvName.setText(item.getDisplayName());
				if (sessionCurrent != null && sessionCurrent.getSessionCode().equals(item.getId()))
				{
					baseView.setBackgroundResource(R.drawable.selector_listitem_channel_1);
					ivListener.setBackgroundResource(R.drawable.ic_listen_high);
					tvUnread.setText(sessionCurrent.getMessageUnreadCount() + "");
					if (sessionCurrent.getMessageUnreadCount() > 0)
						tvUnread.setVisibility(View.VISIBLE);
					else
						tvUnread.setVisibility(View.GONE);
					List<AirContact> members = item.MembersGet();
					if (null != members && members.size() > 0)
					{
						int online = 0;
						final AirtalkeeContactPresence contactPresence = AirtalkeeContactPresence.getInstance();
						for (AirContact member : members)
						{
							if (contactPresence.getContactStateById(member.getIpocId()) != AirContact.CONTACT_STATE_NONE)
							{
								online += 1;
							}
						}
						item.setOnlineNumber(online + 1);
					}
					tvCount.setText(item.getOnlineNumber() + "/" + item.getCount());
				}
				else
				{
					baseView.setBackgroundResource(R.drawable.selector_listitem_channel);
					ivListener.setBackgroundResource(R.drawable.ic_listen);
					tvUnread.setVisibility(View.GONE);
				}
				if (item.getSession() != null && item.getSession().getSessionState() == AirSession.SESSION_STATE_DIALOG)
				{
					ivListener.setVisibility(View.VISIBLE);
				}
				else
				{
					ivListener.setVisibility(View.GONE);
				}
				if (item.getSession() != null && item.getSession().isVoiceLocked())
				{
					ivVoiceLocked.setVisibility(View.VISIBLE);
				}
				else
				{
					ivVoiceLocked.setVisibility(View.GONE);
				}
			}
		}
	}

	@Override
	public int getCount()
	{
		// TODO Auto-generated method stub
		return AirtalkeeChannel.getInstance().getChannels().size();
	}

	@Override
	public Object getItem(int position)
	{
		// TODO Auto-generated method stub
		AirChannel ch = null;
		try
		{
			ch = AirtalkeeChannel.getInstance().getChannels().get(position);
		}
		catch (Exception e)
		{}
		return ch;
	}

	@Override
	public long getItemId(int position)
	{
		// TODO Auto-generated method stub
		return 0;
	}

}
