package com.airtalkee.activity.home.widget;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.airtalkee.R;
import com.airtalkee.activity.home.AdapterMemberAll;
import com.airtalkee.activity.home.AdapterMemberAll.CheckedCallBack;
import com.airtalkee.sdk.entity.AirContact;
import com.airtalkee.sdk.entity.AirContactTiny;
import com.airtalkee.widget.MListView;

public class MemberAllView extends LinearLayout implements OnClickListener,
		OnItemClickListener, TextWatcher, CheckedCallBack {
	public interface MemberCheckListener {
		public void onMemberChecked(boolean isChecked);
	}

	List<AirContact> memberAll = new ArrayList<AirContact>();
	List<AirContact> memberSearchResult = new ArrayList<AirContact>();
	private MListView lvMemberAll;
	private AdapterMemberAll adapterMember;
	CallAlertDialog alertDialog;
	private Button btnSearch;
	private EditText etSearch;
	private MemberCheckListener listener;

	public MemberAllView(Context context, MemberCheckListener l) {
		super(context);
		this.listener = l;
		// TODO Auto-generated constructor stub
		LayoutInflater.from(this.getContext()).inflate(
				R.layout.layout_member_all, this);
		this.listener = l;
		btnSearch = (Button) findViewById(R.id.btn_search);
		etSearch = (EditText) findViewById(R.id.et_search);
		lvMemberAll = (MListView) findViewById(R.id.talk_lv_member_all);
		btnSearch.setOnClickListener(this);
		etSearch.addTextChangedListener(this);

		adapterMember = new AdapterMemberAll(getContext(), this);
		lvMemberAll.setAdapter(adapterMember);
	}

	@Override
	protected void onFinishInflate() {
		// TODO Auto-generated method stub
		super.onFinishInflate();

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btn_search: {
			String key = etSearch.getText().toString();
			memberSearchResult.clear();
			if (TextUtils.isEmpty(key)) {
				adapterMember.notifyMember(memberAll);
			} else {
				for (int i = 0; i < memberAll.size(); i++) {
					AirContact contact = memberAll.get(i);
					if (contact.getDisplayName().equalsIgnoreCase(key)
							|| contact.getIpocId().equals(key)) {
						memberSearchResult.add(contact);
					}
				}
				adapterMember.notifyMember(memberSearchResult);
			}
			break;
		}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		switch (parent.getId()) {

		}

	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		// TODO Auto-generated method stub
		btnSearch.setEnabled(!TextUtils.isEmpty(etSearch.getText()));
	}

	@Override
	public void afterTextChanged(Editable s) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onChecked(boolean isChecked) {
		// TODO Auto-generated method stub
		if (listener != null)
			listener.onMemberChecked(isChecked);
	}
	
	public List<AirContactTiny> getSelectedMember()
	{
		if(adapterMember != null)
		{
			return adapterMember.getSelectedMemberListTiny();
		}
		return null;
	}

}
