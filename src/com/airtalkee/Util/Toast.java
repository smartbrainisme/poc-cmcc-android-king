package com.airtalkee.Util;

import com.airtalkee.R;

import android.content.Context;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


public class Toast extends android.widget.Toast
{
	private static View toastView = null;
	private static TextView toastText = null;
	private static ImageView toastImg = null;
	private static Toast toast = null;
	
	public Toast(Context context)
	{
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public static Toast makeText(Context context,String textString,int duration)
	{
		return make(context, "", textString, duration);
	}
	public static Toast makeText(Context context,int res,int duration)
	{
		return make(context, "", context.getString(res), duration);
	}
	public static Toast makeText(Context context,String name,String textString,int duration)
	{
		return make(context, name, textString, duration);
	}
	private  static Toast make(Context context,String name, String textString,int duration)
	{
		if(context !=null)
		{
			try 
			{
				if(toastView == null || toastText == null)
				{
					toastView = LayoutInflater.from(context).inflate(R.layout.toast_layout, null);
					toastText =(TextView)toastView.findViewById(R.id.toast_text);
					toastImg = (ImageView)toastView.findViewById(R.id.toast_img);
				}
				String build = String.format("<font color=#5aaff3>%s</font> %s", name,textString);
				toastText.setText(Html.fromHtml(build));
				if(toast == null)
				{
					toast =new Toast(context);
					toast.setGravity(Gravity.TOP, 0,Gravity.TOP);
					toast.setMargin(0, 0);
				}
				toast.setDuration(duration);
				toast.setView(toastView);
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		return toast;
	}
	
	

}
