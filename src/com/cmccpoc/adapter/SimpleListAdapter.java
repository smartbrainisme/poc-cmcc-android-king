package com.cmccpoc.adapter;

import java.util.ArrayList;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.cmccpoc.R;

@SuppressWarnings("rawtypes")
public class SimpleListAdapter extends BaseAdapter
{
	protected ArrayList m_childList;
	@SuppressWarnings("unused")
	private int[] m_id;
	private Context context;

	
	public SimpleListAdapter(Context context)
	{
		this.context = context;
		m_childList = new ArrayList();
	}

	public void clear()
	{
		m_childList.clear();
	}

	public void setArrayListDatas(ArrayList data)
	{
		m_childList = data;
		notifyDataSetInvalidated();
	}

	public Object[] getAllChild()
	{
		if (m_childList != null)
		{
			return m_childList.toArray();
		}
		else
		{
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public void addChild(Object[] child)
	{
		for (Object myChild : child)
		{
			m_childList.add(myChild);
		}
	}

	@SuppressWarnings("unchecked")
	public void addChild(Object child)
	{
		if (child != null)
			m_childList.add(child);
	}

	public int getCount()
	{
		return m_childList == null ? 0 : m_childList.size() - 6;
	}

	public Object getItem(int position)
	{
		if (position > getCount() || position < 0)
			return null;
		return m_childList.get(position);
	}

	public long getItemId(int position)
	{
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent)
	{
		if (convertView == null)
		{
			convertView = LayoutInflater.from(context).inflate(R.layout.activity_setting_layout, null);
		}
		TextView t1 = (TextView) convertView.findViewById(R.id.setting);
		TextView t2 = (TextView) convertView.findViewById(R.id.settting_explain);
		@SuppressWarnings("unused")
		ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
		t1.setText(m_childList.get(position).toString());
		t2.setText("");
		// icon.setImageResource(android.R.drawable.);
		// convertView.setId(m_id[position]);
		return convertView;
	}
}
