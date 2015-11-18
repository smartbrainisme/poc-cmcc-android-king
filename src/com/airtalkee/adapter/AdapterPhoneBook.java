package com.airtalkee.adapter;

import java.io.Serializable;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;
import com.airtalkee.R;
import com.airtalkee.Util.Util;
import com.airtalkee.activity.ChannelManageActivity;
import com.airtalkee.sdk.entity.AirContactTiny;
import com.airtalkee.sdk.entity.Phonebook;
import com.airtalkee.sdk.util.Log;

public class AdapterPhoneBook extends BaseAdapter
{
	private Context context = null;
	private List<AirContactTiny> memberList = null;
	@SuppressLint("UseSparseArrays")
	private HashMap<Integer, AirContactTiny> isSelected = new HashMap<Integer, AirContactTiny>();
	public List<String> PbListItem = new ArrayList<String>();

	@SuppressWarnings("deprecation")
	public AdapterPhoneBook(Context _context)
	{
		context = _context;
		PbListItem.clear();
		isSelected.clear();
		memberList = null;
		ChannelManageActivity.getInstance().showDialog(R.id.talk_dialog_waiting);
		new Task().execute();
	}

	private void putSelected(Integer key, AirContactTiny value, boolean isCheck)
	{
		if (isSelected == null)
			return;
		if (isCheck)
		{
			isSelected.put(key, value);
		}
		else if (isSelected.size() > 0)
		{
			isSelected.remove(key);
		}
	}

	public List<AirContactTiny> getSelectedMemberList()
	{
		List<AirContactTiny> selectList = new ArrayList<AirContactTiny>();
		if (isSelected != null)
		{
			@SuppressWarnings("rawtypes")
			Iterator iterable = (Iterator) isSelected.values().iterator();
			while (iterable.hasNext())
			{
				selectList.add((AirContactTiny) iterable.next());
			}
		}
		return selectList;
	}

	public void notifyMember(List<AirContactTiny> _memberList)
	{
		memberList = _memberList;
		isSelected.clear();
		notifyDataSetChanged();
	}

	@Override
	public int getCount()
	{
		// TODO Auto-generated method stub
		return (memberList != null) ? memberList.size() : 0;
	}

	@Override
	public Object getItem(int position)
	{
		// TODO Auto-generated method stub
		AirContactTiny ct = null;
		try
		{
			ct = (memberList != null) ? memberList.get(position) : null;
		}
		catch (Exception e)
		{
		}
		return ct;
	}

