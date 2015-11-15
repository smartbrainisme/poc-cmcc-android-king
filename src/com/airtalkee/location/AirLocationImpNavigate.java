package com.airtalkee.location;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import com.airtalkee.Util.AirMmiTimer;
import com.airtalkee.Util.AirMmiTimerListener;
import com.airtalkee.Util.Util;
import com.airtalkee.sdk.audio.StreamPlayer;
import com.airtalkee.sdk.engine.AirPower;
import com.airtalkee.sdk.util.Log;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

public class AirLocationImpNavigate
{
	private static final String NAVIGATE_POWER_NAME = "NAVI-GPS";

	private static final int NAVIGATE_ACTION_START = 10;
	private static final int NAVIGATE_ACTION_STOP = 11;
	private static final int NAVIGATE_ACTION_REPORT = 100;

	private static final int NAVIGATE_TIME_MIN = 20 * 1000;
	private static final int NAVIGATE_TIME_MAX = 60 * 1000;
	private static final int NAVIGATE_TIME_VALID = 5 * 60 * 1000;
	private static final int NAVIGATE_DISTANCE_MIN = 10;
	LocationClient mClientCell = null;
	private Context context = null;
	private OnMapListener listener = null;
	private boolean isNavigateRunning = false;
	private boolean isNavigateDone = false;

	public void navigateRun(Context context)
	{
		this.context = context;
		mNaviThread.start();
	}

	public void navigateStart(OnMapListener listener)
	{
		if (isNavigateRunning)
		{
			navigateStop();
		}

		this.listener = listener;
		AirPower.PowerManagerWakeup(NAVIGATE_POWER_NAME);
		navigateAction(NAVIGATE_ACTION_START);
		isNavigateRunning = true;
	}

	public void navigateStop()
	{
		if (isNavigateRunning)
		{
			this.listener = null;
			isNavigateRunning = false;
			navigateAction(NAVIGATE_ACTION_STOP);
			AirPower.PowerManagerSleep(NAVIGATE_POWER_NAME);
		}
	}

	public void navigateOnce(OnMapListener listener)
	{
		this.listener = listener;
		naviListenerCallbackOnce();
	}

	private double mNaviLocationLatitude = 0;
	private double mNaviLocationLongitude = 0;
	private double mNaviLocationAltitude = 0;
	private float mNaviLocationSpeed = 0;
	private long mNaviTimestamp = 0;
	private int mNaviType = 0;
	private float mNaviLocationDirection = 0;

	public double getLocLatitude()
	{
		return mNaviLocationLatitude;
	}

	public double getLocLongitude()
	{
		return mNaviLocationLongitude;
	}

	public float getLocSpeed()
	{
		return mNaviLocationSpeed;
	}

	public float getLocDirection()
	{
		return mNaviLocationDirection;
	}

	public double getLocAltitude()
	{
		return mNaviLocationAltitude;
	}

	/*******************************************
	 * 
	 * Navigate engine
	 * 
	 *******************************************/

