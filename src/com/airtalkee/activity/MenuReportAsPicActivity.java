package com.airtalkee.activity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airtalkee.R;
import com.airtalkee.Util.BitmapUtil;
import com.airtalkee.Util.Const;
import com.airtalkee.Util.ThemeUtil;
import com.airtalkee.Util.UriUtil;
import com.airtalkee.Util.Util;
import com.airtalkee.config.Config;
import com.airtalkee.control.AirReportManager;
import com.airtalkee.listener.OnMmiLocationListener;
import com.airtalkee.location.AirLocation;
import com.airtalkee.sdk.AirtalkeeReport;
import com.airtalkee.sdk.util.Log;
import com.airtalkee.services.AirServices;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

public class MenuReportAsPicActivity extends ActivityBase implements OnClickListener, OnMmiLocationListener
{

	private EditText report_detail;
	private TextView report_image_progress;
	private ImageView report_image;
	private TextView report_image_size;
	private Button btn_take, btn_native, btn_post, btn_image_clean;
	private boolean isUploading = false;
	private int picSize = 0;
	private Uri picUri = null;
	private String picPath = "";
	private Uri picUriTemp = null;
	private String picPathTemp = "";
	private boolean isHighQuality = false;
	
	private String taskId = null;
	private String taskName = null;

	protected ImageLoader imageLoader = ImageLoader.getInstance();
	
	DisplayImageOptions 	options = new DisplayImageOptions.Builder().showImageForEmptyUri(R.drawable.msg_image).showImageOnFail(R.drawable.msg_image).resetViewBeforeLoading(true)
		.cacheOnDisc(true).imageScaleType(ImageScaleType.EXACTLY).bitmapConfig(Bitmap.Config.RGB_565).considerExifParams(true)
		.displayer(new FadeInBitmapDisplayer(300)).build();
	
	@Override
	protected void onCreate(Bundle bundle)
	{
		// TODO Auto-generated method stub
		super.onCreate(bundle);
		setRequestedOrientation(Config.screenOrientation);
		setContentView(R.layout.activity_tool_report_as_pic);
		doInitView();
		refreshUI();
		
		bundle = getIntent().getExtras();
		if (bundle != null)
		{
			taskId = bundle.getString("taskId");
			taskName = bundle.getString("taskName");
		}
	}

	private void doInitView()
	{
		TextView ivTitle = (TextView) findViewById(R.id.tv_main_title);
		ivTitle.setText(R.string.talk_tools_report_pic);
		View btnLeft = findViewById(R.id.menu_left_button);
		ImageView ivLeft = (ImageView) findViewById(R.id.bottom_left_icon);
		ivLeft.setImageResource(ThemeUtil.getResourceId(R.attr.theme_ic_topbar_back, this) );
		btnLeft.setOnClickListener(this);

		RelativeLayout ivRightLay = (RelativeLayout) findViewById(R.id.talk_menu_right_button);
		ImageView ivRight = (ImageView) findViewById(R.id.bottom_right_icon);
		ivRight.setVisibility(View.GONE);
		ivRightLay.setVisibility(View.INVISIBLE);
		findViewById(R.id.report_item_panel).setOnClickListener(this);

		report_detail = (EditText) findViewById(R.id.report_detail);
		report_image_progress = (TextView) findViewById(R.id.report_image_progress);
		report_image = (ImageView) findViewById(R.id.report_image);
		report_image_size = (TextView) findViewById(R.id.report_image_size);
		btn_image_clean = (Button) findViewById(R.id.report_image_clean);
		btn_take = (Button) findViewById(R.id.report_btn_take);
		btn_native = (Button) findViewById(R.id.report_btn_native);
		btn_post = (Button) findViewById(R.id.report_btn_post);
		report_image.setOnClickListener(this);
		btn_image_clean.setOnClickListener(this);
		btn_take.setOnClickListener(this);
		btn_native.setOnClickListener(this);
		btn_post.setOnClickListener(this);
	}

	private void refreshUI()
	{
		if (isUploading)
		{
			report_detail.setEnabled(false);
			report_image_progress.setVisibility(View.VISIBLE);
			btn_take.setEnabled(false);
			btn_native.setEnabled(false);
//			report_image.setImageURI(picUri);
			imageLoader.displayImage(picUri.toString(), report_image);
			report_image_size.setText(MenuReportActivity.sizeMKB(picSize));
			btn_image_clean.setVisibility(View.GONE);
		}
		else
		{
			report_detail.setEnabled(true);
			report_image_progress.setVisibility(View.GONE);
			btn_take.setEnabled(true);
			btn_native.setEnabled(true);
			if (picUri != null)
			{
				btn_image_clean.setVisibility(View.VISIBLE);
//				report_image.setImageURI(picUri);
				imageLoader.displayImage(picUri.toString(), report_image);
				report_image_size.setText(MenuReportActivity.sizeMKB(picSize));
				report_image_size.setVisibility(View.VISIBLE);
			}
			else
			{
				btn_image_clean.setVisibility(View.GONE);
				report_image.setImageResource(R.drawable.report_default_pic);
				report_image_size.setVisibility(View.INVISIBLE);
			}
		}
	}

