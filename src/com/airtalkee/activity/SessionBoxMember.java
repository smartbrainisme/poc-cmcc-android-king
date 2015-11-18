package com.airtalkee.activity;

import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.airtalkee.R;
import com.airtalkee.Util.Util;
import com.airtalkee.adapter.AdapterMember;
import com.airtalkee.adapter.AdapterMemberSimple;
import com.airtalkee.config.Config;
import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.AirtalkeeChannel;
import com.airtalkee.sdk.AirtalkeeMessage;
import com.airtalkee.sdk.AirtalkeeSessionManager;
import com.airtalkee.sdk.controller.SessionController;
import com.airtalkee.sdk.entity.AirChannel;
import com.airtalkee.sdk.entity.AirContact;
import com.airtalkee.sdk.entity.AirFunctionSetting;
import com.airtalkee.sdk.entity.AirSession;
import com.airtalkee.sdk.util.Utils;
import com.airtalkee.services.AirServices;
import com.airtalkee.widget.MListView;

public class SessionBoxMember extends View implements OnClickListener, OnItemClickListener
{
	private Activity contextMain = null;
	private SessionBox sessionBox = null;
	private AirSession session = null;

	private MListView lvMember;
	private MListView lvMemberOnline;
	private View layoutBottom;
	private View layoutIcons, layoutIconsDel;
	private TextView tvMemberCount;
	
	public AdapterMember adapterMember;
	public AdapterMemberSimple adapterMemberOnline = null;
	private List<AirContact> tempCallMembers = null;
	private LinearLayout layoutBtns, layoutBtnCallCenter, layoutBtnAlertChannel;
	View parent;
	
	public SessionBoxMember(Context context, View parentView, SessionBox box)
	{
		super(context);
		// TODO Auto-generated constructor stub
		contextMain = (Activity) context;
		sessionBox = box;
		loadView(parentView);
	}

	private void loadView(View parentView)
	{
		this.parent = parentView;
		
		tvMemberCount = (TextView) parentView.findViewById(R.id.talk_label_member);
		layoutBottom = parentView.findViewById(R.id.talk_layout_session_m_bottom);
		layoutIcons = parentView.findViewById(R.id.talk_layout_meida_icon);
		layoutIconsDel = layoutBottom.findViewById(R.id.talk_layout_session_delete);
		layoutBtns = (LinearLayout) parentView.findViewById(R.id.talk_btn_layout);
		layoutBtnCallCenter = (LinearLayout) parentView.findViewById(R.id.talk_btn_layout_call_center);
		layoutBtnAlertChannel = (LinearLayout) parentView.findViewById(R.id.talk_btn_layout_alert_channel);
		parentView.findViewById(R.id.talk_layout_meida_icon_add).setOnClickListener(this);
		parentView.findViewById(R.id.talk_layout_meida_icon_edit).setOnClickListener(this);
		adapterMember = new AdapterMember(contextMain, layoutBottom, layoutBtns, sessionBox.getSessionType() == AirSession.TYPE_CHANNEL ? true : false, true);
		lvMember = (MListView) parentView.findViewById(R.id.talk_lv_session_member);
		lvMember.setAdapter(adapterMember);
		lvMember.setOnItemClickListener(this);
		if (sessionBox.getSessionType() == AirSession.TYPE_DIALOG)
		{
			layoutBtns.setVisibility(View.GONE);
		}
		else
		{
			if (Config.funcCenterCall != AirFunctionSetting.SETTING_DISABLE)
			{
				layoutBtnCallCenter.setVisibility(View.VISIBLE);
				parentView.findViewById(R.id.talk_btn_call_center).setOnClickListener(this);
			}
			else
			{
				layoutBtnCallCenter.setVisibility(View.GONE);
			}

			if (Config.funcChannelCallIn)
			{
				layoutBtnAlertChannel.setVisibility(View.VISIBLE);
				parentView.findViewById(R.id.talk_lv_session_call).setOnClickListener(this);
			}
			else
			{
				layoutBtnAlertChannel.setVisibility(View.GONE);
			}

			if (layoutBtnCallCenter.getVisibility() == View.GONE && layoutBtnAlertChannel.getVisibility() == View.GONE)
				layoutBtns.setVisibility(View.GONE);
			else
				layoutBtns.setVisibility(View.VISIBLE);
		}

		if (Config.pttButtonVisibility == View.GONE)
		{
			adapterMemberOnline = new AdapterMemberSimple(contextMain);
			lvMemberOnline = (MListView) parentView.findViewById(R.id.talk_lv_session_member_online);
			lvMemberOnline.setAdapter(adapterMemberOnline);
		}

		parentView.findViewById(R.id.talk_btn_session_msg).setOnClickListener(this);
		parentView.findViewById(R.id.talk_btn_session_call).setOnClickListener(this);
		parentView.findViewById(R.id.talk_btn_session_cancel).setOnClickListener(this);
		parentView.findViewById(R.id.talk_btn_session_delete).setOnClickListener(this);
	}

