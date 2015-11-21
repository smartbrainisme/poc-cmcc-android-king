package com.airtalkee.activity;

import java.util.ArrayList;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.airtalkee.R;
import com.airtalkee.Util.ThemeUtil;
import com.airtalkee.adapter.AdapterTaskReport;
import com.airtalkee.config.Config;
import com.airtalkee.sdk.controller.AirTaskController;
import com.airtalkee.sdk.controller.AirTaskController.AirTaskListener;
import com.airtalkee.sdk.entity.AirTask;
import com.airtalkee.sdk.entity.AirTaskReport;
import com.airtalkee.widget.MListView;

public class MenuTaskDetailsActivity extends ActivityBase implements OnClickListener, OnItemClickListener,AirTaskListener
{
	private AdapterTaskReport adapterTaskReport;
	private MListView lvReportList;
	private AirTask currentTask;
	private Button 	btnTaskExcute,btnTaskReport;
	private LinearLayout layoutAction;
	private TextView tvTitle;
	private TextView tvLeaderName;
	private TextView tvCreaterName;
	private TextView tvDetail;
	
	@Override
	protected void onCreate(Bundle bundle)
	{
		// TODO Auto-generated method stub
		super.onCreate(bundle);
		bundle = getIntent().getExtras();
		if (bundle != null)
		{
			String taskCode = bundle.getString("taskcode");
			currentTask = AirTaskController.getInstance().findTaskByCode(taskCode);
		}
		setRequestedOrientation(Config.screenOrientation);
		setContentView(R.layout.activity_task_details);
		doInitView();
		doInit();
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
		if (currentTask != null)
		{
			AirTaskController.getInstance().setAirTaskListener(this);
			AirTaskController.getInstance().doTaskContentListGet(currentTask.getTaskCode());
		}
		refreshTaskBtn();
	}
	
	private void doInitView()
	{
		TextView ivTitle = (TextView) findViewById(R.id.tv_main_title);
		ivTitle.setText(R.string.talk_title_task_details);
		View btnLeft = findViewById(R.id.menu_left_button);
		ImageView ivLeft = (ImageView) findViewById(R.id.bottom_left_icon);
		ivLeft.setImageResource(ThemeUtil.getResourceId(R.attr.theme_ic_topbar_back, this));
		btnLeft.setOnClickListener(this);

		RelativeLayout ivRightLay = (RelativeLayout) findViewById(R.id.talk_menu_right_button);
		ImageView ivRight = (ImageView) findViewById(R.id.bottom_right_icon);
		ivRight.setVisibility(View.GONE);
		ivRightLay.setVisibility(View.INVISIBLE);
		
		lvReportList = (MListView) findViewById(R.id.talk_report_list);
		adapterTaskReport = new AdapterTaskReport(currentTask,this);
		lvReportList.setAdapter(adapterTaskReport);
		lvReportList.setOnItemClickListener(this);
		
		btnTaskExcute = (Button)findViewById(R.id.btn_task_excute);
		btnTaskReport = (Button)findViewById(R.id.btn_task_report);
		btnTaskExcute.setOnClickListener(this);
		btnTaskReport.setOnClickListener(this);
		layoutAction = (LinearLayout)findViewById(R.id.layout_task);
		
		tvTitle = (TextView)findViewById(R.id.talk_title);
		tvLeaderName = (TextView)findViewById(R.id.talk_leader_name);
		tvCreaterName = (TextView)findViewById(R.id.talk_creater_name);
		tvDetail = (TextView)findViewById(R.id.talk_desc);
		
	}
	
	private void doInit()
	{
		tvTitle.setText(getString(R.string.talk_task_details_name)+currentTask.getTaskTitle());
		tvLeaderName.setText(getString(R.string.talk_task_details_leader)+currentTask.getTaskLeaderName());
		tvCreaterName.setText(getString(R.string.talk_task_details_creater)+currentTask.getTaskCreaterName());
		tvDetail.setText(currentTask.getTaskDetail());
	}
	
	private void refreshTaskBtn()
	{
		switch(currentTask.getTaskState())
		{
			case AirTask.STATE_IDLE:
				btnTaskExcute.setText(R.string.talk_task_action_start);
				btnTaskReport.setVisibility(View.GONE);
				layoutAction.setVisibility(View.VISIBLE);
				break;
			case AirTask.STATE_DOING:
				btnTaskExcute.setText(R.string.talk_task_action_stop);
				btnTaskReport.setVisibility(View.VISIBLE);
				layoutAction.setVisibility(View.VISIBLE);
				break;
			case AirTask.STATE_COMPLETED:
				layoutAction.setVisibility(View.GONE);
				break;
		}
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
			case R.id.btn_task_excute:
			{
				switch(currentTask.getTaskState())
				{
					case AirTask.STATE_IDLE:
					{
						AlertDialog.Builder builder = new AlertDialog.Builder(this);
						builder.setMessage(getString(R.string.talk_task_action_confirm_start));
						builder.setPositiveButton(getString(R.string.talk_ok), new DialogInterface.OnClickListener()
						{
							public void onClick(DialogInterface dialog, int whichButton)
							{
								dialog.cancel();
								AirTaskController.getInstance().doTaskState(currentTask.getTaskCode(), AirTask.STATE_DOING);
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
					case AirTask.STATE_DOING:
					{
						AlertDialog.Builder builder = new AlertDialog.Builder(this);
						builder.setMessage(getString(R.string.talk_task_action_confirm_stop));
						builder.setPositiveButton(getString(R.string.talk_ok), new DialogInterface.OnClickListener()
						{
							public void onClick(DialogInterface dialog, int whichButton)
							{
								dialog.cancel();
								AirTaskController.getInstance().doTaskState(currentTask.getTaskCode(), AirTask.STATE_COMPLETED);
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
				break;
			}
			case R.id.btn_task_report:
			{
				if(currentTask.getTaskState() == AirTask.STATE_DOING)
				{
					Intent it = new Intent(this, MenuReportActivity.class);
					it.putExtra("taskId", currentTask.getTaskCode());
					it.putExtra("taskName", currentTask.getTaskTitle());
					startActivity(it);
				}
				break;
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	{
		// TODO Auto-generated method stub
		/*
		AirTaskReport taskReport = (AirTaskReport) adapterTaskReport.getItem(position - 1);
		if (taskReport != null)
		{
			Intent it = new Intent(this, MenuReportViewActivity.class);
			it.putExtra("code", taskReport.getCode());
			startActivity(it);
		}
		*/
	}

	@Override
	public void onTaskListGet(boolean isOk, ArrayList<AirTask> tasks)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTaskState(boolean isOk, String taskCode)
	{
		// TODO Auto-generated method stub
		if(isOk && !TextUtils.isEmpty(taskCode) && taskCode.equals(currentTask.getTaskCode()))
		{
			refreshTaskBtn();
		}
	}

	@Override
	public void onTaskContentListGet(boolean isOk, ArrayList<AirTaskReport> tasks, String taskCode)
	{
		// TODO Auto-generated method stub
		if(adapterTaskReport != null)
			adapterTaskReport.notifyDataSetChanged();
	}
	

}
