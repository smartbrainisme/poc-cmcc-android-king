package com.cmccpoc.widget;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.RelativeLayout;

public class MyRelativeLayout extends RelativeLayout
{
	
	public static String ACTION_ON_VIEW_RESIZE="action_on_view_resize";
	public static String EXTRA_IS_SOFTKEYBOARD_SHOWN="Extra_isSoftKeyboardShown";
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
		if (Math.abs(h - oldh) > 100)
		{ 
			notifyOnSizeChange(isShown);
			if(mListener != null)
			{
				Message msg = handler.obtainMessage(0, isShown ? 0 : 1, 0);
				handler.sendMessage(msg);
			}
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
	
	private void notifyOnSizeChange(boolean isShow){
		final Intent intent = new Intent();
		intent.setAction(ACTION_ON_VIEW_RESIZE);
		intent.putExtra(EXTRA_IS_SOFTKEYBOARD_SHOWN, isShow);
		
		getContext().sendBroadcast(intent);
	}

}
