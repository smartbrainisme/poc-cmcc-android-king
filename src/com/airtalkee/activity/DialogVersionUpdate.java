package com.airtalkee.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.TextView;
import android.widget.Toast;

import com.airtalkee.R;
import com.airtalkee.sdk.util.IOoperate;

@SuppressLint("SdCardPath")
public class DialogVersionUpdate extends Dialog implements android.view.View.OnClickListener
{
	public static float datesum=0;
	private float downsum=0.1f;
	private int size;
	private Context context = null;
	private File myTempFile;
	public String path = "";
	private IOoperate io;
	private TextView tvDowloadPro;
	private String currentFilePath = "";
	
	public DialogVersionUpdate(Context context, String path)
	{
		super(context, R.style.MyDialog);
		this.context = context;
		this.setCancelable(false);
		io = new IOoperate();
		this.path = path;
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_version_update);
		tvDowloadPro = (TextView) findViewById(R.id.tv_dowload_pro);
		tvDowloadPro.setOnClickListener(this);
		tvDowloadPro.setEnabled(false);
		tvDowloadPro.setClickable(false);
		getFile();
	}

	public void getFile()
	{
		try
		{
			if (path.equals(currentFilePath))
			{
				getDataSource(path);
			}
			currentFilePath = path;
			Runnable r = new Runnable()
			{
				public void run()
				{
					try
					{
						getDataSource(path);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			};
			new Thread(r).start();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@SuppressWarnings("resource")
	private void getDataSource(String strPath) throws Exception
	{
		if (!URLUtil.isNetworkUrl(strPath))
		{
			Toast.makeText(context, R.string.version_url_error, Toast.LENGTH_LONG).show();
			return;
		}
		else
		{
			URL myURL = new URL(strPath);
			FileOutputStream fos = null;

			myTempFile = new File("/sdcard/update");
			if (!myTempFile.exists())
			{
				myTempFile.mkdir();
			}
			File file = new File("/sdcard/update/" + "AirtalkeeSDT.apk");

			fos = new FileOutputStream(file);
			URLConnection conn = myURL.openConnection();
			conn.connect();
			datesum = conn.getContentLength();
			InputStream is = conn.getInputStream();
			if (is == null)
			{
				throw new RuntimeException("stream is null");
			}
			byte buf[] = new byte[256];
			int count = 0;
			do
			{
				count++;
				int numread = is.read(buf);
				if (numread <= 0)
				{
					break;
				}
				fos.write(buf, 0, numread);
				downsum = file.length();
				size = (int) ((downsum / datesum) * 100);
				if ((count % 100) == 0)
				{
					Message msg = handle.obtainMessage();
					msg.arg1 = size;
					handle.sendMessage(msg);
				}
			}
			while (true);

			try
			{
				is.close();
			}
			catch (Exception ex)
			{
			}
			io.putBoolean("downover", true);
			myTempFile = file;
			Message msg = handle.obtainMessage();
			msg.arg1 = size+1;
			handle.sendMessage(msg);
		}
	}

	private void openFile(File f)
	{
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(android.content.Intent.ACTION_VIEW);

		String type = getMIMEType(f);
		intent.setDataAndType(Uri.fromFile(f), type);
		context.startActivity(intent);

	}

	private String getMIMEType(File f)
	{
		String type = "";
		String fName = f.getName();
		String end = fName.substring(fName.lastIndexOf(".") + 1, fName.length()).toLowerCase();

		if (end.equals("apk"))
		{
			type = "application/vnd.android.package-archive";
		}
		else
		{
			type = "*";
		}
		if (end.equals("apk"))
		{
		}
		else
		{
			type += "/*";
		}
		return type;
	}

	@Override
	public void onClick(View v)
	{
		// TODO Auto-generated method stub
		if(downsum == datesum)
		{
			openFile(myTempFile);
		}
	}

	Handler handle = new Handler()
	{
		public void handleMessage(android.os.Message msg)
		{
			if (downsum == datesum)
			{
				tvDowloadPro.setText(R.string.version_update_install);
				tvDowloadPro.setEnabled(true);
				tvDowloadPro.setClickable(true);
			}
			else
			{
				String message = String.format(context.getString(R.string.version_update_pro), msg.arg1);
				tvDowloadPro.setText(message);
			}
		};
	};

}
