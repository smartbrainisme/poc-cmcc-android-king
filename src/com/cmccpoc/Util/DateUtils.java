package com.cmccpoc.Util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * 日期帮助类
 * @author Yao
 */
public class DateUtils
{
	private static final long INTERVAL_IN_MILLISECONDS = 30 * 1000;

	/**
	 * 转化时间字符串
	 * @param messageDate 日期
	 * @param curLocale 时区
	 * @return
	 */
	public static String getTimestampString(Date messageDate, Locale curLocale)
	{
		String languageCode = curLocale.getLanguage();
		boolean isChinese = languageCode.contains("zh");
		String format = null;
		long messageTime = messageDate.getTime();
		if (isSameDay(messageTime))
		{
			Calendar calendar = GregorianCalendar.getInstance();
			calendar.setTime(messageDate);
			int hour = calendar.get(Calendar.HOUR_OF_DAY);

			format = "HH:mm";

			if (hour > 17)
			{
				if (isChinese)
				{
					format = "晚上 hh:mm";
				}

			}
			else if (hour >= 0 && hour <= 6)
			{
				if (isChinese)
				{
					format = "凌晨 hh:mm";
				}
			}
			else if (hour > 11 && hour <= 17)
			{
				if (isChinese)
				{
					format = "下午 hh:mm";
				}

			}
			else
			{
				if (isChinese)
				{
					format = "上午 hh:mm";
				}
			}
		}
		else if (isYesterday(messageTime))
		{
			if (isChinese)
			{
				format = "昨天 HH:mm";
			}
			else
			{
				format = "MM-dd HH:mm";
			}

		}
		else
		{
			if (isChinese)
			{
				format = "M月d日 HH:mm";
			}
			else
			{
				format = "MM-dd HH:mm";
			}
		}

		if (isChinese)
		{
			return new SimpleDateFormat(format, Locale.CHINA).format(messageDate);
		}
		else
		{
			return new SimpleDateFormat(format, Locale.US).format(messageDate);
		}
	}

	/**
	 * 比较时间是否足够接近
	 * @param time1
	 * @param time2
	 * @return
	 */
	public static boolean isCloseEnough(long time1, long time2)
	{
		long delta = time1 - time2;
		if (delta < 0)
		{
			delta = -delta;
		}
		return delta < INTERVAL_IN_MILLISECONDS;
	}

	/**
	 * 是否是同一天
	 * @param inputTime 时间
	 * @return
	 */
	private static boolean isSameDay(long inputTime)
	{

		TimeInfo tStartAndEndTime = getTodayStartAndEndTime();
		if (inputTime > tStartAndEndTime.getStartTime() && inputTime < tStartAndEndTime.getEndTime())
			return true;
		return false;
	}

	/**
	 * 是否是昨天
	 * @param inputTime 时间
	 * @return
	 */
	private static boolean isYesterday(long inputTime)
	{
		TimeInfo yStartAndEndTime = getYesterdayStartAndEndTime();
		if (inputTime > yStartAndEndTime.getStartTime() && inputTime < yStartAndEndTime.getEndTime())
			return true;
		return false;
	}

	/**
	 * 字符串转日期
	 * @param dateStr 日期字符串
	 * @param formatStr 日期格式
	 * @return
	 */
	public static Date StringToDate(String dateStr, String formatStr)
	{
		DateFormat format = new SimpleDateFormat(formatStr);
		Date date = null;
		try
		{
			date = format.parse(dateStr);
		}
		catch (ParseException e)
		{
			e.printStackTrace();
		}
		return date;
	}

	/**
	 * 转化时间
	 * @param timeLength Millisecond
	 * @return
	 */
	public static String toTime(int timeLength)
	{
		timeLength /= 1000;
		int minute = timeLength / 60;
		// int hour = 0;
		if (minute >= 60)
		{
			// hour = minute / 60;
			minute = minute % 60;
		}
		int second = timeLength % 60;
		// return String.format("%02d:%02d:%02d", hour, minute, second);
		return String.format("%02d:%02d", minute, second);
	}

	/**
	 * 转化时间
	 * @param timeLength second
	 * @return
	 */
	public static String toTimeBySecond(int timeLength)
	{
		// timeLength /= 1000;
		int minute = timeLength / 60;
		// int hour = 0;
		if (minute >= 60)
		{
			// hour = minute / 60;
			minute = minute % 60;
		}
		int second = timeLength % 60;
		// return String.format("%02d:%02d:%02d", hour, minute, second);
		return String.format("%02d:%02d", minute, second);
	}

	/**
	 * 获取昨天的起止时间
	 * @return
	 */
	public static TimeInfo getYesterdayStartAndEndTime()
	{
		Calendar calendar1 = Calendar.getInstance();
		calendar1.add(Calendar.DATE, -1);
		calendar1.set(Calendar.HOUR_OF_DAY, 0);
		calendar1.set(Calendar.MINUTE, 0);
		calendar1.set(Calendar.SECOND, 0);
		calendar1.set(Calendar.MILLISECOND, 0);

		Date startDate = calendar1.getTime();
		long startTime = startDate.getTime();

		Calendar calendar2 = Calendar.getInstance();
		calendar2.add(Calendar.DATE, -1);
		calendar2.set(Calendar.HOUR_OF_DAY, 23);
		calendar2.set(Calendar.MINUTE, 59);
		calendar2.set(Calendar.SECOND, 59);
		calendar2.set(Calendar.MILLISECOND, 999);
		Date endDate = calendar2.getTime();
		long endTime = endDate.getTime();
		TimeInfo info = new TimeInfo();
		info.setStartTime(startTime);
		info.setEndTime(endTime);
		return info;
	}

