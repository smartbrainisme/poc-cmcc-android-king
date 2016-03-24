package com.cmccpoc.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;
import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.AirtalkeeContactPresence;
import com.airtalkee.sdk.OnContactPresenceListener;
import com.airtalkee.sdk.entity.AirContact;
import com.airtalkee.sdk.entity.AirContactTiny;
import com.airtalkee.sdk.entity.AirSession;
import com.cmccpoc.R;
import com.cmccpoc.Util.ThemeUtil;
import com.cmccpoc.config.Config;

/**
 * 成员用户适配器
 * @author Yao
 */
@SuppressLint("UseSparseArrays")
public class AdapterMember extends BaseAdapter implements OnContactPresenceListener
{
	private Context context = null;
	private AirSession session = null;
	private List<AirContact> memberList = null;
	private HashMap<Integer, AirContact> isSelected;
	private View vMemberBottom = null;
	private boolean allowCheck = false;
	private boolean allowRole = false;
	private View layoutBtns; 
	public AdapterMember(Context _context, View v,View v2, boolean allowCheck, boolean allowRole)
	{
		context = _context;
		vMemberBottom = v;
		this.layoutBtns = v2;
		this.allowCheck = allowCheck;
		this.allowRole = allowRole;
	}

	/**
	 * 设置被选中的成员列表
	 * @param key 键值
	 * @param value 成员Entity
	 * @param isCheck 是否选中
	 */
	private void putSelected(Integer key, AirContact value, boolean isCheck)
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

	/**
	 * 获取被选中的成员列表
	 */
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

	/**
	 * 获取被选中的成员列表
	 */
	public List<AirContactTiny> getSelectedMemberListTiny()
	{
		List<AirContactTiny> selectList = new ArrayList<AirContactTiny>();
		if (isSelected != null)
		{
			@SuppressWarnings("rawtypes")
			Iterator iterable = (Iterator) isSelected.values().iterator();
			while (iterable.hasNext())
			{
				AirContact contact = (AirContact) iterable.next();
				AirContactTiny contactTiny = new AirContactTiny();
				contactTiny.setIpocId(contact.getIpocId());
				contactTiny.setDisplayName(contact.getDisplayName());
				selectList.add(contactTiny);
			}
		}
		return selectList;
	}

	/**
	 * 刷新member列表与选中状态
	 * @param _session 会话Entity
	 * @param _memberList 
	 */
	public void notifyMember(AirSession _session, List<AirContact> _memberList)
	{
		session = _session;
		memberList = _memberList;
		resetCheckBox();
	}

	/**
	 * 重置选中状态
	 */
	public void resetCheckBox()
	{
		isSelected = new HashMap<Integer, AirContact>();
		isSelected.clear();
		notifyDataSetChanged();
	}

	@Override
	public int getCount()
	{
		// TODO Auto-generated method stub
		return (memberList != null) ? memberList.size() : 0;
	}

	@Override
	public Object getItem(int position)
	{
		// TODO Auto-generated method stub
		AirContact ct = null;
		try
		{
			ct = (memberList != null) ? memberList.get(position) : null;
		}
		catch (Exception e)
		{
		}
		return ct;
	}

