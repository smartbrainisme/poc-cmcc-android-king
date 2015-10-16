package com.airtalkee.widget;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.RelativeLayout;

public class MyRelativeLayout extends RelativeLayout
{
	private WindowManager manager = null;

	public MyRelativeLayout(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	private OnResizeListener mListener;

	public interface OnResizeListener
	{
		void OnSoftKeyboardChanged(boolean isSoftKeyboardShown);
	}

	public void setOnResizeListener(OnResizeListener l)
	{
		mListener = l;
		manager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		super.onSizeChanged(w, h, oldw, oldh);
		int orientation = 0;
		if (manager != null)
		{
			Display d = manager.getDefaultDisplay();
			orientation = d.getOrientation();
		}
		Log.i("m", String.format("currentHeight =[%d], oldHeight=[%d],current -  old=[%d] orientation=[%d]", h, oldh, Math.abs(h - oldh), orientation));
		boolean isShown = (h < oldh && orientation == 0);
		if (mListener != null && Math.abs(h - oldh) > 100)
		{
			Message msg = handler.obtainMessage(0, isShown ? 0 : 1, 0);
			handler.sendMessage(msg);
		}

	}

	Handler handler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			mListener.OnSoftKeyboardChanged(msg.arg1 == 0);
		}

	};

}
