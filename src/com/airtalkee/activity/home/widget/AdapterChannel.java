package com.airtalkee.activity.home.widget;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.airtalkee.R;
import com.airtalkee.sdk.AirtalkeeChannel;
import com.airtalkee.sdk.entity.AirChannel;

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

		public HodlerView(View baseView)
		{
			tvName = (TextView) baseView.findViewById(R.id.tv_name);
			tvCount = (TextView) baseView.findViewById(R.id.tv_count);
		}

		public void fill(AirChannel item)
		{
			if(item != null)
			{
				tvName.setText(item.getDisplayName());
				tvCount.setText(item.getCount()+"");
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
