package com.airtalkee.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.airtalkee.R;
import com.airtalkee.Util.ThemeUtil;
import com.airtalkee.Util.Util;
import com.airtalkee.adapter.AdapterReport;
import com.airtalkee.config.Config;
import com.airtalkee.control.AirReportManager;
import com.airtalkee.entity.AirReport;
import com.airtalkee.listener.OnMmiReportListener;
import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.AirtalkeeSessionManager;
import com.airtalkee.sdk.controller.SessionController;
import com.airtalkee.sdk.entity.AirSession;
import com.airtalkee.services.AirServices;
import com.airtalkee.widget.MListView;

public class MenuReportActivity extends ActivityBase implements OnClickListener, OnMmiReportListener, OnItemLongClickListener, OnItemClickListener
{

	private AdapterReport adapterReport;
	private MListView lvReportList;
	private View talk_report_list_panel,talk_report_empty;
	private String taskId = null;
	private String taskName = null;

	@Override
	protected void onCreate(Bundle bundle)
	{
		// TODO Auto-generated method stub
		super.onCreate(bundle);
		setRequestedOrientation(Config.screenOrientation);
		setContentView(R.layout.activity_tool_report);
		AirReportManager.getInstance().loadReports();
		doInitView();
		
		bundle = getIntent().getExtras();
		if (bundle != null)
		{
			taskId = bundle.getString("taskId");
			taskName = bundle.getString("taskName");
		}
	}

	@Override
	protected void onPause()
	{
		// TODO Auto-generated method stub
		super.onPause();
		AirReportManager.getInstance().setReportListener(null);
		// adapterReport.showIcons(false);
	}

	@Override
	protected void onResume()
	{
		// TODO Auto-generated method stub
		super.onResume();
		refreshListOrEmpty();
		AirReportManager.getInstance().setReportListener(this);
		if(adapterReport != null)
			adapterReport.notifyDataSetChanged();
		// adapterReport.showIcons(true);
	}

	private void doInitView()
	{
		TextView ivTitle = (TextView) findViewById(R.id.tv_main_title);
		ivTitle.setText(R.string.talk_tools_report);
		View btnLeft = findViewById(R.id.menu_left_button);
		ImageView ivLeft = (ImageView) findViewById(R.id.bottom_left_icon);
		ivLeft.setImageResource(ThemeUtil.getResourceId(R.attr.theme_ic_topbar_back, this));
		btnLeft.setOnClickListener(this);

		RelativeLayout ivRightLay = (RelativeLayout) findViewById(R.id.talk_menu_right_button);
		ImageView ivRight = (ImageView) findViewById(R.id.bottom_right_icon);
		talk_report_list_panel = findViewById(R.id.talk_report_list_panel);
		talk_report_empty =findViewById(R.id.talk_report_empty);
		ivRight.setVisibility(View.GONE);
		ivRightLay.setVisibility(View.INVISIBLE);
		lvReportList = (MListView) findViewById(R.id.talk_report_list);
		adapterReport = new AdapterReport(this, lvReportList);

		lvReportList.setAdapter(adapterReport);
		lvReportList.setOnItemClickListener(this);
		lvReportList.setOnItemLongClickListener(this);

		findViewById(R.id.report_report_btn_pic).setOnClickListener(this);
		findViewById(R.id.report_report_btn_vid).setOnClickListener(this);
		if (Config.funcVideo)
			findViewById(R.id.report_real_video).setOnClickListener(this);
		else
			findViewById(R.id.report_real_video).setVisibility(View.GONE);
		findViewById(R.id.report_report_btn_clean).setOnClickListener(this);
	}

	private void refreshListOrEmpty()
	{
		if (AirReportManager.getInstance().getReports().size() == 0)
		{
			talk_report_list_panel.setVisibility(View.GONE);
			talk_report_empty.setVisibility(View.VISIBLE);
		}
		else
		{
			
			talk_report_list_panel.setVisibility(View.VISIBLE);
			talk_report_empty.setVisibility(View.GONE);
			AirReport  currentReport = AirReportManager.getInstance().getCurrentReportDoing();
			if (currentReport != null )
			{
				if(currentReport.getState() != AirReport.STATE_UPLOADING)
				adapterReport.notifyDataSetChanged();
			}
			else
				adapterReport.notifyDataSetChanged();
		}
	}

