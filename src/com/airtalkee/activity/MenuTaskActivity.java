package com.airtalkee.activity;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airtalkee.R;
import com.airtalkee.Util.ThemeUtil;
import com.airtalkee.adapter.AdapterTask;
import com.airtalkee.config.Config;
import com.airtalkee.sdk.controller.AirTaskController;
import com.airtalkee.sdk.controller.AirTaskController.AirTaskListener;
import com.airtalkee.sdk.entity.AirTask;
import com.airtalkee.sdk.entity.AirTaskReport;
import com.airtalkee.widget.MListView;

public class MenuTaskActivity extends ActivityBase implements OnClickListener, OnItemClickListener,AirTaskListener
{
	private AdapterTask adapterTask;
	private MListView lvTask;
	private ImageView ivTaskEmpty;

	@Override
	protected void onCreate(Bundle bundle)
	{
		// TODO Auto-generated method stub
		super.onCreate(bundle);
		setRequestedOrientation(Config.screenOrientation);
		setContentView(R.layout.activity_task);
		doInitView();
	}

	@Override
	protected void onPause()
	{
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onResume()
	{
		// TODO Auto-generated method stub
		super.onResume();
		AirTaskController.getInstance().setAirTaskListener(this);
		AirTaskController.getInstance().doTaskListGet(AirTask.STATE_IDLE);
		if(adapterTask != null)
		{
			adapterTask.notifyDataSetChanged();
			if (adapterTask.getCount() > 0)
				ivTaskEmpty.setVisibility(View.GONE);
			else
				ivTaskEmpty.setVisibility(View.VISIBLE);
		}
	}

	private void doInitView()
	{
		TextView ivTitle = (TextView) findViewById(R.id.tv_main_title);
		ivTitle.setText(R.string.talk_tools_task);
		View btnLeft = findViewById(R.id.menu_left_button);
		ImageView ivLeft = (ImageView) findViewById(R.id.bottom_left_icon);
		ivLeft.setImageResource(ThemeUtil.getResourceId(R.attr.theme_ic_topbar_back, this));
		btnLeft.setOnClickListener(this);

		RelativeLayout ivRightLay = (RelativeLayout) findViewById(R.id.talk_menu_right_button);
		ImageView ivRight = (ImageView) findViewById(R.id.bottom_right_icon);
		ivRight.setImageResource(ThemeUtil.getResourceId(R.attr.theme_ic_refresh, this) );
		ivRightLay.setOnClickListener(this);
		
		lvTask = (MListView) findViewById(R.id.lv_task);
		ivTaskEmpty = (ImageView) findViewById(R.id.iv_task_empty);
		adapterTask = new AdapterTask(this, lvTask);

		lvTask.setAdapter(adapterTask);
		lvTask.setOnItemClickListener(this);
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
			case R.id.talk_menu_right_button:
			case R.id.bottom_right_icon:
				AirTaskController.getInstance().doTaskListGet(AirTask.STATE_IDLE);
				break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	{
		// TODO Auto-generated method stub
		AirTask task = (AirTask) adapterTask.getItem(position - 1);
		if (task != null)
		{
			Intent it = new Intent(this, MenuTaskDetailsActivity.class);
			it.putExtra("taskcode", task.getTaskCode());
			startActivity(it);
		}
	}

	@Override
	public void onTaskListGet(boolean isOk, ArrayList<AirTask> tasks)
	{
		// TODO Auto-generated method stub
		if (adapterTask != null)
		{
			adapterTask.notifyDataSetChanged();
			if (adapterTask.getCount() > 0)
				ivTaskEmpty.setVisibility(View.GONE);
			else
				ivTaskEmpty.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onTaskState(boolean isOk, String taskCode)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTaskContentListGet(boolean isOk, ArrayList<AirTaskReport> tasks, String taskCode)
	{
		// TODO Auto-generated method stub
		
	}

}
