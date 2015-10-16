package com.airtalkee.adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.airtalkee.R;
import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.controller.AirTaskController;
import com.airtalkee.sdk.entity.AirTask;
import com.nostra13.universalimageloader.core.ImageLoader;

public class AdapterTask extends BaseAdapter 
{
	private Context context = null;
	protected ImageLoader imageLoader = ImageLoader.getInstance();

	public AdapterTask(Context _context, ListView lv)
	{
		context = _context;
	}

	@Override
	public int getCount()
	{
		// TODO Auto-generated method stub
		return  AirTaskController.getInstance().getTaskList() != null ? AirTaskController.getInstance().getTaskList().size() :0;
	}

	@Override
	public Object getItem(int position)
	{
		// TODO Auto-generated method stub
		AirTask task = null;
		if ( AirTaskController.getInstance().getTaskList().size() > 0)
		{
			task =  AirTaskController.getInstance().getTaskList().get( AirTaskController.getInstance().getTaskList().size() - position - 1);
		}
		return task;
	}

	@Override
	public long getItemId(int position)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		// TODO Auto-generated method stub
		ViewHolder holder = null;
		AirTask task = (AirTask) getItem(position);
		if (convertView == null)
		{
			convertView = LayoutInflater.from(context).inflate(R.layout.listitem_task, null);
			holder = new ViewHolder();
			holder.display = (TextView) convertView.findViewById(R.id.talk_title);
			holder.creater = (TextView) convertView.findViewById(R.id.talk_creater);
			holder.status = (TextView) convertView.findViewById(R.id.talk_state);
			convertView.setTag(holder);
		}
		else
		{
			holder = (ViewHolder) convertView.getTag();
		}
		if (task != null)
		{
			holder.display.setText(task.getTaskTitle());
			if (TextUtils.equals(AirtalkeeAccount.getInstance().getUserId(), task.getTaskLeaderId()))
				holder.creater.setText(context.getString(R.string.talk_task_details_leader) + context.getString(R.string.talk_me));
			else
				holder.creater.setText(context.getString(R.string.talk_task_details_leader) + task.getTaskLeaderName());
			switch (task.getTaskState())
			{
				case AirTask.STATE_IDLE:
					holder.status.setText(context.getString(R.string.talk_task_status_notstart));
					holder.status.setTextColor(Color.WHITE);
					break;
				case AirTask.STATE_DOING:
					holder.status.setText(context.getString(R.string.talk_task_status_doing));
					holder.status.setTextColor(Color.YELLOW);
					break;
				case AirTask.STATE_COMPLETED:
					holder.status.setText(context.getString(R.string.talk_task_status_close));
					holder.status.setTextColor(Color.GRAY);
					holder.status.setBackgroundResource(R.drawable.transparent);
					break;
			}
		}
		return convertView;
	}

	class ViewHolder
	{
		TextView display;
		TextView creater;
		TextView status;
	}
	
}
