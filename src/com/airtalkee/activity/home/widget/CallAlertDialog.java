package com.airtalkee.activity.home.widget;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.airtalkee.R;

public class CallAlertDialog extends AlertDialog implements
		android.view.View.OnClickListener {
	boolean isCall = false;

	public CallAlertDialog(Context context, String title, String content,
			boolean isCall, DialogListener listener, int id) {
		super(context, title, content, "", "", listener, id);
		this.isCall = isCall;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

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

		s.setVisibility(isCall ? View.GONE : View.VISIBLE);

		tvTitle.setText(title);
		tvContent.setText(content);
	}

}
