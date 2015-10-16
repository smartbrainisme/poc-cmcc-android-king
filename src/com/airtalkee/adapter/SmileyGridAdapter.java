package com.airtalkee.adapter;

import android.content.Context;
import android.content.res.TypedArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.airtalkee.R;

public class SmileyGridAdapter extends SimpleListAdapter
{
	private static int[] m_ResEmotionIds;
	private static String[] m_StrEmotionCodeArray;
	private Context context;

	public SmileyGridAdapter(Context context)
	{
		super(context);
		this.context = context;
		m_ResEmotionIds = context.getResources().getIntArray(R.array.smiley_resid_array);
		TypedArray resIdArray = context.getResources().obtainTypedArray(R.array.smiley_resid_array);
		m_ResEmotionIds = new int[resIdArray.length()];
		for (int i = 0; i < m_ResEmotionIds.length; i++)
		{
			m_ResEmotionIds[i] = resIdArray.getResourceId(i, 0);
		}
		m_StrEmotionCodeArray = context.getResources().getStringArray(R.array.smiley_code_array);
		initListDate();
		if (resIdArray != null)
			resIdArray.recycle();
	}

	public View getView(int position, View convertView, ViewGroup parent)
	{
		if (convertView == null)
		{
			convertView = LayoutInflater.from(context).inflate(R.layout.grid_item_smiley, null);
		}
		ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
		icon.setImageResource(m_ResEmotionIds[position]);

		return convertView;
	}

	public void initListDate()
	{
		clear();
		addChild(m_StrEmotionCodeArray);
	}

	public String getSmiley(int position)
	{
		return m_StrEmotionCodeArray[position];
	}
}
