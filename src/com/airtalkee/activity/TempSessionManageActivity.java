package com.airtalkee.activity;

import java.util.List;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
import com.airtalkee.adapter.AdapterSession;
import com.airtalkee.config.Config;
import com.airtalkee.sdk.AirtalkeeSessionManager;
import com.airtalkee.sdk.entity.AirSession;
import com.airtalkee.widget.MListView;

public class TempSessionManageActivity extends ActivityBase implements OnClickListener, OnItemClickListener
{
	
	private AdapterSession adapterSessionList;
	private View mPanel;
	private MListView mList;

	@Override
	protected void onCreate(Bundle bundle)
	{
		// TODO Auto-generated method stub
		super.onCreate(bundle);
		setRequestedOrientation(Config.screenOrientation);
		setContentView(R.layout.activity_session);
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
		ivTitle.setText(R.string.talk_tools_session);
		View btnLeft = findViewById(R.id.menu_left_button);
		ImageView ivLeft = (ImageView) findViewById(R.id.bottom_left_icon);
		ivLeft.setImageResource(ThemeUtil.getResourceId(R.attr.theme_ic_topbar_back, this));
		btnLeft.setOnClickListener(this);

		RelativeLayout ivRightLay = (RelativeLayout) findViewById(R.id.talk_menu_right_button);
		ImageView ivRight = (ImageView) findViewById(R.id.bottom_right_icon);
		ivRight.setVisibility(View.GONE);
		ivRightLay.setVisibility(View.INVISIBLE);
		
		mPanel = findViewById(R.id.session_manage_panel);
		mList = (MListView) findViewById(R.id.session_list);
		adapterSessionList = new AdapterSession(this, mPanel);
		mList.setAdapter(adapterSessionList);
		mList.setOnItemClickListener(this);
		
		findViewById(R.id.session_action_cancel).setOnClickListener(this);
		findViewById(R.id.session_action_delete).setOnClickListener(this);
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
			case R.id.session_action_cancel:
				adapterSessionList.selectedSessionsClean();
				break;
			case R.id.session_action_delete:
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage(getString(R.string.talk_session_del_confirm));
				builder.setPositiveButton(getString(R.string.talk_ok), new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int whichButton)
					{
						List<AirSession> sessions = adapterSessionList.selectedSessionsGet();
						for (int i = 0; i < sessions.size(); i ++)
							AirtalkeeSessionManager.getInstance().SessionRemove(sessions.get(i).getSessionCode());
						adapterSessionList.selectedSessionsClean();
					}
				});

				builder.setNegativeButton(getString(R.string.talk_no), new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int whichButton)
					{
						dialog.cancel();
					}
				});
				builder.show();
				break;
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	{
		switch (parent.getId())
		{
			case R.id.session_list:
			{
				CheckBox cb = (CheckBox) view.findViewById(R.id.session_check);
				AirSession s = (AirSession) adapterSessionList.getItem(position - 1);
				if (s != null && cb != null)
				{
					cb.setChecked(!cb.isChecked());
				}
				break;
			}
		}
	}


}
