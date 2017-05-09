package com.cmccpoc.activity.home.adapter;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;
import com.airtalkee.sdk.AirtalkeeReport;
import com.airtalkee.sdk.util.Log;
import com.airtalkee.sdk.util.Utils;
import com.cmccpoc.R;
import com.cmccpoc.Util.Toast;
import com.cmccpoc.control.AirReportManager;
import com.cmccpoc.entity.AirReport;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

/**
 * 上报记录适配器
 * @author Yao
 */
public class AdapterReport extends BaseAdapter implements OnClickListener
{
	private Context context = null;
	private List<AirReport> reports = null;
	Map<String, RelativeLayout> viewMap = new HashMap<String, RelativeLayout>();
	protected ImageLoader imageLoader = ImageLoader.getInstance();
	private onReportCheckedListener checkedListener;

	private boolean isEditing = false;
	private boolean isCheckedAll = false;

	public boolean isEditing()
	{
		return isEditing;
	}

	public boolean isCheckedAll()
	{
		return isCheckedAll;
	}

	public void setCheckedAll(boolean isCheckedAll)
	{
		this.isCheckedAll = isCheckedAll;
	}

	public void setEditing(boolean isEditing)
	{
		this.isEditing = isEditing;
	}

	DisplayImageOptions options = new DisplayImageOptions.Builder().showImageForEmptyUri(R.drawable.report_default_vid).showImageOnFail(R.drawable.report_default_vid).resetViewBeforeLoading(true).cacheOnDisc(true).imageScaleType(ImageScaleType.EXACTLY).bitmapConfig(Bitmap.Config.RGB_565).considerExifParams(true).displayer(new FadeInBitmapDisplayer(0)).build();
	ListView lv;

	public interface onReportCheckedListener
	{
		public void onReportChecked(boolean isChecked, AirReport report);
	}

	public AdapterReport(Context _context, ListView lv)
	{
		this.lv = lv;
		context = _context;
		reports = AirReportManager.getInstance().getReports();
	}

	public AdapterReport(Context _context, ListView lv, onReportCheckedListener listener)
	{
		this(_context, lv);
		this.checkedListener = listener;
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
		ViewHolder holder = null;
		AirReport report = (AirReport) getItem(position);
		if (convertView == null)
		{
			convertView = LayoutInflater.from(context).inflate(R.layout.listitem_report, null);
			holder = new ViewHolder();
			holder.icon = (ImageView) convertView.findViewById(R.id.talk_report_icon);
			holder.video = (VideoView) convertView.findViewById(R.id.talk_report_video);
			holder.play = (ImageView) convertView.findViewById(R.id.talk_report_play);
			holder.task = (TextView) convertView.findViewById(R.id.talk_report_task);
			holder.time = (TextView) convertView.findViewById(R.id.talk_report_time);
			holder.detail = (TextView) convertView.findViewById(R.id.talk_report_detail);
			holder.progressLayout = (RelativeLayout) convertView.findViewById(R.id.talk_report_progress_layout);
			holder.progressBar = (ProgressBar) convertView.findViewById(R.id.talk_report_progress);
			holder.progressText = (TextView) convertView.findViewById(R.id.talk_report_progress_text);
			// 重发按钮图片
			holder.stateRetry = (ImageView) convertView.findViewById(R.id.talk_report_retry);

			// 上传失败
			holder.failText = (TextView) convertView.findViewById(R.id.talk_report_fail_message);
			// 点击重发 等待重发
			holder.uploadStep = (TextView) convertView.findViewById(R.id.talk_report_retry_step);
			holder.cbReport = (CheckBox) convertView.findViewById(R.id.cb_report);
			holder.ivReportEnter = (ImageView) convertView.findViewById(R.id.iv_report_enter);
			holder.retryLayout = (LinearLayout) convertView.findViewById(R.id.talk_report_retry_panel);
			holder.retryLayout.getBackground().setAlpha(200);
			convertView.setTag(holder);
		}
		else
		{
			holder = (ViewHolder) convertView.getTag();
		}
		fillView(report, holder);
		return convertView;
	}

