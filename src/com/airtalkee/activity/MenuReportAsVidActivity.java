package com.airtalkee.activity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.airtalkee.R;
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
import com.airtalkee.sdk.util.Utils;
import com.airtalkee.services.AirServices;
import com.airtalkee.widget.VideoCamera;

public class MenuReportAsVidActivity extends ActivityBase implements OnClickListener, OnMmiLocationListener
{

	private final int VIDEO_MAX_SIZE = 100 * 1024 * 1024;

	private EditText report_detail;
	private TextView report_image_progress;
	private VideoView mVideoView;
	private MediaController mVideoController;
	private TextView report_image_size;
	private Button btn_take, btn_native, btn_post, btn_image_clean;
	private boolean isUploading = false;
	private int videoSize = 0;
	private Uri videoUri = null;
	private String videoPath = "";
	private PopupWindow pwImage = null;
	
	private String taskId = null;
	private String taskName = null;

	@Override
	protected void onCreate(Bundle bundle)
	{
		// TODO Auto-generated method stub
		super.onCreate(bundle);
		setRequestedOrientation(Config.screenOrientation);
		setContentView(R.layout.activity_tool_report_as_vid);
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
		ivTitle.setText(R.string.talk_tools_report_vid);
		View btnLeft = findViewById(R.id.menu_left_button);
		ImageView ivLeft = (ImageView) findViewById(R.id.bottom_left_icon);
		ivLeft.setImageResource(ThemeUtil.getResourceId(R.attr.theme_ic_topbar_back, this) );
		btnLeft.setOnClickListener(this);

		RelativeLayout ivRightLay = (RelativeLayout) findViewById(R.id.talk_menu_right_button);
		ImageView ivRight = (ImageView) findViewById(R.id.bottom_right_icon);
		ivRight.setVisibility(View.GONE);
		ivRightLay.setVisibility(View.INVISIBLE);
		findViewById(R.id.report_item_panel).setOnClickListener(this);

		findViewById(R.id.report_image).setOnClickListener(this);
		mVideoView = (VideoView) findViewById(R.id.report_video);
		mVideoView.setOnClickListener(this);
		report_detail = (EditText) findViewById(R.id.report_detail);
		report_image_progress = (TextView) findViewById(R.id.report_image_progress);
		btn_image_clean = (Button) findViewById(R.id.report_image_clean);
		report_image_size = (TextView) findViewById(R.id.report_image_size);
		btn_take = (Button) findViewById(R.id.report_btn_take);
		btn_native = (Button) findViewById(R.id.report_btn_native);
		btn_post = (Button) findViewById(R.id.report_btn_post);
		btn_image_clean.setOnClickListener(this);
		btn_take.setOnClickListener(this);
		btn_native.setOnClickListener(this);
		btn_post.setOnClickListener(this);

		mVideoController = new MediaController(this);
		mVideoView.setMediaController(mVideoController);
	}

	private void refreshUI()
	{
		if (isUploading)
		{
			report_detail.setEnabled(false);
			report_image_progress.setVisibility(View.VISIBLE);
			btn_take.setEnabled(false);
			btn_native.setEnabled(false);
			btn_image_clean.setVisibility(View.GONE);
			report_image_size.setText(MenuReportActivity.sizeMKB(videoSize) + " (" + AirServices.iOperator.getFileExtension(videoPath) + ")");
		}
		else
		{
			report_detail.setEnabled(true);
			report_image_progress.setVisibility(View.GONE);
			btn_take.setEnabled(true);
			btn_native.setEnabled(true);
			if (!TextUtils.isEmpty(videoPath))
			{
				mVideoView.setVisibility(View.VISIBLE);
				btn_image_clean.setVisibility(View.VISIBLE);
				report_image_size.setText(MenuReportActivity.sizeMKB(videoSize) + " (" + AirServices.iOperator.getFileExtension(videoPath) + ")");
				report_image_size.setVisibility(View.VISIBLE);
			}
			else
			{
				mVideoView.setVisibility(View.GONE);
				btn_image_clean.setVisibility(View.GONE);
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
				if (videoPath.equals(""))
				{
					Util.Toast(this, getString(R.string.talk_report_upload_vid_err_select_pic));
					break;
				}
				isUploading = true;
				Util.hideSoftInput(this);
				refreshUI();

				report_image_progress.setText(getString(R.string.talk_report_upload_getting_gps));
				AirLocation.getInstance(this).onceGet(this, 30);
				break;
			}
			case R.id.report_image:
			case R.id.report_btn_take:
			{
//				Intent i = new Intent(android.provider.MediaStore.ACTION_VIDEO_CAPTURE);
//				gUri = getOutputMediaFile();
//				if(gUri != null)
//					i.putExtra(MediaStore.EXTRA_OUTPUT, gUri);
//				i.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
//				i.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 60);
//				startActivityForResult(i, Const.image_select.REQUEST_CODE_CREATE_VIDEO);
				
				// TODO Auto-generated method stub
				Intent serverIntent = new Intent(MenuReportAsVidActivity.this, VideoCamera.class);
				startActivityForResult(serverIntent, Const.image_select.REQUEST_CODE_CREATE_VIDEO);
				break;
			}
			case R.id.report_btn_native:
			{
				String status = Environment.getExternalStorageState();
				if (!status.equals(Environment.MEDIA_MOUNTED))
				{
					Util.Toast(this, getString(R.string.talk_insert_sd_card));
					return;
				}
				Intent localIntent = new Intent("android.intent.action.GET_CONTENT", null);
				localIntent.setType("video/*");
				startActivityForResult(localIntent, Const.image_select.REQUEST_CODE_BROWSE_VIDEO);
				break;
			}

			case R.id.report_video:
			{
				break;
			}
			case R.id.image_video:
			{
				if (pwImage != null)
					pwImage.dismiss();
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
				mVideoView.setVisibility(View.GONE);
				mVideoView.stopPlayback();
//				mVideoView.setVideoPath("");
				videoPath ="";
				btn_image_clean.setVisibility(View.GONE);
				videoUri = null;
				refreshUI();
				break;
			}
		}
	}
	
