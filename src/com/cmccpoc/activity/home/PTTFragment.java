package com.cmccpoc.activity.home;

import java.util.Locale;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.AirtalkeeMediaVisualizer;
import com.airtalkee.sdk.AirtalkeeMessage;
import com.airtalkee.sdk.AirtalkeeSessionManager;
import com.airtalkee.sdk.OnMediaAudioVisualizerListener;
import com.airtalkee.sdk.controller.SessionController;
import com.airtalkee.sdk.entity.AirFunctionSetting;
import com.airtalkee.sdk.entity.AirMessage;
import com.airtalkee.sdk.entity.AirSession;
import com.airtalkee.sdk.util.Utils;
import com.cmccpoc.R;
import com.cmccpoc.Util.Const;
import com.cmccpoc.Util.DateUtils;
import com.cmccpoc.Util.Util;
import com.cmccpoc.activity.MenuReportAsPicActivity;
import com.cmccpoc.activity.VideoSessionActivity;
import com.cmccpoc.activity.home.widget.AlertDialog;
import com.cmccpoc.activity.home.widget.AlertDialog.DialogListener;
import com.cmccpoc.activity.home.widget.CallAlertDialog;
import com.cmccpoc.activity.home.widget.CallAlertDialog.OnAlertDialogCancelListener;
import com.cmccpoc.config.Config;
import com.cmccpoc.listener.OnMmiLocationListener;
import com.cmccpoc.location.AirLocation;
import com.cmccpoc.widget.AudioVisualizerView;
import com.cmccpoc.widget.VideoCamera;

/**
 * 三大fragment之一：PTT对讲Fragment。
 * 主要包含音柱显示、上报、实时视频传输与呼叫中心。
 * 上方显示最新一条的PTT对讲记录
 * @author Yao
 *
 */
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
	private RelativeLayout ivSpetrum;

	AlertDialog dialog;

	private static PTTFragment mInstance;
	/**
	 * 获取PTTFragment实例对象
	 * @return
	 */
	public static PTTFragment getInstance()
	{
		return mInstance;
	}

	/**
	 * 获取视频Pannel View
	 * @return
	 */
	public View getVideoPannel()
	{
		return videoPannel;
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
		ivSpetrum = (RelativeLayout) findViewById(R.id.iv_spetrum_lay);
		refreshPlayback();
		return v;
	}

	public RelativeLayout getIvSpetrum()
	{
		return ivSpetrum;
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

	/**
	 * 设置session会话
	 * @param s 会话Entity
	 */
	public void setSession(AirSession s)
	{
		this.session = s;
		AirtalkeeMediaVisualizer.getInstance().setOnMediaAudioVisualizerListener(this);
	}

	/**
	 * 呼叫中心
	 * 若调度台不在线，则会弹出窗口，提示是否前去直接留言
	 */
	private void callStationCenter()
	{
		if (Config.funcCenterCall == AirFunctionSetting.SETTING_ENABLE)
		{
			if (AirtalkeeAccount.getInstance().isAccountRunning())
			{
				if (AirtalkeeAccount.getInstance().isEngineRunning())
				{
					AirLocation.getInstance(getActivity()).onceGet(new OnMmiLocationListener()
					{

						@Override
						public void onLocationChanged(boolean isOk, int id, int type, double latitude, double longitude, double altitude, float speed, String time, String address)
						{
							// TODO Auto-generated method stub
						}

						@Override
						public void onLocationChanged(boolean isOk, int id, int type, double latitude, double longitude, double altitude, float speed, String time)
						{
							// TODO Auto-generated method stub
						}
					}, 20);
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
							// checkNewIM(false);
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

	/**
	 * 刷新PTT语音留存
	 */
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
			String datetime = msg.getDate().replace("年", "-").replace("月", "-").replace("日", "") + " " + msg.getTime();
			recPlaybackTime.setText(DateUtils.getTimestampString(DateUtils.StringToDate(datetime, "yyyy-MM-dd HH:mm:ss"), Locale.getDefault()));
			recPlayback.setVisibility(View.VISIBLE);
			recPlaybackNone.setVisibility(View.GONE);
			if (msg.getState() == AirMessage.STATE_NEW)
			{
				recPlaybackNew.setVisibility(View.GONE);
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

	/**
	 * 刷新PTT语音留存
	 * @param session 会话Entity
	 */
	public void refreshPlayback(AirSession session)
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
			{
				callStationCenter();
				break;
			}
			case DIALOG_2_SEND_MESSAGE:
			{
				if (obj != null)
				{
					String sessionCode = obj.toString();
					AirtalkeeMessage.getInstance().MessageRecordPlayStop();
					AirtalkeeSessionManager.getInstance().getSessionByCode(sessionCode);
					HomeActivity.getInstance().pageIndex = BaseActivity.PAGE_IM;
					HomeActivity.getInstance().onViewChanged(sessionCode);
					HomeActivity.getInstance().panelCollapsed();
				}
				break;
			}
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

	/**
	 * 设置上报区域可见性
	 * @param visiblility 是否可见
	 */
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

}
