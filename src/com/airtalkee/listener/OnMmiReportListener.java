package com.airtalkee.listener;

public interface OnMmiReportListener
{
	public void onMmiReportResourceListRefresh();
	public void onMmiReportDel();
	public void onMmiReportProgress(int progress);
}
