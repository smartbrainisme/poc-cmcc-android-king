package com.cmccpoc.adapter;

import java.util.HashMap;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.AirtalkeeContactPresence;
import com.airtalkee.sdk.entity.AirContact;
import com.airtalkee.sdk.entity.AirContactGroup;
import com.cmccpoc.R;
import com.cmccpoc.Util.ThemeUtil;

public class AdapterUserList extends BaseAdapter
{
	private Context context = null;
	private AirContactGroup group = null;
	private HashMap<String, AirContact> selectedContacts = null;

	public AdapterUserList(Context _context, HashMap<String, AirContact> selectedContact)
	{
		context = _context;
		selectedContacts = selectedContact;
	}

	public void notifyGroup(AirContactGroup grp)
	{
		group = grp;
		notifyDataSetChanged();
	}

	@Override
	public int getCount()
	{
		// TODO Auto-generated method stub
		return (group != null) ? group.getChildGroups().size() + group.getChildContacts().size() : 0;
	}

	@Override
	public Object getItem(int position)
	{
		// TODO Auto-generated method stub
		Object item = null;
		if (group != null)
		{
			if (position < group.getChildGroups().size())
			{
				item = group.getChildGroups().get(position);
			}
			else if (position >= group.getChildGroups().size() && position < getCount())
			{
				item = group.getChildContacts().get(position - group.getChildGroups().size());
			}
		}
		return item;
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
			convertView = LayoutInflater.from(context).inflate(R.layout.listitem_member_tree, null);
			holder = new ViewHolder();
			holder.icon = (ImageView) convertView.findViewById(R.id.talk_iv_presence);
			holder.name = (TextView) convertView.findViewById(R.id.talk_tv_group_member);
			holder.id = (TextView) convertView.findViewById(R.id.talk_tv_group_member_id);
			holder.cnt = (TextView) convertView.findViewById(R.id.talk_cb_group_member_cnt);
			holder.cb = (CheckBox) convertView.findViewById(R.id.talk_cb_group_member);
			convertView.setTag(holder);
		}
		else
		{
			holder = (ViewHolder) convertView.getTag();
		}
		Object item = getItem(position);
		if (item != null)
		{
			if (item instanceof AirContactGroup)
			{
				AirContactGroup g = (AirContactGroup) item;
				holder.icon.setImageResource(ThemeUtil.getResourceId(R.attr.theme_org_icon, context));
				holder.name.setText(g.getGroupName());
				holder.name.setTextColor(0xffFF9A00);
				holder.id.setVisibility(View.GONE);
				holder.cb.setVisibility(View.GONE);
				holder.cnt.setVisibility(View.VISIBLE);
				holder.cnt.setText(g.getChildCount() + "");
			}
			else if (item instanceof AirContact)
			{
				AirContact c = (AirContact) item;
				int state = AirtalkeeContactPresence.getInstance().getContactStateById(c.getIpocId());
				switch (state)
				{
					case AirContact.CONTACT_STATE_NONE:
						holder.icon.setImageResource(ThemeUtil.getResourceId(R.attr.theme_user_icon_offline, context));
						break;
					default:
						holder.icon.setImageResource(ThemeUtil.getResourceId(R.attr.theme_user_icon_online_bg, context));
						break;
				}
				holder.name.setText(c.getDisplayName());
				if (c.getType() == AirContact.CONTACT_TYPE_STATION)
				{
					holder.name.setTextColor(0xffff0000);
				}
				else
				{
					holder.name.setTextColor(ThemeUtil.getColor(context, R.attr.theme_button_text_color));
				}
				holder.id.setVisibility(View.VISIBLE);
				holder.id.setText(c.getIpocId());
				holder.cnt.setVisibility(View.GONE);

				if (TextUtils.equals(AirtalkeeAccount.getInstance().getUserId(), c.getIpocId()))
				{
					holder.cb.setVisibility(View.INVISIBLE);
					holder.icon.setImageResource(ThemeUtil.getResourceId(R.attr.theme_user_icon_online_bg, context));
				}
				else
				{
					holder.cb.setVisibility(View.VISIBLE);
					AirContact selected = selectedContacts.get(c.getIpocId());
					if (selected != null)
					{
						holder.cb.setChecked(true);
					}
					else
					{
						holder.cb.setChecked(false);
					}
				}
			}
		}
		return convertView;
	}

	class ViewHolder
	{
		TextView name;
		ImageView icon;
		TextView id;
		TextView cnt;
		CheckBox cb;
	}
}
