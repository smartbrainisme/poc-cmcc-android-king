package com.cmccpoc.activity.home.widget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.AirtalkeeChannel;
import com.airtalkee.sdk.entity.AirChannel;
import com.airtalkee.sdk.entity.AirContact;
import com.airtalkee.sdk.util.Log;
import com.cmccpoc.R;
import com.cmccpoc.Util.Util;
import com.cmccpoc.activity.home.adapter.AdapterMemberAll;
import com.cmccpoc.activity.home.adapter.AdapterMemberAll.CheckedCallBack;
import com.cmccpoc.widget.MListView;

/**
 * 全体成员 自定义View控件
 * @author Yao
 */
public class MemberAllView extends LinearLayout implements OnClickListener, OnItemClickListener, TextWatcher, CheckedCallBack
{
	public interface MemberCheckListener
	{
		/**
		 * 成员选择
		 * @param isChecked 是否选择
		 */
		public void onMemberChecked(boolean isChecked);
	}

	public List<AirContact> memberAll;
	List<AirContact> memberSearchResult = new ArrayList<AirContact>();
	private MListView lvMemberAll;
	public AdapterMemberAll adapterMember;
	CallAlertDialog alertDialog;
	private LinearLayout searchPannel;
	private Button btnSearch;
	private EditText etSearch;
	private ImageView ivSearch;
	private MemberCheckListener listener;

	public MemberAllView(Context context, MemberCheckListener l)
	{
		super(context);
		this.listener = l;
		// TODO Auto-generated constructor stub
		LayoutInflater.from(context).inflate(R.layout.layout_member_all, this);
		this.listener = l;
		btnSearch = (Button) findViewById(R.id.btn_search);
		etSearch = (EditText) findViewById(R.id.et_search);
		btnSearch.setOnClickListener(this);
		etSearch.addTextChangedListener(this);
		memberAll = getAllAirContacts();
		lvMemberAll = (MListView) findViewById(R.id.talk_lv_member_all);
		adapterMember = new AdapterMemberAll(context, this);
		lvMemberAll.setOnItemClickListener(this);
		adapterMember.notifyMember(memberAll);
		lvMemberAll.setAdapter(adapterMember);
		searchPannel = (LinearLayout) findViewById(R.id.serach_pannel);
		ivSearch = (ImageView) findViewById(R.id.iv_search);
	}
	
	/**
	 * 成员搜索layout
	 * @return LinearLayout
	 */
	public LinearLayout getSearchPannel()
	{
		return searchPannel;
	}

	/**
	 * 获取全体成员
	 * 将所有频道内的成员取并集
	 * @return 全体成员列表
	 */
	public List<AirContact> getAllAirContacts()
	{
		List<AirContact> contacts = new ArrayList<AirContact>();
		Map<String, AirContact> allMembers = new HashMap<String, AirContact>();
		List<AirChannel> channels = AirtalkeeChannel.getInstance().getChannels();
		if (channels != null && channels.size() > 0)
		{
			for (AirChannel channel : channels)
			{
				List<AirContact> members = channel.MembersGet();
				for (AirContact member : members)
				{
					allMembers.put(member.getIpocId(), member);
				}
			}
		}
		Iterator<Entry<String, AirContact>> iter = allMembers.entrySet().iterator();
		while (iter.hasNext())
		{
			Map.Entry<String, AirContact> entry = iter.next();
			contacts.add(entry.getValue());
		}
		Collections.sort(contacts, new Comparator<AirContact>()
		{
			@Override
			public int compare(AirContact member1, AirContact member2)
			{
				int result = 0;
				if (member1.chatSortSeed > member2.chatSortSeed)
					result = -1;
				else if (member1.chatSortSeed < member2.chatSortSeed)
					result = 1;
				return result;
			}
		});
		return contacts;
	}

	@Override
	protected void onFinishInflate()
	{
		// TODO Auto-generated method stub
		super.onFinishInflate();
	}

	@Override
	public void onClick(View v)
	{
		// TODO Auto-generated method stub
		switch (v.getId())
		{
			case R.id.btn_search:
			{
				Util.hideSoftInput(getContext());
				searchByKey();
				break;
			}
			case R.id.iv_search:
			{
				etSearch.setText("");
				break;
			}
		}
	}

	/**
	 * 根据key模糊搜索成员
	 */
	private void searchByKey()
	{
		Log.i(MemberAllView.class, "memberall size = " + memberAll.size());
		String key = etSearch.getText().toString();
		memberSearchResult.clear();
		if(memberAll == null || memberAll.size() == 0)
		{
			memberAll = getAllAirContacts();
			lvMemberAll.setAdapter(adapterMember);
		}
		if (TextUtils.isEmpty(key))
		{
			adapterMember.notifyMember(memberAll);
		}
		else
		{
			for (int i = 0; i < memberAll.size(); i++)
			{
				AirContact contact = memberAll.get(i);
				if (contact.getDisplayName().equalsIgnoreCase(key) || contact.getIpocId().equals(key) || contact.getDisplayName().contains(key) || contact.getIpocId().contains(key))
				{
					memberSearchResult.add(contact);
				}
			}
			adapterMember.notifyMember(memberSearchResult);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	{
		// TODO Auto-generated method stub
		switch (parent.getId())
		{
			case R.id.talk_lv_member_all:
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

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after)
	{

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count)
	{
		btnSearch.setEnabled(!TextUtils.isEmpty(etSearch.getText()));
		if (TextUtils.isEmpty(etSearch.getText()))
		{
			adapterMember.notifyMember(memberAll);
			ivSearch.setImageDrawable(getResources().getDrawable(R.drawable.ic_member_search));
			ivSearch.setOnClickListener(null);
		}
		else
		{
			searchByKey();
			ivSearch.setImageDrawable(getResources().getDrawable(R.drawable.ic_close_cicle));
			ivSearch.setOnClickListener(this);
		}
	}

	@Override
	public void afterTextChanged(Editable s)
	{

	}

	@Override
	public void onChecked(boolean isChecked)
	{
		// TODO Auto-generated method stub
		if (listener != null)
			listener.onMemberChecked(isChecked);
	}

	/**
	 * 重置全体成员的选中状态
	 */
	public void resetCheckBox()
	{
		if (adapterMember != null)
		{
			adapterMember.resetCheckBox();
		}
	}

	/**
	 * 获取选择中的成员列表
	 * @return 成员列表
	 */
	public List<AirContact> getSelectedMember()
	{
		if (adapterMember != null)
		{
			return adapterMember.getSelectedMemberList();
		}
		return null;
	}

	/**
	 * 获取选择中的成员数
	 * @return 成员数
	 */
	public int getSelectedMemberSize()
	{
		if (adapterMember != null)
		{
			return adapterMember.getSelectedMemberList().size();
		}
		return 0;
	}

}