	@Override
	public void onClick(View v)
	{
		// TODO Auto-generated method stub
		switch (v.getId())
		{
			case R.id.menu_left_button:
			case R.id.bottom_left_icon:
				finish();
				break;
			case R.id.report_btn_post:
			{
				if (isUploading)
				{
					Util.Toast(this, getString(R.string.talk_report_uploading));
					break;
				}
				if (picUri == null)
				{
					Util.Toast(this, getString(R.string.talk_report_upload_pic_err_select_pic));
					break;
				}
				isUploading = true;
				Util.hideSoftInput(this);
				refreshUI();

				report_image_progress.setText(getString(R.string.talk_report_upload_getting_gps));
				AirLocation.getInstance(this).onceGet(this, 30);
				break;
			}
			case R.id.report_btn_take:
			case R.id.report_btn_native:
			case R.id.report_image:
			{
				pictureQualitySelect(v.getId());
				break;
			}
			case R.id.image_pic:
			{
				break;
			}
			case R.id.report_item_panel:
			{
				Util.hideSoftInput(this);
				break;
			}
			case R.id.report_image_clean:
			{
				if (isUploading)
				{
					Util.Toast(this, getString(R.string.talk_report_uploading));
					break;
				}
				picUri = null;
				refreshUI();
				break;
			}
		}
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode)
		{
			case Const.image_select.REQUEST_CODE_CREATE_IMAGE:
				if (resultCode == RESULT_OK)
				{
					picUri = picUriTemp;
					picPath = picPathTemp;
					resizePicture(false);
					picSize = AirServices.iOperator.getFileSize("", picPath, true);
				}
				else
				{
					picUriTemp = null;
					picPathTemp = "";
				}
				refreshUI();

				break;
			case Const.image_select.REQUEST_CODE_BROWSE_IMAGE:
				if (resultCode == RESULT_OK)
				{
					String filePath = UriUtil.getPath(this, data.getData());
					
					if (filePath != null)
					{
						picUri = data.getData();
						picPath = filePath;
						resizePicture(true);
						picSize = AirServices.iOperator.getFileSize("", picPath, true);
						refreshUI();
					}
					else
					{
						Util.Toast(this, getString(R.string.talk_report_upload_vid_err_select_pic));
					}
				}
				break;
			default:
				break;
		}
	}

	
	public void pictureQualitySelect(final int id)
	{
		new AlertDialog.Builder(this).setTitle(R.string.talk_quality_select).setItems(R.array.picture_quality, new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				isHighQuality = which == 0;
				switch(id)
				{
					case R.id.report_image:
					case R.id.report_btn_take:
					{
						picPathTemp = Util.getImageTempFileName();
						picUriTemp = Uri.fromFile(new File(picPathTemp));
						Intent i = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
						i.putExtra(MediaStore.EXTRA_OUTPUT, picUriTemp);
						startActivityForResult(i, Const.image_select.REQUEST_CODE_CREATE_IMAGE);
						break;
					}
					case R.id.report_btn_native:
					{
						String status = Environment.getExternalStorageState();
						if (!status.equals(Environment.MEDIA_MOUNTED))
						{
							Util.Toast(MenuReportAsPicActivity.this, getString(R.string.talk_insert_sd_card));
							return;
						}
						Intent localIntent = new Intent("android.intent.action.GET_CONTENT", null);
						localIntent.setType("image/*");
						startActivityForResult(localIntent, Const.image_select.REQUEST_CODE_BROWSE_IMAGE);
						break;
					}
				}
			}
		}).show();
	}
	
	private void resizePicture(boolean toCreateFile)
	{
		if(!isHighQuality)
		{
			Bitmap picBitmap =  BitmapUtil.getimage(picPath);
			if (picBitmap != null)
			{
				byte[] bitmapData = null;
				ByteArrayOutputStream streamOut = new ByteArrayOutputStream();
				picBitmap.compress(CompressFormat.JPEG, 80, streamOut);
				bitmapData = streamOut.toByteArray();
				if (toCreateFile)
				{
					picPath = Util.getImageTempFileName();
					picUri = Uri.fromFile(new File(picPath));
					AirServices.iOperator.imageWrite("", picPath, bitmapData);
				}
				else
				{
					AirServices.iOperator.deleteFile(picPath);
					AirServices.iOperator.imageWrite("", picPath, bitmapData);
				}
				try
				{
					streamOut.reset();
					streamOut.close();
				}
				catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				streamOut = null;
				picBitmap.recycle();
				System.gc();
			}
		}
	}

	@Override
	public void onLocationChanged(boolean isOk, int id, int type, double latitude, double longitude, double altitude, float speed, String time)
	{
		// TODO Auto-generated method stub
		if (isUploading && id == AirLocation.AIR_LOCATION_ID_ONCE)
		{
			String detail = report_detail.getText().toString();
			File file = new File(picPath);
			Uri uri = Uri.fromFile(file);
			try
			{
				Date date = new Date(file.lastModified());
				if (date != null)
				{
					if (!TextUtils.isEmpty(detail))
						detail += "\r\n\r\n";
					
					SimpleDateFormat sfd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					String date_string = sfd.format(date);
					detail += getString(R.string.talk_report_upload_capture_time) + " [" + date_string + "]";
				}
			}
			catch (Exception e)
			{
				detail = report_detail.getText().toString();
			}
			
			Log.i(MenuReportAsPicActivity.class, "ReportPicture: TASK[" + taskId + "][" + taskName + "] text=[" + report_detail.getText().toString() + "] x=[" + latitude + "] y=[" + longitude + "]");
			AirReportManager.getInstance().Report(taskId, taskName, AirtalkeeReport.RESOURCE_TYPE_PICTURE, "jpg", uri, picPath, detail, picSize, latitude, longitude);

			isUploading = false;
			finish();
		}
	}

}
