package com.cmccpoc.adapter;

import java.util.List;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.airtalkee.sdk.entity.AirContact;
import com.cmccpoc.R;
import com.cmccpoc.Util.ThemeUtil;

public class AdapterMemberSimple extends BaseAdapter
{
	private Context context = null;
	private List<AirContact> sessionMembers = null;

	public AdapterMemberSimple(Context _context)
	{
		context = _context;
	}

	public void notifySessionMembers(List<AirContact> list)
	{
		sessionMembers = list;
		notifyDataSetChanged();
	}

	@Override
	public int getCount()
	{
		// TODO Auto-generated method stub
		return (sessionMembers != null) ? sessionMembers.size() : 0;
	}

	@Override
	public Object getItem(int position)
	{
		// TODO Auto-generated method stub
		return (sessionMembers != null) ? sessionMembers.get(position) : null;
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
			convertView = LayoutInflater.from(context).inflate(R.layout.listitem_session_member, null);
			holder = new ViewHolder();
			holder.name = (TextView) convertView.findViewById(R.id.talk_tv_dialog_s_member);
			holder.presence = (ImageView) convertView.findViewById(R.id.talk_iv_presence);
			convertView.setTag(holder);
		}
		else
		{
			holder = (ViewHolder) convertView.getTag();
		}
		AirContact member = (AirContact) getItem(position);
		if (member != null)
		{
			holder.name.setText(member.getDisplayName());
			if (member.getStateInChat() == AirContact.IN_CHAT_STATE_OFFLINE)
			{
				holder.presence.setImageResource(ThemeUtil.getResourceId(R.attr.theme_user_icon_offline, context));
			}
			else
			{
				holder.presence.setImageResource(ThemeUtil.getResourceId(R.attr.theme_user_icon_online, context));
			}
		}
		return convertView;
	}

	class ViewHolder
	{
		TextView name;
		ImageView presence;
	}
}
