package com.airtalkee.activity.home.widget;

import android.content.Context;
import android.text.Spannable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.airtalkee.R;
import com.airtalkee.Util.Util;
import com.airtalkee.sdk.AirtalkeeSessionManager;
import com.airtalkee.sdk.entity.AirSession;

public class AdapterSession extends BaseAdapter
{
	int itemHeight;
	Context mContext;
	private AdapterSession adapterSession;
	private boolean isEditing = false;

	public AdapterSession(Context mContext)
	{
		adapterSession = this;
		this.mContext = mContext;
	}

	public boolean isEditing()
	{
		return isEditing;
	}

	public void setEditing(boolean isEditing)
	{
		this.isEditing = isEditing;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		HodlerView hodler = null;
		if (!isEditing) // 非编辑状态
		{
			/*
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
			{
				if(position==0)
				{
					convertView = LayoutInflater.from(mContext).inflate(R.layout.session_header_item, null);
				}
				hodler = (HodlerView) convertView.getTag();
			}*/
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
			hodler.fill((AirSession) getItem(position));
		}
		else
		{

			if (position == 0)
			{
				convertView = LayoutInflater.from(mContext).inflate(R.layout.session_listitem, null);
				hodler = new HodlerView(convertView);
				convertView.setTag(hodler);
			}
			else
			{
				hodler = (HodlerView) convertView.getTag();
			}
			hodler.fill((AirSession) getItem(position));
		}
		return convertView;
	}

	public class HodlerView
	{
		public View baseView;
		public TextView tvName;
		public TextView tvCreate;
		public TextView tvCount;
		public LinearLayout delPannel;
		public ImageView ivDel;
		public TextView tvCancel;
		public LinearLayout missedPanel;
		public TextView tvMissed;

		public HodlerView(View baseView)
		{
			this.baseView = baseView;
			tvCreate = (TextView) baseView.findViewById(R.id.tv_create_session);
			tvName = (TextView) baseView.findViewById(R.id.tv_name);
			tvCount = (TextView) baseView.findViewById(R.id.tv_count);
			delPannel = (LinearLayout) baseView.findViewById(R.id.session_del_pannel);
			ivDel = (ImageView) baseView.findViewById(R.id.btn_session_del);
			missedPanel = (LinearLayout) baseView.findViewById(R.id.session_missed_panel);
			tvMissed = (TextView) baseView.findViewById(R.id.tv_session_missed);
		}

		public void fill(final AirSession item)
		{
			if (item != null)
			{
				String display = item.getDisplayName();
				tvName.setText(display.toString());
				tvCount.setText(item.getMemberAll().size() + "");
				if (isEditing)
				{
					delPannel.setVisibility(View.VISIBLE);
					ivDel.setOnClickListener(new OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							AirtalkeeSessionManager.getInstance().SessionRemove(item.getSessionCode());
							adapterSession.notifyDataSetChanged();
						}
					});
				}
				else
				{
					delPannel.setVisibility(View.GONE);
				}

				String new_Message = (item.getMessageLast() != null) ? item.getMessageLast().getBody() : baseView.getResources().getString(R.string.main_default_message);

				Spannable spannable = Util.buildPlainMessageSpannable(mContext, new_Message.getBytes());
				if (mContext.getString(R.string.talk_call_state_missed_call).equals(spannable.toString()))
				{
					tvMissed.setText(mContext.getString(R.string.talk_call_state_missed_call_short));
					missedPanel.setVisibility(View.VISIBLE);
				}
				else if (mContext.getString(R.string.talk_call_state_rejected_call).equals(spannable.toString()))
				{
					tvMissed.setText(mContext.getString(R.string.talk_call_state_rejected_call_short));
					missedPanel.setVisibility(View.VISIBLE);
				}
				else
				{
					missedPanel.setVisibility(View.GONE);
				}
			}
		}
	}

	@Override
	public int getCount()
	{
		return isEditing ? AirtalkeeSessionManager.getInstance().getSessionList().size() : AirtalkeeSessionManager.getInstance().getSessionList().size() + 1;
	}

	@Override
	public Object getItem(int position)
	{
		AirSession ses = null;
		try
		{
			ses = isEditing ? AirtalkeeSessionManager.getInstance().getSessionList().get(position) : AirtalkeeSessionManager.getInstance().getSessionList().get(position - 1);
		}
		catch (Exception e)
		{}
		return ses;
	}

	@Override
	public long getItemId(int position)
	{
		return 0;
	}
}
