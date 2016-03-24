package com.cmccpoc.Util;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.format.Time;
import android.view.inputmethod.InputMethodManager;
import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.entity.AirContact;
import com.airtalkee.sdk.util.Log;
import com.airtalkee.sdk.util.Utils;
import com.cmccpoc.R;
import com.cmccpoc.services.AirServices;

/**
 * app通用工具类
 * @author Yao
 *
 */
public class Util
{
	public static final int NOTIFI_ID_MESSAGE = 0;
	public static final int NOTIFI_ID_VOICE_LISTEN = 1;
	public static final int NOTIFI_ID_VOICE_TALK = 5;
	public static final int NOTIFI_ID_VOICE_RECORD = 10;
	public static final int NOTIFI_ID_NOTICE = 2;
	public static final int NOTIFI_ID_FENCE_WARNING = 3;
	public static final int NOTIFI_ID_TASK_DISPATCH = 4;

	static NotificationManager nm = null;

	/**
	 * 显示通知
	 * @param id 通知类型
	 * @param context 上下文
	 * @param intent intent对象
	 * @param from 发送人id
	 * @param ticker 标题
	 * @param message 消息内容
	 * @param object 对象参数
	 */
	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	public static void showNotification(int id, Context context, Intent intent, String from, String ticker, String message, Object object)
	{
		nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		PendingIntent contentIntent = PendingIntent.getActivity(context, UUID.randomUUID().hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
		int flag = -1;
		int drawableId = R.drawable.notfiy_icon;
		switch (id)
		{
			case NOTIFI_ID_MESSAGE:
			case NOTIFI_ID_NOTICE:
			case NOTIFI_ID_VOICE_RECORD:
			case NOTIFI_ID_VOICE_TALK:
			case NOTIFI_ID_FENCE_WARNING:
			case NOTIFI_ID_TASK_DISPATCH:
			{
				flag = Notification.FLAG_AUTO_CANCEL;
				break;
			}
		}
		try
		{
			Notification notification;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			{
				notification = new Notification.Builder(context).setSmallIcon(drawableId, 3).setContentText(message).setContentTitle(from).setContentIntent(contentIntent).setTicker(ticker).build();
			}
			else
			{
				notification = new Notification(drawableId, ticker, System.currentTimeMillis());
				notification.setLatestEventInfo(context, from, message, contentIntent);
			}
			if (flag != -1)
			{
				notification.flags = flag;
			}
			nm.notify(id, notification);
		}
		catch (NoSuchMethodError e)
		{
			//
		}
		catch (Exception e)
		{
			// TODO: handle exception
			// Log.e(Util.class, "util--182---error");
		}
	}

	/**
	 * 关闭通知
	 * @param id
	 */
	public static void closeNotification(int id)
	{
		try
		{
			if (nm != null)
			{
				nm.cancel(id);
			}
		}
		catch (Exception e)
		{}
	}

	/**
	 * 获取app版本
	 * @param context 上下文
	 * @return
	 */
	public static String appVersion(Context context)
	{
		String version = "";
		try
		{
			PackageManager manager = context.getPackageManager();
			PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
			version = info.versionName;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return version;
	}

	/**
	 * 调节音量--增大
	 * @param context 上下文
	 */
	public static void setStreamVolumeUp(Context context)
	{
		AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		int max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		int currentVolume = getStreamVolume(context);
		if (currentVolume < max)
		{
			currentVolume++;
			// Toast.makeText(context,
			// String.format(context.getString(R.string.talk_volume),
			// currentVolume), 0).show();
			setStreamVolume(context, currentVolume);
		}
		else
		{
			Toast.makeText(context, R.string.talk_volume_max, Toast.LENGTH_SHORT).show();
		}

	}

	/**
	 * 调节音量--减小
	 * @param context 上下文
	 */
	public static void setStreamVolumeDown(Context context)
	{

		int currentVolume = getStreamVolume(context);
		if (currentVolume > 0)
		{
			currentVolume--;
			// Toast.makeText(context,
			// String.format(context.getString(R.string.talk_volume),
			// currentVolume), 0).show();
			setStreamVolume(context, currentVolume);
		}
		else
		{
			Toast.makeText(context, R.string.talk_volume_min, Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * 设置音量大小
	 * @param context 上下文
	 * @param streamVolume 音量
	 */
	public static void setStreamVolume(Context context, int streamVolume)
	{
		try
		{
			AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
			am.setStreamVolume(AudioManager.STREAM_MUSIC, streamVolume, AudioManager.FLAG_PLAY_SOUND);
		}
		catch (Exception e)
		{
			// Log.e(Util.class, e.toString());
		}
	}

	/**
	 * 获取当前语音模式
	 * @param context 上下文
	 * @return
	 */
	public static int getMode(Context context)
	{
		if (context != null)
		{
			AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
			return am.getMode();
		}
		return -1;
	}

	/**
	 * 设置当前语音模式
	 * @param context 上下文
	 */
	public static void setMode(Context context)
	{
		try
		{
			if (context != null)
			{
				// TODO: ��Ͳ am.setMode(AudioManager.MODE_IN_CALL);//TODO: ����
				// AudioManager.MODE_NORMAL
				AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
				if (am.getMode() == AudioManager.MODE_NORMAL)
				{
					am.setMode(AudioManager.MODE_IN_CALL);
					am.setSpeakerphoneOn(false);
				}
				else
				{
					am.setMode(AudioManager.MODE_NORMAL);
					am.setSpeakerphoneOn(true);
				}
			}
		}
		catch (Exception e)
		{
			// TODO: handle exception
		}
	}

	/**
	 * 获取音量值
	 * @param context 上下文
	 * @return
	 */
	public static int getStreamVolume(Context context)
	{
		int streamVolume = 0;
		try
		{
			AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
			streamVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
		}
		catch (Exception e)
		{
			// Log.e(Util.class, e.toString());
		}
		return streamVolume;
	}

	/**
	 * 获取日期
	 * @return
	 */
	public static String getCurrentDate()
	{
		String currentTime = "";
		try
		{
			Date now = new Date();
			DateFormat d = DateFormat.getDateTimeInstance();
			currentTime = d.format(now);
		}
		catch (Exception e)
		{
			// TODO: handle exception
			// Log.e(Util.class, "getCurrentDate--->>error"+e.toString());
		}
		return currentTime;
	}

	/**
	 * 获取时刻
	 * @return
	 */
	public static String getCurrentTime()
	{
		Time t = new Time();
		t.setToNow();
		int year = t.year;
		int month = t.month + 1;
		int day = t.monthDay;
		int hour = t.hour;
		int minute = t.minute;
		int second = t.second;
		String time = year + "-" + month + "-" + day;
		time += " " + String.format("%02d:%02d:%02d", hour, minute, second);
		return time;
	}

	/**
	 * 登陆信息
	 * @param result 登陆结果
	 * @param context 上下文
	 * @return
	 */
	public static String loginInfo(int result, Context context)
	{
		String info = "";
		if (context == null)
			return info;
		switch (result)
		{
			case AirtalkeeAccount.ACCOUNT_RESULT_ERR_NETWORK:
				info = context.getResources().getString(R.string.talk_login_failed_general);
				break;
			case AirtalkeeAccount.ACCOUNT_RESULT_ERR_USER_NOTEXIST:
				info = context.getResources().getString(R.string.talk_login_failed_nouser);
				break;
			case AirtalkeeAccount.ACCOUNT_RESULT_ERR_SERVER_UNAVAILABLE:
				info = context.getResources().getString(R.string.talk_login_failed_server);
				break;
			case AirtalkeeAccount.ACCOUNT_RESULT_ERR_USER_PWD:
				info = context.getResources().getString(R.string.talk_login_login_failed_user_or_password);
				break;
			case AirtalkeeAccount.ACCOUNT_RESULT_ERR_USER_IN_BLACKLIST:
				info = context.getResources().getString(R.string.talk_login_login_failed_forbidden);
				break;
			case AirtalkeeAccount.ACCOUNT_RESULT_ERR_USER_NOT_ACCEPTABLE:
				info = context.getResources().getString(R.string.talk_login_login_failed_not_acceptable);
				break;
			case AirtalkeeAccount.ACCOUNT_RESULT_ERR_PWD_DUP:
				info = context.getResources().getString(R.string.talk_login_login_failed_pwd_duplicate);
				break;
			case AirtalkeeAccount.ACCOUNT_RESULT_ERR_ACCOUNT_LIMITED:
				info = context.getResources().getString(R.string.talk_userlogin_register_failed_limited);
				break;
			case AirtalkeeAccount.ACCOUNT_RESULT_ERR_ACCOUNT_EXPIRE:
				info = context.getResources().getString(R.string.talk_userlogin_login_expired);
				break;
			case AirtalkeeAccount.ACCOUNT_RESULT_ERR_ACCOUNT_FORBIDDEN:
				info = context.getResources().getString(R.string.talk_userlogin_login_forbidden);
				break;
			case AirtalkeeAccount.ACCOUNT_RESULT_ERR_CODE_EXPIRE:
				info = context.getResources().getString(R.string.talk_user_temp_code_expire);
				break;
			case AirtalkeeAccount.ACCOUNT_RESULT_ERR_SINGLE:
				info = context.getResources().getString(R.string.talk_login_login_failed_single);
				break;
			case AirtalkeeAccount.ACCOUNT_RESULT_ERR_LICENSE:
				info = context.getResources().getString(R.string.talk_login_failed_general);
				break;
			default:
				info = context.getResources().getString(R.string.talk_login_failed_general);
				break;
		}
		return info;
	}

	/**
	 * 版本配置
	 * @param context
	 */
	public static void versionConfig(Context context)
	{
		int preVersion = AirServices.iOperator.getInt("KEY_VERSION");
		int currentVersion = 0;
		try
		{
			currentVersion = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
		}
		catch (NameNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		AirServices.iOperator.putInt("KEY_VERSION", currentVersion);
		AirServices.VERSION_NEW = currentVersion > preVersion;
	}

	/**
	 * 隐藏软键盘
	 * @param context 上下文
	 */
	public static void hideSoftInput(Context context)
	{
		try
		{
			((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(((Activity) context).getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		}
		catch (Exception e)
		{
			// TODO: handle exception
			Log.e(Util.class, "hideSoftInput-->error");
		}
	}

	/**
	 * 显示软键盘
	 * @param context 上下文
	 */
	public static void showSoftInput(Context context)
	{
		try
		{
			InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.toggleSoftInputFromWindow(((Activity) context).getCurrentFocus().getWindowToken(), InputMethodManager.SHOW_IMPLICIT, InputMethodManager.RESULT_SHOWN);
		}
		catch (Exception e)
		{
			// TODO: handle exception
			// Log.e(Util.class, "hideSoftInput-->error");
		}
	}

	/**
	 * 软键盘是否打开
	 * @param context 上下文
	 * @return
	 */
	public static boolean isSoftKeybordOpen(Context context)
	{
		InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		return imm.isFullscreenMode();
	}

	/**
	 * 获取Spannable对象
	 * @param context 上下文
	 * @param text 文字内容
	 * @return
	 */
	public static Spannable getSpannable(Context context, String text)
	{
		Spannable name = null;
		if (context != null && !Utils.isEmpty(text))
			name = Util.buildPlainMessageSpannable(context, text.replaceAll("\r", "").getBytes());
		return name;
	}

	/**
	 * 构建消息容器
	 * @param context 上下文
	 * @param content 文字内容
	 * @return
	 */
	public static Spannable buildPlainMessageSpannable(Context context, byte[] content)
	{
		try
		{
			String msg = "";
			if (content != null)
			{
				try
				{
					msg = new String(content, "UTF-8");
				}
				catch (UnsupportedEncodingException e)
				{}
			}
			SpannableString spannable = new SpannableString(msg);
			Smilify.getInstance(context).addSmiley(spannable);
			return spannable;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 文字截取
	 * @param context 上下文
	 * @param clipStr 截取字符串
	 */
	@SuppressWarnings("deprecation")
	public static void textClip(Context context, String clipStr)
	{
		try
		{
			ClipboardManager clip = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
			clip.setText(clipStr);
		}
		catch (Exception e)
		{}
	}

	/**
	 * 获取图片临时名称
	 * @return
	 */
	public static String getImageTempFileName()
	{
		String saveDir = Environment.getExternalStorageDirectory() + "/DCIM/Camera";
		File dir = new File(saveDir);
		if (!dir.exists())
		{
			dir.mkdir();
		}
		String fileName = saveDir + "/AIR" + Utils.getCurrentTimeInMillis() + ".jpg";
		return fileName;
	}

	/**
	 * 转换English时间格式
	 * @param date 时间字符串
	 * @return
	 */
	public static String convertEnglishDate(String date)
	{
		String edateString = date;
		if (!Utils.isEmpty(date))
		{
			edateString = edateString.replace("年", "/");
			edateString = edateString.replace("月", "/");
			edateString = edateString.replace("日", "");
			String d[] = edateString.split("/");
			if (d != null && d.length == 3)
			{
				edateString = d[2] + "/";
				edateString += d[1] + "/";
				edateString += d[0];
			}
		}
		return edateString;
	}

	/**
	 * 拼命是否亮
	 * @param context 上下文
	 * @return
	 */
	public static boolean isScreenOn(Context context)
	{
		boolean isOn = true;
		if (context != null)
		{
			android.app.KeyguardManager mKeyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
			if (mKeyguardManager != null)
			{
				isOn = !mKeyguardManager.inKeyguardRestrictedInputMode();
			}
		}
		return isOn;
	}

	/*********************************
	 * 
	 * Toast
	 * 
	 *********************************/

	private static Toast mToast = null;

	/**
	 * 显示Toast提示
	 * @param context 上下文
	 * @param content 内容
	 */
	public static void Toast(Context context, String content)
	{
		try
		{
			Toast.makeText1(context, content, Toast.LENGTH_LONG).show();
		}
		catch (Exception e)
		{
			// TODO: handle exception
		}
	}

	/**
	 * 显示Toast提示
	 * @param context 上下文
	 * @param content 内容
	 * @param icon icon图标
	 */
	public static void Toast(Context context, String content, int icon)
	{
		try
		{
			Toast.makeText1(context, icon, content, Toast.LENGTH_LONG).show();
		}
		catch (Exception e)
		{
			// TODO: handle exception
		}
	}

	/**
	 * 显示Toast提示
	 * @param context 上下文
	 * @param content 内容
	 * @param seconds 显示时长
	 * @param icon icon图标
	 */
	public static void Toast(Context context, String content, int seconds, int icon)
	{
		try
		{
			Toast.makeText1(context, content, seconds).show();
		}
		catch (Exception e)
		{
			// TODO: handle exception
		}
	}

	/**
	 * 获取MD5加密后字符串
	 * @param str 原字符串
	 * @return
	 */
	public static String getMD5Str(String str)
	{
		MessageDigest messageDigest = null;
		try
		{
			messageDigest = MessageDigest.getInstance("MD5");
			messageDigest.reset();
			messageDigest.update(str.getBytes("UTF-8"));
		}
		catch (NoSuchAlgorithmException e)
		{
			System.out.println("NoSuchAlgorithmException caught!");
			System.exit(-1);
		}
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
		byte[] byteArray = messageDigest.digest();
		StringBuffer md5StrBuff = new StringBuffer();
		for (int i = 0; i < byteArray.length; i++)
		{
			if (Integer.toHexString(0xFF & byteArray[i]).length() == 1)
				md5StrBuff.append("0").append(Integer.toHexString(0xFF & byteArray[i]));
			else
				md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));
		}
		return md5StrBuff.toString();
	}

	/**
	 * 获取图片Url地址
	 * @param photoid 图片Id
	 * @return
	 */
	public static String getPhotoUrl(String photoid)
	{
		String pwd = getMD5Str(AirtalkeeAccount.getInstance().getUserKey());
		String id = AirtalkeeAccount.getInstance().getUserId();

		String ret = "http://113.11.197.108:1880/airtalkeemobile/mobile/fdownload.action?ipocid=" + id + "&password=" + pwd + "&resid=" + photoid + "&restype=1&dimension=480-480";

		return ret;
	}

	/**
	 * 是否在后台运行
	 * @param context 上下文
	 * @return
	 */
	public static boolean isBackground(Context context)
	{
		ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
		for (RunningAppProcessInfo appProcess : appProcesses)
		{
			if (appProcess.processName.equals(context.getPackageName()))
			{
				if (appProcess.importance == RunningAppProcessInfo.IMPORTANCE_BACKGROUND)
				{
					return true;
				}
				else
				{
					return false;
				}
			}
		}
		return false;
	}

	/**
	 * 获取当前网络状态
	 * @return
	 */
	public static String getCurrentNetType()
	{
		String type = "";
		ConnectivityManager cm = (ConnectivityManager) AirServices.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getActiveNetworkInfo();
		if (info == null)
		{
			type = "null";
		}
		else if (info.getType() == ConnectivityManager.TYPE_WIFI)
		{
			type = "wifi";
		}
		else if (info.getType() == ConnectivityManager.TYPE_MOBILE)
		{
			int subType = info.getSubtype();
			if (subType == TelephonyManager.NETWORK_TYPE_CDMA || subType == TelephonyManager.NETWORK_TYPE_GPRS || subType == TelephonyManager.NETWORK_TYPE_EDGE)
			{
				type = "2g";
			}
			else if (subType == TelephonyManager.NETWORK_TYPE_UMTS || subType == TelephonyManager.NETWORK_TYPE_HSDPA || subType == TelephonyManager.NETWORK_TYPE_EVDO_A || subType == TelephonyManager.NETWORK_TYPE_EVDO_0 || subType == TelephonyManager.NETWORK_TYPE_EVDO_B)
			{
				type = "3g";
			}
			else if (subType == TelephonyManager.NETWORK_TYPE_LTE)
			{// LTE是3g到4g的过渡，是3.9G的全球标准
				type = "4g";
			}
		}
		return type;
	}
}
