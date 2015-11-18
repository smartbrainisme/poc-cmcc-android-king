package com.airtalkee.activity;

import java.util.List;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import com.airtalkee.R;
import com.airtalkee.Util.Util;
import com.airtalkee.adapter.AdapterPhoneBook;
import com.airtalkee.control.AirSessionControl;
import com.airtalkee.sdk.AirtalkeeChannel;
import com.airtalkee.sdk.OnChannelPersonalListener;
import com.airtalkee.sdk.entity.AirChannel;
import com.airtalkee.sdk.entity.AirContactTiny;
import com.airtalkee.sdk.entity.AirSession;
import com.airtalkee.sdk.util.Utils;

public class ChannelManagePbActivity extends ActivityBase implements OnClickListener, OnItemClickListener, OnChannelPersonalListener, OnScrollListener
{
	public static final int TYPE_CREATE = 0;
	public static final int TYPE_ADD = 1;
	public static final int TYPE_EDIT = 2;
	private int type = TYPE_CREATE;
	private String roomId = "";
	private String roomName = "";
	private AdapterPhoneBook adapterPhoneBook;
	private ListView lvContact;
	private EditText etChannelName;
	private TextView tvOverlay;
	AirtalkeeChannel handleChannel;
	List<AirContactTiny> failedMembers = null;
	boolean visible;
	private static ChannelManagePbActivity instance;

	public static ChannelManagePbActivity getInstance()
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
		handleChannel.setOnChannelPersonalListener(this);
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
	public void finish()
	{
		// TODO Auto-generated method stub
		super.finish();
		//Phonebook.getInstance().recycleAll();
	}

	private void initContentView()
	{
		etChannelName = (EditText) findViewById(R.id.talk_et_channel_name);
		lvContact = (ListView) findViewById(R.id.talk_lv_contact);
		this.tvOverlay = (TextView) View.inflate(this, R.layout.overlay, null);
		TextView tvTip = (TextView) findViewById(R.id.tv_tip);
		Button btnCreate = (Button) findViewById(R.id.talk_btn_channel_create);
		Button btnCanncel = (Button) findViewById(R.id.talk_btn_channel_canncel);
		switch (type)
		{
			case TYPE_ADD:
			{
				adapterPhoneBook = new AdapterPhoneBook(this);
				btnCreate.setText(R.string.talk_channel_complete_add);
				etChannelName.setVisibility(View.GONE);
				break;
			}
			case TYPE_CREATE:
			{
				btnCreate.setText(R.string.talk_channel_complete_create);
				adapterPhoneBook = new AdapterPhoneBook(this);
				break;
			}
			case TYPE_EDIT:
			{
				btnCreate.setText(R.string.talk_channel_complete_edit);
				tvTip.setVisibility(View.GONE);
				etChannelName.setText(roomName);
				break;
			}
		}
		fastSearchText();
		btnCreate.setOnClickListener(this);
		btnCanncel.setOnClickListener(this);
		lvContact.setScrollingCacheEnabled(false);
		lvContact.setOnScrollListener(this);
		lvContact.setOnItemClickListener(this);
		lvContact.setAdapter(adapterPhoneBook);

	}

	public void fastSearchText()
	{
		getWindowManager().addView(
			tvOverlay,
			new WindowManager.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_APPLICATION,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, PixelFormat.TRANSLUCENT));
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
		else if (id == R.id.talk_dialog_send_sms)
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.talk_channel_create_tip);
			String nameStr = "";
			if (failedMembers != null)
			{
				for (int i = 0; i < failedMembers.size(); i++)
				{
					AirContactTiny m = failedMembers.get(i);
					if (m != null && m.getCode() == AirContactTiny.CODE_NOTEXIST)
						nameStr += failedMembers.get(i).getDisplayName() + (i < failedMembers.size() - 1 ? "," : "");
				}

			}
			builder.setMessage(String.format(getString(R.string.talk_channel_send_tip), nameStr));
			builder.setPositiveButton(getString(R.string.talk_channel_btn_send_msg), new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int whichButton)
				{
					SmsManager smsManager = SmsManager.getDefault();
					PendingIntent pi = PendingIntent.getBroadcast(instance, 0, new Intent(), 0);
					// SEND MSG
					if (smsManager != null)
					{
						String messageText = getString(R.string.talk_channel_send_msg);
						for (int i = 0; i < failedMembers.size(); i++)
						{
							AirContactTiny contact = failedMembers.get(i);
							if (contact != null && !Utils.isEmpty(contact.getiPhoneNumber()))
							{
								smsManager.sendTextMessage(contact.getiPhoneNumber(), null, messageText, pi, null);
							}
						}
					}
					finish();
				}
			});

			builder.setNegativeButton(getString(R.string.talk_session_call_cancel), new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int whichButton)
				{
					dialog.cancel();
					finish();
				}
			});
			return builder.create();
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
							handleChannel.PersonalChannelCreate(name, adapterPhoneBook.getSelectedMemberList());
							showDialog(R.id.talk_dialog_waiting);
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
							AirChannel channel = new AirChannel();
							channel.setId(roomId);
							handleChannel.PersonalChannelMemberAdd(channel, adapterPhoneBook.getSelectedMemberList());
							showDialog(R.id.talk_dialog_waiting);
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
			AirContactTiny c = (AirContactTiny) adapterPhoneBook.getItem(position);
			if (c != null)
			{
				cb.setChecked(!cb.isChecked());
			}
		}
	}

	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
	{
		// TODO Auto-generated method stub
		if (visible)
		{
			try
			{
				tvOverlay.setText(adapterPhoneBook.PbListItem.get(firstVisibleItem).substring(0, 1));
				tvOverlay.setVisibility(View.VISIBLE);
			}
			catch (Exception e)
			{
				// TODO: handle exception
			}

		}
	}

	public void onScrollStateChanged(AbsListView view, int scrollState)
	{
		// TODO Auto-generated method stub
		visible = true;
		if (scrollState == ListView.OnScrollListener.SCROLL_STATE_IDLE)
		{
			tvOverlay.setVisibility(View.INVISIBLE);
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
			boolean b = false;
			if (failedMembers != null && failedMembers.size() > 0)
			{
				this.failedMembers = failedMembers;
				for (int i = 0; i < failedMembers.size(); i++)
				{
					AirContactTiny m = failedMembers.get(i);
					if (m != null && m.getCode() == AirContactTiny.CODE_NOTEXIST)
					{
						b = true;
						break;
					}
				}
				if (b)
					showDialog(R.id.talk_dialog_send_sms);
			}
			if (!b)
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
			boolean b = false;
			if (failedMembers != null && failedMembers.size() > 0)
			{
				this.failedMembers = failedMembers;
				for (int i = 0; i < failedMembers.size(); i++)
				{
					AirContactTiny m = failedMembers.get(i);
					if (m != null && m.getCode() == AirContactTiny.CODE_NOTEXIST)
					{
						b = true;
						break;
					}
				}
				if (b)
					showDialog(R.id.talk_dialog_send_sms);
			}
			else if (!b)
				finish();

		}
		else
		{
			finish();
			Util.Toast(this, getString(R.string.talk_channel_add_fail));
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
