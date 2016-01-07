package com.airtalkee.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import com.airtalkee.R;
import com.airtalkee.Util.ThemeUtil;
import com.airtalkee.Util.Toast;
import com.airtalkee.adapter.AdapterReport;
import com.airtalkee.adapter.AdapterReport.onReportCheckedListener;
import com.airtalkee.config.Config;
import com.airtalkee.control.AirReportManager;
import com.airtalkee.entity.AirReport;
import com.airtalkee.listener.OnMmiReportListener;
import com.airtalkee.widget.MListView;

public class MenuReportActivity extends ActivityBase implements
		OnClickListener, OnMmiReportListener, OnItemClickListener,
		onReportCheckedListener, OnCheckedChangeListener
{

	private AdapterReport adapterReport;
	private MListView lvReportList;
	private View talk_report_list_panel, talk_report_empty;
	private ImageView ivRight;
	private RelativeLayout reportDelPanel;
	private LinearLayout btReportDel;
	private TextView tvReportTip;
	private CheckBox cbSelectAll;
	private Map<String, AirReport> isSelected = new HashMap<String, AirReport>();

	private boolean isEditing = false;

	@Override
	protected void onCreate(Bundle bundle)
	{
		// TODO Auto-generated method stub
		super.onCreate(bundle);
		setRequestedOrientation(Config.screenOrientation);
		setContentView(R.layout.activity_tool_report);
		AirReportManager.getInstance().loadReports();
		doInitView();

		// bundle = getIntent().getExtras();
		// if (bundle != null)
		// {
		// taskId = bundle.getString("taskId");
		// taskName = bundle.getString("taskName");
		// }
	}

	private void putSelected(String code, AirReport value, boolean isCheck)
	{
		if (isCheck)
		{
			isSelected.put(code, value);
			btReportDel.setClickable(true);
			btReportDel.setBackgroundResource(R.drawable.bg_report_red);
		}
		else if (isSelected.size() > 0)
		{
			isSelected.remove(code);
			btReportDel.setClickable(true);
			btReportDel.setBackgroundResource(R.drawable.bg_report_red);
		}
		else
		{
			btReportDel.setClickable(false);
			btReportDel.setBackgroundResource(R.drawable.bg_report_gray);
		}
		tvReportTip.setText("已选择" + isSelected.size() + "条记录");
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
		if (adapterReport != null)
			adapterReport.notifyDataSetChanged();
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
		ivRight = (ImageView) findViewById(R.id.bottom_right_icon);
		ivRight.setImageResource(ThemeUtil.getResourceId(R.attr.theme_ic_topbar_setting, this));
		ivRightLay.setOnClickListener(this);

		talk_report_list_panel = findViewById(R.id.talk_report_list_panel);
		talk_report_empty = findViewById(R.id.talk_report_empty);
		lvReportList = (MListView) findViewById(R.id.talk_report_list);
		adapterReport = new AdapterReport(this, lvReportList, this);

		lvReportList.setAdapter(adapterReport);
		lvReportList.setOnItemClickListener(this);

		reportDelPanel = (RelativeLayout) findViewById(R.id.rl_report_panel);
		btReportDel = (LinearLayout) findViewById(R.id.bt_report_del);
		btReportDel.setOnClickListener(this);
		tvReportTip = (TextView) findViewById(R.id.tv_report_select_count);
		tvReportTip.setText("已选择0条记录");
		btReportDel.setClickable(false);
		cbSelectAll = (CheckBox) findViewById(R.id.cb_report_selectall);
		cbSelectAll.setOnCheckedChangeListener(this);
		adapterReport.notifyDataSetChanged();
		// lvReportList.setOnItemLongClickListener(this);
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
			AirReport currentReport = AirReportManager.getInstance().getCurrentReportDoing();
			if (currentReport != null)
			{
				if (currentReport.getState() != AirReport.STATE_UPLOADING)
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
			case R.id.talk_menu_right_button:
			{
				if (lvReportList.getCount() > 0)
				{
					isEditing = !isEditing;
					if (isEditing)
					{
						ivRight.setImageResource(ThemeUtil.getResourceId(R.attr.theme_ic_topbar_clean, this));
						reportDelPanel.setVisibility(View.VISIBLE);
						adapterReport.setEditing(true);
						lvReportList.setClickable(false);
						tvReportTip.setText("已选择0条记录");
						btReportDel.setBackgroundResource(R.drawable.bg_report_gray);
						// rootPanel.setBackgroundResource(R.attr.theme_sider_title_bg_report);
					}
					else
					{
						ivRight.setImageResource(ThemeUtil.getResourceId(R.attr.theme_ic_topbar_setting, this));
						reportDelPanel.setVisibility(View.GONE);
						adapterReport.setEditing(false);
						lvReportList.setClickable(true);
						cbSelectAll.setChecked(false);
						// rootPanel.setBackgroundResource(R.attr.theme_sider_title_bg);
					}
					refreshListOrEmpty();
				}
				break;
			}
			case R.id.bt_report_del:
			{
				AirReportManager.getInstance().ReportsDetele(isSelected);
				tvReportTip.setText("已选择0条记录");
				btReportDel.setClickable(false);
				btReportDel.setBackgroundResource(R.drawable.bg_report_gray);
				isSelected.clear();
				Toast.makeText1(this, "已删除", Toast.LENGTH_LONG).show();
				ivRight.setImageResource(ThemeUtil.getResourceId(R.attr.theme_ic_topbar_setting, this));
				reportDelPanel.setVisibility(View.GONE);
				adapterReport.setEditing(false);
				lvReportList.setClickable(true);
				cbSelectAll.setChecked(false);
//				refreshListOrEmpty();
				break;
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	{
		AirReport report = (AirReport) adapterReport.getItem(position - 1);
		if (report != null)
		{
			if (isEditing)
			{
				CheckBox cb = (CheckBox) view.findViewById(R.id.cb_report);
				if (cb != null)
				{
					cb.setChecked(!cb.isChecked());
				}
			}
			else
			{
				Intent it = new Intent(this, MenuReportViewActivity.class);
				it.putExtra("code", report.getCode());
				startActivity(it);
			}
		}
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
		if (adapterReport != null)
			refreshListOrEmpty();
		// adapterReport.notifyDataSetChanged();
	}

	@Override
	public void onReportChecked(boolean isChecked, AirReport report)
	{
		putSelected(report.getCode(), report, isChecked);
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
	{
		adapterReport.setCheckedAll(isChecked);
		refreshListOrEmpty();
	}

	@Override
	public void onMmiReportProgress(int progress)
	{
		// TODO Auto-generated method stub
		
	}
}
