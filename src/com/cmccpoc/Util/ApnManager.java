package com.cmccpoc.Util;

import java.util.HashMap;
import java.util.Map;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import com.airtalkee.sdk.util.Log;

public class ApnManager
{
	// private static final String CURRENT_APN_ID = "current_apn_id";
	private static final Uri APN_TABLE_URI = Uri.parse("content://telephony/carriers");
	// The same as Telephony/Carriers/DEFAULT_SORT_ORDER
	private static final String DEFAULT_SORT_ORDER = "name ASC";
	// The same as ApnSettings/APN_ID
	private static final String APN_ID = "apn_id";
	public static final Uri APN_PREFER_URI = Uri.parse("content://telephony/carriers/preferapn");
	public static final Uri APN_RESTORE_URI = Uri.parse("content://telephony/carriers/restore");

	public static final int CONNECTION_TYPE_NONE = -1;
	public static final int CONNECTION_TYPE_MOBILE = ConnectivityManager.TYPE_MOBILE;
	public static final int CONNECTION_TYPE_WIFI = ConnectivityManager.TYPE_WIFI;

	public static int ConnectionType = CONNECTION_TYPE_NONE;
	public static String ConnectionFeature = "";

	/**
	 * @description Hold the columns of APN
	 * @author Bruce
	 */
	public class ApnColumns
	{
		public static final String ID = "_id";
		public static final String NAME = "name";
		public static final String APN = "apn";
		public static final String TYPE = "type";
		public static final String CURRENT = "current";
	}

	private static String[] PROJECTION = new String[] { ApnColumns.ID, ApnColumns.NAME, ApnColumns.APN, ApnColumns.TYPE };

	/**
	 * @description The interface for updating APN settings.
	 * @param context
	 * @param id
	 */
	public static String updateToNetApn(Context context) // return feature
	{
		String feature = "";
		ContentResolver resolver = context.getContentResolver();
		Map<String, String> apnMap = getNetApn(resolver);
		if (apnMap != null)
		{
			ContentValues values = new ContentValues();
			values.put(APN_ID, apnMap.get(ApnColumns.ID));
			resolver.update(APN_PREFER_URI, values, null, null);
			feature = apnMap.get(ApnColumns.APN);
		}
		return feature;
	}

	/**
	 * @description Get APNs from database.
	 * @param resolver
	 */
	private static Map<String, String> getNetApn(ContentResolver resolver)
	{
		Map<String, String> apnMap = null;
		boolean found = false;
		String selection = new StringBuilder().append(ApnColumns.TYPE).append(" is not null and ").append(ApnColumns.TYPE).append(" <> 'mms' and ").append(ApnColumns.CURRENT)
			.append(" = 1").toString();
		try
		{
			Cursor cursor = resolver.query(APN_TABLE_URI, PROJECTION, selection, null, DEFAULT_SORT_ORDER);

			if ((cursor != null) && (cursor.moveToFirst()))
			{
				// Map<String, String> map = null;
				int count = cursor.getCount();
				String apn = "";
				String name = "";

				for (int i = 0; i < count; i++)
				{
					// Get APN id and to find the next one,try to set prefer APN
					apn = cursor.getString(cursor.getColumnIndex(ApnColumns.APN));
					name = cursor.getString(cursor.getColumnIndex(ApnColumns.NAME));
					Log.i(ApnManager.class, "APN List [" + i + "] = " + apn + " [" + name + "]");
					if (isNetApn(apn, name))
					{
						Log.i(ApnManager.class, "Found net APN!!!");
						found = true;
						break;
					}
					if (!cursor.isLast())
						cursor.moveToNext();
				}
				if (count > 0 && found)
				{
					int apnId = cursor.getInt(cursor.getColumnIndex(ApnColumns.ID));
					apnMap = new HashMap<String, String>();
					apnMap.put(ApnColumns.ID, String.valueOf(apnId));
					apnMap.put(ApnColumns.NAME, cursor.getString(cursor.getColumnIndex(ApnColumns.NAME)));
					apnMap.put(ApnColumns.APN, cursor.getString(cursor.getColumnIndex(ApnColumns.APN)));
				}
				else
					Log.i(ApnManager.class, "NOT Found net APN!!!");
				// Do remember to close the cursor.
				cursor.close();
				Log.i(ApnManager.class, "APN ok!");
			}
			else
			{
				// Actually,no need to add the sentence,it's value is just
				// -1,only strictly.
				apnMap = null;
			}
		}
		catch (Exception e)
		{
			Log.i(ApnManager.class, e.toString());
		}

		return apnMap;
	}

	/**
	 * ����������ӹ���
	 * 
	 * @return
	 */
	private static ConnectivityManager getConnectManager(Context context)
	{
		if (context == null)
			return null;

		ConnectivityManager m_ConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		return m_ConnectivityManager;
	}

	public static int getNetworkType()
	{
		return ConnectionType;
	}

