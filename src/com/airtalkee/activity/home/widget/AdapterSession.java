package com.airtalkee.activity.home.widget;

import java.util.ArrayList;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.airtalkee.R;
import com.airtalkee.sdk.AirtalkeeSessionManager;
import com.airtalkee.sdk.entity.AirChannel;
import com.airtalkee.sdk.entity.AirSession;

public class AdapterSession extends BaseAdapter implements OnClickListener
{
	int itemHeight;
	Context mContext;

	public AdapterSession(Context mContext, ArrayList<AirChannel> data)
	{
		this.mContext = mContext;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		HodlerView hodler = null;
		
		if (convertView == null)
		{
			if(position == 0)
			{
				convertView = LayoutInflater.from(mContext).inflate(R.layout.session_header_item, null);
			}
			else
			{
				convertView = LayoutInflater.from(mContext).inflate(R.layout.channel_listitem, null);
			}
			hodler = new HodlerView(convertView);
			convertView.setTag(hodler);
			// TODO
//			hodler.ivDel.setOnClickListener(new OnClickListener()
//			{
//				
//				@Override
//				public void onClick(View v)
//				{
//					// TODO Auto-generated method stub
//				}
//			});
		}
		else
			hodler = (HodlerView) convertView.getTag();

		hodler.fill((AirSession) getItem(position));
		return convertView;
	}

	class HodlerView
	{
		public TextView tvName;
		public TextView tvCount;
		public ImageView ivDel;

		public HodlerView(View baseView)
		{
			tvName = (TextView) baseView.findViewById(R.id.tv_name);
			tvCount = (TextView) baseView.findViewById(R.id.tv_count);
			ivDel = (ImageView) baseView.findViewById(R.id.btn_session_del);
//			ivDel.setOnClickListener(new OnClickListener()
//			{
//				@Override
//				public void onClick(View v)
//				{
//					// TODO Auto-generated method stub
//				}
//			});
		}

		public void fill(AirSession item)
		{
			if(item != null)
			{
				tvName.setText(item.getDisplayName());
				tvCount.setText(item.getMemberAll().size()+"");
			}
		}

	}

	@Override
	public int getCount()
	{
		// TODO Auto-generated method stub
		return AirtalkeeSessionManager.getInstance().getSessionList().size()+1;
	}

	@Override
	public Object getItem(int position)
	{
		// TODO Auto-generated method stub
		AirSession ses = null;
		try
		{
			ses = AirtalkeeSessionManager.getInstance().getSessionList().get(position-1);
		}
		catch (Exception e)
		{
		}
		return ses;
	}

	@Override
	public long getItemId(int position)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void onClick(View v)
	{
		// TODO Auto-generated method stub
		
	}

}
