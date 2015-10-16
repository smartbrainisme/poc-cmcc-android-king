package com.airtalkee.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.airtalkee.R;
import com.airtalkee.Util.ThemeUtil;
import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.AirtalkeeContactPresence;
import com.airtalkee.sdk.controller.ChannelController;
import com.airtalkee.sdk.entity.AirChannel;
import com.airtalkee.sdk.entity.AirContact;
import com.airtalkee.sdk.util.Log;

public class AdapterGroupSelect extends BaseExpandableListAdapter
{
	private List<AirChannel> group = null;
	Context context = null;
	private HashMap<String, AirContact> isSelected;

	public AdapterGroupSelect(Context context)
	{
		this.context = context;
		group = ChannelController.dataChannelsGet();
		if (group != null)
			Log.i(AdapterGroupSelect.class, group.size() + "");
		initCheckBox();
	}

	private void putSelected(String key, AirContact value, boolean isCheck)
	{
		if (isCheck)
		{
			isSelected.put(key, value);
		}
		else if (isSelected.size() > 0)
		{
			isSelected.remove(key);
		}
	}

	public List<AirContact> getSelectedMemberList()
	{
		List<AirContact> selectList = new ArrayList<AirContact>();
		if (isSelected != null)
		{
			@SuppressWarnings("rawtypes")
			Iterator iterable = (Iterator) isSelected.values().iterator();
			while (iterable.hasNext())
			{
				selectList.add((AirContact) iterable.next());
			}
		}
		return selectList;
	}

	public void initCheckBox()
	{
		isSelected = new HashMap<String, AirContact>();
		isSelected.clear();
		notifyDataSetChanged();
	}

	@Override
	public Object getChild(int groupPosition, int childPosition)
	{
		// TODO Auto-generated method stub
		AirContact ct = null;
		List<AirContact> contacts = null;
		AirChannel group = (AirChannel) getGroup(groupPosition);
		if (group != null)
		{
			contacts = group.MembersGet();
			if (contacts != null && contacts.size() > childPosition)
				ct = contacts.get(childPosition);
		}
		return ct;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition)
	{
		// TODO Auto-generated method stub
		return childPosition;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent)
	{
		// TODO Auto-generated method stub
		ExpListViewChildHolder holder = null;
		final AirContact ct = (AirContact) getChild(groupPosition, childPosition);
		if (ct != null)
		{
			if (convertView == null)
			{
				convertView = LayoutInflater.from(context).inflate(R.layout.exp_listitem_child_ipoc_top, null);
				holder = new ExpListViewChildHolder();
				holder.display = (TextView) convertView.findViewById(R.id.nick_name);
				holder.cbItem = (CheckBox) convertView.findViewById(R.id.talk_cb_member);
				holder.ivPresence = (ImageView) convertView.findViewById(R.id.talk_iv_presence);
				holder.divider = convertView.findViewById(R.id.line_divider);
				convertView.setTag(holder);
			}
			else
			{
				holder = (ExpListViewChildHolder) convertView.getTag();
			}
			if (ct != null)
			{
				holder.cbItem.setTag(ct.getIpocId());
				holder.display.setText(ct.getDisplayName());
				int state = AirtalkeeContactPresence.getInstance().getContactStateById(ct.getIpocId());
				switch (state)
				{
					case AirContact.CONTACT_STATE_NONE:
						holder.ivPresence.setImageResource(ThemeUtil.getResourceId(R.attr.theme_user_icon_offline, context));
						break;
					case AirContact.CONTACT_STATE_ONLINE:
						holder.ivPresence.setImageResource(ThemeUtil.getResourceId(R.attr.theme_user_icon_online_bg, context));
						break;
					case AirContact.CONTACT_STATE_ONLINE_BG:
						holder.ivPresence.setImageResource(ThemeUtil.getResourceId(R.attr.theme_user_icon_online_bg, context));
						break;
				}
				String myIpocId = (AirtalkeeAccount.getInstance() != null) ? AirtalkeeAccount.getInstance().getUserId() : "";
				if (TextUtils.equals(myIpocId, ct.getIpocId()))
				{
					holder.ivPresence.setImageResource(ThemeUtil.getResourceId(R.attr.theme_user_icon_online, context));
					holder.cbItem.setVisibility(View.INVISIBLE);
				}
				else
				{
					holder.cbItem.setVisibility(View.VISIBLE);
				}
				holder.divider.setVisibility(isLastChild ? View.INVISIBLE : View.VISIBLE);
				holder.cbItem.setOnCheckedChangeListener(new OnCheckedChangeListener()
				{
					public void onCheckedChanged(CompoundButton arg0, boolean isCheck)
					{
						putSelected(ct.getIpocId(), ct, isCheck);
						/*
						 * if (isSelected.size() > 0)
						 * {
						 * vMemberBottom.setVisibility(View.VISIBLE);
						 * View del =
						 * vMemberBottom.findViewById(R.id.
						 * talk_layout_session_delete
						 * );
						 * if (del != null)
						 * {
						 * if (!MainActivity.getInstance().isChannelRoot())
						 * {
						 * del.setVisibility(View.GONE);
						 * }
						 * else
						 * del.setVisibility(ModelConfig.resgisterEnable ?
						 * View.VISIBLE : View.GONE);
						 * }
						 * if (btnRequest != null)
						 * btnRequest.setVisibility(View.GONE);
						 * }
						 * else
						 * {
						 * vMemberBottom.setVisibility(View.GONE);
						 * if (btnRequest != null)
						 * btnRequest.setVisibility(ModelConfig
						 * .pttButtonVisibility);
						 * }
						 */
					}
				});
				holder.cbItem.setChecked(!(isSelected != null && isSelected.get(ct.getIpocId()) == null));
			}
		}
		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition)
	{
		// TODO Auto-generated method stub
		List<AirContact> contacts = null;
		AirChannel group = (AirChannel) getGroup(groupPosition);
		if (group != null)
			contacts = group.MembersGet();
		return contacts != null ? contacts.size() : 0;
	}