	public void refreshManageButtons(boolean showIcons)
	{
		boolean toShowManageButton = false;
		if (Config.funcChannelManage && session != null && session.getType() == AirSession.TYPE_CHANNEL)
		{
			AirChannel channel = AirtalkeeChannel.getInstance().ChannelGetByCode(session.getSessionCode());
			if (channel != null)
			{
				toShowManageButton = TextUtils.equals(AirtalkeeAccount.getInstance().getUserId(), channel.getCreatorId());
			}
		}

		if (toShowManageButton)
		{
			layoutIconsDel.setVisibility(View.VISIBLE);
			if (showIcons)
			{
				layoutIcons.setVisibility(View.VISIBLE);
			}
			else
			{
				layoutIcons.setVisibility(View.GONE);
			}
		}
		else
		{
			layoutIconsDel.setVisibility(View.GONE);
			layoutIcons.setVisibility(View.GONE);
		}
	}

	public void refreshMembers(AirSession session, List<AirContact> members)
	{
		this.session = session;
		sessionBox.sessionBoxTalk.refreshRole(false);
		adapterMember.notifyMember(session, members);
		adapterMember.notifyDataSetChanged();
		refreshManageButtons(sessionBox.tabIndex() == SessionBox.PAGE_MEMBER);
	}

	public void refreshMembers()
	{
		adapterMember.notifyDataSetChanged();
	}

	public void refreshMemberOnline(List<AirContact> memberOnline)
	{
		if (adapterMemberOnline != null)
		{
			adapterMemberOnline.notifySessionMembers(memberOnline);
		}
		if (session != null)
		{
			tvMemberCount.setText(session.getSessionMemberOnlineCount() == 0 ? "" : session.getSessionMemberOnlineCount() + "");
		}
		else
		{
			tvMemberCount.setText("");
		}
	}

	public void refreshMemberOnline()
	{
		if (adapterMemberOnline != null)
		{
			adapterMemberOnline.notifyDataSetChanged();
		}
		if (session != null)
		{
			tvMemberCount.setText(session.getSessionMemberOnlineCount() == 0 ? "" : session.getSessionMemberOnlineCount() + "");
		}
		else
		{
			tvMemberCount.setText("");
		}
	}

	public List<AirContact> getSelectedMemberList()
	{
		return adapterMember.getSelectedMemberList();
	}

