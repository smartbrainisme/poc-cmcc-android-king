package com.airtalkee.activity.home;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.airtalkee.R;
import com.airtalkee.Util.Const;
import com.airtalkee.Util.Util;
import com.airtalkee.activity.MenuReportAsPicActivity;
import com.airtalkee.activity.VideoSessionActivity;
import com.airtalkee.activity.home.widget.AlertDialog;
import com.airtalkee.activity.home.widget.AlertDialog.DialogListener;
import com.airtalkee.activity.home.widget.CallAlertDialog;
import com.airtalkee.activity.home.widget.CallAlertDialog.OnAlertDialogCancelListener;
import com.airtalkee.config.Config;
import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.AirtalkeeChannel;
import com.airtalkee.sdk.AirtalkeeMediaVisualizer;
import com.airtalkee.sdk.AirtalkeeMessage;
import com.airtalkee.sdk.AirtalkeeSessionManager;
import com.airtalkee.sdk.OnMediaAudioVisualizerListener;
import com.airtalkee.sdk.controller.SessionController;
import com.airtalkee.sdk.entity.AirChannel;
import com.airtalkee.sdk.entity.AirFunctionSetting;
import com.airtalkee.sdk.entity.AirMessage;
import com.airtalkee.sdk.entity.AirSession;
import com.airtalkee.sdk.util.Utils;
import com.airtalkee.services.AirServices;
import com.airtalkee.widget.AudioVisualizerView;
import com.airtalkee.widget.VideoCamera;

public class PTTFragment extends BaseFragment implements OnClickListener, DialogListener, OnMediaAudioVisualizerListener
{

	private static final int DIALOG_CALL_CENTER_CONFIRM = 100;
	private static final int DIALOG_CALL_CENTER = 101;
	private static final int DIALOG_2_SEND_MESSAGE = 102;
	private LinearLayout recPlayback;
	private ImageView recPlaybackIcon;
	private TextView recPlaybackUser;
	private TextView recPlaybackSeconds;
	private TextView recPlaybackTime;
	private TextView recPlaybackNone;
	private ImageView recPlaybackNew;
	private View videoPannel;
	private AirSession session = null;
	private AirMessage currentMessage;
	private AlertDialog alertDialog;
	public static final int mVisualizerSpectrumNum = 18;
	private AudioVisualizerView mVisualizerView;

	AlertDialog dialog;

	private static PTTFragment mInstance;

	public static PTTFragment getInstance()
	{
		return mInstance;
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		mInstance = this;
	}

	@Override
	public void onResume()
	{
		// TODO Auto-generated method stub
		super.onResume();
		setSession(getSession());
	}

	@Override
	public void onPause()
	{
		// TODO Auto-generated method stub
		super.onPause();
		if (HomeActivity.getInstance().pageIndex == HomeActivity.PAGE_PTT)
			setViedoReportPannelVisiblity(View.GONE);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		v = inflater.inflate(getLayout(), container, false);

		findViewById(R.id.talk_playback).setOnClickListener(this);
		findViewById(R.id.btn_close).setOnClickListener(this);
		findViewById(R.id.btn_image).setOnClickListener(this);
		findViewById(R.id.btn_camera).setOnClickListener(this);
		findViewById(R.id.btn_video).setOnClickListener(this);

		videoPannel = findViewById(R.id.video_pannel);
		recPlayback = (LinearLayout) findViewById(R.id.talk_playback_panel);
		recPlaybackIcon = (ImageView) findViewById(R.id.talk_playback_icon);
		recPlaybackUser = (TextView) findViewById(R.id.talk_playback_user);
		recPlaybackSeconds = (TextView) findViewById(R.id.talk_playback_seconds);
		recPlaybackTime = (TextView) findViewById(R.id.talk_playback_time);
		recPlaybackNone = (TextView) findViewById(R.id.talk_playback_none);
		recPlaybackNew = (ImageView) findViewById(R.id.talk_playback_user_unread);
		mVisualizerView = (AudioVisualizerView) findViewById(R.id.talk_audio_visualizer_new);
		mVisualizerView.setSpectrumNum(mVisualizerSpectrumNum);
		refreshPlayback();
		return v;
	}

	@Override
	public int getLayout()
	{
		// TODO Auto-generated method stub
		return R.layout.frag_ptt_layout;
	}

