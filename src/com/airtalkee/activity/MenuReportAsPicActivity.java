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
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
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
import com.airtalkee.widget.PhotoCamera;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

public class MenuReportAsPicActivity extends ActivityBase implements
		OnClickListener, OnMmiLocationListener, OnCheckedChangeListener
{

	private EditText report_detail;
	private ImageView report_image;
	private Button btn_post;
	private RadioGroup rgSelect;
	private RadioButton rbHigh, rbCompress;
	private boolean isUploading = false;

	private int picSize = 0;// 压缩图大小
	private Uri picUri = null; // 压缩图uri
	private String picPath = ""; // 压缩图path

	private int picSizeTemp = 0; // 原图大小
	private Uri picUriTemp = null; // 原图uri
	private String picPathTemp = ""; // 原图path

	private boolean isHighQuality = false;

	private String taskId = null;
	private String taskName = null;
	private String type = null;

	protected ImageLoader imageLoader = ImageLoader.getInstance();

	DisplayImageOptions options = new DisplayImageOptions.Builder().showImageForEmptyUri(R.drawable.msg_image).showImageOnFail(R.drawable.msg_image).resetViewBeforeLoading(true).cacheOnDisc(true).imageScaleType(ImageScaleType.EXACTLY).bitmapConfig(Bitmap.Config.RGB_565).considerExifParams(true).displayer(new FadeInBitmapDisplayer(300)).build();

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
			type = bundle.getString("type");
		}
		loadCamera(type);
	}

	private void loadCamera(String type)
	{
		if (type != null)
		{
			if (type.equals("camera"))
			{
				picPathTemp = Util.getImageTempFileName();
				picUriTemp = Uri.fromFile(new File(picPathTemp));
				// user-defined photograph
				Intent it = new Intent(this, PhotoCamera.class);
				it.putExtra(MediaStore.EXTRA_OUTPUT, picPathTemp);
				startActivityForResult(it, Const.image_select.REQUEST_CODE_CREATE_IMAGE);
				// system photograph
				// Intent i = new
				// Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
				// i.putExtra(MediaStore.EXTRA_OUTPUT, picUriTemp);
				// startActivityForResult(i,
				// Const.image_select.REQUEST_CODE_CREATE_IMAGE);
			}
			else if (type.equals("image"))
			{
				String status = Environment.getExternalStorageState();
				if (!status.equals(Environment.MEDIA_MOUNTED))
				{
					Util.Toast(this, getString(R.string.talk_insert_sd_card));
					return;
				}
				Intent localIntent = new Intent("android.intent.action.GET_CONTENT", null);
				localIntent.setType("image/*");
				startActivityForResult(localIntent, Const.image_select.REQUEST_CODE_BROWSE_IMAGE);
			}
		}
	}

	private void doInitView()
	{
		TextView ivTitle = (TextView) findViewById(R.id.tv_main_title);
		ivTitle.setText(R.string.talk_tools_report_pic);
		View btnLeft = findViewById(R.id.menu_left_button);
		ImageView ivLeft = (ImageView) findViewById(R.id.bottom_left_icon);
		ivLeft.setImageResource(ThemeUtil.getResourceId(R.attr.theme_ic_topbar_back, this));
		btnLeft.setOnClickListener(this);

		RelativeLayout ivRightLay = (RelativeLayout) findViewById(R.id.talk_menu_right_button);
		ImageView ivRight = (ImageView) findViewById(R.id.bottom_right_icon);
		ivRight.setVisibility(View.GONE);
		ivRightLay.setVisibility(View.INVISIBLE);
		findViewById(R.id.report_item_panel).setOnClickListener(this);

		report_detail = (EditText) findViewById(R.id.report_detail);
		report_image = (ImageView) findViewById(R.id.report_image);
		btn_post = (Button) findViewById(R.id.report_btn_post);
		rgSelect = (RadioGroup) findViewById(R.id.report_file_rg);
		rgSelect.setOnCheckedChangeListener(this);
		rbHigh = (RadioButton) findViewById(R.id.report_file_big);
		rbCompress = (RadioButton) findViewById(R.id.report_file_small);

		report_image.setOnClickListener(this);
		btn_post.setOnClickListener(this);
	}

	private void refreshUI()
	{
		if (isUploading)
		{
			report_detail.setEnabled(false);
			// report_image.setImageURI(picUri);
			imageLoader.displayImage(picUri.toString(), report_image);
		}
		else
		{
			report_detail.setEnabled(true);
			if (picUriTemp != null) // 高清
			{
				imageLoader.displayImage(picUriTemp.toString(), report_image);
				rbHigh.setText(getString(R.string.talk_tools_report_high) + "   " + MenuReportActivity.sizeMKB(picSizeTemp));
			}
			if (picUri != null) // 压缩
			{
				// report_image.setImageURI(picUri);
				imageLoader.displayImage(picUri.toString(), report_image);
				rbCompress.setText(getString(R.string.talk_tools_report_compress) + "   " + MenuReportActivity.sizeMKB(picSize));
			}
			// else
			// {
			// imageLoader.displayImage(picUriTemp.toString(), report_image);
			// report_image.setImageResource(R.drawable.report_default_pic);
			// }
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
				if (picUriTemp == null && picUri == null)
				{
					Util.Toast(this, getString(R.string.talk_report_upload_pic_err_select_pic));
					break;
				}
				isUploading = true;
				Util.hideSoftInput(this);
				refreshUI();
				Util.Toast(this, getString(R.string.talk_report_upload_getting_gps), 60, -1);
				// report_image_progress.setText(getString(R.string.talk_report_upload_getting_gps));
				AirLocation.getInstance(this).onceGet(this, 30);
				break;
			}
			case R.id.report_btn_take:
			case R.id.report_btn_native:
			case R.id.report_image:
			{
				// pictureQualitySelect(v.getId());
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
					picSizeTemp = AirServices.iOperator.getFileSize("", picPathTemp, true);
					picUriTemp = Uri.fromFile(new File(picPathTemp));
					resizePicture(true);
					picSize = AirServices.iOperator.getFileSize("", picPath, true);
					refreshUI();
					/*
					 * picUriTemp = Uri.fromFile(new File(picPathTemp)); picUri
					 * = picUriTemp; picPath = picPathTemp; resizePicture(true);
					 * picSize = AirServices.iOperator.getFileSize("",
					 * picPathTemp, true); refreshUI();
					 */
				}
				else
				{
					picUriTemp = null;
					picPathTemp = "";
					finish();
				}
				break;
			case Const.image_select.REQUEST_CODE_BROWSE_IMAGE:
				if (resultCode == RESULT_OK)
				{
					picPathTemp = UriUtil.getPath(this, data.getData());
					if (picPathTemp != null && picPathTemp.length() > 0)
					{
						picSizeTemp = AirServices.iOperator.getFileSize("", picPathTemp, true);
						picUriTemp = data.getData();
						resizePicture(true);
						picSize = AirServices.iOperator.getFileSize("", picPath, true);
						refreshUI();
					}
					else
					{
						Util.Toast(this, getString(R.string.talk_report_upload_vid_err_select_pic));
					}
				}
				else
				{
					finish();
				}
				break;
			default:
				break;
		}
	}

	/*
	 * public void pictureQualitySelect(final int id) { new
	 * AlertDialog.Builder(this
	 * ).setTitle(R.string.talk_quality_select).setItems(
	 * R.array.picture_quality, new DialogInterface.OnClickListener() {
	 * 
	 * @Override public void onClick(DialogInterface dialog, int which) {
	 * isHighQuality = which == 0; switch (id) { case R.id.report_image: case
	 * R.id.report_btn_take: { picPathTemp = Util.getImageTempFileName();
	 * picUriTemp = Uri.fromFile(new File(picPathTemp)); Intent i = new
	 * Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
	 * i.putExtra(MediaStore.EXTRA_OUTPUT, picUriTemp);
	 * startActivityForResult(i, Const.image_select.REQUEST_CODE_CREATE_IMAGE);
	 * break; } case R.id.report_btn_native: { String status =
	 * Environment.getExternalStorageState(); if
	 * (!status.equals(Environment.MEDIA_MOUNTED)) {
	 * Util.Toast(MenuReportAsPicActivity.this,
	 * getString(R.string.talk_insert_sd_card)); return; } Intent localIntent =
	 * new Intent("android.intent.action.GET_CONTENT", null);
	 * localIntent.setType("image/*"); startActivityForResult(localIntent,
	 * Const.image_select.REQUEST_CODE_BROWSE_IMAGE); break; } } } }).show(); }
	 */

	private void resizePicture(boolean toCreateFile)
	{
		if (!isHighQuality)
		{
			Bitmap picBitmap = BitmapUtil.getimage(picPathTemp);
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
			String path = isHighQuality ? picPathTemp : picPath;
			File file = new File(path);
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
			Util.Toast(this, getString(R.string.talk_tools_report_success), R.drawable.ic_success);
			finish();
		}
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId)
	{
		int rid = group.getCheckedRadioButtonId();
		switch (rid)
		{
			case R.id.report_file_big:
			{
				if (rbHigh.isChecked())
				{
					isHighQuality = true;
				}
				break;
			}
			case R.id.report_file_small:
			{
				if (rbHigh.isChecked())
				{
					isHighQuality = false;
				}
				break;
			}
			default:
				break;
		}
	}

}
