package com.airtalkee.adapter;

import java.util.HashMap;
import java.util.List;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import com.airtalkee.R;
import com.airtalkee.Util.ThemeUtil;
import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.entity.AirContact;
import com.airtalkee.sdk.util.Utils;

public class AdapterUserListSearch extends BaseAdapter
{
	private Context context = null;
	private List<AirContact> contacts = null;
	private HashMap<String, AirContact> selectedContacts = null;

	public AdapterUserListSearch(Context _context, HashMap<String, AirContact> selectedContact)
	{
		context = _context;
		selectedContacts = selectedContact;
	}

	public void notifyContacts(List<AirContact> contacts)
	{
		this.contacts = contacts;
		notifyDataSetChanged();
	}

	@Override
	public int getCount()
	{
		// TODO Auto-generated method stub
		return (contacts != null) ? contacts.size() : 0;
	}

	@Override
	public Object getItem(int position)
	{
		// TODO Auto-generated method stub
		Object item = null;
		if (contacts != null)
		{
			item = contacts.get(position);
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
			convertView = LayoutInflater.from(context).inflate(R.layout.listitem_member_tree_search, null);
			holder = new ViewHolder();
			holder.icon = (ImageView) convertView.findViewById(R.id.talk_iv_presence);
			holder.name = (TextView) convertView.findViewById(R.id.talk_tv_group_member);
			holder.id = (TextView) convertView.findViewById(R.id.talk_tv_group_member_id);
			holder.cb = (CheckBox) convertView.findViewById(R.id.talk_cb_group_member);
			holder.org = (TextView) convertView.findViewById(R.id.talk_tv_group_member_org);
			convertView.setTag(holder);
		}
		else
		{
			holder = (ViewHolder) convertView.getTag();
		}
		AirContact c = (AirContact) getItem(position);
		if (c != null)
		{
			if (c.getState() == AirContact.CONTACT_STATE_NONE)
			{
				holder.icon.setImageResource(ThemeUtil.getResourceId(R.attr.theme_user_icon_offline, context));
			}
			else
			{
				holder.icon.setImageResource(ThemeUtil.getResourceId(R.attr.theme_user_icon_online_bg, context));
			}
			holder.name.setText(c.getDisplayName());
			if (c.getType() == AirContact.CONTACT_TYPE_STATION)
			{
				holder.name.setTextColor(0xffff0000);
			}
			else
			{
				holder.name.setTextColor(ThemeUtil.getColor(context,R.attr.theme_button_text_color));
			}
			holder.id.setVisibility(View.VISIBLE);
			holder.id.setText(c.getIpocId());
			if (Utils.isEmpty(c.getAddress()))
			{
				holder.org.setText(context.getString(R.string.talk_user_search_org_no));
			}
			else
			{
				holder.org.setText(context.getString(R.string.talk_user_search_org) + c.getAddress());
			}

			if (TextUtils.equals(AirtalkeeAccount.getInstance().getUserId(), c.getIpocId()))
			{
				holder.cb.setVisibility(View.INVISIBLE);
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
		return convertView;
	}

	class ViewHolder
	{
		TextView name;
		ImageView icon;
		TextView id;
		CheckBox cb;
		TextView org;
	}
}