	Uri gUri;
	public Uri getOutputMediaFile()
    {
        // To be safe, you should check that the SDCard is mounted

        if(Environment.getExternalStorageState() != null) {
            // this works for Android 2.2 and above
            File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "SMW_VIDEO");

            // This location works best if you want the created images to be shared
            // between applications and persist after your app has been uninstalled.

            // Create the storage directory if it does not exist
            if (! mediaStorageDir.exists()) {
                if (! mediaStorageDir.mkdirs()) {
                    Log.d(MenuReportAsVidActivity.class, "failed to create directory");
                    return null;
                }
            }

            // Create a media file name
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File mediaFile;
           
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                "VID_"+ timeStamp + ".mp4");
         

            return Uri.fromFile(mediaFile);
        }

        return null;
    }

	
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != RESULT_OK)
			return;
		Uri uri =null;
		switch (requestCode)
		{
			case Const.image_select.REQUEST_CODE_CREATE_VIDEO:
			{
//				uri = this.gUri;
//				String filePath = uri.getPath();
				String filePath = data.getExtras().getString(VideoCamera.EXTRA_VIDEO_PATH);
				if (filePath != null)
				{
					int size = AirServices.iOperator.getFileSize("", filePath, true);
					if (size <= VIDEO_MAX_SIZE)
					{
						videoUri = uri;
						videoPath = filePath;
						videoSize = size;
//						mVideoView.setVideoURI(uri);
						mVideoView.setVideoPath(videoPath);
						mVideoView.start();
						refreshUI();
					}
					else
					{
						Util.Toast(this, getString(R.string.talk_report_upload_vid_err_size));
					}
				}
				
				break;
			}
			case Const.image_select.REQUEST_CODE_BROWSE_VIDEO:
			{

				String filePath = UriUtil.getPath(this, data.getData());
				if (filePath != null)
				{
					int size  = AirServices.iOperator.getFileSize("", filePath, true);
					if(size <= VIDEO_MAX_SIZE)
					{
						videoUri = data.getData();
						videoPath = filePath;
						videoSize = size;
						mVideoView.setVideoPath(filePath);
						mVideoView.start();
						refreshUI();
					}
					else
					{
						Util.Toast(this, getString(R.string.talk_report_upload_vid_err_size));
					}
				}
				else
				{
					Util.Toast(this, getString(R.string.talk_report_upload_vid_err_select_pic));
				}
			}
			default:
				break;
		}
	}

	@Override
	public void onLocationChanged(boolean isOk, int id, int type, double latitude, double longitude, double altitude, float speed, String time)
	{
		// TODO Auto-generated method stub
		if (isUploading && id == AirLocation.AIR_LOCATION_ID_ONCE)
		{
			String detail = report_detail.getText().toString();
			File file = new File(videoPath);
			try
			{
				Date date = new Date(file.lastModified());
				if (date != null)
				{
					if (!TextUtils.isEmpty(detail))
						detail += "\r\n\r\n";
					
					SimpleDateFormat sfd = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
					String date_string = sfd.format(date);
					detail += getString(R.string.talk_report_upload_capture_time) + " [" + date_string + "]";
				}
			}
			catch (Exception e)
			{
				detail = report_detail.getText().toString();
			}

			String resTypeExtension = AirServices.iOperator.getFileExtension(videoPath);
			if (Utils.isEmpty(resTypeExtension))
			{
				resTypeExtension = "3gp";
			}
			Log.i(MenuReportAsVidActivity.class, "VideoPicture: TASK[" + taskId + "][" + taskName + "] text=[" + report_detail.getText().toString() + "] x=[" + latitude + "] y=[" + longitude + "]");
			AirReportManager.getInstance().Report(taskId, taskName, AirtalkeeReport.RESOURCE_TYPE_VIDEO, resTypeExtension, videoUri, videoPath, detail, videoSize,
				latitude, longitude);

			isUploading = false;
			finish();
		}
	}

}
