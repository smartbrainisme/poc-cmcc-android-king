package com.airtalkee.activity.home.widget;

import android.app.Dialog;
import android.content.Context;
import android.nfc.cardemulation.OffHostApduService;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.airtalkee.R;

public class AlertDialog extends Dialog implements
		android.view.View.OnClickListener {

	protected TextView tvTitle;
	protected TextView tvContent;
	protected Button cancle, sure;
	protected ImageView ivCancle, ivSure;
	//
	protected String title, content;
	protected String textcancle = "取消";
	protected String textSure = "确定";
	protected int id;
	protected int tvContentSize = 16;
	//
	protected DialogListener listener;

	public interface DialogListener {
		void onClickOk(int id);

		void onClickCancel(int id);
	}

	public AlertDialog(Context context, String title, String content,
			DialogListener listener, int id) {
		super(context, R.style.alert_dialog);
		this.title = title;
		this.content = content;
		this.listener = listener;
		this.id = id;
	}

	public AlertDialog(Context context, String title, String content,
			String textcancle, String textSure, DialogListener listener, int id) {
		super(context, R.style.alert_dialog);
		this.title = title;
		this.content = content;
		this.textcancle = textcancle;

		this.textSure = textSure;
		this.listener = listener;
		this.id = id;
	}

	public AlertDialog(Context context, String title, String url,
			String content, String textcancle, String textSure,
			DialogListener listener, int id) {
		super(context, R.style.alert_dialog);
		this.title = title;
		this.content = content;
		this.textcancle = textcancle;

		this.textSure = textSure;
		this.listener = listener;
		this.id = id;
	}

	public AlertDialog(Context context, String title, String url,
			String content, String textcancle, String textSure,
			DialogListener listener, boolean cancelable, int id) {
		super(context, R.style.alert_dialog);
		this.title = title;
		this.content = content;
		this.textcancle = textcancle;

		this.textSure = textSure;
		this.listener = listener;
		this.id = id;
		this.setCancelable(cancelable);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_alert_layout);
		initView();
		fillView();
	}
	View c ;
	View s ;
	protected void initView() {
		
		tvTitle = (TextView) findViewById(R.id.tv_title);
		tvContent = (TextView) findViewById(R.id.tv_content);
		
//		cancle = (Button) findViewById(R.id.cancle);
//		sure = (Button) findViewById(R.id.sure);
		
		 c = findViewById(R.id.cancle);
		 s = findViewById(R.id.sure);
		if(c instanceof Button)
			cancle = (Button)c;
		if(c instanceof Button)
			sure = (Button) s;
		c.setOnClickListener(this);
		s.setOnClickListener(this);
		
	}

	protected void fillView() {

		if (TextUtils.isEmpty(title)) {
			tvTitle.setVisibility(View.GONE);
		}

		if (TextUtils.isEmpty(content)) {
			tvContent.setVisibility(View.GONE);
		}

		if (TextUtils.isEmpty(textSure)) {
			s.setVisibility(View.GONE);
		}
		tvTitle.setText(title);
		tvContent.setText(content);
		if(null != cancle)
		cancle.setText(textcancle);
		if(null != sure)
		sure.setText(textSure);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.sure: {
			this.dismiss();
			if (null != listener)
				listener.onClickOk(this.id);
			break;
		}
		case R.id.cancle: {
			this.dismiss();
			if (null != listener)
				listener.onClickCancel(id);
			break;
		}
		}

	}

}
