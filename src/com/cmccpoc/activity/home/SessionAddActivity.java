package com.cmccpoc.activity.home;

import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.AirtalkeeSessionManager;
import com.airtalkee.sdk.controller.SessionController;
import com.airtalkee.sdk.entity.AirContact;
import com.airtalkee.sdk.entity.AirSession;
import com.cmccpoc.R;
import com.cmccpoc.activity.home.widget.MemberAllView;
import com.cmccpoc.activity.home.widget.MemberAllView.MemberCheckListener;

public class SessionAddActivity extends Activity implements OnClickListener, MemberCheckListener
{

	private LinearLayout containner;
	private MemberAllView memAllView;
	private ViewGroup bottom;
	private List<AirContact> tempCallMembers = null;
	private ImageView ivAddMember;
	private String sessionCode = "";

	@Override
	protected void onCreate(Bundle bundle)
	{
		super.onCreate(bundle);
		setContentView(R.layout.activity_session_addmember);
		bottom = (ViewGroup) findViewById(R.id.layout_bottom);
		memAllView = new MemberAllView(this, this);
		findViewById(R.id.btn_close).setOnClickListener(this);
		containner = (LinearLayout) findViewById(R.id.containner);
		containner.addView(memAllView);
		memAllView.getSearchPannel().setVisibility(View.VISIBLE);
		ivAddMember = (ImageView) findViewById(R.id.iv_add_member);
		ivAddMember.setOnClickListener(this);
		bundle = getIntent().getExtras();
		if (bundle != null)
		{
			sessionCode = bundle.getString("sessionCode");
		}
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
			case R.id.btn_close:
			{
				this.finish();
				break;
			}
			case R.id.iv_add_member:
			{
				callAddMember();
				break;
			}
		}
	}

	public void callSelectClean()
	{
		memAllView.resetCheckBox();
		refreshBottomView(false);
	}

	public void callAddMember()
	{
		if (tempCallMembers == null)
			tempCallMembers = new ArrayList<AirContact>();
		else
			tempCallMembers.clear();

		for (AirContact c : memAllView.getSelectedMember())
		{
			if (!TextUtils.equals(c.getIpocId(), AirtalkeeAccount.getInstance().getUserId()))
			{
				tempCallMembers.add(c);
			}
		}
		AirtalkeeSessionManager.getInstance().sessionMemberUpdate(sessionCode, tempCallMembers);
		finish();
	}

	@Override
	public void onMemberChecked(boolean isChecked)
	{
		refreshBottomView(isChecked);
		if (null != memAllView.getSelectedMember() && memAllView.getSelectedMember().size() > 0)
		{
			ivAddMember.setImageResource(R.drawable.btn_add_orange);
			ivAddMember.setClickable(true);
		}
		else
		{
			ivAddMember.setImageResource(R.drawable.btn_add_black);
			ivAddMember.setClickable(false);
		}
	}

	private void refreshBottomView(boolean isChecked)
	{
		for (int i = 0; i < bottom.getChildCount(); i++)
		{
			View child = bottom.getChildAt(i);
			child.setEnabled(isChecked);
		}
	}
}
