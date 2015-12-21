package com.airtalkee.activity.home.widget;

import android.R.bool;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import com.airtalkee.R;

public class AlertDialog extends Dialog implements
		android.view.View.OnClickListener
{

	protected TextView tvTitle;
	protected TextView tvContent;
	protected Button cancle, sure;
	protected ImageView ivCancle, ivSure;
	protected CheckBox cbRemember;
	protected Context context;
	//
	protected View c;
	protected View s;
	protected String title = null, content = null;
	protected String textcancle = "取消";
	protected String textSure = "确定";
	protected int id;
	protected Object object;
	protected int tvContentSize = 16;
	protected boolean cbVisible = false;
	protected boolean showCancle = true;
	//
	protected DialogListener listener;

	public interface DialogListener
	{
		void onClickOk(int id, Object obj);

		void onClickOk(int id, boolean isChecked);

		void onClickCancel(int id);
	}

	protected void setListener(DialogListener listener)
	{
		this.listener = listener;
	}

	public AlertDialog(Context context)
	{
		super(context, R.style.alert_dialog);
		this.context = context;
	}

	public AlertDialog(Context context, String title, String content, DialogListener listener, int id)
	{
		super(context, R.style.alert_dialog);
		this.title = title;
		this.content = content;
		this.listener = listener;
		this.id = id;
		this.context = context;
	}

	public AlertDialog(Context context, String title, String content, String textSure, boolean cbVisible, DialogListener listener, int id)
	{
		super(context, R.style.alert_dialog);
		this.title = title;
		this.content = content;
		this.textSure = textSure;
		this.cbVisible = cbVisible;
		this.listener = listener;
		this.id = id;
		this.context = context;
	}

	public AlertDialog(Context context, String title, String content, String textcancle, String textSure, DialogListener listener, int id)
	{
		super(context, R.style.alert_dialog);
		this.title = title;
		this.content = content;
		this.textcancle = textcancle;

		this.textSure = textSure;
		this.listener = listener;
		this.id = id;
		this.context = context;
	}

	public AlertDialog(Context context, String title, String content, String textcancle, String textSure, DialogListener listener, int id, Object object)
	{
		super(context, R.style.alert_dialog);
		this.object = object;
		this.title = title;
		this.content = content;
		this.textcancle = textcancle;

		this.textSure = textSure;
		this.listener = listener;
		this.id = id;
		this.context = context;
	}

	public AlertDialog(Context context, String title, String url, String content, String textcancle, String textSure, DialogListener listener, int id)
	{
		super(context, R.style.alert_dialog);
		this.title = title;
		this.content = content;
		this.textcancle = textcancle;

		this.textSure = textSure;
		this.listener = listener;
		this.id = id;
		this.context = context;
	}

	public AlertDialog(Context context, String content, String textSure, DialogListener listener, boolean flag)
	{
		super(context, R.style.alert_dialog);
		this.showCancle = flag;
		this.content = content;
		this.textSure = textSure;
		this.listener = listener;
	}

	public AlertDialog(Context context, String title, String url, String content, String textcancle, String textSure, DialogListener listener, boolean cancelable, int id)
	{
		super(context, R.style.alert_dialog);
		this.title = title;
		this.content = content;
		this.textcancle = textcancle;

		this.textSure = textSure;
		this.listener = listener;
		this.id = id;
		this.setCancelable(cancelable);
		this.context = context;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_alert_layout);
		initView();
		fillView();
	}

	protected void initView()
	{
		tvTitle = (TextView) findViewById(R.id.tv_title);
		tvContent = (TextView) findViewById(R.id.tv_content);
		try
		{
			cbRemember = (CheckBox) findViewById(R.id.cb_remember);
		}
		catch (Exception e)
		{
			cbRemember = new CheckBox(context);
		}

		c = findViewById(R.id.cancle);
		s = findViewById(R.id.sure);
		if (c instanceof Button)
			cancle = (Button) c;
		if (c instanceof Button)
			sure = (Button) s;
		c.setOnClickListener(this);
		s.setOnClickListener(this);

	}

	protected void fillView()
	{
		if (TextUtils.isEmpty(title))
		{
			tvTitle.setVisibility(View.GONE);
		}

		if (TextUtils.isEmpty(content))
		{
			tvContent.setVisibility(View.GONE);
		}

		if (TextUtils.isEmpty(textSure))
		{
			s.setVisibility(View.GONE);
		}
		tvTitle.setText(title);
		tvContent.setText(content);
		if (null != cancle)
			cancle.setText(textcancle);
		if (null != sure)
			sure.setText(textSure);
		if (null != cbRemember)
		{
			if (!cbVisible)
				cbRemember.setVisibility(View.GONE);
		}
		if (!showCancle)
		{
			cancle.setVisibility(View.GONE);
		}
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
			case R.id.sure:
			{
				this.dismiss();
				if (null != listener)
				{
					if (cbVisible)
					{
						listener.onClickOk(this.id, cbRemember.isChecked());
					}
					else
					{
						listener.onClickOk(this.id, object);
					}
				}
				break;
			}
			case R.id.cancle:
			{
				this.cancel();
				if (null != listener)
					listener.onClickCancel(this.id);
				break;
			}
		}

	}

}