	/**
	 * 填充每个上报记录的View
	 * @param report 上报记录Entity
	 * @param holder 结构
	 */
	private void fillView(final AirReport report, ViewHolder holder)
	{
		if (report != null)
		{
			viewMap.put(report.getCode(), holder.progressLayout);
			holder.video.setVisibility(View.GONE);
			if (report.getType() == AirtalkeeReport.RESOURCE_TYPE_VIDEO)
			{
				holder.icon.setVisibility(View.VISIBLE);
				holder.video.setVideoPath(report.getResPath());
				MediaMetadataRetriever rev = new MediaMetadataRetriever();
				rev.setDataSource(context, Uri.fromFile(new File(report.getResPath())));
				Bitmap bitmap = rev.getFrameAtTime(1 * 1000 * 2000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
				holder.icon.setImageBitmap(bitmap);
			}
			else
			{
				imageLoader.displayImage(report.getResUri().toString(), holder.icon);
				holder.icon.setVisibility(View.VISIBLE);
			}

			holder.time.setText(context.getString(R.string.talk_tools_report_date) + "：" + report.getTime().substring(0, 15));
			// holder.size.setText(MenuReportActivity.sizeMKB(report.getResSize()));

			if (report.getTarget() == AirReport.TARGET_TASK_DISPATCH)
			{
				holder.task.setText(report.getTaskName());
				holder.task.setVisibility(View.VISIBLE);
			}
			else
			{
				holder.task.setVisibility(View.GONE);
			}
			if (!Utils.isEmpty(report.getResContent()))
			{
				String content = report.getResContent().contains("\r") ? report.getResContent().substring(0, report.getResContent().lastIndexOf('\r')) : context.getString(R.string.talk_report_upload_no_content);
				holder.detail.setText(context.getString(R.string.talk_tools_report_description) + "：" + content);
			}
			else
			{
				holder.detail.setText(context.getString(R.string.talk_tools_report_description) + "：" + context.getString(R.string.talk_report_upload_no_content) + System.getProperty("line.separator", "/n"));
			}
			Log.i(AdapterReport.class, "report state="+report.getState());
			switch (report.getState())
			{
				case AirReport.STATE_WAITING:
				{
					holder.stateRetry.setVisibility(View.GONE);
					holder.detail.setVisibility(View.VISIBLE);
					holder.progressBar.setVisibility(View.VISIBLE);
					holder.uploadStep.setText(context.getString(R.string.talk_tools_report_waiting));
					holder.uploadStep.setVisibility(View.VISIBLE);
					holder.failText.setVisibility(View.VISIBLE);
					holder.retryLayout.setVisibility(View.VISIBLE);
					break;
				}
				case AirReport.STATE_UPLOADING:
				{
					holder.uploadStep.setText(context.getString(R.string.talk_tools_report_uploading));
					holder.progressBar.setVisibility(View.VISIBLE);
					holder.uploadStep.setVisibility(View.VISIBLE);
					holder.failText.setVisibility(View.VISIBLE);
					holder.stateRetry.setVisibility(View.GONE);
					holder.retryLayout.setVisibility(View.VISIBLE);
					break;
				}
				case AirReport.STATE_RESULT_OK:
				{
					holder.detail.setVisibility(View.VISIBLE);
					// holder.progressLayout.setVisibility(View.GONE);
					if (report.getType() == AirtalkeeReport.RESOURCE_TYPE_VIDEO)
					{
						holder.play.setVisibility(View.VISIBLE);
						holder.play.setImageResource(R.drawable.btn_report_play);
					}
					else
					{
						holder.play.setVisibility(View.GONE);
					}
					holder.progressBar.setVisibility(View.GONE);
					holder.stateRetry.setVisibility(View.GONE);
					holder.uploadStep.setVisibility(View.GONE);
					holder.failText.setVisibility(View.GONE);
					holder.retryLayout.setVisibility(View.GONE);
					break;
				}
				case AirReport.STATE_RESULT_FAIL:
				{
					holder.stateRetry.setImageResource(R.drawable.selector_report_retry);
					holder.stateRetry.setVisibility(View.VISIBLE);
					holder.stateRetry.setOnClickListener(this);
					holder.stateRetry.setTag(report.getCode());
					holder.failText.setVisibility(View.VISIBLE);
					holder.uploadStep.setText(context.getString(R.string.talk_tools_report_click));
					holder.uploadStep.setVisibility(View.VISIBLE);
					holder.progressBar.setVisibility(View.GONE);
					holder.retryLayout.setVisibility(View.VISIBLE);
					break;
				}
			}
			if (isEditing)
			{
				holder.cbReport.setVisibility(View.VISIBLE);
				holder.ivReportEnter.setVisibility(View.GONE);
				holder.cbReport.setOnCheckedChangeListener(new OnCheckedChangeListener()
				{
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
					{
						// TODO Auto-generated method stub
						if (checkedListener != null)
							checkedListener.onReportChecked(isChecked, report);
					}
				});
			}
			else
			{
				holder.cbReport.setVisibility(View.GONE);
				holder.ivReportEnter.setVisibility(View.VISIBLE);
			}
			if (isCheckedAll)
			{
				holder.cbReport.setChecked(true);
			}
			else
			{
				holder.cbReport.setChecked(false);
			}
		}
	}

	class ViewHolder
	{
		ImageView icon;
		ImageView play;
		TextView task;
		TextView time;
		TextView size;
		TextView detail;
		RelativeLayout progressLayout;
		ProgressBar progressBar;
		TextView progressText;
		ImageView state;
		ImageView stateRetry;
		TextView failText;
		TextView uploadStep;
		CheckBox cbReport;
		ImageView ivReportEnter;
		VideoView video;
		LinearLayout retryLayout;
	}

	@Override
	public void onClick(View v)
	{
		// TODO Auto-generated method stub
		switch (v.getId())
		{
			case R.id.talk_report_retry:
			{
				List<AirReport> reports = AirReportManager.getInstance().getReports();
				if(reports != null && reports.size() > 0)
				{
					for (int i = 0; i < reports.size(); i++)
					{
						if(reports.get(i).getState() == AirReport.STATE_UPLOADING)
						{
							Toast.makeText1(context, "当前有文件正在上报中，请完成后再继续", Toast.LENGTH_LONG).show();
							return;
						}
					}
				}
				AirReportManager.getInstance().setReportDoing(null);
				AirReportManager.getInstance().ReportRetry((String) v.getTag());
				notifyDataSetChanged();
				break;
			}

			default:
				break;
		}
	}

	/**
	 * 刷新上报进度条
	 * @param report 上报记录Entity
	 */
	public void refreshProgress(AirReport report)
	{
		RelativeLayout layout = viewMap.get(report.getCode());
		if (layout != null)
		{
			switch (report.getState())
			{
				case AirReport.STATE_UPLOADING:
				{
					ProgressBar process = (ProgressBar) layout.getChildAt(0);
					TextView tvProcess = (TextView) layout.getChildAt(1);
					process.setProgress(report.getProgress());
					tvProcess.setText(report.getProgress() + "%");
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
			// notifyDataSetChanged();
		}
	}
}
