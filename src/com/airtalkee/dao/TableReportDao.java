package com.airtalkee.dao;

import java.io.File;
import java.util.List;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import com.airtalkee.entity.AirReport;
import com.airtalkee.sdk.util.Log;

public class TableReportDao
{

	private static final TableReportDao instance = new TableReportDao();

	private TableReportDao()
	{};

	private static DBHelp dbHelp;

	protected static TableReportDao getInstance(DBHelp db)
	{
		dbHelp = db;
		return instance;
	}

	/********************************
	 * Message List
	 ********************************/

	protected void ReportLoad(List<AirReport> reports)
	{
		SQLiteDatabase db = dbHelp.DatabaseReadableGet();
		if (db != null)
		{
			String sql = "SELECT * FROM " + DBDefine.db_report + " WHERE " + DBDefine.t_report.UID + "=" + dbHelp.getUid();
			reports.clear();
			try
			{
				Cursor c = db.rawQuery(sql, null);
				while (c.moveToNext())
				{
					AirReport report = new AirReport();
					report.setCode(c.getString(c.getColumnIndex(DBDefine.t_report.code)));
					report.setResPath(c.getString(c.getColumnIndex(DBDefine.t_report.resPath)));
					report.setResUri(Uri.fromFile(new File(report.getResPath())));
					report.setResContent(c.getString(c.getColumnIndex(DBDefine.t_report.resContent)));
					report.setResSize(c.getInt(c.getColumnIndex(DBDefine.t_report.resSize)));
					report.setTime(c.getString(c.getColumnIndex(DBDefine.t_report.time)));
					report.setLocLatitude(Double.parseDouble(c.getString(c.getColumnIndex(DBDefine.t_report.locLatitude))));
					report.setLocLongitude(Double.parseDouble(c.getString(c.getColumnIndex(DBDefine.t_report.locLongitude))));
					report.setType(c.getInt(c.getColumnIndex(DBDefine.t_report.type)));
					report.setTypeExtension(c.getString(c.getColumnIndex(DBDefine.t_report.typeExt)));
					report.setState(c.getInt(c.getColumnIndex(DBDefine.t_report.state)) == 1 ? AirReport.STATE_RESULT_OK : AirReport.STATE_RESULT_FAIL);
					report.setProgress(0);
					reports.add(report);
				}
				c.close();
			}
			catch (Exception e)
			{
				Log.e(DBHelp.class, "[SQL EXCEPTION] " + sql + " -> " + e.getMessage());
			}
			dbHelp.DatabaseReadableClose(db);
		}
	}

	protected void ReportNew(AirReport report)
	{
		ContentValues cv = new ContentValues();
		cv.put(DBDefine.t_report.UID, dbHelp.getUid());
		cv.put(DBDefine.t_report.code, report.getCode());
		cv.put(DBDefine.t_report.resPath, report.getResPath());
		cv.put(DBDefine.t_report.resContent, report.getResContent());
		cv.put(DBDefine.t_report.resSize, report.getResSize());
		cv.put(DBDefine.t_report.locLatitude, report.getLocLatitude() + "");
		cv.put(DBDefine.t_report.locLongitude, report.getLocLongitude() + "");
		cv.put(DBDefine.t_report.time, report.getTime());
		cv.put(DBDefine.t_report.type, report.getType());
		cv.put(DBDefine.t_report.typeExt, report.getTypeExtension());
		cv.put(DBDefine.t_report.state, 0);
		dbHelp.insert(DBDefine.db_report, cv);
	}

	protected void ReportResultOk(String code)
	{
		String sql = "UPDATE " + DBDefine.db_report + " SET " + DBDefine.t_report.state + " = 1 " + " WHERE " + DBDefine.t_report.UID + "=" + dbHelp.getUid() + " AND "
			+ DBDefine.t_report.code + " = '" + code + "'";
		dbHelp.update(sql);
	}

	protected void ReportDelete(String code)
	{
		String sql = "DELETE FROM " + DBDefine.db_report + " WHERE " + DBDefine.t_report.UID + "=" + dbHelp.getUid() + " AND " + DBDefine.t_report.code + " = '" + code + "'";
		dbHelp.del(sql);
	}

	protected void ReportClean()
	{
		String sql = "DELETE FROM " + DBDefine.db_report + " WHERE " + DBDefine.t_report.UID + "=" + dbHelp.getUid() + " AND " + DBDefine.t_report.state + " = 1";
		dbHelp.del(sql);
	}

}
