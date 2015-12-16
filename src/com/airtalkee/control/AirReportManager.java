package com.airtalkee.control;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.json.JSONObject;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import com.airtalkee.R;
import com.airtalkee.Util.Toast;
import com.airtalkee.Util.Util;
import com.airtalkee.activity.MenuReportAsPicActivity;
import com.airtalkee.activity.MenuReportAsVidActivity;
import com.airtalkee.activity.home.widget.AlertDialog;
import com.airtalkee.activity.home.widget.AlertDialog.DialogListener;
import com.airtalkee.dao.DBProxyReport;
import com.airtalkee.entity.AirReport;
import com.airtalkee.listener.OnMmiReportListener;
import com.airtalkee.sdk.AirtalkeeReport;
import com.airtalkee.sdk.OnReportListener;
import com.airtalkee.sdk.util.IOoperate;
import com.airtalkee.sdk.util.Log;
import com.airtalkee.sdk.util.Utils;
import com.airtalkee.services.AirServices;

public class AirReportManager implements OnReportListener
{
	private static AirReportManager mInstance;
	private static DBProxyReport mDbProxy = null;
	private OnMmiReportListener reportListener = null;
	private IOoperate iOperate = new IOoperate();
	private List<AirReport> reports = new ArrayList<AirReport>();
	private AirReport reportDoing = null;
	private boolean isReportLoaded = false;
	

	public static AirReportManager getInstance()
	{
		if (mInstance == null)
		{
			mInstance = new AirReportManager();
			mDbProxy = (DBProxyReport) AirServices.getInstance().dbProxy();
			AirtalkeeReport.getInstance().setReportListener(mInstance);
		}
		return mInstance;
	}

	public void setReportListener(OnMmiReportListener listener)
	{
		this.reportListener = listener;
	}

	public void loadReports()
	{
		if (!isReportLoaded)
		{
			mDbProxy.DbReportLoad(reports);
			isReportLoaded = true;
		}
	}

	public List<AirReport> getReports()
	{
		return reports;
	}

	public AirReport getReport(String code)
	{
		AirReport report = null;
		for (int i = 0; i < reports.size(); i++)
		{
			if (TextUtils.equals(reports.get(i).getCode(), code))
			{
				report = reports.get(i);
				break;
			}
		}
		return report;
	}

	public void Report(int resType, String resTypeExtension, Uri resUri, String resPath, String resContent, int resSize, double locationX, double locationY)
	{
		Report(null, null, resType, resTypeExtension, resUri, resPath, resContent, resSize, locationX, locationY);
	}

	public void Report(String taskId, String taskName, int resType, String resTypeExtension, Uri resUri, String resPath, String resContent, int resSize, double locationX, double locationY)
	{
		boolean toReport = true;
		if (Utils.isEmpty(resPath))
		{
			toReport = false;
		}
		for (int i = 0; i < reports.size() && toReport; i++)
		{
			if (resPath.equals(reports.get(i).getResPath()))
			{
				toReport = false;
			}
		}

		if (toReport)
		{
			String fileName = "";
			if (!TextUtils.isEmpty(resPath))
			{
				int pos = TextUtils.lastIndexOf(resPath, '/');
				if (pos + 1 < resPath.length())
					fileName = resPath.substring(pos + 1);
			}
			AirReport report = new AirReport();
			report.setCode(Utils.getCurrentTimeInMillis() + "");
			report.setResUri(resUri);
			report.setResPath(resPath);
			report.setResFileName(fileName);
			report.setResContent(resContent);
			report.setResSize(resSize);
			report.setLocLatitude(locationX);
			report.setLocLongitude(locationY);
			report.setType(resType);
			report.setTypeExtension(resTypeExtension);
			report.setTime(Util.getCurrentTime());
			report.setTaskId(taskId);
			report.setTaskName(taskName);
			if (!TextUtils.isEmpty(taskId))
				report.setTarget(AirReport.TARGET_TASK_DISPATCH);

			reportActionDoNew(report);
		}
	}

	public void ReportResend(AirReport report)
	{
		AirReport r = new AirReport();
		r.setCode(Utils.getCurrentTimeInMillis() + "");
		r.setResUri(report.getResUri());
		r.setResPath(report.getResPath());
		r.setResContent(report.getResContent());
		r.setResSize(report.getResSize());
		r.setLocLatitude(report.getLocLatitude());
		r.setLocLongitude(report.getLocLongitude());
		r.setType(report.getType());
		r.setTypeExtension(report.getTypeExtension());
		r.setTime(Util.getCurrentTime());
		r.setTaskId(report.getTaskId());
		r.setTaskName(report.getTaskName());
		if (!TextUtils.isEmpty(report.getTaskId()))
			r.setTarget(AirReport.TARGET_TASK_DISPATCH);

		reportActionDoNew(r);
	}

	public void ReportRetry(String code)
	{
		AirReport report = getReport(code);
		reportActionDoRetry(report);
	}

