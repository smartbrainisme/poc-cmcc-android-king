package com.cmccpoc.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.airtalkee.sdk.entity.AirTask;
import com.airtalkee.sdk.entity.AirTaskReport;
import com.cmccpoc.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

public class AdapterTaskReport extends BaseAdapter 
{
	private Context context = null;
	protected ImageLoader imageLoader = ImageLoader.getInstance();
	private AirTask task;

	DisplayImageOptions options = new DisplayImageOptions.Builder().showImageForEmptyUri(R.drawable.msg_image).showImageOnFail(R.drawable.msg_image).resetViewBeforeLoading(true)
		.cacheOnDisc(true).imageScaleType(ImageScaleType.EXACTLY).bitmapConfig(Bitmap.Config.RGB_565).considerExifParams(true).displayer(new FadeInBitmapDisplayer(0)).build();
	public AdapterTaskReport(AirTask task,Context context)
	{
		this.task = task;
		this.context = context;
	}

	@Override
	public int getCount()
	{
		// TODO Auto-generated method stub
		return  task.getContents().size();
	}

	@Override
	public Object getItem(int position)
	{
		// TODO Auto-generated method stub
		AirTaskReport report = null;
		if (task.getContents().size() > 0)
		{
			report = task.getContents().get(task.getContents().size() - position - 1);
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
		AirTaskReport report = (AirTaskReport) getItem(position);
		if (convertView == null)
		{
			convertView = LayoutInflater.from(context).inflate(R.layout.listitem_task_report, null);
			holder = new ViewHolder();
			holder.ivDisplay = (ImageView) convertView.findViewById(R.id.talk_report_icon);
			holder.time = (TextView) convertView.findViewById(R.id.talk_time);
			holder.desc = (TextView) convertView.findViewById(R.id.talk_desc);
			convertView.setTag(holder);
		}
		else
		{
			holder = (ViewHolder) convertView.getTag();
		}
		if (report != null)
		{
			imageLoader.displayImage(report.getUrl(), holder.ivDisplay);
			holder.time.setText(report.getTime());
			holder.desc.setText(report.getDesc()+"");
		}
		return convertView;
	}

	class ViewHolder
	{
		ImageView ivDisplay;
		TextView time;
		TextView desc;
	}
	
}