	/**
	 * 获取今天的起止时间
	 * @return
	 */
	public static TimeInfo getTodayStartAndEndTime()
	{
		Calendar calendar1 = Calendar.getInstance();
		calendar1.set(Calendar.HOUR_OF_DAY, 0);
		calendar1.set(Calendar.MINUTE, 0);
		calendar1.set(Calendar.SECOND, 0);
		calendar1.set(Calendar.MILLISECOND, 0);
		Date startDate = calendar1.getTime();
		long startTime = startDate.getTime();
		// SimpleDateFormat format = new
		// SimpleDateFormat("yyyy-MM-dd HH:mm:ss S");

		Calendar calendar2 = Calendar.getInstance();
		calendar2.set(Calendar.HOUR_OF_DAY, 23);
		calendar2.set(Calendar.MINUTE, 59);
		calendar2.set(Calendar.SECOND, 59);
		calendar2.set(Calendar.MILLISECOND, 999);
		Date endDate = calendar2.getTime();
		long endTime = endDate.getTime();
		TimeInfo info = new TimeInfo();
		info.setStartTime(startTime);
		info.setEndTime(endTime);
		return info;
	}

	/**
	 * 获取前天的起止时间
	 * @return
	 */
	public static TimeInfo getBeforeYesterdayStartAndEndTime()
	{
		Calendar calendar1 = Calendar.getInstance();
		calendar1.add(Calendar.DATE, -2);
		calendar1.set(Calendar.HOUR_OF_DAY, 0);
		calendar1.set(Calendar.MINUTE, 0);
		calendar1.set(Calendar.SECOND, 0);
		calendar1.set(Calendar.MILLISECOND, 0);
		Date startDate = calendar1.getTime();
		long startTime = startDate.getTime();

		Calendar calendar2 = Calendar.getInstance();
		calendar2.add(Calendar.DATE, -2);
		calendar2.set(Calendar.HOUR_OF_DAY, 23);
		calendar2.set(Calendar.MINUTE, 59);
		calendar2.set(Calendar.SECOND, 59);
		calendar2.set(Calendar.MILLISECOND, 999);
		Date endDate = calendar2.getTime();
		long endTime = endDate.getTime();
		TimeInfo info = new TimeInfo();
		info.setStartTime(startTime);
		info.setEndTime(endTime);
		return info;
	}

	/**
	 * endtime为今天
	 * @return
	 */
	public static TimeInfo getCurrentMonthStartAndEndTime()
	{
		Calendar calendar1 = Calendar.getInstance();
		calendar1.set(Calendar.DATE, 1);
		calendar1.set(Calendar.HOUR_OF_DAY, 0);
		calendar1.set(Calendar.MINUTE, 0);
		calendar1.set(Calendar.SECOND, 0);
		calendar1.set(Calendar.MILLISECOND, 0);
		Date startDate = calendar1.getTime();
		long startTime = startDate.getTime();

		Calendar calendar2 = Calendar.getInstance();
		// calendar2.set(Calendar.HOUR_OF_DAY, 23);
		// calendar2.set(Calendar.MINUTE, 59);
		// calendar2.set(Calendar.SECOND, 59);
		// calendar2.set(Calendar.MILLISECOND, 999);
		Date endDate = calendar2.getTime();
		long endTime = endDate.getTime();
		TimeInfo info = new TimeInfo();
		info.setStartTime(startTime);
		info.setEndTime(endTime);
		return info;
	}

	/**
	 * 上个月起止时间信息
	 * @return
	 */
	public static TimeInfo getLastMonthStartAndEndTime()
	{
		Calendar calendar1 = Calendar.getInstance();
		calendar1.add(Calendar.MONTH, -1);
		calendar1.set(Calendar.DATE, 1);
		calendar1.set(Calendar.HOUR_OF_DAY, 0);
		calendar1.set(Calendar.MINUTE, 0);
		calendar1.set(Calendar.SECOND, 0);
		calendar1.set(Calendar.MILLISECOND, 0);
		Date startDate = calendar1.getTime();
		long startTime = startDate.getTime();

		Calendar calendar2 = Calendar.getInstance();
		calendar2.add(Calendar.MONTH, -1);
		calendar2.set(Calendar.DATE, 1);
		calendar2.set(Calendar.HOUR_OF_DAY, 23);
		calendar2.set(Calendar.MINUTE, 59);
		calendar2.set(Calendar.SECOND, 59);
		calendar2.set(Calendar.MILLISECOND, 999);
		calendar2.roll(Calendar.DATE, -1);
		Date endDate = calendar2.getTime();
		long endTime = endDate.getTime();
		TimeInfo info = new TimeInfo();
		info.setStartTime(startTime);
		info.setEndTime(endTime);
		return info;
	}

	/**
	 * 获取当前时间戳
	 * @return
	 */
	public static String getTimestampStr()
	{
		return Long.toString(System.currentTimeMillis());
	}

}