	@Override
	public void dispatchBarClickEvent(int page, int id)
	{
		// TODO Auto-generated method stub
		if (page == HomeActivity.PAGE_PTT)
		{
			// TODO Auto-generated method stub
			switch (id)
			{
				case R.id.bar_left:
					setViedoReportPannelVisiblity(View.VISIBLE);
					break;
				case R.id.bar_mid:// 实时视频回传
					if (session != null)
					{
						Intent intent = new Intent();
						intent.setClass(getActivity(), VideoSessionActivity.class);
						intent.putExtra("sessionCode", session.getSessionCode());
						intent.putExtra("video", true);
						startActivity(intent);
					}
					break;
				case R.id.bar_right:
					dialog = new AlertDialog(getActivity(), getString(R.string.talk_tools_call_center_confirm), null, this, DIALOG_CALL_CENTER_CONFIRM);
					dialog.show();
					break;
			}
		}
	}

	public void setSession(AirSession s)
	{
		this.session = s;
		AirtalkeeMediaVisualizer.getInstance().setOnMediaAudioVisualizerListener(this);
	}

	private void callStationCenter()
	{
		if (Config.funcCenterCall == AirFunctionSetting.SETTING_ENABLE)
		{
			if (AirtalkeeAccount.getInstance().isAccountRunning())
			{
				if (AirtalkeeAccount.getInstance().isEngineRunning())
				{
					final AirSession s = SessionController.SessionMatchSpecial(AirtalkeeSessionManager.SPECIAL_NUMBER_DISPATCHER, getString(R.string.talk_tools_call_center));
					if (s != null)
					{
						alertDialog = new CallAlertDialog(getActivity(), "正在呼叫" + s.getDisplayName(), "请稍后...", s.getSessionCode(), DIALOG_CALL_CENTER, new OnAlertDialogCancelListener()
						{
							@Override
							public void onDialogCancel(int reason)
							{
								// TODO Auto-generated method stub
								switch (reason)
								{
									case AirSession.SESSION_RELEASE_REASON_NOTREACH:
										dialog = new AlertDialog(getActivity(), null, getString(R.string.talk_call_offline_tip), getString(R.string.talk_session_call_cancel), getString(R.string.talk_call_leave_msg), PTTFragment.this, DIALOG_2_SEND_MESSAGE, s.getSessionCode());
										dialog.show();
										break;
									default:
										break;
								}
							}
						});
						alertDialog.show();
					}
				}
				else
				{
					Util.Toast(getActivity(), getString(R.string.talk_network_warning));
				}
			}
		}
		else if (Config.funcCenterCall == AirFunctionSetting.SETTING_CALL_NUMBER && !Utils.isEmpty(Config.funcCenterCallNumber))
		{
			Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + Config.funcCenterCallNumber));
			getActivity().startActivity(intent);
		}
	}

	@Override
	public void onClick(View v)
	{
		// TODO Auto-generated method stub
		switch (v.getId())
		{
			case R.id.talk_playback:
			{
				if (session != null && session.getMessagePlayback() != null)
				{
					currentMessage = session.getMessagePlayback();
					if (currentMessage.isRecordPlaying())
					{
						AirtalkeeMessage.getInstance().MessageRecordPlayStop();
					}
					else
					{
						AirtalkeeMessage.getInstance().MessageRecordPlayStart(currentMessage);
						if (currentMessage.getState() == AirMessage.STATE_NEW)
						{
							session.setMessageUnreadCount(session.getMessageUnreadCount() - 1);
//							checkNewIM(false);
						}
					}
				}
				break;
			}
			case R.id.btn_close:
				setViedoReportPannelVisiblity(View.GONE);
				break;
			case R.id.btn_image:
				Intent itImage = new Intent(getActivity(), MenuReportAsPicActivity.class);
				itImage.putExtra("type", "image");
				startActivity(itImage);
				break;
			case R.id.btn_camera:
				Intent itCamera = new Intent(getActivity(), MenuReportAsPicActivity.class);
				itCamera.putExtra("type", "camera");
				startActivity(itCamera);
				break;
			case R.id.btn_video:
				Intent serverIntent = new Intent(getActivity(), VideoCamera.class);
				startActivityForResult(serverIntent, Const.image_select.REQUEST_CODE_CREATE_VIDEO);
				break;
			default:
				break;
		}
	}

	public void refreshPlayback()
	{
		if (session != null && session.getMessagePlayback() != null)
		{
			AirMessage msg = session.getMessagePlayback();
			if (msg.isRecordPlaying())
			{
				recPlaybackIcon.setImageResource(R.drawable.msg_audio_stop);
			}
			else
			{
				recPlaybackIcon.setImageResource(R.drawable.msg_audio_play);
			}
			if (TextUtils.equals(msg.getIpocidFrom(), AirtalkeeAccount.getInstance().getUserId()))
				recPlaybackUser.setText(getString(R.string.talk_me));
			else
				recPlaybackUser.setText(msg.getInameFrom());
			recPlaybackSeconds.setText(msg.getImageLength() + "''");
			recPlaybackTime.setText(msg.getTime());
			recPlayback.setVisibility(View.VISIBLE);
			recPlaybackNone.setVisibility(View.GONE);
			if (msg.getState() == AirMessage.STATE_NEW)
			{
				recPlaybackNew.setVisibility(View.VISIBLE);
			}
			else
			{
				recPlaybackNew.setVisibility(View.GONE);
			}
		}
		else
		{
			recPlaybackIcon.setImageResource(R.drawable.msg_audio_play);
			recPlaybackUser.setText("");
			recPlaybackSeconds.setText("");
			recPlaybackTime.setText("");
			recPlayback.setVisibility(View.GONE);
			recPlaybackNone.setVisibility(View.VISIBLE);
			recPlaybackNew.setVisibility(View.GONE);
		}
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
	{
		// TODO Auto-generated method stub
		if (key.equals(SESSION_EVENT_KEY))
		{
			if (null != session && session.getMessagePlayback() != null)
			{
				refreshPlayback();
			}
		}

	}

	@Override
	public void onClickOk(int id, Object obj)
	{
		// TODO Auto-generated method stub
		switch (id)
		{
			case DIALOG_CALL_CENTER_CONFIRM:
				callStationCenter();
				break;
			case DIALOG_2_SEND_MESSAGE:
				if (obj != null)
				{
					String sessionCode = obj.toString();
					Intent it = new Intent(getActivity(), SessionDialogActivity.class);
					it.putExtra("sessionCode", sessionCode);
					it.putExtra("type", AirServices.TEMP_SESSION_TYPE_MESSAGE);
					getActivity().startActivity(it);
				}
				break;
		}
	}

	@Override
	public void onClickOk(int id, boolean isChecked)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void onClickCancel(int id)
	{
		// TODO Auto-generated method stub

	}

	private void setViedoReportPannelVisiblity(int visiblility)
	{
		if (visiblility == View.GONE)
		{
			if (videoPannel != null)
				videoPannel.setVisibility(View.GONE);
			if (mediaStatusBar != null)
				mediaStatusBar.setMediaStatusBarVisibility(View.VISIBLE);
		}
		else
		{
			if (videoPannel != null)
				videoPannel.setVisibility(View.VISIBLE);
			if (mediaStatusBar != null)
				mediaStatusBar.setMediaStatusBarVisibility(View.GONE);
		}
	}

	public void pictureQualitySelect(final int id)
	{
		/*
		 * new android.app.AlertDialog.Builder(this).setTitle(R.string.
		 * talk_quality_select).setItems(R.array.picture_quality, new
		 * DialogInterface.OnClickListener() {
		 * 
		 * @Override public void onClick(DialogInterface dialog, int which) {
		 * isHighQuality = which == 0; switch (id) { case R.id.report_image:
		 * case R.id.report_btn_take: { picPathTemp =
		 * Util.getImageTempFileName(); picUriTemp = Uri.fromFile(new
		 * File(picPathTemp)); Intent i = new
		 * Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
		 * i.putExtra(MediaStore.EXTRA_OUTPUT, picUriTemp);
		 * startActivityForResult(i,
		 * Const.image_select.REQUEST_CODE_CREATE_IMAGE); break; } case
		 * R.id.report_btn_native: { String status =
		 * Environment.getExternalStorageState(); if
		 * (!status.equals(Environment.MEDIA_MOUNTED)) {
		 * Util.Toast(MenuReportAsPicActivity.this,
		 * getString(R.string.talk_insert_sd_card)); return; } Intent
		 * localIntent = new Intent("android.intent.action.GET_CONTENT", null);
		 * localIntent.setType("image/*"); startActivityForResult(localIntent,
		 * Const.image_select.REQUEST_CODE_BROWSE_IMAGE); break; } } }
		 * }).show();
		 */
	}

	@Override
	public void onListItemLongClick(int id, int selectedItem)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onMediaAudioVisualizerChanged(byte[] values, int spectrumNum)
	{
		mVisualizerView.updateVisualizer(values);
	}

	private void checkNewIM(boolean toClean)
	{
		int count = 0;
		if (session != null)
		{
			int type = session.getType();
			if (type == AirSession.TYPE_CHANNEL)
			{
				AirChannel channel = AirtalkeeChannel.getInstance().ChannelGetByCode(session.getSessionCode());
				if (channel != null)
				{
					if (toClean)
					{
						channel.msgUnReadCountClean();
					}
					count = channel.getMsgUnReadCount();
				}
			}
			else if (type == AirSession.TYPE_DIALOG)
			{
				if (toClean)
				{
					session.setMessageUnreadCount(0);
				}
				count = session.getMessageUnreadCount();
			}
		}
		if (count > 0)
		{
//			ivIMNew.setVisibility(View.VISIBLE);
		}
		else
		{
//			ivIMNew.setVisibility(View.GONE);
		}
	}
}
