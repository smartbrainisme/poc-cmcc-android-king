package com.airtalkee.adapter;

import java.util.List;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.airtalkee.R;
import com.airtalkee.activity.MenuAttendanceActivity;
import com.airtalkee.sdk.entity.AirAttendance;


@SuppressLint("UseSparseArrays")
public class AdapterAttendace extends BaseAdapter
{
	private MenuAttendanceActivity mActivity = null;
	private List<AirAttendance> mAttendaces = null;
	
	public AdapterAttendace(MenuAttendanceActivity activity)
	{
		mActivity = activity;
		
	}
	
	public void notifyData(List<AirAttendance> attendaces)
	{
		mAttendaces = attendaces;
	}

	@Override
	public int getCount()
	{
		// TODO Auto-generated method stub
		return (mAttendaces != null) ? mAttendaces.size() : 0;
	}

	@Override
	public Object getItem(int position)
	{
		// TODO Auto-generated method stub
		AirAttendance ct = null;
		try
		{
			ct = (mAttendaces != null) ? mAttendaces.get(position) : null;
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
		// Log.e(AdapterMember.class, "AdapterMember getView");
		if (convertView == null)
		{
			convertView = LayoutInflater.from(mActivity).inflate(R.layout.listitem_attendance, null);
			holder = new ViewHolder();
			holder.tvTitle = (TextView) convertView.findViewById(R.id.talk_attendance_title);
			holder.tvContent = (TextView) convertView.findViewById(R.id.talk_attendance_work);
			holder.button = (Button) convertView.findViewById(R.id.talk_attendance_button);
			convertView.setTag(holder);
		}
		else
		{
			holder = (ViewHolder) convertView.getTag();
		}
		
		final MenuAttendanceActivity activity = mActivity;
		final AirAttendance attendance = (AirAttendance) getItem(position);
		if (attendance != null)
		{
			holder.tvTitle.setText(attendance.getRuleName());
			
			String text = "";
			if (!TextUtils.isEmpty(attendance.getWorkStart()))
				text = "  " + mActivity.getString(R.string.talk_attend_title_work_start_time) + attendance.getWorkStart() + "\r\n";
			if (attendance.getResultStart() != AirAttendance.RESULT_NONE)
			{
				text += "  " + mActivity.getString(R.string.talk_attend_title_work_start_result);
				switch(attendance.getResultStart())
				{
					case AirAttendance.RESULT_START_NORMAL:
						text += mActivity.getString(R.string.talk_attend_tip_start_normal);
						break;
					case AirAttendance.RESULT_START_LATE:
						text += mActivity.getString(R.string.talk_attend_tip_start_late);
						break;
					case AirAttendance.RESULT_START_ONTIME_BUT_LOCATION_INCORRECT:
						text += mActivity.getString(R.string.talk_attend_tip_start_ontime_but_location_incorrect);
						break;
				}
				text += "\r\n";
			}
			if (!TextUtils.isEmpty(attendance.getWorkStop()))
				text += "  " + mActivity.getString(R.string.talk_attend_title_work_stop_time) + attendance.getWorkStop() + "\r\n";
			if (attendance.getResultStop() != AirAttendance.RESULT_NONE)
			{
				text += "  " + mActivity.getString(R.string.talk_attend_title_work_stop_result);
				switch(attendance.getResultStop())
				{
					case AirAttendance.RESULT_STOP_NORMAL:
						text += mActivity.getString(R.string.talk_attend_tip_stop_normal);
						break;
					case AirAttendance.RESULT_STOP_LEAVE_EARLY:
						text += mActivity.getString(R.string.talk_attend_tip_stop_leave_early);
						break;
					case AirAttendance.RESULT_STOP_OVERTIME:
						text += mActivity.getString(R.string.talk_attend_tip_stop_overtime);
						break;
					case AirAttendance.RESULT_STOP_ONTIME_BUT_LOCATION_INCORRECT:
						text += mActivity.getString(R.string.talk_attend_tip_stop_ontime_but_location_incorrect);
						break;
					case AirAttendance.RESULT_STOP_OVERTIME_AND_LOCATION_INCORRECT:
						text += mActivity.getString(R.string.talk_attend_tip_stop_overtime_but_location_incorrect);
						break;
				}
				text += "\r\n";
			}
			holder.tvContent.setText(text);
			
			if (attendance.getType() == AirAttendance.TYPE_SHOULD_ON)
			{
				holder.button.setText(mActivity.getString(R.string.talk_attend_work_start));
				holder.button.setVisibility(View.VISIBLE);
			}
			else if (attendance.getType() == AirAttendance.TYPE_SHOULD_OFF)
			{
				holder.button.setText(mActivity.getString(R.string.talk_attend_work_stop));
				holder.button.setVisibility(View.VISIBLE);
			}
			else
				holder.button.setVisibility(View.GONE);
			holder.button.setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View v)
				{
					// TODO Auto-generated method stub
					activity.doAttendanceOperation(attendance.getRuleId(), attendance.getType());
				}
				
			});

		}
		return convertView;
	}

	class ViewHolder
	{
		TextView tvTitle;
		TextView tvContent;
		Button button;
	}

}
