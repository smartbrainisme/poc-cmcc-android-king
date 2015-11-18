package com.airtalkee.activity.home.widget;

import java.util.ArrayList;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.airtalkee.R;
import com.airtalkee.control.AirSessionControl;
import com.airtalkee.sdk.AirtalkeeChannel;
import com.airtalkee.sdk.entity.AirChannel;
import com.airtalkee.sdk.entity.AirSession;

public class AdapterChannel extends BaseAdapter
{
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
		public LinearLayout baseView ;
		public ImageView ivListener;
		public ImageView ivVoiceLocked;
		public HodlerView(View convertView)
		{
			tvName = (TextView) convertView.findViewById(R.id.tv_name);
			tvCount = (TextView) convertView.findViewById(R.id.tv_count);
			baseView = (LinearLayout)convertView.findViewById(R.id.baseview);
			ivListener = (ImageView)convertView.findViewById(R.id.iv_listen);
			ivVoiceLocked = (ImageView)convertView.findViewById(R.id.iv_lock);
		}

		public void fill(AirChannel item)
		{
			AirSession sessionCurrent = AirSessionControl.getInstance().getCurrentChannelSession();
			if(item != null)
			{
				tvName.setText(item.getDisplayName());
				tvCount.setText(item.getCount()+"");
				
				if(sessionCurrent != null && sessionCurrent.getSessionCode().equals(item.getId()))
				{
					baseView.setBackgroundResource(R.drawable.selector_listitem_channel_1);
				}
				else
				{
					baseView.setBackgroundResource(R.drawable.selector_listitem_channel);
				}
				if(item.getSession() != null && item.getSession().getSessionState() == AirSession.SESSION_STATE_DIALOG)
				{
					ivListener.setVisibility(View.VISIBLE);
				}
				else
				{
					ivListener.setVisibility(View.GONE);
				}
				if(item.getSession() != null && item.getSession().isVoiceLocked())
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
		{
		}
		return ch;
	}

	@Override
	public long getItemId(int position)
	{
		// TODO Auto-generated method stub
		return 0;
	}

}
