package com.airtalkee.activity.home;

import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.airtalkee.R;
import com.airtalkee.Util.Util;
import com.airtalkee.activity.home.widget.CallAlertDialog;
import com.airtalkee.activity.home.widget.MemberAllView;
import com.airtalkee.activity.home.widget.MemberAllView.MemberCheckListener;
import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.AirtalkeeMessage;
import com.airtalkee.sdk.AirtalkeeSessionManager;
import com.airtalkee.sdk.controller.SessionController;
import com.airtalkee.sdk.entity.AirContact;
import com.airtalkee.sdk.entity.AirSession;

public class SessionNewActivity extends Activity implements OnClickListener, MemberCheckListener, TextWatcher
{

	private LinearLayout containner;
	private MemberAllView memAllView;
	private ViewGroup bottom;
	private List<AirContact> tempCallMembers = null;
	private CallAlertDialog alertDialog;
	private int DIALOG_CALL = 111;

	// search
//	private EditText etSearch;
//	private ImageView ivSearch;
//	private Button btnSearch;
//	List<AirContact> memberSearchResult = new ArrayList<AirContact>();

	@Override
	protected void onCreate(Bundle bundle)
	{
		// TODO Auto-generated method stub
		super.onCreate(bundle);
		setContentView(R.layout.activity_session_new);
		bottom = (ViewGroup) findViewById(R.id.layout_bottom);
		memAllView = new MemberAllView(this, this);
		findViewById(R.id.btn_close).setOnClickListener(this);
		containner = (LinearLayout) findViewById(R.id.containner);
		containner.addView(memAllView);
		memAllView.getSearchPannel().setVisibility(View.VISIBLE);
		findViewById(R.id.bar_left).setOnClickListener(this);
		findViewById(R.id.bar_mid).setOnClickListener(this);
		findViewById(R.id.bar_right).setOnClickListener(this);

//		etSearch = (EditText) findViewById(R.id.et_search);
//		etSearch.addTextChangedListener(this);
//		ivSearch = (ImageView) findViewById(R.id.iv_search);
//
//		btnSearch = (Button) findViewById(R.id.btn_search);
//		btnSearch.setOnClickListener(this);

	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
			case R.id.btn_close:
				this.finish();
				break;
			case R.id.bar_left:
				callSelectMember(true);
				break;
			case R.id.bar_mid:
				AirtalkeeMessage.getInstance().MessageRecordPlayStop();
				callSelectMember(false);
				callSelectClean();
				this.finish();
				break;
			case R.id.bar_right:
				callSelectClean();
				break;
//			case R.id.iv_search:
//				etSearch.setText("");
//				break;
//			case R.id.btn_search:
//				searchByKey();
//				break;
		}
	}

	public void callSelectClean()
	{
		memAllView.resetCheckBox();
		refreshBottomView(false);
	}

	public void callSelectMember(boolean isCall)
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

		if (tempCallMembers.size() > 0)
		{
			if (AirtalkeeAccount.getInstance().isEngineRunning())
			{
				AirSession s = SessionController.SessionMatch(tempCallMembers);
				if (isCall)
				{
					alertDialog = new CallAlertDialog(this, "正在呼叫" + s.getDisplayName(), "请稍后...", s.getSessionCode(), DIALOG_CALL);
					alertDialog.show();
				}
				else
				{
					if (s != null)
					{
						AirtalkeeSessionManager.getInstance().getSessionByCode(s.getSessionCode());
						HomeActivity.getInstance().onViewChanged(s.getSessionCode());
						HomeActivity.getInstance().panelCollapsed();
					}
					// Intent it = new Intent(this,
					// SessionDialogActivity.class);
					// it.putExtra("sessionCode", s.getSessionCode());
					// it.putExtra("type",
					// AirServices.TEMP_SESSION_TYPE_MESSAGE);
					// startActivity(it);
				}

			}
			else
			{
				Util.Toast(this, getString(R.string.talk_network_warning));
			}
		}
		else
		{
			Util.Toast(this, getString(R.string.talk_tip_session_call));
		}

	}
	
	private void searchByKey()
	{
//		String key = etSearch.getText().toString();
//		memberSearchResult.clear();
//		setSession(getSession());
//		for (int i = 0; i < adapterMember.getCount(); i++)
//		{
//			AirContact contact = (AirContact) adapterMember.getItem(i);
//			if (contact.getDisplayName().equalsIgnoreCase(key) || contact.getIpocId().equals(key) || contact.getDisplayName().contains(key) || contact.getIpocId().contains(key))
//			{
//				memberSearchResult.add(contact);
//			}
//		}
//		refreshMembers(getSession(), memberSearchResult);
	}

	@Override
	public void onMemberChecked(boolean isChecked)
	{
		// TODO Auto-generated method stub
		refreshBottomView(isChecked);
	}

	private void refreshBottomView(boolean isChecked)
	{
		for (int i = 0; i < bottom.getChildCount(); i++)
		{
			View child = bottom.getChildAt(i);
			child.setEnabled(isChecked);
		}
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after)
	{

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count)
	{
//		btnSearch.setEnabled(!TextUtils.isEmpty(etSearch.getText()));
//		if (TextUtils.isEmpty(etSearch.getText()))
//		{
//			ivSearch.setImageDrawable(getResources().getDrawable(R.drawable.ic_member_search));
//			ivSearch.setOnClickListener(null);
//		}
//		else
//		{
//			searchByKey();
//			ivSearch.setImageDrawable(getResources().getDrawable(R.drawable.ic_close_cicle));
//			ivSearch.setOnClickListener(this);
//		}
	}

	@Override
	public void afterTextChanged(Editable s)
	{

	}
}