	@Override
	public long getItemId(int position)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent)
	{
		// TODO Auto-generated method stub
		ViewHolder holder = null;
		if (convertView == null)
		{
			convertView = LayoutInflater.from(context).inflate(R.layout.listitem_member, null);
			holder = new ViewHolder();
			holder.checkBox = (CheckBox) convertView.findViewById(R.id.talk_cb_group_member);
			holder.tvName = (TextView) convertView.findViewById(R.id.talk_tv_group_member);
			holder.tvPresence = (TextView) convertView.findViewById(R.id.talk_tv_group_presence);
			holder.presence = (ImageView) convertView.findViewById(R.id.talk_iv_group_presence);
			holder.presence.setVisibility(View.GONE);
			convertView.setTag(holder);
		}
		else
		{
			holder = (ViewHolder) convertView.getTag();
		}
		final AirContactTiny member = (AirContactTiny) getItem(position);
		if (member != null)
		{
			// Log.e(ChannelCreateActivity.class,
			// String.format("name =[%s],phone=[%s]",
			// member.getDisplayName(),member.getiPhoneNumber()));
			holder.tvName.setText(member.getDisplayName());
			holder.tvPresence.setText(member.getiPhoneNumber());

			holder.checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener()
			{
				public void onCheckedChanged(CompoundButton arg0, boolean isCheck)
				{
					if (member != null)
						putSelected(position, member, isCheck);
				}
			});
			holder.checkBox.setChecked(!(isSelected != null && isSelected.get(position) == null));
		}
		convertView.setBackgroundResource((position % 2 == 1) ? R.drawable.member_selected : R.drawable.member_unselected);
		return convertView;
	}

	class ViewHolder
	{
		CheckBox checkBox;
		TextView tvName;
		TextView tvPresence;
		ImageView presence;
	}

	public List<AirContactTiny> GetPhonebook()
	{
		List<AirContactTiny> phonebook = new ArrayList<AirContactTiny>();
		Cursor cursor = context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
		while (cursor.moveToNext())
		{
			int nameFieldColumnIndex = cursor.getColumnIndex(PhoneLookup.DISPLAY_NAME);
			int numberFieldColumnIndex = cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER);
			if (numberFieldColumnIndex == -1 || nameFieldColumnIndex == -1)
				continue;
			String contactName = cursor.getString(nameFieldColumnIndex);
			String userNumber = cursor.getString(numberFieldColumnIndex);
			String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
			if (userNumber.compareTo("1") == 0)
			{
				Cursor phones = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
					ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null);
				while (phones != null && phones.moveToNext())
				{
					userNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
				}

				phones.close();
			}

			userNumber = Util.GetNumber(userNumber);
			AirContactTiny ct = new AirContactTiny();
			ct.setDisplayName(contactName);
			ct.setiPhoneNumber(userNumber);
			if (Util.IsUserNumber(userNumber))
			{
				phonebook.add(ct);
			}
		}
		cursor.close();
		return phonebook;
	}

	public void GetLocalContact()
	{
		Cursor cursor = context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
		while (cursor.moveToNext())
		{
			int nameFieldColumnIndex = cursor.getColumnIndex(PhoneLookup.DISPLAY_NAME);
			int numberFieldColumnIndex = cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER);
			if (numberFieldColumnIndex == -1 || nameFieldColumnIndex == -1)
				continue;
			String contactName = cursor.getString(nameFieldColumnIndex);
			String userNumber = cursor.getString(numberFieldColumnIndex);
			String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
			if (userNumber.compareTo("1") == 0)
			{
				Cursor phones = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
					ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null);
				while (phones != null && phones.moveToNext())
				{
					userNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
				}

				phones.close();
			}

			userNumber = Util.GetNumber(userNumber);
			AirContactTiny ct = new AirContactTiny();
			ct.setDisplayName(contactName);
			ct.setiPhoneNumber(userNumber);
			// Log.d( "userNumber--->"+userNumber+"userName--->"+contactName);
			if (Util.IsUserNumber(userNumber))
			{
				if (Phonebook.getInstance().isContains(userNumber) == null)
				{
					Phonebook.getInstance().addPhoneBookList(ct);
				}
			}
		}
		cursor.close();
	}

	public void GetSimContact(String add)
	{
		try
		{
			Intent intent = new Intent();
			intent.setData(Uri.parse(add));
			Uri uri = intent.getData();
			Cursor mCursor = context.getContentResolver().query(uri, null, null, null, null);
			if (mCursor != null)
			{
				while (mCursor.moveToNext())
				{
					int nameFieldColumnIndex = mCursor.getColumnIndex("name");
					String contactName = mCursor.getString(nameFieldColumnIndex);
					int numberFieldColumnIndex = mCursor.getColumnIndex("number");
					String userNumber = mCursor.getString(numberFieldColumnIndex);

					userNumber = Util.GetNumber(userNumber);
					AirContactTiny ct = new AirContactTiny();
					ct.setDisplayName(contactName);
					ct.setiPhoneNumber(userNumber);
					if (Util.IsUserNumber(userNumber))
					{
						if (Phonebook.getInstance().isContains(userNumber) == null)
						{
							Phonebook.getInstance().addPhoneBookList(ct);
						}
					}
				}
				mCursor.close();
			}
		}
		catch (Exception e)
		{
			Log.i(Util.class, e.toString());
		}
	}

	private Comparator<AirContactTiny> peopleComparator = new PeopleComparator();

	private static final class PeopleComparator implements Serializable, Comparator<AirContactTiny>
	{
		private static final long serialVersionUID = 1L;

		@SuppressWarnings("unchecked")
		public int compare(AirContactTiny c1, AirContactTiny c2)
		{
			String n1 = c1.getDisplayName();
			String n2 = c2.getDisplayName();
			@SuppressWarnings("rawtypes")
			Comparator cmp = Collator.getInstance(java.util.Locale.CHINA);
			return cmp.compare(n1, n2);
		}
	}

	private class Task extends AsyncTask<Void, Void, String[]>
	{
		protected String[] doInBackground(Void... params)
		{
			// Simulates a background job.
			List<AirContactTiny> members = null;
			if (Phonebook.getInstance().getPhoneBookList() != null && Phonebook.getInstance().getPhoneBookList().size() > 0)
			{
				members = new ArrayList<AirContactTiny>();
				if (Phonebook.getInstance().getPhoneBookList().values() != null)
				{
					Object ct[] = Phonebook.getInstance().getPhoneBookList().values().toArray();
					if (ct != null)
					{
						for (int i = 0; i < ct.length; i++)
						{
							members.add((AirContactTiny) ct[i]);
						}
					}
				}
			}
			else
			{
				members = GetPhonebook();
				if (members != null)
				{
					for (AirContactTiny value : members)
					{
						Phonebook.getInstance().addPhoneBookList(value);
					}
				}
			}
			Collections.sort(members, peopleComparator);
			if (members != null)
				for (int i = 0; i < members.size(); i++)
				{
					PbListItem.add(members.get(i).getDisplayName());
				}
			memberList = members;
			return null;
		}

		@SuppressWarnings("deprecation")
		protected void onPostExecute(String[] result)
		{
			super.onPostExecute(result);
			ChannelManageActivity.getInstance().removeDialog(R.id.talk_dialog_waiting);
			notifyDataSetChanged();
		}
	}

}
