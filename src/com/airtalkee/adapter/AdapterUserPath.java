package com.airtalkee.adapter;

import java.util.List;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.airtalkee.R;
import com.airtalkee.sdk.entity.AirContactGroup;

public class AdapterUserPath extends BaseAdapter
{
	private Context context = null;
	private List<AirContactGroup> queueList = null;

	public AdapterUserPath(Context _context)
	{
		context = _context;
	}

	public void notfiyPathList(List<AirContactGroup> list)
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
		AirContactGroup queue = (AirContactGroup) getItem(position);
		if (queue != null)
		{
			String dot = "";
			for (int i = 0; i < position; i++)
			{
				dot += " ~ ";
			}
			holder.name.setText(dot + " " + queue.getGroupName());
		}
		return convertView;
	}

	class ViewHolder
	{
		TextView name;
	}
}
