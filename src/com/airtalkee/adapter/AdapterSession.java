package com.airtalkee.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
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
import com.airtalkee.sdk.AirtalkeeSessionManager;
import com.airtalkee.sdk.entity.AirSession;


@SuppressLint("UseSparseArrays")
public class AdapterSession extends BaseAdapter
{
	private Context context = null;
	private View vPanel;
	private List<AirSession> mSelectedSessions = new ArrayList<AirSession>();
	private HashMap<String, AirSession> mSelectedSessionsMap = new HashMap<String, AirSession>();
	
	public AdapterSession(Context _context, View panel)
	{
		context = _context;
		vPanel = panel;
	}

	public List<AirSession> selectedSessionsGet()
	{
		return mSelectedSessions;
	}
	
	public void selectedSessionsClean()
	{
		mSelectedSessions.clear();
		mSelectedSessionsMap.clear();
		notifyDataSetChanged();
		if (vPanel != null)
			vPanel.setVisibility(View.GONE);
	}
	
	private void selectedSessionsPut(AirSession session, boolean isChecked)
	{
		if (session != null)
		{
			if (isChecked)
			{
				if (mSelectedSessionsMap.get(session.getSessionCode()) == null)
				{
					mSelectedSessions.add(session);
					mSelectedSessionsMap.put(session.getSessionCode(), session);
				}
			}
			else
			{
				mSelectedSessions.remove(session);
				mSelectedSessionsMap.remove(session.getSessionCode());
			}
		}
	}

	@Override
	public int getCount()
	{
		// TODO Auto-generated method stub
		return AirtalkeeSessionManager.getInstance().getSessionList().size();
	}

	@Override
	public Object getItem(int position)
	{
		// TODO Auto-generated method stub
		AirSession ses = null;
		try
		{
			ses = AirtalkeeSessionManager.getInstance().getSessionList().get(position);
		}
		catch (Exception e)
		{
		}
		return ses;
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
			convertView = LayoutInflater.from(context).inflate(R.layout.listitem_session, null);
			holder = new ViewHolder();
			holder.checkBox = (CheckBox) convertView.findViewById(R.id.session_check);
			holder.sessionName = (TextView) convertView.findViewById(R.id.session_text);
			holder.sessionIcon = (ImageView) convertView.findViewById(R.id.session_icon);
			convertView.setTag(holder);
		}
		else
		{
			holder = (ViewHolder) convertView.getTag();
		}
		final AirSession session = (AirSession) getItem(position);
		if (session != null)
		{
			holder.sessionName.setText(session.getDisplayName());
			holder.checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener()
			{
				public void onCheckedChanged(CompoundButton arg0, boolean isCheck)
				{
					selectedSessionsPut(session, isCheck);
					if (vPanel != null)
					{
						if (mSelectedSessions.size() > 0)
						{
							vPanel.setVisibility(View.VISIBLE);
						}
						else
						{
							vPanel.setVisibility(View.GONE);
						}
					}
				}
			});
			if (mSelectedSessionsMap.get(session.getSessionCode()) == null)
				holder.checkBox.setChecked(false);
			else
				holder.checkBox.setChecked(true);
		}
		return convertView;
	}

	class ViewHolder
	{
		CheckBox checkBox;
		ImageView sessionIcon;
		TextView sessionName;
	}
	
}
