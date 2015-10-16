package com.airtalkee.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airtalkee.R;
import com.airtalkee.Util.ThemeUtil;
import com.airtalkee.Util.Util;
import com.airtalkee.adapter.AdapterMember;
import com.airtalkee.control.AirSessionControl;
import com.airtalkee.sdk.AirtalkeeChannel;
import com.airtalkee.sdk.OnChannelPersonalListener;
import com.airtalkee.sdk.entity.AirChannel;
import com.airtalkee.sdk.entity.AirContact;
import com.airtalkee.sdk.entity.AirContactTiny;
import com.airtalkee.sdk.entity.AirSession;
import com.airtalkee.sdk.util.Utils;

public class ChannelManageActivity extends ActivityBase implements OnClickListener, OnItemClickListener, OnChannelPersonalListener
{
	public static final int TYPE_CREATE = 0;
	public static final int TYPE_ADD = 1;
	public static final int TYPE_EDIT = 2;
	private int type = TYPE_CREATE;
	private String roomId = "";
	private String roomName = "";
	private AdapterMember adapterMember;
	private ListView lvContact;
	private EditText etChannelName;
	private TextView tvChannelTitle;
	AirtalkeeChannel handleChannel;
	List<AirContactTiny> failedMembers = null;
	boolean visible;
	private static ChannelManageActivity instance;

	public static ChannelManageActivity getInstance()
	{
		return instance;
	}

	@Override
	protected void onCreate(Bundle bundle)
	{
		// TODO Auto-generated method stub
		super.onCreate(bundle);
		instance = this;
		setContentView(R.layout.activity_channel_manage);
		handleChannel = AirtalkeeChannel.getInstance();
		bundle = getIntent().getExtras();
		if (bundle != null)
		{
			type = bundle.getInt("type");
			roomId = bundle.getString("roomId");
			roomName = bundle.getString("roomName");
			initContentView();
		}
		else
		{
			finish();
		}
	}
	
	@Override
	protected void onResume()
	{
		// TODO Auto-generated method stub
		super.onResume();
		handleChannel.setOnChannelPersonalListener(this);
	}

	@Override
	public void finish()
	{
		// TODO Auto-generated method stub
		super.finish();
		// Phonebook.getInstance().recycleAll();
	}

	private void initContentView()
	{
		TextView ivTitle = (TextView) findViewById(R.id.tv_main_title);
		ivTitle.setText(R.string.talk_tools_channel);
		View btnLeft = findViewById(R.id.menu_left_button);
		ImageView ivLeft = (ImageView) findViewById(R.id.bottom_left_icon);
		ivLeft.setImageResource(ThemeUtil.getResourceId(R.attr.theme_ic_topbar_back, this) );
		btnLeft.setOnClickListener(this);

		RelativeLayout ivRightLay = (RelativeLayout) findViewById(R.id.talk_menu_right_button);
		ImageView ivRight = (ImageView) findViewById(R.id.bottom_right_icon);
		ivRight.setVisibility(View.GONE);
		ivRightLay.setVisibility(View.INVISIBLE);

		tvChannelTitle = (TextView) findViewById(R.id.talk_et_channel_title);
		etChannelName = (EditText) findViewById(R.id.talk_et_channel_name);
		lvContact = (ListView) findViewById(R.id.talk_lv_contact);
		TextView tvTip = (TextView) findViewById(R.id.tv_tip);
		Button btnCreate = (Button) findViewById(R.id.talk_btn_channel_create);
		Button btnCanncel = (Button) findViewById(R.id.talk_btn_channel_canncel);
		switch (type)
		{
			case TYPE_ADD:
			{
				List<AirContact> members = null;
				AirChannel ch = AirtalkeeChannel.getInstance().ChannelGetByCode(roomId);
				if (ch != null)
				{
					members = new ArrayList<AirContact>();
					for (int i = 0; i < AirtalkeeChannel.getInstance().ChannelAllMembers().size(); i++)
					{
						AirContact c = ch.MembersFind(AirtalkeeChannel.getInstance().ChannelAllMembers().get(i).getIpocId());
						if (c == null)
						{
							members.add(AirtalkeeChannel.getInstance().ChannelAllMembers().get(i));
						}
					}
				}
				else
				{
					members = AirtalkeeChannel.getInstance().ChannelAllMembers();
				}
				adapterMember = new AdapterMember(this, null,null, true, false);
				adapterMember.notifyMember(null, members);
				btnCreate.setText(R.string.talk_channel_complete_add);
				etChannelName.setVisibility(View.GONE);
				tvChannelTitle.setText(getString(R.string.talk_channel_title_add));
				break;
			}
			case TYPE_CREATE:
			{
				adapterMember = new AdapterMember(this, null,null, true, false);
				adapterMember.notifyMember(null, AirtalkeeChannel.getInstance().ChannelAllMembers());
				btnCreate.setText(R.string.talk_next_step);
				tvChannelTitle.setText(getString(R.string.talk_channel_title_create));
				break;
			}
			case TYPE_EDIT:
			{
				btnCreate.setText(R.string.talk_channel_complete_edit);
				tvTip.setVisibility(View.GONE);
				etChannelName.setText(roomName);
				tvChannelTitle.setText(getString(R.string.talk_channel_title_edit));
				break;
			}
		}
		btnCreate.setOnClickListener(this);
		btnCanncel.setOnClickListener(this);
		lvContact.setScrollingCacheEnabled(false);
		lvContact.setOnItemClickListener(this);
		lvContact.setAdapter(adapterMember);

	}

