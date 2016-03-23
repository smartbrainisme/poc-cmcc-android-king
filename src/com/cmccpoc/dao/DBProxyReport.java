package com.cmccpoc.dao;

import java.util.List;
import com.cmccpoc.entity.AirReport;

public interface DBProxyReport
{
	public void DbReportLoad(List<AirReport> reports);

	public void DbReportNew(AirReport report);

	public void DbReportResultOk(String code);

	public void DbReportDelete(String code);

	public void DbReportClean();
}
