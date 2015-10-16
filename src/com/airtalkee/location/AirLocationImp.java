package com.airtalkee.location;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.airtalkee.Util.AirMmiTimer;
import com.airtalkee.Util.AirMmiTimerListener;
import com.airtalkee.Util.Util;
import com.airtalkee.sdk.util.Log;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

public class AirLocationImp
{

	public final static int LOCATION_TYPE_GPS = 0;
	public final static int LOCATION_TYPE_CELL_BAIDU = 2;

	public class TimerParam
	{
		public int id = 0;
		public OnMapListener listener = null;
		public int type = LOCATION_TYPE_GPS;
		public int timeoutSeconds = 0;
		public Context context = null;
		public LocationListener listenerGps = null;
		public BDLocationListener listenerCellBaidu = null;
	}

	public final static double CELL_ERROR = 4.9E-324;

	private LocationClient mClientCell = null;
	private LocationClientOption mClientCellOption = null;

	public static int mType = LOCATION_TYPE_GPS;
	public static double mLatitude = 0;
	public static double mLongitude = 0;
	public static double mAltitude = 0;
	public static float mSpeed = 0;
	public static String mTime = "";

	public void LocationGet(final Context context, final OnMapListener listener, final int actionId, final int timeoutSeconds)
	{
		LocationTerminate(context);
		LocationManager mLocMan = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		if (mLocMan.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER))
		{
			doGpsLocation(actionId, timeoutSeconds, context, listener);
		}
		else
		{
			doCellLocation(actionId, timeoutSeconds, context, listener);
		}
	}

	public void LocationTerminate(final Context context)
	{
		TimerParam param = (TimerParam) AirMmiTimer.getInstance().TimerUnregister(context, doGpsLocationTimeout);
		if (param != null)
		{
			doRelease(context, param);
		}
	}
	
	public int getLocType()
	{
		return mType;
	}

	public double getLocLatitude()
	{
		return mLatitude;
	}

	public double getLocLongitude()
	{
		return mLongitude;
	}

	public double getLocAltitude()
	{
		return mAltitude;
	}

	public float getLocSpeed()
	{
		return mSpeed;
	}
	
	public String getLocTime()
	{
		return mTime;
	}

	private void listenerCallback(OnMapListener listener, int id, int type, boolean isFinal, double latitude, double longitude, double altitude, float speed, String time)
	{
		boolean isOk = false;
		if (latitude != CELL_ERROR && latitude != 0)
			mLatitude = latitude;
		if (longitude != CELL_ERROR && longitude != 0)
			mLongitude = longitude;
		if (altitude != CELL_ERROR && altitude != 0)
			mAltitude = altitude;
		if (speed != CELL_ERROR && speed != 0)
			mSpeed = speed;

		if (mLatitude != 0 && mLongitude != 0)
		{
			isOk = true;
			mTime = time;
			mType = type;
		}

		if (listener != null)
		{
			listener.OnMapLocation(isOk, id, type, isFinal, mLatitude, mLongitude, mAltitude, mSpeed, mTime);
		}
	}

	// =========================================
	// GPS Mode
	// =========================================

	private void doGpsLocation(final int id, final int timeoutSeconds, final Context context, final OnMapListener listener)
	{
		TimerParam param = new TimerParam();
		final TimerParam fparam = param;
		
		LocationListener locationListener = new LocationListener()
		{
			@Override
			public void onStatusChanged(String provider, int status, Bundle extras)
			{
				Log.i(AirLocationImp.class, "[LOCATION][ID:" + id + "][GPS] onStatusChanged, provider = " + provider + " status = " + status);
			}

			@Override
			public void onProviderEnabled(String provider)
			{
				Log.i(AirLocationImp.class, "[LOCATION][ID:" + id + "][GPS] onProviderEnabled, provider = " + provider);
			}

			@Override
			public void onProviderDisabled(String provider)
			{
				Log.i(AirLocationImp.class, "[LOCATION][ID:" + id + "][GPS] onProviderDisabled, provider = " + provider);
			}

			@Override
			public void onLocationChanged(Location location)
			{
				AirMmiTimer.getInstance().TimerUnregister(context, doGpsLocationTimeout);
				Log.i(AirLocationImp.class, "[LOCATION][ID:" + id + "][GPS] latitude: " + location.getLatitude() + ", longitude: " + location.getLongitude() + " Time:"
					+ location.getTime());
				listenerCallback(listener, id, LOCATION_TYPE_GPS, true, location.getLatitude(), location.getLongitude(), location.getAltitude(), location.getSpeed(), Util.getCurrentDate());
				Log.i(AirLocationImp.class, "[LOCATION][ID:" + id + "][GPS] Closed!");
				doRelease(context, fparam);
			}
		};

		param.id = id;
		param.type = LOCATION_TYPE_GPS;
		param.timeoutSeconds = timeoutSeconds;
		param.context = context;
		param.listener = listener;
		param.listenerGps = locationListener;

		Log.i(AirLocationImp.class, "[LOCATION][ID:" + id + "][GPS] Getting...");
		AirMmiTimer.getInstance().TimerRegister(context, doGpsLocationTimeout, true, false, timeoutSeconds * 1000, false, param);
		LocationManager mLocMan = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		//mLocMan.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, null);
		mLocMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
	}
	
	private AirMmiTimerListener doGpsLocationTimeout = new AirMmiTimerListener()
	{
		@Override
		public void onMmiTimer(Context context, Object userData)
		{
			// TODO Auto-generated method stub
			TimerParam param = (TimerParam) userData;
			Log.i(AirLocationImp.class, "[LOCATION][ID:" + param.id + "] Timeout!");
			doRelease(context, param);
			if (param.type == LOCATION_TYPE_GPS)
			{
				Log.i(AirLocationImp.class, "[LOCATION][ID:" + param.id + "][GPS] Timeout to get CELL");
				doCellLocation(param.id, AirLocation.AIR_LOCATION_CELL_TRY_TIME - 5, param.context, param.listener);
			}
		}
	};


	// =========================================
	// Cell Mode
	// =========================================

	private void doCellLocation(final int id, final int timeoutSeconds, final Context context, final OnMapListener listener)
	{
		doCellLocation(id, true, timeoutSeconds, context, listener);
	}

	private void doCellLocation(final int id, final boolean isFinal, final int timeoutSeconds, final Context context, final OnMapListener listener)
	{
		if (mClientCell == null || mClientCellOption == null)
		{
			mClientCell = new LocationClient(context);
			mClientCellOption = new LocationClientOption();
			mClientCellOption.setOpenGps(false);
			mClientCellOption.setAddrType("all");
			mClientCellOption.setCoorType("gcj02");
			//mClientCellOption.disableCache(true);
			mClientCellOption.setScanSpan(LocationClientOption.MIN_SCAN_SPAN);
			//mClientCellOption.setPriority(LocationClientOption.NetWorkFirst);
			//mClientCellOption.setPoiNumber(0);
			//mClientCellOption.setPoiDistance(1000);
			//mClientCellOption.setPoiExtraInfo(false);
			mClientCellOption.setTimeOut(timeoutSeconds * 1000);
			mClientCell.setLocOption(mClientCellOption);
		}
		
		BDLocationListener locationCellListener = new BDLocationListener()
		{
			@Override
			public void onReceiveLocation(BDLocation location)
			{
				long ts = Util.getTimeGap(location.getTime());
				Log.i(AirLocationImp.class, "[LOCATION][ID:" + id + "][BAIDU-CELL][FINAL: " + isFinal + "] X:" + location.getLatitude() + " Y:" + location.getLongitude()
					+ " Time:" + location.getTime() + " TimeGap:" + ts + "s");
				if (ts > -AirLocation.AIR_LOCATION_CELL_TIME_GAP)
				{
					listenerCallback(listener, id, LOCATION_TYPE_CELL_BAIDU, isFinal, location.getLatitude(), location.getLongitude(), location.getAltitude(), location.getSpeed(), Util.getCurrentDate());
				}
				else
				{
					Log.i(AirLocationImp.class, "[LOCATION][ID:" + id + "][BAIDU-CELL] Time gap is too long!!! Ignore!");
				}
				if (mClientCell != null)
				{
					mClientCell.unRegisterLocationListener(this);
					mClientCell.stop();
					Log.i(AirLocationImp.class, "[LOCATION][ID:" + id + "][BAIDU-CELL] Closed!");
				}
				mClientCell.unRegisterLocationListener(this);
			}

		};

		Log.i(AirLocationImp.class, "[LOCATION][ID:" + id + "][BAIDU-CELL] Getting...");
		mClientCell.registerLocationListener(locationCellListener);
		//mClientCell.requestLocation();
		if (mClientCell.isStarted())
			mClientCell.stop();
		mClientCell.start();
	}

	// =========================================
	// Release getting
	// =========================================

	private void doRelease(Context context, TimerParam param)
	{
		switch (param.type)
		{
			case LOCATION_TYPE_GPS:
			{
				LocationManager mLocMan = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
				if (param.listenerGps != null)
				{
					mLocMan.removeUpdates(param.listenerGps);
					Log.i(AirLocationImp.class, "[LOCATION][ID:" + param.id + "][GPS] Released!");
				}
				break;
			}
			case LOCATION_TYPE_CELL_BAIDU:
			{
				if (mClientCell != null)
				{
					mClientCell.unRegisterLocationListener(param.listenerCellBaidu);
					if (mClientCell.isStarted())
						mClientCell.stop();
					Log.i(AirLocationImp.class, "[LOCATION][ID:" + param.id + "][BAIDU-CELL] Released!");
				}
				break;
			}
		}
	}

}