	@SuppressWarnings("deprecation")
	protected Dialog onCreateDialog(int id)
	{
		if (id == R.id.talk_dialog_waiting)
		{
			ProgressDialog dialog = new ProgressDialog(this);
			dialog.setMessage(getString(R.string.talk_tip_waiting));
			return dialog;
		}
		return super.onCreateDialog(id);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onClick(View v)
	{
		// TODO Auto-generated method stub
		switch (v.getId())
		{
			case R.id.menu_left_button:
			case R.id.bottom_left_icon:
			case R.id.talk_btn_channel_canncel:
				this.finish();
				break;
			case R.id.talk_btn_channel_create:
			{
				switch (type)
				{
					case TYPE_CREATE:
					{
						String name = etChannelName.getText().toString();
						if (!Utils.isEmpty(name))
						{
						/*	if (adapterMember.getSelectedMemberListTiny().size() > 0)
							{
								handleChannel.PersonalChannelCreate(name, adapterMember.getSelectedMemberListTiny());
								showDialog(R.id.talk_dialog_waiting);
							}
							else
							{
								Toast.makeText(this, R.string.talk_channel_no_member_tip, 0).show();
							}*/
							
							Intent it = new Intent(this, UserAllActivity.class);
							it.putExtra("type", UserAllActivity.TYPE_CREATE_ADD);
							it.putExtra("name", name);
							startActivity(it);
						}
						else
						{
							Util.Toast(this, getString(R.string.talk_channel_name_tip));
						}
						break;
					}
					case TYPE_ADD:
						if (!Utils.isEmpty(roomId))
						{
							/*if (adapterMember.getSelectedMemberListTiny().size() > 0)
							{
								AirChannel channel = new AirChannel();
								channel.setId(roomId);
								handleChannel.PersonalChannelMemberAdd(channel, adapterMember.getSelectedMemberListTiny());
								showDialog(R.id.talk_dialog_waiting);
							}
							else
							{
								Toast.makeText(this, R.string.talk_channel_no_member_tip, 0).show();
							}*/
							
							Intent it = new Intent(this, UserAllActivity.class);
							it.putExtra("type", UserAllActivity.TYPE_ADD);
							it.putExtra("name", roomId);
							startActivity(it);
						}
						break;
					case TYPE_EDIT:
						String name = etChannelName.getText().toString();
						if (!Utils.isEmpty(roomId) && !Utils.isEmpty(name))
						{
							handleChannel.PersonalChannelRename(roomId, name);
							showDialog(R.id.talk_dialog_waiting);
						}
						break;
				}
				break;
			}
			default:
				break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	{
		// TODO Auto-generated method stub
		if (parent.getId() == R.id.talk_lv_contact)
		{
			CheckBox cb = (CheckBox) view.findViewById(R.id.talk_cb_group_member);
			cb.setChecked(!cb.isChecked());
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onChannelPersonalCreate(int result, AirChannel ch, List<AirContactTiny> failedMembers)
	{
		// TODO Auto-generated method stub
		removeDialog(R.id.talk_dialog_waiting);
		if (result == 0)
		{
			Util.Toast(this, getString(R.string.talk_channel_create_success));
			finish();
		}
		else if (result == 446)
		{
			Util.Toast(this, getString(R.string.talk_channel_create_full));
			finish();
		}
		else
		{
			finish();
			Util.Toast(this, getString(R.string.talk_channel_create_fail));
		}
	}

	@Override
	public void onChannelPersonalDelete(boolean isOk, AirChannel ch)
	{
	// TODO Auto-generated method stub

	}

	@SuppressWarnings("deprecation")
	@Override
	public void onChannelPersonalMemberAdd(int result, AirChannel ch, List<AirContactTiny> failedMembers)
	{
		// TODO Auto-generated method stub
		removeDialog(R.id.talk_dialog_waiting);
		if (result == 0)
		{
			Util.Toast(this, getString(R.string.talk_channel_add_success));
			finish();
		}
		else
		{
			Util.Toast(this, getString(R.string.talk_channel_add_fail));
			finish();
		}
	}

	@Override
	public void onChannelPersonalMemberDel(int result, AirChannel ch, List<AirContactTiny> failedMembers)
	{
	// TODO Auto-generated method stub

	}

	@SuppressWarnings("deprecation")
	@Override
	public void onChannelPersonalRename(int result, AirChannel ch)
	{
		// TODO Auto-generated method stub
		removeDialog(R.id.talk_dialog_waiting);
		if (result == 0)
		{
			AirSession s = AirSessionControl.getInstance().getCurrentSession();
			if (s != null && ch != null && s.getChannel() == ch)
			{
				s.setDisplayName(ch.getDisplayName());
			}
			Util.Toast(this, getString(R.string.talk_channel_editname_success));
			finish();
		}
		else
		{
			Util.Toast(this, getString(R.string.talk_channel_editname_fail));
		}
	}

}
