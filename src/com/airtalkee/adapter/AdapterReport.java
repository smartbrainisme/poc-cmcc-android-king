package com.airtalkee.adapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.airtalkee.R;
import com.airtalkee.activity.MenuReportActivity;
import com.airtalkee.control.AirReportManager;
import com.airtalkee.entity.AirReport;
import com.airtalkee.sdk.AirtalkeeReport;
import com.airtalkee.sdk.util.Utils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

public class AdapterReport extends BaseAdapter implements OnClickListener
{
	private Context context = null;
	private List<AirReport> reports = null;
//	private boolean isShowIcons = true;
	Map<String, RelativeLayout> viewMap = new HashMap<String, RelativeLayout>();
	protected ImageLoader imageLoader = ImageLoader.getInstance();

	DisplayImageOptions options = new DisplayImageOptions.Builder().showImageForEmptyUri(R.drawable.report_default_vid).showImageOnFail(R.drawable.report_default_vid).resetViewBeforeLoading(true)
		.cacheOnDisc(true).imageScaleType(ImageScaleType.EXACTLY).bitmapConfig(Bitmap.Config.RGB_565).considerExifParams(true).displayer(new FadeInBitmapDisplayer(0)).build();
	ListView lv;

	public AdapterReport(Context _context, ListView lv)
	{
		this.lv = lv;
		context = _context;
		reports = AirReportManager.getInstance().getReports();
	}

	
	@Override
	public int getCount()
	{
		// TODO Auto-generated method stub
		return reports.size();
	}

	@Override
	public Object getItem(int position)
	{
		// TODO Auto-generated method stub
		AirReport report = null;
		if (reports.size() > 0)
		{
			report = reports.get(reports.size() - position - 1);
		}
		return report;
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
		AirReport report = (AirReport) getItem(position);
		if (convertView == null)
		{
			convertView = LayoutInflater.from(context).inflate(R.layout.listitem_report, null);
			holder = new ViewHolder();
			holder.icon = (ImageView) convertView.findViewById(R.id.talk_report_icon);
			holder.task = (TextView) convertView.findViewById(R.id.talk_report_task);
			holder.time = (TextView) convertView.findViewById(R.id.talk_report_time);
			holder.size = (TextView) convertView.findViewById(R.id.talk_report_size);
			holder.detail = (TextView) convertView.findViewById(R.id.talk_report_detail);
			holder.progressLayout = (RelativeLayout) convertView.findViewById(R.id.talk_report_progress_layout);
			holder.progressBar = (ProgressBar) convertView.findViewById(R.id.talk_report_progress);
			holder.progressText = (TextView) convertView.findViewById(R.id.talk_report_progress_text);
			holder.state = (ImageView) convertView.findViewById(R.id.talk_report_state);
			holder.stateRetry = (Button) convertView.findViewById(R.id.talk_report_btn_retry);
			convertView.setTag(holder);
		}
		else
		{
			holder = (ViewHolder) convertView.getTag();
		}
		
		
		if (report != null)
		{
			viewMap.put(report.getCode(), holder.progressLayout);
			if (report.getType() == AirtalkeeReport.RESOURCE_TYPE_VIDEO)
				imageLoader.displayImage(null, holder.icon);
			else
				imageLoader.displayImage(report.getResUri().toString(), holder.icon);
			
			holder.time.setText(report.getTime());
			holder.size.setText(MenuReportActivity.sizeMKB(report.getResSize()));
			
			if (report.getTarget() == AirReport.TARGET_TASK_DISPATCH)
			{
				holder.task.setText(report.getTaskName());
				holder.task.setVisibility(View.VISIBLE);
			}
			else
				holder.task.setVisibility(View.GONE);

			if (!Utils.isEmpty(report.getResContent()))
			{
				holder.detail.setText(report.getResContent());
			}
			else
			{
				holder.detail.setText(context.getString(R.string.talk_report_upload_no_content));
			}

			switch (report.getState())
			{
				case AirReport.STATE_WAITING:
				{
					holder.state.setImageResource(R.drawable.report_state_waiting);
					holder.stateRetry.setVisibility(View.GONE);
					holder.detail.setVisibility(View.VISIBLE);
					holder.progressLayout.setVisibility(View.GONE);
					break;
				}
				case AirReport.STATE_UPLOADING:
				{
					holder.state.setImageResource(R.drawable.report_state_uploading);
					holder.stateRetry.setVisibility(View.GONE);
//					holder.detail.setVisibility(View.GONE);
//					holder.progressLayout.setVisibility(View.VISIBLE);
//					holder.progressBar.setProgress(report.getProgress());
//					holder.progressText.setText(report.getProgress() + "%");
					break;
				}
				case AirReport.STATE_RESULT_OK:
				{
					holder.state.setImageResource(R.drawable.report_state_ok);
					holder.stateRetry.setVisibility(View.GONE);
					holder.detail.setVisibility(View.VISIBLE);
					holder.progressLayout.setVisibility(View.GONE);
					break;
				}
				case AirReport.STATE_RESULT_FAIL:
				{
					holder.state.setImageResource(R.drawable.report_state_error);
					holder.stateRetry.setVisibility(View.VISIBLE);
					holder.stateRetry.setOnClickListener(this);
					holder.stateRetry.setTag(report.getCode());
					holder.detail.setVisibility(View.VISIBLE);
					holder.progressLayout.setVisibility(View.GONE);
					break;
				}
			}
		}
		return convertView;
	}

	class ViewHolder
	{
		ImageView icon;
		TextView task;
		TextView time;
		TextView size;
		TextView detail;
		RelativeLayout progressLayout;
		ProgressBar progressBar;
		TextView progressText;
		ImageView state;
		Button stateRetry;
	}

	@Override
	public void onClick(View v)
	{
		// TODO Auto-generated method stub
		switch (v.getId())
		{
			case R.id.talk_report_btn_retry:
			{
				AirReportManager.getInstance().ReportRetry((String) v.getTag());
				break;
			}
			default:
				break;
		}
	}

	public void refreshProgress(AirReport report)
	{
		RelativeLayout layout = viewMap.get(report.getCode());
		if (layout != null)
		{
			switch (report.getState())
			{
				case AirReport.STATE_UPLOADING:
				{
					ProgressBar process = (ProgressBar)layout.getChildAt(0);
					TextView tvProcess = (TextView)layout.getChildAt(1);
					process.setProgress(report.getProgress());
					tvProcess.setText(report.getProgress()+"%");
					layout.setVisibility(View.VISIBLE);
					break;
				}
				default:
					layout.setVisibility(View.GONE);
					break;

			}
		}
		else
		{
			//notifyDataSetChanged();
		}
	}
}
