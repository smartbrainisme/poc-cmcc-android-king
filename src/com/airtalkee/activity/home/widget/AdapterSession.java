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
import com.airtalkee.sdk.AirtalkeeSessionManager;
import com.airtalkee.sdk.entity.AirSession;

public class AdapterSession extends BaseAdapter implements OnClickListener
{
	int itemHeight;
	Context mContext;
	private boolean isCreateShow = true;

	public AdapterSession(Context mContext, ArrayList<AirSession> data)
	{
		this.mContext = mContext;
	}

	public boolean isCreateShow()
	{
		return isCreateShow;
	}

	public void setCreateShow(boolean isCreateShow)
	{
		this.isCreateShow = isCreateShow;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		HodlerView hodler = null;

		if (convertView == null)
		{
			if (position == 0)
			{
				convertView = LayoutInflater.from(mContext).inflate(R.layout.session_header_item, null);
			}
			else
			{
				convertView = LayoutInflater.from(mContext).inflate(R.layout.session_listitem, null);
			}
			hodler = new HodlerView(convertView);
			convertView.setTag(hodler);
		}
		else
			hodler = (HodlerView) convertView.getTag();
		hodler.fill((AirSession) getItem(position));
		return convertView;
	}

	public class HodlerView
	{
		public TextView tvName;
		public TextView tvCreate;
		public TextView tvCount;
		public LinearLayout delPannel;
		public ImageView ivDel;
		public TextView tvCancel;

		public HodlerView(View baseView)
		{
			tvCreate = (TextView) baseView.findViewById(R.id.tv_create_session);
			tvName = (TextView) baseView.findViewById(R.id.tv_name);
			tvCount = (TextView) baseView.findViewById(R.id.tv_count);
			delPannel = (LinearLayout) baseView.findViewById(R.id.session_del_pannel);
			ivDel = (ImageView) baseView.findViewById(R.id.btn_session_del);
		}

		public void fill(final AirSession item)
		{
			if (item != null)
			{
				tvName.setText(item.getDisplayName());
				tvCount.setText(item.getMemberAll().size() + "");
				if (item.isVisible())
				{
					delPannel.setVisibility(View.GONE);
				}
				else
				{
					delPannel.setVisibility(View.VISIBLE);
				}
				ivDel.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						AirtalkeeSessionManager.getInstance().SessionRemove(item.getSessionCode());
						notifyDataSetChanged();
					}
				});
			}
//			else
//			{
//				if (isCreateShow)
//				{
//					tvCreate.setVisibility(View.VISIBLE);
//				}
//				else
//				{
//					tvCreate.setVisibility(View.GONE);
//				}
//			}
		}
	}

	@Override
	public int getCount()
	{
		// TODO Auto-generated method stub
		return AirtalkeeSessionManager.getInstance().getSessionList().size() + 1;
	}

	@Override
	public Object getItem(int position)
	{
		// TODO Auto-generated method stub
		AirSession ses = null;
		try
		{
			ses = AirtalkeeSessionManager.getInstance().getSessionList().get(position - 1);
		}
		catch (Exception e)
		{}
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
