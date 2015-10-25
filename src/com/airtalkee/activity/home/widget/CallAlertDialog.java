package com.airtalkee.activity.home.widget;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.airtalkee.R;

public class CallAlertDialog extends AlertDialog implements
		android.view.View.OnClickListener {

	public CallAlertDialog(Context context, String title, String content,
			String textcancle, String textSure, DialogListener listener, int id) {
		super(context, title, content, textcancle, textSure, listener, id);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_call_receiver_layout);
		initView();
		fillView();
	}

	protected void fillView() {

		if (TextUtils.isEmpty(title)) {
			tvTitle.setVisibility(View.GONE);
		}

		if (TextUtils.isEmpty(content)) {
			tvContent.setVisibility(View.GONE);
		}

		cancle.setText("");
		sure.setText("");
		if (TextUtils.isEmpty(textSure)) {
			sure.setVisibility(View.GONE);
		}
		if (TextUtils.isEmpty(textcancle)) {
			cancle.setVisibility(View.GONE);
		}

		tvTitle.setText(title);
		tvContent.setText(content);
		tvContent.setTextSize(tvContentSize);

	}

}
