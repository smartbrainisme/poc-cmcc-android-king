package com.airtalkee.activity;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airtalkee.R;
import com.airtalkee.Util.ThemeUtil;
import com.airtalkee.Util.Util;
import com.airtalkee.config.Config;
import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.OnSystemDefectListener;
import com.airtalkee.sdk.util.Utils;

public class MenuDefectReportActivity extends ActivityBase implements OnClickListener, OnSystemDefectListener
{

	private EditText mDefectContent;
	private ProgressBar mDefectWait;
	private Button mDefectButton;

	@Override
	protected void onCreate(Bundle bundle)
	{
		// TODO Auto-generated method stub
		super.onCreate(bundle);
		setRequestedOrientation(Config.screenOrientation);
		setContentView(R.layout.activity_tool_defect_report);
		doInitView();
		AirtalkeeAccount.getInstance().setOnSystemDefectListener(this);
	}

	@Override
	protected void onStart()
	{
		// TODO Auto-generated method stub
		super.onStart();
	}

	private void doInitView()
	{
		TextView ivTitle = (TextView) findViewById(R.id.tv_main_title);
		ivTitle.setText(R.string.talk_tools_defect);
		View btnLeft = findViewById(R.id.menu_left_button);
		ImageView ivLeft = (ImageView) findViewById(R.id.bottom_left_icon);
		ivLeft.setImageResource(ThemeUtil.getResourceId(R.attr.theme_ic_topbar_back, this) );
		btnLeft.setOnClickListener(this);

		RelativeLayout ivRightLay = (RelativeLayout) findViewById(R.id.talk_menu_right_button);
		ImageView ivRight = (ImageView) findViewById(R.id.bottom_right_icon);
		ivRight.setVisibility(View.GONE);
		ivRightLay.setVisibility(View.INVISIBLE);

		mDefectContent = (EditText) findViewById(R.id.defect_report_content);
		mDefectWait = (ProgressBar) findViewById(R.id.defect_report_progress);
		mDefectButton = (Button) findViewById(R.id.defect_report_post);
		mDefectButton.setOnClickListener(this);
	}

	@Override
	public void finish()
	{
		// TODO Auto-generated method stub
		super.finish();
		AirtalkeeAccount.getInstance().setOnSystemDefectListener(null);
	}

	@Override
	public void onClick(View v)
	{
		// TODO Auto-generated method stub
		switch (v.getId())
		{
			case R.id.menu_left_button:
			case R.id.bottom_left_icon:
			{
				finish();
				break;
			}
			case R.id.defect_report_post:
			{
				if (!Utils.isEmpty(mDefectContent.getText().toString()))
				{
					mDefectWait.setVisibility(View.VISIBLE);
					mDefectButton.setClickable(false);
					AirtalkeeAccount.getInstance().SystemDefectReport(mDefectContent.getText().toString());
				}
				break;
			}
		}
	}

	@Override
	public void onSystemDefectReport(boolean isOk)
	{
		// TODO Auto-generated method stub
		mDefectWait.setVisibility(View.GONE);
		mDefectButton.setClickable(true);
		if (isOk)
		{
			Util.Toast(this, getString(R.string.talk_tools_defect_report_tip));
			finish();
		}
		else
		{
			Util.Toast(this, getString(R.string.talk_tools_defect_report_error));
		}
	}

}
