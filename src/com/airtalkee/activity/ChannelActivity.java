package com.airtalkee.activity;

import java.util.List;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.airtalkee.R;
import com.airtalkee.Util.ThemeUtil;
import com.airtalkee.Util.Util;
import com.airtalkee.adapter.AdapterChannelList;
import com.airtalkee.config.Config;
import com.airtalkee.sdk.AirtalkeeChannel;
import com.airtalkee.sdk.OnChannelPersonalListener;
import com.airtalkee.sdk.entity.AirChannel;
import com.airtalkee.sdk.entity.AirContactTiny;
import com.airtalkee.widget.MListView;

public class ChannelActivity extends ActivityBase implements OnClickListener, OnItemClickListener, OnChannelPersonalListener
{
	
	private AdapterChannelList adapterChannelList;
	private MListView mList;

	@Override
	protected void onCreate(Bundle bundle)
	{
		// TODO Auto-generated method stub
		super.onCreate(bundle);
		setRequestedOrientation(Config.screenOrientation);
		setContentView(R.layout.activity_channel);
		doInitView();
	}

	@Override
	protected void onStart()
	{
		// TODO Auto-generated method stub
		super.onStart();
	}

	private void doInitView()
	{
		TextView ivTitle = (TextView) findViewById(R.id.tv_main_title);
		ivTitle.setText(R.string.talk_tools_channel);
		View btnLeft = findViewById(R.id.menu_left_button);
		ImageView ivLeft = (ImageView) findViewById(R.id.bottom_left_icon);
		ivLeft.setImageResource(ThemeUtil.getResourceId(R.attr.theme_ic_topbar_back, this));
		btnLeft.setOnClickListener(this);

		RelativeLayout ivRightLay = (RelativeLayout) findViewById(R.id.talk_menu_right_button);
		ImageView ivRight = (ImageView) findViewById(R.id.bottom_right_icon);
		ivRight.setImageResource(R.drawable.ic_topbar_ch_add);
		ivRightLay.setOnClickListener(this);
		
		mList = (MListView) findViewById(R.id.channel_list);
		adapterChannelList = new AdapterChannelList(this);
		mList.setAdapter(adapterChannelList);
		mList.setOnItemClickListener(this);
	}

	@Override
	protected void onResume()
	{
		// TODO Auto-generated method stub
		super.onResume();
		AirtalkeeChannel.getInstance().setOnChannelPersonalListener(this);
		adapterChannelList.notifyDataSetChanged();
	}

	@Override
	public void finish()
	{
		// TODO Auto-generated method stub
		super.finish();
	}


	@Override
	public void onClick(View v)
	{
		// TODO Auto-generated method stub
		switch (v.getId())
		{
			case R.id.menu_left_button:
			case R.id.bottom_left_icon:
				finish();
				break;
			case R.id.talk_menu_right_button:
			case R.id.bottom_right_icon:
			{
				Intent intent = new Intent();
				intent.putExtra("type", ChannelManageActivity.TYPE_CREATE);
				intent.setClass(this, ChannelManageActivity.class);
				startActivity(intent);
				break;
			}

		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	{
		switch (parent.getId())
		{
			case R.id.channel_list:
			{
				CheckBox cb = (CheckBox) view.findViewById(R.id.talk_channel_check);
				AirChannel c = (AirChannel) adapterChannelList.getItem(position - 1);
				if (c != null && cb != null && cb.isClickable())
				{
					cb.setChecked(!cb.isChecked());
				}
				break;
			}
		}
	}

	@Override
	public void onChannelPersonalCreate(int result, AirChannel ch, List<AirContactTiny> failedMembers)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onChannelPersonalDelete(boolean isOk, AirChannel ch)
	{
		// TODO Auto-generated method stub
		adapterChannelList.notifyDataSetChanged();
		if (ch != null)
		{
			if (isOk)
			{
				String tip = String.format(getString(R.string.talk_channel_delete_success), ch.getDisplayName());
				Util.Toast(this, tip);
			}
			else
			{
				String tip = String.format(getString(R.string.talk_channel_delete_fail), ch.getDisplayName());
				Util.Toast(this, tip);
			}
		}
	}

	@Override
	public void onChannelPersonalMemberAdd(int result, AirChannel ch, List<AirContactTiny> failedMembers)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onChannelPersonalMemberDel(int result, AirChannel ch, List<AirContactTiny> failedMembers)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onChannelPersonalRename(int result, AirChannel ch)
	{
		// TODO Auto-generated method stub
		
	}


}
