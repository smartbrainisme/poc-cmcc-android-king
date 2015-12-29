package com.airtalkee.Util;

import java.text.SimpleDateFormat;
import java.util.Date;
import android.content.Context;
import android.text.TextUtils;

public class Language
{
	public static final String LANG_CN = "CN";
	public static final String LANG_CN_TW = "TW";
	public static final String LANG_CN_HK = "HK";
	public static final String LAND_EN = "EN";
	public static final String LAND_FR = "FR";

	public static String getLocalLanguage(Context context)
	{
		String ret = LAND_EN;
		String lang = context.getResources().getConfiguration().locale.getLanguage();
		if (TextUtils.equals(lang, "zh"))
		{
			ret = LANG_CN;
		}
		else if (TextUtils.equals(lang, "fr"))
		{
			ret = LAND_FR;
		}
		else if (TextUtils.equals(lang, "en"))
		{
			ret = LAND_EN;
		}
		else
		{
			ret = LAND_EN;
		}
		return ret;
	}

	public static String getLocalLanguageZH(Context context)
	{
		String ret = LAND_EN;
		String lang = context.getResources().getConfiguration().locale.getLanguage();
		if (TextUtils.equals(lang, "zh"))
		{
			if (LANG_CN.equals(context.getResources().getConfiguration().locale.getCountry()))
			{
				ret = LANG_CN;
			}
			else if (LANG_CN_TW.equals(context.getResources().getConfiguration().locale.getCountry()))
			{
				ret = LANG_CN_TW;
			}
			else if (LANG_CN_HK.equals(context.getResources().getConfiguration().locale.getCountry()))
			{
				ret = LANG_CN_HK;
			}
		}
		else if (TextUtils.equals(lang, "fr"))
		{
			ret = LAND_FR;
		}
		else if (TextUtils.equals(lang, "en"))
		{
			ret = LAND_EN;
		}
		else
		{
			ret = LAND_EN;
		}
		return ret;
	}

	public static String convertDate(String date, boolean isChinese)
	{
		String d = "";
		if (!isChinese)
		{
			String d_temp = date;
			d_temp = d_temp.replace("年", "-");
			d_temp = d_temp.replace("月", "-");
			d_temp = d_temp.replace("日", "");

			try
			{
				String d_splite[] = d_temp.split("-");
				if (d_splite != null && d_splite.length == 3)
				{
					d = d_splite[2] + "/" + d_splite[1] + "/" + d_splite[0];
				}
				else
					d = d_temp;
			}
			catch (Exception e)
			{
				d = d_temp;
			}
		}
		else
		{
			d = date;
		}
		return d;
	}

	public static String convertDate(String date, String time, boolean isChinese)
	{
		String d = "";
		if (!isChinese)
		{
			String d_temp = date;
			d_temp = d_temp.replace("年", "-");
			d_temp = d_temp.replace("月", "-");
			d_temp = d_temp.replace("日", "");

			try
			{
				String d_split[] = d_temp.split("-");
				String t_split[] = time.split(":");
				if (d_split != null && d_split.length == 3 && t_split != null && t_split.length == 3)
				{
					d = d_split[1] + "月" + d_split[2] + "日 " + t_split[0] + ":" + t_split[1];
				}
				else
					d = d_temp;
			}
			catch (Exception e)
			{
				d = d_temp;
			}
		}
		else
		{
			d = date;
		}
		return d;
	}
}
