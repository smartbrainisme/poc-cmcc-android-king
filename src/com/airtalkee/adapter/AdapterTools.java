package com.airtalkee.adapter;

import android.content.Context;
import android.content.res.TypedArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import com.airtalkee.R;

public class AdapterTools extends BaseAdapter
{
	public static final int MENU_EMOTION = 0;
	public static final int MENU_CAREMA = 1;
	public static final int MENU_IMAGE = 2;
	
	public static final int MENU_SPEAKER = 0;
	public static final int MENU_RECEIVER = 1;
	public static final int MENU_BLUETOOTH = 2;
	public TypedArray resIds = null;
	Context context;

	public AdapterTools(Context context,int resId)
	{
		this.context = context;
		resIds = context.getResources().obtainTypedArray(resId);
	}

	@Override
	public int getCount()
	{
		// TODO Auto-generated method stub
		return resIds == null ? 0 : resIds.length();
	}

	@Override
	public Object getItem(int position)
	{
		// TODO Auto-generated method stub
		return resIds.getResourceId(position, 0);
	}

	@Override
	public long getItemId(int position)
	{
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		// TODO Auto-generated method stub
		ViewHolder holder = null;
		if (convertView == null)
		{
			convertView = LayoutInflater.from(context).inflate(R.layout.grid_item_popup_tools, null);
			holder = new ViewHolder();
			holder.icon = (ImageView) convertView.findViewById(R.id.icon);
			convertView.setTag(holder);
		}
		else
		{
			holder = (ViewHolder) convertView.getTag();
		}

		int res = resIds.getResourceId(position, 0);
		if (res != 0)
		{
			holder.icon.setImageResource(res);
		}
		return convertView;
	}

	class ViewHolder
	{
		ImageView icon;
	}
}
