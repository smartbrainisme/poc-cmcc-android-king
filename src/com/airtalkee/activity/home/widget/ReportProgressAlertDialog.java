package com.airtalkee.activity.home.widget;

import com.airtalkee.R;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ReportProgressAlertDialog extends Dialog implements OnClickListener
{
	private ProgressBar reportBar;
	private TextView tvFileSize, tvFileProgress;
	private Button cancel;
	private Context context;

	private String fileSize;

	public ReportProgressAlertDialog(Context context, String fileSize)
	{
		super(context, R.style.alert_dialog);
		this.fileSize = fileSize;
		this.context = context;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
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