	public void ReportClean()
	{
		List<AirReport> reportsTemp = new ArrayList<AirReport>();
		for (int i = 0; i < reports.size(); i++)
		{
			AirReport report = reports.get(i);
			if (report != null && report.getState() != AirReport.STATE_RESULT_OK)
			{
				reportsTemp.add(report);
			}
		}
		reports.clear();
		reports.addAll(reportsTemp);

		mDbProxy.DbReportClean();

		if (reportListener != null)
		{
			reportListener.onMmiReportResourceListRefresh();
		}
	}

	public void ReportDelete(String code)
	{
		AirReport report = getReport(code);
		if (report != null && report.getState() != AirReport.STATE_UPLOADING)
		{
			reports.remove(report);
			if (reportListener != null)
			{
				reportListener.onMmiReportDel();
			}
			mDbProxy.DbReportDelete(report.getCode());
		}
	}

	public void ReportsDetele(Map<String, AirReport> reports)
	{
		if (null != reports && reports.size() > 0)
		{
			Iterator<Entry<String, AirReport>> iterator = reports.entrySet().iterator();
			while (iterator.hasNext())
			{
				Map.Entry<String, AirReport> entry = (Map.Entry<String, AirReport>) iterator.next();
				ReportDelete(entry.getKey());
			}
		}
	}

	private AirReport reportGetTask(String code)
	{
		AirReport report = null;
		for (int i = 0; i < reports.size(); i++)
		{
			if (TextUtils.equals(reports.get(i).getCode(), code))
			{
				if (i + 1 < reports.size())
				{
					report = reports.get(i + 1);
				}
				break;
			}
		}
		return report;
	}

	/********************************************
	 * 
	 * Actions
	 * 
	 ********************************************/

	private void reportActionDoNew(AirReport report)
	{
		if (reportDoing == null)
		{
			if (reportToServer(report))
			{
				reports.add(report);
				report.setProgress(0);
				report.setState(AirReport.STATE_UPLOADING);
				reportDoing = report;
				mDbProxy.DbReportNew(report);
			}
		}
		else
		{
			reports.add(report);
			report.setProgress(0);
			report.setState(AirReport.STATE_WAITING);
			mDbProxy.DbReportNew(report);
		}

		if (reportListener != null)
		{
			reportListener.onMmiReportResourceListRefresh();
		}
		broadCastReportState();
	}

	private void reportActionDoRetry(AirReport report)
	{
		if (report != null && report.getState() == AirReport.STATE_RESULT_FAIL)
		{
			if (reportDoing == null)
			{
				if (reportToServer(report))
				{
					report.setProgress(0);
					report.setState(AirReport.STATE_UPLOADING);
					reportDoing = report;
				}
			}
			else
			{
				report.setProgress(0);
				report.setState(AirReport.STATE_WAITING);
			}

			if (reportListener != null)
			{
				reportListener.onMmiReportResourceListRefresh();
			}
			broadCastReportState();
		}
	}

	private void reportActionProgress(int progress)
	{
		if (reportDoing != null)
		{
			reportDoing.setProgress(progress);
			if (reportListener != null)
			{
				reportListener.onMmiReportResourceListRefresh();
			}
		}
	}