	public static String sizeMKB(int size)
	{
		String str = "";
		if (size >= 1024 && size < 1024 * 1024)
		{
			str = (size / 1024) + "K";
		}
		else if (size >= 1024 * 1024)
		{
			str = (size / 1024 / 1024) + ".";
			if (size % 1024 >= 900)
			{
				str += "9M";
			}
			else
			{
				str += (size % 1024) / 100 + "M";
			}
		}
		else
		{
			str = size + "B";
		}
		return str;
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
			case R.id.report_report_btn_pic:
			{
				Intent it = new Intent(this, MenuReportAsPicActivity.class);
				if (!TextUtils.isEmpty(taskId))
					it.putExtra("taskId", taskId);
				if (!TextUtils.isEmpty(taskName))
					it.putExtra("taskName", taskName);
				it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(it);
				break;
			}
			case R.id.report_report_btn_vid:
			{
				Intent it = new Intent(this, MenuReportAsVidActivity.class);
				if (!TextUtils.isEmpty(taskId))
					it.putExtra("taskId", taskId);
				if (!TextUtils.isEmpty(taskName))
					it.putExtra("taskName", taskName);
				it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(it);
				break;
			}
			case R.id.report_report_btn_clean:
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(getString(R.string.talk_report_upload_cleanall));
				builder.setMessage(getString(R.string.talk_report_upload_cleanall_tip));
				builder.setPositiveButton(getString(R.string.talk_ok), new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int which)
					{
						AirReportManager.getInstance().ReportClean();
					}
				});
				builder.setNegativeButton(this.getString(R.string.talk_no), null);
				builder.show();
				break;
			}
			case R.id.report_real_video:
			{
				if (AirtalkeeAccount.getInstance().isAccountRunning())
				{
					if (AirtalkeeAccount.getInstance().isEngineRunning())
					{
						AirSession session = SessionController.SessionMatchSpecial(AirtalkeeSessionManager.SPECIAL_NUMBER_DISPATCHER,
							getString(R.string.talk_tools_call_center));
						
						Intent intent = new Intent();
						intent.setClass(this, TempSessionActivity.class);
						intent.putExtra("sessionCode", session.getSessionCode());
						intent.putExtra("type", AirServices.TEMP_SESSION_TYPE_OUTGOING);
						intent.putExtra("video", true);
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						this.startActivity(intent);
					}
					else
					{
						Util.Toast(this, getString(R.string.talk_network_warning));
					}
				}
				break;
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	{
		// TODO Auto-generated method stub
		AirReport report = (AirReport) adapterReport.getItem(position - 1);
		if (report != null)
		{
			Intent it = new Intent(this, MenuReportViewActivity.class);
			it.putExtra("code", report.getCode());
			startActivity(it);
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
	{
		// TODO Auto-generated method stub
		if (parent.getId() == R.id.talk_report_list)
		{
			final Activity activity = this;
			final String menuArray[] = getResources().getStringArray(R.array.handle_report);
			final ListAdapter items = mSimpleAdapter(this, menuArray, R.layout.account_switch_listitem, R.id.AccountNameView);
			final AirReport report = (AirReport) adapterReport.getItem(position - 1);
			if (report != null)
			{
				new AlertDialog.Builder(this).setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int whichButton)
					{
						switch (whichButton)
						{
							case 0:// TODO:�鿴
							{
								Intent it = new Intent(activity, MenuReportViewActivity.class);
								it.putExtra("code", report.getCode());
								startActivity(it);
								break;
							}
							case 1:// TODO:ɾ��
							{
								AirReportManager.getInstance().ReportDelete(report.getCode());
								break;
							}
						}
						if (dialog != null)
							dialog.dismiss();
					}
				}).setOnCancelListener(new OnCancelListener()
				{
					@Override
					public void onCancel(DialogInterface dialog)
					{
						// TODO Auto-generated method stub
						if (dialog != null)
							dialog.cancel();
					}
				}).show();
			}
		}
		return true;
	}

	public SimpleAdapter mSimpleAdapter(Context contexts, String[] array, int layout, int id)
	{
		if (array == null)
			return null;
		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		data.clear();
		for (int i = 0; i < array.length; i++)
		{
			Map<String, Object> listItem = new HashMap<String, Object>();
			listItem.put("accountName", array[i]);
			data.add(listItem);
		}
		return new SimpleAdapter(this, data, layout, new String[] { "accountName" }, new int[] { id });
	}

	@Override
	public void onMmiReportResourceListRefresh()
	{
		// TODO Auto-generated method stub
		refreshListOrEmpty();
	}

	@Override
	public void onMmiReportDel()
	{
		// TODO Auto-generated method stub
		if(adapterReport != null)
			adapterReport.notifyDataSetChanged();
	}

}
