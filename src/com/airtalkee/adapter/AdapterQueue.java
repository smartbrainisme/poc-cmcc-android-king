package com.airtalkee.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.airtalkee.R;
import com.airtalkee.sdk.entity.AirContact;

public class AdapterQueue extends BaseAdapter
{
	private Context context = null;
	private List<AirContact> queueList = null;

	public AdapterQueue(Context _context)
	{
		context = _context;
	}

	public void notfiyQueue(List<AirContact> list)
	{
		queueList = list;
		notifyDataSetChanged();
	}

	@Override
	public int getCount()
	{
		// TODO Auto-generated method stub
		return (queueList != null) ? queueList.size() : 0;
	}

	@Override
	public Object getItem(int position)
	{
		// TODO Auto-generated method stub
		return (queueList != null) ? queueList.get(position) : null;
	}

	@Override
	public long getItemId(int position)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		// TODO Auto-generated method stub
		ViewHolder holder = null;
		if (convertView == null)
		{
			convertView = LayoutInflater.from(context).inflate(R.layout.listitem_queue, null);
			holder = new ViewHolder();
			holder.name = (TextView) convertView.findViewById(R.id.talk_tv_group_queue_name);
			convertView.setTag(holder);
		}
		else
		{
			holder = (ViewHolder) convertView.getTag();
		}
		AirContact queue = (AirContact) getItem(position);
		if (queue != null)
		{
			holder.name.setText(queue.getDisplayName());
		}
		return convertView;
	}

	class ViewHolder
	{
		TextView name;
	}
}
