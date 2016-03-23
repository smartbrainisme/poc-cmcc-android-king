package com.cmccpoc.adapter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;
import com.airtalkee.sdk.AirtalkeeChannel;
import com.airtalkee.sdk.AirtalkeeUserInfo;
import com.airtalkee.sdk.entity.AirChannel;
import com.cmccpoc.R;
import com.cmccpoc.Util.ThemeUtil;
import com.cmccpoc.Util.Util;
import com.cmccpoc.control.AirSessionControl;


@SuppressLint("UseSparseArrays")
public class AdapterChannelList extends BaseAdapter
{
	private Context context = null;
	
	public AdapterChannelList(Context _context)
	{
		context = _context;
	}

	@Override
	public int getCount()
	{
		// TODO Auto-generated method stub
		return AirtalkeeChannel.getInstance().getChannels().size();
	}

	@Override
	public Object getItem(int position)
	{
		// TODO Auto-generated method stub
		AirChannel ch = null;
		try
		{
			ch = AirtalkeeChannel.getInstance().getChannels().get(position);
		}
		catch (Exception e)
		{
		}
		return ch;
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
			convertView = LayoutInflater.from(context).inflate(R.layout.listitem_channel, null);
			holder = new ViewHolder();
			holder.checkBox = (CheckBox) convertView.findViewById(R.id.talk_channel_check);
			holder.chName = (TextView) convertView.findViewById(R.id.talk_channel_text);
			holder.chIcon = (ImageView) convertView.findViewById(R.id.talk_channel_icon);
			holder.actDel = (ImageView) convertView.findViewById(R.id.talk_channel_del);
			convertView.setTag(holder);
		}
		else
		{
			holder = (ViewHolder) convertView.getTag();
		}
		final AirChannel channel = (AirChannel) getItem(position);
		if (channel != null)
		{
			if (channel.getLevel() == AirChannel.LEVEL_ALL)
			{
				holder.chIcon.setImageResource(ThemeUtil.getResourceId(R.attr.theme_group_all,context));
			}
			else
			{	
				int resid = ThemeUtil.getResourceId(isChannelRoot(channel) ? R.attr.theme_group_me : R.attr.theme_group_others, context);
				holder.chIcon.setImageResource(resid);
			}
			
			holder.chName.setText(channel.getDisplayName());
			
			if (channel.getLevel() == AirChannel.LEVEL_NORMAL)
			{
				holder.actDel.setVisibility(View.VISIBLE);
				holder.actDel.setOnClickListener(new OnClickListener() 
				{
					@Override
					public void onClick(View v)
					{
						AlertDialog.Builder builder = new AlertDialog.Builder(context);
						builder.setMessage(context.getString(R.string.talk_delete_tip));
						builder.setPositiveButton(context.getString(R.string.talk_ok), new DialogInterface.OnClickListener()
						{
							public void onClick(DialogInterface dialog, int whichButton)
							{
								dialog.cancel();
								Util.Toast(context, context.getString(R.string.talk_deleteing));
								AirtalkeeChannel.getInstance().PersonalChannelDelete(channel.getId());
							}
						});
						builder.setNegativeButton(context.getString(R.string.talk_no), new DialogInterface.OnClickListener()
						{
							public void onClick(DialogInterface dialog, int whichButton)
							{
								dialog.cancel();
							}
						});
						builder.show();
					}
				});
			}
			else
			{
				holder.actDel.setVisibility(View.GONE);
			}
			
			holder.checkBox.setChecked(channel.isAttachItem());
			holder.checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener()
			{
				public void onCheckedChanged(CompoundButton arg0, boolean isCheck)
				{
					channel.setAttachItem(isCheck);
					AirSessionControl.getInstance().channelAttachSave();
				}
			});
		}
		return convertView;
	}

	class ViewHolder
	{
		CheckBox checkBox;
		ImageView chIcon;
		TextView chName;
		ImageView actDel;
	}

	private boolean isChannelRoot(AirChannel channel)
	{
		boolean isRoot = false;
		if (channel != null)
		{
			String myid = AirtalkeeUserInfo.getInstance().getUserInfo() != null ? AirtalkeeUserInfo.getInstance().getUserInfo().getIpocId() : null;
			if (myid != null && myid.equals(channel.getCreatorId()))
				isRoot = true;
		}
		return isRoot;
	}
	
}
