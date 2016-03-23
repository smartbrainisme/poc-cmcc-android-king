package com.cmccpoc.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.AirtalkeeVersionUpdate;
import com.airtalkee.sdk.OnVersionUpdateListener;
import com.airtalkee.sdk.entity.AirStatisticsNetworkByte;
import com.airtalkee.sdk.util.IOoperate;
import com.cmccpoc.R;
import com.cmccpoc.Util.AirMmiTimer;
import com.cmccpoc.Util.AirMmiTimerListener;
import com.cmccpoc.Util.Language;
import com.cmccpoc.Util.ThemeUtil;
import com.cmccpoc.Util.Util;
import com.cmccpoc.config.Config;

public class MenuAboutActivity extends ActivityBase implements OnClickListener, AirMmiTimerListener, OnVersionUpdateListener
{
	private LinearLayout statLayout, checkVersionLayout;
	private TextView statLayoutTime, statLayoutBytes, tvVersion, versionMsg, versionCode;
	private ImageView ivUpdateIcon;
	private int gStatRecv = 0;
	private int gStatSent = 0;
	private long gStatTime = 0;
	private IOoperate iOperate = null;
	private boolean isDownloading = false;
	private boolean isShow = false;

	public boolean isDownloading()
	{
		return isDownloading;
	}

	public void setDownloading(boolean isDownloading)
	{
		this.isDownloading = isDownloading;
	}

	private final String STAT_RECV = "STAT_RECV";
	private final String STAT_SENT = "STAT_SENT";
	private final String STAT_TIME = "STAT_TIME";
	
	private static MenuAboutActivity mInstance;
	public static MenuAboutActivity getInstance()
	{
		return mInstance;
	}

	@Override
	protected void onCreate(Bundle bundle)
	{
		// TODO Auto-generated method stub
		super.onCreate(bundle);
		mInstance = this;
		setRequestedOrientation(Config.screenOrientation);
		setContentView(R.layout.activity_tool_about);
		iOperate = new IOoperate();
		gStatRecv = iOperate.getInt(STAT_RECV, 0);
		gStatSent = iOperate.getInt(STAT_SENT, 0);
		gStatTime = iOperate.getLong(STAT_TIME);
		doInitView();
	}

	@Override
	protected void onResume()
	{
		// TODO Auto-generated method stub
		super.onResume();
		if (statLayout.getVisibility() == View.VISIBLE)
		{
			AirMmiTimer.getInstance().TimerRegister(this, this, false, false, 1000, true, null);
		}
		isShow = false;
		checkVersion();
	}

	@Override
	protected void onPause()
	{
		// TODO Auto-generated method stub
		super.onPause();
		if (statLayout.getVisibility() == View.VISIBLE)
		{
			AirMmiTimer.getInstance().TimerUnregister(this, this);
		}
	}

	@Override
	protected void onStart()
	{
		// TODO Auto-generated method stub
		super.onStart();
	}

	private void doInitView()
	{
		TextView ivTitle = (TextView) findViewById(R.id.tv_main_title);
		ivTitle.setText(R.string.talk_tools_about);
		View btnLeft = findViewById(R.id.menu_left_button);
		ImageView ivLeft = (ImageView) findViewById(R.id.bottom_left_icon);
		ivLeft.setImageResource(ThemeUtil.getResourceId(R.attr.theme_ic_topbar_back, this));
		btnLeft.setOnClickListener(this);

		RelativeLayout ivRightLay = (RelativeLayout) findViewById(R.id.talk_menu_right_button);
		ImageView ivRight = (ImageView) findViewById(R.id.bottom_right_icon);
		ivRight.setVisibility(View.GONE);
		ivRightLay.setVisibility(View.INVISIBLE);

		tvVersion = (TextView) findViewById(R.id.talk_tv_version);
		tvVersion.setText(getString(R.string.talk_version) + Config.VERSION_CODE);

		versionMsg = (TextView) findViewById(R.id.talk_tv_update_msg);

		versionCode = (TextView) findViewById(R.id.talk_tv_version_code);
		ivUpdateIcon = (ImageView) findViewById(R.id.talk_iv_update_icon);
		checkVersionLayout = (LinearLayout) findViewById(R.id.talk_check_version);
		checkVersionLayout.setOnClickListener(this);

		statLayout = (LinearLayout) findViewById(R.id.talk_tv_statistic);
		statLayoutTime = (TextView) findViewById(R.id.talk_tv_statistic_time);
		statLayoutBytes = (TextView) findViewById(R.id.talk_tv_statistic_bytes);
		findViewById(R.id.talk_iv_refresh).setOnClickListener(this);
	}