	public void callStationCenter()
	{
		if (Config.funcCenterCall == AirFunctionSetting.SETTING_ENABLE)
		{
			if (AirtalkeeAccount.getInstance().isAccountRunning())
			{
				if (AirtalkeeAccount.getInstance().isEngineRunning())
				{
					AirSession session = SessionController.SessionMatchSpecial(AirtalkeeSessionManager.SPECIAL_NUMBER_DISPATCHER,
						contextMain.getString(R.string.talk_tools_call_center));
					AirServices.getInstance().switchToSessionTemp(session.getSessionCode(), AirServices.TEMP_SESSION_TYPE_OUTGOING, contextMain);
				}
				else
				{
					Util.Toast(contextMain, contextMain.getString(R.string.talk_network_warning));
				}
			}
		}
		else if (Config.funcCenterCall == AirFunctionSetting.SETTING_CALL_NUMBER && !Utils.isEmpty(Config.funcCenterCallNumber))
		{
			Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + Config.funcCenterCallNumber));
			contextMain.startActivity(intent);
		}
	}

	public void callSelectClean()
	{
		adapterMember.resetCheckBox();
		layoutBottom.setVisibility(View.GONE);
		layoutBtns.setVisibility(View.VISIBLE);
	}

	public void callSelectMember(boolean isCall)
	{
		if (adapterMember.getSelectedMemberList() != null && adapterMember.getSelectedMemberList().size() > 0)
		{
			if (tempCallMembers == null)
				tempCallMembers = new ArrayList<AirContact>();
			tempCallMembers.clear();
			for (AirContact c : adapterMember.getSelectedMemberList())
			{
				if (!TextUtils.equals(c.getIpocId(), AirtalkeeAccount.getInstance().getUserId()))
				{
					tempCallMembers.add(c);
				}
			}

			if (tempCallMembers.size() > 0)
			{
				if (AirtalkeeAccount.getInstance().isEngineRunning())
				{
					AirSession s = SessionController.SessionMatch(tempCallMembers);
					AirServices.getInstance().switchToSessionTemp(s.getSessionCode(), isCall ? AirServices.TEMP_SESSION_TYPE_OUTGOING : AirServices.TEMP_SESSION_TYPE_MESSAGE,
						contextMain);
				}
				else
				{
					Util.Toast(contextMain, contextMain.getString(R.string.talk_network_warning));
				}
			}
			else
			{
				Util.Toast(contextMain, contextMain.getString(R.string.talk_tip_session_call));
			}
		}
		else
		{
			Util.Toast(contextMain, contextMain.getString(R.string.talk_tip_session_call));
		}
	}

	public List<AirContact> callSelectMemberGet()
	{
		return tempCallMembers;
	}

	@SuppressWarnings("deprecation")
	public void deleteSelectMember()
	{
		contextMain.showDialog(R.id.talk_dialog_waiting);
		if (session != null && session.getType() == AirSession.TYPE_CHANNEL)
		{
			AirChannel channel = AirtalkeeChannel.getInstance().ChannelGetByCode(session.getSessionCode());
			if (channel != null)
			{
				List<AirContact> members = getSelectedMemberList();
				if (members != null && members.size() > 0)
				{
					AirtalkeeChannel.getInstance().PersonalChannelMemberDel(channel, members);
				}
			}
			adapterMember.resetCheckBox();
			layoutBottom.setVisibility(View.GONE);
			layoutBtns.setVisibility(View.VISIBLE);
		}
	}

	public boolean onKeyEvent(KeyEvent event)
	{
		boolean isHandled = false;

		return isHandled;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onClick(View v)
	{
		if (sessionBox.isMenuShowing())
		{
			sessionBox.resetMenu();
			return;
		}

		// TODO Auto-generated method stub
		switch (v.getId())
		{
			case R.id.talk_btn_call_center:
			{
				callStationCenter();
				break;
			}
			case R.id.talk_btn_session_msg:
			{
				AirtalkeeMessage.getInstance().MessageRecordPlayStop();
				callSelectMember(false);
				callSelectClean();
				break;
			}
			case R.id.talk_btn_session_call:
			{
				AirtalkeeMessage.getInstance().MessageRecordPlayStop();
				callSelectMember(true);
				callSelectClean();
				break;
			}
			case R.id.talk_btn_session_cancel:
			{
				callSelectClean();
				break;
			}
			case R.id.talk_btn_session_delete:
			{
				contextMain.showDialog(R.id.talk_dialog_member_delete);
				break;
			}
			case R.id.talk_layout_meida_icon_add:
			{
				if (session != null)
				{
					Intent it = new Intent(contextMain, UserAllActivity.class);
					it.putExtra("type", UserAllActivity.TYPE_ADD);
					it.putExtra("name", session.getSessionCode());
					contextMain.startActivity(it);

				}
				break;
			}
			case R.id.talk_layout_meida_icon_edit:
			{
				if (session != null)
				{
					Intent it = new Intent(contextMain, ChannelManageActivity.class);
					it.putExtra("type", ChannelManageActivity.TYPE_EDIT);
					it.putExtra("roomId", session.getSessionCode());
					it.putExtra("roomName", session.getDisplayName());
					contextMain.startActivity(it);
				}
				break;
			}
			case R.id.talk_lv_session_call:
			{
				if (session != null)
				{
					AlertDialog.Builder builder = new AlertDialog.Builder(contextMain);
					builder.setMessage(contextMain.getString(R.string.talk_incoming_channel_alert_send_tip));
					builder.setPositiveButton(contextMain.getString(R.string.talk_ok), new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialog, int whichButton)
						{
							dialog.cancel();
							Util.Toast(contextMain, contextMain.getString(R.string.talk_incoming_channel_alert_sending));
							AirtalkeeChannel.getInstance().ChannelAlert(session.getSessionCode(), false);
						}
					});
					builder.setNegativeButton(contextMain.getString(R.string.talk_no), new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialog, int whichButton)
						{
							dialog.cancel();
						}
					});
					builder.show();
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
		if (sessionBox.isMenuShowing())
		{
			sessionBox.resetMenu();
			return;
		}

		if (sessionBox.getSessionType() == AirSession.TYPE_CHANNEL)
		{
			switch (parent.getId())
			{
				case R.id.talk_lv_session_member:
				{
					CheckBox cb = (CheckBox) view.findViewById(R.id.talk_cb_group_member);
					AirContact c = (AirContact) adapterMember.getItem(position - 1);
					if (c != null)
					{
						if (!AirtalkeeAccount.getInstance().getUserId().equals(c.getIpocId()))
						{
							if (cb != null)
								cb.setChecked(!cb.isChecked());
						}
					}
					break;
				}
			}
		}
	}

}