	@Override
	public long getItemId(int position)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent)
	{
		// TODO Auto-generated method stub
		ViewHolder holder = null;
		// Log.e(AdapterMember.class, "AdapterMember getView");
		if (convertView == null)
		{
			convertView = LayoutInflater.from(context).inflate(R.layout.listitem_member, null);
			holder = new ViewHolder();
			holder.checkBox = (CheckBox) convertView.findViewById(R.id.talk_cb_group_member);
			holder.tvName = (TextView) convertView.findViewById(R.id.talk_tv_group_member);
			holder.tvUid = (TextView) convertView.findViewById(R.id.talk_tv_group_member_id);
			holder.tvPresence = (TextView) convertView.findViewById(R.id.talk_tv_group_presence);
			holder.ivPresence = (ImageView) convertView.findViewById(R.id.talk_iv_group_presence);
			holder.ivSPresence = (ImageView) convertView.findViewById(R.id.talk_iv_presence);
			holder.ivRole = (ImageView) convertView.findViewById(R.id.talk_iv_group_role);
			convertView.setTag(holder);
		}
		else
		{
			holder = (ViewHolder) convertView.getTag();
		}
		final AirContact member = (AirContact) getItem(position);
		if (member != null)
		{
			holder.tvUid.setText(member.getIpocId());
			holder.tvName.setText(member.getDisplayName());
			if (TextUtils.equals(AirtalkeeAccount.getInstance().getUserId(), member.getIpocId()))
			{
				holder.tvPresence.setText(R.string.talk_presence_channel_online);
				holder.ivPresence.setImageResource(R.drawable.user_state_chat);
				holder.ivSPresence.setImageResource(ThemeUtil.getResourceId(R.attr.theme_user_icon_online, context));
			}
			else
			{
				if (member.getStateInChat() == AirContact.IN_CHAT_STATE_ONLINE)
				{
					holder.tvPresence.setText(R.string.talk_presence_channel_online);
					holder.ivPresence.setImageResource(R.drawable.user_state_chat);
					holder.ivSPresence.setImageResource(ThemeUtil.getResourceId(R.attr.theme_user_icon_online, context));
				}
				else
				{
					int state = AirtalkeeContactPresence.getInstance().getContactStateById(member.getIpocId());
					switch (state)
					{
						case AirContact.CONTACT_STATE_NONE:
							holder.ivSPresence.setImageResource(ThemeUtil.getResourceId(R.attr.theme_user_icon_offline, context));
							holder.tvPresence.setText(R.string.talk_presence_offline);
							holder.ivPresence.setImageResource(R.drawable.user_state_offline);
							break;
						case AirContact.CONTACT_STATE_ONLINE:
							holder.ivSPresence.setImageResource(ThemeUtil.getResourceId(R.attr.theme_user_icon_online_bg, context));
							holder.tvPresence.setText(R.string.talk_presence_online);
							holder.ivPresence.setImageResource(R.drawable.user_state_online);
							break;
						case AirContact.CONTACT_STATE_ONLINE_BG:
							holder.ivSPresence.setImageResource(ThemeUtil.getResourceId(R.attr.theme_user_icon_online_bg, context));
							holder.tvPresence.setText(R.string.talk_presence_online);
							holder.ivPresence.setImageResource(R.drawable.user_state_online_bg);
							break;
					}
				}
			}

			if (allowRole)
			{
				switch (member.getCusertype())
				{
					case AirContact.CURSETYPE_CREATEER:
						holder.ivRole.setVisibility(View.VISIBLE);
						holder.ivRole.setImageResource(R.drawable.media_role_creater);
						break;
					case AirContact.CURSETYPE_MANAGER:
						holder.ivRole.setVisibility(View.VISIBLE);
						holder.ivRole.setImageResource(R.drawable.media_role2);
						break;
					case AirContact.CURSETYPE_USER:
						holder.ivRole.setVisibility(View.VISIBLE);
						holder.ivRole.setImageResource(R.drawable.media_role1);
						break;
					case AirContact.CURSETYPE_LISTEN_ONLY:
						holder.ivRole.setVisibility(View.VISIBLE);
						holder.ivRole.setImageResource(R.drawable.media_role_listen);
						break;
					default:
						holder.ivRole.setVisibility(View.GONE);
						break;
				}
			}
			else
			{
				holder.ivRole.setVisibility(View.GONE);
			}

			if (!allowCheck)
			{
				holder.ivRole.setVisibility(View.GONE);
			}

			String myIpocId = (AirtalkeeAccount.getInstance() != null) ? AirtalkeeAccount.getInstance().getUserId() : "";
			if (myIpocId.equals(member.getIpocId()))
			{
				holder.tvName.setText(member.getDisplayName());
				holder.checkBox.setClickable(false);
				holder.checkBox.setVisibility(View.INVISIBLE);
				holder.tvPresence.setText(context != null ? context.getString(R.string.talk_presence_channel_online) : "");
				holder.ivPresence.setImageResource(R.drawable.user_state_chat);
			}
			else
			{
				holder.checkBox.setVisibility(View.VISIBLE);
				holder.checkBox.setClickable(true);
			}
			holder.checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener()
			{
				public void onCheckedChanged(CompoundButton arg0, boolean isCheck)
				{
					putSelected(position, member, isCheck);
					if (vMemberBottom != null)
					{
						if (isSelected.size() > 0)
						{
							vMemberBottom.setVisibility(View.VISIBLE);
							layoutBtns.setVisibility(View.GONE);
						}
						else
						{
							vMemberBottom.setVisibility(View.GONE);
							layoutBtns.setVisibility(View.VISIBLE);
						}
					}
				}
			});
			holder.checkBox.setChecked(!(isSelected != null && isSelected.get(position) == null));
			if (!allowCheck)
			{
				holder.checkBox.setVisibility(View.GONE);
			}
		}
		return convertView;
	}

	class ViewHolder
	{
		CheckBox checkBox;
		TextView tvName;
		TextView tvPresence;
		ImageView ivPresence;
		ImageView ivSPresence;
		TextView tvUid;
		ImageView ivRole;
	}


	@Override
	public void onContactPresence(boolean isSubscribed, HashMap<String, Integer> presenceMap)
	{
		// TODO Auto-generated method stub
		if (session != null && session.getType() == AirSession.TYPE_DIALOG)
		{
			session.MembersSort();
		}
		notifyDataSetChanged();
	}

	@Override
	public void onContactPresence(boolean isSubscribed, String uid, int state)
	{
		// TODO Auto-generated method stub
		if (session != null && session.getType() == AirSession.TYPE_DIALOG)
		{
			session.MembersSort();
		}
		notifyDataSetChanged();
	}
}