	@Override
	public void onClick(View v)
	{
		// TODO Auto-generated method stub
		switch (v.getId())
		{
			case R.id.menu_left_button:
			case R.id.bottom_left_icon:
			{
				finish();
				break;
			}
			case R.id.talk_check_version:
			{
				if(!isDownloading)
				{
					isShow = true;
					checkVersion();
				}
				break;
			}
			case R.id.talk_iv_refresh:
			{
				AirtalkeeAccount.getInstance().statisticsNetworkByteClean();
				gStatRecv = 0;
				gStatSent = 0;
				gStatTime = 0;
				try
				{
					iOperate.putInt(STAT_RECV, 0);
					iOperate.putInt(STAT_SENT, 0);
					iOperate.putLong(STAT_TIME, 0);
				}
				catch (Exception e)
				{
					// TODO: handle exception
				}
				statLayoutTime.setText(getString(R.string.talk_statistic_time) + "00:00:00");
				statLayoutBytes.setText(getString(R.string.talk_statistic_bytes) + "0.0K");
				Util.Toast(this, getString(R.string.talk_statistic_tip));
				break;
			}
			default:
				break;
		}
	}

	@Override
	public void onMmiTimer(Context context, Object userData)
	{
		AirStatisticsNetworkByte net = AirtalkeeAccount.getInstance().statisticsNetworkByte();

		int statRecvBytes = net.getRecvBytes() + gStatRecv;
		int statSentBytes = net.getSentBytes() + gStatSent;
		long statTime = net.getTimeTotal() + gStatTime;

		String timeString = String.format("%02d:%02d:%02d", statTime / 1000 / 60 / 60, statTime / 1000 / 60 % 60, statTime / 1000 % 60);
		statLayoutTime.setText(getString(R.string.talk_statistic_time) + timeString);

		String total = "";
		int bytesTotal = (statRecvBytes + statSentBytes) / 1024;
		if (bytesTotal > 1024) // M
		{
			total = "" + (bytesTotal / 1024) + "." + ((bytesTotal % 1024) / 100) + "M";
		}
		else
		// K
		{
			total = "" + bytesTotal + "." + (((statRecvBytes + statSentBytes) % 1024) / 100) + "K";
		}

		int bytesInterval = net.getRecvBytesInterval() + net.getSentBytesInterval();
		String bytesString = getString(R.string.talk_statistic_bytes) + total;
		bytesString += " (";
		if (net.getTimeInterval() / 1000 > 1)
		{
			bytesString += bytesInterval / (net.getTimeInterval() / 1000);
		}
		else
		{
			bytesString += bytesInterval;
		}
		bytesString += "B/S)";
		statLayoutBytes.setText(bytesString);

		try
		{
			iOperate.putInt(STAT_RECV, statRecvBytes);
			iOperate.putInt(STAT_SENT, statSentBytes);
			iOperate.putLong(STAT_TIME, statTime);
		}
		catch (Exception e)
		{
			// TODO: handle exception
		}
	}

	private void checkVersion()
	{
		String lang = Language.getLocalLanguage(MenuAboutActivity.this);
		String userId = AirtalkeeAccount.getInstance().getUserId();
		String versionCode = Util.appVersion(MenuAboutActivity.this);
		AirtalkeeVersionUpdate.getInstance().versionCheck(this, userId, Config.marketCode, lang, Config.VERSION_PLATFORM, Config.VERSION_TYPE, Config.model, versionCode);
	}

	@Override
	public void UserVersionUpdate(int versionFlag, String versionInfo, final String url)
	{
		// versionFlag = 1;
		if (versionFlag == 0)
		{
			// versionMsg.setVisibility(View.VISIBLE);
			versionMsg.setText(R.string.talk_verion_latest);
			versionMsg.setTextColor(getResources().getColor(R.color.update_text_none));
			versionCode.setVisibility(View.GONE);
			ivUpdateIcon.setVisibility(View.GONE);
		}
		else
		{
			ivUpdateIcon.setVisibility(View.VISIBLE);
			versionCode.setText(versionInfo);
			versionCode.setVisibility(View.VISIBLE);
			versionMsg.setText(R.string.talk_version_new);
			versionMsg.setTextColor(getResources().getColor(R.color.update_text_new));
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.talk_verion_title);
			builder.setMessage(versionInfo);
			builder.setPositiveButton(getString(R.string.talk_verion_upeate), new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int whichButton)
				{
					try
					{
						dialog.cancel();
						DialogVersionUpdate update = new DialogVersionUpdate(MenuAboutActivity.this, url);
						update.show();
						versionMsg.setText("更新中...");
						isDownloading = true;
						versionCode.setVisibility(View.GONE);
					}
					catch (Exception e)
					{
						// TODO: handle exception
					}
				}
			});
			if (versionFlag == 2)
			{
				builder.setCancelable(false);
			}
			else
			{
				builder.setNegativeButton(getString(R.string.talk_verion_cancel), new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int whichButton)
					{
						try
						{
							dialog.cancel();
						}
						catch (Exception e)
						{
							// TODO: handle exception
						}
					}
				});
			}
			Dialog d = builder.create();
			d.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
			if (isShow)
			{
				d.show();
			}
		}
	}
}