	public static boolean getNetworkState(Context context)
	{
		if (getConnectManager(context) != null)
		{
			NetworkInfo activeNetInfo = getConnectManager(context).getActiveNetworkInfo();
			if (activeNetInfo != null && activeNetInfo.getState() == NetworkInfo.State.CONNECTED)
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * @description Get Prefer APN already set.
	 * @param resolver
	 */
	@SuppressWarnings("unused")
	private static boolean isNetApn(Context context)
	{
		boolean ret = false;
		try
		{
			ContentResolver resolver = context.getContentResolver();
			Cursor cursor = resolver.query(APN_PREFER_URI, PROJECTION, null, null, null);
			String apnName = "";
			String apnApn = "";
			if ((cursor != null) && (cursor.moveToFirst()))
			{
				apnName = cursor.getString(cursor.getColumnIndex(ApnColumns.NAME));
				apnApn = cursor.getString(cursor.getColumnIndex(ApnColumns.APN));
				if (isNetApn(apnApn, apnName))
					ret = true;
				// Do remember to close the cursor.
				Log.i(ApnManager.class, "APN name =[" + apnName + "]");
				cursor.close();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return ret;
	}

	private static boolean isNetApn(String apnApn, String apnName)
	{
		boolean ret = false;
		if (apnName.trim().toLowerCase().endsWith("net") || apnApn.trim().toLowerCase().endsWith("net")
			|| (apnName.contains("�й����") || apnName.toLowerCase().contains("ct") || apnApn.trim().contains("777")))
			ret = true;
		return ret;
	}

	@SuppressWarnings("static-access")
	public static boolean activeNetworkFeature(Context context) // return IP
	// address
	{
		Log.i(ApnManager.class, "startUsingNetworkFeature	>>>>>>>>----------->>>> begin");
		if (getConnectManager(context) != null)
		{
			NetworkInfo activeNetInfo = getConnectManager(context).getActiveNetworkInfo();
			if (activeNetInfo != null)
			{
				switch (activeNetInfo.getType())
				{
					case CONNECTION_TYPE_MOBILE:
						ConnectionType = CONNECTION_TYPE_MOBILE;
						Log.i(ApnManager.class, "startUsingNetworkFeature	>>>>>>>>----------->>>> CONNECTION_TYPE_MOBILE");
						/*
						if (isNetApn(context))
						{
						    ConnectionFeature = activeNetInfo.getExtraInfo();
						}
						else
						{
						    //ConnectionFeature = updateToNetApn(context);
						    //getConnectManager(context).startUsingNetworkFeature(getConnectManager(context).TYPE_MOBILE, ConnectionFeature);
						    //return true;
						    return false;
						}
						*/
						ConnectionFeature = activeNetInfo.getExtraInfo();
						if (ConnectionFeature.equals("")) // There is NO net
						// apn, create it!!!
						{
							// Create NET APN
							Log.i(ApnManager.class, "startUsingNetworkFeature	>>>>>>>>----------->>>>APN==null you must to create apn");
						}
						else
						{
							getConnectManager(context).startUsingNetworkFeature(getConnectManager(context).TYPE_MOBILE, ConnectionFeature);
							if (ConnectionFeature.equals(activeNetInfo.getExtraInfo()) && activeNetInfo.getState() == NetworkInfo.State.CONNECTED)
							{
								Log.i(ApnManager.class, "startUsingNetworkFeature	>>>>>>>>----------->>>>mobile--reConnected");
								return true;
							}
						}
						break;
					case CONNECTION_TYPE_WIFI:
						ConnectionType = CONNECTION_TYPE_WIFI;
						Log.i(ApnManager.class, "startUsingNetworkFeature	>>>>>>>>----------->>>> CONNECTION_TYPE_WIFI");
						ConnectionFeature = "";
						if (activeNetInfo.getState() == NetworkInfo.State.CONNECTED)
						{
							Log.d(ApnManager.class, "startUsingNetworkFeature	>>>>>>>>----------->>>>wifi--connected");
							return true;
						}
						else if (context != null)
						{
							Log.d(ApnManager.class, "startUsingNetworkFeature	>>>>>>>>----------->>>>wifi--unconnected");
							// WifiManager wifiManager =
							// (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
							// wifiManager.setWifiEnabled(true);
							// return getLocalIpAddress(true);
						}
						break;
				}
			}
			else
			// �����缤��
			{
				ConnectionType = CONNECTION_TYPE_MOBILE;
				//ConnectionFeature = updateToNetApn(context);
				ConnectionFeature = "";
				Log.d(ApnManager.class, "startUsingNetworkFeature	>>>>>>>>----------->>>>No network!!!");
				return false;
			}
		}
		else
		{
			Log.i(ApnManager.class, "startUsingNetworkFeature	>>>>>>>>----------->>>>context==null");
		}
		return false;
	}

	/**
	 * ��תϵͳ�������ý���
	 * 
	 * @param context
	 */
	public static void startActivitySetting(Context context)
	{
		// ��������Ӧ�õ�activity
		// �ڴ�����"com.android.settings"��Ҫ�򿪵ĳ������"com.android.settings.WirelessSettings"��Ҫ�򿪵�Activity��
		if (context != null)
		{
			Intent intent = null;
			if (android.os.Build.VERSION.SDK_INT > 10)
			{
				intent = new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS);
			}
			else
			{
				intent = new Intent(Intent.ACTION_MAIN);
				ComponentName componentName = new ComponentName("com.android.settings", "com.android.settings.WirelessSettings");
				intent.setComponent(componentName);
			}
			if (intent != null)
				context.startActivity(intent);
		}
	}
}
