package com.cmccpoc.activity.home.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.cmccpoc.R;

/**
 * 视频、图片上报时，弹出的进度条窗口
 * @author Yao
 */
public class ReportProgressAlertDialog extends Dialog implements OnClickListener
{
	private ProgressBar reportBar;
	private TextView tvFileSize, tvFileProgress;
	private Button cancel;

	private String fileSize;

	public ReportProgressAlertDialog(Context context, String fileSize)
	{
		super(context, R.style.alert_dialog);
		this.fileSize = fileSize;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_alert_report);
		reportBar = (ProgressBar) findViewById(R.id.report_progress);
		reportBar.setMax(100);
		tvFileSize = (TextView) findViewById(R.id.tv_file_size);
		tvFileSize.setText(fileSize);

		tvFileProgress = (TextView) findViewById(R.id.tv_file_progress);
		cancel = (Button) findViewById(R.id.report_back);
		cancel.setOnClickListener(this);
	}

	/**
	 * 设置进度值
	 * @param progress 进度值
	 */
	public void setFileProgress(int progress)
	{
		tvFileProgress.setText("进度 " + progress + "%");
		reportBar.setProgress(progress);
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
			case R.id.report_back:
			{
				this.cancel();
				break;
			}
			default:
				break;
		}
	}

}