	private NavigateThread mNaviThread = new NavigateThread();
	private Handler mNaviHandler = null;
	private Handler mNaviMainHandler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			if (msg.what == NAVIGATE_ACTION_REPORT && msg.obj != null)
			{
				if (msg.obj instanceof Location)
				{
					Location location = (Location) msg.obj;
					naviListenerCallback(msg.arg1, location.getLatitude(), location.getLongitude(), location.getAltitude(), location.getBearing(), location.getSpeed());
				}
				else if (msg.obj instanceof BDLocation)
				{
					BDLocation location = (BDLocation) msg.obj;
					naviListenerCallback(msg.arg1, location.getLatitude(), location.getLongitude(), location.getAltitude(), location.getDirection(), location.getSpeed());
				}
			}
		}
	};

	private void navigateAction(int action)
	{
		if (mNaviHandler != null)
		{
			Message msg = mNaviHandler.obtainMessage();
			msg.what = action;
			mNaviHandler.sendMessage(msg);
		}
	}

	private class NavigateThread extends Thread
	{
		@Override
		public void run()
		{
			Log.i(StreamPlayer.class, "[LOCATION][NAVI] Thread begin!");
			Looper.prepare();
			mNaviHandler = new Handler(Looper.myLooper())
			{
				public void handleMessage(Message msg)
				{
					Log.i(StreamPlayer.class, "[LOCATION][NAVI] DO action = " + msg.what);
					switch (msg.what)
					{
						case NAVIGATE_ACTION_START:
						{
							naviEngineStart();
							break;
						}
						case NAVIGATE_ACTION_STOP:
						{
							naviEngineStop();
							break;
						}
						default:
							break;
					}
				}
			};
			Looper.loop();
			Log.i(StreamPlayer.class, "[LOCATION][NAVI] Thread end!");
		}
	}

	private void naviListenerCallbackOnce()
	{
		if (listener != null)
		{
			listener.OnMapLocation((mNaviLocationLatitude != 0 && mNaviLocationLongitude != 0), AirLocation.AIR_LOCATION_ID_ONCE, mNaviType, true, mNaviLocationLatitude, mNaviLocationLongitude, mNaviLocationAltitude, mNaviLocationDirection, mNaviLocationSpeed, Util.getCurrentDate());
		}
	}

	private void naviListenerCallback(int type, double latitude, double longitude, double altitude, float direction, float speed)
	{
		mNaviLocationLatitude = (latitude != AirLocationImp.CELL_ERROR && latitude != 0 ? latitude : mNaviLocationLatitude);
		mNaviLocationLongitude = (longitude != AirLocationImp.CELL_ERROR && longitude != 0 ? longitude : mNaviLocationLongitude);
		mNaviLocationAltitude = (altitude != AirLocationImp.CELL_ERROR && altitude != 0 ? altitude : mNaviLocationAltitude);
		mNaviLocationDirection = (direction != AirLocationImp.CELL_ERROR && direction != 0 ? direction : mNaviLocationDirection);
		mNaviLocationSpeed = (speed != AirLocationImp.CELL_ERROR && speed != 0 ? speed : mNaviLocationSpeed);
		mNaviType = type;

		if (listener != null)
		{
			listener.OnMapLocation((mNaviLocationLatitude != 0 && mNaviLocationLongitude != 0), AirLocation.AIR_LOCATION_ID_LOOP, type, true,
				mNaviLocationLatitude, mNaviLocationLongitude, mNaviLocationAltitude, mNaviLocationDirection, mNaviLocationSpeed,
				Util.getCurrentDate());
		}
	}

	private Location mNaviLocationCache = null;

	private LocationListener mNaviLocationListener = new LocationListener()
	{
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras)
		{
			Log.i(AirLocationImp.class, "[LOCATION][NAVI] onStatusChanged, provider = " + provider + " status = " + status);
		}

		@Override
		public void onProviderEnabled(String provider)
		{
			Log.i(AirLocationImp.class, "[LOCATION][NAVI] onProviderEnabled");
		}

		@Override
		public void onProviderDisabled(String provider)
		{
			Log.i(AirLocationImp.class, "[LOCATION][NAVI] onProviderDisabled");
		}

		@Override
		public void onLocationChanged(Location location)
		{
			boolean toReport = false;
			Log.i(AirLocationImp.class, "[LOCATION][NAVI] latitude: " + location.getLatitude() + ", longitude: " + location.getLongitude() + " Time:" + location.getTime());

			if (mNaviLocationCache == null || ((mNaviLocationCache.distanceTo(location) >= NAVIGATE_DISTANCE_MIN && location.getTime() - mNaviLocationCache.getTime() >= NAVIGATE_TIME_MIN / 2) || location.getTime() - mNaviLocationCache.getTime() >= NAVIGATE_TIME_MIN))
			{
				mNaviLocationCache = location;
				toReport = true;
				Log.i(AirLocationImp.class, "[LOCATION][NAVI] (DO Report) latitude: " + location.getLatitude() + ", longitude: " + location.getLongitude() + " Time:" + location.getTime());
			}

			if (toReport)
			{
				Message msg = mNaviMainHandler.obtainMessage();
				msg.what = NAVIGATE_ACTION_REPORT;
				msg.arg1 = AirLocationImp.LOCATION_TYPE_GPS;
				msg.obj = location;
				mNaviMainHandler.sendMessage(msg);

				mNaviTimestamp = System.currentTimeMillis();
			}
			isNavigateDone = true;
		}
	};

	private void naviEngineStart()
	{
		Log.i(AirLocationImp.class, "[LOCATION][NAVI] Getting...");
		mNaviTimestamp = System.currentTimeMillis();
		AirMmiTimer.getInstance().TimerRegister(context, naviTimerCell, false, false, NAVIGATE_TIME_MAX, true, null);
		LocationManager mLocMan = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		mLocMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, mNaviLocationListener);
		if (!mLocMan.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER))
		{
			naviEngineCell();
		}
	}

	private void naviEngineStop()
	{
		LocationManager mLocMan = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		mLocMan.removeUpdates(mNaviLocationListener);
		AirMmiTimer.getInstance().TimerUnregister(context, naviTimerCell);
		Log.i(AirLocationImp.class, "[LOCATION][NAVI] Released!");
	}

	private AirMmiTimerListener naviTimerCell = new AirMmiTimerListener()
	{
		@Override
		public void onMmiTimer(Context context, Object userData)
		{
			// TODO Auto-generated method stub
			Log.i(AirLocationImp.class, "[LOCATION][NAVI] Timeout! Check if already got GPS. If not, getting CELL or latest one...");
			if (!isNavigateDone)
			{
				if (mNaviLocationLatitude != 0 && mNaviLocationLongitude != 0 && System.currentTimeMillis() - mNaviTimestamp <= NAVIGATE_TIME_VALID / 2)
				{
					Log.i(AirLocationImp.class, "[LOCATION][NAVI] Timeout! Do UI refresh!");
					naviListenerCallback(AirLocationImp.LOCATION_TYPE_GPS, mNaviLocationLatitude, mNaviLocationLongitude, mNaviLocationAltitude, mNaviLocationDirection, mNaviLocationSpeed);
				}
				else if (System.currentTimeMillis() - mNaviTimestamp >= NAVIGATE_TIME_VALID)
				{
					Log.i(AirLocationImp.class, "[LOCATION][NAVI] Timeout! Do cell locate!");
					naviEngineCell();
				}
				else
				{
					Log.i(AirLocationImp.class, "[LOCATION][NAVI] Timeout! Ignore!");
				}
			}
			isNavigateDone = false;
		}
	};

	BDLocationListener locationCellListener = new BDLocationListener()
	{
		@Override
		public void onReceiveLocation(BDLocation location)
		{
			long ts = Util.getTimeGap(location.getTime());
			Log.i(AirLocationImp.class, "[LOCATION][NAVI][CELL] Latitude:" + location.getLatitude() + " Longitude:" + location.getLongitude() + " Time:" + location.getTime() + " TimeGap:" + ts + "s");
			if (ts > -AirLocation.AIR_LOCATION_CELL_TIME_GAP)
			{
				Message msg = mNaviMainHandler.obtainMessage();
				msg.what = NAVIGATE_ACTION_REPORT;
				msg.arg1 = AirLocationImp.LOCATION_TYPE_CELL_BAIDU;
				msg.obj = location;
				mNaviMainHandler.sendMessage(msg);
			}
			else
			{
				Log.i(AirLocationImp.class, "[LOCATION][NAVI][CELL] Time gap is too long!!! Ignore!");
			}
			mClientCell.unRegisterLocationListener(this);
			if (mClientCell.isStarted())
				mClientCell.stop();
			Log.i(AirLocationImp.class, "[LOCATION][NAVI][CELL] End!");
		}

	};

	private void naviEngineCell()
	{
		Log.i(AirLocationImp.class, "[LOCATION][NAVI][CELL] Start...");

		mClientCell = new LocationClient(context);
		if (mClientCell == null)
		{
			mClientCell = new LocationClient(context);
			LocationClientOption mClientCellOption = new LocationClientOption();
			mClientCellOption.setOpenGps(false);
			mClientCellOption.setAddrType("all");
			mClientCellOption.setCoorType("gcj02");
			// mClientCellOption.disableCache(true);
			mClientCellOption.setScanSpan(LocationClientOption.MIN_SCAN_SPAN);
			// mClientCellOption.setPriority(LocationClientOption.NetWorkFirst);
			// mClientCellOption.setPoiNumber(0);
			// mClientCellOption.setPoiDistance(1000);
			// mClientCellOption.setPoiExtraInfo(false);
			mClientCellOption.setTimeOut(NAVIGATE_TIME_MAX);
			mClientCell.setLocOption(mClientCellOption);
		}
		mClientCell.registerLocationListener(locationCellListener);
		// mClientCell.requestLocation();
		if (mClientCell.isStarted())
			mClientCell.stop();
		mClientCell.start();
	}

}
