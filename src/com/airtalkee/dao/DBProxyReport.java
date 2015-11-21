package com.airtalkee.dao;

import java.util.List;
import com.airtalkee.entity.AirReport;

public interface DBProxyReport
{
	public void DbReportLoad(List<AirReport> reports);

	public void DbReportNew(AirReport report);

	public void DbReportResultOk(String code);

	public void DbReportDelete(String code);

	public void DbReportClean();
}
