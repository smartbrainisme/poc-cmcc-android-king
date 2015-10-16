package com.airtalkee.entity;

import android.net.Uri;

import com.airtalkee.sdk.AirtalkeeReport;

public class AirReport
{

	public static final int STATE_WAITING = 0;
	public static final int STATE_UPLOADING = 1;
	public static final int STATE_RESULT_OK = 5;
	public static final int STATE_RESULT_FAIL = 6;
	
	public static final int TARGET_NORMAL = 0;
	public static final int TARGET_TASK_DISPATCH = 1;

	private String code = "";
	private Uri resUri = null;
	private String resPath = "";
	private String resFileName = "";
	private String resContent = "";
	private int resSize = 0;
	private double locLatitude = 0;
	private double locLongitude = 0;
	private int target = TARGET_NORMAL;
	private int type = AirtalkeeReport.RESOURCE_TYPE_PICTURE;
	private String typeExtension = "";
	private int state;
	private int progress = 0;
	private String time = "";
	private String taskId = null;
	private String taskName = null;

	public String getCode()
	{
		return code;
	}

	public void setCode(String code)
	{
		this.code = code;
	}

	public String getResPath()
	{
		return resPath;
	}

	public void setResPath(String resPath)
	{
		this.resPath = resPath;
	}

	public String getResFileName()
	{
		return resFileName;
	}

	public void setResFileName(String resFileName)
	{
		this.resFileName = resFileName;
	}

	public void setResContent(String resContent)
	{
		this.resContent = resContent;
	}

	public String getResContent()
	{
		return resContent;
	}

	public void setResSize(int resSize)
	{
		this.resSize = resSize;
	}

	public int getResSize()
	{
		return resSize;
	}

	public double getLocLatitude()
	{
		return locLatitude;
	}

	public void setLocLatitude(double locLatitude)
	{
		this.locLatitude = locLatitude;
	}

	public double getLocLongitude()
	{
		return locLongitude;
	}

	public void setLocLongitude(double locLongitude)
	{
		this.locLongitude = locLongitude;
	}

	public int getType()
	{
		return type;
	}

	public void setType(int type)
	{
		this.type = type;
	}

	public void setTypeExtension(String typeExtension)
	{
		this.typeExtension = typeExtension;
	}

	public String getTypeExtension()
	{
		return typeExtension;
	}

	public int getState()
	{
		return state;
	}

	public void setState(int state)
	{
		this.state = state;
	}

	public int getProgress()
	{
		return progress;
	}

	public void setProgress(int progress)
	{
		this.progress = progress;
	}

	public void setResUri(Uri resUri)
	{
		this.resUri = resUri;
	}

	public Uri getResUri()
	{
		return resUri;
	}

	public void setTime(String time)
	{
		this.time = time;
	}

	public String getTime()
	{
		return time;
	}

	public int getTarget()
	{
		return target;
	}

	public void setTarget(int target)
	{
		this.target = target;
	}

	public String getTaskId()
	{
		return taskId;
	}

	public void setTaskId(String taskId)
	{
		this.taskId = taskId;
	}

	public String getTaskName()
	{
		return taskName;
	}

	public void setTaskName(String taskName)
	{
		this.taskName = taskName;
	}

}
