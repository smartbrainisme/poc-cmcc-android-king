package com.airtalkee.Util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.widget.SearchView;
import android.widget.TextView;
import com.airtalkee.R;
import com.airtalkee.config.Config;
import com.airtalkee.sdk.util.IOoperate;

public class ThemeUtil
{
	static String key_theme = "key_theme";
	static IOoperate io;
	static int currentTheme = -1;

	public static void setTheme(Activity ac)
	{
		int theme = io.getInt(key_theme, -1);
		if (theme != -1)
		{
			currentTheme = theme;
		}
		else
		{
			currentTheme = Config.defaultTheme;
		}
		ac.setTheme(currentTheme);
	}

	public static void changeTheme(Activity ac)
	{
		if (currentTheme != Config.defaultTheme)
		{
			currentTheme = Config.defaultTheme;
		}
		else
		{
			currentTheme = R.style.AppTheme_Light;
		}
		io.putInt(key_theme, currentTheme);

		if (ac != null)
		{
			ac.finish();
			Intent it = new Intent(ac, ac.getClass());
			ac.startActivity(it);
		}
	}

	static
	{
		io = new IOoperate();
	}

	public static int getColor(Context context, int attr)
	{
		int[] attrs = new int[] { attr };
		TypedArray ta = context.obtainStyledAttributes(attrs);
		int color = ta.getColor(0, 430);
		ta.recycle();
		return color;
	}

	public static Drawable getDrawable(int attr, Context context)
	{
		int[] attrs = new int[] { attr };
		TypedArray ta = context.obtainStyledAttributes(attrs);
		Drawable drawable = ta.getDrawable(0);
		ta.recycle();
		return drawable;

	}

	public static int getResourceId(int attr, Context context)
	{
		int[] attrs = new int[] { attr };
		TypedArray ta = context.obtainStyledAttributes(attrs);
		int id = ta.getResourceId(0, 430);
		ta.recycle();
		return id;
	}

	public static int getDimensionPixelSize(Activity activity, int attr, int defaultValue)
	{
		int[] attrs = new int[] { attr };
		TypedArray ta = activity.obtainStyledAttributes(attrs);
		int value = ta.getDimensionPixelSize(0, defaultValue);
		ta.recycle();
		return value;
	}

	//can't find a public theme attr to modify actionbar searchview text color
	public static void customActionBarSearchViewTextColor(SearchView searchView)
	{
		int id = searchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
		TextView textView = (TextView) searchView.findViewById(id);
		textView.setTextColor(Color.WHITE);
	}

}
