package com.airtalkee.activity;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.airtalkee.R;
import com.airtalkee.Util.ThemeUtil;
import com.airtalkee.adapter.AdapterGroupSelect;
import com.airtalkee.config.Config;
import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.AirtalkeeSessionManager;
import com.airtalkee.sdk.entity.AirContact;

public class TempSessionSelectedActivity extends ActivityBase implements ExpandableListView.OnChildClickListener, OnClickListener
{
	private AdapterGroupSelect adapter;
	public ExpandableListView lvGroup;
	private AirtalkeeSessionManager handleSession = null;
	private String sessionCode = "";

	@Override
	protected void onCreate(Bundle bundle)
	{
		// TODO Auto-generated method stub
		super.onCreate(bundle);
		setRequestedOrientation(Config.screenOrientation);
		setContentView(R.layout.activity_group_select);
		doInitView();
		doInit();

		bundle = getIntent().getExtras();
		if (bundle != null)
		{
			sessionCode = bundle.getString("sessionCode");
		}
	}

	@Override
	public void finish()
	{
		// TODO Auto-generated method stub
		super.finish();

	}
	
	private void doInitView()
	{
		lvGroup = (ExpandableListView) findViewById(R.id.exp_listview_group);
		TextView ivTitle = (TextView) findViewById(R.id.tv_main_title);
		ivTitle.setText(R.string.talk_session_call_title);
		View btnLeft = findViewById(R.id.menu_left_button);
		ImageView ivLeft = (ImageView) findViewById(R.id.bottom_left_icon);
		ivLeft.setImageResource(ThemeUtil.getResourceId(R.attr.theme_ic_topbar_back, this) );
		ImageView ivRight = (ImageView) findViewById(R.id.bottom_right_icon);
		ivRight.setImageResource(ThemeUtil.getResourceId(R.attr.theme_ic_done, this));
		findViewById(R.id.talk_menu_right_button).setOnClickListener(this);
		btnLeft.setOnClickListener(this);

	}

	private void doInit()
	{
		handleSession = AirtalkeeSessionManager.getInstance();
		adapter = new AdapterGroupSelect(this);
		lvGroup.setAdapter(adapter);
		lvGroup.setOnChildClickListener(this);
		lvGroup.expandGroup(0);
	}

	@Override
	public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id)
	{
		// TODO Auto-generated method stub

		CheckBox cb = (CheckBox) v.findViewById(R.id.talk_cb_member);
		AirContact c = (AirContact) adapter.getChild(groupPosition, childPosition);
		if (c != null)
		{
			if (!AirtalkeeAccount.getInstance().getUserId().equals(c.getIpocId()))
			{
				if (cb != null)
					cb.setChecked(!cb.isChecked());
				adapter.notifyDataSetChanged();
			}
		}
		return false;
	}

	@Override
	public void onClick(View v)
	{
		// TODO Auto-generated method stub
		switch (v.getId())
		{
			case R.id.menu_left_button:
				finish();
				break;
			case R.id.talk_menu_right_button:
				callAddMember();
				break;
			default:
				break;
		}
	}

	public void callAddMember()
	{
		if (adapter.getSelectedMemberList() != null && adapter.getSelectedMemberList().size() > 0)
		{
			handleSession.sessionMemberUpdate(sessionCode, adapter.getSelectedMemberList());
			finish();
		}
		else
		{
			Toast.makeText(this, R.string.talk_tip_session_call, 0).show();
		}
	}

}