	private void reportActionResult(int statusCode, String resId)
	{
		if (reportDoing != null)
		{
			if (statusCode == AirtalkeeReport.RESOURCE_STATUS_CODE_OK)
			{
				mDbProxy.DbReportResultOk(reportDoing.getCode());
				Toast myToast = Toast.makeText1(AirServices.getInstance(), R.drawable.ic_success, AirServices.getInstance().getString(R.string.talk_tools_report_success), Toast.LENGTH_LONG);
				myToast.show();
				if (reportDoing.getType() == AirtalkeeReport.RESOURCE_TYPE_PICTURE)
				{
					MenuReportAsPicActivity.getInstance().finish();
				}
				else
				{
					MenuReportAsVidActivity.getInstance().finish();
				}
				// finish()
			}
			else if (statusCode == AirtalkeeReport.RESOURCE_STATUS_CODE_ERR_SPACE_OVERFLOW)
			{
				Util.Toast(AirServices.getInstance(), AirServices.getInstance().getString(R.string.talk_report_upload_err_space_overflow));
			}
			else
			{
				if (reportDoing.getType() == AirtalkeeReport.RESOURCE_TYPE_PICTURE)
				{
					MenuReportAsPicActivity context = MenuReportAsPicActivity.getInstance();
					AlertDialog dialog = new AlertDialog(context, context.getString(R.string.talk_tools_report_fail), context.getString(R.string.talk_tools_report_fail_tip), context.getString(R.string.talk_tools_report_continue), context.getString(R.string.talk_ok_2), new DialogListener()
					{
						@Override
						public void onClickOk(int id, boolean isChecked)
						{
							
						}

						@Override
						public void onClickOk(int id, Object obj)
						{
							MenuReportAsPicActivity.getInstance().finish();
						}

						@Override
						public void onClickCancel(int id)
						{
							// TODO Auto-generated method stub
							MenuReportAsPicActivity.getInstance().reportPost();
						}
					}, -1);
					dialog.show();
					// Util.Toast(AirServices.getInstance(),
					// AirServices.getInstance().getString(R.string.talk_report_upload_pic_err),
					// R.drawable.ic_error);
				}
				else
				{
					MenuReportAsVidActivity context = MenuReportAsVidActivity.getInstance();
					AlertDialog dialog = new AlertDialog(context, context.getString(R.string.talk_tools_report_fail), context.getString(R.string.talk_tools_report_fail_tip), context.getString(R.string.talk_tools_report_continue), context.getString(R.string.talk_ok_2), new DialogListener()
					{
						@Override
						public void onClickOk(int id, boolean isChecked)
						{
						}

						@Override
						public void onClickOk(int id, Object obj)
						{
							MenuReportAsVidActivity.getInstance().finish();
						}

						@Override
						public void onClickCancel(int id)
						{
							MenuReportAsVidActivity.getInstance().reportPost();
						}
					}, -1);
					dialog.show();
					// Util.Toast(AirServices.getInstance(),
					// AirServices.getInstance().getString(R.string.talk_report_upload_vid_err),
					// R.drawable.ic_error);
				}
			}
			reportDoing.setState(statusCode == AirtalkeeReport.RESOURCE_STATUS_CODE_OK ? AirReport.STATE_RESULT_OK : AirReport.STATE_RESULT_FAIL);
			reportDoing.setProgress(0);
			broadCastReportState();

			AirReport report = reportGetTask(reportDoing.getCode());
			if (report != null)
			{
				if (reportToServer(report))
				{
					report.setProgress(0);
					report.setState(AirReport.STATE_UPLOADING);
					reportDoing = report;
				}
				else
				{
					reportDoing = null;
				}
			}
			else
			{
				reportDoing = null;
			}

			if (reportListener != null)
			{
				reportListener.onMmiReportResourceListRefresh();
			}
		}
	}

	private boolean reportToServer(AirReport report)
	{
		boolean isOk = false;
		byte[] data = iOperate.readByteFile("", report.getResPath(), true);
		if (data != null)
		{
			if (report.getTarget() == AirReport.TARGET_NORMAL)
				AirtalkeeReport.getInstance().ReportResource(report.getType(), report.getTypeExtension(), data, report.getResContent(), AirtalkeeReport.LOCATION_TYPE_CELL, report.getLocLatitude() + "", report.getLocLongitude() + "");
			else
				AirtalkeeReport.getInstance().ReportResource(report.getTaskId(), report.getType(), report.getTypeExtension(), data, report.getResContent(), AirtalkeeReport.LOCATION_TYPE_CELL, report.getLocLatitude() + "", report.getLocLongitude() + "");
			// AirtalkeeReport.getInstance().ReportResource(true,
			// report.getType(), report.getResFileName(), data,
			// report.getResContent(), AirtalkeeReport.LOCATION_TYPE_CELL,
			// report.getLocLatitude() + "", report.getLocLongitude() + "");
			isOk = true;
		}
		return isOk;
	}

	public AirReport getCurrentReportDoing()
	{
		return reportDoing;
	}

	/********************************************
	 * 
	 * Events
	 * 
	 ********************************************/

	@Override
	public void onReportResourceProgress(int progress)
	{
		// TODO Auto-generated method stub
		reportActionProgress(progress);
	}

	@Override
	public void onReportResourceUploaded(int statusCode, String resId)
	{
		// TODO Auto-generated method stub
		reportActionResult(statusCode, resId);
	}

	/********************************************
	 * 
	 * BroadCast
	 * 
	 ********************************************/
	public static final String AIR_ACTION = "com.airtalkee.action";
	public static final int OPER_UPLOAD_PICTURE = 1000;
	public static final int OPER_UPLOAD_VIDEO = 1001;

	private void broadCastReportState()
	{
		Intent intent = new Intent(AIR_ACTION);

		if (reportDoing != null)
		{
			int oper = OPER_UPLOAD_VIDEO;
			if (reportDoing.getType() == AirtalkeeReport.RESOURCE_TYPE_PICTURE)
				oper = OPER_UPLOAD_PICTURE;
			intent.putExtra("operCode", oper);
			try
			{
				JSONObject obj = new JSONObject();
				obj.put("state", reportDoing.getState());
				Log.d(AirReportManager.class, "------state----=" + obj.optInt("state"));

				intent.putExtra("json", obj.toString());
				if (AirServices.getInstance() != null)
				{
					AirServices.getInstance().sendBroadcast(intent);
				}
			}
			catch (Exception e)
			{
				Log.e(AirReportManager.class, "broadCastReportState Exception e" + e.toString());
			}
		}
	}
}