	@Override
	public Object getGroup(int groupPosition)
	{
		// TODO Auto-generated method stub
		AirChannel ctList = null;
		if (group != null && group.size() > 0)
		{
			if (groupPosition < group.size())
				ctList = group.get(groupPosition);
		}
		return ctList;
	}

	@Override
	public int getGroupCount()
	{
		// TODO Auto-generated method stub
		return (group != null && group.size() > 0) ? group.size() : 0;
	}

	@Override
	public long getGroupId(int groupPosition)
	{
		// TODO Auto-generated method stub
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
	{
		// TODO Auto-generated method stub

		ExpListViewGroupHolder holder = null;
		AirChannel group = (AirChannel) getGroup(groupPosition);
		if (group != null)
		{
			if (convertView == null)
			{
				convertView = LayoutInflater.from(context).inflate(R.layout.exp_listitem_group_ipoc_top, null);
				holder = new ExpListViewGroupHolder();
				holder.image = (ImageView) convertView.findViewById(R.id.ImageView01);
				holder.title = (TextView) convertView.findViewById(R.id.content_001);
				convertView.setTag(holder);
			}
			else
			{
				holder = (ExpListViewGroupHolder) convertView.getTag();
			}
			holder.title.setText(group.getDisplayName());
			if (isExpanded)
				holder.image.setBackgroundResource(R.drawable.col_down);
			else
				holder.image.setBackgroundResource(R.drawable.col_right);
		}
		return convertView;
	}

	@Override
	public boolean hasStableIds()
	{
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition)
	{
		// TODO Auto-generated method stub
		return true;
	}

	class ExpListViewChildHolder
	{
		TextView display;
		ImageView ivPresence;
		CheckBox cbItem;
		View divider;
	}

	class ExpListViewGroupHolder
	{
		TextView title;
		ImageView image;
	}
}
