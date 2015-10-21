package com.airtalkee.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.os.Build;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.format.Time;
import android.view.inputmethod.InputMethodManager;
import com.airtalkee.Util.Toast;

import com.airtalkee.R;
import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.entity.AirContact;
import com.airtalkee.sdk.util.Log;
import com.airtalkee.sdk.util.Utils;
import com.airtalkee.services.AirServices;
import com.airtalkee.tts.TTSManager;

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
	@SuppressLint("NewApi") @SuppressWarnings("deprecation")
	public static void showNotification(int id, Context context, Intent intent, String from, String ticker, String message, Object object)
	{
		nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
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
			if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.HONEYCOMB)
			{
				 notification = new Notification.Builder(context).setSmallIcon(drawableId, 3).setContentText(from).setContentTitle(from).setContentIntent(contentIntent)
						.setTicker(ticker).build();
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
		{
		}
	}
	
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

	public static boolean getBuleToothModeState(Context context)
	{
		boolean b = false;
		try
		{
			AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
			b = am.isBluetoothScoOn();
		}
		catch (Exception e)
		{
			// TODO: handle exception
		}
		return b;
	}

	public static int getMode(Context context)
	{
		if (context != null)
		{
			AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
			return am.getMode();
		}
		return -1;
	}

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

	public static long getTimeGap(String time)
	{
		long gap = 0;
		/*
		try
		{
			SimpleDateFormat dFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			Date date = dFormat.parse(time);
			long c = date.getTime() / 1000;
			long now = AirtalkeeAccount.getInstance().AirBaseSeconds();
			Log.i(Util.class, "getTimeGap = " + now);
			if (now > 0)
				gap = c - now;
			else
				gap = 0;
		}
		catch (ParseException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		return gap;
	}

	public static AirContact getUser()
	{
		AirContact ct = null;
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
		{
			File xml = new File(Environment.getExternalStorageDirectory() + "/user_properties.xml");
			try
			{
				FileInputStream inStream = new FileInputStream(xml);
				List<AirContact> users = XMLContentHandler.readXML(inStream);
				if (users != null)
				{
					ct = users.get(0);
				}

			}
			catch (Exception er)
			{

			}
		}
		return ct;
	}

	public static String filterDisplayName(String displayName)
	{
		String ret = "";
		if (displayName != null && displayName.length() > 0)
		{
			displayName = displayName.replace('\r', ' ');
			displayName = displayName.replace('\n', ' ');
			displayName = displayName.replace('\\', ' ');
			displayName = displayName.replace('"', ' ');
			displayName = displayName.replace('\'', ' ');
			displayName = displayName.replace('<', ' ');
			displayName = displayName.replace('>', ' ');
			ret = displayName.trim();
		}
		return ret;
	}

	// ��¼�ص���Ϣ
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
			default:
				info = context.getResources().getString(R.string.talk_login_failed_general);
				break;
		}
		return info;
	}

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

	public static boolean IsUserName(String user)
	{
		boolean re = false;
		if (IsUserNumber(user))
			re = true;
		else
		{
			re = !TextUtils.isDigitsOnly(user);
		}
		return re;
	}

	public static boolean IsUserNumber(String num)
	{
		boolean re = false;
		if (num.length() == 11)
		{
			if (num.startsWith("13"))
			{
				re = true;
			}
			else if (num.startsWith("15"))
			{
				re = true;
			}
			else if (num.startsWith("18"))
			{
				re = true;
			}
		}
		return re;
	}

	public static String GetNumber(String num)
	{
		if (num != null)
		{
			if (num.startsWith("+86"))
			{
				num = num.substring(3);
			}
			else if (num.startsWith("86"))
			{
				num = num.substring(2);
			}
		}
		else
		{
			num = "";
		}
		return num;
	}

	public static boolean IsContain(ArrayList<AirContact> list, String userNumber)
	{
		for (int i = 0; i < list.size(); i++)
		{
			if (userNumber.equals(list.get(i).getiPhoneNumber()))
			{
				return true;
			}
		}
		return false;
	}

	public static String getImei(Context context)
	{
		String deviceid = "";
		try
		{
			TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
			deviceid = tm.getDeviceId();
		}
		catch (Exception e)
		{
			// TODO: handle exception
		}
		return deviceid;
	}

	public static void hideSoftInput(Context context)
	{
		try
		{
			((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(((Activity) context).getCurrentFocus().getWindowToken(),
				InputMethodManager.HIDE_NOT_ALWAYS);
		}
		catch (Exception e)
		{
			// TODO: handle exception
			Log.e(Util.class, "hideSoftInput-->error");
		}
	}

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

	public static boolean isSoftKeybordOpen(Context context)
	{
		InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		return imm.isFullscreenMode();
	}

	public static Spannable getSpannable(Context context, String text)
	{
		Spannable name = null;
		if (context != null && !Utils.isEmpty(text))
			name = Util.buildPlainMessageSpannable(context, text.replaceAll("\r", "").getBytes());
		return name;
	}

	/**
	 * 
	 * @param context
	 * @param content
	 * @return
	 */
	public static Spannable buildPlainMessageSpannable(Context context, byte[] content)
	{
		return buildPlainMessageSpannable(context, content, false);
	}

	public static Spannable buildPlainMessageSpannable(Context context, byte[] content, boolean isfontheight)
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
				{
				}
			}
			// create spannable string
			SpannableString spannable = new SpannableString(msg);
			// spannable.setSpan(new SuperTextSpan(), 1, 4,
			// spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

			// add link for URL
			// Linkify.addLinks(spannable,
			// Linkify.ALL);//ȥ�������ӣ�Ŀǰ�޷��������Ự��Ŀ�ϣ�����������»��ߣ�ûɶ�ã�
			// add smiley
			Smilify.getInstance(context).addSmiley(spannable, isfontheight);
			return spannable;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	@SuppressLint("NewApi") @SuppressWarnings("deprecation")
	public static void textClip(Context context, String clipStr)
	{
		try
		{
			ClipboardManager clip = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
			clip.setText(clipStr);
		}
		catch (Exception e)
		{
		}
	}

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

	public static String convertEnglishDate(String date)
	{
		String edateString = date;
		if (!Utils.isEmpty(date))
		{
			edateString = edateString.replace("��", "/");
			edateString = edateString.replace("��", "/");
			edateString = edateString.replace("��", "");
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

	public static void Toast(Context context, String content)
	{
		try
		{
			Toast.makeText1(context, content, Toast.LENGTH_LONG).show();
			TTSManager.getInstance().synth(content);
		}
		catch (Exception e)
		{
			// TODO: handle exception
		}
	}
	
	public static void Toast(Context context, String content,int icon)
	{
		try
		{
			Toast.makeText1(context, icon,content, Toast.LENGTH_LONG).show();
			TTSManager.getInstance().synth(content);
		}
		catch (Exception e)
		{
			// TODO: handle exception
		}
	}

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

	public static String getPhotoUrl(String photoid)
	{
		String pwd = getMD5Str(AirtalkeeAccount.getInstance().getUserKey());
		String id = AirtalkeeAccount.getInstance().getUserId();

		String ret = "http://113.11.197.108:1880/airtalkeemobile/mobile/fdownload.action?ipocid=" + id + "&password=" + pwd + "&resid=" + photoid + "&restype=1&dimension=480-480";

		return ret;
	}
}
