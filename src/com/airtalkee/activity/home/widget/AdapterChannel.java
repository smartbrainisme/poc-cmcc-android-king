package com.airtalkee.activity.home.widget;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.airtalkee.R;
import com.airtalkee.control.AirSessionControl;
import com.airtalkee.sdk.AirtalkeeChannel;
import com.airtalkee.sdk.AirtalkeeContactPresence;
import com.airtalkee.sdk.entity.AirChannel;
import com.airtalkee.sdk.entity.AirContact;
import com.airtalkee.sdk.entity.AirSession;

public class AdapterChannel extends BaseAdapter
{
	public static final int GROUP_POSITION = 0;
	// int itemHeight;
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

		public void fill(final AirChannel item)
		{
			AirSession currentChannel = AirSessionControl.getInstance().getCurrentChannelSession();
			if (item != null)
			{
				tvName.setText(item.getDisplayName());
				if (currentChannel != null && currentChannel.getSessionCode().equals(item.getId()))
				{
					baseView.setBackgroundResource(R.drawable.selector_listitem_channel_1);
					ivListener.setBackgroundResource(R.drawable.ic_listen_high);
					ivListener.setOnClickListener(new OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							AirSessionControl.getInstance().SessionChannelOut(item.getId());
							notifyDataSetChanged();
						}
					});
					tvUnread.setText(currentChannel.getMessageUnreadCount() + "");
				}
				else
				{
					baseView.setBackgroundResource(R.drawable.selector_listitem_channel);
					ivListener.setBackgroundResource(R.drawable.ic_listen);
				}
				
				if (item.getSession() != null && item.getSession().getSessionState() == AirSession.SESSION_STATE_DIALOG)
				{
					int onlineNumber = 1;
					List<AirContact> members = item.MembersGet();
					if(members != null && members.size() > 0)
					{
						for (AirContact member : members)
						{
							int state = AirtalkeeContactPresence.getInstance().getContactStateById(member.getIpocId());
							if(state != AirContact.CONTACT_STATE_NONE)
							{
								onlineNumber++;
							}
						}
					}
					tvCount.setText(onlineNumber + "/" + item.getCount());
					ivListener.setVisibility(View.VISIBLE);
					if (item.getMsgUnReadCount() > 0)
					{
						tvUnread.setVisibility(View.VISIBLE);
						tvUnread.setText(item.getMsgUnReadCount() + "");
					}
					else
						tvUnread.setVisibility(View.GONE);
				}
				else
				{
					tvCount.setText(item.getCount() + "");
					ivListener.setVisibility(View.GONE);
					tvUnread.setVisibility(View.GONE);
				}
				if (item.getSession() != null && item.getSession().isVoiceLocked())
				{
					ivVoiceLocked.setVisibility(View.VISIBLE);
				}
				else
				{
					ivVoiceLocked.setVisibility(View.GONE);
				}
				
				AirSession currentSession = AirSessionControl.getInstance().getCurrentSession();
				if (currentSession != null)
				{
					if (currentSession.getSessionCode().equals(item.getSession().getSessionCode()))
					{
						baseView.setBackgroundResource(R.drawable.selector_listitem_channel_1);
						ivListener.setBackgroundResource(R.drawable.ic_listen_high);
					}
					else
					{
						baseView.setBackgroundResource(R.drawable.selector_listitem_channel);
						ivListener.setBackgroundResource(R.drawable.ic_listen);
					}
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
